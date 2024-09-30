package com.mocafelab.web.report;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.dhatim.fastexcel.Color;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mocafelab.web.common.CommonMapper;
import com.mocafelab.web.device.DeviceMapper;
import com.mocafelab.web.vo.BeanFactory;
import com.mocafelab.web.vo.ResponseMap;

import net.newfrom.lib.util.CommonUtil;

@Service
public class ReportService {
	
	@Autowired
	private BeanFactory beanFactory;
	
	@Autowired
	private ReportMapper reportMapper;
	
	@Autowired
	private DeviceMapper deviceMapper;
	
	@Autowired
	private CommonMapper commonMapper;
	
	/**
	 * 광고주별 리포트
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> reportListByDemand(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 조회할 광고주 목록 입력 받음 
		List<Map<String, Object>> list = new ArrayList<>();
		param.put("hour_list", getHourList(param));
		
		// 광고주의 대행사 목록 조회 
		List<Map<String, Object>> agencyList = reportMapper.getAgencyListByDemand(param);
		for(Map<String, Object> agencyDetail : agencyList) {
			int rowspan1 = 0;
			param.put("agency_id", agencyDetail.get("member_id"));
			// 대행사의 담당 광고주 목록 조회
			List<Map<String, Object>> demandList = (List<Map<String, Object>>) reportMapper.getDemandListByAgency(param);
			for(Map<String, Object> demandDetail : demandList) {
				int rowspan2 = 0;
				// 광고주의 광고 목록 조회 
				param.put("member_id", demandDetail.get("member_id"));
				List<Map<String, Object>> sgList = reportMapper.getListByDemand(param);
				demandDetail.put("sg_list", sgList);
				
				rowspan2 += sgList.size();
				if(rowspan2 == 0) {
					rowspan2 = 1;
				}
				rowspan1 = rowspan1 + rowspan2;
				demandDetail.put("rowspan", rowspan2);
			}
			if(rowspan1 == 0) {
				rowspan1 = 1;
			}
			agencyDetail.put("rowspan", rowspan1);
			agencyDetail.put("demand_list", demandList);
		}
		
		list.addAll(agencyList);
		respMap.setBody("list", list);
		
		return respMap.getResponse();
	}
	
	/**
	 * 매체별 리포트
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> reportListBySupply(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> memberList = new ArrayList<>();
		List<Object> memberIdList = (List<Object>) param.get("member_list");
		if(memberIdList == null || memberIdList.size() == 0) {
			memberList = reportMapper.getSupplyMemberList(); 
		} else {
			for(Object memberObj : memberIdList) {
				int memberId = Integer.valueOf(String.valueOf(memberObj));
				Map<String, Object> memberDetail = reportMapper.getMemberDetail(memberId);
				if(!CommonUtil.checkIsNull(memberDetail)) {
					memberList.add(memberDetail);
				}
			}
		}
		
		param.put("hour_list", getHourList(param));
		
		for(Map<String, Object> memberDetail : memberList) {
			int rowspan1 = 0;
			// 구분 조회
			param.put("member_id", memberDetail.get("member_id"));
			List<Map<String, Object>> categoryList = reportMapper.getProductCategoryList(param);
			for(Map<String, Object> categoryDetail : categoryList) {
				int rowspan2 = 0;
				// 상품 조회
				param.put("ssp_category_id", categoryDetail.get("category_id"));
				List<Map<String, Object>> productList = reportMapper.getProductList(param);
				for(Map<String, Object> productDetail : productList) {
					// 상품의 노출수 조회 
					param.put("product_id", productDetail.get("product_id"));
					productDetail.put("cnt", reportMapper.getProductExposureCnt(param));
					productDetail.put("ssp_company_name", memberDetail.get("company_name"));
					rowspan1++;
					rowspan2++;
				}
				if(rowspan2 == 0) {
					rowspan1++;
					rowspan2 = 1;
				}
				categoryDetail.put("product_list", productList);
				categoryDetail.put("rowspan", rowspan2);
			}
			if(rowspan1 == 0) {
				rowspan1 = 1;
			}
			memberDetail.put("rowspan", rowspan1);
			memberDetail.put("category_list", categoryList);
		}
		
		respMap.setBody("list", memberList);
		
		return respMap.getResponse();
	}
	
	/**
	 * 상품 목록 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> reportProductList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 상품 목록 조회
		respMap.setBody("list", reportMapper.getReportProductList(param));
		
		return respMap.getResponse();
	}
	
	/**
	 * 상품별 리포트
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> reportListByProduct(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> list = new ArrayList<>();
		param.put("hour_list", getHourList(param));
		
		// 상품 목록으로 매체사 목록 조회 
		List<Map<String, Object>> memberList = reportMapper.getSupplyMemberListByProductList(param);
		
		for(Map<String, Object> memberDetail : memberList) {
			int rowspan1 = 0;
			// 매체사 ID로 생성한 상품 조회
			param.put("member_id", memberDetail.get("member_id"));
			List<Map<String, Object>> productList = reportMapper.getProductListBySupplyMember(param);
			
			for(Map<String, Object> productDetail : productList) {
				int rowspan2 = 0;
				param.put("product_id", productDetail.get("product_id"));
				
				List<Map<String, Object>> deviceList = deviceMapper.getList(param);
				for(Map<String, Object> deviceDetail : deviceList) {
					// 상품 - 디바이스의 노출수 조회
					param.put("device_id", deviceDetail.get("device_id"));
					deviceDetail.put("cnt", reportMapper.getProductDeviceExposureCnt(param));
					rowspan1++;
					rowspan2++;
				}
				if(rowspan2 == 0) {
					rowspan1++;
					rowspan2 = 1;
				}
				productDetail.put("rowspan", rowspan2);
				productDetail.put("device_list", deviceList);
			}
			if(rowspan1 == 0) {
				rowspan1 = 1;
			}
			memberDetail.put("rowspan", rowspan1);
			memberDetail.put("product_list", productList);
		}
		
		list.addAll(memberList);
		
		respMap.setBody("list", list);
		return respMap.getResponse();
	}
	

	
	/**
	 * 지역별 리포트 - 지도용
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> reportListByAreaMap(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		param.put("hour_list", getHourList(param));
		
		List<Map<String, Object>> list = reportMapper.getReportListByAreaMap(param);
		respMap.setBody("list", list);
		
		return respMap.getResponse();
	}
	
	
	/**
	 * 지역별 리포트
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> reportListByArea(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		param.put("hour_list", getHourList(param));
		
		// 광고주의 대행사 목록 조회 
		List<Map<String, Object>> agencyList = reportMapper.getAgencyListByDemand(param);
		int rowspan1 = 0;
		for(Map<String, Object> agencyDetail : agencyList) {
			param.put("agency_id", agencyDetail.get("member_id"));
			// 대행사의 담당 광고주 목록 조회
			List<Map<String, Object>> demandList = (List<Map<String, Object>>) reportMapper.getDemandListByAgency(param);
			
			for(Map<String, Object> demandDetail : demandList) {
				param.put("member_id", demandDetail.get("member_id"));
				
				List<Map<String, Object>> areaSiList = commonMapper.getAreaCodeBySi(param);
				List<Map<String, Object>> areaGuList = commonMapper.getAreaCodeByGu(param);
				
				List<Map<String, Object>> exposureList = reportMapper.getAreaExposureList(param);
				areaGuList.forEach(areaGuDetail -> {
					String siCode = (String) areaGuDetail.get("si_code");
					String guCode = (String) areaGuDetail.get("gu_code");
					
					Optional<Map<String, Object>> optional = exposureList.stream().filter(detail -> {
						return detail.get("si_code").equals(siCode) && detail.get("gu_code").equals(guCode); 
					}).findFirst();
					
					if(optional.isPresent()) {
						areaGuDetail.put("cnt", optional.get().get("cnt"));
					} else {
						areaGuDetail.put("cnt",  0);
					}
				});
				
				areaSiList.forEach(areaSiDetail -> {
					List<Map<String, Object>> guList = areaGuList.stream().filter(areaGuDetail -> {
						return areaSiDetail.get("si_code").equals(areaGuDetail.get("si_code"));
					}).collect(Collectors.toList());
					areaSiDetail.put("rowspan", guList.size());
					areaSiDetail.put("gu_list", guList);
				});
				demandDetail.put("rowspan", areaGuList.size());
				demandDetail.put("si_list", areaSiList);
				rowspan1 += areaGuList.size();
			}
			if(rowspan1 == 0) {
				rowspan1 = 1;
			}
			
			agencyDetail.put("demand_list", demandList);
			agencyDetail.put("rowspan", rowspan1);
		}
		
		
		// map level 8, 11, 14 
		// map_level >= 8 -> 시별 조회 
		// map_level >= 11 -> 구별 조회 
		// map_level >= 14 -> 동별 조회
		/*
		int mapLevel = (Integer) param.get("map_level");
		
		// 광고주 목록 
		List<Integer> memberList = (List<Integer>) param.get("member_list");
		
		// 광고주의 대행사 목록 조회 
		List<Map<String, Object>> agencyList = reportMapper.getAgencyListByDemand(param);
		
		for(Map<String, Object> agencyDetail : agencyList) {
			param.put("agency_id", agencyDetail.get("member_id"));
			// 대행사의 담당 광고주 목록 조회
			List<Map<String, Object>> demandList = (List<Map<String, Object>>) reportMapper.getDemandListByAgency(param);
			for(Map<String, Object> demandDetail : demandList) {
				param.put("member_id", demandDetail.get("member_id"));
				
				if(!CommonUtil.checkIsNull(param, "search_si_code")) {
					param.put("si_code", param.get("search_si_code"));
				}
				
				// 지역 조회 (시)
				List<Map<String, Object>> areaSiList = commonMapper.getAreaCodeBySi(param);
				
				if(mapLevel <= 8){
					// 시 단위
					for(Map<String, Object> areaSi : areaSiList) {
						param.put("si_code", areaSi.get("si_code"));
						areaSi.put("cnt", reportMapper.getAreaExposureCnt(param));
					}
				}else if(mapLevel >= 11 && mapLevel < 14) {
					// 구 단위
					for(Map<String, Object> areaSi : areaSiList) {
						param.put("si_code", areaSi.get("si_code"));
						param.remove("gu_code");
						
						if(!CommonUtil.checkIsNull(param, "search_gu_code")) {
							param.put("gu_code", param.get("search_gu_code"));
						}
						
						List<Map<String, Object>> areaGuList = commonMapper.getAreaCodeByGu(param);
						for(Map<String, Object> areaGu : areaGuList) {
							param.put("gu_code", areaGu.get("gu_code"));
							areaGu.put("cnt", reportMapper.getAreaExposureCnt(param));
						}
						areaSi.put("gu_list", areaGuList);
					}
				} else if(mapLevel >= 14) {
					// 동 단위
					for(Map<String, Object> areaSi : areaSiList) {
						param.put("si_code", areaSi.get("si_code"));
						param.remove("gu_code");
						
						if(!CommonUtil.checkIsNull(param, "search_gu_code")) {
							param.put("gu_code", param.get("search_gu_code"));
						}
						
						List<Map<String, Object>> areaGuList = commonMapper.getAreaCodeByGu(param);
						for(Map<String, Object> areaGu : areaGuList) {
							param.put("gu_code", areaGu.get("gu_code"));
							List<Map<String, Object>> areaDongList = commonMapper.getAreaCodeByDong(param);
							
							for(Map<String, Object> areaDong: areaDongList) {
								param.put("dong_code", areaDong.get("dong_code"));
								areaDong.put("cnt", reportMapper.getAreaExposureCnt(param));
							}
							areaGu.put("dong_list", areaDongList);
						}
						areaSi.put("gu_list", areaGuList);
					}
				} 
				demandDetail.put("area_list", areaSiList);
			}
			agencyDetail.put("demand_list", demandList);
		}
		
		*/
		// 모든 지역 조회 (시) 
		
