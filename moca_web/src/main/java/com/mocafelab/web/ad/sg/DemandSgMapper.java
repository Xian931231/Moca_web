package com.mocafelab.web.ad.sg;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DemandSgMapper {

	// 광고주 ( demand )
	public int demandModifyStatus(Map<String, Object> param);
	
	public int demandChangeStatus(Map<String, Object> param);
	public int demandDelete(Map<String, Object> param);

	// 광고(정책) 기본 정보 등록
	public int addSgBasic(Map<String, Object> param);
	// 광고 노출 지역 등록
	public int addSgArea(Map<String, Object> param); 
	// 광고 스케줄 등록
	public int addSgSchedule(Map<String, Object> param);
	// 광고 소재 등록
	public int addSgMaterial(Map<String, Object> param);
	// 캠페인 조회
	public Map<String, Object> getCampaign(Map<String, Object> param);
	// 카테고리 코드 유무 체크
	public int hasCategoryCode(Map<String, Object> param);
	// 지역 코드 유무 체크
	public int hasAreaCode(Map<String, Object> param); 
	// 광고 등록 시 캠페인 등록자 확인
	public int hasCampaignMember(Map<String, Object> param);
	
	// 단일 광고 조회
	Map<String, Object> getSgManager(Map<String, Object> param);
	// 광고 소재 조회
	List<Map<String, Object>> getSgMaterial(Map<String, Object> param);
	// 광고 스케쥴 조회
	List<Map<String, Object>> getSgWeek(Map<String, Object> param);
	// 광고 지역 조회
	Map<String, Object> getSgArea(Map<String, Object> param);
	// 광고 진행 상품 조회
	Map<String, Object> getSgProduct(Map<String, Object> param);
	
	// 광고 정책(기본정보) 수정
	int modifySgManager(Map<String, Object> param);
	// 광고 노출 지역 수정
	int modifySgArea(Map<String, Object> param);
	// 광고 스케쥴 수정
	int modifySgWeek(Map<String, Object> param);
	// 광고 소재 수정
	int modifySgMaterial(Map<String, Object> param);
	// sg 아이디에 해당하는 광고 노출 지역 제거
	int removeSgAreaByAreaId(Map<String, Object> param);
	// 광고 지역 유무 확인
	int hasSgArea(Map<String, Object> param);
	
	// 광고주 캠페인,광고 목록
	List<Map<String, Object>> getDemandSgList(Map<String, Object> param);
	// 광고주 캠페인,광고 목록 카운트
	int getDemandSgListCount(Map<String, Object> param);
	
	// 슬롯에 해당하는 광고id 조회
	public List<Map<String, Object>> getSgIdList(Map<String, Object> param);
	// 슬롯에 광고가 존재할 때 광고가 유효한지 체크
	public int isExpireSg(Map<String, Object> param);
	
	// -- 광고주

	public Map<String, Object> removeSgManager(Map<String, Object> param);
	public int removeSgWeek(Map<String, Object> param);
	public int removeSgArea(Map<String, Object> param);
	public int removeSgMaterial(Map<String, Object> param);
	public int removeSgPayLog(Map<String, Object> param);
	
	public int removeSgMaterialFile(Map<String, Object> param);
	public Map<String, Object> getSgMaterialFile(Map<String, Object> param);
	
	// 가중치 조회
	public Map<String, Object> getRate(Map<String, Object> param);
	// rate_kind 별 가중치 목록 조회
	public List<Map<String, Object>> getRateList(Map<String, Object> param);
	
	// 구 카테고리 목록
	public List<Map<String, Object>> getAreaGuList(Map<String, Object> param);
	
	// 광고 소재 detail
	public Map<String, Object> getMaterialDetail(Map<String, Object> param);
	
	// 캠페인 정보를 포함한 광고 리스트
	public List<Map<String, Object>> getList(Map<String, Object> param);
}