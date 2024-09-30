package com.mocafelab.web.member;

import java.io.File;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.dhatim.fastexcel.Color;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.mocafelab.web.alarm.AlarmData;
import com.mocafelab.web.alarm.AlarmSender;
import com.mocafelab.web.email.EmailService;
import com.mocafelab.web.email.template.ModifyInfoCompleteTemplate;
import com.mocafelab.web.email.template.SignUpAuthTemplate;
import com.mocafelab.web.email.template.SignUpCompleteTemplate;
import com.mocafelab.web.email.template.StaffTempPasswordTemplate;
import com.mocafelab.web.email.template.SupplyApproveTemplate;
import com.mocafelab.web.email.template.TempPasswordTemplate;
import com.mocafelab.web.enums.MemberStatus;
import com.mocafelab.web.enums.MemberType;
import com.mocafelab.web.enums.ModifyHistory;
import com.mocafelab.web.file.S3Service;
import com.mocafelab.web.login.SessionUtil;
import com.mocafelab.web.menu.MenuService;
import com.mocafelab.web.role.RoleMapper;
import com.mocafelab.web.vo.BeanFactory;
import com.mocafelab.web.vo.Code;
import com.mocafelab.web.vo.ResponseMap;

import net.newfrom.lib.file.S3UploadOption;
import net.newfrom.lib.file.UploadFileInfo;
import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.util.FileUtil;
import net.newfrom.lib.util.SHAUtil;
import net.newfrom.lib.util.ValidateUtil;

@Service
@Transactional(rollbackFor = Exception.class)
public class MemberService {

	@Autowired
	private BeanFactory beanFactory;
	
	@Autowired
	private MemberMapper memberMapper;
	
	@Autowired
	private AgencyMemberMapper agencyMemberMapper;
	
	@Autowired
	private RoleMapper roleMapper;
	
	@Autowired
	private AlarmSender alarmSender;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private MenuService menuService;
	
	@Autowired
	private DemandMemberService demandMemberService;
	
	@Autowired
	private SessionUtil sessionUtil;
	
	@Autowired
	private S3Service s3;
	
	@Value("${file.path.upload.s3}")
	private String S3_FILE_DEFAULT_PATH;
	
	@Value("${file.path.member.biznum}")
	private String FILE_MEMBER_BIZNUM_PATH;
	
	@Value("${file.path.member.modify}")
	private String FILE_MEMBER_MODIFY;
	
	/**
	 * 사용자 등록
	 * @param param
	 * @param mRequest
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> addMember(Map<String, Object> param, MultipartHttpServletRequest mRequest) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();

		// 아이디 유효성 체크
		if (!hasValidateId(param)) {
			return respMap.getResponse(Code.MEMBER_ID_IS_NOT_VALID);
		}
		
		// 아이디 중복 체크
		if (memberMapper.hasDuplicateId(param) > 0) {
			return respMap.getResponse(Code.MEMBER_DUPLICATE_ID);
		}
		
		// 비밀번호 유효성 체크
		if (!hasValidatePw(String.valueOf(param.get("passwd")))) {
			return respMap.getResponse(Code.MEMBER_PW_IS_NOT_VALID);
		}
		
		// 비밀번호 암호화 
		String passwd = String.valueOf(param.get("passwd"));
		param.put("passwd", SHAUtil.encrypt(passwd, "SHA-256"));
		
		// 이메일 체크
		String email = (String) param.get("email");
		String companyEmail = (String) param.get("company_email");
		if(!ValidateUtil.checkEmail(email) || !ValidateUtil.checkEmail(companyEmail)) {
			return respMap.getResponse(Code.MEMBER_EMAIL_IS_NOT_VALID);
		} else {
			if (memberMapper.hasDuplicateEmail(param) > 0) {
				return respMap.getResponse(Code.MEMBER_DUPLICATE_EMAIL);
			}
		}

		// 이메일 인증 체크
		if (memberMapper.hasEmailAuth(param) <= 0) {
			return respMap.getResponse(Code.MEMBER_AUTH_IS_NOT_MATCH);
		}
		
		// 연락처 체크
		String mobile = String.valueOf(param.get("mobile"));
		if(mobile.length() > 13) {
			return respMap.getResponse(Code.MEMBER_MOBILE_IS_NOT_VALID);
		} else {
			param.put("mobile", makeMobile(mobile));
		}
		
		// 사업자등록번호 체크
		boolean regnumChk = false;
		String[] corporationNumList = {"81", "82", "83", "84", "85", "86", "87", "88"};
		String regnum = String.valueOf(param.get("company_regnum"));
		String midNum = regnum.substring(3, 5);
		for (String num : corporationNumList) {
			if (midNum.equals(num) && regnum.length() == 10) {
				regnumChk = true;
				break;
			}
		}
		if(regnumChk == false) {
			return respMap.getResponse(Code.MEMBER_REGNUM_IS_NOT_VALID);
		}

		// 계정 종류 체크 (D, B, S만 허용)
		String utype = (String) param.get("utype");
		if (!utype.equals(MemberType.AGENCY.getType()) && !utype.equals(MemberType.DEMAND.getType()) && !utype.equals(MemberType.SUPPLY.getType())) {
			return respMap.getResponse(Code.MEMBER_ROLE_NOT_ACCEPT);
		} 

		// 계정 상태 (A(정상활동), V(승인대기))
		if(utype.equals(MemberType.SUPPLY.getType())) {
			// 매체사인 경우 승인 대기
			param.put("status", MemberStatus.APPROVE_WAIT.getStatus());
		} else {
			param.put("status", MemberStatus.OK.getStatus());
		}
		
		// role
		if(!utype.equals(MemberType.AGENCY.getType())) {
			int roleId = memberMapper.getRole(param);
			param.put("role_id", roleId);
		} else {
			param.put("role_id", 0);
		}

		// DB Insert
		Map<String, Object> addMemberInfo = memberMapper.addMember(param); 
		int memberId =  Integer.valueOf(String.valueOf(addMemberInfo.get("id")));
		param.put("member_id", memberId);
		if (memberId <= 0) {
			throw new RuntimeException();
		}
		
		// 사업자등록증 업로드
		String filePath = FILE_MEMBER_BIZNUM_PATH + File.separator + memberId;
		param.put("signup", true);
		uploadCompanyRegnumImage(param, filePath, mRequest);
		
		if(memberMapper.addMemberBiznumPath(param) < 0) {
			s3.removeFile((String) param.get("company_regnum_image"));
			throw new RuntimeException();
		}
		
		
		//대행사일때 기본권한생성(최고관리자, 관리자, 담당자, 미지정) 
		// role_sort: 0, 1, 2, 3
		if(utype.equals(MemberType.AGENCY.getType())) {
			List<Map<String, Object>> roleList = memberMapper.addDefaultRole(param);
			
			if(roleList.size() < 4) {
				throw new RuntimeException();
			}
			
			// 기본권한 별 디폴트 메뉴 등록
			for(Map<String, Object> role : roleList) {
				Long roleId = (long) role.get("id");
				param.put("role_id", roleId);

				param.put("role_sort", role.get("sort"));
				List<Map<String, Object>> agencyDefaultMenuList = memberMapper.getAgencyDefaultMenuList(param);
				for(Map<String, Object> menuMap : agencyDefaultMenuList) {
					param.put("menu_id", menuMap.get("menu_id"));
					param.put("access_yn", menuMap.get("default_yn"));

					// role_menu 등록
					if(memberMapper.addAgencyRoleMenu(param) <= 0) {
						throw new RuntimeException();
					}
				}
			}
			
			// 생성한 최고 관리자 아이디 
			Long role_id = (long) roleList.get(0).get("id");
			param.put("role_id", role_id);
			
			//대행사 계정의 role_id 수정
			if(memberMapper.modifyRoleId(param) <= 0) {
				throw new RuntimeException();
			}
		}
		
		// 사용할 이메일 템플릿 객체 생성 후 값 설정
		SignUpCompleteTemplate template = new SignUpCompleteTemplate();
		template.setName((String) param.get("uname"));
		template.setUid((String) param.get("uid"));
		template.setSignUpDate((String) addMemberInfo.get("insert_date"));
		
		// 발송 정보 설정 후 전송
		AlarmData alarmData = new AlarmData(email, template);
		alarmSender.sendEmail(alarmData);
		
		return respMap.getResponse();
	}
	
	/**
	 * 아이디 중복 체크
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> hasDuplicateId(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 아이디 유효성 체크
		if (!hasValidateId(param)) {
			return respMap.getResponse(Code.MEMBER_ID_IS_NOT_VALID);
		}
		
		// 중복 검사
		int result = memberMapper.hasDuplicateId(param);
		if (result > 0) {
			return respMap.getResponse(Code.MEMBER_DUPLICATE_ID);
		} else {
			return respMap.getResponse();
		}
	}
	
	/**
	 * 사업자 등록증 업로드
	 * @param param
	 * @param mRequest
	 * @return
	 * @throws Exception
	 */
	private void uploadCompanyRegnumImage(Map<String, Object> param, String filePath, MultipartHttpServletRequest mRequest) throws Exception {
		MultipartFile mFile = mRequest.getFile("company_regnum_image");
		
		String ext = FileUtil.getFileExt(mFile.getOriginalFilename());
		if(!ext.equals("png") && !ext.equals("jpg") && !ext.equals("pdf")) {
			throw new RuntimeException();
		}
		
		String mimeType = mFile.getContentType();
		if(!mimeType.equals("image/jpeg") && !mimeType.equals("image/png") && !mimeType.equals("application/pdf")) {
			throw new RuntimeException();
		}
		
		long size = mFile.getSize();
		
		if(size > 200 * 1024) {
			throw new RuntimeException();
		}
		
		// S3 파일 업로드
		S3UploadOption uploadOptions = (S3UploadOption) s3.getUploadOption();
		
		if(!CommonUtil.checkIsNull(param, "signup")) {
			if(param.get("utype").equals(MemberType.SUPPLY.getType())) {
				uploadOptions.setContentDispoisitionPrefix("inline");
				s3.setUploadOption(uploadOptions);
			}
		}
		UploadFileInfo uploadFileInfo = s3.uploadFile(mFile, filePath);

		param.put("company_regnum_image", uploadFileInfo.getFilePath());
		param.put("company_regnum_file_name", uploadFileInfo.getFileName());
	}
	
