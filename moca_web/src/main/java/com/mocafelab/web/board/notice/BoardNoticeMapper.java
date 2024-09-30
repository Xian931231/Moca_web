package com.mocafelab.web.board.notice;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BoardNoticeMapper {
	public List<Map<String, Object>> getList(Map<String, Object> param);
	public int getListCnt(Map<String, Object> param);
	public Map<String, Object> getDetail(Map<String, Object> param);
	
}
