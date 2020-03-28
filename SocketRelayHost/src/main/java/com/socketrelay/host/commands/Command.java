package com.socketrelay.host.commands;

import java.io.Serializable;

import org.apache.mina.core.session.IoSession;

public abstract class Command <message extends Serializable>{

	public abstract void processCommand(IoSession session, message message);
	
	@SuppressWarnings("unchecked")
	public void processCommandRaw(IoSession session, Serializable message) {
		processCommand(session, (message)message);
	}
}
