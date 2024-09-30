package com.mocafelab.web.ad.sg;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.mocafelab.web.ad.campaign.DemandCampaignMapper;
import com.mocafelab.web.ad.schedule.DemandScheduleMapper;
import com.mocafelab.web.ad.schedule.ScheduleMapper;
import com.mocafelab.web.common.CommonMapper;
import com.mocafelab.web.enums.ModifyHistory;
import com.mocafelab.web.enums.PayType;
import com.mocafelab.web.enums.SgPageSize;
import com.mocafelab.web.enums.SgRatio;
import com.mocafelab.web.file.S3Service;
import com.mocafelab.web.member.DemandMemberService;
import com.mocafelab.web.product.ProductMapper;
import com.mocafelab.web.vo.BeanFactory;
import com.mocafelab.web.vo.Code;
import com.mocafelab.web.vo.ResponseMap;

import lombok.extern.slf4j.Slf4j;
import net.newfrom.lib.file.UploadFileInfo;
import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.util.FileUtil;

/**
 * 캠페인내 광고 관련 
 * @author mure96
 *
 */
@Transactional(rollbackFor = {Exception.class})
@Service
@Slf4j
@SuppressWarnings({"unchecked", "serial", "rawtypes"})
public class DemandSgService {
	
	@Autowired
	private BeanFactory beanFactory;
	
	@Autowired
	private DemandSgMapper demandSgMapper;
	
	@Autowired
	private CommonMapper commonMapper;
	
	@Autowired
	private DemandMemberService demandMemberService;
	
	@Autowired
	private ScheduleMapper scheduleMapper;
	
	@Autowired
	private ProductMapper productMapper;
	
	@Autowired
	private DemandScheduleMapper demandScheduleMapper;
	
	@Autowired
	private DemandCampaignMapper demandCampaignMapper;
	
	@Autowired
	private S3Service s3Service;
	
	@Value("${file.path.upload.s3}")
	private String S3_FILE_DEFAULT_PATH;
	
	@Value("${file.path.ad.sg}")
	private String FILE_AD_SG_PATH;
	
	// 광고주 
	
