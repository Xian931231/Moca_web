package com.mocafelab.web.interceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.mocafelab.web.email.template.ModifyInfoCompleteTemplate;
import com.mocafelab.web.login.ExternalSessionUtil;
import com.mocafelab.web.login.SessionUtil;
import com.mocafelab.web.member.MemberMapper;

import lombok.extern.slf4j.Slf4j;
import net.newfrom.lib.util.CommonUtil;

@Slf4j
public class CustomInterceptor implements HandlerInterceptor {

	@Value("${secret.default.key}")
	private String SECRET_DEFAULT_KEY;
	
	@Autowired
	private SessionUtil sessionUtil;
	
	@Autowired
	private ExternalSessionUtil externalSessionUtil;
	
	@Autowired
	private MemberMapper memberMapper;
	
	private final String MODIFY_INFO_BASIC_URL = "/member/info/modify";
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		
		/**
		 * 
		 * 1) Mocafe 
		 *   1. 로그인을 한 후 /login/*, /signup/* 주소 입력 시 
		 *   2. url에 returnUrl이 포함되어있고
		 *   3. 그 값이 정해진 url이면 redirect, 그 외는 메인으로 이동
		 * 2) External 
		 *   1. 로그인을 한 후 /external, /external/login 주소 입력 시
		 *   2. default url로 이동
		 */
		HttpSession session = request.getSession();
		
		String requestUrl = request.getRequestURI();
		
		// 모카페 로그인 후 모카페 로그인 페이지, 회원가입 페이지 접근 불가
		if(requestUrl.startsWith("/login") || requestUrl.startsWith("/signup/")){
			boolean isMocafeLogin = sessionUtil.sessionValidation(session);
			
			if(isMocafeLogin) {
				
				Map<String, Object> urlParam = CommonUtil.getUrlParams(request);
				
				if(!CommonUtil.checkIsNull(urlParam)) {
					String returnUrl = (String) urlParam.get("returnUrl");
					
					if(returnUrl != null) {
						List<Map<String, Object>> menuList = sessionUtil.getLoginMenuList();
						
						for(Map<String, Object> menu : menuList) {
							String url = (String) menu.get("url");
							
							if(!url.equals("/") && (returnUrl.startsWith(url) || returnUrl.equals(url))) {
								response.sendRedirect(returnUrl);
								return true;
							}
						}
					}
					
				}
				
				response.sendRedirect("/");
				return false;
			}
		} else if(requestUrl.equals("/external") || requestUrl.equals("/external/login")) {
			boolean isExternalLogin = externalSessionUtil.sessionValidation(session);
			
			if(isExternalLogin) {
				String roleId = externalSessionUtil.getLoginRoleIdInSession(session);
					
				Map<String, Object> menuParam = new HashMap<>();
				menuParam.put("role_id", roleId);
				
				Map<String, Object> defaultMenu = memberMapper.getDefaultMenu(menuParam);
				if(defaultMenu != null) {
					String defaultUrl = (String)defaultMenu.get("url");
					if(defaultUrl != null && !defaultUrl.equals("")) {
						response.sendRedirect(defaultUrl);
						return false;
					}
				}
			}
		}
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
	}
}
