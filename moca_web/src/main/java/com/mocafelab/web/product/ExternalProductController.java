package com.mocafelab.web.product;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.vo.RequestMap;

/**
 * 관리자 별도 페이지
 * @author lky
 *
 */
@RestController
@RequestMapping("${apiPrefix}/external/product")
public class ExternalProductController {
	
	@Autowired
	private ExternalProductService externalProductService;
	
	/**
	 * 상품 리스트
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/list")
	public Map<String, Object> getList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "category_id");
		
		return externalProductService.getList(param);
	}
	
	/**
	 * 상품 스팩 상세
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/spec/detail")
	public Map<String, Object> getProductSpecDetail(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "product_id");
		
		return externalProductService.getProductSpecDetail(param);
	}
	
	/**
	 * 상품 스팩 등록
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/spec/add")
	public Map<String, Object> addProductSpec(RequestMap reqMap, MultipartHttpServletRequest mRequest) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "product_id");
		
		return externalProductService.addProductSpec(param, mRequest);
	}
	
	/**
	 * 상품이 존재하는 매체 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/supply/list")
	public Map<String, Object> getSupplyList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return externalProductService.getSupplyList(param);
	}
	
	/**
	 * 상품이 존재하는 분류 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/category/list")
	public Map<String, Object> getCategoryList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return externalProductService.getCategoryList(param);
	}
}