	/**
	 * 회원가입 이메일 인증번호 발송
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> sendMail(Map<String,Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 이메일 체크
		String email = (String) param.get("company_email");
		
		if(!ValidateUtil.checkEmail(email)) {
			return respMap.getResponse(Code.MEMBER_INFO_IS_NOT_MATCH);
		}
		
		if(param.get("login_id") != null) {
			if(memberMapper.hasEqualsMemberEmail(param) > 0) {
				return respMap.getResponse(Code.MEMBER_EQUALS_EMAIL);
			}
		}
		
		if (memberMapper.hasDuplicateEmail(param) > 0) {
			return respMap.getResponse(Code.MEMBER_DUPLICATE_EMAIL);
		}
		
		
		// 랜덤 문자열
		String authValue = CommonUtil.makeRandStr(8);
		param.put("auth_value", authValue);
		param.put("auth_kind", "E");
		param.put("auth_type", "S");
		
		// 인증용 테이블에 해당 이메일이 있을 경우 인증번호 갱신
		if(memberMapper.hasAuthCnt(param) > 0) {
			// 인증번호 업데이트
			memberMapper.modifyAuth(param);
		} else {
			// (신규)인증번호 저장
			memberMapper.addAuth(param);
		}
		
		// 사용할 이메일 템플릿 객체 생성 후 값 설정
		SignUpAuthTemplate template = new SignUpAuthTemplate();
		template.setAuthValue(authValue);
		
		// 발송 정보 설정 후 전송
		AlarmData alarmData = new AlarmData(email, template);
		emailService.sendEmail(email, alarmData.getTitle(), alarmData.getContent());

		return respMap.getResponse();
	}
	
	/**
	 * 인증번호 체크
	 * @param param
	 * @param reqeust
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> hasAuth(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		param.put("auth_value", String.valueOf(param.get("auth_value")));
		param.put("auth_type", "S");
		
		Map<String, Object> authInfo = memberMapper.hasAuth(param);
		
		if(authInfo != null) {
			if(authInfo.get("auth_yn").equals("Y")) {
				return respMap.getResponse();
			}
			// true: 만료, flase: 유효
			boolean expire = (boolean) authInfo.get("is_expire");
			if(expire) {
				return respMap.getResponse(Code.MEMBER_AUTH_VALUE_IS_EXPIRED);
			} else { 
				// 인증 성공 시 auth_tmp 수정
				if (memberMapper.modifyAuthSuccess(param) <= 0) {
					throw new RuntimeException();
				}
			}
		} else { 
			// 올바르지 않은 인증번호
			return respMap.getResponse(Code.MEMBER_AUTH_IS_NOT_MATCH);
		}
		
		return respMap.getResponse();
	}

	/**
	 * 	아이디 찾기
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getId(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 이메일 체크
		String email = (String) param.get("email");
		if(!ValidateUtil.checkEmail(email)) {
			return respMap.getResponse(Code.MEMBER_INFO_IS_NOT_MATCH);
		}
		
		Map<String, Object> userInfo = memberMapper.getMemberInfo(param);
		if (userInfo != null ) {
			// 아이디 마스킹
			String uId = (String) userInfo.get("uid");
			String idMasking = uId.replaceAll("(?<=.{4}).", "*");
			respMap.setBody("uid", idMasking);
		} else {
			return respMap.getResponse(Code.MEMBER_INFO_IS_NOT_MATCH);
		}
		
		return respMap.getResponse();
	}
	
	/**
	 * 	비밀번호 찾기 (임시 비밀번호)
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getPw(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 이메일 체크
		String email = String.valueOf(param.get("email"));
		if(!ValidateUtil.checkEmail(email)) {
			return respMap.getResponse(Code.MEMBER_INFO_IS_NOT_MATCH);
		}
		
		Map<String, Object> userInfo = memberMapper.getMemberInfo(param);
		if(userInfo == null) {
			return respMap.getResponse(Code.MEMBER_INFO_IS_NOT_MATCH);
		}
		String uId = String.valueOf(param.get("uid"));
		
		// 임시 비밀번호 발급
		if(uId != null) {
			String newPasswd = makeRandTempPasswd(10);
			String encPasswd = SHAUtil.encrypt(newPasswd, "SHA-256");
			param.put("new_passwd", encPasswd);
			param.put("login_id", userInfo.get("member_id"));
			param.put("temp_passwd_yn", "Y");
			if(memberMapper.modifyNewPw(param) > 0) {
				// 메일 설정
				String uname = (String) userInfo.get("uname");
				String utype = (String) userInfo.get("utype"); 
				
				// 사용할 이메일 템플릿 객체 생성 후 값 설정
				TempPasswordTemplate template = new TempPasswordTemplate();
				template.setName(uname);
				template.setUid(uId);
				template.setTempPassword(newPasswd);
				if(utype.equals(MemberType.AGENCY.getType())) {
					template.setReturnUrl(MemberType.AGENCY.getModifyUrl());
				} else if(utype.equals(MemberType.DEMAND.getType())) {
					template.setReturnUrl(MemberType.DEMAND.getModifyUrl());
				} else if(utype.equals(MemberType.SUPPLY.getType())) {
					template.setReturnUrl(MemberType.SUPPLY.getModifyUrl());
				} else {
					template.setReturnUrl(MemberType.ADMIN.getModifyUrl());
				}
				
				// 발송 정보 설정 후 전송
				AlarmData alarmData = new AlarmData(email, template);
				emailService.sendEmail(email, alarmData.getTitle(), alarmData.getContent());
				
				// 인증번호 저장
				param.put("company_email", email);
				param.put("auth_value", newPasswd);
				param.put("auth_kind", "E");
				param.put("auth_type", "P");
				
				// 인증용 테이블에 해당 이메일이 있을 경우 인증번호 갱신
				if(memberMapper.hasAuthCnt(param) > 0) {
					// 인증번호 업데이트
					memberMapper.modifyAuth(param);
				} else {
					// (신규)인증번호 저장
					memberMapper.addAuth(param);
				}
			} else {
				throw new RuntimeException();
			}
		} else {
			return respMap.getResponse(Code.MEMBER_INFO_IS_NOT_MATCH);
		}
		
		return respMap.getResponse();
	}
	
	/**
	 * 비밀번호 변경
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> modifyPw(HttpServletRequest request, Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();

		// 현재 비밀번호 암호화
		String currentPw = String.valueOf(param.get("passwd"));
		param.put("passwd", SHAUtil.encrypt(currentPw, "SHA-256"));
		
		// 현재 비밀번호 확인
		HttpSession session = request.getSession();
		if(sessionUtil.sessionValidation(session)) {
			String savePw = memberMapper.getSavePassword(param);
			if(savePw == null) {
				return respMap.getResponse(Code.MEMBER_PW_IS_NOT_MATCH);
			}
			
			String newPw = String.valueOf(param.get("new_passwd"));
			String confirmPw = String.valueOf(param.get("confirm_passwd"));
			
			// 새 비밀번호 유효성 체크
			if (!hasValidatePw(newPw)) {
				return respMap.getResponse(Code.MEMBER_PW_IS_NOT_VALID);
			}
			
			// 새 비밀번호, 비밀번호 확인 일치 체크
			if(!newPw.equals(confirmPw)) {
				return respMap.getResponse(Code.MEMBER_NEW_PW_IS_NOT_MATCH);
			}
			
			// 새 비밀번호 암호화
			String encryptNewPw = SHAUtil.encrypt(newPw, "SHA-256");
			if(savePw.equals(encryptNewPw)) {
				return respMap.getResponse(Code.MEMBER_PW_IS_EXIST);
			}
			
			// 비밀번호 변경
			param.put("new_passwd", encryptNewPw);
			param.put("temp_passwd_yn", "N");
			if (memberMapper.modifyNewPw(param) <= 0) {
				throw new RuntimeException();
			}
		}
		
		return respMap.getResponse();
	}
	
	/**
	 * 30일 후에 비밀번호 변경
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> modifyPwLater(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		memberMapper.modifyPwLater(param);
		
		return respMap.getResponse();
	}
	
	/**
	 * 로그인 후 본인 정보 조회
	 * @param request
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getMyData(HttpServletRequest request, Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		Map<String, Object> myData = memberMapper.getMyData(param);
		if (CommonUtil.checkIsNull(myData)) {
			return respMap.getResponse(Code.NOT_EXIST_DATA);
		} else {
			respMap.setBody("data",myData);
		}
	
		return respMap.getResponse();
	}
	
	/**
	 * 로그인 후 본인 정보 조회 (마스킹용)
	 * @param request
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getMyMaskingData(HttpServletRequest request, Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		Map<String, Object> myData = memberMapper.getMyMaskingData(param);
		if (CommonUtil.checkIsNull(myData)) {
			return respMap.getResponse(Code.NOT_EXIST_DATA);
		} else {
			respMap.setBody("data",myData);
		}
	
		return respMap.getResponse();
	}
	
	
	
	/**
	 * 회원 정보 수정 (대행사, 광고주)
	 * @param request
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> modifyInfo(HttpServletRequest request, Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		// 내 정보 가져오기
		Map<String, Object> myInfo = memberMapper.getMyData(param);
		if (CommonUtil.checkIsNull(myInfo)) {
			return respMap.getResponse(Code.MEMBER_INFO_IS_NULL);
		}
		
		//기본 정보 수정 유무
		boolean hasModifyBasicInfo = false;
		// 업무 담당자 수정 유무
		boolean hasModifyPicInfo = false;
		
		/**
		 * 기본 정보 비교
		 */
		// 비밀번호 수정 확인 => 파라미터에 passwd, new_passwd, confirm_passwd 값이 있으면 수정 O
		String passwd = null;
		if (!CommonUtil.checkIsNull(param, "passwd") && !CommonUtil.checkIsNull(param, "new_passwd") && !CommonUtil.checkIsNull(param, "confirm_passwd")) {
			passwd = SHAUtil.encrypt(String.valueOf(param.get("passwd")), "SHA-256");
			param.put("passwd", passwd);
			
			// 기존 비밀번호 일치 확인
			if (!myInfo.get("passwd").equals(passwd)) {
				return respMap.getResponse(Code.MEMBER_PW_IS_NOT_MATCH);
			}
			
			// 비밀번호 변경
			String newPasswd = null;
			if (!CommonUtil.checkIsNull(param, "new_passwd")) {
				// 새 비밀번호 재입력 확인
				if (!param.get("new_passwd").equals(param.get("confirm_passwd"))) {
					return respMap.getResponse(Code.MEMBER_NEW_PW_IS_NOT_MATCH);
				}
				
				// 새 비밀번호 유효성 체크
				if(!hasValidatePw((String) param.get("new_passwd"))) {
					return respMap.getResponse(Code.MEMBER_PW_IS_NOT_VALID);
				} 
				
				newPasswd = SHAUtil.encrypt(String.valueOf(param.get("new_passwd")), "SHA-256");
				param.put("new_passwd", newPasswd);
			} 
			
			hasModifyBasicInfo = true; 
		}
		
