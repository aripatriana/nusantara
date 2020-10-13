package com.nusantara.automate.service;

import java.io.IOException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.nusantara.automate.WebExchange;
import com.nusantara.automate.util.JsonUtils;
import com.nusantara.automate.util.WSRestClient;

public class GatewayServiceImpl implements GatewayService {

	Logger log = LoggerFactory.getLogger(GatewayServiceImpl.class);
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <T> ResponseEntity<T> sendPostToGW(String url, String authTokenId, String loginTransactionId, String serviceID, Object mParam, Class<T> restResponseType) {
		WSRestClient wsRestClient = new WSRestClient();
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
		headers.add("authenticationtoken", authTokenId);
		headers.add("logintransactionid", loginTransactionId);
		headers.add("serviceid", serviceID);
		headers.add("Content-Type", "application/json");
		headers.add("Accept", "application/json");

		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		HttpEntity<T> requestEntity = new HttpEntity(mParam, headers);
		wsRestClient.setRestTemplate(restTemplate);
		ResponseEntity<T> resp = new ResponseEntity<T>(null);
		try {
			resp = wsRestClient.post(url, requestEntity, restResponseType, new HashMap<String, Object>());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return resp;
	}
	
	@SuppressWarnings("unchecked")
	public void remoteLogin(String url, WebExchange webExchange) throws Exception, IOException, RemoteAccessException {
		try {
			HashMap<String, String> reqBodyForLogin = new HashMap<String,String>();
			reqBodyForLogin.put("userId", webExchange.get("user.gateway.username").toString());
			reqBodyForLogin.put("password", webExchange.get("user.gateway.password").toString());
			reqBodyForLogin.put("memberCode", webExchange.get("user.gateway.memberCode").toString());
			reqBodyForLogin.put("keyFile", webExchange.get("user.gateway.token").toString());
			reqBodyForLogin.put("dc", "o=IT,o=Internal,dc=eclears,dc=kpei");
			
			log.info("Prepare to login to gateway with " + reqBodyForLogin);
			
			JsonUtils jsonUtils = new JsonUtils();
			HttpHeaders header = new HttpHeaders();
			header.setContentType(MediaType.APPLICATION_JSON);
	
			HttpEntity<String> req = new HttpEntity<String>(jsonUtils.toJson(reqBodyForLogin), header);
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
			restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
	
			ResponseEntity<Object> res = restTemplate.exchange(url, HttpMethod.POST, req, Object.class);
			
			HashMap<String, Object> responseMap = new HashMap<String, Object>();
			try {
				String responseJson = jsonUtils.toJson(res.getBody());
				responseMap = (HashMap<String, Object>) jsonUtils.fromJson(responseJson, HashMap.class);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if ("success".equalsIgnoreCase(responseMap.get("requestStatus").toString())) {
				
				webExchange.put("authenticationToken", responseMap.get("authenticationtoken").toString());
				webExchange.put("loginTransactionId", responseMap.get("transactionid").toString());
				
				log.info("Get authentication-token " + webExchange.get("authenticationToken").toString());
			} else {
				log.info("Login failed : " + res.getBody());
				throw new Exception("Login failed");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			throw e1;
		} catch (ResourceAccessException e) {
			throw e;
		}
	}
	
}
