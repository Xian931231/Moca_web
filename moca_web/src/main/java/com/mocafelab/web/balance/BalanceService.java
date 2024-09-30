package com.mocafelab.web.balance;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mocafelab.web.vo.BeanFactory;
import com.mocafelab.web.vo.ResponseMap;

@Service
@Transactional(rollbackFor = Exception.class)
public class BalanceService {
	
	@Autowired
	private BalanceMapper balanceMapper;
	
	@Autowired
	private BeanFactory beanFactory;

	/**
	 * 정산관리 목록
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		respMap.setBody("list", balanceMapper.getList(param));
		respMap.setBody("tot_cnt", balanceMapper.getListCnt(param));
		
		return respMap.getResponse();
	}
	
	/**
	 * 정산금 지급
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> payCalculate(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		if(balanceMapper.payCalculate(param) < 0) {
			throw new RuntimeException();
		}
		
		return respMap.getResponse();
	}
}