	/**
	 * 광고주 메인화면 광고 목록
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getSgList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		Map<String, Object> campaignTotal = demandCampaignMapper.getCountCampaign(param);
		if(!CommonUtil.checkIsNull(campaignTotal)) {
			List<Map<String, Object>> sgList = demandSgMapper.getList(param);
			campaignTotal.put("list", sgList);
			respMap.setBody("data", campaignTotal);
		} else {
			return respMap.getResponse(Code.NOT_EXIST_DATA);
		}
		return respMap.getResponse();
	}
	
	
	/**
	 * 캠페인내 광고 상태 변경
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> demandModifyStatus(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<String> failSgList = new ArrayList<>();
		List<String> sgIdList = (List<String>) param.get("sg_id_list");
		
		for(String sgId : sgIdList) {
			param.put("sg_id", sgId);
			
			// 상태 업데이트
			if(demandSgMapper.demandModifyStatus(param) < 0) {
				failSgList.add(sgId);
			}
		}
		
		if(failSgList.size() > 0) {
			respMap.setBody("fail_list", failSgList);
		}
		
		return respMap.getResponse();
	}
	
	/**
	 * 광고(정책) 정보 등록
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> addInfo(Map<String, Object> param, MultipartHttpServletRequest mRequest) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		log.info("param : {}", param);
		
		// 캠페인 등록자인지 확인
		if(demandSgMapper.hasCampaignMember(param) <= 0) {
			throw new RuntimeException();
		}
		// 기본 정보
		addSgBasic(param);
		//활동이력 추가
		demandMemberService.addDspModHistory(param, ModifyHistory.DEMAND_ADD);
		
		if (param.get("pay_type").equals("CPM")) {
			// 지역 
			if (param.get("target_area_yn").equals("Y")) {
				addSgArea(param);
			}
			
			// 스케쥴
			if (param.get("target_week_yn").equals("Y")) {
				addSgSchedule(param);
			}
		} else {
			// 슬롯 등록
			List<Map<String, Object>> possibleSlotList = scheduleMapper.getPossibleSlotList(param);
			
			for(Map<String, Object> slot: possibleSlotList) {
				if(CommonUtil.checkIsNull(slot, "sg_id")) { // sg_id 없으면 등록 가능
					slot.put("sg_id", param.get("sg_id"));
					scheduleMapper.addScheduleProductSlotSg(slot);
					break;
				} else {
					List<Map<String, Object>> sgIdList = demandSgMapper.getSgIdList(slot);
					param.put("sgIdList", sgIdList);
					if(demandSgMapper.isExpireSg(param) == 0) {
						slot.put("sg_id", param.get("sg_id"));
						scheduleMapper.addScheduleProductSlotSg(slot);
						break;
					}
				}
			}
		}
		
		// 소재
		addSgMaterial(param, mRequest);
			
		return respMap.getResponse();
	}

	/**
	 * 광고(정책) 기본 정보 등록
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> addSgBasic(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 캠페인 유무 체크
		Map<String, Object> campaignData = demandSgMapper.getCampaign(param);
		if (CommonUtil.checkIsNull(campaignData)) {
			throw new RuntimeException();
		}
		
		String payType = (String) campaignData.get("pay_type");
		param.put("pay_type", payType);
		
		String paramPrice = String.valueOf(param.get("price"));
		int price = 0;
		
		if (payType.equals(PayType.CPP.name())) {
			param.put("target_area_yn", "N");
			param.put("target_week_yn", "N");
			param.put("default_yn", "Y");
			param.put("material_ratio", "D");
			param.put("exposure_horizon_type", "N");
			param.put("exposure_vertical_type", "N");

			price = calculateCppProcess(param);
		} else {
			price = calculateCpmProcess(param);
		}
		
		// 파라미터로 받은 금액과 계산한 금액이 다를 경우 에러
		if(!String.valueOf(price).equals(paramPrice)) {
			throw new RuntimeException();
		}

		// 카테고리 유무 체크
		if (demandSgMapper.hasCategoryCode(param) <= 0) {
			throw new RuntimeException();
		}

		param.put("pay_price", param.get("price"));
		param.put("status", 0);

		// 등록
		if (demandSgMapper.addSgBasic(param) <= 0) {
			throw new RuntimeException();
		}
		
		param.put("sg_id", param.get("id"));
		
		return respMap.getResponse();
	}
	
	/**
	 * 광고 노출 지역 등록
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> addSgArea(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();

		if (demandSgMapper.hasAreaCode(param) > 0) {
			demandSgMapper.addSgArea(param);
		} else {
			throw new RuntimeException();
		}
		
		return respMap.getResponse();
	}
	
	/**
	 * 광고 스케줄 등록
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> addSgSchedule(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		Map<String, Object> weekParam = new HashMap<>();
		weekParam.put("sg_id", param.get("sg_id"));
		
		String unescapeScheduleList = StringEscapeUtils.unescapeHtml4((String) param.get("schedule_list"));
		List<Map<String, Object>> scheduleList = CommonUtil.jsonArrayToList(unescapeScheduleList);
		
		for (Map<String, Object> schedule : scheduleList) {
			weekParam.put("week_code", schedule.get("week_code"));
			weekParam.put("use_yn", (String) schedule.get("use_yn"));
			
			List<Map<String, Object>> hourList = (List<Map<String, Object>>) schedule.get("schedule");
			for (Map<String, Object> hour : hourList) {
				weekParam.put((String) hour.get("name"), hour.get("value"));
			}
			
			// 등록
			if (demandSgMapper.addSgSchedule(weekParam) <= 0) {
				throw new RuntimeException();
			}
		}
		
		return respMap.getResponse();
	}
	
	/**
	 * 광고 소재 등록
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> addSgMaterial(Map<String, Object> param, MultipartHttpServletRequest mRequest) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> mFileList = new ArrayList<>();
		
		// 파일 
		if(!CommonUtil.checkIsNull(mRequest, "file_w_1920")) {
			MultipartFile mFile = mRequest.getFile("file_w_1920");
			
			String unescapeFileData = StringEscapeUtils.unescapeHtml4((String) param.get("file_data_w_1920"));
			Map<String, Object> fileData = CommonUtil.jsonToMap(unescapeFileData);
			
			mFileList.add(new HashMap() {{
				put("file", mFile);
				put("page_size", SgPageSize.W_1920.getPageSize());
				put("ratio_type", SgRatio.RATIO_H.getRatio());
				put("height", fileData.get("height"));
				put("width", fileData.get("width"));
				put("playtime", fileData.get("playtime"));
			}});
		}
		
		if(!CommonUtil.checkIsNull(mRequest, "file_w_1600")) {
			MultipartFile mFile = mRequest.getFile("file_w_1600");
			
			String unescapeFileData = StringEscapeUtils.unescapeHtml4((String) param.get("file_data_w_1600"));
			Map<String, Object> fileData = CommonUtil.jsonToMap(unescapeFileData);

			mFileList.add(new HashMap() {{
				put("file", mFile);
				put("page_size", SgPageSize.W_1600.getPageSize());
				put("ratio_type", SgRatio.RATIO_H.getRatio());
				put("height", fileData.get("height"));
				put("width", fileData.get("width"));
				put("playtime", fileData.get("playtime"));
			}});
		}
		
		if(!CommonUtil.checkIsNull(mRequest, "file_w_2560")) {
			MultipartFile mFile = mRequest.getFile("file_w_2560");
			
			String unescapeFileData = StringEscapeUtils.unescapeHtml4((String) param.get("file_data_w_2560"));
			Map<String, Object> fileData = CommonUtil.jsonToMap(unescapeFileData);
			
			mFileList.add(new HashMap() {{
				put("file", mFile);
				put("page_size", SgPageSize.W_2560.getPageSize());
				put("ratio_type", SgRatio.RATIO_H.getRatio());
				put("height", fileData.get("height"));
				put("width", fileData.get("width"));
				put("playtime", fileData.get("playtime"));
			}});
		}
		
		if(!CommonUtil.checkIsNull(mRequest, "file_h_1920")) {
			MultipartFile mFile = mRequest.getFile("file_h_1920");
			
			String unescapeFileData = StringEscapeUtils.unescapeHtml4((String) param.get("file_data_h_1920"));
			Map<String, Object> fileData = CommonUtil.jsonToMap(unescapeFileData);
			
			mFileList.add(new HashMap() {{
				put("file", mFile);
				put("page_size", SgPageSize.H_1920.getPageSize());
				put("ratio_type", SgRatio.RATIO_V.getRatio());
				put("height", fileData.get("height"));
				put("width", fileData.get("width"));
				put("playtime", fileData.get("playtime"));
			}});
		}
		
		if(!CommonUtil.checkIsNull(mRequest, "file_h_1600")) {
			MultipartFile mFile = mRequest.getFile("file_h_1600");
			
			String unescapeFileData = StringEscapeUtils.unescapeHtml4((String) param.get("file_data_h_1600"));
			Map<String, Object> fileData = CommonUtil.jsonToMap(unescapeFileData);
			
			mFileList.add(new HashMap() {{
				put("file", mFile);
				put("page_size", SgPageSize.H_1600.getPageSize());
				put("ratio_type", SgRatio.RATIO_V.getRatio());
				put("height", fileData.get("height"));
				put("width", fileData.get("width"));
				put("playtime", fileData.get("playtime"));
			}});
		}
		
		if(!CommonUtil.checkIsNull(mRequest, "file_h_2560")) {
			MultipartFile mFile = mRequest.getFile("file_h_2560");
			
			String unescapeFileData = StringEscapeUtils.unescapeHtml4((String) param.get("file_data_h_2560"));
			Map<String, Object> fileData = CommonUtil.jsonToMap(unescapeFileData);
			
			mFileList.add(new HashMap() {{
				put("file", mFile);
				put("page_size", SgPageSize.H_2560.getPageSize());
				put("ratio_type", SgRatio.RATIO_V.getRatio());
				put("height", fileData.get("height"));
				put("width", fileData.get("width"));
				put("playtime", fileData.get("playtime"));
			}});
		}
		
		if(!CommonUtil.checkIsNull(mRequest, "file_o")) {
			MultipartFile mFile = mRequest.getFile("file_o");
			
			String unescapeFileData = StringEscapeUtils.unescapeHtml4((String) param.get("file_data_o"));
			Map<String, Object> fileData = CommonUtil.jsonToMap(unescapeFileData);
			
			mFileList.add(new HashMap() {{
				put("file", mFile);
				put("page_size", SgPageSize.O_1080.getPageSize());
				put("ratio_type", SgRatio.RATIO_S.getRatio());
				put("height", fileData.get("height"));
				put("width", fileData.get("width"));
				put("playtime", fileData.get("playtime"));
			}});
		}
		
		for(Map<String, Object> mFileMap : mFileList) {
			MultipartFile mFile = (MultipartFile) mFileMap.get("file");
			
			String pageSize = (String) mFileMap.get("page_size");
			param.put("width", mFileMap.get("width"));
			param.put("height", mFileMap.get("height"));
			param.put("playtime", mFileMap.get("playtime"));
			param.put("default_yn", "N");
			param.put("page_size_code", pageSize);
			
			getFileInfo(param, mFile);

			if(pageSize.equals(SgPageSize.W_1920.getPageSize()) || pageSize.equals(SgPageSize.H_1920.getPageSize()) || pageSize.equals(SgPageSize.O_1080.getPageSize())) {
				param.put("default_yn", "Y");
			}
			
			param.put("ratio_type", mFileMap.get("ratio_type"));
			
			// 등록
			if (demandSgMapper.addSgMaterial(param) <= 0) {
				throw new RuntimeException();
			}
		}
		
		return respMap.getResponse();
	}
	
	/**
	 * 파일 정보
	 * @param param
	 * @param mFile
	 * @return
	 * @throws Exception
	 */
	private void getFileInfo(Map<String, Object> param, MultipartFile mFile) throws Exception {
		
		Map<String, Object> fileInfo = new HashMap<>();
		
		// 파일 기본 경로 (/ad/sg/광고id)
		String fileBasicPath = FILE_AD_SG_PATH + File.separator + param.get("sg_id");
		
		// 파일 크기
		long fileSize = mFile.getSize();
		long fileSizeLimit = 300;
		
		if((fileSize / (1024 * 1024)) > fileSizeLimit) {
			throw new RuntimeException();
		}
		fileInfo.put("file_size", fileSize);
		
		// 파일명
		String fileFullName = mFile.getOriginalFilename();
		
		fileInfo.put("file_full_name", fileFullName);

		// 확장자 검사 
		String fileExt = FileUtil.getFileExt(fileFullName);
		
		if(fileExt == null) {
			throw new RuntimeException();
		}
		
		int fileWidth = 0; 
		int fileHeight = 0; 
		int filePlaytime = 0;
		
		// MIME-Type
		String mimeType = mFile.getContentType();
		
		if(fileExt.equals("mp4")) {
			if(!mimeType.equals("video/mp4")) {
				throw new RuntimeException();
			}
			fileWidth = Integer.valueOf(String.valueOf(param.get("width")));
			fileHeight = Integer.valueOf(String.valueOf(param.get("height")));
			filePlaytime = Integer.valueOf(String.valueOf(param.get("playtime")));
			
			fileInfo.put("file_type", "VIDEO");
			fileInfo.put("playtime", filePlaytime);
		} else if(fileExt.equals("jpg") || fileExt.equals("jpeg") || fileExt.equals("png")) {
			if(!mimeType.equals("image/jpeg") && !mimeType.equals("image/png")) {
				throw new RuntimeException();
			}
			Map<String, Object> imgInfo = FileUtil.getImgFileInfo(mFile);

			fileWidth = Integer.valueOf(String.valueOf(imgInfo.get("width")));
			fileHeight = Integer.valueOf(String.valueOf(imgInfo.get("height")));
			
			fileInfo.put("file_type", "IMAGE");
		} else {
			throw new RuntimeException();
		}
		fileInfo.put("width", fileWidth);
		fileInfo.put("height", fileHeight);
		
		// 사이즈 검사
		String pageSizeCode = String.valueOf(param.get("page_size_code"));
		Map<String, Object> codeParam = new HashMap<>() {{
			put("parent_code", "PAGE_SIZE");
			put("code", pageSizeCode);
		}};
		
		Map<String, Object> codeDetail = commonMapper.getCodeDetail(codeParam);
		if(CommonUtil.checkIsNull(codeDetail)) {
			throw new RuntimeException();
		}
		
		String[] codes = String.valueOf(codeDetail.get("code")).split("x");
		
		int width = Integer.valueOf(codes[0]);
		int height = Integer.valueOf(codes[1]);
		
		// 사이즈 불일치
		if(fileWidth < width || fileHeight < height) {
			throw new RuntimeException();
		}
		
		// 파일 업로드 경로
		UploadFileInfo uploadPath = s3Service.uploadFile(mFile, fileBasicPath);
		
		fileInfo.put("file_path", uploadPath.getFilePath());

		// 파일명
		fileInfo.put("file_name", uploadPath.getFileName());
		
		// 파일명 확장자 제외
		fileInfo.put("name", uploadPath.getFileName().substring(0, uploadPath.getFileName().lastIndexOf(".")));
		
		param.putAll(fileInfo);
	}
	
