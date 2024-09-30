package com.mocafelab.web.member;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.mocafelab.web.ad.campaign.DemandCampaignMapper;
import com.mocafelab.web.ad.sg.DemandSgMapper;
import com.mocafelab.web.alarm.AlarmData;
import com.mocafelab.web.email.EmailService;
import com.mocafelab.web.email.template.StaffTempPasswordTemplate;
import com.mocafelab.web.enums.MemberStatus;
import com.mocafelab.web.enums.MemberType;
import com.mocafelab.web.file.S3Service;
import com.mocafelab.web.login.LoginMapper;
import com.mocafelab.web.login.LoginService;
import com.mocafelab.web.role.AgencyRoleMapper;
import com.mocafelab.web.vo.BeanFactory;
import com.mocafelab.web.vo.Code;
import com.mocafelab.web.vo.ResponseMap;

import lombok.extern.slf4j.Slf4j;
import net.newfrom.lib.file.S3UploadOption;
import net.newfrom.lib.file.UploadFileInfo;
import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.util.FileUtil;
import net.newfrom.lib.util.SHAUtil;
import net.newfrom.lib.util.ValidateUtil;

@Slf4j
@Service
@Transactional(rollbackFor = {Exception.class})
public class AgencyMemberService {
	
	@Autowired
	private AgencyMemberMapper agencyMemberMapper;
	
	@Autowired
	private MemberMapper memberMapper;
	
	@Autowired
	private AgencyRoleMapper agencyRoleMapper;

	@Autowired
	private DemandSgMapper demandSgMapper;
	
	@Autowired
	private DemandCampaignMapper demandCampaignMapper;
	
	@Autowired
	private LoginMapper loginMapper;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private MemberService memberService;
	
	@Autowired
	private LoginService loginService;
	
	@Autowired
	private BeanFactory beanFactory;
	
	@Autowired
	private S3Service s3;
	
	@Value("${file.path.upload.s3}")
	private String S3_FILE_DEFAULT_PATH;
	
	@Value("${file.path.member.biznum}")
	private String FILE_MEMBER_BIZNUM_PATH;
	
