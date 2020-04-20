package com.socketrelay.host.commands;

import java.io.IOException;
import java.io.Serializable;
import java.net.UnknownHostException;

import org.apache.mina.core.session.IoSession;

public abstract class Command <message extends Serializable>{

	public abstract void processCommand(IoSession session, message message) throws UnknownHostException, IOException;
	
	@SuppressWarnings("unchecked")
	public void processCommandRaw(IoSession session, Serializable message) throws UnknownHostException, IOException {
		processCommand(session, (message)message);
	}
}