	/**
	 * 광고 상세 조회 CPM, CPP
	 * @param param
	 * @return
	 */
	public Map<String, Object> getSgDetail(Map<String, Object> param) {
		ResponseMap responseMap = beanFactory.getResponseMap();
		
		// 광고 기본 정보
		Map<String, Object> getSgManager = demandSgMapper.getSgManager(param);
		if (CommonUtil.checkIsNull(getSgManager)) {
			return responseMap.getResponse(Code.NOT_EXIST_DATA);
		}

		param.put("campaign_id", getSgManager.get("campaign_id"));
		
		// 캠페인 id, name, pay_type
		Map<String, Object> getCampaign = demandSgMapper.getCampaign(param);
		if (CommonUtil.checkIsNull(getCampaign)) {
			return responseMap.getResponse(Code.NOT_EXIST_DATA);
		}
		
		String payType = (String) getCampaign.get("pay_type"); // CPM, CPP
		String targetWeekYN = (String) getSgManager.get("target_week_yn"); // 노출 시간대 사용 여부 YN
		String targetAreaYN = (String) getSgManager.get("target_area_yn"); // 타겟 지역 사용 여부 YN
		
		// 광고 소재 정보
		List<Map<String, Object>> getSgMaterial = demandSgMapper.getSgMaterial(param);
		
		getSgManager.put("pay_type", payType);
		getSgManager.put("sg_material_list", getSgMaterial);
		
		// CPM 일 때
		if (payType.equals(PayType.CPM.name())) {
			// 노출 옵션 - 스케쥴 사용 시 
			if (targetWeekYN != null && targetWeekYN.equals("Y")) {
				List<Map<String, Object>> getSgWeek = demandSgMapper.getSgWeek(param);
				
				getSgManager.put("sg_schedule_list", getSgWeek);
			}
			
			// 노출 옵션 - 지역 사용 시
			if (targetAreaYN != null && targetAreaYN.equals("Y")) {
				Map<String,Object> getSgArea = demandSgMapper.getSgArea(param);
				
				getSgManager.put("sg_area", getSgArea);
			}
		}
		// CPP 일 때
		else {
			param.put("ssp_product_id", getSgManager.get("ssp_product_id"));
			Map<String, Object> getSspProduct = demandSgMapper.getSgProduct(param);
			
			getSgManager.put("ssp_product", getSspProduct);
		}
		
		responseMap.setBody("sg_manager", getSgManager);
		
		return responseMap.getResponse();
	}
	
