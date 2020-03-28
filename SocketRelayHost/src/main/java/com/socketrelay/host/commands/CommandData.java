package com.socketrelay.host.commands;

import org.apache.mina.core.session.IoSession;
import static com.socketrelay.host.Consts.*;

import com.socketrelay.host.ServerConnectionThread;
import com.socketrelay.messages.Data;

public class CommandData extends Command<Data> {

	@Override
	public void processCommand(IoSession session, Data message) {
		ServerConnectionThread serverConnectionThread=(ServerConnectionThread)session.getAttribute(HostConnectionThreadAttribute);
		serverConnectionThread.writeToClient(message);
	}

}
