package com.mocafelab.web.common;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommonMapper {
	
	public List<Map<String, Object>> getCodeList(Map<String, Object> param);
	public int getCodeListCnt(Map<String, Object> param);
	public Map<String, Object> getCodeDetail(Map<String, Object> param);
	public int hasCode(Map<String, Object> param);
	public int addCode(Map<String, Object> param);
	public int modifyCode(Map<String, Object> param);
	public int removeCode(Map<String, Object> param);
	
	// 광고 카테고리 목록
	List<Map<String, Object>> getSgCodeList(Map<String, Object> param);
	
	// 디바이스 OS 목록
	List<Map<String, Object>> getDeviceCodeList(Map<String, Object> param);

	
	// 허용된 IP 대역인지 확인 
	public int hasPermitIp(Map<String, Object> param);
	
	//좌표에 대한 지역을 가져오기
	public Map<String, Object> getLocation(Map<String, Object> param);

	// product 정보 확인
	Map<String, Object> getProductDetail(Map<String, Object> param);
	// api키 등록
	int addApiKey(Map<String, Object> param);
	int addPackageId(Map<String, Object> param);
	
	
	// week_manager
	// 데이터 조회
	public List<Map<String, Object>> getWeekTime(Map<String, Object> param);
	// 데이터 수정
	public int modifyWeekTime(Map<String, Object> param);
	
	// area_code 
	public List<Map<String, Object>> getAreaCodeBySi(Map<String, Object> param);
	public List<Map<String, Object>> getAreaCodeByGu(Map<String, Object> param);
	public List<Map<String, Object>> getAreaCodeByDong(Map<String, Object> param);
}
