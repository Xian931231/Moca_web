package com.mocafelab.web.login;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.mocafelab.web.enums.MemberStatus;
import com.mocafelab.web.enums.MemberType;
import com.mocafelab.web.member.MemberMapper;
import com.mocafelab.web.menu.MenuService;

import net.newfrom.lib.jwt.JWTService;
import net.newfrom.lib.util.CommonUtil;

@Component
public class ExternalSessionUtil {
	
	@Value("${token.key.external.login}")
	private String EXTERNAL_TOKEN_NM;

	@Value("${session.key.external.login.id}")
	private String SESSION_KEY_EXTERNAL_LOGIN_ID;
	
	@Value("${session.key.external.login.role.id}")
	private String SESSION_KEY_EXTERNAL_LOGIN_ROLE_ID;
	
	@Value("${session.key.external.login.utype}")
	private String SESSION_KEY_EXTERNAL_LOGIN_UTYPE;
	
	@Value("${session.key.external.login.status}")
	private String SESSION_KEY_EXTERNAL_LOGIN_STATUS;
		
	@Value("${session.key.external.login.menu.list}")
	private String SESSION_KEY_EXTERNAL_LOGIN_MENU_LIST;
	
	@Value("${session.key.external.login.expire.seconds}")
	private String SESSION_KEY_EXTERNAL_LOGIN_EXPIRE_SECONDS;
	
	@Value("${jwt.issuer}")
	private String ISSUER;
	
	private final int EXPIRATION_MINUTES = 60 * 60 * 24 * 90; // 90일
	
	@Autowired
	private JWTService jwtService;
	
	@Autowired
	private MemberMapper memberMapper;
	
