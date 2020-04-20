package com.socketrelay.host.commands;

import static com.socketrelay.host.Consts.HostConnectionThreadAttribute;

import org.apache.mina.core.session.IoSession;

import com.socketrelay.host.ServerConnectionThreadTcp;
import com.socketrelay.messages.ClientClose;

public class CommandClientClose extends Command<ClientClose> {

	@Override
	public void processCommand(IoSession session, ClientClose message) {
		ServerConnectionThreadTcp serverConnectionThread=(ServerConnectionThreadTcp)session.getAttribute(HostConnectionThreadAttribute);
		serverConnectionThread.closeClient(message.getClientId(),message.getConnectionId());
	}

}
