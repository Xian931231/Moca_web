package com.mocafelab.web.member;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.vo.RequestMap;

@RestController
@RequestMapping("${apiPrefix}/member/external")
public class ExternalMemberController {

	@Autowired
	private ExternalMemberService externalMemberService;
	
	/**
	 * 로그인 정보 조회
	 * @param request
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/myData/get")
	public Map<String, Object> getMyData(HttpServletRequest request, RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return externalMemberService.getMyData(request, param);
	}
	
	/**
	 * 메뉴 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/menu/list")
	public Map<String, Object> getExternalMenuList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();

		CommonUtil.checkNullThrowException(param, "external_login_id");
		CommonUtil.checkNullThrowException(param, "external_login_utype");
		CommonUtil.checkNullThrowException(param, "external_login_role_id");
		
		param.put("utype", param.get("external_login_utype"));
		param.put("role_id", param.get("external_login_role_id"));
		
		return externalMemberService.getMenuList(param); 
	}	
}