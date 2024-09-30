package com.mocafelab.web.device;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ExternalDeviceMapper {
	// 디바이스 목록
	public List<Map<String, Object>> getList(Map<String, Object> param);
	// 디바이스 등록
	public int addDevice(Map<String, Object> param);
	// 디바이스 업데이트
	public int modifyDevice(Map<String, Object> param);
	// 디바이스 삭제
	public int removeDevice(Map<String, Object> param);
	// 디바이스 시리얼넘버 중복 체크 
	public int hasDuplicateDeviceSerialNum(Map<String, Object> param);
	// 상품에 디바이스 등록 날짜 수정
	public int modifyProductDeviceInsertDt(Map<String, Object> param);
	// 상품에 속하는 디바이스인지 확인
	public int hasDeviceInCategory(Map<String, Object> param);
	
	// 측정 장비 목록
	public List<Map<String, Object>> getSensorList(Map<String, Object> param);
	// 측정 장비 상세
	public Map<String, Object> getSensorDetail(Map<String, Object> param);
	// 측정 장비 이름 중복 체크
	public int hasDuplicateSensorName(Map<String, Object> param);
	// 측정 장비 등록 
	public int addSensor(Map<String, Object> param);
	// 측정 장비 수정 
	public int modifySensor(Map<String, Object> param);
	// 측정 장비 시리얼 번호 목록
	public List<Map<String, Object>> getSensorDeviceList(Map<String, Object> param);
	// 측정 장비 시리얼 번호 중복 체크
	public int hasDuplicateSensorSerialNum(Map<String, Object> param);
	// 측정 장비 시리얼 번호 등록
	public int addSensorDevice(Map<String, Object> param);
	// 측정 장비 시리얼 번호 수정 
	public int modifySensorDevice(Map<String, Object> param);
	// 측정 장비 시리얼 번호 삭제
	public int removeSensorDevice(Map<String, Object> param);
	// 장비에 속하는 측정 장비인지 확인
	public int hasSensorDeviceInSensor(Map<String, Object> param);
	
	// 게재 위치 목록
	public List<Map<String, Object>> getMotorPositionList(Map<String, Object> param);
	// 게재 위치 상세
	public Map<String, Object> getMotorPositionDetail(Map<String, Object> param);
	// 게재 위치 이름 중복 체크 
	public int hasDuplicateMotorPositionName(Map<String, Object> param);
	// 게재 위치 등록
	public int addMotorPosition(Map<String, Object> param);
	// 게재 위치 수정
	public int modifyMotorPosition(Map<String, Object> param);
	// 게재 위치 아이디 목록
	public List<Map<String, Object>> getMotorPositionIdList(Map<String, Object> param);
	// 게재 위치 구분 아이디 중복 체크
	public int hasDuplicateMotorId(Map<String, Object> param);
	// 게재 위치 구분 IP 중복 체크
	public int hasDuplicateMotorIP(Map<String, Object> param);
	// 게재 위치 구분 아이디 등록
	public int addMotorPositionId(Map<String, Object> param);
	// 게재 위치 구분 아이디 수정
	public int modifyMotorPositionId(Map<String, Object> param);
	// 게재 위치 구분 아이디 삭제
	public int removeMotorPositionId(Map<String, Object> param);
	// 게재 위치 구분 아이디 상세
	public Map<String, Object> getMotorPositionIdDetail(Map<String, Object> param);
	// 게재 위치와 매칭된 디바이스 확인
	public Map<String, Object> hasMatchingDevice(Map<String, Object> param);
	// 개제 위치와 매칭된 상품 기기 해제
	public int modifyDeviceMotorIdToNull(Map<String, Object> param);
	// 개제 위치와 매칭된 측정 장비 해제
	public int modifySensorMotorIdToNull(Map<String, Object> param);
	// 진행중인 CPP 광고 상품에 속한 디바이스인지 조회
	public int hasDeviceInProgressCppCategory(Map<String, Object> param);
	
	// 기기/위치 매칭 검수
	public int modifyMotorStatus(Map<String, Object> param);
	// 디바이스 게재 위치 아이디 수정
	public int modifyDeviceMotorId(Map<String, Object> param);
	// 측정 장비 게재 위치 아이디 수정
	public int modifySensorDeviceMotorId(Map<String, Object> param);
	// 매칭되는 디바이스 유효성 검사
	public Map<String, Long> hasValidMatchingDevice(Map<String, Object> param);
	// 매칭되는 측정장비 유효성 검사
	public Map<String, Long> hasValidMatchingSensor(Map<String, Object> param);
}