	/**
	 * 직원 리스트
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> list = agencyMemberMapper.getList(param);
		
		int listCnt = agencyMemberMapper.getListCnt(param);
		
		respMap.setBody("list", list);
		respMap.setBody("tot_cnt", listCnt);
		
		return respMap.getResponse();
	}
	
	/**
	 * 로그인 후 대행사(최고관리자) 정보 조회
	 * @param request
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getMyAgencyData(HttpServletRequest request, Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		param.put("agency_id", param.get("login_agency_id"));
		
		Map<String, Object> myData = agencyMemberMapper.getAgencyMaskingData(param);
		if (CommonUtil.checkIsNull(myData)) {
			return respMap.getResponse(Code.NOT_EXIST_DATA);
		} else {
			respMap.setBody("data",myData);
		}
	
		return respMap.getResponse();
	}
	
	/**
	 * 직원 등록
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> addMember(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 아이디 유효성 체크
		if (!hasValidateId(param)) {
			return respMap.getResponse(Code.MEMBER_ID_IS_NOT_VALID);
		}
		
		// 아이디 중복 체크
		if (memberMapper.hasDuplicateId(param) > 0) {
			return respMap.getResponse(Code.MEMBER_DUPLICATE_ID);
		}
		
		// 이메일 정규식 패턴 체크
		if (!ValidateUtil.checkEmail(String.valueOf(param.get("company_email")))) {
			return respMap.getResponse(Code.MEMBER_EMAIL_IS_NOT_VALID);
		}
		
		// 이메일 중복 체크
		if (memberMapper.hasDuplicateEmail(param) > 0) {
			return respMap.getResponse(Code.MEMBER_DUPLICATE_EMAIL);
		}
		// 임시 비밀번호 생성
		String tempPasswd = memberService.makeRandTempPasswd(10);
		String encTempPasswd = SHAUtil.encrypt(tempPasswd, "SHA-256");
		
		param.put("status", "A");
		param.put("passwd", encTempPasswd);
		param.put("utype", MemberType.AGENCY.getType());
		
		// login_agency_id의 정보
		Map<String, Object> getAgencyData = agencyMemberMapper.getAgencyData(param);
		param.put("company_name", getAgencyData.get("company_name"));
		
		
		// 직원 등록 
		if (agencyMemberMapper.addMember(param) <= 0) {
			throw new RuntimeException();
		}
		// 임시 비밀번호 이메일 전송
		sendTempPassword(param, tempPasswd);
		
		return respMap.getResponse();
	}
	
	/**
	 * 직원 정보 수정
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> modifyMember(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();

		// 기존 정보
		Map<String, Object> myInfo = memberMapper.getMyData(param);
		
		// 비밀번호 수정
		if (!CommonUtil.checkIsNull(param, "passwd") && !CommonUtil.checkIsNull(param, "new_passwd") && !CommonUtil.checkIsNull(param, "confirm_passwd")) {
			String passwd = SHAUtil.encrypt(String.valueOf(param.get("passwd")), "SHA-256");
			String newPasswd = (String)param.get("new_passwd");
			String confirmPasswd = (String)param.get("confirm_passwd");
			
			// 기존 비밀번호 일치 확인
			if (!myInfo.get("passwd").equals(passwd)) {
				return respMap.getResponse(Code.MEMBER_PW_IS_NOT_MATCH);
			}
			
			// 새 비밀번호 재입력 확인
			if (!newPasswd.equals(confirmPasswd)) {
				return respMap.getResponse(Code.MEMBER_NEW_PW_IS_NOT_MATCH);
			}
			
			// 새 비밀번호 유효성 체크
			if(!memberService.hasValidatePw(newPasswd)) {
				return respMap.getResponse(Code.MEMBER_PW_IS_NOT_VALID);
			} 
			param.put("new_passwd", SHAUtil.encrypt(newPasswd, "SHA-256"));
		}
		
		// 정보 수정
		if(agencyMemberMapper.modifyMember(param) <= 0) {
			throw new RuntimeException();
		}
		return respMap.getResponse();
	}
	
	/**
	 * 직원 삭제
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> removeMember(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		param.put("status", "L"); // 탈퇴
		
		// 입력한 비밀번호 암호화
		String passwd = (String) param.get("passwd");
		String enPasswd = SHAUtil.encrypt(passwd, "SHA-256");
		param.put("passwd", enPasswd);
		
		// 비밀번호 일치 확인 
		String savePasswd = memberMapper.getSavePassword(param);
		
		if(!enPasswd.equals(savePasswd)) {
			return respMap.getResponse(Code.MEMBER_PW_IS_NOT_MATCH);
		}
		
		List<String> staffIdList = (List<String>) param.get("staff_id_list");
		
		for(String staffId : staffIdList) {
			// 삭제
			param.put("member_id", staffId);
			
			agencyRoleMapper.removeStaffAccessDsp(param);
			
			if(agencyMemberMapper.removeMember(param) <= 0) {
				throw new RuntimeException();
			}
			// 삭제 로그
			addRemoveMemberHistory(param);
		}
		
		
		
		return respMap.getResponse();
	}
	
	/**
	 * 대행사 소속 광고주 목록 (팀, 직원 중복 허용)
	 * @param param
	 * @return
	 * @throws Exception 
	 */
	public Map<String, Object> getDemandList(Map<String, Object> param) throws Exception {
		ResponseMap responseMap = beanFactory.getResponseMap();
		
		log.info("param : {}", param);
		
		Map<String,Object> agencyMember = agencyMemberMapper.getAgencyMember(param);
		if (CommonUtil.checkIsNull(agencyMember)) {
			return responseMap.getErrResponse();
		}
		
		List<Map<String,Object>> demandList = agencyMemberMapper.getDemandList(param);
		int demandListCount = agencyMemberMapper.getDemandListCount(param);
		
		responseMap.setBody("list", demandList);
		responseMap.setBody("tot_cnt", demandListCount);
		
		return responseMap.getResponse();
	}
	
