package com.socketrelay.host;

import java.io.Serializable;

public class SocketRelayConfig implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private int serverport=10000;
	private String serverIp="0.0.0.0";
	
	private int clientLow=10001;
	private int clientHigh=12000;
	
	public int getServerport() {
		return serverport;
	}
	
	public void setServerport(int serverport) {
		this.serverport = serverport;
	}
	
	public String getServerIp() {
		return serverIp;
	}
	
	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}
	
	public int getClientLow() {
		return clientLow;
	}
	
	public void setClientLow(int clientLow) {
		this.clientLow = clientLow;
	}
	
	public int getClientHigh() {
		return clientHigh;
	}
	
	public void setClientHigh(int clientHigh) {
		this.clientHigh = clientHigh;
	}
}
