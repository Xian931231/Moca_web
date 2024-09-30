package com.mocafelab.web.device;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.vo.RequestMap;

@RestController
@RequestMapping("${apiPrefix}/device")
public class DeviceController {

	@Autowired
	private DeviceService deviceService;
	
	/**
	 * 디바이스 리스트
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/list")
	public Map<String, Object> getList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return deviceService.getList(param);
	}
	
	/**
	 * 디바이스 별 현황 리스트
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/withMotorList")
	public Map<String, Object> getDeviceWithMotorList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return deviceService.getDeviceWithMotorList(param);
	}
	
	/**
	 * 디바이스 상태 변경
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/modify/status")
	public Map<String, Object> modifyDeviceStatus(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "ssp_device_id");
		CommonUtil.checkNullThrowException(param, "status");
		CommonUtil.checkNullThrowException(param, "notes");
		
		return deviceService.modifyDeviceStatus(param);
	}
	
}
