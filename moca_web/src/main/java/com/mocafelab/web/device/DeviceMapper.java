package com.mocafelab.web.device;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DeviceMapper {
	// 디바이스 리스트
	public List<Map<String, Object>> getList(Map<String, Object> param);
	// 디바이스 리스트 개수
	public int getListCnt(Map<String, Object> param);
	// 디바이스 별 현황 매체사 리스트
	public List<Map<String, Object>> getSupplyList(Map<String, Object> param);
	// 디바이스 별 현황 매체사 개수
	public int getSupplyCnt(Map<String, Object> param);
	// 디바이스 별 현황 디바이스 리스트
	public List<Map<String, Object>> getDeviceWithMotorList(Map<String, Object> param);
	// 디바이스 상태 변경
	public int modifyDeviceStatus(Map<String, Object> param);
	// 디바이스 상세 정보
	public Map<String, Object> getDeviceDetail(Map<String, Object> param);
	// 디바이스 삭제
	public int removeDevice(Map<String, Object> param);
}
