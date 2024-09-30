package com.mocafelab.web.report;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DemandReportMapper {

	//광고 조회
	public Map<String, Object> getSgInfo(Map<String, Object> param);
	
	//캠페인 이름조회
	public String getCampaignName(Map<String, Object> param);
	
	//상품에 노출된 광고가 속해있는 캠페인 리스트 
	public List<Map<String, Object>> getCampaignList(Map<String, Object> param);
	
	//광고 리스트 조회
	public List<Map<String, Object>> getSgList(Map<String, Object> param);
	
	//상품에 노출된광고 리스트 조회
	public List<Map<String, Object>> getCountSgList(Map<String, Object> param);
	
	//시간별 리포트
	public Map<String, Object> getListByTime(Map<String, Object> param);
	
	//일별 리포트
	public Map<String, Object> getListByDate(Map<String, Object> param);
	
	//월별 리포트
	public Map<String, Object> getListByMonth(Map<String, Object> param);
	
	//매체별 리포트
	public int getProductExposureCnt(Map<String, Object> param);
	
	//지역별 리포트
	public List<Map<String, Object>> getAreaExposureList(Map<String, Object> param);
	
	//지역별 리포트 맵용
	public List<Map<String, Object>> getReportListByAreaMap(Map<String, Object> param);
	
	//로그인한 광고주의 광고를 노출 할 수 있는 매체사 리스트 조회
	public List<Map<String, Object>> getSupplyMemberListOfDemand(Map<String , Object> param);
	
	//로그인한 광고주의 광고를 노출 할 수 있는 구분 리스트 조회
	public List<Map<String, Object>>getCategoryListOfDemand(Map<String, Object> param);
	
	//로그인한 광고주의 광고를 노출 할 수 있는 상품 리스트 조회
	public List<Map<String, Object>>getProductListOfDemand(Map<String, Object> param);
	
	
}