	/**
	 * 구분의 직원 리스트
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getRoleStaffList(Map<String, Object> param) throws Exception {
		ResponseMap responseMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> roleStaffList = agencyMemberMapper.getRoleStaffList(param);
		int roleStaffCnt = agencyMemberMapper.getRoleStaffListCnt(param);
		
		responseMap.setBody("list", roleStaffList);
		responseMap.setBody("tot_cnt", roleStaffCnt);
		
		return responseMap.getResponse();
	}
	
	/**
	 * 광고주별 담당 직원 리스트
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getDemandAccessStaffList(Map<String, Object> param) throws Exception {
		ResponseMap responseMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> accessStaffList = agencyMemberMapper.getDemandAccessStaffList(param);
		responseMap.setBody("list", accessStaffList);
		
		return responseMap.getResponse();
	}
	/**
	 * 광고주별 담당 직원 수정
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> modifyDemandAccessStaff(Map<String, Object> param) throws Exception {
		ResponseMap responseMap = beanFactory.getResponseMap();
		
		// 로그 등록
		addAgencyMemberChangeHistory(param);
		
		agencyRoleMapper.removeStaffAccessDsp(param);

		List<Map<String, Object>> staffList = (List<Map<String,Object>>) param.get("staff_json");
		//직원 리스트 없으면 담당직원 모두 삭제 
		if(!staffList.isEmpty()) {
			for(Map<String, Object> staffId : staffList) {
				param.put("staff_id", staffId.get("staff_id"));
				
				//이미 등록 되어있는지 확인
				//대행사에 소속된 직원인지 체크
				if(agencyMemberMapper.hasAgencyStaff(param) <= 0) {
					throw new RuntimeException();
				}
				//등록
				if(agencyRoleMapper.addStaffAccessDsp(param) <= 0 ) {
					throw new RuntimeException();
				}
			}
		}
		
		return responseMap.getResponse();
	}
	
	
	/**
	 * 구분별 담당 광고주 목록
	 * @param param
	 * @return
	 * @throws Exception 
	 */
	public Map<String, Object> getPartDemandList(Map<String, Object> param) throws Exception {
		ResponseMap responseMap = beanFactory.getResponseMap();
		
		Map<String,Object> agencyMember = agencyMemberMapper.getAgencyMember(param);
		if (CommonUtil.checkIsNull(agencyMember)) {
			return responseMap.getErrResponse();
		}
		
		List<Map<String,Object>> partDemandList = agencyMemberMapper.getPartDemandList(param);
		
		responseMap.setBody("list", partDemandList);
		
		return responseMap.getResponse();
	}
	
	/**
	 * 개인별 담당 광고주 목록
	 * @param param
	 * @return
	 * @throws Exception 
	 */
	public Map<String, Object> getPersonalDemandList(Map<String, Object> param) throws Exception {
		ResponseMap responseMap = beanFactory.getResponseMap();
		
		//조회 하려는 member_id(직원)의 데이터 조회
		Map<String, Object> memberData = agencyRoleMapper.getStaffData(param);
		
		//없으면 직원이 아니거나 대행사 마스터 계정
		if(memberData == null || memberData.isEmpty()) {
			return responseMap.getResponse(Code.NOT_EXIST_DATA);
		}
		
		param.put("role_id", memberData.get("role_id"));
		
		List<Map<String, Object>> demandList = new ArrayList<>();
		if(agencyRoleMapper.hasStaffAccessDsp(param) > 0 ) {
			demandList = agencyMemberMapper.getPersonalDemandList(param);
		}else {
			demandList = agencyMemberMapper.getPartDemandList(param);
		}
		
		responseMap.setBody("list", demandList);
		return responseMap.getResponse();
	}
	
	/**
	 * 대행사 로그인된 직원의 구분별 담당 광고주 목록
	 * @param param
	 * @return
	 */
	public Map<String, Object> getMemberPartDemandList(Map<String, Object> param) {
		ResponseMap responseMap = beanFactory.getResponseMap();
		
		List<Map<String,Object>> demandList = agencyMemberMapper.getMemberPartDemandList(param);
		
		responseMap.setBody("list", demandList);
		
		return responseMap.getResponse();
	}
	
