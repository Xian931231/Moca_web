package com.mocafelab.web.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPageController {
	/*
	 * 관리자 페이지 컨트롤러
	 */
	
	// 대시보드 (관리자 메인)
	@GetMapping("/dashboard")
	public String adminMain() {
		return "admin/dashboard";
	}
	
	// * 광고 관리 탭 (/campaignManage/ ~) start
	// 소재 검수/승인 
	@GetMapping("/campaign/inspection") 
	public String adminCampaignInspectionList() {
		return "admin/campaign/inspection/list";
	}
	
	// 소재 상세 (검수 페이지)
	@GetMapping("/campaign/inspection/detail")
	public String adminCampaignInspectionDetail() {
		return "admin/campaign/inspection/detail";
	}
	
	// 캠페인/광고 관리
	@GetMapping("/campaign/sg")
	public String adminCampaignSgList() {
		return "admin/campaign/sg/list";
	}
	
	// 광고 관리 (디폴트 광고 관리)
	@GetMapping("/campaign/default")
	public String adminDefault() {
		return "admin/campaign/defaults/list";
	}
	
	// 광고 편성 관리 목록
	@GetMapping("/campaign/schedule/list")
	public String adminScheduleList() {
		return "admin/campaign/schedule/list";
	}
	
	// 광고 편성 관리 등록
	@GetMapping("/campaign/schedule/add")
	public String adminScheduleAdd() {
		return "admin/campaign/schedule/add";
	}
	
	// 광고 편성 관리 수정
	@GetMapping("/campaign/schedule/modify")
	public String adminScheduleModify() {
		return "admin/campaign/schedule/modify";
	}
	
	// CPP 지정 순서 변경
	@GetMapping("/campaign/schedule/modify/order")
	public String adminScheduleModifyOrder() {
		return "admin/campaign/schedule/modifyOrder";
	}
	
	// 시간옵션 관리
	@GetMapping("/campaign/weektime")
	public String adminWeetime() {
		return "admin/campaign/weektime/list";
	}
	
	// 광고 오류 관리
	@GetMapping("/campaign/abnormal")
	public String adminSgAbnormal() {
		return "admin/campaign/abnormal/list";
	}
	
	// 종료 광고 관리
	@GetMapping("/campaign/end")
	public String adminSgEnd() {
		return "admin/campaign/end/list";
	}
	// * 광고 관리 탭 end
	
	
	// * 광고 리포트 탭 (/report/ ~) start
	// 광고주별 리포트 
	@GetMapping("/report/demand")
	public String reportDemand() {
		return "admin/report/demand";
	}
	
	// 매체별 리포트 
	@GetMapping("/report/supply")
	public String reportSupply() {
		return "admin/report/supply";
	}
	
	// 상품별 리포트
	@GetMapping("/report/product")
	public String reportProduct() {
		return "admin/report/product";
	}
	
	// 지역별 리포트
	@GetMapping("/report/area")
	public String reportArea() {
		return "admin/report/area";
	}
	// * 광고 리포트 탭 end
	
	// * 매체 관리 탭 (???) start
	// 매체/상품 관리 
	@GetMapping("/ssp/product")
	public String supplyProduct() {
		return "admin/supply/product/list";
	}

	// 매체 승인 
	@GetMapping("/ssp/approval")
	public String supplyApproval() {
		return "admin/supply/approval/list";
	}
	
	// 광고 오류 조회 
	@GetMapping("/ssp/abnormal")
	public String supplyAbnormal() {
		return "admin/supply/abnormal/list";
	}
	
	// 정산 관리 
	@GetMapping("/ssp/balance")
	public String supplyBalance() {
		return "admin/supply/balance/list";
	}
	
	// 디바이스 별 현황
	@GetMapping("/ssp/device")
	public String supplyDevice() {
		return "admin/supply/device/list";
	}
	// * 매체 관리 탭 end
	
	// * 회원관리 탭 (/member/ ~ ) start
	
	// 광고주 정보 수정 요청 관리
	@GetMapping("/member/demand/list")
	public String demandModifyReqestList(){
		return "admin/member/demand/list";
	}
	
	// 매체 정보 수정 요청 관리
	@GetMapping("/member/supply/list")
	public String supplyModifyReqestList(){
		return "admin/member/supply/list";
	}
	
	// 사업자 정보 입력
	@GetMapping("/member/info/company/modify")
	public String companyModify(){
		return "admin/member/info/companyModify";
	}
	
	//탈퇴 요청 관리
	@GetMapping("/member/leave/list")
	public String leaveRequestList(){
		return "admin/member/leave/list";
	}
	
	//회원 기본 정보 수정
	@GetMapping("/member/info/modify") 
	public String memberModify() {
		return "admin/member/info/modify";
	}
	
	//관리자 권한 설정
	@GetMapping("/member/staff/list")
	public String memberStaffList() {
		return "admin/member/staff/list";
	}
	
	//전체 회원정보
	@GetMapping("/member/entire/all/list") 
	public String allMemberList() {
		return "admin/member/entire/list";
	}
	// * 회원관리 탭 end
	
}
