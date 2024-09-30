package com.mocafelab.web.ad.schedule;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Transactional과 syncronized를 동시에 사용시 발생하는 이슈로인해 생성한 서비스 
 * (커밋되지 않은 데이터를 읽을 수 있는 이슈)
 */
@Service
public class ScheduleSyncService {

	@Autowired
	private ScheduleService scheduleService;
	
	public synchronized Map<String, Object> addSyncSchedule(Map<String, Object> param) throws Exception {
		String type = (String) param.get("type");
		
		if(type.equals("A")) {
			// 등록
			return scheduleService.addSchedule(param);
		} else {
			// 수정
			return scheduleService.modifySchedule(param);
		}
	}
}
