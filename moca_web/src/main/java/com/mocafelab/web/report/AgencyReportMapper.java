package com.mocafelab.web.report;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AgencyReportMapper {
	
	//회원 조회
	public Map<String, Object> getMemberInfo(Map<String, Object> param);
	
	//로그인한 유저의 담당 광고주 조회	
	public List<Map<String, Object>> getLoginUserAccessDemand(Map<String, Object> param);
	
	//로그인 한 계정이 담당하는 광고 노출이 완료된(dsp_report.count_sg) 광고주 조회
	public List<Map<String, Object>> getLoginCountSgDemandList(Map<String, Object> param);
	
	//광고주별 리포트 리스트 조회
	public List<Map<String, Object>> getListByDemand(Map<String, Object> param);
	
	//기간별 리포트 리스트 조회
	public Map<String, Object> getListByTime(Map<String, Object> param);

	//일별 리포트 리스트 조회
	public Map<String, Object> getListByDate(Map<String, Object> param);
	
	//월별 리포트 리스트 조회
	public Map<String, Object> getListByMonth(Map<String, Object> param);
	
	// 로그인한 대행사가 담당하는 광고주가 등록한 광고를 노출한 적있는 상품을 등록한 매체사 조회
	public List<Map<String, Object>> getDemandOfAgencySupplyMemberList(Map<String, Object> param);
	
	// 로그인한 대행사가 담당하는 광고주가 등록한 광고를 노출한 적있는 상품의 구분 리스트 조회
	public List<Map<String, Object>> getDemandOfAgencyCategoryList(Map<String, Object> param);
	
	// 로그인한 대행사가 담당하는 광고주가 등록한 광고를 노출한 적있는 상품 리스트 조회
	public List<Map<String, Object>> getDemandOfAgencyProductList(Map<String, Object> param);
	
	//매체사의 상품 디바이스별 노출 수 조회
	public Integer getProductExposureCnt(Map<String, Object> param);
	
	//지역별 리포트 리스트 조회
	public List<Map<String, Object>> getAreaExposureList(Map<String, Object> param);
	
	//지역별 리포트 리스트 맵용 조회
	public List<Map<String, Object>> getReportListByAreaMap(Map<String, Object> param);
	
	
	
	
	
}
