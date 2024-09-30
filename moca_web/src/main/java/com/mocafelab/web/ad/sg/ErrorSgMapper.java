package com.mocafelab.web.ad.sg;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErrorSgMapper {
	
	public int getListCntForSgManage(Map<String, Object> param);
	public List<Map<String, Object>> getListForSgManage(Map<String, Object> param);
	public int modifyLogStatus(Map<String, Object> param);
	
	public int getListCntForSspManage(Map<String, Object> param);
	public List<Map<String, Object>> getListForSspManage(Map<String, Object> param);
	
	public Map<String, Object> getAdEventLogEventDate(Map<String, Object> param);
	public int modifyBatchMonitorLastIndex(Map<String, Object> param);
}