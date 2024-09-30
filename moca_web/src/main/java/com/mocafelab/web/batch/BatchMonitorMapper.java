package com.mocafelab.web.batch;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BatchMonitorMapper {

	// 배치 코드로 batch_monitor 테이블 조회
	Map<String, Object> getBatchMonitor(Map<String, Object> param);
	
	// 배치 이름 or 배치 결과로 정렬해서 조회
	List<Map<String, Object>> getBatchMonitorList(Map<String, Object> param);

}