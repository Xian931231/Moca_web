package com.mocafelab.web.ad.schedule;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DemandScheduleMapper {
	// 지정된 광고 삭제 CPM
	public int removeScheduleTableBlock(Map<String, Object> param);
	// 지정된 광고 삭제 CPP
	public int removeScheduleProductSlotSg(Map<String, Object> param);
}
