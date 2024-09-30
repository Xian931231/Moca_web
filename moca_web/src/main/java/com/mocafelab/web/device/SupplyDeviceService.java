package com.mocafelab.web.device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mocafelab.web.vo.BeanFactory;
import com.mocafelab.web.vo.ResponseMap;

@Service
@SuppressWarnings("unchecked")
public class SupplyDeviceService {

	@Autowired
	private BeanFactory beanFactory;
	
	@Autowired
	private SupplyDeviceMapper supplyDeviceMapper;
	
	/**
	 * 매체현황 > 장비 오류 현황 리스트
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getDeviceErrorList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> deviceErrorList = supplyDeviceMapper.getDeviceErrorList(param);
		respMap.setBody("list", deviceErrorList);
		
		int total = supplyDeviceMapper.getDeviceErrorCnt(param);
		respMap.setBody("tot_cnt", total);
		
		return respMap.getResponse();
	}
	
	/**
	 * 상품관리 > 디바이스 관리
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getDeviceList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();

		// 차량 리스트
		List<Map<String, Object>> motorList = new ArrayList<>();
		
		// 디바이스 리스트
		List<Map<String, Object>> deviceList = supplyDeviceMapper.getDeviceList(param);
		
		for(Map<String, Object> device : deviceList) {
			
			// motorList 사이즈가 0일 경우
			if(motorList.stream().filter(m -> m.get("motor_id").equals(device.get("motor_id"))).count() <= 0) {
				Map<String, Object> devieMap = new HashMap<>();
				List<Map<String, Object>> deviceGroup = new ArrayList<>();
				
				devieMap.put("motor_id", device.get("motor_id"));
				
				deviceGroup.add(device);
				devieMap.put("device_list", deviceGroup);
				
				motorList.add(devieMap);
			} else {
				List<Map<String, Object>> deviceGroup = motorList.stream().filter(m -> m.get("motor_id").equals(device.get("motor_id"))).collect(Collectors.toList());
				List<Map<String, Object>> list = (List<Map<String, Object>>) deviceGroup.get(0).get("device_list");
				list.add(device);
			}
		}
		
		respMap.setBody("list", motorList);
		
		int total = supplyDeviceMapper.getDeviceCnt(param);
		respMap.setBody("tot_cnt", total);
		
		return respMap.getResponse();
	}
}