		// 모든 지역 조회 (구) 
		
		// 구별 노출수 조회
		
		
//		List<Map<String, Object>> list = reportMapper.getAreaExposureCnt(param);
		respMap.setBody("list", agencyList);
		
		return respMap.getResponse();
	}
	

	/**
	 * 광고주별 리포트 엑셀 다운로드
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public void reportExcelByDemand(Map<String, Object> param, HttpServletResponse response) throws Exception {
		Workbook wb = new Workbook(response.getOutputStream(), "ExcelWriter", "1.0");
		Worksheet ws = wb.newWorksheet("sheet1");
		
		int row = 0;
		int startCol = 0, lastCol = 3;
		
		ws.value(row, 0, setExcelSearchDate(param));
		ws.range(row, startCol, row, lastCol).merge();
		
		row++;
		
		// title
		ws.value(row, 0, "대행사");
		ws.value(row, 1, "광고주");
		ws.value(row, 2, "광고명");
		ws.value(row, 3, "노출량");
		ws.range(row, startCol, row, lastCol).style().fillColor(Color.GRAY3).set();
		
		// col width 
		ws.width(0, 25);
		ws.width(1, 50);
		ws.width(2, 50);
		ws.width(3, 10);
		
		param.put("hour_list", getHourList(param));
		
		int totalCnt = 0;
		// 광고주의 대행사 목록 조회 
		List<Map<String, Object>> agencyList = reportMapper.getAgencyListByDemand(param);
		for(Map<String, Object> agencyDetail : agencyList) {
			int startRow1 = row + 1;
			String agencyName = (String) agencyDetail.get("company_name");
			param.put("agency_id", agencyDetail.get("member_id"));
			// 대행사의 담당 광고주 목록 조회
			List<Map<String, Object>> demandList = (List<Map<String, Object>>) reportMapper.getDemandListByAgency(param);
			for(Map<String, Object> demandDetail : demandList) {
				int startRow2 = row + 1;
				// 광고주의 광고 목록 조회
				String demandName = (String) demandDetail.get("company_name");
				param.put("member_id", demandDetail.get("member_id"));
				List<Map<String, Object>> sgList = reportMapper.getListByDemand(param);
				if(sgList.size() > 0) {
					for(Map<String, Object> sgDetail : sgList) {
						row++;
						String sgName = (String) sgDetail.get("sg_name");
						int cnt = Integer.valueOf(String.valueOf(sgDetail.get("cnt")));
						
						ws.value(row, 0, agencyName);
						ws.value(row, 1, demandName);
						ws.value(row, 2, sgName);
						ws.value(row, 3, cnt);
						
						totalCnt += cnt;
					}
				} else {
					row++;
					ws.value(row, 0, agencyName);
					ws.value(row, 1, demandName);
					ws.value(row, 2, "-");
					ws.value(row, 3, "-");
				}
				ws.range(startRow2, 1, row, 1).merge();
			}
			ws.range(startRow1, 0, row, 0).merge();
		}
		
		row++;
		
		ws.value(row, startCol, "합계");
		ws.range(row, startCol, row, lastCol - 1).merge();
		ws.value(row, lastCol, totalCnt);
		ws.range(row, startCol, row, lastCol).style().fillColor(Color.GRAY3).set();
		ws.range(1, startCol, row, lastCol).style().verticalAlignment("center").horizontalAlignment("center").set();
		
		
		excelDownload(response, wb, ws, "광고주별_리포트");
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
		
		List<Map<String, Object>> memberList = new ArrayList<>();
		List<Object> memberIdList = (List<Object>) param.get("member_list");
		if(memberIdList == null || memberIdList.size() == 0) {
			memberList = reportMapper.getSupplyMemberList(); 
		} else {
			for(Object memberObj : memberIdList) {
				int memberId = Integer.valueOf(String.valueOf(memberObj));
				Map<String, Object> memberDetail = reportMapper.getMemberDetail(memberId);
				if(!CommonUtil.checkIsNull(memberDetail)) {
					memberList.add(memberDetail);
				}
			}
		}
		
		param.put("hour_list", getHourList(param));
		
		int totalCnt = 0;
		for(Map<String, Object> memberDetail : memberList) {
			int startRow1 = row + 1;
			// 구분 조회
			String supplyName = (String) memberDetail.get("company_name");
			param.put("member_id", memberDetail.get("member_id"));
			List<Map<String, Object>> categoryList = reportMapper.getProductCategoryList(param);
			
			if(categoryList.size() > 0) {
				for(Map<String, Object> categoryDetail : categoryList) {
					int startRow2 = row + 1;
					// 상품 조회
					String categoryName = (String) categoryDetail.get("category_name");
					param.put("ssp_category_id", categoryDetail.get("category_id"));
					List<Map<String, Object>> productList = reportMapper.getProductList(param);
					if(productList.size() > 0) {
						for(Map<String, Object> productDetail : productList) {
							row++;
							// 상품의 노출수 조회 
							String productName = (String) productDetail.get("product_name");
							param.put("product_id", productDetail.get("product_id"));
							int cnt = reportMapper.getProductExposureCnt(param);
							
							ws.value(row, 0, supplyName);
							ws.value(row, 1, categoryName);
							ws.value(row, 2, productName);
							ws.value(row, 3, cnt);
							
							totalCnt += cnt;
						}
					} else {
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
		
		excelDownload(response, wb, ws, "매체별_리포트");
	}

	/**
	 * 상품별 리포트 엑셀 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public void reportExcelByProduct(Map<String, Object> param, HttpServletResponse response) throws Exception {
		Workbook wb = new Workbook(response.getOutputStream(), "ExcelWriter", "1.0");
		Worksheet ws = wb.newWorksheet("sheet1");
		
		int row = 0;
		int startCol = 0, lastCol = 3;
		
		ws.value(row, 0, setExcelSearchDate(param));
		ws.range(row, startCol, row, lastCol).merge();
		
		row++;
		
		// title
		ws.value(row, 0, "매체");
		ws.value(row, 1, "상품명");
		ws.value(row, 2, "디바이스명");
		ws.value(row, 3, "노출량");
		ws.range(row, startCol, row, lastCol).style().fillColor(Color.GRAY3).set();
		
		// col width 
		ws.width(0, 25);
		ws.width(1, 50);
		ws.width(2, 50);
		ws.width(3, 10);
		
		param.put("hour_list", getHourList(param));
		
		int totalCnt = 0;
		
		// 상품 목록으로 매체사 목록 조회 
		List<Map<String, Object>> memberList = reportMapper.getSupplyMemberListByProductList(param);
		for(Map<String, Object> memberDetail : memberList) {
			int startRow1 = row + 1;
			// 매체사 ID로 생성한 상품 조회
			String supplyName = (String) memberDetail.get("company_name");
			param.put("member_id", memberDetail.get("member_id"));
			List<Map<String, Object>> productList = reportMapper.getProductListBySupplyMember(param);
			if(productList.size() > 0) {
				for(Map<String, Object> productDetail : productList) {
					int startRow2 = row + 1;
					String productName = (String) productDetail.get("product_name");
					param.put("product_id", productDetail.get("product_id"));
					
					List<Map<String, Object>> deviceList = deviceMapper.getList(param);
					if(deviceList.size() > 0) {
						for(Map<String, Object> deviceDetail : deviceList) {
							row++;
							
							// 상품 - 디바이스의 노출수 조회
							String serialNumber = (String) deviceDetail.get("serial_number");
							param.put("device_id", deviceDetail.get("device_id"));
							int cnt = reportMapper.getProductDeviceExposureCnt(param);
							
							ws.value(row, 0, supplyName);
							ws.value(row, 1, productName);
							ws.value(row, 2, serialNumber);
							ws.value(row, 3, cnt);
							
							totalCnt += cnt;
						}
					} else {
						row++;
						
						ws.value(row, 0, supplyName);
						ws.value(row, 1, productName);
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
		
		excelDownload(response, wb, ws, "상품별_리포트");
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
		int startCol = 0, lastCol = 4;
		
		ws.value(row, 0, setExcelSearchDate(param));
		ws.range(row, startCol, row, lastCol).merge();
		
		row++;
		
		// title
		ws.value(row, 0, "대행사");
		ws.value(row, 1, "광고주");
		ws.value(row, 2, "지역");
		ws.value(row, 3, "지역구분");
		ws.value(row, 4, "노출량");
		ws.range(row, startCol, row, lastCol).style().fillColor(Color.GRAY3).set();
		
		// col width 
		ws.width(0, 25);
		ws.width(1, 25);
		ws.width(2, 25);
		ws.width(3, 25);
		ws.width(4, 10);
		
		param.put("hour_list", getHourList(param));
		
		int totalCnt = 0;
		
		// 광고주의 대행사 목록 조회 
		List<Map<String, Object>> agencyList = reportMapper.getAgencyListByDemand(param);
		for(Map<String, Object> agencyDetail : agencyList) {
			int startRow1 = row + 1;
			param.put("agency_id", agencyDetail.get("member_id"));
			String agencyName = (String) agencyDetail.get("company_name");
			
			// 대행사의 담당 광고주 목록 조회
			List<Map<String, Object>> demandList = (List<Map<String, Object>>) reportMapper.getDemandListByAgency(param);
			for(Map<String, Object> demandDetail : demandList) {
				int startRow2 = row + 1;
				param.put("member_id", demandDetail.get("member_id"));
				
				String demandName = (String) demandDetail.get("company_name");
				
				List<Map<String, Object>> areaSiList = commonMapper.getAreaCodeBySi(param);
				List<Map<String, Object>> areaGuList = commonMapper.getAreaCodeByGu(param);
				
				List<Map<String, Object>> exposureList = reportMapper.getAreaExposureList(param);
				
				for(Map<String, Object> areaSiDetail : areaSiList) {
					int startRow3 = row + 1;
					
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
						
						ws.value(row, 0, agencyName);
						ws.value(row, 1, demandName);
						ws.value(row, 2, siName);
						ws.value(row, 3, guName);
						ws.value(row, 4, cnt);
						
						totalCnt += cnt;
					}
					ws.range(startRow3, 2, row, 2).merge();
				}
				ws.range(startRow2, 1, row, 1).merge();
			}
			ws.range(startRow1, 0, row, 0).merge();
		}
		
		row++;
		
		ws.value(row, startCol, "합계");
		ws.range(row, startCol, row, lastCol - 1).merge();
		ws.value(row, lastCol, totalCnt);
		ws.range(row, startCol, row, lastCol).style().fillColor(Color.GRAY3).set();
		ws.range(1, startCol, row, lastCol).style().verticalAlignment("center").horizontalAlignment("center").set();
		
		excelDownload(response, wb, ws, "지역별_리포트");
	}
	
	/**
	 * end_hour - start_hour의 시간을 토대로 hour_** 시간 생성
	 * @param param
	 * @return
	 * @throws Exception
	 */
	private List<String> getHourList(Map<String, Object> param) throws Exception {
		// hour_list 생성
		List<String> hourList = new ArrayList<>();
		
		int sHour = Integer.valueOf(String.valueOf(param.get("start_hour")));
		int eHour = Integer.valueOf(String.valueOf(param.get("end_hour")));
		for(int i=sHour; i<eHour; i++) {
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
	 * 엑셀 조회 기간 텍스트 설정
	 * @param param
	 * @param excelWriter
	 * @throws Exception
	 */
	private String setExcelSearchDate(Map<String, Object> param) throws Exception {
		String startDate = (String) param.get("start_date");
		String startHour = (String) param.get("start_hour");
		String endDate = (String) param.get("end_date");
		String endHour = (String) param.get("end_hour");
		
		String searchDate = "조회기간: " + startDate + " " + startHour + ":00 ~ " + endDate + " " + endHour + ":00";
		
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
}
