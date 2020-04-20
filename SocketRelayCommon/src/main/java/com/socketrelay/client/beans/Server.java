package com.socketrelay.client.beans;

import java.io.Serializable;

public class Server implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String ip;
	private int port;
	private String name;
	
	public Server() {
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
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
