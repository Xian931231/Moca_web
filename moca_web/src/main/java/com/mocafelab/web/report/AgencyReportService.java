package com.mocafelab.web.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import java.util.Date;
import java.util.Calendar;

import org.dhatim.fastexcel.Color;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mocafelab.web.common.CommonMapper;
import com.mocafelab.web.vo.BeanFactory;
import com.mocafelab.web.vo.ResponseMap;

import net.newfrom.lib.util.CommonUtil;

@Service
public class AgencyReportService {

	@Autowired
	private BeanFactory beanFactory;
	
	@Autowired
	private AgencyReportMapper agencyReportMapper;
	
	@Autowired
	private CommonMapper commonMapper;
	
	
	/**
	 * 로그인한 유저의 담당 광고주 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getLoginUserAccessDemand(Map<String, Object> param) throws Exception{
		ResponseMap respMap = beanFactory.getResponseMap();

		List<Map<String, Object>> accessDemandList = agencyReportMapper.getLoginCountSgDemandList(param);
		
		respMap.setBody("list", accessDemandList);
		return respMap.getResponse();
		
	}
	
	/**
	 * 광고주별 리포트
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> reportListByDemand(Map<String, Object> param) throws Exception{
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> list = new ArrayList<>(); 
		List<String> demandMemberList = (List<String>) param.get("member_list");
		for(String memberId : demandMemberList) {
			param.put("member_id", memberId);
			Map<String, Object> memberInfo = agencyReportMapper.getMemberInfo(param);
			
			if(!CommonUtil.checkIsNull(memberInfo)) {
				List<Map<String, Object>> sgList = agencyReportMapper.getListByDemand(param);
				memberInfo.put("sg_list", sgList);
				list.add(memberInfo);
			}
		}
		respMap.setBody("list", list);
		
		return respMap.getResponse();
	}
	
	
	/**
	 * 광고주별 리포트 엑셀 다운로드
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void reportExcelByDemand(Map<String, Object> param, HttpServletResponse response) throws Exception{
		Workbook wb = new Workbook(response.getOutputStream(), "ExcelWriter", "1.0");
		Worksheet ws = wb.newWorksheet("sheet1");
		
		int row = 0;
		int startCol = 0, lastCol = 2;
		
		ws.value(row, 0, setExcelSearchDate(param));
		ws.range(row, startCol, row, lastCol).merge();
		
		row++;
		
		// title
		ws.value(row, 0, "광고주");
		ws.value(row, 1, "광고명");
		ws.value(row, 2, "노출량");
		ws.range(row, startCol, row, lastCol).style().fillColor(Color.GRAY3).set();
		
		// col width 
		ws.width(0, 50);
		ws.width(1, 50);
		ws.width(2, 10);
		
		//광고주 목록
		int totalCnt = 0;
		List<String> demandMemberList = (List<String>) param.get("member_list");
		
		for(String memberId : demandMemberList) {
			int startRow = row + 1;
			param.put("member_id", memberId);
			
			//광고주 정보 조회
			Map<String, Object> memberInfo = agencyReportMapper.getMemberInfo(param);
			if(!CommonUtil.checkIsNull(memberInfo)) {
				String demandName = (String) memberInfo.get("company_name");
				//광고 및 노출수 조회
				List<Map<String, Object>> sgList = agencyReportMapper.getListByDemand(param);
				
				if(sgList.size() > 0 ) {
					for(Map<String, Object> sgInfo : sgList) {
						row++;
						String sgName = (String) sgInfo.get("sg_name");
						int cnt = Integer.valueOf(String.valueOf(sgInfo.get("cnt")));
						
						ws.value(row, 0, demandName);
						ws.value(row, 1, sgName);
						ws.value(row, 2, cnt);
						
						totalCnt += cnt;
					}
					
				}else {
					row++;
					ws.value(row, 0, demandName);
					ws.value(row, 1, "-");
					ws.value(row, 2, "-");
				}
				ws.range(startRow, 0, row, 0).merge();
			}
			
		}
		
		row++;
		
		ws.value(row, startCol, "합계");
		ws.range(row, startCol, row, lastCol - 1).merge();
		ws.value(row, lastCol, totalCnt);
		ws.range(row, startCol, row, lastCol).style().fillColor(Color.GRAY3).set();
		ws.range(1, startCol, row, lastCol).style().verticalAlignment("center").horizontalAlignment("center").set();
		
		excelDownload(response, wb, ws, "광고주별_리포트", setReplaceSearchDate(param));
		
	}
	
	/**
	 * 시간별 리포트
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> reportListByTime(Map<String, Object> param) throws Exception{
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> list = new ArrayList<>();
		param.put("hour_list", getHourList());
		
		List<String> demandMemberList = (List<String>) param.get("member_list");
		
		for(String demandMemberId: demandMemberList) {
			
			//전체 광고주 선택
			if(demandMemberId.equals("0")) {
				Map<String, Object> periodList = agencyReportMapper.getListByTime(param);
				Map<String, Object> allInfo = new HashMap<>();
				allInfo.put("company_name", "전체 광고");
				allInfo.put("hour_list", periodList);
				list.add(allInfo);
			}else {
				param.put("member_id", demandMemberId);
				Map<String, Object> memberInfo = agencyReportMapper.getMemberInfo(param);
				if(memberInfo != null) {
					Map<String, Object> periodList = agencyReportMapper.getListByTime(param);
					memberInfo.put("hour_list", periodList);
					list.add(memberInfo);
				}
			}
		}
		
		respMap.setBody("list", list);
		
		return respMap.getResponse();
	}
	
	/**
	 * 기간별(시간) 리포트 엑셀 다운로드
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public void reportExcelByTime(Map<String, Object> param, HttpServletResponse response) throws Exception {
		Workbook wb = new Workbook(response.getOutputStream(), "ExcelWriter", "1.0");
		Worksheet ws = wb.newWorksheet("sheet1");
		
		String searchDate = (String) param.get("search_dt");
		
		
		int row = 0;
		int startCol = 0, lastCol = 2;
		
		String date = "조회 날짜 : "+ param.get("search_dt");
		
		ws.value(row, 0, date);
		ws.range(row, startCol, row, lastCol).merge();
		
		row++;
		
		// title
		ws.value(row, 0, "광고주명");
		ws.value(row, 1, "시간");
		ws.value(row, 2, "노출량");
		ws.range(row, startCol, row, lastCol).style().fillColor(Color.GRAY3).set();
		
		// col width 
		ws.width(0, 25);
		ws.width(1, 50);
		ws.width(2, 10);
		
		//24시간 리스트
		param.put("hour_list", getHourList());
		
		int totalCnt = 0;
		
		List<String> demandMemberList = (List<String>) param.get("member_list");
		
		for(String demandMemberId: demandMemberList) {
			int startRow = row + 1;
			
			String demandName = "";
			
			//전체 광고주 선택
			if(demandMemberId.equals("0")) {
				demandName = "전체 광고";
				
				Map<String, Object> periodList = agencyReportMapper.getListByTime(param);
				
				for(int i =0; i < 24; i ++) {
					row++;
					
					String startHourStr = "";
					String endHourStr = "";
					
					if(i < 10) {
						startHourStr = "0"+ i;
					}else {
						startHourStr =  Integer.toString(i);
					}
					
					if((i+1) < 10) {
						endHourStr = "0" + (i+1);
					}else if((i+1) == 24){
						endHourStr = "00";
					}else {
						endHourStr =  Integer.toString(i+1);
					}
					
					long cnt = (long) periodList.get("hour_"+startHourStr);
					
					ws.value(row, 0, demandName);
					ws.value(row, 1, startHourStr+ ":00 ~ " + endHourStr + ":00");
					ws.value(row, 2, cnt);
					totalCnt += cnt;
				}
				ws.range(startRow, 0, row, 0).merge();
				
			}else {
				param.put("member_id", demandMemberId);
				Map<String, Object> memberInfo = agencyReportMapper.getMemberInfo(param);
				
				demandName = String.valueOf(memberInfo.get("company_name"));
				
				if(memberInfo != null) {
					Map<String, Object> periodList = agencyReportMapper.getListByTime(param);
					
					for(int i =0; i < 24; i ++) {
						row++;
						
						String startHourStr = "";
						String endHourStr = "";
						
						if(i < 10) {
							startHourStr = "0"+ i;
						}else {
							startHourStr =  Integer.toString(i);
						}
						
						if((i+1) < 10) {
							endHourStr = "0" + (i+1);
						}else if((i+1) == 24){
							endHourStr = "00";
						}else {
							endHourStr =  Integer.toString(i+1);
						}
						
						long cnt = (long) periodList.get("hour_"+startHourStr);
						
						ws.value(row, 0, demandName);
						ws.value(row, 1, startHourStr+ ":00 ~ " + endHourStr + ":00");
						ws.value(row, 2, cnt);
						totalCnt += cnt;
					}
					ws.range(startRow, 0, row, 0).merge();
				}
			}
		}
		
		row++;
		
		ws.value(row, startCol, "합계");
		ws.range(row, startCol, row, lastCol - 1).merge();
		ws.value(row, lastCol, totalCnt);
		ws.range(row, startCol, row, lastCol).style().fillColor(Color.GRAY3).set();
		ws.range(1, startCol, row, lastCol).style().verticalAlignment("center").horizontalAlignment("center").set();
		
		
		String match = "[^0-9]";
		String repSearchDate = searchDate.replaceAll(match, "");
		
		excelDownload(response, wb, ws, "시간별리포트", repSearchDate);
		
	}
	
	/**
	 * 일별 리포트
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> reportListByDate(Map<String, Object> param) throws Exception{
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> list = new ArrayList<>();
		
		//시작 ~ 끝 날짜간의 모든 날짜 리스트 
		param.put("date_list", getDateList(param, "date"));
		
		
		List<String> demandMemberList = (List<String>) param.get("member_list");
		
		for(String demandMemberId: demandMemberList) {
			
			String demandName = "";
			//전체 광고주 선택
			if(demandMemberId.equals("0")) {
				Map<String, Object> periodList = agencyReportMapper.getListByDate(param);
				Map<String, Object> allInfo = new HashMap<>();
				allInfo.put("company_name", "전체 광고");
				allInfo.put("period_list", periodList);
				list.add(allInfo);
			}else {
				param.put("member_id", demandMemberId);
				Map<String, Object> memberInfo = agencyReportMapper.getMemberInfo(param);
				if(memberInfo != null) {
					Map<String, Object> periodList = agencyReportMapper.getListByDate(param);
					memberInfo.put("period_list", periodList);
					list.add(memberInfo);
				}
			}
		}
		
		respMap.setBody("list", list);
		
		return respMap.getResponse();
	}
	
	/**
	 * 기간별(일) 리포트 엑셀 다운로드
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public void reportExcelByDate(Map<String, Object> param, HttpServletResponse response) throws Exception {
		Workbook wb = new Workbook(response.getOutputStream(), "ExcelWriter", "1.0");
		Worksheet ws = wb.newWorksheet("sheet1");
		
		String searchDate = param.get("str_dt") +"~"+param.get("end_dt");
		
		
		int row = 0;
		int startCol = 0, lastCol = 2;
		
		ws.value(row, 0, "조회 기간 : " + searchDate);
		ws.range(row, startCol, row, lastCol).merge();
		
		row++;
		
		// title
		ws.value(row, 0, "광고명");
		ws.value(row, 1, "시간");
		ws.value(row, 2, "노출량");
		ws.range(row, startCol, row, lastCol).style().fillColor(Color.GRAY3).set();
		
		// col width 
		ws.width(0, 25);
		ws.width(1, 50);
		ws.width(2, 10);
		
		//시작 ~ 끝 날짜간의 모든 날짜 리스트 
		param.put("date_list", getDateList(param, "date"));
		
		int totalCnt = 0;
		
		List<String> demandMemberList = (List<String>) param.get("member_list");
		
		for(String demandMemberId: demandMemberList) {
			int startRow = row + 1;
			
			String demandName = "";
			
			//전체 광고주 선택
			if(demandMemberId.equals("0")) {
				demandName = "전체 광고";
				
				Map<String, Object> periodList = agencyReportMapper.getListByDate(param);
				
				for (Entry<String, Object> entry : periodList.entrySet()) {
					row++;
					
					String key = entry.getKey();
					
					int cnt = Integer.parseInt(String.valueOf(entry.getValue()));
					
					ws.value(row, 0, demandName);
					ws.value(row, 1, key);
					ws.value(row, 2, cnt);
					
					totalCnt += cnt;
					
				}
				
				ws.range(startRow, 0, row, 0).merge();
				
				
			}else {
				param.put("member_id", demandMemberId);
				Map<String, Object> memberInfo = agencyReportMapper.getMemberInfo(param);
				
				demandName = String.valueOf(memberInfo.get("company_name"));
				
				if(memberInfo != null) {
					Map<String, Object> periodList = agencyReportMapper.getListByDate(param);
					
					for (Entry<String, Object> entry : periodList.entrySet()) {
						row++;
						
						String key = entry.getKey();
						int cnt = Integer.parseInt(String.valueOf(entry.getValue()));
						
						ws.value(row, 0, demandName);
						ws.value(row, 1, key);
						ws.value(row, 2, cnt);
						
						totalCnt += cnt;
						
					}
					ws.range(startRow, 0, row, 0).merge();
				}
			}
		}
		row++;
		
		ws.value(row, startCol, "합계");
		ws.range(row, startCol, row, lastCol - 1).merge();
		ws.value(row, lastCol, totalCnt);
		ws.range(row, startCol, row, lastCol).style().fillColor(Color.GRAY3).set();
		ws.range(1, startCol, row, lastCol).style().verticalAlignment("center").horizontalAlignment("center").set();
		
		excelDownload(response, wb, ws, "일별리포트", setReplaceSearchDate(param) );
		
	}
	
	/**
	 * 월별 리포트
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> reportListByMonth(Map<String, Object> param) throws Exception{
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> list = new ArrayList<>();
		
		param.put("date_list", getDateList(param,"month"));
		
		
		List<String> demandMemberList = (List<String>) param.get("member_list");
		
		for(String demandMemberId: demandMemberList) {
			//전체 광고주 선택
			if(demandMemberId.equals("0")) {
				Map<String, Object> periodList = agencyReportMapper.getListByMonth(param);
				Map<String, Object> allInfo = new HashMap<>();
				allInfo.put("company_name", "전체 광고");
				allInfo.put("period_list", periodList);
				list.add(allInfo);
			}else {
				param.put("member_id", demandMemberId);
				Map<String, Object> memberInfo = agencyReportMapper.getMemberInfo(param);
				if(memberInfo != null) {
					Map<String, Object> periodList = agencyReportMapper.getListByMonth(param);
					memberInfo.put("period_list", periodList);
					list.add(memberInfo);
				}
			}
		}
		
		respMap.setBody("list", list);
		
		return respMap.getResponse();
	}
	
	/**
	 * 기간별(월) 리포트 엑셀 다운로드
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public void reportExcelByMonth(Map<String, Object> param, HttpServletResponse response) throws Exception {
		Workbook wb = new Workbook(response.getOutputStream(), "ExcelWriter", "1.0");
		Worksheet ws = wb.newWorksheet("sheet1");
		
		String searchDate = param.get("str_year") + "년" + param.get("str_month") + "월~" + param.get("end_year") + "년" + param.get("end_month") + "월";
		
		
		int row = 0;
		int startCol = 0, lastCol = 2;
		
		ws.value(row, 0, "조회 기간 : " + searchDate);
		ws.range(row, startCol, row, lastCol).merge();
		
		row++;
		
		// title
		ws.value(row, 0, "광고명");
		ws.value(row, 1, "시간");
		ws.value(row, 2, "노출량");
		ws.range(row, startCol, row, lastCol).style().fillColor(Color.GRAY3).set();
		
		// col width 
		ws.width(0, 25);
		ws.width(1, 50);
		ws.width(2, 10);
		
		//시작 ~ 끝 날짜간의 모든 날짜 리스트 
		param.put("date_list", getDateList(param, "month"));
		
		int totalCnt = 0;
		
		List<String> demandMemberList = (List<String>) param.get("member_list");
		
		for(String demandMemberId: demandMemberList) {
			int startRow = row + 1;
			
			String demandName = "";
			
			//전체 광고주 선택
			if(demandMemberId.equals("0")) {
				demandName = "전체 광고";
				
				Map<String, Object> periodList = agencyReportMapper.getListByMonth(param);
				
				for (Entry<String, Object> entry : periodList.entrySet()) {
					row++;
					
					String key = entry.getKey();
					
					int cnt = Integer.parseInt(String.valueOf(entry.getValue()));
					
					ws.value(row, 0, demandName);
					ws.value(row, 1, key);
					ws.value(row, 2, cnt);
					
					totalCnt += cnt;
					
				}
				
				ws.range(startRow, 0, row, 0).merge();
				
				
			}else {
				param.put("member_id", demandMemberId);
				Map<String, Object> memberInfo = agencyReportMapper.getMemberInfo(param);
				
				demandName = String.valueOf(memberInfo.get("company_name"));
				
				if(memberInfo != null) {
					Map<String, Object> periodList = agencyReportMapper.getListByMonth(param);
					
					for (Entry<String, Object> entry : periodList.entrySet()) {
						row++;
						
						String key = entry.getKey();
						int cnt = Integer.parseInt(String.valueOf(entry.getValue()));
						
						ws.value(row, 0, demandName);
						ws.value(row, 1, key);
						ws.value(row, 2, cnt);
						
						totalCnt += cnt;
						
					}
					ws.range(startRow, 0, row, 0).merge();
				}
			}
		}
		row++;
		
		ws.value(row, startCol, "합계");
		ws.range(row, startCol, row, lastCol - 1).merge();
		ws.value(row, lastCol, totalCnt);
		ws.range(row, startCol, row, lastCol).style().fillColor(Color.GRAY3).set();
		ws.range(1, startCol, row, lastCol).style().verticalAlignment("center").horizontalAlignment("center").set();
		
		String repSearchDate = ""+param.get("str_year") + param.get("str_month") + "~" + param.get("end_year") + param.get("end_month");
		
		excelDownload(response, wb, ws, "월별리포트", repSearchDate );
		
	}
	
	/**
	 * 대행사 담당 광고주들이 등록한 광고를 노출한 상품을 등록한 매체사 조회 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getDemandOfAgencySupplyMemberList(Map<String, Object> param) throws Exception{
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> memberList = agencyReportMapper.getDemandOfAgencySupplyMemberList(param);
		respMap.setBody("list", memberList);
		
		return respMap.getResponse();
	}
	
	/**
	 * 매체별 리포트
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> reportListBySupply(Map<String, Object> param) throws Exception{
		ResponseMap respMap = beanFactory.getResponseMap();
		
		//대행사 담당 광고주들이 등록한 광고를 노출한 상품을 등록한 매체사 조회
		List<Map<String, Object>> memberList = agencyReportMapper.getDemandOfAgencySupplyMemberList(param);
		
		for(Map<String, Object> memberInfo : memberList) {
			
			int rowspan1 = 0;
			//구분 조회
			param.put("member_id", memberInfo.get("member_id"));
			List<Map<String, Object>> categoryList = agencyReportMapper.getDemandOfAgencyCategoryList(param);
			
			for(Map<String, Object> categoryInfo : categoryList) {
				int rowspan2 = 0;
				//상품 조회
				param.put("category_id", categoryInfo.get("category_id"));
				List<Map<String, Object>> productList = agencyReportMapper.getDemandOfAgencyProductList(param);
				
				for(Map<String, Object> productInfo : productList) {
					//상품 노출수 조회
					param.put("product_id", productInfo.get("product_id"));
					productInfo.put("exposure_cnt", agencyReportMapper.getProductExposureCnt(param));
					rowspan1++;
					rowspan2++;
				}
				if(rowspan2 == 0) {
					rowspan1++;
					rowspan2 = 1;
				}
				categoryInfo.put("product_list", productList);
				categoryInfo.put("rowspan", rowspan2);
			}
			if(rowspan1 == 0) {
				rowspan1 = 1;
			}
			memberInfo.put("rowspan", rowspan1);
			memberInfo.put("category_list", categoryList);
		}
		
		respMap.setBody("list", memberList);
		
		return respMap.getResponse();
	}
	
	/**
	 * 매체별 리포트 엑셀 다운로드
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public void reportExcelBySupply(Map<String, Object> param, HttpServletResponse response) throws Exception {
		Workbook wb = new Workbook(response.getOutputStream(), "ExcelWriter", "1.0");
		Worksheet ws = wb.newWorksheet("sheet1");
		
		int row = 0;
		int startCol = 0, lastCol = 3;
		
		ws.value(row, 0, setExcelSearchDate(param));
		ws.range(row, startCol, row, lastCol).merge();
		
		row++;
		
		// title
		ws.value(row, 0, "매체");
		ws.value(row, 1, "구분");
		ws.value(row, 2, "상품명");
		ws.value(row, 3, "노출량");
		ws.range(row, startCol, row, lastCol).style().fillColor(Color.GRAY3).set();
		
		// col width 
		ws.width(0, 25);
		ws.width(1, 50);
		ws.width(2, 50);
		ws.width(3, 10);
		
		List<Map<String, Object>> memberList = agencyReportMapper.getDemandOfAgencySupplyMemberList(param);
		
		int totalCnt = 0;
		for(Map<String, Object> memberInfo : memberList) {
			int startRow1 = row + 1;
			
			// 구분 조회
			String supplyName = (String) memberInfo.get("company_name");
			
			param.put("member_id", memberInfo.get("member_id"));
			List<Map<String, Object>> categoryList = agencyReportMapper.getDemandOfAgencyCategoryList(param);
			
			if(categoryList.size() > 0) {
				for(Map<String, Object> categoryInfo : categoryList) {
					int startRow2 = row + 1;
					
					// 상품 조회
					String categoryName = (String) categoryInfo.get("category_name");
					
					param.put("category_id", categoryInfo.get("category_id"));
					List<Map<String, Object>> productList = agencyReportMapper.getDemandOfAgencyProductList(param);
					
					if(productList.size() > 0) {
						for(Map<String, Object> productInfo : productList) {
							row++;
							
							//상품 노출수 조회
							String productName = (String) productInfo.get("product_name");
							param.put("product_id", productInfo.get("product_id"));
							int cnt = agencyReportMapper.getProductExposureCnt(param);
							
							ws.value(row, 0, supplyName);
							ws.value(row, 1, categoryName);
							ws.value(row, 2, productName);
							ws.value(row, 3, cnt);
							
							totalCnt += cnt;
						}
					}else {
						row++;
						ws.value(row, 0, supplyName);
						ws.value(row, 1, categoryName);
						ws.value(row, 2, "-");
						ws.value(row, 3, "-");
					}
					ws.range(startRow2, 1, row, 1).merge();
				}	
			} else {
				row++;
				ws.value(row, 0, supplyName);
				ws.value(row, 1, "-");
				ws.value(row, 2, "-");
				ws.value(row, 3, "-");
			}
			ws.range(startRow1, 0, row, 0).merge();
		}
		
		row++;
		
		ws.value(row, startCol, "합계");
		ws.range(row, startCol, row, lastCol - 1).merge();
		ws.value(row, lastCol, totalCnt);
		ws.range(row, startCol, row, lastCol).style().fillColor(Color.GRAY3).set();
		ws.range(1, startCol, row, lastCol).style().verticalAlignment("center").horizontalAlignment("center").set();
		
		excelDownload(response, wb, ws, "매체별_리포트",setReplaceSearchDate(param));
		
	}
	
	
	
	
	/**
	 * 지역별 리포트
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> reportListByArea(Map<String, Object> param) throws Exception{
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> demandList = agencyReportMapper.getLoginCountSgDemandList(param);
		int rowspan1 = 0;

		//전체 선택시
		if(demandList.size() <= 0) {
			List<Map<String, Object>> allInfoList = new ArrayList<>();
			Map<String, Object> allInfo = new HashMap<>();
			
			allInfo.put("company_name", "전체 광고주");
			
			List<Map<String, Object>> areaSiList = commonMapper.getAreaCodeBySi(param);
			List<Map<String, Object>> areaGuList = commonMapper.getAreaCodeByGu(param);
			
			List<Map<String, Object>> exposureList = agencyReportMapper.getAreaExposureList(param);
			
			areaGuList.forEach(areaGuInfo -> {
				String siCode = (String) areaGuInfo.get("si_code");
				String guCode = (String) areaGuInfo.get("gu_code");
				
				
				long totalCnt = 0;
				for(Map<String, Object> exposureInfo : exposureList) {
					if(siCode.equals(exposureInfo.get("si_code")) && guCode.equals(exposureInfo.get("gu_code"))){
						totalCnt += (long)exposureInfo.get("cnt");
					}
				}
				
				if(totalCnt > 0) {
					areaGuInfo.put("cnt", totalCnt);
				} else {
					areaGuInfo.put("cnt",  0);
				}
			});
			
			areaSiList.forEach(areaSiDetail -> {
				List<Map<String, Object>> guList = areaGuList.stream().filter(areaGuDetail -> {
					return areaSiDetail.get("si_code").equals(areaGuDetail.get("si_code"));
				}).collect(Collectors.toList());
				areaSiDetail.put("rowspan", guList.size());
				areaSiDetail.put("gu_list", guList);
			});
			allInfo.put("rowspan", areaGuList.size());
			allInfo.put("si_list", areaSiList);
			rowspan1 += areaGuList.size();
			if(rowspan1 == 0) {
				rowspan1 = 1;
			}
			allInfoList.add(allInfo);
			respMap.setBody("list", allInfoList);
		//선택시
		}else {
			for(Map<String, Object> demandInfo : demandList) {
				param.put("member_id", demandInfo.get("member_id"));
				
				List<Map<String, Object>> areaSiList = commonMapper.getAreaCodeBySi(param);
				List<Map<String, Object>> areaGuList = commonMapper.getAreaCodeByGu(param);
				
				List<Map<String, Object>> exposureList = agencyReportMapper.getAreaExposureList(param);
				
				areaGuList.forEach(areaGuInfo -> {
					String siCode = (String) areaGuInfo.get("si_code");
					String guCode = (String) areaGuInfo.get("gu_code");
					
					Optional<Map<String, Object>> optional = exposureList.stream().filter(info -> {
						return info.get("si_code").equals(siCode) && info.get("gu_code").equals(guCode); 
					}).findFirst();
					
					if(optional.isPresent()) {
						areaGuInfo.put("cnt", optional.get().get("cnt"));
					} else {
						areaGuInfo.put("cnt",  0);
					}
				});
				
				areaSiList.forEach(areaSiDetail -> {
					List<Map<String, Object>> guList = areaGuList.stream().filter(areaGuDetail -> {
						return areaSiDetail.get("si_code").equals(areaGuDetail.get("si_code"));
					}).collect(Collectors.toList());
					areaSiDetail.put("rowspan", guList.size());
					areaSiDetail.put("gu_list", guList);
				});
				demandInfo.put("rowspan", areaGuList.size());
				demandInfo.put("si_list", areaSiList);
				rowspan1 += areaGuList.size();
				if(rowspan1 == 0) {
					rowspan1 = 1;
				}
			}
			
			respMap.setBody("list", demandList);
		}
		
		return respMap.getResponse();
	}
	
	/**
	 * 지역별 리포트 지도용
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> reportListByAreaMap(Map<String, Object> param) throws Exception{
		ResponseMap respMap = beanFactory.getResponseMap();
		List<Map<String, Object>> list = agencyReportMapper.getReportListByAreaMap(param);
		respMap.setBody("list", list);
		
		return respMap.getResponse();
	}
	/**
	 * 지역별 리포트 엑셀 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public void reportExcelByArea(Map<String, Object> param, HttpServletResponse response) throws Exception {
		Workbook wb = new Workbook(response.getOutputStream(), "ExcelWriter", "1.0");
		Worksheet ws = wb.newWorksheet("sheet1");
		
		int row = 0;
		int startCol = 0, lastCol = 3;
		
		ws.value(row, 0, setExcelSearchDate(param));
		ws.range(row, startCol, row, lastCol).merge();
		
		row++;
		
		ws.value(row, 0, "광고주");
		ws.value(row, 1, "지역");
		ws.value(row, 2, "지역구분");
		ws.value(row, 3, "노출량");
		ws.range(row, startCol, row, lastCol).style().fillColor(Color.GRAY3).set();
		
		// col width 
		ws.width(0, 25);
		ws.width(1, 25);
		ws.width(2, 25);
		ws.width(3, 10);
		
		int totalCnt = 0;
		
		List<Map<String, Object>> demandList = agencyReportMapper.getLoginCountSgDemandList(param);
		
		//전체 선택시
		if(demandList.size() <= 0) {
			int startRow1 = row + 1;
			Map<String, Object> allInfo = new HashMap<>();
			
			String demandName = "전체 광고주";
			
			allInfo.put("company_name", demandName);
			
			List<Map<String, Object>> areaSiList = commonMapper.getAreaCodeBySi(param);
			List<Map<String, Object>> areaGuList = commonMapper.getAreaCodeByGu(param);
			
			List<Map<String, Object>> exposureList = agencyReportMapper.getAreaExposureList(param);
			
			for(Map<String, Object> areaSiDetail : areaSiList) {
				int startRow2 = row + 1;
				
				List<Map<String, Object>> guList = areaGuList.stream().filter(guDetail -> {
					return guDetail.get("si_code").equals(areaSiDetail.get("si_code"));
				}).collect(Collectors.toList());
				
				for(Map<String, Object> guDetail : guList) {
					row++;
					
					String siCode = (String) guDetail.get("si_code");
					String guCode = (String) guDetail.get("gu_code");
					String siName = (String) guDetail.get("si_name");
					String guName = (String) guDetail.get("gu_name");
					
					Optional<Map<String, Object>> optional = exposureList.stream().filter(exposureDetail -> {
						return exposureDetail.get("si_code").equals(siCode) && exposureDetail.get("gu_code").equals(guCode); 
					}).findFirst();
					
					int cnt = 0;
					
					if(optional.isPresent()) {
						cnt = Integer.valueOf(String.valueOf(optional.get().get("cnt")));
					} 
					
					ws.value(row, 0, demandName);
					ws.value(row, 1, siName);
					ws.value(row, 2, guName);
					ws.value(row, 3, cnt);
					
					totalCnt += cnt;
				}
				ws.range(startRow2, 1, row, 1).merge();
			}
			ws.range(startRow1, 0, row, 0).merge();
			row++;
		//선택시
		}else {
			for(Map<String, Object> demandInfo : demandList) {
				int startRow1 = row + 1;
				param.put("member_id", demandInfo.get("member_id"));
				
				String demandName = (String) demandInfo.get("company_name");
				
				List<Map<String, Object>> areaSiList = commonMapper.getAreaCodeBySi(param);
				List<Map<String, Object>> areaGuList = commonMapper.getAreaCodeByGu(param);
				
				List<Map<String, Object>> exposureList = agencyReportMapper.getAreaExposureList(param);
				
				for(Map<String, Object> areaSiDetail : areaSiList) {
					int startRow2 = row + 1;
					
					List<Map<String, Object>> guList = areaGuList.stream().filter(guDetail -> {
						return guDetail.get("si_code").equals(areaSiDetail.get("si_code"));
					}).collect(Collectors.toList());
					
					for(Map<String, Object> guDetail : guList) {
						row++;
						
						String siCode = (String) guDetail.get("si_code");
						String guCode = (String) guDetail.get("gu_code");
						String siName = (String) guDetail.get("si_name");
						String guName = (String) guDetail.get("gu_name");
						
						Optional<Map<String, Object>> optional = exposureList.stream().filter(exposureDetail -> {
							return exposureDetail.get("si_code").equals(siCode) && exposureDetail.get("gu_code").equals(guCode); 
						}).findFirst();
						
						int cnt = 0;
						
						if(optional.isPresent()) {
							cnt = Integer.valueOf(String.valueOf(optional.get().get("cnt")));
						} 
						
						ws.value(row, 0, demandName);
						ws.value(row, 1, siName);
						ws.value(row, 2, guName);
						ws.value(row, 3, cnt);
						
						totalCnt += cnt;
					}
					ws.range(startRow2, 1, row, 1).merge();
				}
				ws.range(startRow1, 0, row, 0).merge();
			}
			row++;
		}
		
		ws.value(row, startCol, "합계");
		ws.range(row, startCol, row, lastCol - 1).merge();
		ws.value(row, lastCol, totalCnt);
		ws.range(row, startCol, row, lastCol).style().fillColor(Color.GRAY3).set();
		ws.range(1, startCol, row, lastCol).style().verticalAlignment("center").horizontalAlignment("center").set();
		
		excelDownload(response, wb, ws, "지역별_리포트",setReplaceSearchDate(param));
		
	}
	
	
	/**
	 * 엑셀 다운로드
	 * @param response
	 * @param wb
	 * @param ws
	 * @param fileName
	 * @throws Exception
	 */
	private void excelDownload(HttpServletResponse response, Workbook wb, Worksheet ws, String fileName) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMdd");
		String date = sdf.format(new Date());
		fileName = date + "_" + fileName;
		
