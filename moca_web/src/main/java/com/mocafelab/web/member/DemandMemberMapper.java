package com.mocafelab.web.member;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface DemandMemberMapper {

	//활동 이력 리스트
	public List<Map<String, Object>> getList(Map<String, Object> param);
	//활동 이력 리스트개수
	public int getListCnt(Map<String, Object> param);
	//활동 이력 추가
	public int addDspModHistory(Map<String, Object> param);
	
	public List<Map<String, Object>> getSupplyList(Map<String, Object> param);
	
}
