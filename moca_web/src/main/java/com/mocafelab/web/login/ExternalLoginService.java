package com.mocafelab.web.login;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mocafelab.web.enums.MemberStatus;
import com.mocafelab.web.enums.MemberType;
import com.mocafelab.web.member.MemberMapper;
import com.mocafelab.web.vo.BeanFactory;
import com.mocafelab.web.vo.Code;
import com.mocafelab.web.vo.ResponseMap;

import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.util.CookieUtil;
import net.newfrom.lib.util.SHAUtil;

@Service
public class ExternalLoginService {

	@Autowired
	private LoginMapper loginMapper;
	
	@Autowired
	private MemberMapper memberMapper;
	
	@Autowired
	private ExternalSessionUtil externalSessionUtil;
	
	@Autowired
	private BeanFactory beanFactory;
	
	final static int COOKIE_TIME = 60 * 60 * 24 * 90; // 90일
	
	/**
	 * 별도 페이지 로그인
	 * 별도 페이지는 하나의 아이디로 여러곳에서 동시 로그인 가능
	 * @param param
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> login(Map<String, Object> param, HttpServletRequest request, HttpServletResponse response) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 비밀번호 
		String passwd = String.valueOf(param.get("passwd"));
		passwd = SHAUtil.encrypt(passwd, "SHA-256");
		param.put("passwd", passwd);
		
		// 로그인 정보
		Map<String, Object> loginData = loginMapper.getLoginData(param);
		
		Code code = Code.OK;

		if(CommonUtil.checkIsNull(loginData)) {
			code = Code.LOGIN_FAIL;
		} else {
			// 로그인 가능 여부 검사
			code = isLoginEnable(loginData);
		}
		
		if(code.equals(Code.OK)) {
			param.put("member_id", loginData.get("member_id"));
			
			// 세션 및 쿠키 저장
			externalSessionUtil.setAccessToken(loginData, request, response);
			
			// 로그인 성공 처리 
			loginMapper.modifyLoginSuccess(loginData);
			
			// 로그인 성공 로그 추가
			loginData.put("remote_ip", param.get("remote_ip"));
			loginMapper.addLoginSuccessLog(loginData);
			
		} else {
			return respMap.getResponse(code);
		}
	
		long roleId = (long)loginData.get("role_id");
		param.put("role_id", roleId);
		
		// 디폴트 메뉴로 리다이렉트
		Map<String, Object> defaultMenu = memberMapper.getDefaultMenu(param);
		if(defaultMenu != null) {
			String defaultUrl = (String)defaultMenu.get("url");

			if(defaultUrl != null && !defaultUrl.equals("")) {
				respMap.setBody("default_url", defaultUrl);
			}
		}
		return respMap.getResponse();
	}
	
	/**
	 * 로그아웃
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	public Map<String, Object> logout(Map<String, Object> param, HttpServletRequest request, HttpServletResponse response) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		param.put("member_id", param.get("external_login_id"));
		
		externalSessionUtil.removeSession(request, response);
		
		return respMap.getResponse();
	}
	
	/**
	 * 로그인 가능 여부 검사
	 * @param param
	 * @param loginData
	 * @return
	 * @throws Exception
	 */
	public Code isLoginEnable(Map<String, Object> loginData) throws Exception {
		CommonUtil.checkNullThrowException(loginData, "utype");
		CommonUtil.checkNullThrowException(loginData, "status");
		
		Code code = Code.OK;
		
		String utype = (String) loginData.get("utype");

		String memberStatus = (String) loginData.get("status");
		
		if(!MemberType.getType(utype).equals(MemberType.EXTERNAL)) {
			// 매체사 현장 직원이 아니면 로그인 불가
			code = Code.LOGIN_FAIL;
		} else {
			switch(MemberStatus.getStatus(memberStatus)) {
				case OK:
					code = Code.OK;
					break;
				default:
					code = Code.LOGIN_FAIL;
			}
		}
		return code;
	}
}
