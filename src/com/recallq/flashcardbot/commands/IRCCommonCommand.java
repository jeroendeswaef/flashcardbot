package com.recallq.flashcardbot.commands;

/**
 * Represents a command in the form :source ACTION destination (:message)
 * The message is optional.
 *
 * f.e. :Macha!~macha@unaffiliated/macha PRIVMSG #botwar :Test response
 * f.e. :Macha!~macha@unaffiliated/macha JOIN #botwar
 *
 * Note that the source is only known when receiving the message, not when sending.
 */
public abstract class IRCCommonCommand extends IRCCommand {
	private Sender source;
	private String destination;
	private String message;

	public IRCCommonCommand(String destination) {
		this.destination = destination;
	}

	public IRCCommonCommand(String destination, String message) {
		this.destination = destination;
		this.message = message;
	}

	public IRCCommonCommand(Sender source, String destination, String message) {
		this.source = source;
		this.destination = destination;
		this.message = message;
	}

	public Sender getSource() {
		return source;
	}

	public String getMessage() {
		return message;
	}
	
	public abstract String getActionValue();

	@Override
	public String getLine() {
		StringBuilder builder = new StringBuilder();

		builder.append(getActionValue());
		builder.append(" ");

		builder.append(destination);
		builder.append(" ");

		if (message != null) {
			builder.append(":");
			builder.append(message);
		}

		return builder.toString();
	}
}