		// 인증용 이메일 수정 확인 => db 값과 비교해서 다르면 수정 O, 같으면 수정 X 
		String companyEmail = (String) param.get("company_email");
		if (!companyEmail.equals(myInfo.get("company_email"))) {
			// 이메일 중복 확인
			if (memberMapper.hasDuplicateEmail(param) > 0) {
				return respMap.getResponse(Code.MEMBER_DUPLICATE_EMAIL);
			}
			
			// 이메일 인증 확인
			if (memberMapper.hasEmailAuth(param) == 0) {
				return respMap.getResponse(Code.MEMBER_AUTH_IS_NOT_MATCH);
			}
			
			hasModifyBasicInfo = true;
		}

		// url이 null일 경우 변환
		if (CommonUtil.checkIsNull(param, "url")) {
			param.put("url", "");
		}
		if (CommonUtil.checkIsNull(myInfo, "url")) {
			myInfo.put("url", "");
		}
		
		
		// 대표 URL, 수신동의 수정 확인 => db 값과 다르면 수정 O
		if (!param.get("url").equals(myInfo.get("url")) || !param.get("accept_email").equals(myInfo.get("accept_email")) || !param.get("accept_sms").equals(myInfo.get("accept_sms"))) {
			hasModifyBasicInfo = true;
		}
		
		
		
