package com.mocafelab.web.report;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.vo.RequestMap;

@RestController
@RequestMapping("${apiPrefix}/report/agency")
public class AgencyReportController {
	
	@Autowired
	private AgencyReportService agencyReportService;
	
	/**
	 * 로그인한 유저의 담당 광고주 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/access/demand/list")
	public Map<String, Object> getLoginUserAccessDemand(RequestMap reqMap) throws Exception{
		Map<String, Object> param = reqMap.getMap();
		
		return agencyReportService.getLoginUserAccessDemand(param);
	}
	
	
	/**
	 * 광고주별 리포트 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/list/byDemand")
	public Map<String, Object> reportListByDemand(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return agencyReportService.reportListByDemand(param);
	}
	
	/**
	 * 광고주별 검색 리스트 엑셀 다운  
	 * @param reqMap
	 * @throws Exception
	 */
	@PostMapping("/excel/byDemand")
	public void exceldownload(RequestMap reqMap, HttpServletResponse response) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		agencyReportService.reportExcelByDemand(param, response);
	}
	
	/**
	 * 대행사 담당 광고주들이 등록한 광고를 노출한 상품을 등록한 매체사 조회 
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/supply/member/list")
	public Map<String, Object> getDemandOfAgencySupplyMemeberList(RequestMap reqMap) throws Exception{
		Map<String, Object> param = reqMap.getMap();
		
		return agencyReportService.getDemandOfAgencySupplyMemberList(param);
	}
	
	/**
	 * 매체별 리포트 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/list/bySupply")
	public Map<String, Object> reportListBySupply(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return agencyReportService.reportListBySupply(param);
	}
	
	/**
	 * 매체별 리포트 엑셀 다운로드
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/excel/bySupply")
	public void reportExcelBySupply(RequestMap reqMap, HttpServletResponse response) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "str_dt");
		CommonUtil.checkNullThrowException(param, "end_dt");
		
		
		agencyReportService.reportExcelBySupply(param, response);
	}
	
	
	/**
	 * 시간별 리포트 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/list/byTime")
	public Map<String, Object> reportListByTime(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "search_dt");
		
		return agencyReportService.reportListByTime(param);
	}
	
	/**
	 * 시간별 리포트 엑셀 다운로드
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/excel/byTime")
	public void reportExcelByTime(RequestMap reqMap, HttpServletResponse response) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		agencyReportService.reportExcelByTime(param, response);
	}
	
	/**
	 * 일별 리포트 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/list/byDate")
	public Map<String, Object> reportListByDate(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "str_dt");
		CommonUtil.checkNullThrowException(param, "end_dt");
		
		return agencyReportService.reportListByDate(param);
	}
	
	/**
	 * 일별 리포트 엑셀 다운로드
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/excel/byDate")
	public void reportExcelByDate(RequestMap reqMap, HttpServletResponse response) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		agencyReportService.reportExcelByDate(param, response);
	}
	
	/**
	 * 월별 리포트 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/list/byMonth")
	public Map<String, Object> reportListByMonth(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return agencyReportService.reportListByMonth(param);
	}
	
	/**
	 * 월별 리포트 엑셀 다운로드
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/excel/byMonth")
	public void reportExcelByMonth(RequestMap reqMap, HttpServletResponse response) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		agencyReportService.reportExcelByMonth(param, response);
	}
	
	/**
	 * 지역별 리포트 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/list/byArea")
	public Map<String, Object> reportListByArea(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "str_dt");
		CommonUtil.checkNullThrowException(param, "end_dt");
		
		return agencyReportService.reportListByArea(param);
	}
	
	/**
	 * 지역별 리포트 조회 지도용
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/list/byAreaMap")
	public Map<String, Object> reportListByAreaMap(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "str_dt");
		CommonUtil.checkNullThrowException(param, "end_dt");
		
		return agencyReportService.reportListByAreaMap(param);
	}
	
	/**
	 * 지역별 리포트 엑셀 다운로드
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/excel/byArea")
	public void reportExcelByArea(RequestMap reqMap, HttpServletResponse response) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "str_dt");
		CommonUtil.checkNullThrowException(param, "end_dt");
		
		agencyReportService.reportExcelByArea(param, response);
	}
	
	

}
