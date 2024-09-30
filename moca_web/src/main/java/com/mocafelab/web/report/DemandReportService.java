package com.mocafelab.web.report;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.dhatim.fastexcel.Color;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mocafelab.web.common.CommonMapper;
import com.mocafelab.web.vo.BeanFactory;
import com.mocafelab.web.vo.ResponseMap;

@Service
public class DemandReportService {
	
	@Autowired
	private BeanFactory beanFactory;
	
	@Autowired
	private DemandReportMapper demandReportMapper;
	
	@Autowired
	private CommonMapper commonMapper;
	
	/**
	 * 광고 조회
	 * @param parama
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getSgList(Map<String, Object> param) throws Exception{
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> list = demandReportMapper.getSgList(param);
		respMap.setBody("list", list);
		
		return respMap.getResponse();
	}
	/**
	 * 상품에 노출된 광고가 속해있는 캠페인 리스트 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getCampaignList(Map<String, Object> param) throws Exception{
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> list = demandReportMapper.getCampaignList(param);
		respMap.setBody("list", list);
		
		return respMap.getResponse();
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
		
		//24시간 리스트
		param.put("hour_list", getHourList());
		
		List<Map<String, Object>> sgInfoList = demandReportMapper.getSgList(param);
		
		//캠페인만 선택한 경우
		//해당캠페인에 광고 리스트 조회
	
		//반복문
		for(Map<String, Object> sgInfo : sgInfoList) {
			param.put("sg_id",sgInfo.get("sg_id")); 
			//광고Id 로 cnt 추출
			Map<String, Object> periodList = demandReportMapper.getListByTime(param);
			sgInfo.put("hour_list", periodList);
			list.add(sgInfo);
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
		
		String campaignName = demandReportMapper.getCampaignName(param);
		
		String searchDate = (String) param.get("search_dt");
		
		
		int row = 0;
		int startCol = 0, lastCol = 2;
		
		String date = "조회 날짜 : "+ param.get("search_dt");
		
		ws.value(row, 0, date);
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
		
		//24시간 리스트
		param.put("hour_list", getHourList());
		
		int totalCnt = 0;
		
		List<Map<String, Object>> sgInfoList = demandReportMapper.getSgList(param);
		
		//반복문
		for(Map<String, Object> sgInfo : sgInfoList) {
			int startRow = row + 1;
			
			String sgName = (String)sgInfo.get("sg_name");
			
			param.put("sg_id",sgInfo.get("sg_id")); 
			//광고Id 로 cnt 추출
			Map<String, Object> periodList = demandReportMapper.getListByTime(param);
			
			
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
				
				ws.value(row, 0, sgName);
				ws.value(row, 1, startHourStr+ ":00 ~ " + endHourStr + ":00");
				ws.value(row, 2, cnt);
				
				totalCnt += cnt;
				
			}
			ws.range(startRow, 0, row, 0).merge();
		}
		
		row++;
		
		ws.value(row, startCol, "합계");
		ws.range(row, startCol, row, lastCol - 1).merge();
		ws.value(row, lastCol, totalCnt);
		ws.range(row, startCol, row, lastCol).style().fillColor(Color.GRAY3).set();
		ws.range(1, startCol, row, lastCol).style().verticalAlignment("center").horizontalAlignment("center").set();
		
		String match = "[^0-9]";
		String repSearchDate = searchDate.replaceAll(match, "");
		
		excelDownload(response, wb, ws, "시간별리포트", campaignName, repSearchDate);
		
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
		
		List<Map<String, Object>> sgInfoList = demandReportMapper.getSgList(param);
		//반복문
		for(Map<String, Object> sgInfo : sgInfoList) {
			param.put("sg_id",sgInfo.get("sg_id")); 
			//광고Id 로 cnt 추출
			Map<String, Object> periodList = demandReportMapper.getListByDate(param);
			sgInfo.put("period_list", periodList);
			list.add(sgInfo);
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
		
		String campaignName = demandReportMapper.getCampaignName(param);
		
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
		
		List<Map<String, Object>> sgInfoList = demandReportMapper.getSgList(param);
		
		//반복문
		for(Map<String, Object> sgInfo : sgInfoList) {
			int startRow = row + 1;
			
			String sgName = (String)sgInfo.get("sg_name");
			
			param.put("sg_id",sgInfo.get("sg_id")); 
			//광고Id 로 cnt 추출
			Map<String, Object> periodList = demandReportMapper.getListByDate(param);
			
			for (Entry<String, Object> entry : periodList.entrySet()) {
				row++;
				
				String key = entry.getKey();
				long cnt = (long) entry.getValue();
				
				ws.value(row, 0, sgName);
				ws.value(row, 1, key);
				ws.value(row, 2, cnt);
				
				totalCnt += cnt;
			}
			ws.range(startRow, 0, row, 0).merge();
		}
		
		row++;
		
		ws.value(row, startCol, "합계");
		ws.range(row, startCol, row, lastCol - 1).merge();
		ws.value(row, lastCol, totalCnt);
		ws.range(row, startCol, row, lastCol).style().fillColor(Color.GRAY3).set();
		ws.range(1, startCol, row, lastCol).style().verticalAlignment("center").horizontalAlignment("center").set();
		
		excelDownload(response, wb, ws, "일별리포트", campaignName, setReplaceSearchDate(param));
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
		
		//시작 ~ 끝 날짜간의 모든 날짜 리스트 
		param.put("date_list", getDateList(param, "month"));
		
		List<Map<String, Object>> sgInfoList = demandReportMapper.getSgList(param);
		//반복문
		for(Map<String, Object> sgInfo : sgInfoList) {
			param.put("sg_id",sgInfo.get("sg_id")); 
			//광고Id 로 cnt 추출
			Map<String, Object> periodList = demandReportMapper.getListByMonth(param);
			sgInfo.put("period_list", periodList);
			list.add(sgInfo);
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
		
		String campaignName = demandReportMapper.getCampaignName(param);
		
		int row = 0;
		int startCol = 0, lastCol = 2;
		
		String searchDate = param.get("str_year") + "년" + param.get("str_month") + "월~" + param.get("end_year") + "년" + param.get("end_month") + "월"; 
		
		ws.value(row, 0, "조회 기간 : " +searchDate);
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
		
		List<Map<String, Object>> sgInfoList = demandReportMapper.getSgList(param);
		
		//반복문
		for(Map<String, Object> sgInfo : sgInfoList) {
			int startRow = row + 1;
			
			String sgName = (String)sgInfo.get("sg_name");
			
			param.put("sg_id",sgInfo.get("sg_id")); 
			//광고Id 로 cnt 추출
			Map<String, Object> periodList = demandReportMapper.getListByMonth(param);
			
			for (Entry<String, Object> entry : periodList.entrySet()) {
				row++;
				
				String key = entry.getKey();
				long cnt = (long) entry.getValue();
				
				ws.value(row, 0, sgName);
				ws.value(row, 1, key);
				ws.value(row, 2, cnt);
				
				totalCnt += cnt;
			}
			ws.range(startRow, 0, row, 0).merge();
		}
		
		row++;
		
		ws.value(row, startCol, "합계");
		ws.range(row, startCol, row, lastCol - 1).merge();
		ws.value(row, lastCol, totalCnt);
		ws.range(row, startCol, row, lastCol).style().fillColor(Color.GRAY3).set();
		ws.range(1, startCol, row, lastCol).style().verticalAlignment("center").horizontalAlignment("center").set();
		
		
		String repSearchDate = ""+param.get("str_year") + param.get("str_month") + "~" + param.get("end_year") + param.get("end_month");
		
		excelDownload(response, wb, ws, "월별리포트", campaignName, repSearchDate);
		
	}
	
	/**
	 * 매체별 리포트
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> reportListBySupply(Map<String, Object> param) throws Exception{
		ResponseMap respMap = beanFactory.getResponseMap();
		
		//로그인한 광고주가 등록한 광고를 노출한 상품을 등록한 매체사 조회
		List<Map<String, Object>> memberList = demandReportMapper.getSupplyMemberListOfDemand(param);
		
		for(Map<String, Object> memberInfo: memberList) {
			
			int rowspan1 = 0;
			param.put("member_id", memberInfo.get("member_id"));
			//구분 조회
			List<Map<String, Object>> categoryList = demandReportMapper.getCategoryListOfDemand(param);
			
			for(Map<String, Object> categoryInfo : categoryList) {
				int rowspan2 = 0;
				//상품 조회
				param.put("category_id", categoryInfo.get("category_id"));
				List<Map<String, Object>> productList = demandReportMapper.getProductListOfDemand(param);
				
				for(Map<String, Object> productInfo : productList) {
					//상품 노출수 조회
					param.put("product_id", productInfo.get("product_id"));
					productInfo.put("exposure_cnt", demandReportMapper.getProductExposureCnt(param));
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
		
		String searchDate = param.get("str_dt") +"~"+param.get("end_dt");
		
		ws.value(row, 0, "조회 기간 : " + searchDate);
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
		
		//로그인한 광고주가 등록한 광고를 노출한 상품을 등록한 매체사 조회
		List<Map<String, Object>> memberList = demandReportMapper.getSupplyMemberListOfDemand(param);
		
		int totalCnt = 0;
		for(Map<String, Object> memberInfo: memberList) {
			int startRow1 = row + 1;
			
			String supplyName = (String) memberInfo.get("company_name");
			
			param.put("member_id", memberInfo.get("member_id"));
			//구분 조회
			List<Map<String, Object>> categoryList = demandReportMapper.getCategoryListOfDemand(param);
			if(categoryList.size() > 0) {
				for(Map<String, Object> categoryInfo : categoryList) {
					int startRow2 = row + 1;
					
					// 상품 조회
					String categoryName = (String) categoryInfo.get("category_name");
					
					param.put("category_id", categoryInfo.get("category_id"));
					List<Map<String, Object>> productList = demandReportMapper.getProductListOfDemand(param);
					
					if(productList.size() > 0) {
						for(Map<String, Object> productInfo : productList) {
							row++;
							
							//상품 노출수 조회
							String productName = (String) productInfo.get("product_name");
							param.put("product_id", productInfo.get("product_id"));
							int cnt = demandReportMapper.getProductExposureCnt(param);
							
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
		
		excelDownload(response, wb, ws, "매체별_리포트", setReplaceSearchDate(param));
	}
	
	
	/**
	 * 로그인한 아이디의 광고를 상품에 노출한 매체사 리스트
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getSupplyMemberListOfLoginId(Map<String, Object> param) throws Exception{
		ResponseMap respMap = beanFactory.getResponseMap();
		
		//로그인한 광고주가 등록한 광고를 노출한 상품을 등록한 매체사 조회
		List<Map<String, Object>> memberList = demandReportMapper.getSupplyMemberListOfDemand(param);
		
		respMap.setBody("list", memberList);
		
		return respMap.getResponse();
		
	}
	
	/**
	 * 지역별 리포트
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> reportListByArea(Map<String, Object> param) throws Exception{
		ResponseMap respMap = beanFactory.getResponseMap();
		
		int rowspan1 = 0;
		
		//전체일때
		if(param.get("campaign_id") == null || param.get("campaign_id") == "") {
			List<Map<String, Object>> allInfoList = new ArrayList<>();
			Map<String, Object> allInfo = new HashMap<>();
			allInfo.put("sg_name", "전체 광고");
			
			List<Map<String, Object>> areaSiList = commonMapper.getAreaCodeBySi(param);
			List<Map<String, Object>> areaGuList = commonMapper.getAreaCodeByGu(param);
			
			List<Map<String, Object>> exposureList = demandReportMapper.getAreaExposureList(param);
			
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
		}else {
			List<Map<String, Object>> countSgList = demandReportMapper.getCountSgList(param);
			for(Map<String, Object> countSgInfo : countSgList) {
				param.put("sg_id", countSgInfo.get("sg_id"));
				
				List<Map<String, Object>> areaSiList = commonMapper.getAreaCodeBySi(param);
				List<Map<String, Object>> areaGuList = commonMapper.getAreaCodeByGu(param);
				
				List<Map<String, Object>> exposureList = demandReportMapper.getAreaExposureList(param);
				
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
				countSgInfo.put("rowspan", areaGuList.size());
				countSgInfo.put("si_list", areaSiList);
				rowspan1 += areaGuList.size();
				if(rowspan1 == 0) {
					rowspan1 = 1;
				}
			}
			
			respMap.setBody("list", countSgList);
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
		
		List<Map<String, Object>> list = demandReportMapper.getReportListByAreaMap(param);
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
		
		String searchDate = param.get("str_dt") + "~" + param.get("end_dt");
		
		ws.value(row, 0, setExcelSearchDate(param));
		ws.range(row, startCol, row, lastCol).merge();
		
		row++;
		
		ws.value(row, 0, "광고");
		ws.value(row, 1, "지역");
		ws.value(row, 2, "지역구분");
		ws.value(row, 3, "노출량");
		ws.range(row, startCol, row, lastCol).style().fillColor(Color.GRAY3).set();
		
		// col width 
		ws.width(0, 25);
		ws.width(1, 25);
		ws.width(2, 25);
		ws.width(3, 10);
		
		String campaignName = "";
		
		int totalCnt = 0;
		
		//전체일때
		if(param.get("campaign_id") == null || param.get("campaign_id") == "") {
			int startRow1 = row + 1;
			System.out.println(1);
			String sgName = "전체 광고";
			
			campaignName = "전체_광고";
			
			List<Map<String, Object>> areaSiList = commonMapper.getAreaCodeBySi(param);
			List<Map<String, Object>> areaGuList = commonMapper.getAreaCodeByGu(param);
			
			List<Map<String, Object>> exposureList = demandReportMapper.getAreaExposureList(param);
			
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
					
					long cnt = 0;
					for(Map<String, Object> exposureInfo : exposureList) {
						if(siCode.equals(exposureInfo.get("si_code")) && guCode.equals(exposureInfo.get("gu_code"))){
							cnt += (long)exposureInfo.get("cnt");
						}
						
						if(cnt < 0) {
							cnt = 0;
						}
					}
					
					ws.value(row, 0, sgName);
					ws.value(row, 1, siName);
					ws.value(row, 2, guName);
					ws.value(row, 3, cnt);
					
					totalCnt += cnt;
				}
				ws.range(startRow2, 1, row, 1).merge();
			}
			ws.range(startRow1, 0, row, 0).merge();
			row++;
		//광고 선택시
		}else {
			
			campaignName = demandReportMapper.getCampaignName(param);
			
			List<Map<String, Object>> countSgList = demandReportMapper.getCountSgList(param);
			for(Map<String, Object> countSgInfo : countSgList) {
				int startRow1 = row +1;
				param.put("sg_id", countSgInfo.get("sg_id"));
				
				String sgName = (String) countSgInfo.get("sg_name");
				
				List<Map<String, Object>> areaSiList = commonMapper.getAreaCodeBySi(param);
				List<Map<String, Object>> areaGuList = commonMapper.getAreaCodeByGu(param);
				
				List<Map<String, Object>> exposureList = demandReportMapper.getAreaExposureList(param);
				
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
						
						ws.value(row, 0, sgName);
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
		
		excelDownload(response, wb, ws, "지역별_리포트", campaignName, setReplaceSearchDate(param));
		
		
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
