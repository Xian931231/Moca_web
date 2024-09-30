package com.mocafelab.web.batch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class BatchMonitorRepository {
	
	@Value("${batch.path.default}")
	private String DEFAULT_FILE_PATH;


	public List<Map<String, Object>> getLogList(Map<String, Object> param) {
		List<Map<String, Object>> responseList = new ArrayList<>();
		
		String startDate = (String) param.get("str_dt");
		String endDate = (String) param.get("end_dt");
		
		// 배치 경로
		String logFilePath = DEFAULT_FILE_PATH + (String) param.get("log_path");
		
        String filePath = getfilePath(logFilePath);
        String fileName = getfileName(logFilePath);
        
		// 페이징 처리를 위한 계산
		int limit = (int) param.get("limit");
		int offset = (int) param.get("offset");
		
		offset = (1 + (offset - 1) * limit) - 1;
		
		Properties props = createProperty();
		
		String sql = "SELECT *" +
                " FROM " + fileName +
                " WHERE SUBSTRING(date, 1, 10) >= '" + startDate + "'" +
                " AND SUBSTRING(date, 1, 10) <= '" + endDate + "'" +
                " LIMIT " + limit + " OFFSET " + offset;

        try (Connection conn = DriverManager.getConnection("jdbc:xbib:csv:" + filePath, props);
             Statement stmt = conn.createStatement();
             ResultSet results = stmt.executeQuery(sql);
        ) {

            while (results.next()) {
            	addLogDataToList(responseList, results);
            }
        } catch (SQLException e) {
        	throw new RuntimeException(e);
		}
        
		return responseList;
	}
	
	public int getLogTotalCount(Map<String, Object> param) {
		String startDate = (String) param.get("str_dt");
		String endDate = (String) param.get("end_dt");
		
		// 배치 경로
		String logFilePath = DEFAULT_FILE_PATH + (String) param.get("log_path");
		
		String filePath = getfilePath(logFilePath);
        String fileName = getfileName(logFilePath);
		
		Properties props = createProperty();

		int count = 0;

		String sql =  "SELECT count(*)" +
                " FROM " + fileName +
                " WHERE SUBSTRING(date, 1, 10) >= '" + startDate + "'" +
                " AND SUBSTRING(date, 1, 10) <= '" + endDate + "'";
		
        try (Connection conn = DriverManager.getConnection("jdbc:xbib:csv:" + filePath, props);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
        ) {

        	if (rs.next()) {
        		count = rs.getInt(1);
            }
        } catch (SQLException e) {
        	throw new RuntimeException(e);
		}
        
        return count;
	}

	/**
	 * 파일 경로 추출
	 * @param logFilePath
	 * @return
	 */
	private String getfilePath(String logFilePath) {
		int pathIndex = logFilePath.lastIndexOf("/");
		
		return logFilePath.substring(0, pathIndex + 1);
	}
	
	/**
	 * 파일 이름 추출
	 * @param logFilePath
	 * @return
	 */
	private String getfileName(String logFilePath) {
		int pathIndex = logFilePath.lastIndexOf("/");
        int prevDateIndex = logFilePath.lastIndexOf("_");
		
		return logFilePath.substring(pathIndex + 1, prevDateIndex);
	}
	
	/**
	 * 프로퍼티 설정
	 * @return
	 */
	private Properties createProperty() {
		Properties props = new Properties();
        props.put("fileExtension", ".log");
        props.put("indexedFiles", "true");
        props.put("fileTailPattern", "_(\\d+)");
        props.put("fileTailParts", ",");
        
        return props;
	}

	/**
	 * 리스트에 데이터 담기
	 * @param responseList
	 * @param results
	 * @throws SQLException
	 */
	private void addLogDataToList(List<Map<String, Object>> responseList, ResultSet results) throws SQLException {
		Map<String, Object> map = new LinkedHashMap<>();
		
		map.put("log_date", results.getString("date"));
		map.put("log_result", results.getString("result"));
		map.put("log_param", results.getString("param"));
		map.put("log_total_count", results.getString("total"));
		
		responseList.add(map);
	}
}