		response.setContentType("application/vnd.ms-excel");
		response.setCharacterEncoding("utf-8");
		response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8") + ".xlsx");
		
		ws.flush();
		ws.finish();
		wb.finish();
	}
	
	/**
	 * hour_** 시간 생성
	 * @param param
	 * @return
	 * @throws Exception
	 */
	private List<String> getHourList() throws Exception {
		// hour_list 생성
		List<String> hourList = new ArrayList<>();
		
		for(int i=00; i<24; i++) {
			if(i == 24) {
				continue;
			}
			String str = String.valueOf(i);
			if(str.length() < 2) {
				str = "0" + str;
			}
			hourList.add(str);
		}
		
		return hourList;
	}
	
	/**
	 * 시작 날짜 부터 종료 날짜 까지의 날짜 생성
	 * @return
	 * @throws Exception
	 */
	private List<String> getDateList(Map<String, Object> param, String type) throws Exception{
		//type = month : 월, date : 일 
		String DATE_PATTERN = "yyyy-MM-dd";
		// hour_list 생성
		List<String> dateList = new ArrayList<>();
		
		String strDt = String.valueOf(param.get("str_dt"));
		String endDt = String.valueOf(param.get("end_dt"));
		
		if(type.equals("month")) {
			DATE_PATTERN = "yyyy-MM";
			
			strDt = String.valueOf(param.get("str_year")) + "-" + String.valueOf(param.get("str_month"));
			endDt = String.valueOf(param.get("end_year")) + "-" + String.valueOf(param.get("end_month"));
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
		Date startDate = sdf.parse(strDt);
		Date endDate = sdf.parse(endDt);
		ArrayList<String> dates = new ArrayList<String>();
		Date currentDate = startDate;
		while (currentDate.compareTo(endDate) <= 0) {
			dates.add(sdf.format(currentDate));
			Calendar c = Calendar.getInstance();
			c.setTime(currentDate);

			if(type.equals("month")) {
				c.add(Calendar.MONTH, 1);
			}else {
				c.add(Calendar.DAY_OF_MONTH, 1);
			}
			currentDate = c.getTime();
		}
		for (String date : dates) {
			dateList.add(date);
		}
		
		return dateList;
	}
	
	/**
	 * 엑셀 조회 기간 텍스트 설정
	 * @param param
	 * @param excelWriter
	 * @throws Exception
	 */
	private String setExcelSearchDate(Map<String, Object> param) throws Exception {
		String startDate = (String) param.get("str_dt");
		String endDate = (String) param.get("end_dt");
		
		String searchDate = "조회기간: " + startDate + " ~ " + endDate ;
		
		return searchDate;
	}
	
	/**
	 * 기간 특수문자 제거 텍스트 설정
	 * @param param
	 * @param excelWriter
	 * @throws Exception
	 */
	private String setReplaceSearchDate(Map<String, Object> param) throws Exception {
		String match = "[^0-9]";
		String startDate = (String) param.get("str_dt");
		String endDate = (String) param.get("end_dt");
		String repStr = startDate.replaceAll(match, "");
		String repEnd = endDate.replaceAll(match, "");
		String searchDate = repStr + "~" + repEnd ;
		
		return searchDate;
	}
	
	
	/**
	 * 엑셀 다운로드
	 * @param response
	 * @param wb
	 * @param ws
	 * @param fileName
	 * @throws Exception
	 */
	private void excelDownload(HttpServletResponse response, Workbook wb, Worksheet ws, String fileName, String campaignName, String searchDate) throws Exception {
		fileName = searchDate + "_" +campaignName + "_" + fileName;
		
		response.setContentType("application/vnd.ms-excel");
		response.setCharacterEncoding("utf-8");
		response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8") + ".xlsx");
		
		ws.flush();
		ws.finish();
		wb.finish();
	}
	
	/**
	 * 엑셀 다운로드
	 * @param response
	 * @param wb
	 * @param ws
	 * @param fileName
	 * @throws Exception
	 */
	private void excelDownload(HttpServletResponse response, Workbook wb, Worksheet ws, String fileName, String searchDate) throws Exception {
		fileName = searchDate  + "_" + fileName;
		
		response.setContentType("application/vnd.ms-excel");
		response.setCharacterEncoding("utf-8");
		response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8") + ".xlsx");
		
		ws.flush();
		ws.finish();
		wb.finish();
	}
}
