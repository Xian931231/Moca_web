package com.mocafelab.web.balance;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.vo.RequestMap;

/**
 * 매체사 - 정산관리 
 * @author mure96
 *
 */
@RestController
@RequestMapping("${apiPrefix}/balance/supply")
public class SupplyBalanceController {

	@Autowired
	private SupplyBalanceService supplyBalanceService;
	
	/**
	 * 정산관리 목록
	 * @param reqMap
	 * @return
	 */
	@PostMapping("/list")
	public Map<String, Object> getList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "search_start_date");
		CommonUtil.checkNullThrowException(param, "search_end_date");
		
		return supplyBalanceService.getList(param);
	}
	
	/**
	 * 정산 회원 정보 조회
	 * @param reqMap
	 * @return
	 */
	@PostMapping("/info")
	public Map<String, Object> getInfo(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return supplyBalanceService.getInfo(param);
	}
	
	
}
