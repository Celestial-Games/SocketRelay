package com.socketrelay.client;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.Timer;

import com.socketrelay.client.Notifications.Event;
import com.socketrelay.client.TrafficCounter.TrafficBuffer;

public class TrafficImage extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;

	private static SizeSegmentation[] sizeSegmentations=new SizeSegmentation[]{
			new SizeSegmentation("256 bps",(int)(256/8),"%.0f bps",1f/8f),
			new SizeSegmentation("512 bps",(int)(512/8),"%.0f bps",1f/8f),
			new SizeSegmentation("768 bps",(int)(768/8),"%.0f bps",1f/8f),
			new SizeSegmentation("1 kbps",(int)(1*1024/8),"%.0f kbps",1024f/8),
			new SizeSegmentation("2 kbps",(int)(2*1024/8),"%.0f kbps",1024f/8),
			new SizeSegmentation("4 kbps",(int)(4*1024/8),"%.0f kbps",1024f/8),
			new SizeSegmentation("8 kbps",(int)(8*1024/8),"%.0f kbps",1024f/8),
			new SizeSegmentation("16 kbps",(int)(16*1024/8),"%.0f kbps",1024f/8f),
			new SizeSegmentation("32 kbps",(int)(32*1024/8),"%.0f kbps",1024f/8f),
			new SizeSegmentation("48 kbps",(int)(48*1024/8),"%.0f kbps",1024f/8f),
			new SizeSegmentation("64 kbps",(int)(64*1024/8),"%.0f kbps",1024f/8f),
			new SizeSegmentation("96 kbps",(int)(96*1024/8),"%.0f kbps",1024f/8f),
			new SizeSegmentation("128 kbps",(int)(128*1024/8),"%.0f kbps",1024f/8f),
			new SizeSegmentation("256 kbps",(int)(256*1024/8),"%.0f kbps",1024f/8f),
			new SizeSegmentation("512 kbps",(int)(512*1024/8),"%.0f kbps",1024f/8f),
			new SizeSegmentation("768 kbps",(int)(768*1024/8),"%.0f kbps",1024f/8f),
			new SizeSegmentation("1 Mbps",(int)(1024*1024/8),"%.0f Mbps",1024f*1024f/8f),
			new SizeSegmentation("2 Mbps",(int)(2*1024*1024/8),"%.0f Mbps",1024f*1024f/8f),
			new SizeSegmentation("5 Mbps",(int)(5*1024*1024/8),"%.0f Mbps",1024f*1024f/8f),
			new SizeSegmentation("7 Mbps",(int)(7*1024*1024/8),"%.0f Mbps",1024f*1024f/8f),
			new SizeSegmentation("10 Mbps",(int)(10*1024*1024/8),"%.0f Mbps",1024f*1024f/8f),
			new SizeSegmentation("20 Mbps",(int)(20*1024*1024/8),"%.0f Mbps",1024f*1024f/8f),
			new SizeSegmentation("40 Mbps",(int)(40*1024*1024/8),"%.0f Mbps",1024f*1024f/8f),
			new SizeSegmentation("50 Mbps",(int)(50*1024*1024/8),"%.0f Mbps",1024f*1024f/8f),
			new SizeSegmentation("75 Mbps",(int)(75*1024*1024/8),"%.0f Mbps",1024f*1024f/8f),
			new SizeSegmentation("100 Mbps",(int)(100*1024*1024/8),"%.0f Mbps",1024f*1024f/8f),
			new SizeSegmentation("120 Mbps",(int)(120*1024*1024/8),"%.0f Mbps",1024f*1024f/8f),
			new SizeSegmentation("150 Mbps",(int)(150*1024*1024/8),"%.0f Mbps",1024f*1024f/8f),
			new SizeSegmentation("200 Mbps",(int)(200*1024*1024/8),"%.0f Mbps",1024f*1024f/8f),
			new SizeSegmentation("300 Mbps",(int)(300*1024*1024/8),"%.0f Mbps",1024f*1024f/8f),
			new SizeSegmentation("500 Mbps",(int)(500*1024*1024/8),"%.0f Mbps",1024f*1024f/8f),
			new SizeSegmentation("750 Mbps",(int)(750*1024*1024/8),"%.0f Mbps",1024f*1024f/8f),
			new SizeSegmentation("1000 Mbps",(int)(1000*1024*1024/8),"%.0f Mbps",1024f*1024f/8f)
	};
	
	enum DataSizeDisplay {
		Bytes(1024l){
			public String getSize(long bytes) {
				return ""+bytes+" bytes";
			}
		},
		KB16(16l*1024l){
			public String getSize(long bytes) {
				return String.format("%.2f kb", ((float)bytes)/1024f);
			}
		},
		KB64(64l*1024l){
			public String getSize(long bytes) {
				return String.format("%.1f kb", ((float)bytes)/1024f);
			}
		},
		KB(1024l*1024l){
			public String getSize(long bytes) {
				return String.format("%.0f kb", ((float)bytes)/1024f);
			}
		},
		MB(1024l*1024l*1024l){
			public String getSize(long bytes) {
				return String.format("%.2f mb", ((float)bytes)/1024f/1024f);
			}
		},
		GB(1024l*1024l*1024l*1024l){
			public String getSize(long bytes) {
				return String.format("%.2f gb", ((float)bytes)/1024f/1024f/1024f);
			}
		};
		
		private long maxBytes;
		
		DataSizeDisplay(long maxBytes){
			this.maxBytes=maxBytes;
		}
		
		static DataSizeDisplay getDisplayType(long bytes) {
			DataSizeDisplay display=GB;
			for (DataSizeDisplay dataSizeDisplay:DataSizeDisplay.values()) {
				if (dataSizeDisplay.maxBytes>bytes) {
					display=dataSizeDisplay;
					break;
				}
			}
			return display;
		}
		
		abstract public String getSize(long bytes);
	}

	private static Color overlayLowColor=new Color(0x45a29e);
	private static Color overlayHighColor=new Color(0x66fcf1);

	private Map<String,Color> allocatedColors=new HashMap<>();
	private TrafficCounterSource trafficCounterSource=null;
	
	private Timer timer;
	
	public TrafficImage() {
		timer=new Timer(1000*TrafficCounter.getSecondsPerSample(),this);
		timer.setRepeats(true);
		timer.start();
		allocatedColors.put("overlayLowColor", overlayLowColor);
		allocatedColors.put("overlayHighColor", overlayHighColor);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		repaint();
	}
	
	public TrafficCounterSource getTrafficCounterSource() {
		return trafficCounterSource;
	}

	public void setTrafficCounterSource(TrafficCounterSource trafficCounterSource) {
		this.trafficCounterSource = trafficCounterSource;
		repaint();
	}
	
	private Color getRandomColor() {
		return new Color(Color.HSBtoRGB((float)Math.random(), (float)Math.random()/2f+.5f, (float)Math.random()/2f+.5f));
	}

	private Color getColor(String dataId) {
		if (allocatedColors.size()==0) {
			synchronized(allocatedColors) {
				allocatedColors.put(dataId, getRandomColor());
			}
		} else {
			if (!allocatedColors.containsKey(dataId)) {
				synchronized(allocatedColors) {
					int diff=0;
					Color color=null;
					
					for (int t=0;t<200;t++) {
						Color c=new Color(Color.HSBtoRGB((float)Math.random(), (float)Math.random()/2f+.5f, (float)Math.random()/2f+.5f));
						int cDiff=256*3;
						for (Color us:allocatedColors.values()) {
							int usDiff=Math.abs(us.getRed()-c.getRed())+Math.abs(us.getGreen()-c.getGreen())+Math.abs(us.getBlue()-c.getBlue());
							if (cDiff>usDiff){
								cDiff=usDiff;
							}
						}

						if (cDiff>diff || color==null) {
							diff=cDiff;
							color=c;
						}
					}

					allocatedColors.put(dataId, color);
				}
			}
		}
		return allocatedColors.get(dataId);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2=(Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setColor(Color.black);
		g2.fillRect(0, 0, getWidth(), getHeight());

		Map<String,TrafficCounter> trafficCounters=trafficCounterSource!=null?trafficCounterSource.getTrafficCounters():null;

		if (trafficCounters!=null) {
			Map<String,TrafficBuffer> dataSet=new HashMap<>();
			String[] clients=null;
			
			synchronized (trafficCounters) {
				clients=new String[trafficCounters.size()];
				int c=0;
				for (TrafficCounter trafficCounter:trafficCounters.values()) {
					clients[c++]=trafficCounter.getClientId();
					dataSet.put(trafficCounter.getClientId(), trafficCounter.getTraffic());
				}
			}
			
			// Calculate max and average
			int samplesToDraw=TrafficCounter.getMaxSamples()-1;
			int max=0;
			long average=0;
			for (int t=0;t<samplesToDraw;t++) {
				int totalData=0;
				for (String client:clients) {
					int data=dataSet.get(client).getDataUsedPerSecond(t);
					totalData+=data;
					average+=data;
					if (totalData>max) {
						max=totalData;
					}
				}
			}
			average/=samplesToDraw;

			// Find range
			SizeSegmentation sizeSegmentation=sizeSegmentations[sizeSegmentations.length-1];
			for (SizeSegmentation ss:sizeSegmentations) {
				if (ss.canHandle(max)) {
					sizeSegmentation=ss;
					break;
				}
			}
			
			Arrays.sort(clients);
			
			int itemWidth=(getWidth()/samplesToDraw);
			int width=itemWidth*samplesToDraw;
			int left=(getWidth()-(width-1))/2;
			
			int top=4;
			int height=getHeight()-24;
			
			sizeSegmentation.paintUnderlay(g2, left, 0, width, height);
			int current=0;
			for (int t=0;t<samplesToDraw;t++) {
				int totalData=0;
				int lastHeight=0;
				current=0;
				for (String client:clients) {
					int data=dataSet.get(client).getDataUsedPerSecond(t);
					int currentHeight=sizeSegmentation.getScaled(height,totalData+data);
					totalData+=data;
					current+=data;
					if (currentHeight>lastHeight) {
						g2.setColor(getColor(client));
						g2.fillRect(t*itemWidth+left, top+height-currentHeight, itemWidth-1, currentHeight-lastHeight);
						lastHeight=currentHeight;
					}
				}
			}
			long totalBytes=trafficCounterSource.getTotalBytes();
			sizeSegmentation.paintOverlay(g2, left, top, width, height, max, (int)average, current, DataSizeDisplay.getDisplayType(totalBytes).getSize(totalBytes));
			
			
			Event[] events=Notifications.getEvents();
			int lastPos=-1;
			int y=0;
			for (int t=events.length-1;t>=0;t--) {
				Event event=events[t];
				int p=samplesToDraw-((int)(System.currentTimeMillis()-event.getTimeStamp()))/(TrafficCounter.SECONDS_PER_SAMPLE*1000);
				
				if (lastPos!=p) {
					lastPos=p;
					y=top+height+3;
				}
				
				if (p>=0) {
					int w=itemWidth/2;
					int x=p*itemWidth+left+w;
					String draw="";
					switch (event.getEventType()) {
					case ClientJoined:
						g2.setColor(Color.yellow);
						draw="+";
						break;
					case ClientLeft:
						g2.setColor(Color.yellow);
						draw="-";
						break;
					case ConnectedToServer:
						g2.setColor(Color.cyan);
						draw="+";
						break;
					case ConnectedToServerLost:
						g2.setColor(Color.cyan);
						draw="-";
						break;
					}
					Rectangle2D bounds=g.getFontMetrics().getStringBounds(draw, g);
			        g2.drawString(draw, (int)(x-bounds.getWidth()), y+7+("-".equals(draw)?-1:0));
			        y+=6;
				} else {
					break;
				}
			}
		}
	}
	
	private static class SizeSegmentation {
		private int maxBytes;
		private String format;
		private String description;
		private float divisor;
		
		private SizeSegmentation(String description, int maxBytes, String format, float divisor) {
			this.maxBytes=maxBytes;
			this.description=description;
			this.format=format;
			this.divisor=divisor;
		}
		
		public boolean canHandle(int bytes) {
			return maxBytes>bytes;
		}

		public int getScaled(int height, int bytes) {
			return (bytes*height)/maxBytes;
		}
		
		private String getFormatted(int speed) {
			return String.format(format, ((float)speed)/divisor);
		}
		
		public void paintUnderlay(Graphics2D g, int left, int top, int width, int height) {
		}
		
		public void paintOverlay(Graphics2D g, int left, int top, int width, int height, int max, int average, int current,String total) {
			g.setColor(overlayLowColor);
			g.drawLine(left, top, left, top+height);
			g.drawLine(left, top+height, left+width, top+height);
			
	        Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
	        g.setStroke(dashed);
	        g.drawLine(left, top+height/2, left+width, top+height/2);

			g.setColor(overlayHighColor);
	        g.drawString(description, left+3, top+13);
			
	        // Draw max
	        int ypos=height*max/maxBytes;
	        dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2,4}, 0);
	        g.setStroke(dashed);
	        g.drawLine(left, top+height-ypos, left+width, top+height-ypos);
	        String maxString=getFormatted(max);
			Rectangle2D bounds=g.getFontMetrics().getStringBounds(maxString, g);
			g.setColor(Color.black);
			g.drawString(maxString, left+3+width/4-(int)(bounds.getWidth()/2)-1, top+height-ypos+13);
			g.drawString(maxString, +left+3+width/4-(int)(bounds.getWidth()/2)+1, top+height-ypos+13);
			g.drawString(maxString, left+3+width/4-(int)(bounds.getWidth()/2), top+height-ypos+13-1);
			g.drawString(maxString, left+3+width/4-(int)(bounds.getWidth()/2), top+height-ypos+13+1);
			g.setColor(overlayHighColor);
			g.drawString(maxString, left+3+width/4-(int)(bounds.getWidth()/2), top+height-ypos+13);

	        // Draw average
	        ypos=height*average/maxBytes;
	        dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{1,4}, 0);
	        g.setStroke(dashed);
	        g.drawLine(left, top+height-ypos, left+width, top+height-ypos);
	        String aveString=getFormatted(average);
			bounds=g.getFontMetrics().getStringBounds(maxString, g);
			g.setColor(Color.black);
			g.drawString(aveString, left+3+width*3/4-(int)(bounds.getWidth()/2)-1, top+height-ypos-5);
			g.drawString(aveString, +left+3+width*3/4-(int)(bounds.getWidth()/2)+1, top+height-ypos-5);
			g.drawString(aveString, left+3+width*3/4-(int)(bounds.getWidth()/2), top+height-ypos-5-1);
			g.drawString(aveString, left+3+width*3/4-(int)(bounds.getWidth()/2), top+height-ypos-5+1);
			g.setColor(overlayHighColor);
			g.drawString(aveString, left+3+width*3/4-(int)(bounds.getWidth()/2), top+height-ypos-5);

			
			bounds=g.getFontMetrics().getStringBounds(total, g);
			g.setColor(Color.black);
			g.drawString(total, left+width-((int)bounds.getWidth()+width)/2+1, top+13);
			g.drawString(total, left+width-((int)bounds.getWidth()+width)/2-1, top+13);
			g.drawString(total, left+width-((int)bounds.getWidth()+width)/2, top+13+1);
			g.drawString(total, left+width-((int)bounds.getWidth()+width)/2, top+13-1);
			g.setColor(overlayHighColor);
			g.drawString(total, left+width-((int)bounds.getWidth()+width)/2, top+13);

			String currentString="Current "+getFormatted(current);
			bounds=g.getFontMetrics().getStringBounds(currentString, g);
			g.setColor(Color.black);
			g.drawString(currentString, left+width-(int)bounds.getWidth()-3+1, top+13);
			g.drawString(currentString, left+width-(int)bounds.getWidth()-3-1, top+13);
			g.drawString(currentString, left+width-(int)bounds.getWidth()-3, top+13+1);
			g.drawString(currentString, left+width-(int)bounds.getWidth()-3, top+13-1);
			g.setColor(overlayHighColor);
			g.drawString(currentString, left+width-(int)bounds.getWidth()-3, top+13);

		}
		
	}
}
