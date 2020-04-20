package com.socketrelay.messages;

import java.io.Serializable;

import com.socketrelay.client.beans.Game;

public class HostConfiguration implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Game game;

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
}
