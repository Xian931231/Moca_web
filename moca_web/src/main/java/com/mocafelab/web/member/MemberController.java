package com.mocafelab.web.member;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.vo.RequestMap;

@RestController
@RequestMapping("${apiPrefix}/member")
public class MemberController {

	@Autowired
	private MemberService memberService;
	
	/**
	 * 사용자 등록
	 * @param reqMap
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/signUp/add")
	public Map<String, Object> addMember(RequestMap reqMap, MultipartHttpServletRequest mRequest) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "uid");
		CommonUtil.checkNullThrowException(param, "passwd");
		CommonUtil.checkNullThrowException(param, "uname");
		
		return memberService.addMember(param, mRequest);
	}
	
	/**
	 * 아이디 중복 체크
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/signUp/duplicate/id")
	public Map<String, Object> hasDuplicateId(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "uid");
		
		return memberService.hasDuplicateId(param);
	}
	
	/**
	 * 인증번호 이메일 발송
	 * @param reqMap
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/signUp/send/email")
	public Map<String, Object> sendEmail(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "company_email");
		
		return memberService.sendMail(param);
	}
	
	/**
	 * 인증번호 체크
	 * @param reqMap
	 * @param reqeust
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/signUp/auth/check")
	public Map<String, Object> hasAuth(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "company_email");
		CommonUtil.checkNullThrowException(param, "auth_value");
		
		return memberService.hasAuth(param);
	}
	
	/**
	 * 아이디 찾기
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/signUp/find/id")
	public Map<String, Object> getId(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "email");
		
		return memberService.getId(param);
	}
	
	/**
	 * 비밀번호 찾기
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/signUp/find/pw")
	public Map<String, Object> getPw(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "uid");
		CommonUtil.checkNullThrowException(param, "email");
		
		return memberService.getPw(param);
	}
	
	/**
	 * 비밀번호 변경
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/modify/pw")
	public Map<String, Object> modifyPw(HttpServletRequest request, RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "passwd");
		CommonUtil.checkNullThrowException(param, "new_passwd");
		CommonUtil.checkNullThrowException(param, "confirm_passwd");
		
		return memberService.modifyPw(request, param);
	}
	
	/**
	 * 30일 후에 비밀번호 변경
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/modify/pw/later")
	public Map<String, Object> modifyPwLater(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return memberService.modifyPwLater(param);
	}
	
	/**
	 * 로그인 후 본인 정보 조회
	 * @param request
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/myData/get")
	public Map<String, Object> getMyData(HttpServletRequest request, RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return memberService.getMyData(request, param);
	}
	
	/**
	 * 로그인 후 본인 정보 조회(마스킹 버전)
	 * @param request
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/myData/get/masking")
	public Map<String, Object> getMyMaskingData(HttpServletRequest request, RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return memberService.getMyMaskingData(request, param);
	}
	
	/**
	 * 회원 정보 수정 (대행사, 광고주)
	 * @param request
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/modify/info")
	public Map<String, Object> modifyInfo(HttpServletRequest request, RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return memberService.modifyInfo(request, param);
	}
	
	/**
	 * 사업자 정보 수정 요청
	 * @param reqMap
	 * @param mRequest
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/modify/request")
	public Map<String, Object> modifyRequest(RequestMap reqMap, MultipartHttpServletRequest mRequest) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return memberService.modifyRequest(param, mRequest);
	}
	
	/**
	 * 수정 요청 목록
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/modify/request/list")
	public Map<String, Object> getModifyRequestList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return memberService.getModifyRequestList(param);
	}
	
	/**
	 * 정보 수정
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/modify/companyInfo")
	public Map<String, Object> modifyCompanyInfo(RequestMap reqMap, MultipartHttpServletRequest mRequest) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		//CommonUtil.checkNullThrowException(param, "member_update_req_id");
		
		return memberService.modifyCompanyInfo(param, mRequest);
	}
	
	/**
	 * 종료되지 않은 광고가 있는지 체크
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/processing/Sg")
	public Map<String, Object> getProcessingSg(RequestMap reqMap) throws Exception{
		Map<String, Object> param = reqMap.getMap();
		
		return memberService.getProcessingSg(param);
	}
	
	/**
	 * 종료되지 않은 광고가 있는지 체크
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/processing/product")
	public Map<String, Object> getProcessingProduct(RequestMap reqMap) throws Exception{
		Map<String, Object> param = reqMap.getMap();
		
		return memberService.getProcessingProduct(param);
	}
	
	/**
	 * 회원 비밀번호 확인
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/hasPassword")
	public Map<String, Object> hasPassword(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "passwd");
		
		return memberService.hasPassword(param);
	}
	
	/**
	 * 회원 탈퇴 요청
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/leave/request")
	public Map<String, Object> leaveRequestMember(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "passwd");
		
		return memberService.leaveRequestMember(param);
	} 
	

	/**
	 * 회원 탈퇴 처리
	 * @param reqMap 
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/leave/request/accept")
	public Map<String, Object> leaveRequestAccept(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "member_id");
		CommonUtil.checkNullThrowException(param, "passwd");
		
		return memberService.leaveRequestAccept(param);
	} 
	
	/**
	 * 회원 탈퇴 요청 목록
	 * @param reqMap 
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/leave/request/list")
	public Map<String, Object> getLeaveRequestList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return memberService.getLeaveRequestList(param);
	} 
	
	/**
	 * 회원 목록 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/list")
	public Map<String, Object> getAllList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
	
		return memberService.getList(param);
	}
	
	/**
	 * 회원 전체 목록 조회(관리자 제외,대행사 최고 관리자만)
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/user/list")
	public Map<String, Object> getMemberUserList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
	
		return memberService.getMemberUserList(param);
	}
	
	/**
	 * 회원 전체 목록 엑셀 다운로드
	 * @param reqMap
	 * @param response
	 * @throws Exception
	 */
	@PostMapping("/user/list/excel")
	public void getMemberUserExcel(RequestMap reqMap, HttpServletResponse response) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		memberService.getMemberUserExcel(param, response);
	}
	
	/**
	 * 권한별 회원 개수 조회 (관리자 제외)
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/role/cnt")
	public Map<String, Object> getRoleMemberCnt(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return memberService.roleMemberCnt(param);
	}
	
	/**
	 * 회원 1명 상세 정보 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/all/detail")
	public Map<String, Object> getDetail(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
	
		CommonUtil.checkNullThrowException(param, "member_id");
		
		return memberService.getDetail(param);
	}
	
	/**
	 * 회원의 사업자 정보 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/company/detail")
	public Map<String, Object> getMemberCompanyInfo(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
	
		CommonUtil.checkNullThrowException(param, "request_id");
		
		return memberService.getMemberCompanyInfo(param);
	}
	
	
	/**
	 * 회원 로그인 이력 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/all/loginHistory/list")
	public Map<String, Object> getLoginHistory(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
	
		return memberService.getLoginHistory(param);
	}
	
	/**
	 * 관리자 페이지 회원 리스트 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/staff/list")
	public Map<String, Object> getStaffList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return memberService.getStaffList(param);
	
	}

	/**
	 * 관리자 수정이력 목록 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/staff/history/list")
	public Map<String, Object> getStaffHistoryList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "member_id");
		
		return memberService.getStaffHistoryList(param);
	}
	
	/**
	 * 관리자 추가
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/staff/add")
	public Map<String, Object> addStaff(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "uid");
		CommonUtil.checkNullThrowException(param, "uname");
		CommonUtil.checkNullThrowException(param, "role_id");
		CommonUtil.checkNullThrowException(param, "company_email");
		
		return memberService.addStaff(param);
	}
	
	
	/**
	 * 관리자 권한구분 수정
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/staff/role/modify")
	public Map<String, Object> modifyStaffRole(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "member_id");
		CommonUtil.checkNullThrowException(param, "role_id");
		
		return memberService.modifyStaffRole(param);
	}
	
	/**
	 * 관리자 본인 계정 수정 (권한구분은 x)
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/myInfo/modify")
	public Map<String, Object> modifyMyInfo(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return memberService.modifyMyInfo(param);
	}
	
	/**
	 * 관리자 회원 삭제 (개인권한 동시 삭제)
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/staff/remove")
	public Map<String, Object> removeStaff(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();

		CommonUtil.checkNullThrowException(param, "staff_id_list");
		CommonUtil.checkNullThrowException(param, "passwd");
		CommonUtil.checkNullThrowException(param, "leave_reason");
		
		return memberService.removeStaff(param);
	}
	
	/**
	 * 로그인한 사용자의 메뉴 목록 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/menu/list")
	public Map<String, Object> getMenuList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();

		CommonUtil.checkNullThrowException(param, "login_id");
		CommonUtil.checkNullThrowException(param, "login_utype");
		CommonUtil.checkNullThrowException(param, "login_role_id");
		
		param.put("utype", param.get("login_utype"));
		param.put("role_id", param.get("login_role_id"));
		
		return memberService.getMenuList(param); 
	}
	
	/**
	 * 매체사 승인요청 목록
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/supply/approve/list")
	public Map<String, Object> getSupplyApproveList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return memberService.getSupplyApproveList(param);
	}
	
	/**
	 * 매체 승인 / 거부
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/supply/approve")
	public Map<String, Object> modifySupplyStatus(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return memberService.modifySupplyStatus(param);
	}
	
	// 매체/상품 관리 > 분류 관리 > 매체사 목록 조회
	@PostMapping("/supply/list")
	public Map<String, Object> getSupplyList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return memberService.getSupplyList(param);
	}
	
	/**
	 * 광고주 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/demand/list")
	public Map<String, Object> getDemandList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return memberService.getDemandList(param);
	}
	
}
