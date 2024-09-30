package com.mocafelab.web.ad.campaign;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.newfrom.lib.vo.RequestMap;


/**
 * 광고 캠페인
 * @author mure96
 *
 */
@RestController
@RequestMapping("${apiPrefix}/campaign")
public class CampaignController {
	
	@Autowired
	private CampaignService campaignService;
	
	/*
	@PostMapping("/")
	public Map<String, Object> (RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		return .(param);
	}
	*/
	
	
	// 관리자
	
	//캠페인 리스트
	@PostMapping("/list")
	public Map<String, Object> getCampaignList(RequestMap reqMap)throws Exception{
		Map<String, Object> param = reqMap.getMap();
		
		return campaignService.getCampaignList(param);
	}
	
	//캠페인 상세
	@PostMapping("/detail")
	public Map<String, Object> getCampaignDetail(RequestMap reqMap)throws Exception{
		Map<String, Object> param = reqMap.getMap();
		
		return campaignService.getCampaignDetail(param);
	}

}
