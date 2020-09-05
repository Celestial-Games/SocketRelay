package com.socketrelay.host;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.LogManager;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;
import org.apache.mina.filter.keepalive.KeepAliveRequestTimeoutHandler;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SocketRelayHost {
	private static final Logger logger=LoggerFactory.getLogger(SocketRelayConfig.class);
	
	private static final Gson gson=new GsonBuilder().create();
	
	private SocketRelayConfig config=new SocketRelayConfig(); 
	private Set<Integer> portsUsed=Collections.synchronizedSet(new HashSet<>());
	private NioSocketAcceptor acceptor; 
	
	private SocketRelayHost() throws IOException {
		loadConfig();
	}
	
	private void loadConfig() throws IOException {
		File configFile=new File("config.json");
		if (configFile.exists()) {
			config=gson.fromJson(new FileReader(configFile), SocketRelayConfig.class);
		}
	}

	private void start() throws Exception {
		acceptor=new NioSocketAcceptor(); 
		acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));
        
        KeepAliveMessageFactory keepAliveMessageFactory = new KeepAliveHeartbeatMessageFactory();
        KeepAliveFilter keepAliveFilter = new KeepAliveFilter(keepAliveMessageFactory, IdleStatus.WRITER_IDLE, KeepAliveRequestTimeoutHandler.LOG,3,3);
        keepAliveFilter.setForwardEvent(true);
        acceptor.getFilterChain().addLast("heart", keepAliveFilter);
        
        HostHandler hostHandler=new HostHandler(this,config,portsUsed);
        acceptor.setHandler(hostHandler);
        acceptor.getSessionConfig().setMaxReadBufferSize(1024*1024);
        acceptor.getSessionConfig().setReadBufferSize(1024);
        acceptor.getSessionConfig().setIdleTime(IdleStatus.READER_IDLE, 6);

        InetSocketAddress inetSocketAddress=new InetSocketAddress(config.getServerIp(), config.getServerport());
        
		acceptor.bind(inetSocketAddress);
		logger.info("Socket Relay listener started on "+inetSocketAddress.toString());
	}

	public void close() {
		acceptor.dispose();
		System.exit(0);
	}

	enum Argument {
		Start("start","Start the SocketRelay."){
			public void executeArgument(String parts) throws Exception{
				SocketRelayHost socketRelayHost=new SocketRelayHost();
				socketRelayHost.start();
			}
		},
		Loop("loop","{port} Starts a loop socket listener on this port."){
			public int processArgument(Map<Argument,String> arguments,String[] args, int pos) {
				if (args.length<pos+1) {
					try {
						Help.executeArgument("");
					} catch (Exception e) {
					}
					System.exit(-1);
				}
				arguments.put(this,args[pos+1]);
				return pos+1;
			}
			
			public void executeArgument(String parts) throws Exception{
				new SocketLoopback(Integer.parseInt(parts));
			}
		},
		Help("help","Show the help screen"){
			public void executeArgument(String parts) throws Exception{
				PrintStream out=System.out;
				
				out.println(
						"Socket Relay Host Help\n"
						+ "\n"
						+ "Use java - jar socketrelayhost.jar {params}\n"
						+ "\n"
						+ "Params:");
				for (Argument argument:Argument.values()) {
					out.println(argument.param+"\t"+argument.help);
				}
			}
		};
		
		private String param;
		private String help;
		
		Argument(String param,String help){
			this.param=param;
			this.help=help;
		}
		
		public int processArgument(Map<Argument,String> arguments,String[] args, int pos) {
			arguments.put(this,null);
			return pos;
		}
		
		public abstract void executeArgument(String parts) throws Exception;
	}
	
	private static boolean getArguments(String[] args) throws Exception{
		Map<Argument,String> arguments=new HashMap<>();
		
		for (int t=0;t<args.length;t++) {
			boolean found=false;
			for (Argument argument:Argument.values()) {
				if (argument.param.equals(args[t])) {
					t=argument.processArgument(arguments, args, t);
					argument.executeArgument(arguments.get(argument));
					found=true;
					break;
				}
			}
			if (!found) {
				logger.error("Unable to read argument '"+args[t]+"'.");
				return false;
			}
		}
		
		return arguments.size()>0;
	}

	public static void main(String[] args) {
		try {
			File loggingConfigFile = new File("logging.properties");
			if (loggingConfigFile.exists()) {
				FileInputStream loggingConfigFileStream = new FileInputStream(loggingConfigFile);
				LogManager.getLogManager().readConfiguration(loggingConfigFileStream);
				loggingConfigFileStream.close();
			}
		} catch (IOException ex) {
			logger.error(ex.getMessage(), ex);
			System.exit(-1);
		}

		try {
			if (!getArguments(args)) {
				logger.error("Unable to start SocketRelay server.");
				Argument.Help.executeArgument(null);
			}
		} catch (Exception e) {
			logger.error("Unable to start SocketRelay server.",e);
			System.exit(-1);
		}
		
	}
}
