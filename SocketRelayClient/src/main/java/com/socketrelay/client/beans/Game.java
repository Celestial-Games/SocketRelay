package com.socketrelay.client.beans;

import java.io.Serializable;

public class Game implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int port;
	private String name;

	public Game() {
		super();
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