	/**
	 * 대행사 로그인된 직원의 개인별 담당 광고주 목록
	 * @param param
	 * @return
	 */
	public Map<String, Object> getMemberPersonalDemandList(Map<String, Object> param) {
		ResponseMap responseMap = beanFactory.getResponseMap();
		
		List<Map<String,Object>> demandList = agencyMemberMapper.getMemberPersonalDemandList(param);
		
		responseMap.setBody("list", demandList);
		
		return responseMap.getResponse();
	}
	
	
	/**
	 * 종료되지 않은 광고가 있는지 체크
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getProcessingSgByAgency(Map<String, Object> param) throws Exception{
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 상태가 승인요청, 진행중인 광고 갯수 확인
		if(agencyMemberMapper.getProcessingSgCntByAgency(param) > 0) {
			return respMap.getResponse(Code.MEMBER_PROCESSING_SG);
		}
		
		return respMap.getResponse();
	}
	
	/**
	 * 대행사 소속 광고주의 계정 삭제
	 * @param param
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> removeDemand(Map<String, Object> param) throws Exception {
		ResponseMap responseMap = beanFactory.getResponseMap();
		
		// 로그인한 대행사 or 직원 조회
		Map<String,Object> agencyMember = agencyMemberMapper.getAgencyMember(param);
		if (CommonUtil.checkIsNull(agencyMember)) {
			throw new RuntimeException();
		}
		
		// 입력한 비밀번호 암호화
		String confirmPw = (String) param.get("passwd");
		param.put("passwd", SHAUtil.encrypt(confirmPw, "SHA-256"));
		
		// 저장된 비밀번호
		String savePw = memberMapper.getSavePassword(param);
		
		// 비밀번호 확인
		if(savePw == null) {
			return responseMap.getResponse(Code.MEMBER_PW_IS_NOT_MATCH);
		}
		
		List<String> demandList = (List<String>)param.get("demand_list");
		
		for(String demandId : demandList) {
			param.put("member_id", demandId);
			
			addRemoveMemberHistory(param);
			
			// 계정 탈퇴 요청
			if (agencyMemberMapper.removeDemand(param) < 1) {
				throw new RuntimeException();
			}	
		}
		
		return responseMap.getResponse();
	}
	
	/**
	 * 대행사 소속 광고주의 계정 삭제 체크
	 * @param param
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> removeDemandValidate(Map<String, Object> param) throws Exception {
		ResponseMap responseMap = beanFactory.getResponseMap();
		
		// 로그인한 대행사 or 직원 조회
		Map<String,Object> agencyMember = agencyMemberMapper.getAgencyMember(param);
		
		if (CommonUtil.checkIsNull(agencyMember)) {
			throw new RuntimeException();
		}
		
		// 입력한 비밀번호 암호화
		String confirmPw = (String) param.get("passwd");
		param.put("passwd", SHAUtil.encrypt(confirmPw, "SHA-256"));
		
		// 저장된 비밀번호
		String savePw = memberMapper.getSavePassword(param);
		
		// 비밀번호 확인
		if(savePw == null) {
			return responseMap.getResponse(Code.MEMBER_PW_IS_NOT_MATCH);
		}
		
		List<String> demandList = (List<String>)param.get("demand_list");
		
		for(String demandId : demandList) {
			param.put("member_id", demandId);
			
			// 종료되지 않은 광고가 있는지 체크 대행사 광고주 용
			if(agencyMemberMapper.getProcessingSgCntByMember(param) > 0) {
				return responseMap.getResponse(Code.MEMBER_PROCESSING_SG);
			}
		}
		
		return responseMap.getResponse();
	}
	
	/**
	 * 대행사 소속 광고주 목록
	 * @param param
	 * @return
	 */
	public Map<String, Object> getAgencyDemandList(Map<String, Object> param) {
		ResponseMap responseMap = beanFactory.getResponseMap();
		
		String loginId = String.valueOf(param.get("login_id"));
		String agencyId = String.valueOf(param.get("login_agency_id"));
		
		if (!loginId.equals(agencyId)) {
			param.put("staff_id", loginId);
		}
		
		responseMap.setBody("list", agencyMemberMapper.getAgencyDemandList(param));
		
		return responseMap.getResponse();
	}
	
	/**
	 * 대행사 소속 담당자 목록
	 * @param param
	 * @return
	 */
	public Map<String, Object> getAgencyStaffList(Map<String, Object> param) {
		ResponseMap responseMap = beanFactory.getResponseMap();
		
		responseMap.setBody("list", agencyMemberMapper.getAgencyStaffList(param));
		
		return responseMap.getResponse();
	}
	
