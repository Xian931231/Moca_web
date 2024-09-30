package com.mocafelab.web.report;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.vo.RequestMap;

/**
 * 리포트
 * @author mure96
 *
 */
@RestController
@RequestMapping("${apiPrefix}/report")
public class ReportController {
	/*
	@PostMapping("/")
	public Map<String, Object> (RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		return .(param);
	}
	*/
	
	@Autowired
	private ReportService reportService;
	
	/**
	 * 광고주별 리포트 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/list/byDemand")
	public Map<String, Object> reportListByDemand(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "member_list");
		CommonUtil.checkNullThrowException(param, "start_date");
		CommonUtil.checkNullThrowException(param, "end_date");
		CommonUtil.checkNullThrowException(param, "start_hour");
		CommonUtil.checkNullThrowException(param, "end_hour");
		
		return reportService.reportListByDemand(param);
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
		
		CommonUtil.checkNullThrowException(param, "start_date");
		CommonUtil.checkNullThrowException(param, "end_date");
		CommonUtil.checkNullThrowException(param, "start_hour");
		CommonUtil.checkNullThrowException(param, "end_hour");
		
		return reportService.reportListBySupply(param);
	}
	
	/**
	 * 상품 목록 조회 
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/product/list")
	public Map<String, Object> reportProductList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		return reportService.reportProductList(param);
	}
	
	/**
	 * 상품별 리포트 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/list/byProduct")
	public Map<String, Object> reportListByProduct(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "product_list");
		CommonUtil.checkNullThrowException(param, "start_date");
		CommonUtil.checkNullThrowException(param, "end_date");
		CommonUtil.checkNullThrowException(param, "start_hour");
		CommonUtil.checkNullThrowException(param, "end_hour");
		
		return reportService.reportListByProduct(param);
	}
	
	/**
	 * 지역별 리포트 조회 - 지도
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/list/byAreaMap")
	public Map<String, Object> reportListByAreaMap(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "member_list");
		CommonUtil.checkNullThrowException(param, "start_date");
		CommonUtil.checkNullThrowException(param, "end_date");
		CommonUtil.checkNullThrowException(param, "start_hour");
		CommonUtil.checkNullThrowException(param, "end_hour");
		CommonUtil.checkNullThrowException(param, "map_level");
		
		return reportService.reportListByAreaMap(param);
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
		
		CommonUtil.checkNullThrowException(param, "member_list");
		CommonUtil.checkNullThrowException(param, "start_date");
		CommonUtil.checkNullThrowException(param, "end_date");
		CommonUtil.checkNullThrowException(param, "start_hour");
		CommonUtil.checkNullThrowException(param, "end_hour");
		CommonUtil.checkNullThrowException(param, "map_level");
		
		return reportService.reportListByArea(param);
	}
	
	/**
	 * 광고주별 리포트 엑셀 다운로드
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/excel/byDemand")
	public void reportExcelByDemand(RequestMap reqMap, HttpServletResponse response) throws Exception {
		Map<String, Object> param = reqMap.getMap();

		CommonUtil.checkNullThrowException(param, "member_list");
		CommonUtil.checkNullThrowException(param, "start_date");
		CommonUtil.checkNullThrowException(param, "end_date");
		CommonUtil.checkNullThrowException(param, "start_hour");
		CommonUtil.checkNullThrowException(param, "end_hour");
		
//		Integer [] memberList = {47};
//		param.put("member_list", memberList);
//		param.put("start_date", "2023-11-01");
//		param.put("end_date", "2023-11-30");
//		param.put("start_hour", "00");
//		param.put("end_hour", "01");
		
		reportService.reportExcelByDemand(param, response);
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
		
		CommonUtil.checkNullThrowException(param, "start_date");
		CommonUtil.checkNullThrowException(param, "end_date");
		CommonUtil.checkNullThrowException(param, "start_hour");
		CommonUtil.checkNullThrowException(param, "end_hour");
		
//		Integer [] memberList = {47};
//		param.put("member_list", memberList);
//		param.put("start_date", "2023-11-01");
//		param.put("end_date", "2023-11-30");
//		param.put("start_hour", "00");
//		param.put("end_hour", "01");
		
		reportService.reportExcelBySupply(param, response);
	}

	/**
	 * 상품별 리포트 엑셀 다운로드
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/excel/byProduct")
	public void reportExcelByProduct(RequestMap reqMap, HttpServletResponse response) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "product_list");
		CommonUtil.checkNullThrowException(param, "start_date");
		CommonUtil.checkNullThrowException(param, "end_date");
		CommonUtil.checkNullThrowException(param, "start_hour");
		CommonUtil.checkNullThrowException(param, "end_hour");
		
		reportService.reportExcelByProduct(param, response);
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
		
		CommonUtil.checkNullThrowException(param, "member_list");
		CommonUtil.checkNullThrowException(param, "start_date");
		CommonUtil.checkNullThrowException(param, "end_date");
		CommonUtil.checkNullThrowException(param, "start_hour");
		CommonUtil.checkNullThrowException(param, "end_hour");
		
		reportService.reportExcelByArea(param, response);
	}
	
}
