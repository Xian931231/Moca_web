package com.mocafelab.web.member;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AgencyMemberMapper {
	// 대행사 아이디 조회
	public int getAgencyId(Map<String, Object> param);
	// 직원 등록
	public int addMember(Map<String, Object> param);						
	// 직원 리스트 조회
	public List<Map<String, Object>> getList(Map<String, Object> param);
	// 직원 리스트 총 개수
	public int getListCnt(Map<String, Object> param);
	// 직원 정보 수정
	public int modifyMember(Map<String, Object> param);
	// 대행사 계정 탈퇴시 대행사 직원 탈퇴 처리
	public int setMemberLeave(Map<String, Object> param);
	// 직원 탈퇴
	public int removeMember(Map<String, Object> param);
	//종료되지 않은 광고가 있는지 체크
	public int getProcessingSgCntByAgency(Map<String, Object> param);
	
	//대행사 마스킹 상세 정보
	public Map<String, Object> getAgencyMaskingData(Map<String, Object> param);
	//대행사 상세 정보
	public Map<String, Object> getAgencyData(Map<String, Object> param);
	
	// 대행사로 가입된 멤버 조회
	Map<String, Object> getAgencyMember(Map<String, Object> param);
	// 대행사의 광고주 조회
	Map<String, Object> getDemandMember(Map<String, Object> param);
	// 대행사의 소속 광고주인지 확인
	int hasDemandMember(Map<String, Object> param);
	// 종료되지 않은 광고가 있는지 체크 대행사 광고주 용
	int getProcessingSgCntByMember(Map<String, Object> param);
	
	//  대행사 소속 광고주 목록 (팀, 직원 중복 허용) - 광고주 계정 관리 용도
	List<Map<String, Object>> getDemandList(Map<String, Object> param);
	// 대행사 소속 광고주 목록 카운트
	int getDemandListCount(Map<String, Object> param);

	// 구분별 담당 광고주 목록
	List<Map<String, Object>> getPartDemandList(Map<String, Object> param);
	// 개인별 담당광고주 목록
	List<Map<String, Object>> getPersonalDemandList(Map<String, Object> param);
	// 대행사 로그인된 직원의 구분별 담당 광고주 목록
	List<Map<String, Object>> getMemberPartDemandList(Map<String, Object> param);
	// 대행사 로그인된 직원의 개인별 담당 광고주 목록
	List<Map<String, Object>> getMemberPersonalDemandList(Map<String, Object> param);
	
	// 해당 구분(권한)으로 접근 가능 여부
	int hasAccessPart(Map<String, Object> param);
	// 해당 담당자 접근 가능 여부
	int hasAccessStaff(Map<String, Object> param);

	// 대행사 소속 광고주 목록 
	List<Map<String, Object>> getAgencyDemandList(Map<String, Object> param);
	//
	List<Map<String, Object>> getAgencyStaffList(Map<String, Object> param);
	
	// 대행사 소속 광고주와 직원 목록
	List<Map<String, Object>> getAgencyDemandStaffList(Map<String, Object> param);
	// 대행사 소속 광고주와 직원 목록 카운트
	int getAgencyDemandStaffCount(Map<String, Object> param);
	// 대행사 소속 광고주 캠페인 목록 
	List<Map<String, Object>> getDemandCampaignList(Map<String, Object> param);
	// 캠페인의 광고 목록
	List<Map<String, Object>> getCampaignSgList(Map<String, Object> param);
	// 대행사 소속 광고주의 캠페인 및 광고 조인
	List<Map<String, Object>> getDemandCostList(Map<String, Object> param);
	// 대행사 소속 광고주의 캠페인 및 광고 카운트
	int getDemandCostListCount(Map<String, Object> param);
	
	
	//구분별 소속의 직원 리스트
	public List<Map<String, Object>> getRoleStaffList(Map<String, Object> param);
	public int getRoleStaffListCnt(Map<String, Object> param);
	//광고주별 담당 직원 리스트
	public List<Map<String, Object>> getDemandAccessStaffList(Map<String, Object> param);
	//대행사 소속 직원인지 체크
	public int hasAgencyStaff(Map<String, Object> param);
	
	//광고주별 담당직원 삭제(모두)
	public int removeDemandAccessStaff(Map<String, Object> param);
	
	// 대행사 소속 광고주 삭제(탈퇴 요청)
	int removeDemand(Map<String, Object> param);
	
	//광고주 기본 권한 
	int defaultDemandRole(Map<String, Object> param);
	
	// 광고주 등록
	Map<String, Object> addDemand(Map<String, Object> param);
	// 광고주 수정
	int modifyDemand(Map<String, Object> param);
	
	// 광고주 담당자 목록
	public List<Map<String, Object>> getDemandAgencyMemberList(Map<String, Object> param);
	// 담당자 리스트
	public List<Map<String, Object>> getAgencyMemberList(Map<String, Object> param);
	// 활동 이력 등록 
	public int addMemberModifyHistory(Map<String, Object> param);
	// 활동 이력 조회
	public List<Map<String, Object>> getMemberModifyHistoryList(Map<String, Object> param);
	
	public List<Map<String, Object>> getAgencyWithDemandList(Map<String, Object> param);
	
	public List<Map<String, Object>> getDemandCampaginSgList(Map<String, Object> param);
}
