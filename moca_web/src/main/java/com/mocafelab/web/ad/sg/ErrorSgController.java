package com.mocafelab.web.ad.sg;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.vo.RequestMap;

@RestController
@RequestMapping("${apiPrefix}/sg/error")
public class ErrorSgController {
	
	@Autowired
	private ErrorSgService errorSgService;
	
	@PostMapping("/list")
	public Map<String, Object> getListForSgManage(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return errorSgService.getListForSgManage(param);
	}
	
	@PostMapping("/modify/status")
	public Map<String, Object> modifyStatusForSgManage(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "ad_event_log_id");
		CommonUtil.checkNullThrowException(param, "process_kind");
		
		return errorSgService.modifyStatusForSgManage(param);
	}
	
	@PostMapping("/ssp/list")
	public Map<String, Object> getListForSspManage(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return errorSgService.getListForSspManage(param);
	}
}
