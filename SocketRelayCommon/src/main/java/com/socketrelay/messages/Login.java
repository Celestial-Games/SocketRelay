package com.socketrelay.messages;

import java.io.Serializable;

public class Login implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum Type{
		Player,
		Host
	}
	
	private Type type;
	private String sessionTicket;
	private String gameSessionId;
	
	public Login() {
	}
	
	public Type getType() {
		return type;
	}
	
	public void setType(Type type) {
		this.type = type;
	}

	public String getSessionTicket() {
		return sessionTicket;
	}

	public void setSessionTicket(String sessionTicket) {
		this.sessionTicket = sessionTicket;
	}

	public String getGameSessionId() {
		return gameSessionId;
	}

	public void setGameSessionId(String gameSessionId) {
		this.gameSessionId = gameSessionId;
	}
	
	
}
