package com.socketrelay.client.beans;

import java.io.Serializable;

public class Game implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public enum Protocol {
		TCP,
		UDP;
	}
	
	private int port;
	private String name;
	private Protocol protocol=Protocol.TCP;
	
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

	public Protocol getProtocol() {
		return protocol;
	}

	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
	}
}
