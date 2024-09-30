package com.mocafelab.web.device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mocafelab.web.alarm.AlarmData;
import com.mocafelab.web.alarm.AlarmSender;
import com.mocafelab.web.email.template.DeviceErrorNoticeTemplate;
import com.mocafelab.web.vo.BeanFactory;
import com.mocafelab.web.vo.ResponseMap;

@Service
public class DeviceService {

	@Autowired
	private BeanFactory beanFactory;
	
	@Autowired
	private DeviceMapper deviceMapper;
	
	@Autowired
	private AlarmSender alarmSender;
	
	/**
	 * 디바이스 목록
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> deviceList = deviceMapper.getList(param);
		
		int deviceListCnt = deviceMapper.getListCnt(param);
		
		respMap.setBody("list", deviceList);
		respMap.setBody("tot_cnt", deviceListCnt);
		
		return respMap.getResponse();
	}
	
	/**
	 * 디바이스 별 현황 리스트
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getDeviceWithMotorList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();

		// 매체사 리스트
		List<Map<String, Object>> supplyList = deviceMapper.getSupplyList(param);
		
		// 매체사 리스트 총수
		int supplyListCnt = deviceMapper.getSupplyCnt(param);
		
		// 디바이스 리스트
		List<Map<String, Object>> deviceList = deviceMapper.getDeviceWithMotorList(param);
		
		// 매체사별 디바이스 리스트 맵핑
		for(Map<String, Object> supply : supplyList) {
			long memberId = (long)supply.get("member_id");
			
			Map<String, List<Map<String, Object>>> motorGroupList = deviceList.stream()
				.filter(device -> {
					long deviceMemberId = (long)device.get("member_id");

					return memberId == deviceMemberId;
				}).collect(Collectors.groupingBy(device -> (String)device.get("car_number"), TreeMap::new, Collectors.toList()));
			
			List<Map<String, Object>> motorList = new ArrayList<>();
			
			motorGroupList.forEach((k, v) -> {
				Map<String, Object> group = new HashMap<>();
				group.put("car_number", k.equals("zz") ? "-" : k);
				group.put("device_list", v);
				
				motorList.add(group);
			});
			supply.put("motor_list", motorList);
		}
		respMap.setBody("list", supplyList);
		respMap.setBody("tot_cnt", supplyListCnt);
		
		return respMap.getResponse();
	}
	
	/**
	 * 디바이스 상태 변경
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> modifyDeviceStatus(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 관리자, 매체사 확인
		if(!param.get("login_utype").equals("A") && !param.get("login_utype").equals("S")) {
			throw new RuntimeException();
		}
		
		if(deviceMapper.modifyDeviceStatus(param) <= 0) {
			throw new RuntimeException();
		}
		
		String status = (String) param.get("status");
		if(status.equals("R") || status.equals("D")) {
			Map<String, Object> deviceDetail = deviceMapper.getDeviceDetail(param);
			
			// 이메일 발송
			DeviceErrorNoticeTemplate template = new DeviceErrorNoticeTemplate();
			template.setName((String) deviceDetail.get("uname"));
			template.setDeviceName((String) deviceDetail.get("model_name"));
			template.setSerialNumber((String) deviceDetail.get("serial_number"));
			template.setDate((String) deviceDetail.get("update_date"));
			
			// 발송 정보 설정 후 전송
			AlarmData alarmData = new AlarmData((String) deviceDetail.get("email"), template);
			alarmSender.sendEmail(alarmData);
		}
		
		return respMap.getResponse();
	}
}
