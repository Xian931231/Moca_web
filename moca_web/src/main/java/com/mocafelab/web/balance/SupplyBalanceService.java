package com.mocafelab.web.balance;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mocafelab.web.vo.BeanFactory;
import com.mocafelab.web.vo.Code;
import com.mocafelab.web.vo.ResponseMap;

import net.newfrom.lib.util.CommonUtil;

@Service
@Transactional(rollbackFor = Exception.class)
public class SupplyBalanceService {
	
	@Autowired
	private SupplyBalanceMapper supplyBalanceMapper;
	
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
		
		// 리스트 
		List<Map<String, Object>> list = supplyBalanceMapper.getList(param);
		
		// 리스트 총 개수
		int totCnt = supplyBalanceMapper.getListCnt(param);
		
		respMap.setBody("list", list);
		respMap.setBody("tot_cnt", totCnt);
		
		return respMap.getResponse();
	}
	
	/**
	 * 정산 회원 정보 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getInfo(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		Map<String, Object> data = supplyBalanceMapper.getInfo(param);
		if(CommonUtil.checkIsNull(data)) {
			return respMap.getResponse(Code.NOT_EXIST_DATA);
		}
		
		respMap.setBody("data", data);
		
		return respMap.getResponse();
	}
}
