package com.recallq.flashcardbot;

import java.util.Map;
import java.util.HashMap;
import java.util.Random;

import java.net.Socket;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

import com.recallq.flashcardbot.commands.*;
import java.util.ResourceBundle;

/**
 * TODO - remove hardcoded links - add junit tests - add README
 *
 */
public class FlashCardBot {

    private static final Logger logger = Logger.getLogger(FlashCardBot.class);

    // map of Question -> Answer
    private Map<String, String> cards = new HashMap<String, String>();
    private Object[] cardKeys = null;
    // map of questions currently asked for connected users. User -> Question
    private Map<String, String> currentQuestions = new HashMap<String, String>();

    private Connection connection = null;

    private static void writeCommand(BufferedWriter writer, String command) throws IOException {
        writer.write(command + "\r\n");
        writer.flush();
    }

    /**
     * @return The next card to practise. For the moment takes a random element.
     *
     * For the future: implement an algorithm based on the cards practised in
     * the past by the user.
     */
    public String getNextCard() {
        if (cardKeys.length > 0) {
            Random generator = new Random();
            String randomKey = (String) cardKeys[generator.nextInt(cardKeys.length)];
            return randomKey;
        }
        return null;
    }

    public void askQuestion(BufferedWriter writer, String user) throws IOException {
        String nextQuestion = getNextCard();
        IRCCommand questionCommand = new IRCMessageCommand(user, nextQuestion);
        currentQuestions.put(user, nextQuestion);
        writeCommand(writer, questionCommand.getLine());
    }

    public void checkAnswer(BufferedWriter writer, String user, String answer) throws IOException {
        String currentQuestion = currentQuestions.get(user);
        if (currentQuestion != null) {
            String correctAnswer = cards.get(currentQuestion);
            String response;
            if (correctAnswer.toUpperCase().equals(answer.toUpperCase())) {
                response = "Correct";
            } else {
                response = String.format("False, the correct answer is: %s", correctAnswer);
            }
            IRCCommand responseCommand = new IRCMessageCommand(user, response);
            writeCommand(writer, responseCommand.getLine());
            askQuestion(writer, user);
        }
    }

    /**
     * load the cards map from the db.
     */
    private void loadCards() throws Exception {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM CARD;");
        long cardCount = 0L;
        while (rs.next()) {
            String question = rs.getString("question");
            String answer = rs.getString("answer");
            cards.put(question, answer);
            cardCount++;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Loaded %d cards into memory", cardCount));
        }
        cardKeys = cards.keySet().toArray();
        rs.close();
        stmt.close();
    }

    public FlashCardBot(ResourceBundle rb) {
        try {
            Class.forName(rb.getString("dbdriver"));
            final String dbConnection = rb.getString("dbconnection");
            connection = DriverManager.getConnection(dbConnection);
            logger.info(String.format("Opened database %s successfully", dbConnection));
            loadCards();
            connection.close();
        } catch (Exception e) {
            logger.error(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        String configSuffix;
        if (args.length > 0) {
            configSuffix = ("-" + args[0]);
        } else {
            configSuffix = new String();
        }
        final String bundleName = "config" + configSuffix;
        ResourceBundle rb = ResourceBundle.getBundle(bundleName);
        logger.info("Loading configuration from bundle: " + bundleName);
        
        FlashCardBot flashcardbot = new FlashCardBot(rb);
        String server = "irc.freenode.net";
        String nick = "flashcardbot";
        String login = "flashcardbot";

        String channel = "#learncapitals";

        Socket socket = new Socket(server, 6667);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        writer.write("NICK " + nick + "\r\n");
        writer.write("USER " + login + "8 * Flash Card Bot\r\n");
        writer.flush();

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.indexOf("004") >= 0) {
                logger.info(String.format("Logged in as %s", nick));
                break;
            } else if (line.indexOf("433") >= 0) {
                logger.error(String.format("Nickname %s is already in use", nick));
                return;
            }
        }

        IRCJoinCommand joinCommand = new IRCJoinCommand(channel);
        writeCommand(writer, joinCommand.getLine());

        while ((line = reader.readLine()) != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Got << " + line);
            }
            IRCCommand command;
            String answerLine = null;
            if ((command = IRCPingCommand.createCommand(line)) != null) {
                answerLine = ((IRCPingCommand) command).getLine();
            }
            if ((command = IRCJoinCommand.createCommand(line)) != null) {
                String user = ((IRCJoinCommand) command).getSource().getNick();
                if (!nick.equals(user)) {
                    logger.info("Joined " + user);
                    flashcardbot.askQuestion(writer, user);
                }
            } else if ((command = IRCMessageCommand.createCommand(line)) != null) {
                IRCMessageCommand messageCommand = (IRCMessageCommand) command;
                String user = messageCommand.getSource().getNick();
                flashcardbot.checkAnswer(writer, user, messageCommand.getMessage());
            }
            if (answerLine != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Answer >> " + answerLine);
                }
                writeCommand(writer, answerLine);
            }
        }

    }
}
