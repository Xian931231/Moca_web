package com.mocafelab.web.product;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.mocafelab.web.ad.sg.DemandSgMapper;
import com.mocafelab.web.device.DeviceMapper;
import com.mocafelab.web.enums.PayType;
import com.mocafelab.web.file.S3Service;
import com.mocafelab.web.member.MemberMapper;
import com.mocafelab.web.vo.BeanFactory;
import com.mocafelab.web.vo.Code;
import com.mocafelab.web.vo.ResponseMap;

import net.newfrom.lib.file.S3UploadOption;
import net.newfrom.lib.file.UploadFileInfo;
import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.util.FileUtil;

@Service
public class ProductService {

	@Autowired
	private BeanFactory beanFactory;
	
	@Autowired
	private ProductMapper productMapper;
	
	@Autowired
	private MemberMapper memberMapper;
	
	@Autowired
	private DemandSgMapper demandSgMapper;
	
	@Autowired
	private DeviceMapper deviceMapper;
	
	@Autowired
	private S3Service s3;
	
	@Value("${file.path.ssp.product}")
	private String FILE_SSP_PRODUCT_PATH;
	
	@Value("${file.path.ssp.category}")
	private String FILE_SSP_CATEGORY_PATH;
	
	/**
	 * 매체/상품 목록 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getSupplyProductList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();

		// 매체사
		List<Map<String, Object>> supplyList = memberMapper.getProductSupplyList(param);
		
		// 상품
		List<Map<String, Object>> productList = productMapper.getSupplyProductList(param);

		if(productList.size() > 0) {
			List<Long> productInSupply = new ArrayList<>();
			
			for(Map<String, Object> product : productList) {
				productInSupply.add((long)product.get("member_id"));
			}
			
			productInSupply = productInSupply.stream().distinct().collect(Collectors.toList());
			param.put("product_list", productInSupply);
		}
		
		List<Map<String, Object>> categoryList = productMapper.getCategoryListBySupply(param);
		
		for(Map<String, Object> supply : supplyList) { // 매체사
			String supplyId = String.valueOf(supply.get("member_id"));
			List<Map<String, Object>> categoryListWithSupply = new ArrayList<>();
			
			for(Map<String, Object> category : categoryList) { // 분류
				if(productList.size() > 0) {
					String categorySupplyId = String.valueOf(category.get("member_id"));
					if(supplyId.equals(categorySupplyId)) {
						categoryListWithSupply.add(category);
					}
					
					String categoryId = String.valueOf(category.get("category_id"));
					List<Map<String, Object>> productListWithCategory = new ArrayList<>();
					
					for(Map<String, Object> product : productList) { // 상품
						String categoryIdInProduct = String.valueOf(product.get("category_id"));
						if(categoryId.equals(categoryIdInProduct)) {
							productListWithCategory.add(product);
						}
					}
					category.put("product_list", productListWithCategory);
				}
			}
			supply.put("category_list", categoryListWithSupply);
		}
		
		int total = memberMapper.getProductSupplyCnt(param);
		respMap.setBody("list", supplyList);
		respMap.setBody("tot_cnt", total);

		return respMap.getResponse();
	}
	
	/**
	 * 분류 목록
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getCategoryList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> list = productMapper.getCategoryList(param);
		respMap.setBody("list", list);
		
		int total = productMapper.getCategoryCnt(param);
		respMap.setBody("tot_cnt", total);
		
		return respMap.getResponse();
	}
	
	/**
	 * 분류 추가
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> addCategory(Map<String, Object> param, MultipartHttpServletRequest mRequest) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 관리자 확인
		if(!param.get("login_utype").equals("A")) {
			throw new RuntimeException();
		}
		
		// 매체 확인
		if(memberMapper.getSupplyCnt(param) <= 0) {
			throw new RuntimeException();
		}
		
		// 분류명 중복 확인
		if(productMapper.hasSupplyCategory(param) > 0) {
			throw new RuntimeException();
		}
		
		// 분류 추가
		Map<String, Object> category = productMapper.addCategory(param);
		param.put("ssp_category_id", category.get("ssp_category_id"));
		
		// 아이콘 등록
		modifyCategoryIcon(param, mRequest);
		
		return respMap.getResponse();
	}
	
	/**
	 * 아이콘 수정
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> modifyCategoryIcon(Map<String, Object> param, MultipartHttpServletRequest mRequest) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		Map<String, Object> category = productMapper.getCategoryDetail(param);
		if(!CommonUtil.checkIsNull(category, "icon_image")) {
			String savedIcon = (String) category.get("icon_image");
			
			if(savedIcon.indexOf(FILE_SSP_CATEGORY_PATH) != -1) {
				s3.removeFile(savedIcon);
			}
		}
		
		if(param.get("icon_type").equals("F")) {
			uploadIcon(param, mRequest);
		}
		
		if(productMapper.modifyCategoryIcon(param) <= 0) {
			throw new RuntimeException();
		}
		
		return respMap.getResponse();
	}
	
	/**
	 * 아이콘 이미지 업로드
	 * @param param
	 * @param mRequest
	 * @throws Exception
	 */
	private void uploadIcon(Map<String, Object> param, MultipartHttpServletRequest mRequest) throws Exception {
		
		MultipartFile mFile = mRequest.getFile("icon_image");
		
		String filePath = FILE_SSP_CATEGORY_PATH + File.separator + param.get("ssp_category_id");;
		
		// MIME TYPE
		String mimeType = mFile.getContentType();
		if(!mimeType.equals("image/jpeg") && !mimeType.equals("image/png")) {
			throw new RuntimeException();
		}

		// 사이즈 (55 x 55)
		Map<String, Object> fileInfo = FileUtil.getImgFileInfo(mFile);
		if((int) fileInfo.get("width") != 55 || (int) fileInfo.get("height") != 55) {
			throw new RuntimeException();
		}
		
		UploadFileInfo uploadFileInfo = s3.uploadFile(mFile, filePath);
		
		param.put("icon_image", uploadFileInfo.getFilePath());
		param.put("icon_file_name", uploadFileInfo.getFileName());
	}
	