		/**
		 * 업무 담당자 정보 비교
		 */
		// 담당자 이름, 담당자 이메일 수정 확인 => db 값과 다르면 수정 O
		
		if (CommonUtil.checkIsNull(param, "uname")) {
			param.put("uname", myInfo.get("uname"));
		}
		
		if (!param.get("uname").equals(myInfo.get("uname")) || !param.get("email").equals(myInfo.get("email"))) {
			hasModifyPicInfo = true;
		}
		
		if (CommonUtil.checkIsNull(param, "mobile")) {
			param.put("mobile", myInfo.get("mobile"));
		}
		
		// 핸드폰 번호 수정 확인 => db 값과 다르면 수정 O
		
		if (!param.get("mobile").equals(myInfo.get("mobile"))) {
			hasModifyBasicInfo = true;
		}
		
		// 하나라도 수정이 있을 경우 
		if (memberMapper.modifyInfo(param) <= 0) {
			throw new RuntimeException();
		}
		
		//활동 이력 추가
		//광고주 일때만 기본 이력 추가
		if(myInfo.get("utype").equals("S")) {
			if(hasModifyBasicInfo) {
				param.put("message", "기본 정보");
				demandMemberService.addDspModHistory(param, ModifyHistory.MEMBER_INFO_MODIFY);
			}
			//업무 담당자 추가
			if(hasModifyPicInfo) {
				param.put("message", "업무담당자 정보");
				demandMemberService.addDspModHistory(param, ModifyHistory.MEMBER_INFO_MODIFY);
			}
		}
		
