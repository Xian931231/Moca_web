package com.mocafelab.web.product;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper {

	// 매체/상품 목록 조회
	public List<Map<String, Object>> getSupplyProductList(Map<String, Object> param);
	
	// 분류 목록
	public List<Map<String, Object>> getCategoryList(Map<String, Object> param);
	
	// 매체 > 분류
	public List<Map<String, Object>> getCategoryListBySupply(Map<String, Object> param);
	
	// 분류 목록 개수
	public int getCategoryCnt(Map<String, Object> param);
	
	// 분류 추가
	public Map<String, Object> addCategory(Map<String, Object> param);
	
	// 동일 매체 내 분류명 중복 확인
	public int hasSupplyCategory(Map<String, Object> param);
	
	// 분류 삭제
	public int removeCategory(Map<String, Object> param);
	
	// 아이콘 수정
	public int modifyCategoryIcon(Map<String, Object> param);

	// 상품 유무 확인
	public Map<String, Object> hasProduct(Map<String, Object> param);
	
	// 상품 개수
	public int hasProductCnt(Map<String, Object> param);
	
	// 상품명 중복 확인
	public int hasProductName(Map<String, Object> param);
		
	// 상품 목록
	public List<Map<String, Object>> getProductList(Map<String, Object> param);
	
	// 상품 목록 개수
	public int getProductCnt(Map<String, Object> param);
	
	// 상품 추가
	public int addProduct(Map<String, Object> param);
	
	// 상품 관리 - 상품 기본정보 수정
	public int modifyProduct(Map<String, Object> param);
	
	// 상품 관리 - 상품 사양 등록
	public int modifyProductSpec(Map<String, Object> param);
	
	// 상품 상세보기
	public Map<String, Object> getProductDetail(Map<String, Object> param);
	
	// 상품 삭제
	public int removeProduct(Map<String, Object> param);
	
	// 상품 판매 시작
	public int startProductSale(Map<String, Object> param);
	
	// api 키 등록
	int addApiKey(Map<String, Object> param);
	
	// CPP 진행 상품 리스트
	public List<Map<String, Object>> getCppProductList(Map<String, Object> param);
	
	// CPP 진행 상품 개수
	public int getCppProductCnt(Map<String, Object> param);

	// 분류 상세
	public Map<String, Object> getCategoryDetail(Map<String, Object> param);	
	
	// 진행 중인 광고 상품 확인
	public int hasProgressSgInProduct(Map<String, Object> param);
}
