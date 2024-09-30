package com.mocafelab.web.ad.sg;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.vo.RequestMap;

/**
 * 광고 전략 (광고주)
 * @author mure96
 *
 */
@RestController
@RequestMapping("${apiPrefix}/sg/demand")
public class DemandSgController {
	/*
	@PostMapping("/")
	public Map<String, Object> (RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		return .(param);
	}
	*/
	
	@Autowired
	private DemandSgService demandSgService;
	
	
	// 광고주 (demand)
	@PostMapping("/list")
	public Map<String, Object> sgList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return demandSgService.getSgList(param);
	}
	
	/**
	 * 캠페인내 광고 상태 변경
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/modifyStatus")
	public Map<String, Object> demandModifyStatus(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "sg_id_list");
		CommonUtil.checkNullThrowException(param, "status");
		
		return demandSgService.demandModifyStatus(param);
	}
	
	/**
	 * 광고(정책) 정보 등록
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/addInfo")
	public Map<String, Object> addInfo(RequestMap reqMap, MultipartHttpServletRequest mRequest) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "campaign_id");
		CommonUtil.checkNullThrowException(param, "name");
		
		return demandSgService.addInfo(param, mRequest);
	}
	
	/**
	 * 광고(정책) 기본 정보 등록
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/addSgBasic")
	public Map<String, Object> addSgBasic(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "campaign_id");
		CommonUtil.checkNullThrowException(param, "name");
		
		return demandSgService.addSgBasic(param);
	}

	/**
	 * 광고 노출 지역 등록
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/addSgArea")
	public Map<String, Object> addSgArea(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "sg_id");
		CommonUtil.checkNullThrowException(param, "area_id");
		
		return demandSgService.addSgArea(param);
	}
	
	/**
	 * 광고 소재 등록
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/addSgMaterial")
	public Map<String, Object> addSgMaterial(RequestMap reqMap, MultipartHttpServletRequest mRequest) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "sg_id");
		
		return demandSgService.addSgMaterial(param, mRequest);
	}
	
	/**
	 * 광고 스케줄 등록
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/addSgSchedule")
	public Map<String, Object> addSgSchedule(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "sg_id");
		CommonUtil.checkNullThrowException(param, "schedule_list");
		
		return demandSgService.addSgSchedule(param);
	}
	
	/**
	 * 광고 상세 조회 CPM, CPP
	 * @param requestMap
	 * @return
	 */
	@PostMapping("/detail")
	public Map<String, Object> getSgDetail(RequestMap requestMap) {
		Map<String, Object> param = requestMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "sg_id");
		
		return demandSgService.getSgDetail(param);
	}
	
	/**
	 * 광고 수정
	 * @param mRequest
	 * @param requestMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/modify/info")
	public Map<String, Object> modifySgManager(MultipartHttpServletRequest mRequest, RequestMap requestMap) throws Exception {
		Map<String, Object> param = requestMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "sg_id");
		
		return demandSgService.modifySgManager(mRequest, param);
	}
	
	/**
	 * 광고 정보 수정 기본
	 * @param requestMap
	 * @return
	 * @throws Exception 
	 */
	@PostMapping("/modify/basic")
	public Map<String, Object> modifySgBasic(RequestMap requestMap) throws Exception {
		Map<String, Object> param = requestMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "sg_id");
		
		return demandSgService.modifySgBasic(param);
	}
	
	/**
	 * 광고 노출 스케쥴 수정
	 * @param requestMap
	 * @return
	 * @throws JsonProcessingException 
	 * @throws JsonMappingException 
	 */
	@PostMapping("/modify/week")
	public Map<String, Object> modifySgWeek(RequestMap requestMap) {
		Map<String, Object> param = requestMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "sg_id");
		CommonUtil.checkNullThrowException(param, "schedule_list");
		
		return demandSgService.modifySgWeek(param);
	}
	
	/**
	 * 광고 노출 지역 수정
	 * @param requestMap
	 * @return
	 */
	@PostMapping("/modify/area")
	public Map<String, Object> modifySgArea(RequestMap requestMap) {
		Map<String, Object> param = requestMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "sg_id");
		
		return demandSgService.modifySgArea(param);
	}
	
	/**
	 * 광고 소재 수정
	 * @param mRequest
	 * @param requestMap
	 * @return
	 * @throws Exception 
	 */
	@PostMapping("/modify/material")
	public Map<String, Object> modifySgMaterial(MultipartHttpServletRequest mRequest, RequestMap requestMap) throws Exception {
		Map<String, Object> param = requestMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "sg_id");
		
		return demandSgService.modifySgMaterial(mRequest, param);
	}
	
	/**
	 * 캠페인내 광고 삭제
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/remove")
	public Map<String, Object> removeDemand(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "sg_id_list");
		
		return demandSgService.removeDemand(param);
	}
	
	/**
	 * CPP 단가 계산
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/calculate/cpp")
	public Map<String, Object> calculateCPP(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return demandSgService.calculateCPP(param);
	}
	
	/**
	 * CPM 금액 계산
	 * @param reqMap
	 * @return
	 */
	@PostMapping("/calculate/cpm")
	public Map<String, Object> calculateCPM(RequestMap reqMap) {
		Map<String, Object> param = reqMap.getMap();
		
		return demandSgService.calculateCPM(param);
	}
	
	/**
	 * 광고 집행금액용 캠페인 및 광고 목록
	 * @param requestMap
	 * @return
	 */
	@PostMapping("/member/amount/list")
	public Map<String, Object> getDemandSgList(RequestMap requestMap) {
		Map<String, Object> param = requestMap.getMap();
		
		return demandSgService.getDemandSgList(param);
	}
	
	/**
	 * (지역)구 목록
	 * @param requestMap
	 * @return
	 */
	@PostMapping("/areaGu/list")
	public Map<String, Object> getAreaGuList(RequestMap requestMap) {
		Map<String, Object> param = requestMap.getMap();
		
		return demandSgService.getAreaGuList(param);
	}
	
	@PostMapping("/material/detail")
	public Map<String, Object> getMaterialDetail(RequestMap requestMap) {
		Map<String, Object> param = requestMap.getMap();
		
		return demandSgService.getMaterialDetail(param);
	}
	
	// -- 광고주
}