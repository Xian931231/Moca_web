package com.mocafelab.web.product;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SupplyProductMapper {

	// 총 노출 수
	public int getProductImpressionsCnt(Map<String, Object> param);
	
	// 매체현황 > 총 노출 수 리스트
	public List<Map<String, Object>> getProductImpressionsList(Map<String, Object> param);
	
	// 매체현황 > 운영 중 상품 리스트
	public List<Map<String, Object>> getProductProgressList(Map<String, Object> param);
	
	// 운영 중 상품 개수
	public int getProductCnt(Map<String, Object> param);
	
	// 상품관리 > 상품관리 리스트
	public List<Map<String, Object>> getProductManageList(Map<String, Object> param);
	
	// 로그인한 매체사의 분류 목록
	public List<Map<String, Object>> getCategoryList(Map<String, Object> param);
	
	// 로그인한 매체사의 분류 개수
	public int getCategoryCnt(Map<String, Object> param);
}
