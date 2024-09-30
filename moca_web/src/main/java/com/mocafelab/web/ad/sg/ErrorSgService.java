package com.mocafelab.web.ad.sg;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mocafelab.web.vo.BeanFactory;
import com.mocafelab.web.vo.ResponseMap;

@Service
@Transactional(rollbackFor = Exception.class)
public class ErrorSgService {

	@Autowired
	private BeanFactory beanFactory;
	
	@Autowired
	private ErrorSgMapper errorSgMapper;
	
	/**
	 * 광고 관리 > 오류 광고 목록 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getListForSgManage(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> getList = errorSgMapper.getListForSgManage(param);
		
		int listCnt = errorSgMapper.getListCntForSgManage(param);
		
		respMap.setBody("list", getList);
		respMap.setBody("tot_cnt", listCnt);
		
		return respMap.getResponse();
	}

	/**
	 * 광고 관리 > 오류 광고 상태 변경
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> modifyStatusForSgManage(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		if(errorSgMapper.modifyLogStatus(param) <= 0) {
			throw new RuntimeException();
		}
		String processKind = (String)param.get("process_kind");
		
		// 정상 처리일 경우 배치 모니터링 last_index 업데이트
		if(processKind.equals("P")) {
			Map<String, Object> eventDate = errorSgMapper.getAdEventLogEventDate(param);
			
			errorSgMapper.modifyBatchMonitorLastIndex(eventDate);
		}
		return respMap.getResponse();
	}
	
	/**
	 * 매체 관리 > 오류 광고 목록 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getListForSspManage(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> getList = errorSgMapper.getListForSspManage(param);
		
		int listCnt = errorSgMapper.getListCntForSspManage(param);
		
		respMap.setBody("list", getList);
		respMap.setBody("tot_cnt", listCnt);
		
		return respMap.getResponse();
	}
}