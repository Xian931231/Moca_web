package com.mocafelab.web.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;

public class EntityEscapes {
	private Map<String, Object> request;
	
	public EntityEscapes() {
		
	}
	
	public EntityEscapes(Map<String, Object> requestMap) {
		 this.request = requestMap;
	}

	/**
	 * 파라미터 특수문자 이스케이프
	 * @return
	 */
	public Map<String, Object> getEntityEscapes() {
	
		Map<String, Object> escapeRequest = new HashMap<>();
		
		for(String key : request.keySet()) {
			String escapeValue = "";
			if((request.get(key) != null && request.get(key)!= "" )) {
				if(request.get(key) instanceof ArrayList != true) {
					String value = String.valueOf(request.get(key));
					
					escapeValue = StringEscapeUtils.escapeHtml4(value);
					
					escapeRequest.put(key, escapeValue);
				}else {
					escapeRequest.put(key, request.get(key));
				}
				
			}
		}
		
		return escapeRequest;
	}
	
	/**
	 * 파라미터 특수문자 언이스케이프
	 * @return
	 */
	public Map<String, Object> getEntityUnEscapes(){
		
		Map<String, Object> unEscapeRequest = new HashMap<>();
		
		for(String key : request.keySet()) {
			String unEscapeValue = "";
			if(request.get(key) != null && request.get(key)!= "") {
				String value = String.valueOf(request.get(key));
				
				unEscapeValue = StringEscapeUtils.unescapeHtml4(value);
			}
			
			unEscapeRequest.put(key, unEscapeValue);
			
		}
		
		return unEscapeRequest;
	}
  
}
