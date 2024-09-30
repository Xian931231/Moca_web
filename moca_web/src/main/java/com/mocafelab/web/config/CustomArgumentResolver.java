package com.mocafelab.web.config;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.mocafelab.web.login.ExternalSessionUtil;
import com.mocafelab.web.login.SessionUtil;
import com.mocafelab.web.util.EntityEscapes;

import net.newfrom.lib.util.AES256Util;
import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.vo.RequestMap;

/**
 * 일반 파라미터용 resolver
 * @author asd
 *
 */
public class CustomArgumentResolver implements HandlerMethodArgumentResolver {
	
	@Autowired
	private SessionUtil sessionUtil;
	
	@Autowired
	private ExternalSessionUtil ExternalSessionUtil;
	
	
	@Value("${secret.default.key}")
	private String SECRET_DEFAULT_KEY;
	
	@Value("${secret.bank.account.number}")
	private String SECRET_BANK_ACCOUNT_NUMBER;
	
	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();

		Map<String, Object> headerMap = new HashMap<String, Object>();
		Map<String, Object> bodyMap = new HashMap<String, Object>();
		HttpSession session = request.getSession();

		// 세션값 저장
		headerMap.putAll(sessionUtil.setSessionValue(session));
		headerMap.putAll(ExternalSessionUtil.setSessionValue(session));
		
		headerMap.put("remote_ip", CommonUtil.getRemoteIP(request));
		headerMap.put("os", CommonUtil.getOs(request));
		headerMap.put("browser", CommonUtil.getBrowser(request));
		
		// 암호화키 저장
		headerMap.put("secret_bank_account_number", SECRET_BANK_ACCOUNT_NUMBER);
		
		String secretKey = (String) session.getAttribute("secret_key");
		if(secretKey == null || secretKey.equals("")) {
			// 세션에 새로 발급받은 암호화키가 없다면 디폴트 암호화 키로 복호화 
			secretKey = SECRET_DEFAULT_KEY;
		} 
		
		AES256Util aes256Util = new AES256Util(secretKey);
		
		// 헤더
		/*Enumeration<String> headerNames = request.getHeaderNames();
		while(headerNames.hasMoreElements()) {
			String key = headerNames.nextElement();
			String value = request.getHeader(key);
			if(value != null) {
				headerMap.put(key, value);
			}
		}*/
		// 파라미터 
		Enumeration<String> parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String key = parameterNames.nextElement();
			String[] values = request.getParameterValues(key);

			if (values != null) {
				if(key.equals("e")) {
					String decryptStr = aes256Util.decrypt(values[0]);
					if(decryptStr != null && !decryptStr.equals("") && !decryptStr.equals("\"\"")) {
//						Map<String, Object> escMap = CommonUtil.jsonToMap(decryptStr);
						
						//파라미터 치환
						
						EntityEscapes esc = new EntityEscapes(CommonUtil.jsonToMap(decryptStr));
						Map<String, Object> escMap = esc.getEntityEscapes();
						
						for(String encKey : escMap.keySet()) {
							try {
								Object encValue = escMap.get(encKey);
								if(encValue != null) {
									if(String.valueOf(encValue).startsWith("0") && String.valueOf(encValue).length() > 1) {
									} else {
										escMap.put(encKey, Integer.valueOf(encValue.toString()));
									}
								}
							} catch (NumberFormatException e) {
								continue;
							}
						}
						bodyMap.putAll(escMap);
					}
				} else if (values.length == 1) {
					bodyMap.put(key, values[0]);
				} else {
					bodyMap.put(key, values);
				}
			}
		}
		
		RequestMap reqMap = new RequestMap(headerMap, bodyMap);
		reqMap.setLimit(20);
		return reqMap;
	}
	
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		// TODO Auto-generated method stub;
		return parameter.getParameterType().isAssignableFrom(RequestMap.class);
	}
}
