package com.mocafelab.web.ad.campaign;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DemandCampaignMapper {
	
	// 캠페인 조회
	public List<Map<String, Object>> getCampaignList(Map<String, Object> param);
	public int getCampaignListCount(Map<String, Object> param);
	
	// 캠페인 상세조회
	public Map<String, Object> getCampaignDetail(Map<String, Object> param);
	public List<Map<String, Object>> getSgList(Map<String, Object> param);
	
	// 캠페인 등록
	public Map<String, Object> addCampaign(Map<String, Object> param);
	
	// 캠페인 수정
	public int modifyCampaign(Map<String, Object> param);
	
	// 진행중인 광고
	public int getProceedSgInCampaign(String campaign_id);
	
	// 캠페인 삭제
	public List<Map<String, Object>> getSgListInCampaign(String campaign_id);
	public Map<String, Object> removeCampaign(Map<String, Object> parma);

	// 내 소유이면서 존재하는 캠페인인지 체크 
	public int hasMyCampaign(Map<String, Object> param);
	// 내 소유의 캠페인 정보
	public Map<String, Object> getCampaignDetailMin(Map<String, Object> param);
	
	// 광고주 광고 진행 상황 count
	public Map<String, Object> getCountCampaign(Map<String, Object> param);
	
	// 광고주 캠페인 리스트
	public List<Map<String, Object>> getCampaignListOfDemand(Map<String, Object> param);
}
