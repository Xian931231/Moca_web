package com.mocafelab.web.common;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.vo.RequestMap;

/**
 * 공통 controller
 * @author mure96
 *
 */
@RequestMapping("${apiPrefix}/common")
@RestController
public class CommonController {
	
	@Autowired
	private CommonService commonService;
	
	// code 
	
	/**
	 * 코드 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/code/list")
	public Map<String, Object> getCodeList (RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return commonService.getCodeList(param);
	}
	
	/**
	 * 코드 상세 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@Deprecated
//	@PostMapping("/code/detail")
	public Map<String, Object> getCodeDetail (RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "code");
		CommonUtil.checkNullThrowException(param, "parent_code");
		
		return commonService.getCodeDetail(param);
	}
	
	/**
	 * 코드 등록 
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/code/add")
	public Map<String, Object> addCode (RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "code");
		CommonUtil.checkNullThrowException(param, "code_name");
		CommonUtil.checkNullThrowException(param, "step");
		CommonUtil.checkNullThrowException(param, "parent_code");
		CommonUtil.checkNullThrowException(param, "description");
		CommonUtil.checkNullThrowException(param, "sort");
		
		return commonService.addCode(param);
	}
	
	/**
	 * 코드 수정 
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/code/modify")
	public Map<String, Object> modifyCode (RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "code");
		CommonUtil.checkNullThrowException(param, "code_name");
		CommonUtil.checkNullThrowException(param, "step");
		CommonUtil.checkNullThrowException(param, "parent_code");
		CommonUtil.checkNullThrowException(param, "description");
		CommonUtil.checkNullThrowException(param, "sort");
		
		return commonService.modifyCode(param);
	}
	
	/**
	 * 코드 삭제
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/code/remove")
	public Map<String, Object> removeCode (RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "code");
		CommonUtil.checkNullThrowException(param, "parent_code");
		
		return commonService.removeCode(param);
	}
	
	/**
	 * 광고 카테고리 목록 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/code/sg/list")
	public Map<String, Object> getSgCodeList (RequestMap requestMap) {
		Map<String, Object> param = requestMap.getMap();
		
		return commonService.getSgCodeList(param);
	}
	
	/**
	 * 광고 카테고리 목록 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/code/device/list")
	public Map<String, Object> getDeviceCodeList (RequestMap requestMap) {
		Map<String, Object> param = requestMap.getMap();
		
		return commonService.getDeviceCodeList(param);
	}
	
	// -- code 
	
	/**
	 * api key 발급 
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/add/package")
	public Map<String, Object> addPackageId(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "product_id");
		CommonUtil.checkNullThrowException(param, "package_id");
		
		return commonService.addPackageId(param);
	}
	
	/**
	 * api key 발급 
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/apiKey")
	public Map<String, Object> getApiKey(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "product_id");
		
		return commonService.getApiKey(param);
	}
	
	// week_manager 
	/**
	 * week_manager 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/weektime/list")
	public Map<String, Object> getWeekTime(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		return commonService.getWeekTime(param);
	}
	
	/**
	 * week_manager 수정
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/weektime/modify")
	public Map<String, Object> modifyWeekTime(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "weektime_json");
		
		return commonService.modifyWeekTime(param);
	}
	
	// -- week_manager
	
	// area_code 
	/**
	 * 위치 정보 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@PostMapping("map/getLocation")
	public Map<String, Object> getLocation(RequestMap reqMap) throws Exception{
		Map<String, Object> param = reqMap.getMap();
		
		return commonService.getLocation(param);
	}
	
	@PostMapping("/areacode/list")
	public Map<String, Object> getAreaCodeList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return commonService.getAreaCodeList(param);
	}
	// -- area_code
}
