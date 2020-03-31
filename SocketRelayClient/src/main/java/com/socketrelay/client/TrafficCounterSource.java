package com.socketrelay.client;

import java.util.Map;

public interface TrafficCounterSource {
	public Map<String,TrafficCounter> getTrafficCounters();
}
