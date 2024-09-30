package com.mocafelab.web.login;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.vo.RequestMap;

/**
 * 별도 페이지 로그인
 * @author lky4530
 *
 */
@RestController
@RequestMapping("/external")
public class ExternalLoginController {

	@Autowired
	private ExternalLoginService externalLoginService;
	
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
		
		return externalLoginService.login(param, request, response);
	}
	
	/**
	 * 로그아웃
	 * @param reqMap
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/logout")
	public Map<String, Object> lgout(RequestMap reqMap, HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return externalLoginService.logout(param, request, response);
	}
}