	/**
	 * 광고 정보 수정
	 * @param param
	 * @return
	 * @throws Exception 
	 */
	public Map<String, Object> modifySgManager(MultipartHttpServletRequest mRequest, Map<String, Object> param) throws Exception {
		ResponseMap responseMap = beanFactory.getResponseMap();
		
		Map<String, Object> getSgManager = demandSgMapper.getSgManager(param);
		if (CommonUtil.checkIsNull(getSgManager)) {
			throw new RuntimeException();
		}

		// 광고 상태가 진행중일 때 수정 불가
		String sgStatus = String.valueOf(getSgManager.get("status"));;
		
		if (sgStatus.equals("1")) {
			throw new RuntimeException();
		}
		
		param.put("campaign_id", getSgManager.get("campaign_id"));
		
		// 캠페인 id, name, pay_type
		Map<String, Object> getCampaign = demandSgMapper.getCampaign(param);
		if (CommonUtil.checkIsNull(getCampaign)) {
			throw new RuntimeException();
		}
		
		String payType = (String) getCampaign.get("pay_type"); // CPM, CPP
		param.put("pay_type", payType);
		String targetWeekYN = (String) param.get("target_week_yn"); // 노출 시간대 사용 여부 YN
		String targetAreaYN = (String) param.get("target_area_yn"); // 타겟 지역 사용 여부 YN
		
		// 광고 기본 정보 수정
		modifySgBasic(param);
		
		// CPM 일 때
		if (payType.equals(PayType.CPM.name())) {

			demandSgMapper.removeSgArea(param);
			demandSgMapper.removeSgWeek(param);
			
			// 노출 시간, 지역을 모두 사용하면 예외
			if ((targetWeekYN != null && targetWeekYN.equals("Y")) && (targetAreaYN != null && targetAreaYN.equals("Y"))) {
	            throw new RuntimeException();
	        }
			
			if (targetWeekYN != null && targetWeekYN.equals("Y")) {
				// 광고 노출 스케쥴 수정
				modifySgWeek(param);
			}
			
			if (targetAreaYN != null && targetAreaYN.equals("Y")) {
				// 광고 노출 지역 정보 수정
				modifySgArea(param);
			}
		}
		
		// 광고 소재 정보 수정
		modifySgMaterial(mRequest, param);
		
		//활동 이력 추가
		demandMemberService.addDspModHistory(param, ModifyHistory.DEMAND_MODIFY);
		
		return responseMap.getResponse();
	}
	
