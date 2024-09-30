package com.mocafelab.web.batch;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mocafelab.web.vo.BeanFactory;
import com.mocafelab.web.vo.ResponseMap;

import net.newfrom.lib.vo.RequestMap;

@RequestMapping("${apiPrefix}/batch/schedule")
@RestController
public class ScheduleBatchController {
	
	@Autowired
	private ScheduleBatchService scheduleBatchService; 
	
	@PostMapping("/table")
	public Map<String, Object> insert_schedule_table(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return scheduleBatchService.insert_schedule_table(param);
	}
	
	@PostMapping("/table/slot")
	public Map<String, Object> insert_schedule_table_slot(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return scheduleBatchService.insert_schedule_table_slot(param);
	}
	
	@PostMapping("/table/block")
	public Map<String, Object> insert_schedule_table_block(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return scheduleBatchService.insert_schedule_table_block(param);
	}
	
	
	@PostMapping("/setting")
	public Map<String, Object> batchScheduleSetting(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return scheduleBatchService.batchScheduleSetting(param);
	}
}