		return respMap.getResponse();
	}
	
	/**
	 * 사업자 정보 수정 요청
	 * @param param
	 * @param mRequest
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> modifyRequest(Map<String, Object> param, MultipartHttpServletRequest mRequest) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 사업자등록증
		MultipartFile mFile = mRequest.getFile("company_regnum_image");
		
		if (mFile == null) {
			return respMap.getResponse(Code.ERROR);
		}
		
		String memberId = String.valueOf(param.get("login_id"));
		param.put("member_id", memberId);
		
		
		Map<String, Object> modifyRequestMap = memberMapper.getModifyRequest(param);
		if(!CommonUtil.checkIsNull(modifyRequestMap)) {
			// 기존 파일 삭제
			String savedFile = (String) modifyRequestMap.get("company_regnum_image");
			s3.removeFile(savedFile);
			memberMapper.removeModifyRequest(param);
		}
		
		String filePath = FILE_MEMBER_MODIFY + File.separator + memberId;
		
		// S3 파일 업로드
		uploadCompanyRegnumImage(param, filePath, mRequest);
		
		if (memberMapper.modifyRequest(param) <= 0) {
			s3.removeFile((String) param.get("company_regnum_image"));
		};
		
		return respMap.getResponse();
	}
	
	/**
	 * 수정 요청 목록
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getModifyRequestList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> list = memberMapper.getModifyRequestList(param);
		respMap.setBody("list", list);
		
		int total = memberMapper.getModifyRequestCnt(param);
		respMap.setBody("tot_cnt", total);
		
		return respMap.getResponse();
	}
	
	/**
	 * 정보 수정
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> modifyCompanyInfo(Map<String, Object> param, MultipartHttpServletRequest mRequest) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		// 수정할 회원 id, 파일 경로 가져오기
		Map<String, Object> memberInfo = memberMapper.getModifyMemberInfo(param);
		String memberFilePath = (String) memberInfo.get("company_regnum_image");
		param.put("company_regnum", memberInfo.get("company_regnum"));
		MultipartFile mFile = mRequest.getFile("company_regnum_image");
		
		String memberId = String.valueOf(memberInfo.get("member_id"));
		param.put("member_id", memberId);
		
		//이동할 파일 패스
		String oldFilePath = (String) param.get("req_company_regnum_image");
		
		if(mFile != null) {
			// 기존 사업자 등록증 삭제
			s3.removeFile(memberFilePath);
			s3.removeFile(oldFilePath);
			
			// 사업자 등록증 업로드
			String uploadFilePath = FILE_MEMBER_BIZNUM_PATH + File.separator + memberId;
			uploadCompanyRegnumImage(param, uploadFilePath, mRequest);
			
		}else {
			String fileName = (String) param.get("req_company_regnum_file_name");
			//이동될 파일 패스
			String newFilePath = S3_FILE_DEFAULT_PATH + FILE_MEMBER_BIZNUM_PATH + File.separator + memberId + File.separator + fileName;
			//파일 이동
			
			Map<String, Object> fileInfo = s3.copyFile(oldFilePath, newFilePath);
			
			if(!CommonUtil.checkIsNull(fileInfo)) {
				param.put("company_regnum_image", fileInfo.get("file_path"));
				param.put("company_regnum_file_name", fileInfo.get("file_name"));
			}
			
			//파일 삭제
			//member에 있던 파일 삭제
			s3.removeFile(memberFilePath);
			//request에 있던 파일 삭제
			s3.removeFile(oldFilePath);
		}
		
		// 정보 수정
		if (memberMapper.modifyCompanyInfo(param) <= 0) {
			throw new RuntimeException();
		}
		
		// 수정 요청 상태 변경
		if (memberMapper.modifyRequestStatus(param) <= 0) {
			throw new RuntimeException();
		}
		
		// 수정 완료 이메일
		ModifyInfoCompleteTemplate template = new ModifyInfoCompleteTemplate();
		template.setName((String) memberInfo.get("uname"));
		template.setUid((String) memberInfo.get("uid"));
		template.setDate((String) memberInfo.get("request_date"));
				
		// 발송 정보 설정 후 전송
		AlarmData alarmData = new AlarmData((String) memberInfo.get("email"), template);
		alarmSender.sendEmail(alarmData);
		
		return respMap.getResponse();
	}
	
	/**
	 * 비밀번호 확인
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> hasPassword(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 입력한 비밀번호 암호화
		String confirmPw = (String) param.get("passwd");
		param.put("passwd", SHAUtil.encrypt(confirmPw, "SHA-256"));
		
		// 저장된 비밀번호
		String savePw = memberMapper.getSavePassword(param);
		
		// 비밀번호 확인
		if(savePw == null) {
			return respMap.getResponse(Code.MEMBER_PW_IS_NOT_MATCH);
		}
		
		return respMap.getResponse();
	}
	
	/**
	 * 종료되지 않은 광고가 있는지 체크
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getProcessingSg(Map<String, Object> param) throws Exception{
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 상태가 승인요청, 진행중인 광고 갯수 확인
		if(memberMapper.getProcessingSgCnt(param) > 0) {
			return respMap.getResponse(Code.MEMBER_PROCESSING_SG);
		}
		
		return respMap.getResponse();
	}
	
	/**
	 * 종료되지 않은 상품이 있는지 체크
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getProcessingProduct(Map<String, Object> param) throws Exception{
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 상태가 승인요청, 진행중인 광고 갯수 확인
		if(memberMapper.getProcessingSgCnt(param) > 0) {
			return respMap.getResponse(Code.MEMBER_PROCESSING_SG);
		}
		
		return respMap.getResponse();
	}
	
	
	/**
	 * 회원 탈퇴 요청
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> leaveRequestMember(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 상태가 승인요청, 진행중인 광고 갯수 확인
		if(memberMapper.getProcessingSgCnt(param) > 0) {
			return respMap.getResponse(Code.MEMBER_PROCESSING_SG);
		}
		
		// 입력한 비밀번호 암호화
		String passwd = (String) param.get("passwd");
		String enPasswd = SHAUtil.encrypt(passwd, "SHA-256");
		param.put("passwd", enPasswd);
		
		// 비밀번호 일치 확인 
		String savePasswd = memberMapper.getSavePassword(param);
		
		if(!enPasswd.equals(savePasswd)) {
			return respMap.getResponse(Code.MEMBER_PW_IS_NOT_MATCH);
		}
		
		// 회원 탈퇴 요청 처리
		if(memberMapper.leaveRequestMember(param) <= 0) {
			throw new RuntimeException();
		}
		
		return respMap.getResponse();
	}
	
	/**
	 * 회원 탈퇴 승인
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> leaveRequestAccept(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 상태가 승인요청, 진행중, 일시정지인 광고 갯수 확인
		if(memberMapper.getProcessingSgCnt(param) > 0) {
			throw new RuntimeException();
		}
		
		// 입력한 비밀번호 암호화
		String confirmPw = (String) param.get("passwd");
		param.put("passwd", SHAUtil.encrypt(confirmPw, "SHA-256"));
		
		// 저장된 비밀번호
		String savePw = memberMapper.getSavePassword(param);
		
		// 비밀번호 확인
		if(savePw == null) {
			return respMap.getResponse(Code.MEMBER_PW_IS_NOT_MATCH);
		}
		
		// 회원 탈퇴 승인
		if(memberMapper.leaveRequestAccept(param) <= 0) {
			throw new RuntimeException();
		}
		
		Map<String, Object> memberData = memberMapper.getMemberData(param);
		if(!CommonUtil.checkIsNull(memberData)) {
			// 이메일 발송
			String memberEmail = (String) memberData.get("email");
			if(memberEmail != null) {
				// TODO 임시
				//emailService.sendEmail(memberEmail, "[Mocafe] 회원 탈퇴 요청 승인 안내", "승인되었습니다.");
			}
			
			// 대행사 계정인 경우
			String memberUtype = (String) memberData.get("utype");
			int agencyId = Integer.valueOf(String.valueOf(param.get("login_agency_id")));
			if(MemberType.getType(memberUtype).equals(MemberType.AGENCY) && agencyId == 0) {
				// 대행사 계정중 직접 가입한 경우 = 최고관리자인 경우 
				// 하위 계정 사용 불가 상태
				param.put("leave_reason", "대행사 최상위 계정 탈퇴 승인");
				agencyMemberMapper.setMemberLeave(param);
			}
		}
		
		return respMap.getResponse();
	}
	
	/**
	 * 회원 탈퇴 요청 목록 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getLeaveRequestList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		respMap.setBody("tot_cnt", memberMapper.leaveRequestMemberListCnt(param));
		respMap.setBody("list", memberMapper.leaveRequestMemberList(param));
		
		return respMap.getResponse();
	}
	
	/**
	 * 회원 목록 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 개수
		int total = memberMapper.getMemberListCnt(param);
		respMap.setBody("tot_cnt", total);
		
		List<Map<String, Object>> memberList = memberMapper.getMemberList(param);
		respMap.setBody("list", memberList);
		
		return respMap.getResponse();
	}
	/**
	 * 권한별 회원 수 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> roleMemberCnt(Map<String, Object> param)throws Exception{
		ResponseMap respMap = beanFactory.getResponseMap();
		
		//권한별 회원 수
		Map<String, Object> roleMemberCnt = memberMapper.getRoleMemberCnt(param);
		respMap.setBody("role_member_cnt", roleMemberCnt);
		
		return respMap.getResponse();
	}
	
	/**
	 * 회원 전체 목록 조회(관리자 제외)
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getMemberUserList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> memberList = memberMapper.getMemberUserList(param);
		respMap.setBody("list", memberList);
		
		// 개수
		int total = memberMapper.getMemberUserListCnt(param);
		respMap.setBody("tot_cnt", total);
		
		return respMap.getResponse();
	}
	
	/**
	 * 회원 전체목록 엑셀 다운로드
	 * @param param
	 * @param response
	 * @throws Exception
	 */
	public void getMemberUserExcel(Map<String, Object> param,  HttpServletResponse response) throws Exception{
		Workbook wb = new Workbook(response.getOutputStream(), "ExcelWriter", "1.0");
		Worksheet ws = wb.newWorksheet("sheet1");
		
		int row = 0;
		
		// title
		ws.value(row, 0, "구분");
		ws.value(row, 1, "회원명");
		ws.value(row, 2, "회원 ID");
		ws.value(row, 3, "가입 일자");
		ws.value(row, 4, "메일 주소");
		ws.value(row, 5, "E-mail 수신동의");
		ws.range(row, 0, row, 5).style().fillColor(Color.GRAY3).set();
		
		
		// col width 
		ws.width(0, 15);
		ws.width(1, 50);
		ws.width(2, 50);
		ws.width(3, 50);
		ws.width(4, 100);
		ws.width(5, 15);
		
		List<Map<String, Object>> memberList = memberMapper.getMemberUserList(param);
		
		for(Map<String, Object> memberInfo : memberList) {
			row++;
			
			//구분
			{
				String utype = String.valueOf(memberInfo.get("utype"));
				String utype_str = "";
				if(MemberType.getType(utype).equals(MemberType.AGENCY)){
					 utype_str = "대행사";
				}else if(MemberType.getType(utype).equals(MemberType.DEMAND)) {
					 utype_str = "광고주";
				}else if(MemberType.getType(utype).equals(MemberType.SUPPLY)) {
					 utype_str = "매체사";
				}
				ws.value(row, 0, utype_str);	
			}
			
			//회원명
			{
				String companyName = String.valueOf(memberInfo.get("company_name"));
				ws.value(row, 1, companyName);
			}
			
			//회원 ID
			{
				String uid = String.valueOf(memberInfo.get("uid"));
				ws.value(row, 2, uid);
			}
			
			//가입일자
			{
				String insertDate = String.valueOf(memberInfo.get("insert_date"));
				ws.value(row, 3, insertDate);
			}
			
			//이메일
			{
				String companyEmail = String.valueOf(memberInfo.get("company_email"));
				ws.value(row, 4, companyEmail);
			}
			
			//수신동의
			{
				String acceptEmail = String.valueOf(memberInfo.get("accept_email"));
				String accept_str = "비동의";
				
				if(acceptEmail.equals("Y")) {
					accept_str = "동의";
				}
				
				ws.value(row, 5, accept_str);
			}
			
			//취소선 
			{
				String status = String.valueOf(memberInfo.get("status"));
				if(!status.equals("A")) {
					ws.range(row, 0, row, 5).style().strikeThrough().set();
				}
			}
		}
		
		excelDownload(response, wb, ws, "전체회원리스트");
		
	}
	
	/**
	 * 회원 1명 상세 정보 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getDetail(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		Map<String, Object> memberDetail = memberMapper.getMemberData(param);

		if (memberDetail == null) {
			return respMap.getResponse(Code.NOT_EXIST_DATA);
		}
		
		// 광고주 => 직접 가입 / 대행사 통한 가입 여부 확인
		if(memberDetail.get("utype").equals("D")) {
			if(memberDetail.get("agency_id").equals((long) 0)) {
				memberDetail.put("with_agency", false);
			} else {
				memberDetail.put("with_agency", true);
			}
		}

		respMap.setBody("data", memberDetail);
		
		return respMap.getResponse();
	}
	/**
	 * 회원의 사업자 정보 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getMemberCompanyInfo(Map<String, Object> param) throws Exception {
		ResponseMap respMap= beanFactory.getResponseMap();
		
		Map<String, Object> companyInfo = memberMapper.getMemberCompanyInfo(param);
		
		if (companyInfo == null) {
			return respMap.getResponse(Code.NOT_EXIST_DATA);
		}
		
		respMap.setBody("data", companyInfo);
		
		return respMap.getResponse();
	}
	
	/**
	 * 회원 로그인 이력 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getLoginHistory(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();

		// 개수
		int total = memberMapper.getMemberLoginHistoryCnt(param);
		respMap.setBody("tot_cnt", total);
		
		List<Map<String, Object>> memberLoginHistoryList = memberMapper.getMemberLoginHistory(param);
		respMap.setBody("list", memberLoginHistoryList);
		
		return respMap.getResponse(); 
	}
	
	/**
	 * 아이디 유효성 체크 
	 * 영문, 숫자 혼용 6~15자
	 * @param param
	 * @return
	 * @throws Exception
	 */
	private boolean hasValidateId(Map<String, Object> param) throws Exception {
		Pattern pattern = Pattern.compile("^.*(?=^.{6,15}$)(?=.*\\d)(?=.*[a-zA-Z]).*$");
		Matcher matcher = pattern.matcher(String.valueOf(param.get("uid")));
		
		if (matcher.matches()) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 비밀번호 유효성 체크
	 * 특수문자 1자리, 영어 2자리, 숫자 N 총 10자리 이상
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public boolean hasValidatePw(String passwd) throws Exception {
		int cnt = 0;
		for (int i = 0; i < passwd.length(); i++) {
			if((64 < passwd.charAt(i) && passwd.charAt(i) < 91) || (96 < passwd.charAt(i) && passwd.charAt(i) < 123)) {
				cnt++;
			}
		}
		if(cnt < 2) {
			return false;
		}
		
		Pattern pattern = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]{10,}$");
		Matcher matcher = pattern.matcher(passwd);
		
		if (matcher.matches()) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 관리자 페이지 회원 리스트 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getStaffList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> staffList = memberMapper.getStaffList(param);
		int staffTotal = memberMapper.getStaffListCount(param);
		respMap.setBody("list", staffList);
		respMap.setBody("tot_cnt", staffTotal);
		
		return respMap.getResponse();
	}
	
	/**
	 * 관리자 수정이력 목록 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getStaffHistoryList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> list = memberMapper.getStaffHistoryList(param);
		
		respMap.setBody("list", list);
		return respMap.getResponse();
	}
	
	/**
	 * 관리자 등록
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> addStaff(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 1. 이메일 유효성 검사
		String email = (String) param.get("company_email");
		if(!ValidateUtil.checkEmail(email)) {
			return respMap.getResponse(Code.MEMBER_EMAIL_IS_NOT_VALID);
		}
		
		// 이메일 중복 체크
		if (memberMapper.hasDuplicateEmail(param) > 0) {
			return respMap.getResponse(Code.MEMBER_DUPLICATE_EMAIL);
		}
		
		// role_id가 관리자에서 사용하고 활성화된 권한구분인지 체크
		if(!param.get("role_id").equals("0")) { // 미지정
			if(memberMapper.isRoleStaff(param) <= 0) {
				return respMap.getResponse(Code.ADMIN_STAFF_ROLE_NOT_ACCEPT);
			}
		}
		
		// 아이디 유효성 체크
		if (!hasValidateId(param)) {
			return respMap.getResponse(Code.MEMBER_ID_IS_NOT_VALID);
		}
		
		// 아이디 중복 체크
		if (memberMapper.hasDuplicateId(param) > 0) {
			return respMap.getResponse(Code.MEMBER_DUPLICATE_ID);
		}
		
		// 2. 비밀번호 랜덤 생성
		String newPasswd = makeRandTempPasswd(10);
		String encPasswd = SHAUtil.encrypt(newPasswd, "SHA-256");
		param.put("passwd", encPasswd);
		
		// 관리자 생성
		if(memberMapper.addStaff(param) <= 0) {
			throw new RuntimeException();
		}
		
		// 3. 메일 보내기
		String uId = (String) param.get("uid");
		String uname = (String) param.get("uname");

		// 사용할 이메일 템플릿 객체 생성 후 값 설정
		StaffTempPasswordTemplate template = new StaffTempPasswordTemplate();
		template.setName(uname);
		template.setUid(uId);
		template.setTempPassword(newPasswd);
		template.setReturnUrl(MemberType.ADMIN.getModifyUrl());
		
		// 발송 정보 설정 후 전송
		AlarmData alarmData = new AlarmData(email, template);
		emailService.sendEmail(email, alarmData.getTitle(), alarmData.getContent());
		
		return respMap.getResponse();
	}
	
	/**
	 * 관리자 권한구분 수정
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> modifyStaffRole(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		Map<String, Object> staffDetail = memberMapper.getStaffDetail(param);
		// 존재하는 직원인지 체크
		if(CommonUtil.checkIsNull(staffDetail)) {
			throw new RuntimeException();
		}
		
		// 권한구분 변경
		String sRole = String.valueOf(staffDetail.get("role_id"));
		String role = String.valueOf(param.get("role_id"));
		
		if(!sRole.equals(role)) {
			// role_id가 관리자에서 사용하고 활성화된 권한구분인지 체크
			Map<String, Object> roleDetail = roleMapper.getStaffRoleDetail(param);
			String newRole = "";
			if(!role.equals("0")) {
				if(CommonUtil.checkIsNull(roleDetail)) {
					throw new RuntimeException();
				} else {
					newRole = (String) roleDetail.get("name");
				}
			} else {
				newRole = "미지정";
			}
			
			// 권한구분 변동 시 로그
			String oldRole = (String) staffDetail.get("name");
			if(oldRole == null || oldRole.isEmpty()) {
				oldRole = "미지정";
			}
			param.put("message", "구분 [" + oldRole + "] ▶ [" + newRole + "] 변경");
			
			if(memberMapper.addStaffHistory(param) < 1) {
				throw new RuntimeException();
			}
		}
		
		if(memberMapper.modifyStaffRole(param) < 1) {
			throw new RuntimeException();
		}
		
		return respMap.getResponse();
	}
	
	/**
	 * 관리자 본인 계정 수정 (권한구분은 x)
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> modifyMyInfo(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 내 정보 가져오기
        Map<String, Object> myInfo = memberMapper.getMyData(param);
        if (CommonUtil.checkIsNull(myInfo)) {
            return respMap.getResponse(Code.MEMBER_INFO_IS_NULL);
        }
		
        // 비밀번호 수정 확인 => 파라미터에 passwd, new_passwd, confirm_passwd 값이 있으면 수정 O
        String passwd = null;
        if (!CommonUtil.checkIsNull(param, "passwd") && !CommonUtil.checkIsNull(param, "new_passwd") && !CommonUtil.checkIsNull(param, "confirm_passwd")) {
            passwd = SHAUtil.encrypt(String.valueOf(param.get("passwd")), "SHA-256");
            param.put("passwd", passwd);
            
            // 기존 비밀번호 일치 확인
            if (!myInfo.get("passwd").equals(passwd)) {
                return respMap.getResponse(Code.MEMBER_PW_IS_NOT_MATCH);
            }
            
            // 비밀번호 변경
            String newPasswd = null;
            if (!CommonUtil.checkIsNull(param, "new_passwd")) {
                // 새 비밀번호 재입력 확인
                if (!param.get("new_passwd").equals(param.get("confirm_passwd"))) {
                    return respMap.getResponse(Code.MEMBER_NEW_PW_IS_NOT_MATCH);
                }
                
                // 새 비밀번호 유효성 체크
                if(!hasValidatePw((String) param.get("new_passwd"))) {
                    return respMap.getResponse(Code.MEMBER_PW_IS_NOT_VALID);
                } 
                
                newPasswd = SHAUtil.encrypt(String.valueOf(param.get("new_passwd")), "SHA-256");
                param.put("new_passwd", newPasswd);
            } 
            
        }
		
		// 3. 이미 기존 메일 (member 테이블에 있는 이메일)
		String companyEmail = (String) param.get("company_email");
		
		if (!companyEmail.equals(myInfo.get("company_email"))) {
            // 이메일 중복 확인
            if (memberMapper.hasDuplicateEmail(param) > 0) {
                return respMap.getResponse(Code.MEMBER_DUPLICATE_EMAIL);
            }
            
            // 이메일 인증 확인
            if (memberMapper.hasEmailAuth(param) == 0) {
                return respMap.getResponse(Code.MEMBER_AUTH_IS_NOT_MATCH);
            }
            
        }
		
		// 수정 (이메일, 비밀번호)
		if(memberMapper.modifyMyInfo(param) < 1) {
			throw new RuntimeException();
		}
		
		return respMap.getResponse();
	}

	/**
	 * 관리자 회원 삭제
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> removeStaff(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		
		
		// 비밀번호 체크
		String passwd = String.valueOf(param.get("passwd"));
		param.put("passwd", SHAUtil.encrypt(passwd, "SHA-256"));
		if(memberMapper.isPasswd(param) < 1) {
			return respMap.getResponse(Code.MEMBER_PW_IS_NOT_MATCH);
		}
		
		List<String> staffIdList = (List<String>) param.get("staff_id_list");
		
		for(String staffId: staffIdList) {
			// 삭제
			param.put("member_id", staffId);
			
			// 존재하는 직원인지 체크
			if(memberMapper.hasStaff(param) < 1) {
				throw new RuntimeException();
			}
			
			// 최고 관리자 계정인지 체크
			if(memberMapper.isSuperStaff(param) > 0) {
				throw new RuntimeException();
			}
			
			if(memberMapper.removeStaff(param) < 1) {
				throw new RuntimeException();
			}
			
			// history 쌓기
			param.put("message", "계정 삭제 / 사유 : " + param.get("leave_reason"));
			
			if(memberMapper.addStaffHistory(param) < 1) {
				throw new RuntimeException();
			}
		}
		
		
		
		return respMap.getResponse();
	}
	
	/**
	 * 로그인한 사용자의 메뉴 목록 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getMenuList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> menuList = sessionUtil.getLoginMenuList();
		menuList = menuService.getTreeMenuList(menuList);
		
		respMap.setBody("list", menuList);
		
		return respMap.getResponse();
	}

	/**
	 * 매체사 승인요청 목록
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getSupplyApproveList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		param.put("utype", MemberType.SUPPLY.getType());
		List<Map<String, Object>> list = memberMapper.getSupplyApproveList(param);
		respMap.setBody("list", list);
		
		int total = memberMapper.getSupplyApproveCnt(param);
		respMap.setBody("tot_cnt", total);
		
		return respMap.getResponse();
	}
	
	/**
	 * 매체 승인 / 거부
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> modifySupplyStatus(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 관리자 확인
		if(!param.get("login_utype").equals("A")) {
			throw new RuntimeException();
		}
		
		memberMapper.modifySupplyStatus(param);

		Map<String, Object> memberDetail = memberMapper.getMemberData(param);

		if(param.get("status").equals("A")) {
			// 사용할 이메일 템플릿 객체 생성 후 값 설정
			SupplyApproveTemplate template = new SupplyApproveTemplate();
			template.setName((String) memberDetail.get("uname"));
			template.setUid((String) memberDetail.get("uid"));
			template.setDate((String) memberDetail.get("approve_date"));
			
			// 발송 정보 설정 후 전송
			AlarmData alarmData = new AlarmData((String) memberDetail.get("email"), template);
			alarmSender.sendEmail(alarmData);
		}
		
		return respMap.getResponse();
	}
	
	/**
	 *  매체/상품 관리 > 분류 관리 > 매체사 목록 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getSupplyList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();

		List<Map<String, Object>> list = memberMapper.getSupplyList(param);
		respMap.setBody("list", list);
		
		return respMap.getResponse();
	}

	/**
	 * 광고주 조회 (대행사가 존재하면 대행사 정보도 함께 조회)
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getDemandList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> demandList = memberMapper.getDemandList(param);
		int totCnt = memberMapper.getDemandListCount(param);
		
		respMap.setBody("list", demandList);
		respMap.setBody("tot_cnt", totCnt);
		
		return respMap.getResponse();
	}
	
	/**
	 * 영어 대/소문자, 숫자, 특수문자 3가지 조합 n자리 랜덤 임시 비밀번호 생성
	 * @param len
	 * @return
	 * @throws Exception
	 */
	public String makeRandTempPasswd(int len) throws Exception {
		if(len > 0) {
			StringBuffer randStr = new StringBuffer();
			Random random = new Random();
			char[] symbolChar = {'@', '$', '!', '%', '*', '?', '#'};

			for (int i = 0; i < len; i++) {
				int type = random.nextInt(4);
				double mathRandom = Math.random();

				switch(type) {
				case 0:
					randStr.append((int)(mathRandom * 9 + 1));
					break;
				case 1:
					randStr.append((char)(mathRandom * 26 + 'a'));
					break;
				case 2:
					randStr.append((char)(mathRandom * 26 + 'A'));
					break;
				default: 
					randStr.append(symbolChar[random.nextInt(symbolChar.length)]);
					break;
				}
			}
			if(!hasValidatePw(randStr.toString())) {
				return makeRandTempPasswd(len);
			}

			return randStr.toString();
		} else {
			return "";
		}
	}
	
	/**
	 * 연락처 형식 변경
	 * @param param
	 * @throws Exception
	 */
	public String makeMobile(String mobile) throws Exception {
		String result = "";
		
		if (mobile.length() == 11) { // 010-1234-5678
			result = mobile.substring(0, 3) + "-" + mobile.substring(3, 7) + "-" + mobile.substring(7);
		} else if (mobile.length() == 8) { // 1588-1234
			result = mobile.substring(0, 4) + "-" + mobile.substring(4);
		} else if (mobile.startsWith("02")) { // 02-123-1234, 02-1234-1234
			if(mobile.length() == 9)  {
				result = mobile.substring(0, 2) + "-" + mobile.substring(2, 5) + "-" + mobile.substring(5);
			} else {
				result = mobile.substring(0, 2) + "-" + mobile.substring(2, 6) + "-" + mobile.substring(6);
			}
		} else { // 031-999-8888
			result = mobile.substring(0, 3) + "-" + mobile.substring(3, 6) + "-" + mobile.substring(6);
		}
		
		return result;
 	}
	
	/**
	 * 엑셀 다운로드
	 * @param response
	 * @param wb
	 * @param ws
	 * @param fileName
	 * @throws Exception
	 */
	private void excelDownload(HttpServletResponse response, Workbook wb, Worksheet ws, String fileName) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMdd");
		String date = sdf.format(new Date());
		fileName = date + "_" + fileName;
		
		response.setContentType("application/vnd.ms-excel");
		response.setCharacterEncoding("utf-8");
		response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8") + ".xlsx");
		
		ws.flush();
		ws.finish();
		wb.finish();
	}
}
