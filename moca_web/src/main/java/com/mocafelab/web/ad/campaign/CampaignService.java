package com.mocafelab.web.ad.campaign;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mocafelab.web.vo.BeanFactory;
import com.mocafelab.web.vo.ResponseMap;

@Service
public class CampaignService {

	@Autowired
	private BeanFactory beanFactory;
	
	@Autowired
	private CampaignMapper campaignMapper;
	
	/**
	 * 운영자용 캠페인 리스트
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getCampaignList(Map<String, Object> param) throws Exception{
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> campaignList = campaignMapper.getCampaignList(param);
		
		int listCnt = campaignMapper.getCampaignCnt(param);
		
		respMap.setBody("list", campaignList);
		respMap.setBody("tot_cnt", listCnt);
		
		return respMap.getResponse();
	}
	
	/**
	 * 운영자용 캠페인 상세 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getCampaignDetail(Map<String, Object> param) throws Exception{
		ResponseMap respMap = beanFactory.getResponseMap();
		
		Map<String, Object> campaigmDetail = campaignMapper.getCampaignDetail(param);
		List<Map<String, Object>> sgList = campaignMapper.getSgList(param);
		
		campaigmDetail.put("sg_list", sgList);
		
		respMap.setBody("data", campaigmDetail);
		
		return respMap.getResponse();
	}
	
}