	/**
	 * 광고 기본 정보 수정
	 * @param param
	 * @return
	 * @throws Exception 
	 */
	public Map<String, Object> modifySgBasic(Map<String, Object> param) throws Exception {
		ResponseMap responseMap = beanFactory.getResponseMap();

		Map<String, Object> getSgManager = demandSgMapper.getSgManager(param);
		if (CommonUtil.checkIsNull(getSgManager)) {
			return responseMap.getErrResponse();
		}
		
		param.put("campaign_id", getSgManager.get("campaign_id"));
		
		// 캠페인 id, name, pay_type
		Map<String, Object> getCampaign = demandSgMapper.getCampaign(param);
		if (CommonUtil.checkIsNull(getCampaign)) {
			throw new RuntimeException();
		}
		
		// 카테고리 유무 체크
		if (demandSgMapper.hasCategoryCode(param) <= 0) {
			throw new RuntimeException();
		}
		
		// CPM, CPP
		String payType = (String) getCampaign.get("pay_type");

		int price = Integer.parseInt(String.valueOf(param.get("price")));
		int calculatePrice = 0; 
		
        // 과금방식이 CPM 일 때 
        if (payType.equals(PayType.CPM.name())) {
			int exposureTarget = Integer.parseInt(String.valueOf(param.get("exposure_target")));
			int exposureLimit = Integer.parseInt(String.valueOf(param.get("exposure_limit")));
			
			// 일 노출 제한이 목표 노출 수 보다 크면 예외
			if (exposureLimit > exposureTarget) {
				throw new RuntimeException();
			}
			
        	// 광고 금액 계산
			calculatePrice = calculateCpmProcess(param);
		}
        // 과금방식이 CPP 일 때
        else {
        	// 수정할 상품 정보가 있을 때 상품과 슬롯 관련 데이터 저장
        	if (!CommonUtil.checkIsNull(param, "ssp_product_id")) {
        		// 이전 상품에 관해 등록된 슬롯 정보 삭제
        		if (scheduleMapper.removeScheduleProductSlotSg(param) < 1) {
        			throw new RuntimeException();
        		}
        		List<Map<String, Object>> possibleSlotList = scheduleMapper.getPossibleSlotList(param);
        		
        		for(Map<String, Object> slot: possibleSlotList) {
    				if(CommonUtil.checkIsNull(slot, "sg_id")) { // sg_id 없으면 등록 가능
    					slot.put("sg_id", param.get("sg_id"));
    					scheduleMapper.addScheduleProductSlotSg(slot);
    					break;
    				} else {
    					List<Map<String, Object>> sgIdList = demandSgMapper.getSgIdList(slot);
    					param.put("sgIdList", sgIdList);
    					if(demandSgMapper.isExpireSg(param) == 0) {
    						slot.put("sg_id", param.get("sg_id"));
    						scheduleMapper.addScheduleProductSlotSg(slot);
    						break;
    					}
    				}
    			}
        	}
        	
        	// 광고 금액 계산
			calculatePrice = calculateCppProcess(param);
        }
        
        // 계산 금액이 다르면
		if (price != calculatePrice) {
			throw new RuntimeException();
		}
		
		int totalPayPrice = 0;
		String totalPayPriceString = String.valueOf(getSgManager.get("total_pay_price"));
		// 입금해야할 금액 수정
		if (totalPayPriceString != null) {
			totalPayPrice= Integer.parseInt(totalPayPriceString);
		}
		int payPrice = price - totalPayPrice;
		
		param.put("pay_price", payPrice);
	
		if (demandSgMapper.modifySgManager(param) < 1) {
			throw new RuntimeException();
		}
		
		return responseMap.getResponse();
	}
	
