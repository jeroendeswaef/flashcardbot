package com.recallq.flashcardbot.commands;

public class Sender {
	private String nick;
	private String address;

	/**
	 * @param source f.e. nick!022434f4@gateway/web/freenode/ip.2.36.52.244
	 */
	public Sender(String source) {
		String[] parts = source.split("!");
		nick = parts[0];
		if (parts.length > 1) {
			address = parts[1];
		}
	}

	public String getNick() {
		return nick;
	}
}