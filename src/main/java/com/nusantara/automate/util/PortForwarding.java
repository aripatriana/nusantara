package com.nusantara.automate.util;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.nusantara.automate.WebExchange;

public class PortForwarding {

	private Logger log = LoggerFactory.getLogger(PortForwarding.class);
	
	private String user;
	private String password;
	private String host;
	private String remoteHost;
	private int remotePort;
	private int localPort;
	private Session session;
	private static AtomicInteger count = new AtomicInteger();
	
	public PortForwarding(WebExchange webExchange) {
		this(webExchange.get("gateway.address.username").toString(), webExchange.get("gateway.address.password").toString(), 
				webExchange.get("gateway.address.host").toString(), webExchange.get("gateway.address.remoteHost").toString(), 
				Integer.valueOf(webExchange.get("gateway.address.remotePort").toString()), 
				Integer.valueOf(webExchange.get("gateway.address.localPort").toString()));
	}
	public PortForwarding(String user, String password, String host, String remoteHost, int remotePort, int localPort) {
		this.user = user;
		this.password = password;
		this.host = host;
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
		this.localPort = localPort;
	}
	
	public void connect() {
		if (count.incrementAndGet() == 1) {
			java.util.Properties config = new java.util.Properties(); 
			config.put("StrictHostKeyChecking", "no");
			
			JSch jsch=new JSch();
			try {
				session = jsch.getSession(user, host, 22);
				session.setConfig(config);
				session.setPassword(password);
			} catch (JSchException e) {
				log.error("ERROR ", e);
			}

			if (session != null) {
				try {
					session.connect();
					session.setPortForwardingL(localPort, remoteHost, remotePort);
				} catch (JSchException e) {
					log.error("ERROR ", e);
				}
			}
			
		}
	}
	
	public void disconnect() {
		if (count.decrementAndGet() == 0) {
			if (session != null) {
				session.disconnect();
			}			
		}
	}
	
	
	
	
	 
}
