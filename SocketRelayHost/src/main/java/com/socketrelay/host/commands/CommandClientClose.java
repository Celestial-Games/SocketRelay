package com.socketrelay.host.commands;

import static com.socketrelay.host.Consts.HostConnectionThreadAttribute;

import org.apache.mina.core.session.IoSession;

import com.socketrelay.host.ServerConnectionThread;
import com.socketrelay.messages.ClientClose;

public class CommandClientClose extends Command<ClientClose> {

	@Override
	public void processCommand(IoSession session, ClientClose message) {
		ServerConnectionThread serverConnectionThread=(ServerConnectionThread)session.getAttribute(HostConnectionThreadAttribute);
		serverConnectionThread.closeClient(message.getClientId());
	}

}
