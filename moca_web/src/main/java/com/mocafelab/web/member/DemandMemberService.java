package com.mocafelab.web.member;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mocafelab.web.enums.ModifyHistory;
import com.mocafelab.web.vo.BeanFactory;
import com.mocafelab.web.vo.ResponseMap;

/**
 * 
 * @author just3377
 *
 */
@Service
public class DemandMemberService {
	
	@Autowired
	private BeanFactory beanFactory;
	
	@Autowired
	private DemandMemberMapper demandMemberMapper;
	
	
	/**
	 * 활동이력 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> list = demandMemberMapper.getList(param);
		
		int listCnt = demandMemberMapper.getListCnt(param);
		
		respMap.setBody("list",list);
		respMap.setBody("tot_cnt", listCnt);
		
		return respMap.getResponse();
	}
	
	/**
	 * 활동이력 추가
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public void addDspModHistory(Map<String, Object> param, ModifyHistory enums) throws Exception {
		
		String type = enums.getType();
		String kind = enums.getKind();
		
		Map<String, Object> historyParam = new HashMap<>();
		
		historyParam.put("kind", kind);
		historyParam.put("status", type);
		historyParam.put("update_member_id", param.get("login_id"));
		historyParam.put("ip_addr", param.get("remote_ip"));
		
		
		if(kind.equals("MHK001")){
			historyParam.put("modify_id", param.get("sg_id"));
			historyParam.put("message", param.get("name"));
		}else if(kind.equals("MHK002")){
			historyParam.put("modify_id", param.get("campaign_id"));
			historyParam.put("message", param.get("campaign_name"));
		}else if(kind.equals("MHK003")) {
			historyParam.put("modify_id", param.get("login_id"));
			historyParam.put("message", param.get("message"));
		}
		
		if(demandMemberMapper.addDspModHistory(historyParam) <= 0) {
			throw new RuntimeException();
		}
	}
	
	/**
	 * 매체 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getSupplyList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> supplayList = demandMemberMapper.getSupplyList(param);
		respMap.setBody("list", supplayList);
		
		return respMap.getResponse();
	}
}
