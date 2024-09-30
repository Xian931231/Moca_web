package com.mocafelab.web.member;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberMapper {
	
	// 사용자 등록
	public Map<String, Object> addMember(Map<String, Object> param);

	// 사업자 등록증 파일 경로 등록
	public int addMemberBiznumPath(Map<String, Object> param);

	// 아이디 중복 체크
	public int hasDuplicateId(Map<String, Object> param);

	// 이메일 중복 체크
	public int hasDuplicateEmail(Map<String, Object> param);
	
	// 이메일 인증 확인
	public int hasEmailAuth(Map<String, Object> param);
	
	// 인증 테이블 확인
	public int hasAuthCnt(Map<String, Object> param);
	
	// 인증번호 저장
	public int addAuth(Map<String, Object> param);

	// 인증번호 업데이트
	public int modifyAuth(Map<String, Object> param);
	
	// 인증번호 조회
	public Map<String, Object> hasAuth(Map<String, Object> param);
	
	// 인증 완료 
	public int modifyAuthSuccess(Map<String, Object> param);
	
	// 아이디 찾기, 비밀번호 찾기 회원 정보
	public Map<String, Object> getMemberInfo(Map<String, Object> param);
	
	// 비밀번호 찾기, 변경
	public int modifyNewPw(Map<String, Object> param);

	// 30일 후에 비밀번호 변경
	public int modifyPwLater(Map<String, Object> param);
	
	// 로그인한 계정의 현재 비밀번호 확인
	public String getSavePassword(Map<String, Object> param);
	
	// 로그인 후 본인 정보 조회
	public Map<String, Object> getMyData(Map<String, Object> param);
	
	// 로그인 후 본인 정보 조회
	public Map<String, Object> getMyMaskingData(Map<String, Object> param);
	
	// 회원 정보 수정
	public int modifyInfo(Map<String, Object> param);
	
	// 사업자 정보 수정 요청 유무 확인
	public Map<String, Object> getModifyRequest(Map<String, Object> param);

	// 사업자 정보 수정 요청 추가
	public int removeModifyRequest(Map<String, Object> param);
	
	// 사업자 정보 수정 요청 업데이트
	public int modifyRequest(Map<String, Object> param);
	
	// 수정 요청 목록
	public List<Map<String, Object>> getModifyRequestList(Map<String, Object> param);
	
	// 수정 요청 목록 개수
	public int getModifyRequestCnt(Map<String, Object> param);
	
	// 수정할 회원 정보
	public Map<String, Object> getModifyMemberInfo(Map<String, Object> param);
	
	// 정보 수정
	public int modifyCompanyInfo(Map<String, Object> param);
	
	// 수정 요청 상태 변경
	public int modifyRequestStatus(Map<String, Object> param);
	
	// 승인요청, 진행중인 광고 갯수 조회
	public int getProcessingSgCnt(Map<String, Object> param);
	
	// 진행중인 상품 갯수 조회
	public int getProcessingProductCnt(Map<String, Object> param);

	// 회원 탈퇴 요청 
	public int leaveRequestMember(Map<String, Object> param);

	// 회원 탈퇴 승인
	public int leaveRequestAccept(Map<String, Object> param);
	
	// 회원 탈퇴 요청 목록 조회
	public int leaveRequestMemberListCnt(Map<String, Object> param);
	public List<Map<String, Object>> leaveRequestMemberList(Map<String, Object> param);
	
	// 회원 목록 개수
	public int getMemberListCnt(Map<String, Object> param);
	// 회원 목록 조회
	public List<Map<String, Object>> getMemberList(Map<String, Object> param);
	
	//회원 전체 목록 조회
	public List<Map<String, Object>> getMemberUserList(Map<String, Object> param);
	
	//회원 전체 목록 개수
	public int getMemberUserListCnt(Map<String, Object> param);
	
	public Map<String, Object> getRoleMemberCnt(Map<String, Object> param);
	
	// 회원 상세 정보
	public Map<String, Object> getMemberData(Map<String, Object> param);
	
	// 회원 사업자 정보 조회
	public Map<String, Object> getMemberCompanyInfo(Map<String, Object> param);
	
	// 회원 로그인 이력 조회
	public List<Map<String, Object>> getMemberLoginHistory(Map<String, Object> param);
		
	// 회원 로그인 이력 개수
	public int getMemberLoginHistoryCnt(Map<String, Object> param);

	// 권한구분 조회
	public int getRole(Map<String, Object> param);
	
	//수정하려는 회원의 이메일과 동일한지 조회
	public int hasEqualsMemberEmail(Map<String, Object> param);
	
	// ############################### 관리자 회원 관리 #################################
	// 관리자 목록 조회
	public List<Map<String, Object>> getStaffList(Map<String, Object> param);
	// 관리자 count
	public int getStaffListCount(Map<String, Object> param);
	
	// 관리자 상세
	public Map<String, Object> getStaffDetail(Map<String, Object> param);

	// 관리자 수정 이력 조회
	public List<Map<String, Object>> getStaffHistoryList(Map<String, Object> param);	
	
	// 관리자 추가
	public int addStaff(Map<String, Object> param);
	// 관리자 권한구분인지 검사
	public int isRoleStaff(Map<String, Object> param);
	
	// 관리자 권한구분 수정
	public int modifyStaffRole(Map<String, Object> param);
	// 현재 비밀번호 맞는지 체크
	public int isPasswd(Map<String, Object> param);
	
	// 인증완료가 되었는지 체크
	public int isSuccessAuth(Map<String, Object> param);
	
	// 본인 정보 수정 (관리자)
	public int modifyMyInfo(Map<String, Object> param);
	
	// 관리자의 이메일 존재하는지 체크
	public int hasStaffEmail(Map<String, Object> param);
	
	// 최고 관리자 권한을 가진 회원인지 검사
	public int isSuperStaff(Map<String, Object> param);
	// 관리자 삭제
	public int removeStaff(Map<String, Object> param);
	
	// 직원 존재 여부 확인
	public int hasStaff(Map<String, Object> param);
	
	// 관리자 이력 추가
	public int addStaffHistory(Map<String, Object> param);

	
	// 해당 사용자의 메뉴 목록 조회 
	public List<Map<String, Object>> getMenuList(Map<String, Object> param);
	
	// 디폴트 메뉴 조회
	public Map<String, Object> getDefaultMenu(Map<String, Object> param);
	
	//대행사 기본 권한 등록
	public List<Map<String, Object>> addDefaultRole(Map<String, Object> param);
	
	//대행사 권한별 메뉴 조회
	public List<Map<String, Object>> getAgencyDefaultMenuList(Map<String, Object> param); 

	//대행사 기본 권한별 메뉴 등록
	public int addAgencyRoleMenu(Map<String, Object> param);
	
	//대행사 계정 role_id 수정
	public int modifyRoleId(Map<String, Object> param);
	
	// 매체사 승인요청 목록
	public List<Map<String, Object>> getSupplyApproveList(Map<String, Object> param);
	
	// 매체사 승인요청 목록 개수
	public int getSupplyApproveCnt(Map<String, Object> param);
	
	// 매체 승인 / 거부
	public int modifySupplyStatus(Map<String, Object> param);
	
	// 광고주 조회
	public List<Map<String, Object>> getDemandAgencyList(Map<String, Object> param);
	public int getDemandAgencyListCount(Map<String, Object> param);
	
	// 관리자 > 매체/상품 관리 매체사 목록 조회
	public List<Map<String, Object>> getProductSupplyList(Map<String, Object> param);
	
	// 관리자 > 매체/상품 관리 매체사 목록 개수
	public int getProductSupplyCnt(Map<String, Object> param);

	// (상품관리) 매체사 조회
	public List<Map<String, Object>> getSupplyList(Map<String, Object> param);
	
	// (상품관리) 매체사 개수
	public int getSupplyCnt(Map<String, Object> param);

	// 광고주 목록 조회
	public List<Map<String, Object>> getDemandList(Map<String, Object> param);
	public int getDemandListCount(Map<String, Object> param);

}
