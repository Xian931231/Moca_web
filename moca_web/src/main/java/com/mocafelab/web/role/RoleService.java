package com.mocafelab.web.role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mocafelab.web.common.CommonMapper;
import com.mocafelab.web.enums.MemberType;
import com.mocafelab.web.member.MemberMapper;
import com.mocafelab.web.menu.MenuService;
import com.mocafelab.web.vo.BeanFactory;
import com.mocafelab.web.vo.Code;
import com.mocafelab.web.vo.ResponseMap;

import net.newfrom.lib.util.CommonUtil;

/**
 * 권한, 권한별 메뉴 설정 
 * @author mure96 
 *
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class RoleService {
	
	@Autowired
	private BeanFactory beanFactory;
	
	@Autowired
	private RoleMapper roleMapper;
	
	@Autowired
	private CommonMapper commonMapper;
	
	@Autowired
	private MemberMapper memberMapper;
	
	@Autowired
	private MenuService menuService;
	
	/**
	 * 권한 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getRoleList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		respMap.setBody("list", roleMapper.getRoleList(param));
		
		return respMap.getResponse();
	}
	
	/**
	 * 권한 상세 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getRoleDetail(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		respMap.setBody("data", roleMapper.getRoleDetail(param));
		
		return respMap.getResponse();
	}
	
	/**
	 * 권한 등록
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> addRole(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		roleMapper.addRole(param);
		
		return respMap.getResponse();
	}
	
	/**
	 * 권한 수정
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> modifyRole(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		roleMapper.modifyRole(param);
		
		return respMap.getResponse();
	}
	
	/**
	 * 권한 삭제
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> removeRole(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		roleMapper.removeRole(param);
		
		return respMap.getResponse();
	}
	
	/**
	 * 권한별 메뉴 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getRoleMenuList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		param.put("utype", MemberType.ADMIN.getType());
		param.put("login_agency_id", 0);
		
		List<Map<String, Object>> list = roleMapper.getRoleMenuList(param); 
		menuService.setAccessYn(list);
		
		list = menuService.getTreeMenuList(list);
		
		respMap.setBody("list", list);
		
		return respMap.getResponse();
	}
	
	/**
	 * 권한별 메뉴 등록
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> addRoleManagerMenu(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		roleMapper.addRoleManagerMenu(param);
		
		return respMap.getResponse();
	}
	
	/**
	 * 권한별 메뉴 수정(단건)
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> modifyRoleManagerMenu(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		roleMapper.modifyRoleManagerMenu(param);
		
		return respMap.getResponse();
	}
	
	/**
	 * 권한별 메뉴 수정(일괄)
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> modifyRoleManagerMenuAll(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 1. 권한 존재 체크
		Map<String, Object> detail = roleMapper.getRoleDetail(param);
		if(detail == null) {
			return respMap.getErrResponse();
		}
		
		// menu_list
		// [{'menu_id':'1', 'access_yn':'Y'}, {'menu_id':'2', 'access_yn':'N'}]
		
		List<Map<String, Object>> menuList = (List<Map<String, Object>>) param.get("menu_list");
		
		for(Map<String, Object> menuObject : menuList) {
			
			CommonUtil.checkNullThrowException(menuObject, "menu_id");
			CommonUtil.checkNullThrowException(menuObject, "access_yn");
			
			// 2. menu_list의 menu_id가 존재하는 메뉴 인지 확인
			if(roleMapper.hasRoleMenu(menuObject) > 0) {
				Map<String, Object> menuParam = new HashMap<>();
				
				menuParam.put("role_id", param.get("role_id"));
				menuParam.put("menu_id", menuObject.get("menu_id"));
				menuParam.put("access_yn", menuObject.get("access_yn"));
				
				// 해당 권한이 갖고있는 메뉴 권한인지 확인 
				if(roleMapper.hasRoleManagerMenu(menuParam) > 0) {
					// 이미 등록되어있다면 수정 
					roleMapper.modifyRoleManagerMenu(menuParam);
				} else {
					// 없는 메뉴 라면 등록 
					roleMapper.addRoleManagerMenu(menuParam);
				}
			}
		}
		
		
		return respMap.getResponse();
	}

	/**
	 * 권한별 메뉴 삭제
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> removeRoleManagerMenu(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		roleMapper.removeRoleManagerMenu(param);
		
		return respMap.getResponse();
	}
	
	/**
	 * 권한별 메뉴 접근 가능 여부 확인
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> hasManagerMenuIpPermission(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		int denyCnt = 0;
		String requestMenuUrl = (String) param.get("menu_url");
		String utype = (String) param.get("utype");
		
		if(utype.equals(MemberType.ADMIN.getType())) {
			List<Map<String, Object>> menuList = roleMapper.getRoleMenuList(param);
			
			List<Map<String, Object>> limitMenuList = menuList.stream().filter(menuItem -> {
				return menuItem.get("limit_ip_yn").equals("Y");
			}).collect(Collectors.toList());

			denyCnt = (int) limitMenuList.stream().filter(menuItem -> {
				String menuUrl = (String) menuItem.get("url");
				
				return (requestMenuUrl.startsWith(menuUrl) || requestMenuUrl.equalsIgnoreCase(menuUrl)) && commonMapper.hasPermitIp(param) <= 0;
			}).count();
		}
		
		if(denyCnt > 0) {
			return respMap.getResponse(Code.MENU_DENY_IP);
		}
		
		return respMap.getResponse();
	}
	
	// 관리자 권한구분
	
	/**
	 * 관리자가 등록한 권한구분 리스트 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getStaffRoleList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> staffRoleList = roleMapper.getStaffRoleList(param);
		respMap.setBody("list", staffRoleList);
		
		return respMap.getResponse();
	}
	
	
	/**
	 * 관리자 권한구분 등록
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> addStaffRole(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		//권한구분명 중복체크 
		if (roleMapper.hasDuplicateName(param) > 0) {
			return respMap.getResponse(Code.AGENCY_ROLE_DUPLICATE_NAME);
		}
		
		// 관리자 권한 확인
		if (!param.get("login_utype").equals(MemberType.ADMIN.getType())) {
			return respMap.getResponse(Code.ADMIN_STAFF_ROLE_NOT_ACCEPT);
		}
		
		// default_menu_id
		param.put("default_menu_id", roleMapper.getStaffDefaultMenuId(param).get("id"));
		
		// role_manager에 등록, id 가져옴
		if (roleMapper.addStaffRole(param) > 0) {
			param.put("role_id", param.get("id"));
			
			// 관리자 디폴트 메뉴를 role_menu에 등록
			List<Map<String, Object>> menuList = roleMapper.getDefaultMenu(param);

			for (Map<String, Object> menuMap : menuList) {
				param.put("menu_id", menuMap.get("id"));
				param.put("access_yn", menuMap.get("default_yn"));
				
				if (roleMapper.addStaffRoleMenu(param) <= 0) {
					throw new RuntimeException();
				}
			}
		} else {
			throw new RuntimeException();
		}
		
		return respMap.getResponse();
	}
	
	/**
	 * 관리자 권한구분 수정
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> modifyStaffRole(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 관리자 권한 확인
		if (!param.get("login_utype").equals(MemberType.ADMIN.getType())) {
			return respMap.getResponse(Code.ADMIN_STAFF_ROLE_NOT_ACCEPT);
		}
		
		// 수정 가능한 권한구분인지 체크
		if (roleMapper.hasStaffRoleManager(param) <= 0) {
			return respMap.getResponse(Code.ADMIN_STAFF_ROLE_NOT_ACCEPT);
		}
		
		// 해당 권한구분의 role_menu를 모두 지우고 param의 menu를 등록
		if (roleMapper.hasRoleMenu(param) > 0) {
			if (roleMapper.removeStaffRoleMenu(param) <= 0) {
				throw new RuntimeException();
			}
		}
		
		List<Map<String, Object>> menuList = (List<Map<String, Object>>) param.get("menu_list");
		
		// default_menu_id 수정
		for (Map<String, Object> menuMap : menuList) {
			String defaultUrl = String.valueOf(menuMap.get("url"));
			String accessYn = String.valueOf(menuMap.get("access_yn"));
			int menuId = (int) menuMap.get("menu_id");
			param.put("menu_id", menuId);
			
			if (!defaultUrl.equals("/") && defaultUrl != "" && !accessYn.equals("N")) {
				param.put("default_menu_id", menuId);
				roleMapper.modifyStaffDefaultMenuId(param);
				break;
			}
		}
		
		for (Map<String, Object> menuMap : menuList) {
			param.put("menu_id", menuMap.get("menu_id"));
			param.put("access_yn", menuMap.get("access_yn"));
			
			Map<String, Object> menuData = roleMapper.getMenu(param);
			if (!CommonUtil.checkIsNull(menuData)) {
				if (roleMapper.addStaffRoleMenu(param) <= 0) {
					throw new RuntimeException();
				}
			} else {
				throw new RuntimeException();
			}
		}
		
		return respMap.getResponse();
	}
	
	/**
	 * 관리자 권한구분 명 수정
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> modifyStaffRoleName(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		//권한구분명 중복체크 
		if (roleMapper.hasDuplicateName(param) > 0) {
			return respMap.getResponse(Code.AGENCY_ROLE_DUPLICATE_NAME);
		}
		
		// 관리자 권한 확인
		if (!param.get("login_utype").equals(MemberType.ADMIN.getType())) {
			return respMap.getResponse(Code.ADMIN_STAFF_ROLE_NOT_ACCEPT);
		}
		
		// 수정 가능한 권한구분인지 체크
		if (roleMapper.hasStaffRoleManager(param) <= 0) {
			return respMap.getResponse(Code.ADMIN_STAFF_ROLE_NOT_ACCEPT);
		}
		
		// 권한구분 명 수정
		if (roleMapper.modifyStaffRoleName(param) <= 0) {
			throw new RuntimeException();
		}
		
		return respMap.getResponse();
	}
	
	/**
	 * 관리자 권한구분 순서 수정
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> modifyStaffRoleSort(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 관리자 권한 확인
		if (!param.get("login_utype").equals(MemberType.ADMIN.getType())) {
			return respMap.getResponse(Code.ADMIN_STAFF_ROLE_NOT_ACCEPT);
		}
		
		
		
		List<String> rolIdList = (List<String>) param.get("role_id_list");

		int idx = (int) param.get("modify_n_cnt");
		
		for(String roleId: rolIdList) {
			++idx;
			param.put("role_id", roleId);
			param.put("sort", idx);
			if (roleMapper.modifyStaffRoleSort(param) <= 0) {
				throw new RuntimeException();
			}
			
		}
		
		return respMap.getResponse();
	}
	
	/**
	 * 관리자 권한구분 삭제
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> removeStaffRole(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 관리자 권한 확인
		if (!param.get("login_utype").equals(MemberType.ADMIN.getType())) {
			return respMap.getResponse(Code.ADMIN_STAFF_ROLE_NOT_ACCEPT);
		}
		
		List<Map<String, Object>> roleIdList = (List<Map<String, Object>>) param.get("role_id_list");
		
		for(Map<String, Object> roleId : roleIdList) {
			param.put("role_id", roleId.get("role_id"));
			
			String	oldRole = (String)roleId.get("role_name");
			
			// 수정 가능한 권한구분인지 체크
			if (roleMapper.hasStaffRoleManager(param) <= 0) {
				return respMap.getResponse(Code.ADMIN_STAFF_ROLE_NOT_ACCEPT);
			}
			
			// role_menu 삭제
			if (roleMapper.removeStaffRoleMenu(param) > 0) {
				// role_manager 삭제
				if (roleMapper.removeStaffRole(param) > 0) {
					// 해당 권한구분을 가지고 있던 계정의 role_id를 미지정으로 변경
					//삭제할 role_id 를 가진 member들 조회
					List<Map<String, Object>> memberRoleList = roleMapper.hasMemberRole(param); 
					if ( memberRoleList.size() > 0) {
						for(Map<String, Object> memberRoleItem : memberRoleList) {
							param.put("member_id", memberRoleItem.get("member_id"));
							// history 쌓기
							param.put("message", "구분 [" + oldRole + "] ▶ [ 미지정 ] 변경");
							
							if(memberMapper.addStaffHistory(param) < 1) {
								throw new RuntimeException();
							}	
						}
						
						if (roleMapper.modifyMemberRoleId(param) <= 0) {
							throw new RuntimeException();
						}
					}
				} else {
					throw new RuntimeException();
				}
			} else {
				throw new RuntimeException();
			}
		}
		
		
		return respMap.getResponse();
	}
	
	/**
	 * 직원 개별 권한 메뉴 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getStaffPermissionRoleMenu(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 관리자 권한 확인
		if (!param.get("login_utype").equals(MemberType.ADMIN.getType())) {
			return respMap.getResponse(Code.ADMIN_STAFF_ROLE_NOT_ACCEPT);
		}
		
		// 파라미터가 없을 경우 본인 메뉴 조회
		if (CommonUtil.checkIsNull(param, "member_id")) {
			param.put("member_id", param.get("login_id"));
		}

		// utype A만 허용
		if (roleMapper.hasMemberStaffRoleUtype(param) <= 0) {
			return respMap.getResponse(Code.ADMIN_STAFF_ROLE_NOT_ACCEPT);
		}

		// permission이 있으면 개별 권한 메뉴, 없으면 role_id별 메뉴 조회
		List<Map<String, Object>> list = new ArrayList<>();
		int roleId = roleMapper.hasMemberRoleId(param);
		param.put("utype", param.get("login_utype"));
		param.put("role_id", roleId);
		param.put("login_agency_id", 0);
		list = roleMapper.getRoleMenuList(param);
		
		list = menuService.getTreeMenuList(list);
		respMap.setBody("list", list);
		
		return respMap.getResponse();
	}
}