package com.mocafelab.web.product;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ExternalProductMapper {
	
	// 상품 리스트
	public List<Map<String, Object>> getList(Map<String, Object> param);
	// 상품 사양 상세
	public Map<String, Object> getProductSpecDetail(Map<String, Object> param);
	// 상품 사양 등록 / 수정
	public int addProductSpec(Map<String, Object> param);
	// 상품이 존재하는 매체 리스트
	public List<Map<String, Object>> getSupplyList(Map<String, Object> param);
	// 상품이 존재하는 분류 리스트
	public List<Map<String, Object>> getCategoryList(Map<String, Object> param);
}