	/**
	 * 대행사 소속 광고주의 전체 캠페인 리스트
	 * @param param
	 * @return
	 */
	public Map<String, Object> getDemandCampaignList(Map<String, Object> param) {
		ResponseMap responseMap = beanFactory.getResponseMap();
		
		// 로그인한 대행사 or 직원 조회
		Map<String,Object> agencyMember = agencyMemberMapper.getAgencyMember(param);
		if (CommonUtil.checkIsNull(agencyMember)) {
			return responseMap.getErrResponse();
		}
		
		// 대행사 마스터 계정이 아니면 staff_id 를 지정
		if (Integer.parseInt(String.valueOf(agencyMember.get("agency_id"))) != 0) {
			param.put("staff_id", agencyMember.get("member_id"));
		}
		
		List<Map<String,Object>> demandList = agencyMemberMapper.getAgencyDemandStaffList(param);
		int agencyDemandStaffCount = agencyMemberMapper.getAgencyDemandStaffCount(param);
		
		// 광고주의 캠페인 목록
		for (Map<String,Object> demandMap : demandList) {
			param.put("demand_id", demandMap.get("demand_id"));
			
			List<Map<String,Object>> campaignList = agencyMemberMapper.getDemandCampaignList(param);
			
			demandMap.put("campaign_list", campaignList);
			
			// 캠페인에 해당하는 광고 목록
			for (Map<String,Object> campaignMap : campaignList) {
				Long campaignId = (Long) campaignMap.get("campaign_id");
				if (campaignId != null) {
					param.put("campaign_id", campaignId);
					List<Map<String,Object>> campaignSgList = agencyMemberMapper.getCampaignSgList(param);
					
					campaignMap.put("sg_list", campaignSgList);
				}
			}
		}
		
		responseMap.setBody("list", demandList);
		responseMap.setBody("total_count", agencyDemandStaffCount);
		
		return responseMap.getResponse();
	}
	
	/**
	 * 대행사 소속 광고주의 전체 캠페인 및 광고 리스트 (집행요청관리)
	 * @param param
	 * @return
	 */
	public Map<String, Object> getDemandCostList(Map<String, Object> param) {
		ResponseMap responseMap = beanFactory.getResponseMap();
		
		// 로그인한 대행사 or 직원 조회
		Map<String,Object> agencyMember = agencyMemberMapper.getAgencyMember(param);
		if (CommonUtil.checkIsNull(agencyMember)) {
			return responseMap.getErrResponse();
		}
		
		// 대행사 마스터 계정이 아니면 staff_id 를 지정
		if (Integer.parseInt(String.valueOf(agencyMember.get("agency_id"))) != 0) {
			param.put("staff_id", agencyMember.get("member_id"));
		}
		
		List<Map<String,Object>> demandCostList = agencyMemberMapper.getDemandCostList(param);
		int totalCount = agencyMemberMapper.getDemandCostListCount(param);
		
		responseMap.setBody("list", demandCostList);
		responseMap.setBody("total_count", totalCount);
		
		return responseMap.getResponse();
	}
	
	/**
	 * 광고주 계정 등록(생성)
	 * @param param
	 * @return
	 * @throws Exception 
	 */
	public Map<String, Object> addDemand(Map<String, Object> param, MultipartHttpServletRequest mRequest) throws Exception {
		ResponseMap responseMap = beanFactory.getResponseMap();
		
		Map<String,Object> agencyMember = agencyMemberMapper.getAgencyMember(param);
		if (CommonUtil.checkIsNull(agencyMember)) {
			throw new RuntimeException();
		}
		
		// 아이디 중복 체크
		if (memberMapper.hasDuplicateId(param) > 0) {
			return responseMap.getResponse(Code.MEMBER_DUPLICATE_ID);
		}
		
		// 이메일 정규식 패턴 체크
		if (!ValidateUtil.checkEmail(String.valueOf(param.get("company_email")))) {
			return responseMap.getResponse(Code.MEMBER_EMAIL_IS_NOT_VALID);
		}
		
		// 이메일 중복 체크
		if (memberMapper.hasDuplicateEmail(param) > 0) {
			return responseMap.getResponse(Code.MEMBER_DUPLICATE_EMAIL);
		}
		
		// 비밀번호 암호화
		String tempPassword = memberService.makeRandTempPasswd(10);
		String encryptPassword = SHAUtil.encrypt(tempPassword, "SHA-256");

		int roleId = agencyMemberMapper.defaultDemandRole(param);
		
		param.put("email", param.get("company_email"));
		param.put("role_id", roleId);
		param.put("passwd", encryptPassword);
		param.put("status", MemberStatus.OK.getStatus());
		param.put("temp_passwd_yn", "Y");
		
		// 회원 등록
		Map<String, Object> memberInfo = agencyMemberMapper.addDemand(param);
		
		if(CommonUtil.checkIsNull(memberInfo)) {
			throw new RuntimeException();
		}
		
		param.put("member_id", memberInfo.get("member_id"));
		
		
		String filePath = FILE_MEMBER_BIZNUM_PATH + File.separator + memberInfo.get("member_id");
		
		// s3 저장
		saveS3File(param, filePath, mRequest);
		
		// 사업자 등록증 경로 등록
		if(memberMapper.addMemberBiznumPath(param) < 0) {
			s3.removeFile((String) param.get("company_regnum_image"));
		}
		param.put("demand_id", memberInfo.get("member_id"));
		param.put("member_id", param.get("login_agency_id"));
		
		//대행사 최고관리자는 모든 생성한 광고주 모두 담당한다.
		if(agencyRoleMapper.addStaffAccessDsp(param) < 1) {
			throw new RuntimeException();
		}
		
		// 임시 비밀번호 이메일 발송
		sendTempPassword(param, tempPassword);
		
		return responseMap.getResponse();
	}
	
