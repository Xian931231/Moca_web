package com.mocafelab.web.filter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import lombok.NoArgsConstructor;

/**
 * filter 클래스에서 제외할 URL 모음 클래스
 * @author mure96
 *
 */
@NoArgsConstructor
public class FilterExcludeUrl {
	public final static String[] COMMON_URL_LIST = {
		// page, file
		"/messages/*",
		"/error/*",
		"/assets/*",
		"/js/*",
		"/css/*", 
		"/img/*", 
		"/favicon.ico",
		"/signup/*",
		"/support/*",
		"/vendor/*",

		// api 
		"/login/*",
		"/external",
		"/external/login",
		"/api/v1/member/staff/login",
	};
	
	
	private final String[] JWT_FILTER_URL_LIST = {
		"/api/v1/member/signUp/*"
	};
	
	private final String[] ROLE_MENU_FILTER_URL_LIST = {
		"/logout",
		"/external/logout",
		"/api/v1/*",
	};
	
	/**
	 * 리다이렉트 되지않아야할 url list
	 */
	private final String[] NOT_REDIRECT_URL_LIST = {
		"/",
		"/login",
		"/external/login",
		"/logout",
		"/api/v1",
	};
	
	/**
	 * JWT 필터에서 제외할 url 리스트
	 * @return
	 */
	public List<String> getJwtFilterUrl () {
		List<String> commonList = Arrays.asList(COMMON_URL_LIST);
		List<String> jwtList = Arrays.asList(JWT_FILTER_URL_LIST);
		
		List<String> urlList = new ArrayList<>();
		
		urlList.addAll(commonList);
		urlList.addAll(jwtList);
		
		return urlList;
	}
	
	/**
	 * 권한별 메뉴 필터에서 제외할 url 리스트
	 * @return
	 */
	public List<String> getRoleMenuFilterUrl () {
		List<String> commonList = Arrays.asList(COMMON_URL_LIST);
		List<String> roleMenuList = Arrays.asList(ROLE_MENU_FILTER_URL_LIST);
		
		List<String> urlList = new ArrayList<>();
		
		urlList.addAll(commonList);
		urlList.addAll(roleMenuList);
		
		return urlList;
	}
	
	/**
	 * 요청 주소가 redirect 되는 url 인지 확인
	 * @param requestUrl
	 * @return
	 */
	public boolean isRedirectURL(String requestUrl) {
		boolean isRedirect = true;
		
		List<String> notRedirectUrlList = Arrays.asList(NOT_REDIRECT_URL_LIST);
		for(String url : notRedirectUrlList) {
			if(requestUrl.equals(url) || requestUrl.startsWith(url)) {
				isRedirect = false;
				break;
			}
		}
		
		return isRedirect;
	}
	
	/**
	 * referer과 host가 같은지 확인
	 * @param request
	 * @return
	 */
	public boolean isValidReferer(HttpServletRequest request) {
		String referer = request.getHeader("referer");
		String host = request.getHeader("host");
		
		if(referer == null || referer.equals("")) {
			return false;
		}
		boolean isValid = false;
		
		try {
			URL refererURL = new URL(referer);
			String refererAuthority = refererURL.getAuthority();

			if(host.equals(refererAuthority)) {
				isValid = true;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			isValid = false;
		}
		return isValid;
	}
}
 