package com.mocafelab.web.exception;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocafelab.web.vo.BeanFactory;
import com.mocafelab.web.vo.ResponseMap;

import lombok.extern.slf4j.Slf4j;


/**
 * 필터에서 발생하는 에러 처리를 위한 핸들러
 * @author mure96
 *
 */

@Slf4j
@Component
@Order(1)
public class CustomExceptionHandler extends OncePerRequestFilter {
	
	@Autowired
	private BeanFactory beanFactory;
	
	@Value("${secret.default.key}")
	private String SECRET_DEFAULT_KEY;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			filterChain.doFilter(request, response);
		} catch (Exception e) {
			log.debug("CustomExceptionHandler doFilterInternal");
			
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String stacktrace = sw.toString();
			log.error("{}", stacktrace);
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			ResponseMap respMap = beanFactory.getResponseMap();
			
			String result = objectMapper.writeValueAsString(respMap.getErrResponse(e.getMessage()));
			
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.getWriter().write(result);
		} 
	}
}
