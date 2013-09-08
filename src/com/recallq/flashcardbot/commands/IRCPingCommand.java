package com.recallq.flashcardbot.commands;

public class IRCPingCommand extends IRCCommand {
	private String message;

	public IRCPingCommand(String message) {
		this.message = message;
	}

	@Override
	public String getLine() {
		return ("PONG " + message);
	}

	public static IRCCommand createCommand(String line) {
		IRCCommand returnCommand = null;
		if (line.toUpperCase().startsWith("PING ")) {
			String message = line.substring(5);
			returnCommand = new IRCPingCommand(message);
		}
		return returnCommand;
	}
}
