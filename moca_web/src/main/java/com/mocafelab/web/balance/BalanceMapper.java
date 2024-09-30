package com.mocafelab.web.balance;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BalanceMapper {
	// 정산관리 목록
	public List<Map<String, Object>> getList(Map<String, Object> param);
	
	// 정산관리 목록 갯수
	public int getListCnt(Map<String, Object> param);
	
	// 정산금 지급
	public int payCalculate(Map<String, Object> param);
}

