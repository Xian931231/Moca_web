package com.mocafelab.web.ad.sg;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SgMapper {
	// 관리자 
	public List<Map<String, Object>> getList(Map<String, Object> param); 	// 광고 리스트
	public int getListCnt(Map<String, Object> param); 						// 광고 리스트 총 개수
	public Map<String, Object> getDetail(Map<String, Object> param); 		// 광고 상세
	public List<Map<String, Object>> getMaterialDetail(Map<String, Object> param);// 광고 소재 상세
	public int modifyApprovalStatus(Map<String, Object> param); 			// 광고 승인 정보 업데이트
	
	// 승인 거절 사유 수정
	public int modifyRejectReason(Map<String, Object> param);
	
	// 대기중인 광고인지 검사
	public int isWaitSg(Map<String, Object> param);
	// 입금 완료 상태인지 검사
	public int isPayComplete(Map<String, Object> param);
	
	//캠페인 내 광고 삭제
	public int removeSgManager(Map<String, Object> param);
	public int removeSgWeek(Map<String, Object> param);
	public int removeSgArea(Map<String, Object> param);
	public int removeSgMaterial(Map<String, Object> param);

	// 광고 긴급 종료
	public int stopSg(Map<String, Object> param);
	//진행중인 광고인지 확인
	public int isProceed(Map<String, Object> param);
	
	// 모든 광고 조회
	public List<Map<String, Object>> getSgListByDemand(Map<String, Object> param);
	
	//종료 상태인 모든 광고 조회
	public List<Map<String, Object>> getEndSgList(Map<String, Object> param);
	//종료 상태인 모든 광고 조회 개수
	public int getEndSgListCnt(Map<String, Object> param);
	// 환급 시 어떤 입금내역에 대한 환급인지 id 가져오기
	public Map<String, Object> getDepositPayLogId(Map<String, Object> param);
	// 광고 입금 금액 로그 쌓기
	public Map<String, Object> addSgPayLog(Map<String, Object> param);
	// 광고 입금액 변경
	public Map<String, Object> modifyPayPrice(Map<String, Object> param);
	
	
	// -- 관리자 
}