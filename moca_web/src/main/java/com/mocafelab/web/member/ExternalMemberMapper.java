package com.mocafelab.web.member;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ExternalMemberMapper {
	
	public Map<String, Object> getMyData(Map<String, Object> param);
}