	/**
	 * 광고 소재 수정
	 * @param mRequest 
	 * @param param
	 * @return
	 * @throws Exception 
	 */
	public Map<String, Object> modifySgMaterial(MultipartHttpServletRequest mRequest, Map<String, Object> param) throws Exception {
		ResponseMap responseMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> mFileList = new ArrayList<>();
		
		// 소재 정보 체크
		String payType = (String) param.get("pay_type"); // CPM, CPP
		
		if (payType.equals(PayType.CPM.name())) {
			if (!checkExposureType(mRequest, param)) {
				throw new RuntimeException();
			};	
		}
		
		// 파일 
		if(!CommonUtil.checkIsNull(mRequest, "file_w_1920")) {
			MultipartFile mFile = mRequest.getFile("file_w_1920");
			
			String unescapeFileData = StringEscapeUtils.unescapeHtml4((String) param.get("file_data_w_1920"));
			Map<String, Object> fileData = CommonUtil.jsonToMap(unescapeFileData);
			
			mFileList.add(new HashMap() {{
				put("file", mFile);
				put("page_size", SgPageSize.W_1920.getPageSize());
				put("ratio_type", SgRatio.RATIO_H.getRatio());
				put("height", fileData.get("height"));
				put("width", fileData.get("width"));
				put("playtime", fileData.get("playtime"));
			}});
		}
		
		if(!CommonUtil.checkIsNull(mRequest, "file_w_1600")) {
			MultipartFile mFile = mRequest.getFile("file_w_1600");
			
			String unescapeFileData = StringEscapeUtils.unescapeHtml4((String) param.get("file_data_w_1600"));
			Map<String, Object> fileData = CommonUtil.jsonToMap(unescapeFileData);
			
			mFileList.add(new HashMap() {{
				put("file", mFile);
				put("page_size", SgPageSize.W_1600.getPageSize());
				put("ratio_type", SgRatio.RATIO_H.getRatio());
				put("height", fileData.get("height"));
				put("width", fileData.get("width"));
				put("playtime", fileData.get("playtime"));
			}});
		}
		
		if(!CommonUtil.checkIsNull(mRequest, "file_w_2560")) {
			MultipartFile mFile = mRequest.getFile("file_w_2560");
			
			String unescapeFileData = StringEscapeUtils.unescapeHtml4((String) param.get("file_data_w_2560"));
			Map<String, Object> fileData = CommonUtil.jsonToMap(unescapeFileData);
			
			mFileList.add(new HashMap() {{
				put("file", mFile);
				put("page_size", SgPageSize.W_2560.getPageSize());
				put("ratio_type", SgRatio.RATIO_H.getRatio());
				put("height", fileData.get("height"));
				put("width", fileData.get("width"));
				put("playtime", fileData.get("playtime"));
			}});
		}
		
		if(!CommonUtil.checkIsNull(mRequest, "file_h_1920")) {
			MultipartFile mFile = mRequest.getFile("file_h_1920");
			
			String unescapeFileData = StringEscapeUtils.unescapeHtml4((String) param.get("file_data_h_1920"));
			Map<String, Object> fileData = CommonUtil.jsonToMap(unescapeFileData);
			
			mFileList.add(new HashMap() {{
				put("file", mFile);
				put("page_size", SgPageSize.H_1920.getPageSize());
				put("ratio_type", SgRatio.RATIO_V.getRatio());
				put("height", fileData.get("height"));
				put("width", fileData.get("width"));
				put("playtime", fileData.get("playtime"));
			}});
		}
		
		if(!CommonUtil.checkIsNull(mRequest, "file_h_1600")) {
			MultipartFile mFile = mRequest.getFile("file_h_1600");
			
			String unescapeFileData = StringEscapeUtils.unescapeHtml4((String) param.get("file_data_h_1600"));
			Map<String, Object> fileData = CommonUtil.jsonToMap(unescapeFileData);
			
			mFileList.add(new HashMap() {{
				put("file", mFile);
				put("page_size", SgPageSize.H_1600.getPageSize());
				put("ratio_type",SgRatio.RATIO_V.getRatio());
				put("height", fileData.get("height"));
				put("width", fileData.get("width"));
				put("playtime", fileData.get("playtime"));
			}});
		}
		
		if(!CommonUtil.checkIsNull(mRequest, "file_h_2560")) {
			MultipartFile mFile = mRequest.getFile("file_h_2560");
			
			String unescapeFileData = StringEscapeUtils.unescapeHtml4((String) param.get("file_data_h_2560"));
			Map<String, Object> fileData = CommonUtil.jsonToMap(unescapeFileData);
			
			mFileList.add(new HashMap() {{
				put("file", mFile);
				put("page_size", SgPageSize.H_2560.getPageSize());
				put("ratio_type",SgRatio.RATIO_V.getRatio());
				put("height", fileData.get("height"));
				put("width", fileData.get("width"));
				put("playtime", fileData.get("playtime"));
			}});
		}
		
		if(!CommonUtil.checkIsNull(mRequest, "file_o")) {
			MultipartFile mFile = mRequest.getFile("file_o");
			
			String unescapeFileData = StringEscapeUtils.unescapeHtml4((String) param.get("file_data_o"));
			Map<String, Object> fileData = CommonUtil.jsonToMap(unescapeFileData);
			
			mFileList.add(new HashMap() {{
				put("file", mFile);
				put("page_size", SgPageSize.O_1080.getPageSize());
				put("ratio_type",SgRatio.RATIO_S.getRatio());
				put("height", fileData.get("height"));
				put("width", fileData.get("width"));
				put("playtime", fileData.get("playtime"));
			}});
		}

		// 기존 파일 먼저 제거
		if (!CommonUtil.checkIsNull(param, "material_list")) {
			List<String> materialList = Arrays.asList(String.valueOf(param.get("material_list")).split(","));
			for(String materialId : materialList) {
				param.put("material_id", materialId);
				Map<String, Object> fileInfo = demandSgMapper.getSgMaterialFile(param);
				
				if(!CommonUtil.checkIsNull(fileInfo)) {
					String fullPath = (String) fileInfo.get("file_path");
					s3Service.removeFile(fullPath);
					demandSgMapper.removeSgMaterialFile(param);
				}
			}
		}
		
		// 소재 저장
		for(Map<String, Object> mFileMap : mFileList) {
			MultipartFile mFile = (MultipartFile) mFileMap.get("file");
			
			String pageSize = (String) mFileMap.get("page_size");
			param.put("height", mFileMap.get("height"));
			param.put("width", mFileMap.get("width"));
			param.put("playtime", mFileMap.get("playtime"));
			param.put("default_yn", "N");
			param.put("page_size_code", pageSize);
			
			getFileInfo(param, mFile);

			if(pageSize.equals(SgPageSize.W_1920.getPageSize()) || pageSize.equals(SgPageSize.H_1920.getPageSize()) || pageSize.equals(SgPageSize.O_1080.getPageSize())) {
				param.put("default_yn", "Y");
			}
			
			param.put("ratio_type", mFileMap.get("ratio_type"));
			
			// 등록
			if (demandSgMapper.addSgMaterial(param) <= 0) {
				throw new RuntimeException();
			}
		}
		
		
		return responseMap.getResponse();
	}

