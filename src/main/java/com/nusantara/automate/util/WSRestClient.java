package com.nusantara.automate.util;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class WSRestClient {

	private RestTemplate rt;
	
	private static String TOTAL_CON 	= System.getProperty("framework.wsrc.total.con", "2500");
	private static String PER_ROUTE_CON = System.getProperty("framework.wsrc.per.route.con", "500");
	
	private static Logger logger = LoggerFactory.getLogger(WSRestClient.class);
	
	public WSRestClient() {
		logger.info("Init WSRestClient.class - MaxConnTotal : {}, MaxConnPerRoute : {}.", TOTAL_CON, PER_ROUTE_CON);
		HttpClient httpClient = HttpClientBuilder.create()
		        .setMaxConnTotal(Integer.valueOf(TOTAL_CON))
		        .setMaxConnPerRoute(Integer.valueOf(PER_ROUTE_CON))
		        .build();
		HttpComponentsClientHttpRequestFactory fc = new HttpComponentsClientHttpRequestFactory(httpClient);
		rt = new RestTemplate(fc);
	}

	/**
	 * Create a new resource by POSTing the given object to the URI template,
	 * and returns the response as {@link HttpEntity}.
	 * <p>
	 * URI Template variables are expanded using the given map.
	 * <p>
	 * The {@code request} parameter can be a {@link HttpEntity} in order to add
	 * additional HTTP headers to the request.
	 * 
	 * @param url
	 *            the URL
	 * @param request
	 *            the Object to be POSTed, may be {@code null}
	 * @param pathVariables
	 *            the variables to expand the template
	 * @return the converted object
	 * @see HttpEntity
	 */
	public <T> ResponseEntity<T> post(String url, Object request, Class<T> responseType,
			Map<String, Object> pathVariables) throws RestClientException {
		request = putTransactionId(request);
		return rt.postForEntity(url, request, responseType, pathVariables);
	}

	/**
	 * Create a new resource by POSTing the given object to the URI template,
	 * and returns the response as {@link HttpEntity}.
	 * <p>
	 * URI Template variables are expanded using the given map.
	 * <p>
	 * The {@code request} parameter can be a {@link HttpEntity} in order to add
	 * additional HTTP headers to the request.
	 * <p>
	 * you can use map for the url path variables if the service using
	 * PathVariables or use map for the url requestParam if the service using
	 * RequestParam. where each map should not be null
	 * 
	 * @param url
	 *            the URL
	 * @param request
	 *            the Object to be POSTed, may be {@code null}
	 * @param pathVariables
	 *            the map containing variables for the URI template (if service
	 *            use PathVariables)
	 * @param requestParams
	 *            the map containing variables for the URI template (if service
	 *            use requestParams like (?name=xxx&pass=yyy))
	 * @return the converted object
	 * @see HttpEntity
	 */
	public <T> ResponseEntity<T> post(String url, Object request, Class<T> responseType,
			Map<String, Object> pathVariables, Map<String, Object> requestParams) throws RestClientException {
		if (!requestParams.isEmpty()) {
			Set<String> s = requestParams.keySet();
			url += "?";
			for (String string : s) {
				url += string + "=" + requestParams.get(string) + "&";
			}
			url = url.substring(0, url.length() - 1);
		}
		request = putTransactionId(request);
		return rt.postForEntity(url, request, responseType, pathVariables);
	}

	/**
	 * Retrieve a representation by doing a GET on the URI template. The
	 * response is converted and stored in an {@link ResponseEntity}.
	 * <p>
	 * URI Template variables are expanded using the given map.
	 * 
	 * @param url
	 *            the URL
	 * @param responseType
	 *            the type of the return value
	 * @param pathVariables
	 *            the map containing variables for the URI template
	 * @return the converted object
	 */
	public <T> ResponseEntity<T> get(String url, Class<T> responseType, Map<String, Object> pathVariables)
			throws RestClientException {
		return rt.getForEntity(url, responseType, pathVariables);
	}

	/**
	 * Retrieve a representation by doing a GET on the URI template. The
	 * response is converted and stored in an {@link ResponseEntity}.
	 * <p>
	 * URI Template variables are expanded using the given map. you can use map
	 * for the url path variables if the service using PathVariables or use map
	 * for the url requestParam if the service using RequestParam. where each
	 * map should not be null
	 * 
	 * @param url
	 *            the URL
	 * @param responseType
	 *            the type of the return value
	 * @param pathVariables
	 *            the map containing variables for the URI template (if service
	 *            use PathVariables)
	 * @param requestParams
	 *            the map containing variables for the URI template (if service
	 *            use requestParams like (?name=xxx&pass=yyy))
	 * @return the converted object
	 * 
	 * @throws RestClientException
	 */
	public <T> ResponseEntity<T> get(String url, Class<T> responseType, Map<String, Object> pathVariables,
			Map<String, Object> requestParams) throws RestClientException {
		if (!requestParams.isEmpty()) {
			Set<String> s = requestParams.keySet();
			url += "?";
			for (String string : s) {
				url += string + "=" + requestParams.get(string) + "&";
			}
			url = url.substring(0, url.length() - 1);
		}
		return rt.getForEntity(url, responseType, pathVariables);

	}

	/**
	 * Creates a new resource by PUTting the given object to URI template.
	 * <p>
	 * URI Template variables are expanded using the given map.
	 * <p>
	 * The {@code request} parameter can be a {@link HttpEntity} in order to add
	 * additional HTTP headers to the request.
	 * 
	 * @param url
	 *            the URL
	 * @param request
	 *            the Object to be PUT, may be {@code null}
	 * @param uriVariables
	 *            the variables to expand the template
	 * @see HttpEntity
	 */
	public void put(String requestUrl, Object requestBody, Map<String, Object> urlVariables)
			throws RestClientException {
		requestBody = putTransactionId(requestBody);
		rt.put(requestUrl, requestBody, urlVariables);
	}

	/**
	 * Delete the resources at the specified URI.
	 * <p>
	 * URI Template variables are expanded using the given map.
	 *
	 * @param url
	 *            the URL
	 * @param uriVariables
	 *            the variables to expand the template
	 */
	public void delete(String requestUrl, Map<String, Object> urlVariables) throws RestClientException {
		rt.delete(requestUrl, urlVariables);
	}

	/**
	 * Execute the HTTP method to the given URI template, preparing the request
	 * with the {@link RequestCallback}, and reading the response with a
	 * {@link ResponseExtractor}.
	 * <p>
	 * URI Template variables are expanded using the given URI variables map.
	 * 
	 * @param url
	 *            the URL
	 * @param requestCallback
	 *            object that prepares the request
	 * @param responseExtractor
	 *            object that extracts the return value from the response
	 * @param uriVariables
	 *            the variables to expand in the template
	 * @return an arbitrary object, as returned by the {@link ResponseExtractor}
	 */
	public <T> T patch(String url, RequestCallback requestCallback, ResponseExtractor<T> responseExtractor,
			Map<String, Object> urlVariables) throws RestClientException {
		return rt.execute(url, HttpMethod.PATCH, requestCallback, responseExtractor, urlVariables);
	}

	// ==================== TOKEN ====================

	@SuppressWarnings("unchecked")
	private <T> HttpEntity<T> entity(Object requestBody, String accesstoken, String serviceid,
			MediaType contentType, MediaType accept, Map<String, String> paramHeaders) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("authenticationtoken", accesstoken);
		headers.set("serviceid", serviceid);
		
		if(paramHeaders != null){
			for(String key : paramHeaders.keySet()){
				headers.set(key, paramHeaders.get(key));
			}
		}
		
		if (accept != null)
			headers.setAccept(Collections.singletonList(accept));
		if (contentType != null)
			headers.setContentType(contentType);
		
		setHeaderTransactionId(headers);
		
		HttpEntity<T> entity = (HttpEntity<T>) new HttpEntity<>(requestBody, headers);
		return entity;
	}

	@SuppressWarnings("unchecked")
	private <T> HttpEntity<T> entity(String accesstoken, String serviceid, MediaType contentType,
			MediaType accept) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("authenticationtoken", accesstoken);
		headers.set("serviceid", serviceid);
		if (accept != null)
			headers.setAccept(Collections.singletonList(accept));
		if (contentType != null)
			headers.setContentType(contentType);
		
		setHeaderTransactionId(headers);
		
		HttpEntity<T> entity = (HttpEntity<T>) new HttpEntity<>(headers);
		return entity;
	}

	public <T> ResponseEntity<T> post(String url, Object request, Class<T> responseType,
			Map<String, Object> uriVariables, String accesstoken, String serviceid, MediaType contentType,
			MediaType accept) throws RestClientException {
		return rt.postForEntity(url, entity(request, accesstoken, serviceid, contentType, accept, null),
				responseType, uriVariables);
	}
	
	public <T> ResponseEntity<T> post(String url, Object request, Class<T> responseType,
			Map<String, Object> uriVariables, String accesstoken, String serviceid, MediaType contentType,
			MediaType accept, Map<String, String> paramHeaders) throws RestClientException {
		return rt.postForEntity(url, entity(request, accesstoken, serviceid, contentType, accept, paramHeaders),
				responseType, uriVariables);
	}
	
	public <T> ResponseEntity<T> post(String url, Object request, Class<T> responseType,
			Map<String, Object> uriVariables, String accesstoken, String serviceid, MediaType contentType,
			MediaType accept, int timeoutInMillis) throws RestClientException {
		SimpleClientHttpRequestFactory rf = (SimpleClientHttpRequestFactory)rt.getRequestFactory();
		rf.setReadTimeout(timeoutInMillis);
		rf.setConnectTimeout(timeoutInMillis);
		
		rt.setRequestFactory(rf);
		
		return rt.postForEntity(url, entity(request, accesstoken, serviceid, contentType, accept, null),
				responseType, uriVariables);
	}
	
	public <T> ResponseEntity<T> post(String url, Object request, Class<T> responseType,
			Map<String, Object> uriVariables, String accesstoken, String serviceid, MediaType contentType,
			MediaType accept, Map<String, String> paramHeaders, int timeoutInMillis) throws RestClientException {
		SimpleClientHttpRequestFactory rf = (SimpleClientHttpRequestFactory)rt.getRequestFactory();
		rf.setReadTimeout(timeoutInMillis);
		rf.setConnectTimeout(timeoutInMillis);
		
		rt.setRequestFactory(rf);
		return rt.postForEntity(url, entity(request, accesstoken, serviceid, contentType, accept, paramHeaders),
				responseType, uriVariables);
	}

	public <T> ResponseEntity<T> get(String url, Class<T> responseType, Map<String, Object> uriVariables,
			String accesstoken, String serviceid, MediaType contentType, MediaType accept)
			throws RestClientException {
		return rt.exchange(url, HttpMethod.GET, entity(accesstoken, serviceid, contentType, accept),
				responseType, uriVariables);
	}

	public <T> ResponseEntity<T> get(String url, Class<T> responseType, Map<String, Object> pathVariables,
			Map<String, Object> requestParams, String accesstoken, String serviceid, MediaType contentType,
			MediaType accept) throws RestClientException {
		if (!requestParams.isEmpty()) {
			Set<String> s = requestParams.keySet();
			url += "?";
			for (String string : s) {
				url += string + "=" + requestParams.get(string) + "&";
			}
			url = url.substring(0, url.length() - 1);
		}
		return rt.exchange(url, HttpMethod.GET, entity(accesstoken, serviceid, contentType, accept),
				responseType, pathVariables);
	}
	
	public <T> ResponseEntity<T> get(String url, Class<T> responseType, Map<String, Object> pathVariables,
			Map<String, Object> requestParams, HttpHeaders header) throws RestClientException {
		if (!requestParams.isEmpty()) {
			Set<String> s = requestParams.keySet();
			url += "?";
			for (String string : s) {
				url += string + "=" + requestParams.get(string) + "&";
			}
			url = url.substring(0, url.length() - 1);
		}
		return rt.exchange(url, HttpMethod.GET, new HttpEntity<T>(header),
				responseType, pathVariables);
	}

	public void put(String requestUrl, Object requestBody, Map<String, Object> urlVariables,
			String accesstoken, String serviceid, MediaType contentType, MediaType accept)
			throws RestClientException {
		rt.put(requestUrl, entity(requestBody, accesstoken, serviceid, contentType, accept, null), urlVariables);
	}

	public void delete(String requestUrl, Map<String, Object> urlVariables, String accesstoken,
			String serviceid, MediaType contentType, MediaType accept) throws RestClientException {
		rt.exchange(requestUrl, HttpMethod.DELETE, entity(accesstoken, serviceid, contentType, accept),
				Object.class, urlVariables);
	}

	// ==================== EXCHANGE ====================

	public <T> ResponseEntity<T> exchange(RequestEntity<T> requestEntity, Class<T> responseType)
			throws RestClientException {
		requestEntity = putTransactionId(requestEntity);
		return rt.exchange(requestEntity, responseType);
	}

	public <T> ResponseEntity<T> exchange(RequestEntity<T> requestEntity,
			ParameterizedTypeReference<T> responseType) throws RestClientException {
		requestEntity = putTransactionId(requestEntity);
		return rt.exchange(requestEntity, responseType);
	}

	public <T> ResponseEntity<T> exchange(URI url, HttpMethod httpMethod, RequestEntity<T> requestEntity,
			Class<T> responseType) throws RestClientException {
		requestEntity = putTransactionId(requestEntity);
		return rt.exchange(url, httpMethod, requestEntity, responseType);
	}

	public <T> ResponseEntity<T> exchange(URI url, HttpMethod httpMethod, RequestEntity<T> requestEntity,
			ParameterizedTypeReference<T> responseType) throws RestClientException {
		requestEntity = putTransactionId(requestEntity);
		return rt.exchange(url, httpMethod, requestEntity, responseType);
	}

	public <T> ResponseEntity<T> exchange(String url, HttpMethod httpMethod, RequestEntity<T> requestEntity,
			Class<T> responseType, Map<String, Object> uriVariables) throws RestClientException {
		requestEntity = putTransactionId(requestEntity);
		return rt.exchange(url, httpMethod, requestEntity, responseType, uriVariables);
	}

	public <T> ResponseEntity<T> exchange(String url, HttpMethod httpMethod, RequestEntity<T> requestEntity,
			Class<T> responseType, Object... uriVariables) throws RestClientException {
		requestEntity = putTransactionId(requestEntity);
		return rt.exchange(url, httpMethod, requestEntity, responseType, uriVariables);
	}

	public <T> ResponseEntity<T> exchange(String url, HttpMethod httpMethod, RequestEntity<T> requestEntity,
			ParameterizedTypeReference<T> responseType, Map<String, Object> uriVariables)
			throws RestClientException {
		requestEntity = putTransactionId(requestEntity);
		return rt.exchange(url, httpMethod, requestEntity, responseType, uriVariables);
	}

	public <T> ResponseEntity<T> exchange(String url, HttpMethod httpMethod, RequestEntity<T> requestEntity,
			ParameterizedTypeReference<T> responseType, Object... uriVariables) throws RestClientException {
		requestEntity = putTransactionId(requestEntity);
		return rt.exchange(url, httpMethod, requestEntity, responseType, uriVariables);
	}

	public RestTemplate getRestTemplate() {
		return rt;
	}

	public void setRestTemplate(RestTemplate rt) {
		this.rt = rt;
	}
	
	private <T> RequestEntity<T> putTransactionId(RequestEntity<T> requestEntity) {
		setHeaderTransactionId(requestEntity.getHeaders());
		return requestEntity;
	}
	
	@SuppressWarnings("unchecked")
	private Object putTransactionId(Object request) {
		if(request instanceof HttpEntity) {
			HttpEntity<Object> entity = (HttpEntity<Object>)request;
			setHeaderTransactionId(entity.getHeaders());
			return entity;
		}
		return request;
	}
	
	private void setHeaderTransactionId(HttpHeaders header) {
		try {
			String trxId = MDC.get("transactionId");
			header.set("transactionid", trxId);
		} catch (Exception e) {}
	}
}
