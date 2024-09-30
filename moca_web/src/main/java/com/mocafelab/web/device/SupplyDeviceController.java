package com.mocafelab.web.device;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.newfrom.lib.vo.RequestMap;

@RestController
@RequestMapping("${apiPrefix}/supply/device")
public class SupplyDeviceController {

	@Autowired
	private SupplyDeviceService supplyDeviceService;
	
	/**
	 * 매체현황 > 장비 오류 현황 리스트
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/error/list")
	public Map<String, Object> getDeviceErrorList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return supplyDeviceService.getDeviceErrorList(param);
	}
	
	/**
	 * 상품관리 > 디바이스 관리
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/list")
	public Map<String, Object> getDeviceList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return supplyDeviceService.getDeviceList(param);
	}
	
}
