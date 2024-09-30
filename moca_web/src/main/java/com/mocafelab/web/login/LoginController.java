package com.mocafelab.web.login;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.vo.RequestMap;

@RestController
public class LoginController {
	
	@Autowired
	private LoginService loginService;
	
	/**
	 * 로그인
	 * @param reqMap
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/login")
	public Map<String, Object> login(RequestMap reqMap, HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "uid");
		CommonUtil.checkNullThrowException(param, "passwd");
		
		param.put("duplicate_login", "N");
		
		return loginService.login(param, request, response);
	}
	
	/**
	 * 중복 로그인
	 * @param reqMap
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/login/duplicate")
	public Map<String, Object> duplicateLogin(RequestMap reqMap, HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "uid");
		CommonUtil.checkNullThrowException(param, "passwd");
		
		param.put("duplicate_login", "Y");
		
		return loginService.duplicateLogin(param, request, response);
	}

	/**
	 * 로그인 여부 체크
	 * @param reqMap
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@PostMapping("${apiPrefix}/login/check")
	public Map<String, Object> hasLogin(RequestMap reqMap, HttpServletRequest request) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return loginService.hasLogin(param, request);
	}
	
	/**
	 * 로그아웃
	 * @param reqMap
	 * @return
	 */
	@PostMapping("/logout")
	public Map<String, Object> logout(RequestMap reqMap, HttpServletRequest request, HttpServletResponse response) throws Exception{
		Map<String, Object> param = reqMap.getMap();
		
		return loginService.logout(param, request, response);
	}
	
	/**
	 * 로그인 연장
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@PostMapping("${apiPrefix}/login/extension")
	public Map<String, Object> loginExtension(RequestMap reqMap, HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return loginService.loginExtension(param, request, response);
	}
}
