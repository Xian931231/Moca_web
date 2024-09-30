package com.mocafelab.web.device;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SupplyDeviceMapper {

	// 오류 장비 목록
	public List<Map<String, Object>> getDeviceErrorList(Map<String, Object> param);
	
	// 오류 장비 개수
	public int getDeviceErrorCnt(Map<String, Object> param);
	
	// 상품관리 > 디바이스 관리 목록
	public List<Map<String, Object>> getDeviceList(Map<String, Object> param);
	
	// 상품관리 > 디바이스 관리 개수
	public int getDeviceCnt(Map<String, Object> param);
}
