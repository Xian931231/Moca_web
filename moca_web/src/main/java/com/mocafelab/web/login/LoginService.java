package com.mocafelab.web.login;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mocafelab.web.enums.MemberStatus;
import com.mocafelab.web.enums.MemberType;
import com.mocafelab.web.member.MemberMapper;
import com.mocafelab.web.vo.BeanFactory;
import com.mocafelab.web.vo.Code;
import com.mocafelab.web.vo.ResponseMap;

import lombok.extern.slf4j.Slf4j;
import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.util.SHAUtil;

@Service
@Transactional
@Slf4j
public class LoginService {

	@Autowired
	private BeanFactory beanFactory;
	
	@Autowired
	private LoginMapper loginMapper;
	
	@Autowired
	private MemberMapper memberMapper;
	
	@Autowired
	private SessionUtil sessionUtil;
	
	/**
	 * 로그인
	 * @param param
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> login(Map<String, Object> param, HttpServletRequest request, HttpServletResponse response) throws Exception {
		return loginProcess(param, request, response);
	}
	
	/**
	 * 중복 로그인
	 * @param param
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> duplicateLogin(Map<String, Object> param, HttpServletRequest request, HttpServletResponse response) throws Exception {
		return loginProcess(param, request, response);
	}
	

	/**
	 * 로그인 공통 프로세스
	 * @param param
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	private Map<String, Object> loginProcess(Map<String, Object> param, HttpServletRequest request, HttpServletResponse response) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 비밀번호 
		String passwd = String.valueOf(param.get("passwd"));
		passwd = SHAUtil.encrypt(passwd, "SHA-256");
		param.put("passwd", passwd);
		
		// 회원 정보 설정
		Map<String, Object> loginData = loginMapper.getLoginData(param);
		
		// 로그인 접속 정보 설정 
		Map<String, Object> accessData = loginMapper.getAccessData(loginData);
		
		Code code = Code.OK;
		if(CommonUtil.checkIsNull(loginData)) {
			// 1. 로그인 시도 ID, PW 를 가진 회원이 있는지 확인
			code = Code.LOGIN_FAIL; 
		} else {
			// member_id 설정
			param.put("member_id", loginData.get("member_id"));
			
			// 로그인 가능 여부 검사
			code = isLoginEnable(param, loginData);
			
			// 중복 로그인 검사
			if(hasDuplicateLogin(param, accessData) == true) {
				return respMap.getResponse(Code.LOGIN_IS_DUPLICATED);
			}
			
			if(!CommonUtil.checkIsNull(accessData) && !CommonUtil.checkIsNull(accessData, "access_token")) {
				loginData.put("access_token", accessData.get("access_token"));
			}
		}
		
		if(code.equals(Code.OK)) {
			setLoginSuccess(param, loginData, request, response);
		} else if(code.equals(Code.LOGIN_FAIL) || code.equals(Code.LOGIN_FAIL_COUNT_5)) {
			// 로그인 정보 틀리거나 로그인 실패 횟수가 5회 이상인 경우 실패 로그 추가 
			setLoginFail(param);			
		}
		
		return respMap.getResponse(code);
	}
	
	/**
	 * 로그인 여부 체크(세션)
	 * @param reqMap
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> hasLogin(Map<String, Object> param, HttpServletRequest request) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		HttpSession session = request.getSession();
		if (sessionUtil.sessionValidation(session)) {
			if (loginMapper.hasLogin(param) == 0) {
				return respMap.getResponse(Code.LOGIN_INFO_IS_NULL);
			}
		} else {
			return respMap.getResponse(Code.LOGIN_INFO_IS_NULL);
		}

		return respMap.getResponse();
	}
	
	/**
	 * 로그아웃
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	public Map<String, Object> logout(Map<String, Object> param, HttpServletRequest request, HttpServletResponse response) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		param.put("member_id", param.get("login_id"));
		loginMapper.removeAccessToken(param);
		
		sessionUtil.removeSession(request, response);
		
		return respMap.getResponse();
	}
	
	/**
	 * 로그인 연장 => 세션 30분으로 초기화
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	public Map<String, Object> loginExtension(Map<String, Object> param, HttpServletRequest request, HttpServletResponse response) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();

		Map<String, Object> loginData = new HashMap<>();
		loginData.put("member_id", param.get("login_id"));
		loginData.put("role_id", param.get("login_role_id"));
		loginData.put("utype", param.get("login_utype"));
		loginData.put("status", param.get("login_status"));
		loginData.put("agency_id", param.get("login_agency_id"));
		
		String accessToken = sessionUtil.setAccessToken(loginData, request, response);
		if (accessToken == null) {
			return respMap.getResponse(Code.LOGIN_FAIL);
		}
		
		// db 토큰 업데이트
		sessionUtil.updateSession(request, response);
		
		return respMap.getResponse();
	}
	
	/**
	 * 로그인 가능 여부 검사
	 * @param param
	 * @param loginData: 로그인 요청 회원 정보
	 * @param isAdmin: 관리자 요청인 경우 true / default: false
	 * @return
	 * @throws Exception
	 */
	public Code isLoginEnable(Map<String, Object> param, Map<String, Object> loginData) throws Exception {
		CommonUtil.checkNullThrowException(loginData, "utype");
		CommonUtil.checkNullThrowException(loginData, "login_fail_cnt");
		CommonUtil.checkNullThrowException(loginData, "status");
		
		Code code = Code.OK;
		
		// 3. 회원 타입 검사
		String utype = (String) loginData.get("utype");
		
		// 4. 회원정보의 로그인 실패횟수 확인
		int loginFailCnt = Integer.valueOf(String.valueOf(loginData.get("login_fail_cnt")));
		
		// 5. 회원정보의 회원 상태 확인
		String memberStatus = (String) loginData.get("status");
		if(MemberType.getType(utype).equals(MemberType.ADMIN)) {
			// 관리자 로그인 불가
			code = Code.LOGIN_FAIL_UNAUTHORIZED;
		}else if(MemberType.getType(utype).equals(MemberType.EXTERNAL)) {
			// 매체사 현장 직원인 경우 로그인 불가
			code = Code.LOGIN_FAIL_EXTERNAL;
		} else if(loginFailCnt > 4) {
			// 로그인 횟수가 5회가 넘는다면 fail 
			code = Code.LOGIN_FAIL_COUNT_5;
		} else {
			// 로그인 정보가 없다면 -> 로그인 진행
			// 관리자의 로그인 시도는 회원 상태 체크 안함
			switch(MemberStatus.getStatus(memberStatus)) {
				case OK:
					code = Code.OK;
					break;
				case LEAVE_REQUEST:
				case LEAVE:
//					code = Code.OK;
//					break;
					code = Code.LOGIN_FAIL_LEAVE;
					break;
				case AUTH_WAIT:
					code = Code.LOGIN_FAIL_WAITING;
					break;
				case SERVICE_HOLD:
					code = Code.LOGIN_FAIL_HALT;
					break;
				case HUMAN_MEMBER:
					code = Code.LOGIN_FAIL_DORMANCY;
					break;
				case APPROVE_WAIT:
					code = Code.LOGIN_FAIL_APPROVE_WAIT;
					break;
				case APPROVE_REJECT:
					code = Code.LOGIN_FAIL_APPROVE_REJECT;
					break;
				default:
					code = Code.LOGIN_FAIL;
			}
		}
		
		log.debug("Login Code =====> " + code);
		
		return code;
	}
	
