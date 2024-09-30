package com.mocafelab.web.role;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface AgencyRoleMapper {
	
	//권한 구분 리스트 조회
	public List<Map<String, Object>> getRoleList(Map<String, Object> param);

	//권한 상세
	public Map<String, Object> getRoleDetail(Map<String, Object> param);
	//권한구분 등록
	public int addRole(Map<String, Object> param);
	
	//대행사의 최고관리자 정보 조회
	public Map<String, Object>getAgencySuperRole(Map<String, Object> param);
	
	//권한구분 명 수정
	public int modifyRoleName(Map<String, Object> param);
	
	//권한구분 순서 수정
	public int modifyRoleSort(Map<String, Object> param);
	
	//권한구분 조회
	public List<Map<String, Object>> getUtypeList(Map<String, Object> param);
	
	//광고주 조회
	public int hasDsp(Map<String, Object> param);
	
	public int hasRoleManager(Map<String, Object> param);
	
	//last sort 조회
	public int hasLastSort(Map<String, Object> param);
	
	//기본 권한 등록
	public int addRoleMenu(Map<String, Object> param);
	
	//권한구분 삭제
	public int removeRoleManager(Map<String, Object> param);
	//권한구분의 페이지 접근권한 삭제
	public int removeRoleMenu(Map<String, Object> param);
	//권한구분 수정용 권한구분의 페이지 접근권한 삭제 
	public int modifyRoleremoveMenu(Map<String, Object> param);
	
	public int removeMemberRoleId(Map<String, Object> param);
	public int hasStaffRoleManager(Map<String, Object> param);
	//role_manager > use_yn = 'N'
	public int disabledRoleId(Map<String, Object> param);
	
	//권한구분명 중복 체크
	public int hasDuplicateName(Map<String, Object> param);
	
	//담당 광고주 삭제
	public int removeRoleAccessDsp(Map<String, Object> param);
	
	//담당 등록
	public int addDsp(Map<String, Object> param);
	
	/////////////////// 개인별 ///////////////////////
	
	//직원 조회
	public Map<String, Object> getStaffData(Map<String, Object> param);
	
	//직원 담당 광고주 조회
	public int hasStaffAccessDsp(Map<String, Object> param);
	
	//직원의 role_id 변경
	public int modifyStaffRoleId(Map<String, Object> param);
	
	//대행사 직원의 광고주 등록
	public int addStaffAccessDsp(Map<String, Object> param);
	
	//대행사 직원의 광고주 삭제
	public int removeStaffAccessDsp(Map<String, Object> param);
	
	//대행사 계정의 미지정 권한 조회
	public int hasUnratedRoleId(Map<String, Object> param);
	
	//대행사 직원의 담당 광고주 조회
	public List<Map<String, Object>> getAgencyDemendList(Map<String, Object> param);
	
	// 광고주 정보 
	public List<Map<String, Object>> getDemendList(Map<String, Object> param);
}
