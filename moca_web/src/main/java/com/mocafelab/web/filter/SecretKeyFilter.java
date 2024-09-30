package com.mocafelab.web.filter;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mocafelab.web.login.SessionUtil;

import lombok.extern.slf4j.Slf4j;
import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.util.CookieUtil;

/**
 * SecretKey 생성위한 필터
 * 기본 암호화키/쿠키 미존재/세션 암호화키 미존재일 경우 시크릿키 생성 
 * 쿠키로 넘어온 암호화키와 서버에 존재하는 암호화키가 다를 경우 시크릿키 생성
 * 생성된 시크릿키를 클라이언트에 전달
 * @author mure96
 *
 */
@Slf4j
@Component
@Order(2)
public class SecretKeyFilter extends OncePerRequestFilter {
	@Value("${secret.default.key}")
	private String SECRET_DEFAULT_KEY;
	
	@Value("${apiPrefix}")
	private String APIPREFIX;
	
	@Autowired
	private SessionUtil sessionUtil;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		boolean showLog = false;
		if(request.getRequestURI().startsWith(APIPREFIX)) {
			showLog = true;
		}
		
		try {
			HttpSession session = request.getSession();
			String secretKey = (String) session.getAttribute("secret_key");
			
			if(showLog) {
				log.debug("Session Secret Key ======= " + secretKey);
			}
			
			Optional<Cookie> cookies = CookieUtil.getCookie(request, "new_secret_key");
			if(!cookies.isPresent()){
				// 쿠키가 없으면
				secretKey = SECRET_DEFAULT_KEY;
			} else if(
					secretKey == null || secretKey.equals("") ||							// 세션에 저장된 시크릿키가 없을때 
					secretKey.equals(SECRET_DEFAULT_KEY) || 								// 세션에 저장된 시크릿키가 디폴트 키일때
					(cookies.isPresent() && !cookies.get().getValue().equals(secretKey))	// 쿠키가 있고, 쿠키에 있는 값이 서버의 시크릿키값과 다를때
			) {
				secretKey = CommonUtil.makeRandStr(32);
			} 
			
			// 세션에 저장
			session.setAttribute("secret_key", secretKey);
			
			// 클라이언트에 전달
			CookieUtil.addCookie(request, response, "new_secret_key", secretKey, sessionUtil.getExpirationMinutes(), false);
			
			if(showLog) {
				log.debug("Saved Session Secret Key ======= " + secretKey);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		filterChain.doFilter(request, response);
	}
}