	/**
	 * 분류 삭제
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> removeCategory(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 관리자 확인
		if(!param.get("login_utype").equals("A")) {
			throw new RuntimeException();
		}
		
		List<Integer> categoryList = (List<Integer>) param.get("ssp_category_id_list");
		for(int categoryId : categoryList) {
			
			param.put("ssp_category_id", categoryId);
			
			// 분류 내 상품 유무 확인
			if(productMapper.hasProductCnt(param) > 0) {
				return respMap.getResponse(Code.ADMIN_CATEGORY_HAS_PRODUCT);
			}
			
			// 아이콘 삭제
			Map<String, Object> category = productMapper.getCategoryDetail(param);
			String icon = (String) category.get("icon_image");
			if(icon.indexOf(FILE_SSP_CATEGORY_PATH) != -1) {
				s3.removeFile(icon);
			}
			
			// 분류 삭제
			if(productMapper.removeCategory(param) < 0) {
				throw new RuntimeException();
			}
		}
		
		return respMap.getResponse();
	}
	
	/**
	 * 상품 목록
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getProductList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> list = productMapper.getProductList(param);
		respMap.setBody("list", list);
		
		int total = productMapper.getProductCnt(param);
		respMap.setBody("tot_cnt", total);
		
		return respMap.getResponse();
	}
	
	/**
	 * 상품 추가
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> addProduct(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 관리자 확인
		if(!param.get("login_utype").equals("A")) {
			throw new RuntimeException();
		}
		
		// 동일 분류 내 상품명 중복 확인
		if(productMapper.hasProductName(param) > 0) {
			throw new RuntimeException();
		}
		
		// 상품 추가
		productMapper.addProduct(param);
		
		return respMap.getResponse();
	}
	
	/**
	 * 상품 사양 등록,수정
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> modifyProductSpec(Map<String, Object> param, MultipartHttpServletRequest mRequest) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 상품 유무 확인
		Map<String, Object> productMap = productMapper.hasProduct(param);
		if(CommonUtil.checkIsNull(productMap)) {
			throw new RuntimeException();
		} else {
			if(!String.valueOf(param.get("product_name")).equals(productMap.get("product_name"))) {
				// 상품명 수정 시 동일 분류 내 상품명 중복 확인
				param.put("category_id", productMap.get("category_id"));
				if(productMapper.hasProductName(param) > 0) {
					throw new RuntimeException();
				}
			}
		}
		
		MultipartFile mFile = mRequest.getFile("product_file");

		// 사양 수정 => 파일이 바뀌는 경우 기존 파일 삭제
		if(mFile != null && productMap.get("saved_product_image") != null) {
			s3.removeFile((String) productMap.get("saved_product_image"));
		}
		
		// 기본 정보 수정
		if(productMapper.modifyProduct(param) <= 0) {
			throw new RuntimeException();
		}
		
		// /mocafe/ssp/product/
		String filePath = FILE_SSP_PRODUCT_PATH + File.separator + param.get("ssp_product_id");
		UploadFileInfo uploadFileInfo = null;
		
		if(mFile != null) {
			// 파일 업로드
			S3UploadOption uploadOptions = (S3UploadOption) s3.getUploadOption();
			uploadOptions.setContentDispoisitionPrefix("inline");
			s3.setUploadOption(uploadOptions);
			uploadFileInfo = s3.uploadFile(mFile, filePath);
			param.put("product_image", uploadFileInfo.getMap().get("file_path"));
			param.put("product_file_name", uploadFileInfo.getFileName());
		}
		
		// 상품 사양 등록,수정
		if(productMapper.modifyProductSpec(param) <= 0) {
			if(uploadFileInfo != null) {
				s3.removeFile(uploadFileInfo.getFilePath());
			}
			throw new RuntimeException();
		}
		
		return respMap.getResponse();
	}
	
	/**
	 * 상품 상세보기
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getProductDetail(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();

		Map<String, Object> data = productMapper.getProductDetail(param);
		
		if(CommonUtil.checkIsNull(data)) {
			return respMap.getResponse(Code.NOT_EXIST_DATA);
		}
		
		// 상품 단가 계산
		int priceBasic = PayType.CPP.getPrice();
		float priceRate = (float) ((int) data.get("price_rate")) / 100;
		int pricePay = (int) Math.round(priceBasic * priceRate);
		
		data.put("price_pay", pricePay);
		
		respMap.setBody("data", data);
		
		return respMap.getResponse();
	}
	
	/**
	 * 상품 삭제
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> removeProduct(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 관리자 확인
		if(!param.get("login_utype").equals("A")) {
			throw new RuntimeException();
		}
		
		if(productMapper.hasProgressSgInProduct(param) > 0) {
			return respMap.getResponse(Code.ADMIN_PRODUCT_HAS_SG);
		}
		
		String fileFullPath = (String) productMapper.getProductDetail(param).get("product_image");
		
		if(deviceMapper.removeDevice(param) < 0) {
			throw new RuntimeException();
		}
		
		// 상품 삭제
		if(productMapper.removeProduct(param) > 0) {
			// 파일 삭제
			s3.removeFile(fileFullPath);
		}
		
		return respMap.getResponse();
	}
	
	/**
	 * 상품 판매 시작
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> startProductSale(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 관리자 확인
		if(!param.get("login_utype").equals("A")) {
			throw new RuntimeException();
		}
		
		// 상품 사양 등록 여부 확인
		Map<String, Object> product = productMapper.getProductDetail(param);
		String status = (String) product.get("status");
		
		// 필수값 X
		product.remove("deny_category_main");
		product.remove("deny_category_middle");
		product.remove("deny_category_sub");
		product.remove("deny_category_code1");
		product.remove("deny_category_code2");
		product.remove("deny_category_code3");
		product.remove("notes");
		
		for(String key: product.keySet()) {
			if(CommonUtil.checkIsNull(product, key) || !status.equals("S")) {
				// 상품 디바이스 등록 여부 확인
				if(key.equals("device_insert_date")) {
					return respMap.getResponse(Code.ADMIN_PRODUCT_NOT_EXIST_DEVICE);
				}
				
				return respMap.getResponse(Code.ADMIN_PRODUCT_SPEC_IS_NULL);
			}
		}
		
		if(productMapper.startProductSale(param) <= 0) {
			throw new RuntimeException();
		}
		
		return respMap.getResponse();
	}
	
	/**
	 * CPP 진행 상품 리스트
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getCppProductList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();

		List<Map<String, Object>> list = productMapper.getCppProductList(param);
		
		param.put("rate_kind", "TIME_TYPE");
		List<Map<String, Object>> rateList = demandSgMapper.getRateList(param);
		// 일일 금액
		for (Map<String, Object> item : list) {
			String rateCode = "SEC" + item.get("play_time");
			for(Map<String, Object> rateData : rateList) {
				if(rateCode.equals(rateData.get("rate_code"))) {
					float rate = (float) (int) rateData.get("rate") / 100;
					float priceRate = (float) (int)item.get("price_rate") / 100;
					int price = (int) (PayType.CPP.getPrice() * rate * priceRate);
					item.put("price", price);
				}
			}
		}
		
		respMap.setBody("list", list);
		
		int total = productMapper.getCppProductCnt(param);
		respMap.setBody("tot_cnt", total);
		
		return respMap.getResponse();
	}

	/**
	 * 분류 상세
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getCategoryDetail(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		Map<String, Object> categoryDetail = productMapper.getCategoryDetail(param);
		
		if(CommonUtil.checkIsNull(categoryDetail)) {
			return respMap.getResponse(Code.NOT_EXIST_DATA);
		}
		
		respMap.setBody("data" , categoryDetail);
		
		return respMap.getResponse();
	}
}
