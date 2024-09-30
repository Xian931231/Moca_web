package com.mocafelab.web.dashboard;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DashboardMapper {
	// 대시보드 정보 조회
	public Map<String, Object> getDashboardDetail(Map<String, Object> param);
	
	// 총 노출 수 그래프
	public int getExposureTotal(Map<String, Object> param);
	public List<Map<String, Object>> getExposureData(Map<String, Object> param);
	
	// 총 집행금액 그래프
	public int getPriceTotal(Map<String, Object> param);
	public List<Map<String, Object>> getPriceData(Map<String, Object> param);
	
	// 전체 매체 수
	public int getSupplyCount(Map<String, Object> param);
	
	// 운영 중인 상품 수
	public int getProductCount(Map<String, Object> param);
	
	// 매체 별 노출 수
	public List<Map<String, Object>> getSupplyExpousreTotal(Map<String, Object> param);

	// 구분 별 노출 수
	public List<Map<String, Object>> getCategoryExposureTotal(Map<String, Object> param);
	
	// 시/도 별 노출 수
	public List<Map<String, Object>> getSiExposureTotal(Map<String, Object> param);
	public List<Map<String, Object>> getGuExposureTotal(Map<String, Object> param);
	
	// 차량의 총 노출 수 + 차량 마지막 위치
	public List<Map<String, Object>> getMotorExposureTotal(Map<String, Object> param);
	
	// 디바이스 별 노출 수
	public List<Map<String, Object>> getProductExposureTotal(Map<String, Object> param);
}
