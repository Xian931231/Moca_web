package com.mocafelab.web.ad.sg;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.mocafelab.web.common.CommonMapper;
import com.mocafelab.web.enums.SgPageSize;
import com.mocafelab.web.file.S3Service;
import com.mocafelab.web.vo.BeanFactory;
import com.mocafelab.web.vo.ResponseMap;

import lombok.extern.slf4j.Slf4j;
import net.newfrom.lib.file.UploadFileInfo;
import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.util.FileUtil;

/**
 * 디폴트 광고 관리
 * @author mure96
 *
 */
@Transactional(rollbackFor = {Exception.class})
@Service
@Slf4j
public class DefaultSgService {

	@Autowired
	private BeanFactory beanFactory;
	
	@Autowired 
	private DefaultSgMapper defaultSgMapper;
	
	@Autowired 
	private CommonMapper commonMapper;
	
	@Autowired
	private S3Service s3Service;
	
	@Value("${cloud.aws.credentials.profile-name}")
	private String AWS_CREDENTIALS_PROFILE_NAME;
	
	@Value("${cloud.aws.s3.bucket}")
	private String BUCKET_NAME;
	
	@Value("${file.path.upload.s3}")
	private String FILE_PATH_UPLOAD_S3;
	
	@Value("${file.path.ad.sg.default}")
	private String FILE_PATH_AD_SG_DEFAULT;
	
	@Value("${spring.servlet.multipart.max-file-size}")
	private String MAX_FILE_SIZE;
	
	/**
	 * 목록 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getDefaultSgList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> list = new ArrayList<>();
		
		for(SgPageSize sgPageSize : SgPageSize.values()) {
			String pageSizeCode = sgPageSize.getPageSize();
			param.put("page_size_code", pageSizeCode);
			
			Map<String, Object> sgInfo = new HashMap<>();
			sgInfo.put("page_size_code", pageSizeCode);
			sgInfo.put("tot_cnt", defaultSgMapper.getListCnt(param));
			sgInfo.put("sg_list", defaultSgMapper.getList(param));
			
			list.add(sgInfo);
		}
		
		respMap.setBody("list", list);
		
		return respMap.getResponse();
	}
	
	/**
	 * 상세 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getDefaultSgDetail(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		respMap.setBody("data", defaultSgMapper.getDetail(param));
		
		return respMap.getResponse();
	}
	
	/**
	 * 등록
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("serial")
	public Map<String, Object> addDefaultSg(Map<String, Object> param, MultipartFile mFile) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 광고 타입 검사
		String adType = String.valueOf(param.get("ad_type"));
		if(!adType.equals("P") && !adType.equals("D")) {
			throw new RuntimeException();
		}
		
		// 확장자 검사 
		String fileName = mFile.getOriginalFilename();
		String fileExt = FileUtil.getFileExt(fileName);
		
		if(fileExt == null) {
			throw new RuntimeException();
		}
		
		int fWidth = 0;
		int fHeight = 0;
		long fSize = mFile.getSize();
		String fType = "";
		fileExt = fileExt.toLowerCase();
		
		if(fileExt.equals("mp4") || fileExt.equals("mov")) {
			CommonUtil.checkNullThrowException(param, "width");
			CommonUtil.checkNullThrowException(param, "height");
			CommonUtil.checkNullThrowException(param, "playtime");
			
			fWidth = Integer.valueOf(String.valueOf(param.get("width")));
			fHeight = Integer.valueOf(String.valueOf(param.get("height")));
			
			fType = "VIDEO";
		} else {
			// 지원하지 않는 타입
			throw new RuntimeException();
		} 
		/* 이미지 제거 
		else if(fileExt.equals("jpg") || fileExt.equals("png")){
			Map<String, Object> imgInfo = FileUtil.getImgFileInfo(mFile);
			fWidth = Integer.valueOf(String.valueOf(imgInfo.get("width")));
			fHeight = Integer.valueOf(String.valueOf(imgInfo.get("height")));
			
			fType = "I";
		} 
		*/
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
		if(fWidth < width || fHeight < height) {
			throw new RuntimeException();
		}
		
		// 파일 크기 검사
		int maxFileSize = Integer.valueOf(MAX_FILE_SIZE.replace("MB", ""));
		if((fSize / (1024 * 1024)) > maxFileSize) {
			throw new RuntimeException();
		}
		
		// 등록
		int dspServiceAdId = defaultSgMapper.addDefaultSg(param);
		if(dspServiceAdId == 0) {
			throw new RuntimeException();
		}
		
		param.put("dsp_service_ad_id", dspServiceAdId);
		param.put("width", fWidth);
		param.put("height", fHeight);
		param.put("file_size", fSize);
		param.put("file_type", fType);
		
		// dsp_service_ad_id
		
		// 파일 업로드
		UploadFileInfo uploadFileInfo = s3Service.uploadFile(mFile, FILE_PATH_AD_SG_DEFAULT + File.separator + dspServiceAdId); 
		
		param.put("file_path", uploadFileInfo.getFilePath());
		param.put("file_name", uploadFileInfo.getFileName());
		
		defaultSgMapper.modifyFileInfo(param);
		
		return respMap.getResponse();
	}
	
	/**
	 * 수정
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> modifyDefaultSg(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		param.put("parent_code", "");
		
		Map<String, Object> pageSizeCodeDetail = commonMapper.getCodeDetail(param);
		
		;
		
		defaultSgMapper.modifyDefaultSg(param);
		
		return respMap.getResponse();
	}
	
	/**
	 * 삭제
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> removeDefaultSg(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Integer> idList = (List<Integer>) param.get("id_list");
		
		for(int id : idList) {
			param.put("dsp_service_ad_id", id);
			
			// 상세 정보 조회
			Map<String, Object> detail = defaultSgMapper.getDetail(param);
			
			if(CommonUtil.checkIsNull(detail) || CommonUtil.checkIsNull(detail, "file_path")) {
				throw new RuntimeException();
			}
			
			String fullPath = (String) detail.get("file_path");
			
			// 파일 삭제 
			if(s3Service.removeFile(fullPath) == false) {
				throw new RuntimeException();
			}
			
			// DB 삭제
			if(defaultSgMapper.removeDefaultSg(param) < 0) {
				throw new RuntimeException();
			}
		}
		
		return respMap.getResponse();
	}
	
}