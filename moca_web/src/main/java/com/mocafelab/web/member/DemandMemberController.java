package com.mocafelab.web.member;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.newfrom.lib.vo.RequestMap;

/**
 * 
 * @author just3377
 *
 */
@RestController
@RequestMapping("${apiPrefix}/member/demand")
public class DemandMemberController {

	@Autowired
	private DemandMemberService demandMemberSerivce;
	
	/**
	 * 활동 이력 리스트 
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/modify/history/list")
	public Map<String, Object> getList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return demandMemberSerivce.getList(param);
	}
	
	/**
	 * 매체 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/supply/list")
	public Map<String, Object> getSupplyList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return demandMemberSerivce.getSupplyList(param);
	}
	
}
