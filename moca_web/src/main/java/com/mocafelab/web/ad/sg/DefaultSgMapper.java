package com.mocafelab.web.ad.sg;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DefaultSgMapper {
	// 목록 
	public int getListCnt(Map<String, Object> param);
	public List<Map<String, Object>> getList(Map<String, Object> param);
	
	// 상세
	public Map<String, Object> getDetail(Map<String, Object> param);
	
	// 등록
	public int addDefaultSg(Map<String, Object> param);
	
	// 수정 
	public int modifyDefaultSg(Map<String, Object> param);
	
	// 삭제 
	public int removeDefaultSg(Map<String, Object> param);
	
	// 파일 정보 업데이트
	public int modifyFileInfo(Map<String, Object> param);
	
}