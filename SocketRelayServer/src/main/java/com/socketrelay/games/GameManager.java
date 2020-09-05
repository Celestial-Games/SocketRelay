package com.socketrelay.games;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameManager {
	private static final Logger log=LoggerFactory.getLogger(GameManager.class);
	
	private Map<String, GameSession> hostGames=new HashMap<>();
	
	private int sessionIdleMs;
	
	private Timer timer=new Timer();
	
	public GameManager(int sessionIdleMs) {
		this.sessionIdleMs=sessionIdleMs;
		timer.scheduleAtFixedRate(new CleanupTask(), sessionIdleMs, sessionIdleMs/10);
	}
	

	public GameSession getOrCreateGameSession(String sessionName, String sessionPassword) {
		GameSession gameSession=getGameSession(sessionName);
		
		if (gameSession!=null && (sessionPassword==null?gameSession.getSessionPassword()!=null:!sessionPassword.equals(gameSession.getSessionPassword()))){
			// This session has a different password so we MUST close it and start a new one.
			gameSession.close();
			gameSession=null;
		}
		
		if (gameSession==null || gameSession.expired()) {
			synchronized (hostGames) {
				if (gameSession==null || gameSession.expired()) {
					gameSession=new GameSession(sessionName, sessionPassword, sessionIdleMs);
					hostGames.put(sessionName,gameSession);
				} else {
					gameSession=hostGames.get(sessionName);
				}
			}
		}

		return gameSession;
	}
	
	public GameSession getGameSession(String sessionName) {
		
		GameSession gameSession=null;
		synchronized (hostGames) {
			gameSession=hostGames.get(sessionName);
		}
		
		return gameSession;
	}
	
	class CleanupTask extends TimerTask {

		@Override
		public void run() {
			try {
				Map<String,GameSession> removeSet=new HashMap<>();
				synchronized (hostGames) {
					for (Entry<String, GameSession> entry:hostGames.entrySet()) {
						if (entry.getValue().expired()) {
							removeSet.put(entry.getKey(), entry.getValue());
						}
					}
					for (String key:removeSet.keySet()) {
						hostGames.remove(key);
					}
				}
				for (GameSession gameSession:removeSet.values()) {
					gameSession.close();
				}
			} catch (Exception e){
				log.error(e.getMessage(),e);
			}
		}
		
	}
	
}
