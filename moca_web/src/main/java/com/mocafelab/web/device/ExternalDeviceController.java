package com.mocafelab.web.device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.vo.RequestMap;

@RestController
@RequestMapping("${apiPrefix}/external")
public class ExternalDeviceController {

	@Autowired
	private ExternalDeviceService externalDeviceService;
	
	/**
	 * 상품에 등록된 디바이스 목록 
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/device/list")
	public Map<String, Object> getList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "product_id");
		
		return externalDeviceService.getList(param);
	}
	
	/**
	 * 상품에 디바이스 등록/수정/삭제 
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/device/add")
	public Map<String, Object> addDevice(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "product_id");
		CommonUtil.checkNullThrowException(param, "add_device_list");
		CommonUtil.checkNullThrowException(param, "remove_device_id_list");
		
		return externalDeviceService.addDevice(param);
	}
	
	/**
	 * 측정 장비 목록
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/sensor/list")
	public Map<String, Object> getSensorList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "category_id");
		
		return externalDeviceService.getSensorList(param); 
	}
	
	/**
	 * 측정 장비 상세
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/sensor/detail")
	public Map<String, Object> getSensorDetail(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "sensor_id");
		
		return externalDeviceService.getSensorDetail(param);
	}
	
	/**
	 * 측정 장비명 등록/수정 
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/sensor/add")
	public Map<String, Object> addSensor(RequestMap reqMap, MultipartHttpServletRequest mRequest) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "category_id");
		
		return externalDeviceService.addSensor(param, mRequest);
	}
	
	/**
	 * 측정 장비명 수정 
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/sensor/modify")
	public Map<String, Object> modifySensor(RequestMap reqMap, MultipartHttpServletRequest mRequest) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "sensor_id");
		
		return externalDeviceService.modifySensor(param, mRequest);
	}
	
	/**
	 * 측정 장비 디바이스 번호 목록
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/sensor/device/list")
	public Map<String, Object> getSensorDeviceList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "sensor_id");
		
		return externalDeviceService.getSensorDeviceList(param);
	}
	
	/**
	 * 측정 장비 디바이스 번호 등록/수정/삭제
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/sensor/device/add")
	public Map<String, Object> addSensorDevice(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "sensor_id");
		CommonUtil.checkNullThrowException(param, "add_sensor_device_list");
		CommonUtil.checkNullThrowException(param, "remove_sensor_device_id_list");
		
		return externalDeviceService.addSensorDevice(param);
	}
	
	/**
	 * 게재 위치 목록
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/motor/list")
	public Map<String, Object> getMotorPositionList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "category_id");
		
		return externalDeviceService.getMotorPositionList(param);
	}
	
	/**
	 * 게재 위치 상세
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/motor/detail")
	public Map<String, Object> getMotorPositionDetail(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "motor_position_id");
		
		return externalDeviceService.getMotorPositionDetail(param);
	}
	
	/**
	 * 게재 위치 등록
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/motor/add")
	public Map<String, Object> addMotorPosition(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "position_name");
		CommonUtil.checkNullThrowException(param, "category_id");
		
		return externalDeviceService.addMotorPosition(param);
	}
	
	/**
	 * 게재 위치 구분 아이디 목록
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/motor/id/list")
	public Map<String, Object> getMotorPositionIdList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "motor_position_id");
		
		return externalDeviceService.getMotorPositionIdList(param);
	}
	
	/**
	 * 게재 위치 구분 아이디 등록
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/motor/id/add")
	public Map<String, Object> addMotorPositionId(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "motor_position_id");
		CommonUtil.checkNullThrowException(param, "add_motor_id_list");
		CommonUtil.checkNullThrowException(param, "remove_motor_id_list");
		
		return externalDeviceService.addMotorPositionId(param);
	}
	
	/**
	 * 기기/위치 매칭 상세 정보 
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/matching/detail")
	public Map<String, Object> getMatchingDetail(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "motor_id");
		
		return externalDeviceService.getMatchingDetail(param);
	}
	
	/**
	 * 기기/위치 매칭 검수 
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/matching/status/modify")
	public Map<String, Object> modifyMotorStatus(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "motor_id");
		CommonUtil.checkNullThrowException(param, "status");
		
		return externalDeviceService.modifyMotorStatus(param);
	}
	
	/**
	 * 기기/위치 매칭 저장 
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/matching/add")
	public Map<String, Object> addMatching(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "motor_id");
		CommonUtil.checkNullThrowException(param, "add_device_id_list");
		CommonUtil.checkNullThrowException(param, "remove_device_id_list");
		CommonUtil.checkNullThrowException(param, "add_sensor_device_id_list");
		CommonUtil.checkNullThrowException(param, "remove_sensor_device_id_list");
		
		return externalDeviceService.addMatching(param);
	}
}
