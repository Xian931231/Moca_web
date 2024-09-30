package com.mocafelab.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import net.newfrom.lib.jwt.JWTService;
import net.newfrom.lib.vo.CustomProperty;

@Configuration
@PropertySource(value= {"classpath:/properties/common.properties"}, encoding = "UTF-8")
public class BeanConfig {

	@Value("${jwt.secret.key}")
	private String SECRET_KEY;
	
	/**
	 *  code관련 proerties파일의 빈 추가
	 * @return CustomProperty
	 */
	@Bean(name="codeProperty")
	public CustomProperty codeProperty() {
		return new CustomProperty("properties/code.properties");
	}
	
	/**
	 * JWTService 빈 추가
	 * @return
	 */
	@Bean(name="jwtService")
	public JWTService jwtService() {
		return new JWTService(SECRET_KEY);
	}
}