	/**
	 * 임시 비밀번호 발송
	 * @param param
	 * @param tempPassword
	 * @throws Exception
	 */
	public void sendTempPassword(Map<String, Object> param, String tempPassword) throws Exception {
		String name = (String) param.get("uname");
		String uid = (String) param.get("uid");
		String company_email = (String) param.get("company_email");
		
		// 사용할 이메일 템플릿 객체 생성 후 값 설정
		StaffTempPasswordTemplate template = new StaffTempPasswordTemplate();
		template.setName(name);
		template.setUid(uid);
		template.setTempPassword(tempPassword);
		template.setReturnUrl(MemberType.AGENCY.getModifyUrl());
		
		// 발송 정보 설정 후 전송
		AlarmData alarmData = new AlarmData(company_email, template);
		emailService.sendEmail(company_email, alarmData.getTitle(), alarmData.getContent());
	}

	/**
	 * s3 파일 저장 후 업로드 경로 추출
	 * @param mRequest
	 * @param param
	 * @return
	 * @throws Exception 
	 */
	private void saveS3File(Map<String, Object> param, String filePath, MultipartHttpServletRequest mRequest) throws Exception {
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
	 * 대행사 직원, 광고주 삭제 로그
	 * @param param
	 */
	private void addRemoveMemberHistory(Map<String, Object > param) throws Exception {
		param.put("type", "C");
		param.put("kind", "MHK006");
		param.put("message", param.get("leave_reason"));
		
		String historyMsg = createHistoryMsg(param);
		
		param.put("message", historyMsg);
		
		// 삭제 히스토리
		if(agencyMemberMapper.addMemberModifyHistory(param) < 1) {
			throw new RuntimeException();
		}
	}
	
	/**
	 * 광고주 담당자 변경 로그
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void addAgencyMemberChangeHistory(Map<String, Object> param) throws Exception {
		// 기존 담당자
		List<Map<String, Object>> originAgencyMember = agencyMemberMapper.getDemandAgencyMemberList(param);
		
		// 변경되는 담당자
		List<Map<String, Object>> modifyAgencyMember = (List<Map<String, Object>>) param.get("staff_json");
		
		// 담당자 정보 맵핑
		if(!modifyAgencyMember.isEmpty()) {
			param.put("staff_id_list", modifyAgencyMember);
			modifyAgencyMember = agencyMemberMapper.getAgencyMemberList(param);
		}

		List<Map<String, Object>> historyList = this.<Long, Long>partitionByRemoveAdd(originAgencyMember, modifyAgencyMember, "staff_id", (v1, v2) -> {
			return v1.equals(v2);
		});
		
		if(historyList.size() > 0) {
			historyList.forEach(history -> {
				history.put("kind", "MHK007");
				history.put("message", history.get("uname"));
			});
		    String historyMsg = createHistoryMsg(historyList);
		    
			param.put("message", historyMsg);
			
			// demand_id > member_id로 변경 하기 위해 Map생성
			Map<String, Object> historyMap = new HashMap<>();
			historyMap.putAll(param);
			historyMap.put("member_id", historyMap.get("demand_id"));
		    
		    // 히스토리 등록
			if(agencyMemberMapper.addMemberModifyHistory(historyMap) < 1) {
				throw new RuntimeException();
			}
		}
	}
	
	/**
	 * 기존 리스트와 변경된 리스트를 비교하여 항목에 추가된 항목인지 삭제된 항목인지 구분값 저장
	 * D: 삭제, A: 추가
	 * @param <T>
	 * @param <U>
	 * @param originList 기존 리스트
	 * @param modifyList 변경된 리스트
	 * @param key 비교할 키값
	 * @param predicate 비교 로직 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T,U> List<Map<String, Object>> partitionByRemoveAdd(List<Map<String, Object>> originList, List<Map<String, Object>> modifyList, String key, BiPredicate<T,U> predicate) throws Exception {
		List<Map<String, Object>> historyList = new ArrayList<>();
		
		if((originList == null || originList.isEmpty()) && (modifyList == null ||modifyList.isEmpty())) {
			return historyList;
		}
		
		// 기존 리스트 사이즈가 0이고 변경된 리스트 사이즈가 0보다 크면 모두 추가된 항목
		if(originList.size() <= 0 && modifyList.size() > 0) {
			modifyList.forEach(modify -> {
				modify.put("type", "A");
			});
			return modifyList;
		} 
		
		// 기존 리스트 사이즈가 0보다 크고 변경된 리스트 사이즈가 0이면 모두 삭제된 항목
		if(originList.size() > 0 && modifyList.size() <= 0) {
			originList.forEach(origin -> {
				origin.put("type", "D");
			});
			return originList;
		}
		
		// 삭제된 리스트 
		List<Map<String, Object>> removeList = originList.stream().filter(modify -> modifyList.stream().noneMatch(origin -> {
			return predicate.test((T)origin.get(key), (U)modify.get(key));
		})).peek(remove -> {
			remove.put("type", "D");
		}).collect(Collectors.toList());
		
		// 추가된 리스트
		List<Map<String, Object>> addList = modifyList.stream().filter(origin -> originList.stream().noneMatch(modify -> {
			return predicate.test((T)origin.get(key), (U)modify.get(key));
		})).peek(add -> {
			add.put("type", "A");
		}).collect(Collectors.toList());
		
		historyList.addAll(removeList);
		historyList.addAll(addList);
		
		return historyList;
	}
	
	/**
	 * 히스토리 메시지 생성
	 * @param historyList
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public String createHistoryMsg(List<Map<String, Object>> historyList) throws Exception {
		if(historyList == null || historyList.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < historyList.size(); i ++) {
			Map<String, Object> history = historyList.get(i);
			
			String kind = (String)history.get("kind");
			String type = (String)history.get("type");
			String message = (String)history.get("message");
			String kindName = "";
			String typeName = "";
			
			if(kind.equals("MHK001")) {
				kindName = "광고";
			} else if(kind.equals("MHK002")) {
				kindName = "캠페인";
			} else if(kind.equals("MHK003")) {
				kindName = "회원정보";
			} else if(kind.equals("MHK004")) {
				kindName = "구분";
			} else if(kind.equals("MHK005")) {
				kindName = "접근권한";
			} else if(kind.equals("MHK006")) {
				kindName = "계정삭제";
			} else if(kind.equals("MHK007")) {
				kindName = "담당자";
			} else if(kind.equals("MHK008")) {
				kindName = "담당 광고주";
			} 
			
			if(type.equals("C")) {
				typeName = "변경";
			} else if(type.equals("A")) {
				typeName = "추가";
			} else if(type.equals("D")) {
				typeName = "삭제";
			}
			
			if(kind.equals("MHK006")) {
				sb.append(kindName + " / 사유 : " + message);
			} else {
				sb.append(kindName + " " + message + " " + typeName);
			}
			
			if(i < historyList.size() - 1) {
				sb.append(System.lineSeparator());
			}
		}
		return sb.toString();
	}
	
	private String createHistoryMsg(Map<String, Object> history) throws Exception {
		if(history == null || history.isEmpty()) {
			return "";
		}
		List<Map<String, Object>> historyList = new ArrayList<Map<String, Object>>();
		historyList.add(history);

		return createHistoryMsg(historyList);
	}

	/**
	 * 활동 이력 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getModifyHistoryList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> historyList = agencyMemberMapper.getMemberModifyHistoryList(param);
		
		respMap.setBody("list" , historyList);

		return respMap.getResponse();
	}
	
	/**
	 * 로그인 계정 대행사 소속 광고주 계정으로 로그인  
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> demandLoginByAgency(Map<String, Object> param, HttpServletRequest request, HttpServletResponse response) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();

		String agencyMemberUtype = String.valueOf(param.get("login_utype"));
		
		// 대행사 계정 정보 
		Map<String, Object> agencyMember = agencyMemberMapper.getAgencyMember(param);
		if(CommonUtil.checkIsNull(agencyMember) || CommonUtil.checkIsNull(agencyMember, "utype")) {
			return respMap.getResponse(Code.LOGIN_FAIL);
		}
		
		// 대행사 계정인지 확인
		if(!MemberType.getType(agencyMemberUtype).equals(MemberType.AGENCY)) {
			return respMap.getResponse(Code.LOGIN_FAIL);
		}
		
		// 광고주 계정 확인 
		Map<String, Object> demandMember = agencyMemberMapper.getDemandMember(param);
		if(CommonUtil.checkIsNull(demandMember) || CommonUtil.checkIsNull(demandMember, "agency_id")) {
			return respMap.getResponse(Code.LOGIN_FAIL);
		}
		
		Map<String, Object> demandAccessData = loginMapper.getAccessData(demandMember);
		
		int demandMemberId = Integer.valueOf(String.valueOf(demandMember.get("member_id")));
		int demandAgencyId = Integer.valueOf(String.valueOf(demandMember.get("agency_id")));
		
		// 로그인 가능한 상태인지 확인
		// 로그인 프로세스 
		Code code = Code.OK;
		
		// 로그인 가능 여부 검사
		code = loginService.isLoginEnable(param, demandMember);
		
		// 중복 로그인 검사
		if(loginService.hasDuplicateLogin(param, demandAccessData) == true) {
			return respMap.getResponse(Code.LOGIN_IS_DUPLICATED);
		}
		
		if(!CommonUtil.checkIsNull(demandAccessData) && !CommonUtil.checkIsNull(demandAccessData, "access_token")) {
			param.put("access_token", demandAccessData.get("access_token"));
		}
		
		if(code.equals(Code.OK)) {
			// 대행사 로그인 토큰 제거
			Map<String, Object> agencyAccessParam = new HashMap<>() {{
				put("member_id", agencyMember.get("member_id"));
				put("user_token", param.get("user_token"));
			}};
			loginMapper.removeAccessToken(agencyAccessParam);
			
			loginService.setLoginSuccess(param, demandMember, request, response);
		} else {
			param.put("uid", demandMember.get("uid"));
			loginService.setLoginFail(param);
			return respMap.getResponse(code);
		}

		return respMap.getResponse();
	}
	
	/**
	 * 대행사 소속 광고주의 캠페인 및 광고 정보
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getDemandSgList(Map<String, Object> param) {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> demandList = agencyMemberMapper.getAgencyWithDemandList(param);
		if(demandList.size() > 0) {
			param.put("demandList", demandList);
		}
		Map<String, Object> campaignTotal = demandCampaignMapper.getCountCampaign(param);
		
		List<Map<String, Object>> demandListWithSg = new ArrayList<>();
		
		if(param.get("list_type").equals("withCpSg")) {
			List<Map<String, Object>> campaignList = demandCampaignMapper.getCampaignListOfDemand(param);
			List<Map<String, Object>> sgList = demandSgMapper.getList(param);
			// 캠페인에 광고 정보를 먼저 담는다.
			for(Map<String, Object> campaign : campaignList) {
				List<Map<String, Object>> campaignListWithSg = new ArrayList<>();
				
				for(Map<String, Object> sg : sgList) {
					if(campaign.get("campaign_id").equals(sg.get("campaign_id"))) {
						campaignListWithSg.add(sg);
					}
				}
				campaign.put("sg_list", campaignListWithSg);
			}
			
			for(Map<String, Object> demand : demandList) {
				List<Map<String, Object>> demandListWithCampaign = new ArrayList<>();
				
				for(Map<String, Object> campaign : campaignList) {
					if(demand.get("dsp_id").equals(campaign.get("member_id"))) {
						demandListWithCampaign.add(campaign);
					}
				}
				demand.put("campaign_list", demandListWithCampaign);
				demandListWithSg.add(demand);
			}
			respMap.setBody("list", demandListWithSg);
		} else if(param.get("list_type").equals("sg")){
			// 광고주, 담당자
			for(Map<String, Object> demand : demandList) { // 광고주, 담당자
				param.put("login_id", demand.get("dsp_id"));
				
				List<Map<String, Object>> sgList = demandSgMapper.getList(param);
				List<Map<String, Object>> sgListWithDemand = new ArrayList<>();
				
				for(Map<String, Object> sg: sgList) {
					if(sg.get("member_id").equals(demand.get("dsp_id"))) {
						sgListWithDemand.add(sg);
					}
				}
				demand.put("sg_list", sgListWithDemand);
				demandListWithSg.add(demand);
			}
			respMap.setBody("list", demandListWithSg);
		} else if(param.get("list_type").equals("request")) {
			demandListWithSg = agencyMemberMapper.getDemandCampaginSgList(param);
			respMap.setBody("list", demandListWithSg);
		}
		respMap.setBody("total", campaignTotal);

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
}
