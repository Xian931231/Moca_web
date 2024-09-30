package com.mocafelab.web.report;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReportMapper {
	
	public Map<String, Object> getMemberDetail(int memberId);
	
	// 광고주의 담당 대행사 목록 조회
	public List<Map<String, Object>> getAgencyListByDemand(Map<String, Object> param);
	
	// 대행사의 담당 광고주 목록 조회
	public List<Map<String, Object>> getDemandListByAgency(Map<String, Object> param);
	
	// 광고주별 리포트 조회
	public List<Map<String, Object>> getListByDemand(Map<String, Object> param);
	
	// 승인된 매체사 목록 조회
	public List<Map<String, Object>> getSupplyMemberList();
	
	// 매체사가 등록한 카테고리 조회(상품이 있는 카테고리만) 
	public List<Map<String, Object>> getProductCategoryList(Map<String, Object> param);
	
	// 매체사가 등록한 상품 목록 조회 
	public List<Map<String, Object>> getProductList(Map<String, Object> param);
	
	// 매체사의 상품별 노출수 조회 
	public Integer getProductExposureCnt(Map<String, Object> param);
	
	// 상품을 생성한 매체사 목록 조회
	public List<Map<String, Object>> getSupplyMemberListByProductList(Map<String, Object> param);
	
	// 매체사가 생성한 상품 목록 조회(등록한 디바이스가 있는경우만)
	public List<Map<String, Object>> getProductListBySupplyMember(Map<String, Object> param);
	
	
	// 승인된 매체사의 매체/구분/상품명으로 조회 가능한 상품 목록 조회
	public List<Map<String, Object>> getReportProductList(Map<String, Object> param);
	
	// 매체사의 상품 디바이스별 노출수 조회 
	public Integer getProductDeviceExposureCnt(Map<String, Object> param);
	
	public List<Map<String, Object>> getReportListByAreaMap(Map<String, Object> param);
	
	// 지역별 노출수 조회
	public List<Map<String, Object>> getAreaExposureList(Map<String, Object> param);
}
