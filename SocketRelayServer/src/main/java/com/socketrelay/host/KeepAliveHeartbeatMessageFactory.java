package com.socketrelay.host;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;

import com.socketrelay.messages.Heartbeat;

public class KeepAliveHeartbeatMessageFactory implements KeepAliveMessageFactory {
	@Override
	public Object getRequest(IoSession session) {
		return new Heartbeat();
	}

	@Override
	public Object getResponse(IoSession session, Object message) {
		return null;
	}

	@Override
	public boolean isRequest(IoSession session, Object message) {
		return message instanceof Heartbeat;
	}

	@Override
	public boolean isResponse(IoSession session, Object message) {
		return message instanceof Heartbeat;
	}

}