	/**
	 * 중복 로그인 체크 
	 * @param accessData
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public boolean hasDuplicateLogin(Map<String, Object> param, Map<String, Object> accessData) throws Exception {
		if(!CommonUtil.checkIsNull(accessData)) {
			
			CommonUtil.checkNullThrowException(accessData, "is_expire");
			
			// 로그인 정보가 있고
			String isExpire = (String) accessData.get("is_expire");
			String duplicateLoginYn = (String) param.get("duplicate_login");
			
			if(duplicateLoginYn != null && duplicateLoginYn.equals("N")) {
				// 중복 로그인 요청이 아니고
				if(isExpire != null && isExpire.equals("N")){
					// 만료되지 않은 토큰이라면

					// 로그인 실패 로그 추가
//					loginMapper.addLoginFailLog(param);
					
					return true;
				} else {
					// 만료된 토큰이라면 -> 토큰 갱신 
				}
			} else {
				// 중복 로그인 요청이라면 -> 토큰 갱신
			}
		}
		
		return false;
	}
	
	/**
	 * 로그인 성공 처리
	 * - 만료 토큰 있으면 제거
	 * - 토큰 발급, 세션 및 쿠키 저장
	 * - 로그인 실패 횟수 초기화 및 최근 로그인 시간 변경 
	 * - 로그인 토큰 저장 
	 * - 로그인 성공 로그 추가
	 * @throws Exception
	 */
	public void setLoginSuccess(Map<String, Object> param, Map<String, Object> loginData, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// 만료 토큰 있으면 제거
		loginMapper.removeAccessToken(loginData);
		
		// 토큰 발급, 세션 및 쿠키 저장
		String accessToken = sessionUtil.setAccessToken(loginData, request, response);
		loginData.put("access_token", accessToken);
		
		// 로그인 성공 처리 
		loginMapper.modifyLoginSuccess(loginData);
		
		// 로그인 토큰 저장
		loginMapper.addAccessToken(loginData);
		
		// 로그인 성공 로그 추가
		loginData.put("remote_ip", param.get("remote_ip"));
		loginMapper.addLoginSuccessLog(loginData);
	}
	
	/**
	 * 로그인 실패 처리 
	 *  - 실패 카운트 증가 
	 *  - 실패 로그 추가
	 * @param param
	 * @throws Exception
	 */
	public void setLoginFail(Map<String, Object> param) throws Exception {
		// 로그인 실패 카운트 증가
		loginMapper.addLoginFailCnt(param);
		
		// 로그인 실패 로그 추가
		loginMapper.addLoginFailLog(param);
	}
	

	/**
	 * 관리자 로그인 시도시 처리 로직
	 * @param request
	 * @param response
	 */
	public void setAdminLogin(HttpServletRequest request, HttpServletResponse response) {
		try {
			String requestUri = request.getRequestURI();
			Map<String, Object> urlParam = CommonUtil.getUrlParams(request);
			
			if(requestUri.startsWith("/login") && !CommonUtil.checkIsNull(urlParam)) {
				urlParam.put("remote_ip", CommonUtil.getRemoteIP(request));
				if(!CommonUtil.checkIsNull(urlParam, "temp_auth_value") && !CommonUtil.checkIsNull(urlParam, "member_id")) {
					log.debug("urlParam : {}", urlParam);
					// DB에 존재하는 temp_auth_value를 확인하고 있고, 유효하다면 로그인 프로세스 실행
					if(loginMapper.hasTempAuthValue(urlParam) > 0) {
						// tempAuthValue를 가지고있을 경우 DB에서 초기화해준다. 
						
						Map<String, Object> memberData = memberMapper.getMemberData(urlParam);
						
						if(isLoginEnable(urlParam, memberData).equals(Code.OK)) {
							setLoginSuccess(urlParam, memberData, request, response);
							loginMapper.removeTempAuthValue(urlParam);
						} else {
							setLoginFail(urlParam);
						}
					} 
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
}