	@Autowired
	private MenuService menuService;
	
	
	/**
	 * 액세스 토큰 발급 및 저장
	 * @param loginData
	 * @param request
	 * @param response
	 * @return
	 */
	@SuppressWarnings("finally")
	public String setAccessToken(Map<String, Object> loginData, HttpServletRequest request, HttpServletResponse response) {
		String accessToken = null;
		
		try {
			String accessIp = CommonUtil.getRemoteIP(request);
			String accessBrowser = CommonUtil.getBrowser(request);
			String accessOs = CommonUtil.getOs(request);
			boolean isMobile = CommonUtil.getIsMobile(request);
			
			// JWT user_token 발급
			Map<String, Object> claims = new HashMap<>();
			claims.put(SESSION_KEY_EXTERNAL_LOGIN_ID, loginData.get("member_id"));
			claims.put(SESSION_KEY_EXTERNAL_LOGIN_ROLE_ID, loginData.get("role_id"));
			claims.put(SESSION_KEY_EXTERNAL_LOGIN_UTYPE, loginData.get("utype"));
			claims.put(SESSION_KEY_EXTERNAL_LOGIN_STATUS, loginData.get("status"));
			claims.put("access_ip", accessIp);
			claims.put("access_browser", accessBrowser);
			claims.put("access_os", accessOs);
			claims.put("is_mobile", isMobile);
			
			accessToken = jwtService.makeLoginToken(claims, ISSUER, EXPIRATION_MINUTES);
			
			HttpSession session = request.getSession();
			session.setAttribute(EXTERNAL_TOKEN_NM, accessToken);
			loginData.put("accessToken", accessToken);
			
			// 세션 저장
			if (!setSession(session, response, loginData)) {
				accessToken = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return accessToken;
		}
	}
	
	/**
	 * 세션에 로그인 정보 저장
	 * @param session
	 * @param response
	 * @param loginData
	 * @return
	 */
	@SuppressWarnings("finally")
	private boolean setSession(HttpSession session, HttpServletResponse response, Map<String, Object> loginData) {
		boolean result = false;
		
		try {
			// 세션에 로그인 정보 저장, 세션 유지 시간 설정
			session.setAttribute(SESSION_KEY_EXTERNAL_LOGIN_ID, loginData.get("member_id"));
			session.setAttribute(SESSION_KEY_EXTERNAL_LOGIN_ROLE_ID, loginData.get("role_id"));
			session.setAttribute(SESSION_KEY_EXTERNAL_LOGIN_UTYPE, loginData.get("utype"));
			session.setAttribute(SESSION_KEY_EXTERNAL_LOGIN_STATUS, loginData.get("status"));
			session.setMaxInactiveInterval(EXPIRATION_MINUTES);
			
			// 해당 회원의 메뉴 정보 저장 
			Map<String, Object> roleParam = new HashMap<>();
			roleParam.put("login_id", loginData.get("member_id"));
			roleParam.put("role_id", loginData.get("role_id"));
			roleParam.put("utype", loginData.get("utype"));
			
			List<Map<String, Object>> menuList = memberMapper.getMenuList(roleParam);
			menuService.setAccessYn(menuList);
			
			String jsonMenuList = CommonUtil.listToJsonArray(menuList);
			session.setAttribute(SESSION_KEY_EXTERNAL_LOGIN_MENU_LIST, jsonMenuList);
			
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return result;
		}
	}
	
	/**
	 * 세션 유효성 검증
	 * @param session
	 * @return
	 */
	@SuppressWarnings("finally")
	public boolean sessionValidation(HttpSession session) {
		boolean result = false;
		
		try {
			// 세션
			String loginId = String.valueOf(session.getAttribute(SESSION_KEY_EXTERNAL_LOGIN_ID));
			String loginRole = String.valueOf(session.getAttribute(SESSION_KEY_EXTERNAL_LOGIN_ROLE_ID));
			String loginUType = (String) session.getAttribute(SESSION_KEY_EXTERNAL_LOGIN_UTYPE);
			String loginStatus = (String) session.getAttribute(SESSION_KEY_EXTERNAL_LOGIN_STATUS);
			String userToken = (String) session.getAttribute(EXTERNAL_TOKEN_NM);
			
			if (loginId != null && loginRole != null && loginUType != null && loginUType.equals(MemberType.EXTERNAL.getType()) && loginStatus != null && userToken != null) {
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return result;
		}
	}
	
	/**
	 * 세션 login_id 가져오기
	 * @param session
	 * @return
	 */
	public String getLoginIdInSession(HttpSession session) {
		String loginId = null;
		
		if (session.getAttribute(SESSION_KEY_EXTERNAL_LOGIN_ID) != null) {
			loginId = String.valueOf(session.getAttribute(SESSION_KEY_EXTERNAL_LOGIN_ID));
		}
		
		return loginId;
	}
	
	/**
	 * 세션 login_role_id 가져오기
	 * @param session
	 * @return
	 */
	public String getLoginRoleIdInSession(HttpSession session) {
		String loginRoleId = null; 
		
		if (session.getAttribute(SESSION_KEY_EXTERNAL_LOGIN_ROLE_ID) != null) {
			loginRoleId = String.valueOf(session.getAttribute(SESSION_KEY_EXTERNAL_LOGIN_ROLE_ID));
		}

		return loginRoleId;
	}
	
	/**
	 * 세션 login_utype 가져오기
	 * @param session
	 * @return
	 */
	public String getLoginUtypeInSession(HttpSession session) {
		String loginUtype = null;
		
		if (session.getAttribute(SESSION_KEY_EXTERNAL_LOGIN_UTYPE) != null) {
			loginUtype = (String) session.getAttribute(SESSION_KEY_EXTERNAL_LOGIN_UTYPE);
		}

		return loginUtype;
	}
	
	/**
	 * 세션 login_status 가져오기
	 * @param session
	 * @return
	 */
	private String getLoginStatusInSession(HttpSession session) {
		String loginStatus = null;
		
		if (session.getAttribute(SESSION_KEY_EXTERNAL_LOGIN_STATUS) != null) {
			loginStatus = (String) session.getAttribute(SESSION_KEY_EXTERNAL_LOGIN_STATUS);
		}

		return loginStatus;
	}

	/**
	 * 세션 user_token 가져오기
	 * @param session
	 * @return
	 */
	public String getUserTokenInSession(HttpSession session) {
		String userToken = null;
		
		if (session.getAttribute(EXTERNAL_TOKEN_NM) != null) {
			userToken = String.valueOf(session.getAttribute(EXTERNAL_TOKEN_NM));
		}

		return userToken;
	}
	
	/**
	 * 저장된 세션 값 해제
	 * @param request
	 * @param response
	 */
	@SuppressWarnings("finally")
	public boolean removeSession(HttpServletRequest request, HttpServletResponse response) {
		boolean result = false;
		HttpSession session = request.getSession();
		
		try {
			session.removeAttribute(SESSION_KEY_EXTERNAL_LOGIN_ID);
			session.removeAttribute(SESSION_KEY_EXTERNAL_LOGIN_ROLE_ID);
			session.removeAttribute(SESSION_KEY_EXTERNAL_LOGIN_UTYPE);
			session.removeAttribute(SESSION_KEY_EXTERNAL_LOGIN_STATUS);
			session.removeAttribute(EXTERNAL_TOKEN_NM);
			
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return result;
		}
	}
	
	/**
	 * 파라미터에 세션값 설정
	 * @param session
	 * @return
	 */
	public Map<String, Object> setSessionValue(HttpSession session) {
		Map<String, Object> sessionMap = new HashMap<String, Object>();
		
		// 로그인 member_id
		if(getLoginIdInSession(session) != null) {
			sessionMap.put(SESSION_KEY_EXTERNAL_LOGIN_ID, getLoginIdInSession(session));
		}
		
		// 로그인 role_id
		if(getLoginRoleIdInSession(session) != null) {
			sessionMap.put(SESSION_KEY_EXTERNAL_LOGIN_ROLE_ID, getLoginRoleIdInSession(session));
		}
		
		// 로그인 회원 계정 타입
		if(getLoginUtypeInSession(session) != null) {
			sessionMap.put(SESSION_KEY_EXTERNAL_LOGIN_UTYPE, getLoginUtypeInSession(session));
		}
		
		// 로그인 회원 stauts
		if(getLoginStatusInSession(session) != null) {
			sessionMap.put(SESSION_KEY_EXTERNAL_LOGIN_STATUS, getLoginStatusInSession(session));
		}
		
		// 로그인 토큰
		if(getUserTokenInSession(session) != null) {
			sessionMap.put(EXTERNAL_TOKEN_NM, getUserTokenInSession(session));
		}
		
		sessionMap.put(SESSION_KEY_EXTERNAL_LOGIN_EXPIRE_SECONDS, EXPIRATION_MINUTES);
		
		return sessionMap;
	}
	
	/**
	 * 세션에 저장되어있는 로그인한 사용자의 접근 가능한 메뉴 목록  
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getLoginMenuList() throws Exception {
		List<Map<String, Object>> menuList = new ArrayList<>();
		
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = attributes.getRequest();
		HttpSession session = request.getSession();
		
		String jsonMenuList = (String) session.getAttribute(SESSION_KEY_EXTERNAL_LOGIN_MENU_LIST);
		if(jsonMenuList != null) {
			menuList.addAll(CommonUtil.jsonArrayToList(jsonMenuList));
		}
		
		return menuList;
	}
	
	/**
	 * 만료시간 가져오기
	 * @return
	 */
	public int getExpirationMinutes() {
		return EXPIRATION_MINUTES;
	}
}