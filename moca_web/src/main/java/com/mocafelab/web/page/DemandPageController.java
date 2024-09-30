package com.mocafelab.web.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/demand")
public class DemandPageController {
	/**
	 * 광고주 페이지 컨트롤러
	 */
	
	// 광고 현황 (광고주 메인)
	@GetMapping("/main") 
	public String dspMain() {
		return "demand/main";
	}
	
	// * 광고 관리 탭 (/campaign/ ~ ) start
	// ***** 캠페인 관련 ******
	// 캠페인 리스트
	@GetMapping("/campaign/list") 
	public String dspList() {
		return "demand/campaign/list";
	}

	// 새 캠페인 만들기
	@GetMapping("/campaign/add") 
	public String dspAdd() {
		return "demand/campaign/add";
	}
	
	// 캠페인 수정
	@GetMapping("/campaign/modify")
	public String demandCampaignModify() {
		return "demand/campaign/modify";
	}
	
	// ***** end *****

	
	// ***** 광고 관련 *****
	// 캠페인 내 광고 리스트
	@GetMapping("/campaign/sg/list")
	public String demandCampaignSgList() {
		return "demand/campaign/sg/list";
	}
	
	@GetMapping("/campaign/sg/detail")
	public String demandCampaignSgDetail() {
		return "demand/campaign/sg/detail";
	}
	
	// 광고 등록 CPM
	@GetMapping("/campaign/sg/addcpm")
	public String sgAddCpm() {
		return "demand/campaign/sg/addCpm";
	}
	
	// 광고 등록 CPP
	@GetMapping("/campaign/sg/addcpp")
	public String sgAddCpp() {
		return "demand/campaign/sg/addCpp";
	}
	
	// 광고신청 정보 수정 CPM
	@GetMapping("/campaign/sg/modifycpm")
	public String sgModifyCpm() {
		return "demand/campaign/sg/modifyCpm";
	}
	
	// 광고신청 정보 수정 CPP
	@GetMapping("/campaign/sg/modifycpp")
	public String sgModifyCpp() {
		return "demand/campaign/sg/modifyCpp";
	}
	
	// ***** end *****
	
	// * 리포트 탭 (/report/ ~ ) start
	@GetMapping("/report/period")
	public String reportPeriodList() {
		return "demand/report/period";
	}
	@GetMapping("/report/supply")
	public String reportSupplyList() {
		return "demand/report/supply";
	}
	@GetMapping("/report/area")
	public String reportRegionList() {
		return "demand/report/area";
	}
	// * 리포트 탭 (/report/ ~ ) end
	
	// * 광고 관리 탭 end
	
	// * 회원 관리 탭 (/member/ ~ ) start
	@GetMapping("/member/cost/list")
	public String sgAmountList() {
		return "demand/member/cost/list";
	}
	//활동 이력 조회
	@GetMapping("/member/histroy/list")
	public String modifyHistoryList() {
		return "demand/member/history/list";
	}
	
	//회원 정보 수정
	@GetMapping("/member/info/modify")
	public String modifyMemberInfo() {
		return "demand/member/info/modify";
	}
	// * 회원 관리 탭 (/member/ ~ ) end
	
}
