package com.socketrelay.client;

import java.util.Arrays;

public class TrafficCounter {
	public static final int SECONDS_PER_SAMPLE=1;
	public static final int SAMPLES_TO_KEEP=5*60/2/SECONDS_PER_SAMPLE;
	
	private String clientId;

	private int[] ringbuffer=new int[SAMPLES_TO_KEEP];
	private int lastPos;
	private long createdMs;
	private int currentMax=0;
	
	public TrafficCounter(String clientId) {
		this.clientId=clientId;
		createdMs=System.currentTimeMillis();
	}

	public String getClientId() {
		return clientId;
	}
	
	public static int getMaxSamples() {
		return SAMPLES_TO_KEEP;
	}
	
	public static int getSecondsPerSample() {
		return SECONDS_PER_SAMPLE;
	}
	
	public int getCurrentMax() {
		return currentMax/SECONDS_PER_SAMPLE;
	}

	private int getPos() {
		int pos=(int)(((System.currentTimeMillis()-createdMs)/1000/SECONDS_PER_SAMPLE)%SAMPLES_TO_KEEP);
		if (pos!=lastPos) {
			synchronized (ringbuffer) {
				while (pos!=lastPos) {
					lastPos=(lastPos+1)%SAMPLES_TO_KEEP;
					ringbuffer[lastPos]=0;
				}
				int newMax=0;
				for (int m:ringbuffer) {
					if (newMax<m) {
						newMax=m;
					}
				}
				currentMax=newMax;
			}
		}
		return pos;
	}
	
	/** NOTE: Need to double check if <code>synchronized this is really needed, casuse a lot of thrashing I would prefer to avoid
	 * 
	 */
	public synchronized void addBytesCount(int count) {
		ringbuffer[getPos()]+=count;
	}
	
	public TrafficBuffer getTraffic() {
		int pos;
		int[] buffer;
		synchronized (ringbuffer) {
			pos=getPos();
			buffer=Arrays.copyOf(ringbuffer,SAMPLES_TO_KEEP);
		}
		
		if (pos!=SAMPLES_TO_KEEP-1) {
			
		}
		
		return new TrafficBuffer(pos,buffer);
	}
	
	public class TrafficBuffer{
		private int pos;
		private int[] buffer;
		
		private TrafficBuffer(int pos, int[] buffer) {
			super();
			this.pos = pos;
			this.buffer = buffer;
		}
		
		public int getDataUsedPerSecond(int t) {
			return (int)(buffer[(pos+1+t)%SAMPLES_TO_KEEP]/SECONDS_PER_SAMPLE);
		}
	}
}
