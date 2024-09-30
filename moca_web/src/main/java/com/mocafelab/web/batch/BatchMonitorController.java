package com.mocafelab.web.batch;

import java.io.IOException;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.vo.RequestMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("${apiPrefix}/batch")
public class BatchMonitorController {
	
	private final BatchMonitorService batchService;

	/**
	 * batch shell 실행
	 * @param requestMap
	 * @return
	 */
	@PostMapping("/directExecute")
	public Map<String, Object> executeBatchShell(RequestMap requestMap) {
		
		Map<String, Object> param = requestMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "batch_code");
		
		return batchService.executeBatchShell(param);
	}
	
	/**
	 * 최근에 실행된 배치 명령어별 상황 조회
	 * @param requestMap
	 * @return
	 * @throws IOException
	 */
	@PostMapping("/list")
	public Map<String, Object> getBatchMonitorList(RequestMap requestMap) throws IOException {
		
		Map<String, Object> param = requestMap.getMap();
		
		return batchService.getBatchMonitorList(param);
	}
	
	/**
	 * 특정한 배치 명령어에 대해 실행된 히스토리 정보 조회
	 * @param requestMap
	 * @return
	 * @throws IOException 
	 */
	@PostMapping("/detail")
	public Map<String, Object> getBatchLogList(RequestMap requestMap) throws IOException {
		
		Map<String, Object> param = requestMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "batch_code");
		CommonUtil.checkNullThrowException(param, "str_dt");
		CommonUtil.checkNullThrowException(param, "end_dt");
		
		return batchService.getBatchLogList(param);
	}
	
}
