package com.mocafelab.web.balance;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.vo.RequestMap;

/**
 * 매체관리 > 매체 정산 관리
 * @author mure96
 *
 */
@RestController
@RequestMapping("${apiPrefix}/balance")
public class BalanceController {
	/*
	@PostMapping("/")
	public Map<String, Object> (RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		return .(param);
	}
	*/
	
	@Autowired
	private BalanceService balanceService;
	
	/**
	 * 목록 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/list")
	public Map<String, Object> getList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "search_start_date");
		CommonUtil.checkNullThrowException(param, "search_end_date");
		
		return balanceService.getList(param);
	}
	
	/**
	 * 정산금 지급
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/pay/calculate")
	public Map<String, Object> payCalculate(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "balance_info_id");
		
		return balanceService.payCalculate(param);
	}
}
