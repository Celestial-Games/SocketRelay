package com.socketrelay.messages;

import java.io.Serializable;

import com.socketrelay.client.beans.Game;

public class HostConfiguration implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Game game;
	private String password;

	public HostConfiguration() {
	}

	public HostConfiguration(Game game) {
		super();
		this.game=game;
	}

	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
}
