package com.socketrelay.messages;

public class LoginToGame extends Login {
	private static final long serialVersionUID = 1L;

	private String sessionName;
	private String sessionPassword;
	
	public String getSessionName() {
		return sessionName;
	}
	
	public void setSessionName(String sessionName) {
		this.sessionName = sessionName;
	}
	
	public String getSessionPassword() {
		return sessionPassword;
	}
	
	public void setSessionPassword(String sessionPassword) {
		this.sessionPassword = sessionPassword;
	}
}
