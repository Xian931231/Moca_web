package com.mocafelab.web.page;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mocafelab.web.login.SessionUtil;

import net.newfrom.lib.vo.RequestMap;

@Controller
@RequestMapping("/agency")
public class AgencyPageController {
	
	@Autowired
	private SessionUtil sessionUtil;
	
	/**
	 * 대행사 페이지 컨트롤러
	 */
	
	// 광고 현황 (대행사 메인)
	@GetMapping("/main") 
	public String agencyMain() {
		return "agency/main";
	}
	
	// * 광고 관리 탭 (/campaignManage/ ~) start
	// 광고 관리 (캠페인/광고 관리)
	@GetMapping("/campaign/sg/list") 
	public String agencyCampaignList(RequestMap requestMap) {
		Map<String, Object> param = requestMap.getMap();
		
		String loginId = String.valueOf(param.get("login_id"));
		String agencyId = String.valueOf(param.get("login_agency_id"));
		
		if (loginId.equals(agencyId)) {
			return "agency/campaign/sg/agencyList";
		}
		return "agency/campaign/sg/staffList";
		
	}
	// 광고 집행요청 관리
	@GetMapping("/campaign/cost/list") 
	public String agencyCostList(RequestMap requestMap) {
		Map<String, Object> param = requestMap.getMap();
		
		String loginId = String.valueOf(param.get("login_id"));
		String agencyId = String.valueOf(param.get("login_agency_id"));
		
		if (loginId.equals(agencyId)) {
			return "agency/campaign/cost/agencyList";
		}
		return "agency/campaign/cost/staffList";
	}
	// * 광고 관리 탭 end
	
	// * 리포트 탭 (/report/ ~ ) start
	@GetMapping("/report/demand")
	public String reportDemandList() {
		return "agency/report/demand";
	}
	@GetMapping("/report/period")
	public String reportPeriodList() {
		return "agency/report/period";
	}
	@GetMapping("/report/supply")
	public String reportSupplyList() {
		return "agency/report/supply";
	}
	@GetMapping("/report/area")
	public String reportRegionList() {
		return "agency/report/area";
	}
	// * 리포트 탭 end
	
	// * 회원관리 탭 (/member/ ~ ) start
	//회원 기본 정보 수정
	@GetMapping("/member/info/modify") 
	public String memberSimpleModify(HttpServletRequest request) {
		HttpSession session = request.getSession();
		String loginAgnecyId = sessionUtil.getLoginAgencyIdInSession(session);
		String url = "agency/member/info/simpleModify";
		
		//대행사 최고관리자일 경우 대행사 정보수정으로 이동
		if(loginAgnecyId.equals("0")) {
			url = "agency/member/info/modify";
		}
		
		return url;
	}
	// 대행사 정보 수정
	@GetMapping("/super/member/info/modify") 
	public String memberModify() {
		return "agency/member/info/modify";
	}
	//담당자 계정 관리
	@GetMapping("/member/staff/list") 
	public String memberStaffList() {
		return "agency/member/staff/list";
	}
	//담당자 계정 관리
	@GetMapping("/member/demand/list") 
	public String memberDemandList() {
		return "agency/member/demand/list";
	}
	
	//대행사 소속 광고주 계정 생성
	@GetMapping("/member/demand/add") 
	public String memberDemandadd() {
		return "agency/member/demand/add";
	}
	
	// * 회원관리 탭 end
	
}
