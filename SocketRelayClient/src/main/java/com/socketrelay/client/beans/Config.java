package com.socketrelay.client.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Config implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private List<Server> servers=new ArrayList<>();
	private List<Game> games=new ArrayList<>();
	
	public Config() {
	}

	public List<Server> getServers() {
		return servers;
	}

	public void setServers(List<Server> servers) {
		this.servers = servers;
	}

	public List<Game> getGames() {
		return games;
	}

	public void setGames(List<Game> games) {
		this.games = games;
	}
}
