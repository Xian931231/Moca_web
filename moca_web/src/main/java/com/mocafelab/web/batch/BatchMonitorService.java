package com.mocafelab.web.batch;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mocafelab.web.vo.BeanFactory;
import com.mocafelab.web.vo.Code;
import com.mocafelab.web.vo.ResponseMap;

import lombok.RequiredArgsConstructor;
import net.newfrom.lib.util.CommonUtil;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = {Exception.class})
public class BatchMonitorService {

 	private final BeanFactory beanFactory;
	
	private final BatchMonitorMapper batchMapper;
	
	private final BatchMonitorRepository batchRepository;
	
	@Value("${batch.path.default}")
	private String DEFAULT_FILE_PATH;

	/*
	 * 배치 CODE or ID 값으로 특정 배치를 바로 실행
	 * @throws IOException 
	 */
	public Map<String, Object> executeBatchShell(Map<String, Object> param) {
		ResponseMap responseMap = beanFactory.getResponseMap();
		
		// 배치 코드에 대한 정보 읽어오기
		Map<String, Object> getBatchMonitor = batchMapper.getBatchMonitor(param);
		if (CommonUtil.checkIsNull(getBatchMonitor)) {
			return responseMap.getErrResponse();
		}
		
		try {
			String shellFullPath = DEFAULT_FILE_PATH + (String) getBatchMonitor.get("batch_path");
			
			// 배치(명령) 실행
			executeCommand(shellFullPath);
			
		} catch (IOException e) {
			responseMap.setBody("code", Code.ERROR.code);
			responseMap.setBody("msg", "execute log batch shell Fail!");
		}
		
		return responseMap.getResponse();
	}
	
	/**
	 * Shell Script Command 실행
	 * @param batchPath
	 * @throws IOException
	 */
	public void executeCommand(String batchPath) throws IOException {
		ProcessBuilder builder = new ProcessBuilder();
		
        // 명령어 주입 및 실행
        builder.command(batchPath);
        
        // 명령어를 시작 시킴
		builder.start();
	}
	
	/**
	 * 최근에 실행된 배치 명령어별 조회 (배치 이름, 배치 결과, limit, offset)
	 * @param param
	 * @return
	 */
	public Map<String, Object> getBatchMonitorList(Map<String, Object> param) {
		ResponseMap responseMap = beanFactory.getResponseMap();
		
		// 배치 이름, 결과값으로 정렬된 리스트
		List<Map<String,Object>> batchList = batchMapper.getBatchMonitorList(param);
		
		responseMap.setBody("list", batchList);
		
		return responseMap.getResponse();
	}

	/**
	 * 특정한 배치 명령어에 대해 실행된 히스토리 정보 출력 (배치 코드, 시작~종료 날짜, limit, offset)
	 * @param param
	 * @return
	 * @throws IOException 
	 */
	public Map<String, Object> getBatchLogList(Map<String, Object> param) {
		ResponseMap responseMap = beanFactory.getResponseMap();
		
		// 배치 모니터링 정보 얻기
		Map<String, Object> getBatchMonitor = batchMapper.getBatchMonitor(param);
		if (CommonUtil.checkIsNull(getBatchMonitor)) {
			return responseMap.getErrResponse(Code.ERROR);
		}
		
		// 배치 경로
		String logFilePath = (String) getBatchMonitor.get("log_path");

		param.put("log_path", logFilePath);
		
		// 배치 로그 파일 목록
		List<Map<String, Object>> logList = batchRepository.getLogList(param);
		// 총 로그 수
		int count = batchRepository.getLogTotalCount(param);
	
		responseMap.setBody("list", logList);
		responseMap.setBody("total_count", count);
			
		return responseMap.getResponse();
	}
	

}
