package com.mocafelab.web.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mocafelab.web.vo.BeanFactory;
import com.mocafelab.web.vo.ResponseMap;

import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.util.SHAUtil;
/**
 * 공통 서비스 
 * @author mure96
 *
 */
@Service
public class CommonService {
	
	@Autowired
	private BeanFactory beanFactory;

	@Autowired
	private CommonMapper commonMapper;
	
	// code
	/**
	 * 코드 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getCodeList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		respMap.setBody("list", commonMapper.getCodeList(param));
		respMap.setBody("tot_cnt", commonMapper.getCodeListCnt(param));
		
		return respMap.getResponse();
	}
	
	/**
	 * 코드 상세 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@Deprecated
	public Map<String, Object> getCodeDetail(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		respMap.setBody("data", commonMapper.getCodeDetail(param));
		
		return respMap.getResponse();
	}
	
	/**
	 * 코드 등록
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> addCode(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		if(commonMapper.hasCode(param) > 0) {
			return respMap.getErrResponse();
		}
		
		if(commonMapper.addCode(param) < 0) {
			return respMap.getErrResponse();
		}
		
		return respMap.getResponse();
	}
	
	/**
	 * 코드 수정
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> modifyCode(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		if(commonMapper.modifyCode(param) < 0) {
			return respMap.getErrResponse();
		}
		
		return respMap.getResponse();
	}
	
	/**
	 * 코드 삭제
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> removeCode(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		if(commonMapper.removeCode(param) < 0) {
			return respMap.getErrResponse();
		}
		
		return respMap.getResponse();
	}
	
	/**
	 * 광고 카테고리 목록 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getSgCodeList(Map<String, Object> param) {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		respMap.setBody("list", commonMapper.getSgCodeList(param));
		
		return respMap.getResponse();
	}
	
	/**
	 * 디바이스 OS 목록 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getDeviceCodeList(Map<String, Object> param) {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		respMap.setBody("list", commonMapper.getDeviceCodeList(param));
		
		return respMap.getResponse();
	}
	
	// -- code 

	// product
	
	/**
	 * 상품 package_id 등록
	 * @param param
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	public Map<String, Object> addPackageId(Map<String, Object> param) {
		ResponseMap responseMap = beanFactory.getResponseMap(); 
		
		Map<String, Object> getProductDetail = commonMapper.getProductDetail(param);
		if (CommonUtil.checkIsNull(getProductDetail)) {
			throw new RuntimeException();
		}
		
		if (commonMapper.addPackageId(param) < 1) {
			throw new RuntimeException();	
		}
		
		return responseMap.getResponse();
	}
	
	/**
	 * 상품 api key 발급
	 * @param param
	 * @return
	 * @throws Exception 
	 */
	@Transactional(rollbackFor = Exception.class)
	public Map<String, Object> getApiKey(Map<String, Object> param) throws Exception {
		ResponseMap responseMap = beanFactory.getResponseMap();
		
		Map<String, Object> getProductDetail = commonMapper.getProductDetail(param);
		if (CommonUtil.checkIsNull(getProductDetail)) {
			throw new RuntimeException();
		}
		
		if (!CommonUtil.checkIsNull(getProductDetail, "api_key")) {
			throw new RuntimeException();
		}
		
		String os = (String) getProductDetail.get("os");
		
		if (!os.contains("A") || os.contains("W")) {
			throw new RuntimeException();
		}
		
		int productId = Integer.parseInt(String.valueOf(param.get("product_id")));
		
		String packageName = (String) getProductDetail.get("package_id");
		
		String apiKey = UUID.randomUUID().toString() + productId + os + packageName;
		String encryptKey = SHAUtil.encrypt(apiKey, "SHA-256");
		
		param.put("api_key", encryptKey);
		
		if (commonMapper.addApiKey(param) < 1) {
			throw new RuntimeException();
		};
		
		responseMap.setBody("api_key", encryptKey);
		
		return responseMap.getResponse();
	}
	
	// product
	
	//map
	
	/**
	 * 지역 정보 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getLocation(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		//지역정보 조회
		Map<String, Object> locationInfo = commonMapper.getLocation(param);
		
		respMap.setBody("location_info", locationInfo);
		
		return respMap.getResponse();
	}
	
	// week_manager
	/**
	 * week_manager 데이터 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getWeekTime(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		respMap.setBody("list", commonMapper.getWeekTime(param));
		
		return respMap.getResponse();
	}
	
	/**
	 * week_manager 수정
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> modifyWeekTime(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		/**
		 * weektime_json =
		 * [
		 * 	{
		 * 		week_code: 0, (0: 일, ... 6: 토)
		 * 		hour_00: 0, (0: 미사용, 1: 사용)
		 * 		hour_01: 1,
		 * 		...
		 * 		hour_22: 0,
		 * 		hour_23: 1
		 * 	},
		 * 	...
		 * 	{
		 * 		week_code: 6,
		 * 		...
		 * 	}
		 * ]
		 */
		List<Map<String, Object>> weekTimeList = (List<Map<String, Object>>) param.get("weektime_json");
		for(Map<String, Object> weekTime : weekTimeList) {
			// 수정
			if(commonMapper.modifyWeekTime(weekTime) < 0) {
				throw new RuntimeException();
			}
		}
		
		return respMap.getResponse();
	}
	// -- week_manager
	
	// area_code
	/**
	 * area_code 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getAreaCodeList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> list = new ArrayList<>();
		
		if(CommonUtil.checkIsNull(param, "si_code") && CommonUtil.checkIsNull(param, "gu_code")) {
			list = commonMapper.getAreaCodeBySi(param);
		} else if(CommonUtil.checkIsNull(param, "gu_code")) {
			list = commonMapper.getAreaCodeByGu(param);
		} else {
			list = commonMapper.getAreaCodeByDong(param);
		}
		
		respMap.setBody("list", list);
		
		return respMap.getResponse();
	}
	// -- area_code

}