	/**
	 * 소재 타입에 맞게 파일이 업로드 됐는지 체크
	 * @param mRequest
	 * @param param 
	 */
	private boolean checkExposureType(MultipartHttpServletRequest mRequest, Map<String, Object> param ) {
		
		String material_ratio = (String) param.get("material_ratio");
		String exposureHorizonType = (String) param.get("exposure_horizon_type");
		String exposureVerticalType = (String) param.get("exposure_vertical_type");
		
		// 소재 비율: 가로/세로
		if (material_ratio.equals("D")) {
			if (exposureHorizonType.equals("N") && exposureVerticalType.equals("N")) {
				return false;
			}
		} 
		// 소재 비율: 1:1
//		else {
//			if (CommonUtil.checkIsNull(mRequest, "file_o")) {
//				return false;
//			}
//		}
		
		// 가로 소재를 사용하지 않을 때 가로 소재 관련 파일이 존재하면 false
		if (exposureHorizonType.equals("N")) {
			if (!CommonUtil.checkIsNull(mRequest, "file_w_1920") || !CommonUtil.checkIsNull(mRequest, "file_w_1600") || !CommonUtil.checkIsNull(mRequest, "file_w_2560")) {
				return false;
			}
		}

		// 세로 소재를 사용하지 않을 때 세로 소재 관련 파일이 존재하면 false
		if (exposureVerticalType.equals("N")) {
			if (!CommonUtil.checkIsNull(mRequest, "file_h_1920") || !CommonUtil.checkIsNull(mRequest, "file_h_1600") || !CommonUtil.checkIsNull(mRequest, "file_h_2560")) {
				return false;
			}
		}
		
		return true;
	}

	/**
	 * 광고 스케쥴 수정
	 * @param param
	 * @return
	 */
	public Map<String, Object> modifySgWeek(Map<String, Object> param) {
		ResponseMap responseMap = beanFactory.getResponseMap();
		
		// json을 분석해서 데이터를 담을 map
		Map<String, Object> weekParam = new HashedMap<>();
		weekParam.put("sg_id", param.get("sg_id"));

		String unescapeScheduleList = StringEscapeUtils.unescapeHtml4((String) param.get("schedule_list"));
		List<Map<String, Object>> scheduleList = CommonUtil.jsonArrayToList(unescapeScheduleList);
		
		for (Map<String, Object> scheduleListMap : scheduleList) {
			weekParam.put("week_code", scheduleListMap.get("week_code"));
			weekParam.put("use_yn", scheduleListMap.get("use_yn"));
			
			List<Map<String, Object>> schedules = (List<Map<String, Object>>) scheduleListMap.get("schedule");
			
			Map<String,Object> scheduleMap = schedules.stream()
				.collect(Collectors.toMap(s -> String.valueOf(s.get("name")), s -> s.get("value")));

			weekParam.putAll(scheduleMap);
			if (demandSgMapper.addSgSchedule(weekParam) < 1) {
				throw new RuntimeException();
			}	
		}
		
		return responseMap.getResponse();
	}
	
	/**
	 * 광고 지역 정보 수정 
	 * @param param
	 * @return
	 */
	public Map<String, Object> modifySgArea(Map<String, Object> param) {
		ResponseMap responseMap = beanFactory.getResponseMap();
		
		if (demandSgMapper.addSgArea(param) < 1) {
			throw new RuntimeException();
		}
		
		return responseMap.getResponse();
	}
	
