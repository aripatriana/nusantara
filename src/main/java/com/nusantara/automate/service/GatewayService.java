package com.nusantara.automate.service;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.remoting.RemoteAccessException;

import com.nusantara.automate.WebExchange;

public interface GatewayService {

	public <T> ResponseEntity<T> sendPostToGW(String url, String authTokenId, String loginTransactionId, String serviceID, Object mParam, Class<T> restResponseType);
	
	public void remoteLogin(String url, WebExchange webExchange) throws Exception, IOException, RemoteAccessException;

}
