package com.mocafelab.web.role;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.vo.RequestMap;

@RestController
@RequestMapping("${apiPrefix}/role/agency")
public class AgencyRoleController {

	@Autowired
	private AgencyRoleService agenctRoleService; 
	

	/**
	 * 권한 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/staff/list")
	public Map<String, Object> getRoleList(RequestMap reqMap) throws Exception{
		Map<String, Object> param = reqMap.getMap();
		
		return agenctRoleService.getRoleList(param);
	}

	/**
	 * 권한 메뉴 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/staff/menu/list")
	public Map<String, Object> getRoleMenuList(RequestMap reqMap) throws Exception{
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "role_id");
		
		return agenctRoleService.getRoleMenuList(param);
	}
	
	/**
	 * 대행사 권한구분 등록
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/staff/add")
	public Map<String, Object> addRole(RequestMap reqMap) throws Exception{
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "role_name");
		
		return agenctRoleService.addRole(param);
	}
	
	/**
	 * 대행사 권한구분 수정
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/staff/modify")
	public Map<String, Object> modifyRole(RequestMap reqMap) throws Exception{
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "role_id");
		CommonUtil.checkNullThrowException(param, "role_json");
		
		return agenctRoleService.modifyRole(param);
	}
	
	/**
	 * 대행사 권한구분명 수정
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/staff/modify/name")
	public Map<String, Object> modifyRoleName(RequestMap reqMap) throws Exception{
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "role_id");
		CommonUtil.checkNullThrowException(param, "role_name");
		
		return agenctRoleService.modifyRoleName(param);
	}
	
	/**
	 * 대행사 권한구분명 수정
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/staff/modify/sort")
	public Map<String, Object> modifyRoleSort(RequestMap reqMap) throws Exception{
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "role_id_list");
		
		
		return agenctRoleService.modifyRoleSort(param);
	}
	
	/**
	 * 대행사 직원별 권한 수정
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/staff/modify/permission")
	public Map<String, Object> modifyRolePermission(RequestMap reqMap) throws Exception{
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "member_id");
		CommonUtil.checkNullThrowException(param, "role_id");
		CommonUtil.checkNullThrowException(param, "demand_id_list");
		
		
		return agenctRoleService.modifyRolePermission(param);
	}
	
	/**
	 * 대행사 직원별 권한 메뉴 리스트 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/staff/permission/list")
	public Map<String, Object> getStaffRoleMenuList(RequestMap reqMap) throws Exception{
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "member_id");
		
		return agenctRoleService.getStaffRoleMenuList(param);
	}
	
	/**
	 * 대행사 권한구분 삭제
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/staff/remove")
	public Map<String, Object> removeRole(RequestMap reqMap) throws Exception{
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "role_id_list");
		
		return agenctRoleService.removeRole(param);
	}
}
