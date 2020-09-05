package com.socketrelay.games;

import static com.socketrelay.host.Consts.PlayerAttribute;

import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.session.IoSession;

import com.socketrelay.messages.ClientClose;
import com.socketrelay.messages.Data;

public class GameSession {
	
	private Map<String,IoSession> players=new HashMap<>();
	private String sessionName;
	private String sessionPassword;
	private IoSession host;
	private long lastTouched=System.currentTimeMillis();
	private int sessionIdleMs;

	public GameSession(String sessionName,String sessionPassword,int sessionIdleMs){
		this.sessionName=sessionName;
		this.sessionPassword=sessionPassword;
		this.sessionIdleMs=sessionIdleMs;
	}
	
	public String getSessionName() {
		return sessionName;
	}

	public String getSessionPassword(){
		return sessionPassword;
	}
	
	public void touch() {
		lastTouched=System.currentTimeMillis();
	}

	public boolean expired() {
		return System.currentTimeMillis()-lastTouched>sessionIdleMs;
	}

	public void setHost(IoSession host) {
		this.host=host;
	}
	
	public void removePlayer(IoSession session) {
		String player=(String)session.getAttribute(PlayerAttribute);
		host.write(new ClientClose(player));
		synchronized (players) {
			players.remove(player);
		}
	}
	
	public void addPlayer(String playerName,IoSession session) {
		synchronized (players) {
			players.put(playerName, session);
		}
	}
	
	public void playerData(IoSession session, Data data) {
		String player=(String)session.getAttribute(PlayerAttribute);
		
		// Make sure map contains correct player session
		if (!players.containsKey(player) || players.get(player)==session) {
			synchronized (players) {
				players.put(player, session);
			}
		}

		// Pass message on to host if host is not available kill connection
		if (isHostConnected()) {
			data.setClientId(player);
			host.write(data);
		} else {
			session.write(new ClientClose(player));
		}
	}
	
	public void hostData(IoSession session, Data data) {
		String player=(String)session.getAttribute(PlayerAttribute);
		
		// Make sure map contains correct player session
		if (players.containsKey(data.getClientId()) && players.get(data.getClientId()).isActive()) {
			players.get(data.getClientId()).write(data);
		} else {
			host.write(new ClientClose(player));
		}
	}
	
	private boolean isHostConnected() {
		return host!=null && host.isActive();
	}
	
	public void close() {
		if (host!=null && host.isConnected()){
			host.closeNow();
		}
		
		synchronized (players) {
			for (IoSession session:players.values()) {
				if (session.isConnected()) {
					session.closeNow();
				}
			}
		}
	}
}
