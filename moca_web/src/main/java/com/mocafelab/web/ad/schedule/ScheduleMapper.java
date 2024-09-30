package com.mocafelab.web.ad.schedule;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ScheduleMapper {
	// 편성표 리스트
	public List<Map<String, Object>> getList(Map<String, Object> param);
	// 편성표 리스트 총 개수
	public int getListCnt(Map<String, Object> param);
	// 편성표에 등록된 상품 조회
	public List<Map<String, Object>> getProductList(Map<String, Object> param);
	// 편성표 상세
	public Map<String, Object> getDetail(Map<String, Object> param);
	// 편성표에 등록된 슬롯 리스트
	public List<Map<String, Object>> getSlotList(Map<String, Object> param);
	// 편성표 삭제
	public int removeSchedule(Map<String, Object> param);
	// 진행중인 CPP 광고 체크
	public int hasInProgressCpp(int param);
	// 편성표 등록 
	public int addSchedule(Map<String, Object> param);
	// 편성표 슬롯 등록 
	public int addScheduleSlot(Map<String, Object> param);
	// 편성표에 속하는 상품 등록 
	public int addScheduleProduct(Map<String, Object> param);
	// 편성표에 속하는 상품 유효성 
	public Map<String, Object> hasValidProduct(Map<String, Object> param);
	// 편성표 수정
	public int modifySchedule(Map<String, Object> param);
	// 편성표 슬롯 수정
	public int modifyScheduleSlot(Map<String, Object> param);
	// 편성표 슬롯 삭제
	public int removeScheduleSlot(Map<String, Object> param);
	// 편성표에 속하는 상품 삭제
	public int removeScheduleProduct(Map<String, Object> param);
	// 광고와 매칭 되었는지 체크
	public int hasMatchingSg(Map<String, Object> param);
	// 상품이 등록된 스케쥴 조회
	Map<String, Object> getProductSchedule(Map<String, Object> param);
	// 상품의 정보: 매체 > 분류 > 상품
	Map<String, Object> getProductDetail(Map<String, Object> param);
	// 같은 스케쥴의 광고 중 재생시간이 같은 광고
	List<Map<String, Object>> getSameScheduleSgList(Map<String, Object> param);
	// CPP 광고 지정 정보 제거
	int removeScheduleProductSlotSg(Map<String, Object> param);
	// CPP 광고 지정 정보 등록
	int addScheduleProductSlotSg(Map<String, Object> param);
	// 상품, 슬롯 중복 확인
	public int hasScheduleProductSlot(Map<String, Object> param);
	// 상품 product_id 가져오기
	public Map<String, Object> getScheduleProductId(Map<String, Object> param);
	// 편성표에 속하는 상품 정보
	public Map<String, Object> getScheduleProduct(int param);
	// 매체 목록 
	public List<Map<String, Object>> getSupplyList(Map<String, Object> param);
	// 분류 목록 
	public List<Map<String, Object>> getCategoryList(Map<String, Object> param);
	// 편성표에 등록 안된 상품 목록 
	public List<Map<String, Object>> getRemainProductList(Map<String, Object> param);
	// 편성표 상품에 속한 광고 리스트
	public List<Map<String, Object>> getScheduleProductSgList(Map<String, Object> param);
	// 상품당 맵핑되는 슬롯 정보 등록
	public int addScheduleProductSlot(Map<String, Object> param);
	// 상품과 맵핑된 슬롯 정보 삭제
	public int removeScheduleProductSlot(Map<String, Object> param);
	// 편성표 상품 상세 조회
	public Map<String, Object> getScheduleProduct(Map<String, Object> param);
	// 편성표 상품에 속한 슬롯 정보
	public List<Map<String, Object>> getScheduleProductSlot(Map<String, Object> param);
	// CPP 광고 정보
	public Map<String, Object> getScheduleProductSlotSg(Map<String, Object> param);
	// 신청하려는 cpp광고 조건에 맞는 상품의 슬롯 목록
	public List<Map<String, Object>> getPossibleSlotList(Map<String, Object> param);
	// 슬롯과 연결된 광고 개수
	public int getScheduleProductSlotSgCnt(Map<String, Object> param);
}

