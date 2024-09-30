package com.mocafelab.web.board.notice;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mocafelab.web.vo.BeanFactory;
import com.mocafelab.web.vo.Code;
import com.mocafelab.web.vo.ResponseMap;

import lombok.extern.slf4j.Slf4j;
import net.newfrom.lib.util.CommonUtil;

@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class BoardNoticeService {
	@Autowired
	private BeanFactory beanFactory;
	
	@Autowired
	private BoardNoticeMapper boardNoticeMapper;
	
	/**
	 * 목록 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		respMap.setBody("list", boardNoticeMapper.getList(param));
		respMap.setBody("tot_cnt", boardNoticeMapper.getListCnt(param));
		
		return respMap.getResponse();
	}
	
	/**
	 * 상세 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getDetail(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		Map<String, Object> data = boardNoticeMapper.getDetail(param);
		if(CommonUtil.checkIsNull(data)) {
			return respMap.getResponse(Code.NOT_EXIST_DATA);
		} 
		
		respMap.setBody("data", data);
		return respMap.getResponse();
	}
}
