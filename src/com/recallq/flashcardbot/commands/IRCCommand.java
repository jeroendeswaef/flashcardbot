package com.recallq.flashcardbot.commands;

/**
 * http://blog.initprogram.com/2010/10/14/a-quick-basic-primer-on-the-irc-protocol/
 */
public abstract class IRCCommand {
	public abstract String getLine();
}