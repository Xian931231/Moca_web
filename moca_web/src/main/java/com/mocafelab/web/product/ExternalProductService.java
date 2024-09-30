package com.mocafelab.web.product;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.mocafelab.web.device.ExternalDeviceMapper;
import com.mocafelab.web.file.S3Service;
import com.mocafelab.web.vo.BeanFactory;
import com.mocafelab.web.vo.Code;
import com.mocafelab.web.vo.ResponseMap;

import net.newfrom.lib.file.UploadFileInfo;
import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.util.FileUtil;

@Service
@Transactional(rollbackFor = Exception.class)
public class ExternalProductService {
	
	@Autowired
	private ExternalProductMapper externalProductMapper;
	
	@Autowired
	private ExternalDeviceMapper externalDeviceMapper;
	
	@Autowired
	private BeanFactory beanFactory;
	
	@Autowired
	private S3Service s3Service;
	
	@Value("${file.path.ssp.product}")
	private String FILE_SSP_PRODUCT_PATH;
	
	private final List<String> ALLOWED_EXT = List.of("jpg", "jpeg", "png", "gif", "bmp");
	
	/**
	 * 상품 리스트
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> productList = externalProductMapper.getList(param);
		
		// 상품 아이디에 해당하는 디바이스 목록 맵핑
		if(!CommonUtil.checkIsNull(param, "is_with_device")) {
			String isWithDevice = (String)param.get("is_with_device");
			
			if(isWithDevice.equals("Y")) {
				productList.forEach(product -> {
					product.put("is_not_matching", "Y");
					product.put("status", param.get("status"));
					product.put("except_motor_id", param.get("except_motor_id"));
					
					List<Map<String, Object>> deviceList = externalDeviceMapper.getList(product); 
					
					product.put("device_list", deviceList);
				});
			}
		}
		respMap.setBody("list", productList);
		
		return respMap.getResponse();
	}
	
	/**
	 * 상품 상세
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getProductSpecDetail(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		Map<String, Object> productSpecDetail = externalProductMapper.getProductSpecDetail(param);
		
		if(CommonUtil.checkIsNull(productSpecDetail)) {
			return respMap.getResponse(Code.NOT_EXIST_DATA);
		}
		
		respMap.setBody("data", productSpecDetail);
		
		return respMap.getResponse();
	}
	
	/**
	 * 상품 스팩 등록
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> addProductSpec(Map<String, Object> param, MultipartHttpServletRequest mRequest) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		MultipartFile mFile = mRequest.getFile("product_image");
		
		UploadFileInfo uploadFileInfo = null;
				
		// 파일 업로드
		if(mFile != null) {
			if(!isAllowdFileType(mFile)) {
				throw new RuntimeException();
			}
			
			// 기존에 등록된 파일이 있으면 삭제
			Map<String, Object> productSpecDetail = externalProductMapper.getProductSpecDetail(param);
			
			if(!CommonUtil.checkIsNull(productSpecDetail, "product_image")) {
				String savedFile = (String) productSpecDetail.get("product_image");
				
				if(s3Service.removeFile(savedFile) == false) {
					throw new RuntimeException();
				}
			}
			uploadFileInfo = s3Service.uploadFile(mFile, FILE_SSP_PRODUCT_PATH + File.separator + param.get("product_id")); 
			param.put("add_product_image", uploadFileInfo.getFilePath());
			param.put("add_proeuct_file_name", uploadFileInfo.getFileName());
		}
		
		// 상품의 상태가 P이면 상태를 S로 변경하고 스팩등록일을 등록한다.
		Map<String, Object> product = externalProductMapper.getProductSpecDetail(param);
		param.put("status", product.get("status"));
		
		// 상품 사양 등록,수정
		if(externalProductMapper.addProductSpec(param) <= 0) {
			s3Service.removeFile(uploadFileInfo.getFilePath());
			
			throw new RuntimeException();
		} 
		return respMap.getResponse();
	}
	
	/**
	 * 상품이 존재하는 매체 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getSupplyList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> supplyList = externalProductMapper.getSupplyList(param);
		
		respMap.setBody("list", supplyList);
		
		return respMap.getResponse();
	}
	
	/**
	 * 상품이 존재하는 분류 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getCategoryList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> categoryList = externalProductMapper.getCategoryList(param);
		
		respMap.setBody("list", categoryList);
		
		return respMap.getResponse();
	}
	
	/**
	 * 파일 mime, ext 체크 
	 * @param mFile
	 * @return
	 * @throws Exception
	 */
	public boolean isAllowdFileType(MultipartFile mFile) throws Exception {
		String ext = FileUtil.getFileExt(mFile.getOriginalFilename());
		
		String mimeType = mFile.getContentType();
		
		return mimeType.startsWith("image") && ALLOWED_EXT.contains(ext) && ALLOWED_EXT.stream().anyMatch(allowedExt -> mimeType.contains(allowedExt));
	}
}
