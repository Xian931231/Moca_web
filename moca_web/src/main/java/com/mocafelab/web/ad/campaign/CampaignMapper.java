package com.mocafelab.web.ad.campaign;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CampaignMapper {
	//캠페인 리스트
	public List<Map<String, Object>> getCampaignList(Map<String,Object> param);
	//캠페인 갯수
	public int getCampaignCnt(Map<String, Object> param);
	//캠페인 상세
	public Map<String, Object> getCampaignDetail(Map<String, Object> param);
	//캠페인 내 광고 리스트
	public List<Map<String, Object>> getSgList(Map<String, Object> param);
	// 캠페인 내 광고 상세 정보
	public Map<String, Object> getSgDetail(Map<String, Object> param);

	// 모든 캠페인 조회
	public List<Map<String, Object>> getCampaignListByDemand(Map<String, Object> param);
	
}
