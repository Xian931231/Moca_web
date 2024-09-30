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
@RequestMapping("${apiPrefix}/member/agency")
public class AgencyMemberController {

	@Autowired
	private AgencyMemberService agencyMemberService;
	
	/**
	 * 직원 리스트
	 * @param reqMap
	 * @return
	 */
	@PostMapping("/staff/list")
	public Map<String, Object> getList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return agencyMemberService.getList(param);
	}
	
	/**
	 * 로그인 후 대행사(최고관리자) 정보 조회
	 * @param request
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/data/get")
	public Map<String, Object> getMyAgencyData(HttpServletRequest request, RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return agencyMemberService.getMyAgencyData(request, param);
	}
	
	/**
	 * 직원 등록
	 * @param reqMap
	 * @return
	 */
	@PostMapping("/staff/add")
	public Map<String, Object> addMember(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "uid");
		CommonUtil.checkNullThrowException(param, "uname");
		CommonUtil.checkNullThrowException(param, "company_email");
		CommonUtil.checkNullThrowException(param, "role_id"); 
		
		return agencyMemberService.addMember(param);
	}
	
	/**
	 * 직원 정보 수정
	 * @param reqMap
	 * @return
	 */
	@PostMapping("/staff/modify")
	public Map<String, Object> modifyMember(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "member_id");
		
		return agencyMemberService.modifyMember(param);
	}
	
	/**
	 * 직원 삭제
	 * @param reqMap
	 * @return
	 */
	@PostMapping("/staff/remove")
	public Map<String, Object> removeMember(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "staff_id_list");
		CommonUtil.checkNullThrowException(param, "passwd");
		CommonUtil.checkNullThrowException(param, "leave_reason");
		
		return agencyMemberService.removeMember(param);
	}
	/**
	 * 구분별 소속 직원 리스트
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/role/staff/list")
	public Map<String, Object> getRoleStaffList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return agencyMemberService.getRoleStaffList(param);
	}
	/**
	 * 광고주별 담당 직원 리스트
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/demand/access/staff/list")
	public Map<String, Object> getDemandAccessStaffList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "demand_id");
		
		return agencyMemberService.getDemandAccessStaffList(param);
	}
	/**
	 * 광고주별 담당 직원 수정
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/demand/access/staff/modify")
	public Map<String, Object> modifyDemandAccessStaff(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "demand_id");
		
		return agencyMemberService.modifyDemandAccessStaff(param);
	}
	
	/**
	 * 대행사 소속 광고주 계정 목록 조회
	 * @param requestMap
	 * @return
	 * @throws Exception 
	 */
	@PostMapping("/demand/list")
	public Map<String, Object> getDemandList(RequestMap requestMap) throws Exception {
		Map<String, Object> param = requestMap.getMap();
		
		return agencyMemberService.getDemandList(param);
	}
	
	/**
	 * 구분별 담당 광고주 목록 조회
	 * @param requestMap
	 * @return
	 * @throws Exception 
	 */
	@PostMapping("/demand/part/list")
	public Map<String, Object> getPartDemandList(RequestMap requestMap) throws Exception {
		Map<String, Object> param = requestMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "role_id");
		
		return agencyMemberService.getPartDemandList(param);
	}
	
	/**
	 * 종료되지 않은 광고가 있는지 체크
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/processing/Sg")
	public Map<String, Object> getProcessingSgByAgency(RequestMap reqMap) throws Exception{
		Map<String, Object> param = reqMap.getMap();
		
		return agencyMemberService.getProcessingSgByAgency(param);
	}
	
	/**
	 * 개인별 담당 광고주 목록 조회
	 * @param requestMap
	 * @return
	 * @throws Exception 
	 */
	@PostMapping("/demand/personal/list")
	public Map<String, Object> getPersonalDemandList(RequestMap requestMap) throws Exception {
		Map<String, Object> param = requestMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "member_id");
		
		return agencyMemberService.getPersonalDemandList(param);
	}
	
	/**
	 * 대행사 로그인된 직원의 구분별 담당 광고주 목록
	 * @param requestMap
	 * @return
	 * @throws Exception 
	 */
	@PostMapping("/demand/self/part/list")
	public Map<String, Object> getMemberPartDemandList(RequestMap requestMap) {
		Map<String, Object> param = requestMap.getMap();
		
		return agencyMemberService.getMemberPartDemandList(param);
	}
	
	/**
	 * 대행사 로그인된 직원의 개인별 담당 광고주 목록
	 * @param requestMap
	 * @return
	 * @throws Exception 
	 */
	@PostMapping("/demand/self/personal/list")
	public Map<String, Object> getMemberPersonalDemandList(RequestMap requestMap) {
		
		Map<String, Object> param = requestMap.getMap();
		
		return agencyMemberService.getMemberPersonalDemandList(param);
	}

	/**
	 * 대행사 소속 광고주 계정 삭제
	 * @param requestMap
	 * @return
	 * @throws Exception 
	 */
	@PostMapping("/demand/remove")
	public Map<String, Object> removeDemand(RequestMap requestMap) throws Exception {
		Map<String, Object> param = requestMap.getMap();
		
		return agencyMemberService.removeDemand(param);
	}
	
	/**
	 * 대행사 소속 광고주 계정 삭제
	 * @param requestMap
	 * @return
	 * @throws Exception 
	 */
	@PostMapping("/demand/remove/validate")
	public Map<String, Object> removeDemandValidate(RequestMap requestMap) throws Exception {
		Map<String, Object> param = requestMap.getMap();
		
		return agencyMemberService.removeDemandValidate(param);
	}
	
	/**
	 * 광고주 계정 등록(생성)
	 * @param requestMap
	 * @return
	 * @throws Exception 
	 */
	@PostMapping("/demand/add")
	public Map<String, Object> addDemand(RequestMap requestMap, MultipartHttpServletRequest mRequest) throws Exception {
		Map<String, Object> param = requestMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "company_regnum");
		
		CommonUtil.checkNullThrowException(param, "uid");
		CommonUtil.checkNullThrowException(param, "mobile");
		CommonUtil.checkNullThrowException(param, "company_email");
		
		return agencyMemberService.addDemand(param, mRequest);
	}

	/**
	 * 활동 이력 조회
	 * @param requstMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/history/list")
	public Map<String, Object> getModifyHistoryList(RequestMap requstMap) throws Exception {
		Map<String, Object> param = requstMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "member_id");
		
		return agencyMemberService.getModifyHistoryList(param);
	}
	
	/**
	 * 로그인 계정 대행사 소속 광고주 계정으로 로그인 
	 * @param requestMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/demand/login")
	public Map<String, Object> demandLoginByAgency(RequestMap requestMap, HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String, Object> param = requestMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "demand_member_id");
		
		return agencyMemberService.demandLoginByAgency(param, request, response);
	}
	
	// 광고 관리
	/**
	 * 대행사 소속 광고주 캠페인 리스트
	 * @param requestMap
	 * @return
	 */
	@PostMapping("/sg/manage/campaign/list")
	public Map<String, Object> getDemandCampaignList(RequestMap requestMap) {
		Map<String, Object> param = requestMap.getMap();
		
		return agencyMemberService.getDemandCampaignList(param);
	}
	
	/**
	 * 대행사 소속 광고주 집행요청금액 리스트
	 * @param requestMap
	 * @return
	 */
	@PostMapping("/sg/manage/cost/list")
	public Map<String, Object> getDemandCostList(RequestMap requestMap) {
		Map<String, Object> param = requestMap.getMap();
		
		return agencyMemberService.getDemandCostList(param);
	}
	
	
	/**
	 * 대행사 소속 광고주 목록
	 * @param requestMap
	 * @return
	 */
	@PostMapping("/sg/manage/demand/list")
	public Map<String, Object> getAgencyDemandList(RequestMap requestMap) {
		Map<String, Object> param = requestMap.getMap();
		
		return agencyMemberService.getAgencyDemandList(param);
	}
	
	/**
	 * 대행사 소속 직원 목록
	 * @param requestMap
	 * @return
	 */
	@PostMapping("/sg/manage/staff/list")
	public Map<String, Object> getAgencyStaffList(RequestMap requestMap) {
		
		Map<String, Object> param = requestMap.getMap();
		
		return agencyMemberService.getAgencyStaffList(param);
	}
	
	/**
	 * 대행사 소속 광고주의 광고 목록 조회
	 * @param requestMap
	 * @return
	 */
	@PostMapping("/sg/list")
	public Map<String, Object> getDemandSgList(RequestMap requestMap) {
		Map<String, Object> param = requestMap.getMap();
		
		return agencyMemberService.getDemandSgList(param);
	}
}
