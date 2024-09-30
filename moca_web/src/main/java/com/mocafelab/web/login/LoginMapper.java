package com.mocafelab.web.login;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoginMapper {

	// 로그인 정보
	public Map<String, Object> getLoginData(Map<String, Object> param);
	
	// 로그인 날짜 갱신, 로그인 실패 카운트 초기화
	public int modifyLoginSuccess(Map<String, Object> param);
	
	// 로그인 로그 추가
	@Deprecated
	public int addAccessLog(Map<String, Object> param);
	
	// 로그인 성공 로그 추가 
	public int addLoginSuccessLog(Map<String, Object> param);
	
	// 로그인 실패 로그 추가 
	public int addLoginFailLog(Map<String, Object> param);
	
	// 로그인 실패 카운트 추가
	public int addLoginFailCnt(Map<String, Object> param);
	
	// 로그인 여부 체크
	public int hasLogin(Map<String, Object> param);
	
	// 중복 로그인 체크
	public int hasDuplicateLogin(Map<String, Object> param);
	
	// 로그인 세션 가져오기
	public Map<String, Object> getAccessData(Map<String, Object> param);
	
	// 로그인 토큰 저장
	public int addAccessToken(Map<String, Object> param);
	
	// 로그인 토큰 업데이트
	public int modifyAccessToken(Map<String, Object> param);

	// 로그아웃시 토큰 삭제
	public int removeAccessToken(Map<String, Object> param);

	// tempAuthValue 설정
	public int setTempAuthValue(Map<String, Object> param);
	
	// tempAuthValue 존재 확인
	public int hasTempAuthValue(Map<String, Object> param);
	
	// tempAuthValue 삭제
	public int removeTempAuthValue(Map<String, Object> param);
}
