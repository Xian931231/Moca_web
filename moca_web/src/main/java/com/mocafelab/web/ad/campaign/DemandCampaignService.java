package com.mocafelab.web.ad.campaign;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mocafelab.web.ad.schedule.DemandScheduleMapper;
import com.mocafelab.web.ad.sg.DemandSgMapper;
import com.mocafelab.web.enums.ModifyHistory;
import com.mocafelab.web.enums.PayType;
import com.mocafelab.web.file.S3Service;
import com.mocafelab.web.member.DemandMemberService;
import com.mocafelab.web.vo.BeanFactory;
import com.mocafelab.web.vo.Code;
import com.mocafelab.web.vo.ResponseMap;

import net.newfrom.lib.util.CommonUtil;

@Transactional(rollbackFor = {Exception.class})
@Service
public class DemandCampaignService {
	
	@Autowired
	private BeanFactory beanFactory;
	
	@Autowired
	private DemandCampaignMapper demandCampaignMapper;
	
	@Autowired
	private DemandSgMapper demandSgMapper;
	
	@Autowired
	private DemandScheduleMapper demandScheduleMapper;
	
	@Autowired
	private DemandMemberService demandMemberService;
	
	@Autowired
	private S3Service s3Service;
	
	/**
	 * 캠페인 정보 리스트 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getCampaignList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> campaignList = demandCampaignMapper.getCampaignList(param);
		int campaignCount = demandCampaignMapper.getCampaignListCount(param);
		
		respMap.setBody("list", campaignList);
		respMap.setBody("tot_cnt", campaignCount);
		
		return respMap.getResponse();
	}
	
	/**
	 * 캠페인 상세 정보 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getCampaignDetail(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		Map<String, Object> campaignDetail = demandCampaignMapper.getCampaignDetail(param);
		
		if(!CommonUtil.checkIsNull(campaignDetail)) {
			List<Map<String, Object>> sgList = demandCampaignMapper.getSgList(param);
			campaignDetail.put("list", sgList);
			respMap.setBody("data", campaignDetail);
		} else {
			return respMap.getResponse(Code.NOT_EXIST_DATA);
		}
		return respMap.getResponse();
	}
	
	/**
	 * 캠페인 상세 정보 조회 (광고 제외)
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getCampaignDetailMin(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		Map<String, Object> detail = demandCampaignMapper.getCampaignDetailMin(param);
		
		if(!CommonUtil.checkIsNull(detail)) {
			respMap.setBody("data", detail);
		} else {
			return respMap.getResponse(Code.NOT_EXIST_DATA);
		}
		return respMap.getResponse();
	}
	
	/**
	 * 캠페인 등록
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> addCampaign(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		String payType = (String) param.get("pay_type");
		payType = payType.toUpperCase();
		
		// 과금 방식 검사
		if(!payType.equals(PayType.CPM.name()) && !payType.equals(PayType.CPP.name())) {
			throw new RuntimeException();
		}
		
		param.put("pay_type", payType);
		// 캠페인 등록
		Map<String, Object> addCampaign = demandCampaignMapper.addCampaign(param);
		
		if(CommonUtil.checkIsNull(addCampaign)) {
			throw new RuntimeException();
		}
		
		param.put("campaign_id", addCampaign.get("id"));
		//활동 이력 추가
		demandMemberService.addDspModHistory(param, ModifyHistory.CAMPAIGN_ADD);
		
		respMap.setBody("data", addCampaign);
		
		return respMap.getResponse();
	}
	
	/**
	 * 캠페인 수정
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> modifyCampaign(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
	
		// 존재하는 캠페인인지 검사
		if(demandCampaignMapper.hasMyCampaign(param) < 1) {
			throw new RuntimeException();
		}
		
		if(demandCampaignMapper.modifyCampaign(param) < 1) {
			throw new RuntimeException();
		}
		
		//활동 이력 추가
		//demandMemberService.addDspModHistory(param, "MHK002", "MHS003");
		demandMemberService.addDspModHistory(param, ModifyHistory.CAMPAIGN_MODIFY);
		
		return respMap.getResponse();
	}
	
	/**
	 * 캠페인 삭제 (하위 데이터 일괄 삭제)
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> removeCampaign(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<String> campaignIdList = (List<String>) param.get("campaign_id_list");
		for(String campaignId : campaignIdList) {
			param.put("campaign_id", campaignId);
			Map<String, Object> campaignInfo = demandCampaignMapper.getCampaignDetailMin(param);
			if(CommonUtil.checkIsNull(campaignInfo)) { // 내 소유이면서 존재하는 캠페인인지 체크
				throw new RuntimeException();
			}
			
			List<Map<String, Object>> sgList = demandCampaignMapper.getSgListInCampaign(campaignId);
			// 광고 존재
			if(sgList != null && !sgList.isEmpty()) {
				if(demandCampaignMapper.getProceedSgInCampaign(campaignId) > 0) { // 진행중인 광고가 존재할 경우
					throw new RuntimeException();
				}
				// sub 일괄 삭제
				for (Map<String, Object> sgId : sgList) {
					List<Map<String, Object>> materialList = demandSgMapper.getSgMaterial(sgId);
					for(Map<String, Object> materialInfo : materialList) {
						String fullPath = (String) materialInfo.get("file_path");
						s3Service.removeFile(fullPath);
					}
					demandSgMapper.removeSgMaterial(sgId);
					demandSgMapper.removeSgPayLog(sgId);
					
					String payType = (String) campaignInfo.get("pay_type");
					// 지정된 광고정보 삭제
					if(payType.equals(PayType.CPM.name())) { // CPM 광고일 때
						demandSgMapper.removeSgArea(sgId);
						demandSgMapper.removeSgWeek(sgId);
						demandScheduleMapper.removeScheduleTableBlock(sgId);
					} else if(payType.equals(PayType.CPP.name())) { // CPP 광고일 때
						demandScheduleMapper.removeScheduleProductSlotSg(sgId);
					}
					
					//광고 삭제
					Map<String, Object> removeSgManager = demandSgMapper.removeSgManager(sgId);
					if(removeSgManager.isEmpty()) {
						throw new RuntimeException();
					}
					param.put("sg_id", sgId.get("sg_id"));
					param.put("name", removeSgManager.get("name"));
					
					// 활동 이력 추가(광고)
					demandMemberService.addDspModHistory(param, ModifyHistory.DEMAND_REMOVE);
				}
			}
			param.put("campaign_id", campaignId);
			
			Map<String, Object> removeCampagin = demandCampaignMapper.removeCampaign(param);
			param.put("campaign_name", removeCampagin.get("name"));
			
			if(removeCampagin.isEmpty()) {
				throw new RuntimeException();
			}
			//활동 이력 추가 (캠페인)
			demandMemberService.addDspModHistory(param, ModifyHistory.CAMPAIGN_REMOVE);
		}
		
		return respMap.getResponse();
	}
}
