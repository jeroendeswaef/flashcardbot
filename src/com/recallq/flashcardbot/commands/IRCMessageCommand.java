package com.recallq.flashcardbot.commands;

public class IRCMessageCommand extends IRCCommonCommand {
	public static final String ACTION = "PRIVMSG";

	public IRCMessageCommand(Sender sender, String destination, String message) {
		super(sender, destination, message);
	}

	public IRCMessageCommand(String destination, String message) {
		super(destination, message);
	}

	@Override
	public String getActionValue() {
		return ACTION;
	}

	public static IRCCommand createCommand(String line) {
		String[] parts = line.split(" ");
		IRCCommand returnCommand = null;
		if (parts.length > 3 && ACTION.equals(parts[1]) && parts[0].startsWith(":") && parts[3].startsWith(":")) {
			String destination = parts[2];
			String sourceStr = parts[0];
			sourceStr = sourceStr.replace(":", "");
			Sender source = new Sender(sourceStr);
			// taking into account that the message itself may contain spaces, so part[3] may not contain
			// the full message
			// + 3 for the spaces between the parts
			int messageStartIndex = parts[0].length() + parts[1].length() + parts[2].length() + 3;
			// + 1 to ignore the ":" at the start of the message
			String message = line.substring(messageStartIndex + 1, line.length());
			Sender sender = new Sender(sourceStr);
			returnCommand = new IRCMessageCommand(sender, "", message);
		}
		return returnCommand;
	}

}