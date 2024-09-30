package com.mocafelab.web.role;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RoleMapper {
	
	// 권한
	public List<Map<String, Object>> getRoleList(Map<String, Object> param);
	public Map<String, Object> getRoleDetail(Map<String, Object> param);
	public int addRole(Map<String, Object> param);
	public int modifyRole(Map<String, Object> param);
	public int removeRole(Map<String, Object> param);
	
	// 권한별 메뉴
	public List<Map<String, Object>> getRoleMenuList(Map<String, Object> param);
	public int addRoleManagerMenu(Map<String, Object> param);
	public int modifyRoleManagerMenu(Map<String, Object> param);
	public int removeRoleManagerMenu(Map<String, Object> param);
	public int hasRoleManagerMenu(Map<String, Object> param);
	public int hasRoleMenu(Map<String, Object> param);
	public int hasDuplicateName(Map<String, Object> param);
	
	// 허용된 IP 인지 확인
	int hasPermitIp(Map<String, Object> param);
	
	// 관리자 권한구분 목록
	public List<Map<String, Object>> getStaffRoleList(Map<String, Object> param);
	
	
	
	// 관리자 권한 등록/수정/삭제
	public int addStaffRole(Map<String, Object> param);
	public List<Map<String, Object>> getDefaultMenu(Map<String, Object> param);
	public int addStaffRoleMenu(Map<String, Object> param);
	public int modifyStaffRoleName(Map<String, Object> param);
	public Map<String, Object> getMenu(Map<String, Object> param);
	public int removeStaffRole(Map<String, Object> param);
	public int removeStaffRoleMenu(Map<String, Object> param);
	public List<Map<String, Object>> hasMemberRole(Map<String, Object> param);
	public int modifyMemberRoleId(Map<String, Object> param);
	public int modifyStaffRoleSort(Map<String, Object> param);
	public int hasStaffRoleManager(Map<String, Object> param);
	public int hasMemberStaffRoleUtype(Map<String, Object> param);
	public int hasMemberRoleId(Map<String, Object> param);
	public Map<String, Object> getStaffDefaultMenuId(Map<String, Object> param);
	public int modifyStaffDefaultMenuId(Map<String, Object> param);
	
	// 관리자 권한구분 상세
	public Map<String, Object> getStaffRoleDetail(Map<String, Object> param);
}