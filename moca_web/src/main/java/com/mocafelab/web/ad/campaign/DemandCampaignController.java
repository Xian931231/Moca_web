package com.mocafelab.web.ad.campaign;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.vo.RequestMap;

@RestController
@RequestMapping("${apiPrefix}/campaign/demand")
public class DemandCampaignController {
	
	@Autowired
	private DemandCampaignService demandCampaignService;
	
	/**
	 * 캠페인 정보 리스트 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/list")
	public Map<String, Object> getCampaignList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return demandCampaignService.getCampaignList(param);
	}
	
	/**
	 * 캠페인 상세 정보 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/detail")
	public Map<String, Object> getCampaignDetail(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "campaign_id");

		return demandCampaignService.getCampaignDetail(param);
	}
	
	/**
	 * 캠페인 상세 정보 (광고 제외)
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/min/detail")
	public Map<String, Object> getCampaignDetailMin(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "campaign_id");
		
		return demandCampaignService.getCampaignDetailMin(param);
	}
	
	/**
	 * 캠페인 기본 정보 등록
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/add")
	public Map<String, Object> addCampaign(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "campaign_name");
		CommonUtil.checkNullThrowException(param, "pay_type");
		
		return demandCampaignService.addCampaign(param);
	}
	
	/**
	 * 캠페인 정보 수정
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/modify")
	public Map<String, Object> modifyCampaign(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "campaign_id");
		
		return demandCampaignService.modifyCampaign(param);
	}
	
	/**
	 * 캠페인 삭제 (sub 정보 일괄 삭제)
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/remove")
	public Map<String, Object> removeCampaign(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "campaign_id_list");
		
		return demandCampaignService.removeCampaign(param);
	}
}
