package com.mocafelab.web.role;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.vo.RequestMap;

/**
 * 권한, 권한별 메뉴 설정 
 * @author mure96 
 *
 */
@RestController
@RequestMapping("${apiPrefix}/role")
public class RoleController {

	@Autowired
	private RoleService service;

	/**
	 * 권한 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/list")
	public Map<String, Object> getRoleList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		return service.getRoleList(param);
	}

	/**
	 * 권한 상세 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/detail")
	public Map<String, Object> getRoleDetail(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "role_id");
		
		return service.getRoleDetail(param);
	}
	
	/**
	 * 권한 등록
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/add")
	public Map<String, Object> addRole(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "name");
		CommonUtil.checkNullThrowException(param, "utype");
		CommonUtil.checkNullThrowException(param, "use_yn");
		
		return service.addRole(param);
	}
	
	/**
	 * 권한 수정
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/modify")
	public Map<String, Object> modifyRole(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "role_id");
		CommonUtil.checkNullThrowException(param, "name");
		
		return service.modifyRole(param);
	
	}

	/**
	 * 권한 삭제
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/remove")
	public Map<String, Object> removeRole(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "role_id");
		
		return service.removeRole(param);
	}
	

	/**
	 * 권한별 메뉴 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/menu/list")
	public Map<String, Object> getRoleMenuList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "role_id");
		
		return service.getRoleMenuList(param);
	}

	/**
	 * 권한별 메뉴 등록
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/menu/add")
	public Map<String, Object> addRoleManagerMenu(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "role_id");
		CommonUtil.checkNullThrowException(param, "menu_id");
		CommonUtil.checkNullThrowException(param, "access_yn");
		
		return service.addRoleManagerMenu(param);
	}
	
	/**
	 * 권한별 메뉴 수정(단건)
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/menu/modify")
	public Map<String, Object> modifyRoleManagerMenu(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "role_id");
		CommonUtil.checkNullThrowException(param, "menu_id");
		CommonUtil.checkNullThrowException(param, "access_yn");
		
		return service.modifyRoleManagerMenu(param);
	}
	
	/**
	 * 권한별 메뉴 수정(일괄)
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/menu/modifyAll")
	public Map<String, Object> roleManagerMenuUpdateAll(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "role_id");
		CommonUtil.checkNullThrowException(param, "menu_list");
		
		return service.modifyRoleManagerMenuAll(param);
	}
	
	/**
	 * 권한별 메뉴 삭제
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/menu/remove")
	public Map<String, Object> removeRoleManagerMenu(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "role_menu_id");
		
		return service.removeRoleManagerMenu(param);
	}
	
	/**
	 * 권한별 메뉴 접근 가능 여부 확인
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/menu/hasIpPermission")
	public Map<String, Object> hasManagerMenuIpPermission(RequestMap reqMap, HttpServletRequest request) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "menu_url");
		
		param.put("request_ip", CommonUtil.getRemoteIP(request));
		param.put("role_id", param.get("login_role_id"));
		param.put("utype", param.get("login_utype"));
		
		return service.hasManagerMenuIpPermission(param);
	}
	
	// 관리자 권한구분
	
	/**
	 * 관리자가 등록한 권한 리스트 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/staff/list")
	public Map<String, Object> getStaffRoleList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return service.getStaffRoleList(param);
	}
	
	/**
	 * 관리자 권한구분 등록
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/staff/add")
	public Map<String, Object> addStaffRole(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "name");
		
		return service.addStaffRole(param);
	}
	
	/**
	 * 관리자 권한구분 수정
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/staff/modify")
	public Map<String, Object> modifyStaffRole(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "role_id");
		
		return service.modifyStaffRole(param);
	}
	
	/**
	 * 관리자 권한구분 명 수정
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/staff/modify/name")
	public Map<String, Object> modifyStaffRoleName(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "role_id");
		CommonUtil.checkNullThrowException(param, "name");
		
		return service.modifyStaffRoleName(param);
	}
	
	/**
	 * 관리자 권한구분 순서 수정
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/staff/modify/sort")
	public Map<String, Object> modifyStaffRoleSort(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return service.modifyStaffRoleSort(param);
	}
	
	/**
	 * 관리자 권한구분 삭제
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/staff/remove")
	public Map<String, Object> removeStaffRole(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "role_id_list");
		
		return service.removeStaffRole(param);
	}
	
	/**
	 * 직원 개별 권한 메뉴 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/staff/permission/menu/list")
	public Map<String, Object> getStaffPermissionRoleMenu(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return service.getStaffPermissionRoleMenu(param);
	}
}
