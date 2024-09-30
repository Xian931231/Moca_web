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
@RequestMapping("${apiPrefix}/report/demand")
public class DemandReportController {
	
	@Autowired
	private DemandReportService demandReportService;
	
	
	/**
	 * 광고 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/sg/list")
	public Map<String, Object> getSgList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return demandReportService.getSgList(param);
	}
	
	/**
	 * 캠페인 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/campaign/list")
	public Map<String, Object> getCampaignList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return demandReportService.getCampaignList(param);
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
		
		
		return demandReportService.reportListByTime(param);
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
		
		demandReportService.reportExcelByTime(param, response);
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
		
		return demandReportService.reportListByDate(param);
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
		
		demandReportService.reportExcelByDate(param, response);
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
		
		return demandReportService.reportListByMonth(param);
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
		
		demandReportService.reportExcelByMonth(param, response);
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
		
		return demandReportService.reportListBySupply(param);
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
		
		
		demandReportService.reportExcelBySupply(param, response);
	}
	
	/**
	 * 로그인한 광고주의 광고가 노출된 매체사 리스트
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/list/supply/member")
	public Map<String, Object> getSupplyMemberListOfLoginId(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return demandReportService.getSupplyMemberListOfLoginId(param);
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
		
		return demandReportService.reportListByArea(param);
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
		
		return demandReportService.reportListByAreaMap(param);
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
		
		demandReportService.reportExcelByArea(param, response);
	}
	

}
