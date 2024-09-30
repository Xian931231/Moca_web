package com.mocafelab.web.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mocafelab.web.common.CommonMapper;
import com.mocafelab.web.enums.MemberType;
import com.mocafelab.web.login.ExternalSessionUtil;
import com.mocafelab.web.login.SessionUtil;
import com.mocafelab.web.member.MemberMapper;

import lombok.extern.slf4j.Slf4j;
import net.newfrom.lib.util.CommonUtil;

/**
 * 권한별 메뉴 접근 제한 필터
 * @author mure96
 *
 */
@Slf4j
@Component
@Order(5)
public class RoleMenuFilter extends OncePerRequestFilter{
	
	@Autowired
	private SessionUtil sessionUtil;
	
	@Autowired
	private ExternalSessionUtil externalSessionUtil;
	
	@Autowired
	private CommonMapper commonMapper;
	
	@Autowired
	private MemberMapper memberMapper;
	
	/**
	 * 필터 제외 URL 인지 체크
	 * @param requestUrl
	 * @return
	 */
	private boolean isExcludeUrl(String requestUrl) {
		FilterExcludeUrl filterExcludeUrl = new FilterExcludeUrl();
		List<String> urlList = filterExcludeUrl.getRoleMenuFilterUrl();
		return urlList.stream().filter(url -> {
			if(url.endsWith("*")) {
				url = url.substring(0, url.lastIndexOf("/"));
				return requestUrl.startsWith(url);
			} else {
				return requestUrl.equals(url);
			}
		}).findFirst().isPresent();
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		/**
		 * 1. sessiond의 role_id 조회 
		 * 2. role_id에 해당하는 menu list 조회 
		 * 3. 현재 접속하려는 페이지가 menu_list에 존재하는지 확인  
		 */
		String requestURI = request.getRequestURI();
		String requestIp = CommonUtil.getRemoteIP(request);
		
		if(isExcludeUrl(requestURI)) {
			filterChain.doFilter(request, response);
			return;
		}
		HttpSession session = request.getSession();
		
		try {
			// 모카페 로그인 role id 
			Object roleId_o = session.getAttribute("login_role_id");
			
			// 별도 페이지 로그인 role id
			Object externalRoleId_o = session.getAttribute("external_login_role_id"); 
			
			// 모카페 메뉴 권한 체크 
			if(roleId_o != null) {
				int roleId = Integer.valueOf(String.valueOf(roleId_o));
				String utype = (String) session.getAttribute("login_utype");
				List<Map<String, Object>> mocafeMenuList = sessionUtil.getLoginMenuList();
				
				Map<String, Object> param = new HashMap<>();
				param.put("role_id", roleId);
				param.put("login_utype", utype);
				param.put("request_ip", requestIp);
			
				String defaultUrl = (String) memberMapper.getDefaultMenu(param).get("url");
				
				log.info("Mocafe roleId, utype =========== " + roleId + ", " + utype);
				
				log.debug("Mocafe menuList = " + CommonUtil.listToJsonArray(mocafeMenuList));
				
				log.debug("Mocafe defaultUrl = " + defaultUrl);
				
				for(Map<String, Object> menuItem : mocafeMenuList) {
					Object accessYn = menuItem.get("access_yn");
					Object limitIpYn = menuItem.get("limit_ip_yn");
					String menuUrl = (String) menuItem.get("url");

					if (requestURI.equals("/")) {
						// 로그인 상태로 "/" 접속할 경우 default menu url 이동
						response.sendRedirect(defaultUrl);
						return;
					} else if(requestURI.equals(menuUrl) && !menuUrl.equals("/")) {
						if(utype.equals(MemberType.ADMIN.getType()) && limitIpYn != null && limitIpYn.equals("Y")) {
							// 관리자 계정일 경우
							// permit_ip 체크 
							if(commonMapper.hasPermitIp(param) > 0) {
								filterChain.doFilter(request, response);
							} else {
								break;
							}
						}

						if(accessYn != null && accessYn.equals("Y")) {
							// access_yn 체크
							filterChain.doFilter(request, response);
							return;
						} 
					}
				}
			}
			
			// 별도페이지 메뉴 권한 체크
			if(externalRoleId_o != null) {
				List<Map<String, Object>> externalMenuList = externalSessionUtil.getLoginMenuList();
				
				for(Map<String, Object> menuItem : externalMenuList) {
					Object accessYn = menuItem.get("access_yn");
					String menuUrl = (String) menuItem.get("url");

					if (requestURI.equals("/")) { // 루트 접근 시 모카페 로그인 페이지로 이동
						response.sendRedirect("/login");
						return;
					} else if(requestURI.equals(menuUrl) && !menuUrl.equals("/")) {
						if(accessYn != null && accessYn.equals("Y")) {
							filterChain.doFilter(request, response);
							return;
						} 
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
			
		// TODO IP 접근 제한 설정 
//		throw new NotFoundManagerRoleException();
		response.sendRedirect("/error/role");
	}
}
