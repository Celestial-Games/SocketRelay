package com.socketrelay.messages;

import java.io.Serializable;

public class Configuration implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int clientPort=0;

	public Configuration() {
	}

	public Configuration(int clientPort) {
		super();
		this.clientPort = clientPort;
	}

	public int getClientPort() {
		return clientPort;
	}

	public void setClientPort(int clientPort) {
		this.clientPort = clientPort;
	}
}