	/**
	 * 캠페인내 광고 삭제
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> removeDemand(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<String> sgIdList = (List<String>) param.get("sg_id_list");
		
		for(String sgId : sgIdList) {
			param.put("sg_id", sgId);
			
			//소재 삭제
			List<Map<String, Object>> materialList = demandSgMapper.getSgMaterial(param);
			for(Map<String, Object> materialInfo : materialList) {
				if(!CommonUtil.checkIsNull(materialInfo)) {
					String fullPath = (String) materialInfo.get("file_path");
					s3Service.removeFile(fullPath);
				}
			}
			
			if(demandSgMapper.removeSgMaterial(param) <= 0) {
				throw new RuntimeException();
			}
			
			demandSgMapper.removeSgPayLog(param);
			
			if(param.get("pay_type").equals(PayType.CPM.name())) { // CPM 인 경우
				demandSgMapper.removeSgArea(param); // 광고 지역
				demandSgMapper.removeSgWeek(param); // 광고 날짜
				demandScheduleMapper.removeScheduleTableBlock(param);
			} else if(param.get("pay_type").equals(PayType.CPP.name())) { // CPP 인 경우
				demandScheduleMapper.removeScheduleProductSlotSg(param);
			}
			
			//광고 삭제
			Map<String, Object> removeSgManager = demandSgMapper.removeSgManager(param);
			param.put("name", removeSgManager.get("name"));
			if(removeSgManager.isEmpty()) {
				throw new RuntimeException();
			}
			
			demandMemberService.addDspModHistory(param, ModifyHistory.DEMAND_REMOVE);
		}
		
		return respMap.getResponse();
	}
	
	/**
	 * CPP 광고 신청금액 계산
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> calculateCPP(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		int price = calculateCppProcess(param);
		respMap.setBody("price", price);
		
		return respMap.getResponse();
	}
	
	/**
	 * CPP 광고 금액 계산 로직
	 * @param param
	 * @return
	 * @throws Exception
	 */
	private int calculateCppProcess(Map<String, Object> param) throws Exception {
		
		// 선택한 상품의 가중치
		int sspProductRate = (int) productMapper.getProductDetail(param).get("price_rate");
		float productRate = (float) sspProductRate / 100;
		
		int price = (int) (PayType.CPP.getPrice() * productRate);
		
		// 날짜 포맷
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date startDate = formatter.parse((String) param.get("start_ymd"));
		Date endDate = formatter.parse((String) param.get("end_ymd"));
		
		// 종료일 - 시작일
		long differenceTime = endDate.getTime() - startDate.getTime();
		int differenceDate = (int) (differenceTime / (24 * 60 * 60 * 1000) + 1); 
		
		price = price * differenceDate;
		
		List<String> typeList = new ArrayList<>();
		typeList.add((String) param.get("material_kind"));
		typeList.add("SEC" + String.valueOf(param.get("exposure_time")));
		
		for(String type : typeList) {
			Map<String, Object> typeParam = new HashMap<>();
			typeParam.put("rate_code", type);
			
			int ratePer = (int) demandSgMapper.getRate(typeParam).get("rate");
			float rate = (float) ratePer / 100;
			
			price = (int) (price * rate);
		}
		
		return price;
	}
	
	public Map<String, Object> calculateCPM(Map<String, Object> param) {
		ResponseMap responseMap = beanFactory.getResponseMap();
		
		int price = calculateCpmProcess(param);
		responseMap.setBody("price", price);
		
		return responseMap.getResponse();
	}
	
	/**
	 * CPM 광고신청 금액 계산
	 * @param param
	 * @return
	 */
	private int calculateCpmProcess(Map<String, Object> param) {
		final int cppPrice = PayType.CPM.getPrice();
		final int percentage = 100;
		
		// 목표 노출 수가 100으로 나누어 떨어지지 않을 때
		int exposureTarget = Integer.parseInt(String.valueOf(param.get("exposure_target"))); // 목표 노출 수
		if ((exposureTarget % percentage) != 0) {
			throw new RuntimeException();
		}
		
		// 노출 당 금액
		int price = (int) (exposureTarget / percentage) * cppPrice;
		
		String targetWeekYN = (String) param.get("target_week_yn"); // 노출 시간대 사용 여부 YN
		String targetAreaYN = (String) param.get("target_area_yn"); // 타겟 지역 사용 여부 YN
		
		List<String> rateCodeList = new ArrayList<>();
		
		// 노출 시간, 지역을 모두 사용하면 예외
		if ((targetWeekYN != null && targetWeekYN.equals("Y")) && (targetAreaYN != null && targetAreaYN.equals("Y"))) {
            throw new RuntimeException();
        }
		
		// 시간 노출 옵션 사용
		if (targetWeekYN != null && targetWeekYN.equals("Y")) {
			rateCodeList.add("OPT_TIME");
		}
		// 지역 노출 옵션 사용
		if (targetAreaYN != null && targetAreaYN.equals("Y")) {
			rateCodeList.add("OPT_AREA");
		}

		rateCodeList.add((String) param.get("material_kind"));
		
		// 소재 노출 시간
		rateCodeList.add("SEC" + param.get("exposure_time"));
		
		for (String rateCode : rateCodeList) {
			Map<String, Object> rateParam = new HashMap<>();
			rateParam.put("rate_code", rateCode);
			
			int ratePer = Integer.parseInt(String.valueOf(demandSgMapper.getRate(rateParam).get("rate")));
			double rate = (double) ratePer / percentage;
			price = (int) (price * rate);
		}
		
		return price;
	}
	
	
	// 광고주 회원 관리
	
	/**
	 * 광고 집행금액용 캠페인 및 광고 목록
	 * @param param
	 * @return
	 */
	public Map<String, Object> getDemandSgList(Map<String, Object> param) {
		ResponseMap responseMap = beanFactory.getResponseMap();
		
		responseMap.setBody("list", demandSgMapper.getDemandSgList(param));
		responseMap.setBody("total_count", demandSgMapper.getDemandSgListCount(param));
		
		return responseMap.getResponse();
	}
	
	/**
	 * 구 카테고리 조회
	 * @param param
	 * @return
	 */
	public Map<String, Object> getAreaGuList(Map<String, Object> param) {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> list = demandSgMapper.getAreaGuList(param);
		respMap.setBody("list", list);
		
		return respMap.getResponse();
	}
	
	/**
	 * 광고 소재 상세
	 * @param param
	 * @return
	 */
	public Map<String, Object> getMaterialDetail(Map<String, Object> param) {
		ResponseMap respMap = beanFactory.getResponseMap();

		Map<String, Object> detail = demandSgMapper.getMaterialDetail(param);
		
		if(!CommonUtil.checkIsNull(detail)) {
			respMap.setBody("data", detail);
		} else {
			return respMap.getResponse(Code.NOT_EXIST_DATA);
		}
		return respMap.getResponse();
	}
	
	// -- 광고주
}