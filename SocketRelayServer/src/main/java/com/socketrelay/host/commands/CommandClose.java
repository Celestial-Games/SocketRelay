package com.socketrelay.host.commands;

import org.apache.mina.core.session.IoSession;

import com.socketrelay.messages.Close;

public class CommandClose extends Command<Close> {

	@Override
	public void processCommand(IoSession session, Close message) {
		session.closeNow();
	}

}
