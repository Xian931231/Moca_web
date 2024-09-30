package com.mocafelab.web.ad.schedule;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.vo.RequestMap;

/**
 * 광고 편성 관리 컨트롤러
 * @author lky4530
 *
 */
@RestController
@RequestMapping("${apiPrefix}/campaign/schedule")
public class ScheduleController {

	@Autowired
	private ScheduleService scheduleService;
	
	@Autowired
	private ScheduleSyncService scheduleSyncService;
	
	/**
	 * 편성표 리스트
	 * @param reqMap
	 * @return
	 */
	@PostMapping("/list")
	public Map<String, Object> getList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return scheduleService.getList(param);
	}
	
	/**
	 * 편성표 상세
	 * @param reqMap
	 * @return
	 */
	@PostMapping("/detail")
	public Map<String, Object> getDetail(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		if(CommonUtil.checkIsNull(param, "schedule_id")) {
			CommonUtil.checkNullThrowException(param, "product_id");
		}
		
		return scheduleService.getDetail(param);
	}
	
	/**
	 * 편성표 등록
	 * @param reqMap
	 * @return
	 */
	@PostMapping("/add")
	public Map<String, Object> addSchedule(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "schedule_name");
		CommonUtil.checkNullThrowException(param, "slot_list");
		CommonUtil.checkNullThrowException(param, "product_id_list");
		param.put("type", "A");
		
		return scheduleSyncService.addSyncSchedule(param);
	}
	
	/**
	 * 편성표 수정
	 * @param reqMap
	 * @return
	 */
	@PostMapping("/modify")
	public Map<String, Object> modifySchedule(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "schedule_id");
		CommonUtil.checkNullThrowException(param, "schedule_name");
		CommonUtil.checkNullThrowException(param, "slot_list");
		CommonUtil.checkNullThrowException(param, "remove_slot_id_list");
		CommonUtil.checkNullThrowException(param, "product_id_list");
		CommonUtil.checkNullThrowException(param, "remove_schdule_product_id_list");
		param.put("type", "M");
		
		return scheduleSyncService.addSyncSchedule(param);
	}
	
	/**
	 * 편성표 삭제
	 * @param reqMap
	 * @return
	 */
	@PostMapping("/remove")
	public Map<String, Object> removeShcedule(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
	
		CommonUtil.checkNullThrowException(param, "schedule_id_list");
		
		return scheduleService.removeSchedule(param);
	}
	
	/**
	 * 상품 CPP 광고 지정 순서 변경
	 * @param reqMap
	 * @return
	 */
	@PostMapping("/modify/order/detail")
	public Map<String, Object> getModifyCppOrderDetail(RequestMap reqMap) {
		Map<String, Object> param = reqMap.getMap();
	
		CommonUtil.checkNullThrowException(param, "product_id");
		
		return scheduleService.getModifyCppOrderDetail(param);
	}
	
	/**
	 * 같은 스케쥴의 광고 중 재생시간이 같은 광고
	 * @param reqMap
	 * @return
	 */
	@PostMapping("/sg/list") 
	public Map<String, Object> getSameScheduleSgList(RequestMap reqMap) {
		Map<String, Object> param = reqMap.getMap();
	
		CommonUtil.checkNullThrowException(param, "slot_id");
		CommonUtil.checkNullThrowException(param, "schedule_id");
		
		return scheduleService.getSameScheduleSgList(param);
	}
	
	/**
	 * 상품 광고 지정 순서 변경
	 * @param reqMap
	 * @return
	 */
	@PostMapping("/modify/order")
	public Map<String, Object> modifyCppOrder(RequestMap reqMap) {
		Map<String, Object> param = reqMap.getMap();
	
		CommonUtil.checkNullThrowException(param, "product_id");
		
		return scheduleService.modifyCppOrder(param);
	}
	
	/*
	* 광고 스케쥴 정보 설정 
	* 
	* 스케쥴의 전날 다음날 광고들 설정 
	* schedule_table, schedule_table_slot, schedule_table_block 테이블 설정
	* 매체사 조회
	* @param reqMap
	* @return
	* @throws Exception
	*/
	@PostMapping("/setting")
	public Map<String, Object> setSchedule(RequestMap reqMap) throws Exception {
 		Map<String, Object> param = reqMap.getMap();
		return scheduleService.setSchedule(param);
	}
	
	/**
	 * 편성표에 등록 가능한 상품 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/product/remain/list")
	public Map<String, Object> getRemainProductList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return scheduleService.getRemainProductList(param);
	}
	
	/**
	 * 편성표 상품에 속한 광고 리스트
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/product/sg/list")
	public Map<String, Object> getScheduleProductSgList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "product_id");
		
		return scheduleService.getScheduleProductSgList(param);
	}
}
