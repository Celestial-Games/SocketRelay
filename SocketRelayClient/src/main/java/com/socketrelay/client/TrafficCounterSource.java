package com.socketrelay.client;

import java.util.Map;

public interface TrafficCounterSource {
	
	public long getTotalBytes();
	public Map<String,TrafficCounter> getTrafficCounters();
}
