package com.mocafelab.web.exception;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.mocafelab.web.vo.BeanFactory;
import com.mocafelab.web.vo.ResponseMap;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {
	
	@Autowired
	private BeanFactory beanFactory;
	
	@ExceptionHandler(Exception.class)
	protected Map<String, Object> Exception(Exception e) {
		
		log.debug("ExceptionAdvice Exception");
		
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		String stacktrace = sw.toString();
		log.error("{}", stacktrace);
		
		ResponseMap respMap = beanFactory.getResponseMap();
		e.printStackTrace();
		return respMap.getErrResponse();
	}
	
	@ExceptionHandler(RuntimeException.class)
	protected Map<String, Object> RuntimeException(RuntimeException e) {
		log.debug("ExceptionAdvice RuntimeException");
		
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		String stacktrace = sw.toString();
		log.error("{}", stacktrace);
		
		ResponseMap respMap = beanFactory.getResponseMap();
		e.printStackTrace();
		return respMap.getErrResponse();
	}
}
