package com.mocafelab.web.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocafelab.web.login.ExternalSessionUtil;
import com.mocafelab.web.login.LoginMapper;
import com.mocafelab.web.login.LoginService;
import com.mocafelab.web.login.SessionUtil;
import com.mocafelab.web.vo.BeanFactory;
import com.mocafelab.web.vo.Code;
import com.mocafelab.web.vo.ResponseMap;

import lombok.extern.slf4j.Slf4j;
import net.newfrom.lib.util.CommonUtil;

@Slf4j
@Component
@Order(3)
public class JWTFilter extends OncePerRequestFilter{

	@Autowired
	private BeanFactory beanFactory;
	
	@Autowired
	private SessionUtil sessionUtil;

	@Autowired
	private LoginMapper loginMapper;
	
	@Autowired
	private LoginService loginService;
	
	@Autowired
	private ExternalSessionUtil externalSessionUtil;
	
	@Value("${secret.default.key}")
	public String SECRET_DEFAULT_KEY;
	
	@Value("${apiPrefix}")
	private String APIPREFIX;
	
	@Value("${session.key.login.utype}")
	private String SESSION_KEY_LOGIN_UTYPE;
		
	@Value("${session.key.login.role.id}")
	private String SESSION_KEY_LOGIN_ROLE_ID;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		FilterExcludeUrl filterExcludeUrl = new FilterExcludeUrl();
		
		// 필터 제외 리스트
		List<String> excludePathList = filterExcludeUrl.getJwtFilterUrl();
		
		HttpSession session = request.getSession();
		
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 필터 제외 페이지 체크
		boolean isExclude = false;
		String requestURI = request.getRequestURI();
		
		for (String uri : excludePathList) {
			// /js/* 하위체크
			int lastIndex = uri.lastIndexOf("*");
			if (lastIndex > -1) {
				String startUri = uri.substring(0, lastIndex - 1);
				if (requestURI.startsWith(startUri)) {
					isExclude = true;
					break;
				}
			} else {
				if (requestURI.equals(uri)) {
					isExclude = true;
					break;
				}
			}
		}
		
		loginService.setAdminLogin(request, response);
		
		// 필터 제외 주소, 별도 페이지 요청은 JWT 검증 없이 통과
		if (isExclude) {
			filterChain.doFilter(request, response);
			return;
		}
		
		// API 호출 시 referer 체크
    	if(requestURI.startsWith("/api/v1/") && !filterExcludeUrl.isValidReferer(request)) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		
		log.info("requestUrl = {}", requestURI);
		
		// Mocafe 로그인 체크
        boolean isMocafeAuth = sessionUtil.sessionValidation(session);
        // External 로그인 체크
        boolean isExternalAuth = externalSessionUtil.sessionValidation(session);
 
        if (isMocafeAuth || isExternalAuth || isPerformanceAccount(request, response)) { 
			log.info("--------------------- JWT 인증 성공");
			
			if(isMocafeAuth) {
				// 이벤트 발생 시 세션 갱신
				if(!requestURI.equals("/logout")) {
					sessionUtil.updateSession(request, response);
				}
			}
			filterChain.doFilter(request, response);
		} else {
			// 인증 실패 시 세션값 제거 후 로그인 페이지로 이동 
			log.info("--------------------- JWT 인증 실패");
			
			String serialized = "";

			if (sessionUtil.removeSession(request, response) && externalSessionUtil.removeSession(request, response)) {
				serialized = new ObjectMapper().writeValueAsString(respMap.getResponse(Code.LOGIN_INFO_IS_NULL));
			} 
			
            byte[] responseToSend = serialized.getBytes();
            response.getOutputStream().write(responseToSend);
            response.setContentType("application/json");
            
            if(!requestURI.startsWith("/api/v1/")) {
            	// 리다이렉트 URL 일 경우에만 리다이렉트처리
                if (filterExcludeUrl.isRedirectURL(requestURI)) {
                	response.sendRedirect("/error");
                } else {
                	if(requestURI.equals("/")){
                		response.sendRedirect("/login");
    	        	} else if(!requestURI.equals("/logout")){
    	        		response.sendRedirect("/error");
    	        	}
                }
            }
		}
	}
	
	private boolean isPerformanceAccount(HttpServletRequest request, HttpServletResponse response){
		List<String> allowList = new ArrayList<>();
		allowList.add("new_a1");
		allowList.add("new_d1");
		allowList.add("new_a1_dsp01");
		allowList.add("test_su_001");
		allowList.add("external0002");
		
		try {
			
			String allowId = request.getHeader("allowId");
			
			if(allowId != null && allowList.contains(allowId)) {
				Map<String, Object> param = new HashMap<>() {{
					put("uid", allowId);
				}};
				
				Map<String, Object> loginData = loginMapper.getLoginData(param);
				if(!CommonUtil.checkIsNull(loginData)) {
					request.getSession().setAttribute("secret_key", SECRET_DEFAULT_KEY);
					sessionUtil.setAccessToken(loginData, request, response);
					return true;
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}

		
		return false;
	}
}