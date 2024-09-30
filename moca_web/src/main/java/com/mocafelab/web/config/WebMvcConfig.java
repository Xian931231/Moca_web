package com.mocafelab.web.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.mocafelab.web.interceptor.CustomInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
	

	/**
	 * ArgumentResolver 설정 
	 */	
	@Bean
	public CustomArgumentResolver customArgumentResolver() {
		return new CustomArgumentResolver();
	}
	
	@Bean
    public CustomInterceptor interceptor(){
        return new CustomInterceptor();
    }

	
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		// TODO Auto-generated method stub
		resolvers.add(customArgumentResolver());
	}
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// TODO Auto-generated method stub
		registry.addInterceptor(interceptor())
		.excludePathPatterns("/assets/**", "/js/**", "/css/**", "/favicon.ico")
		;
	}
}
