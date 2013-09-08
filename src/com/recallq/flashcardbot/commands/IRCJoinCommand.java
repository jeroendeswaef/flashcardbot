package com.recallq.flashcardbot.commands;

public class IRCJoinCommand extends IRCCommonCommand {
	public static final String ACTION = "JOIN";

	public IRCJoinCommand(String destination) {
		super(destination);
	}

	public IRCJoinCommand(Sender source, String destination) {
		super(source, destination, "");
	}

	@Override
	public String getActionValue() {
		return ACTION;
	}

	public static IRCCommand createCommand(String line) {
		String[] parts = line.split(" ");
		IRCCommand returnCommand = null;
		if (parts.length > 2 && ACTION.equals(parts[1]) && parts[0].startsWith(":")) {
			String destination = parts[2];
			String sourceStr = parts[0];
			sourceStr = sourceStr.replace(":", "");
			Sender source = new Sender(sourceStr);
			returnCommand = new IRCJoinCommand(source, destination);
		}
		return returnCommand;
	}

}