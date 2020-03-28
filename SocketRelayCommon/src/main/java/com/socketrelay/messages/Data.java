package com.socketrelay.messages;

import java.io.Serializable;

public class Data implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String clientId;
	private byte[] data;
	
	public Data() {
	}
	
	public Data(String clientId, byte[] data) {
		super();
		this.clientId = clientId;
		this.data = data;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
}
