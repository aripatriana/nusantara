package com.nusantara.automate.util;

import java.util.concurrent.atomic.AtomicInteger;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class PortForwarding {

	private String user;
	private String password;
	private String host;
	private String remoteHost;
	private int remotePort;
	private int localPort;
	private Session session;
	private static AtomicInteger count = new AtomicInteger();
	

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
				e.printStackTrace();
			}

			if (session != null) {
				try {
					session.connect();
					session.setPortForwardingL(localPort, remoteHost, remotePort);
				} catch (JSchException e) {
					e.printStackTrace();
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
