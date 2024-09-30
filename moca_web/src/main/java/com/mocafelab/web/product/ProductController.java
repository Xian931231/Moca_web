package com.mocafelab.web.product;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.vo.RequestMap;

@RestController
@RequestMapping("${apiPrefix}/product")
public class ProductController {

	@Autowired
	private ProductService productService;
	
	/**
	 * 매체/상품 목록 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/all/list")
	public Map<String, Object> getSupplyProductList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return productService.getSupplyProductList(param);
	}
	
	/**
	 * 분류 목록
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/category/list")
	public Map<String, Object> getCategoryList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return productService.getCategoryList(param);
	}
	
	/**
	 * 분류 추가
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/category/add")
	public Map<String, Object> addCategory(RequestMap reqMap, MultipartHttpServletRequest mRequest) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return productService.addCategory(param, mRequest);
	}
	
	/**
	 * 아이콘 수정
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/category/modify/icon")
	public Map<String, Object> modifyCategoryIcon(RequestMap reqMap, MultipartHttpServletRequest mRequest) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return productService.modifyCategoryIcon(param, mRequest);
	}
	
	/**
	 * 분류 삭제
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/category/remove")
	public Map<String, Object> removeCategory(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return productService.removeCategory(param);
	}
	
	/**
	 * 상품 목록
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/list")
	public Map<String, Object> getProductList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return productService.getProductList(param);
	}
	
	/**
	 * 상품 추가
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/add")
	public Map<String, Object> addProduct(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return productService.addProduct(param);
	}
	
	/**
	 * 상품 사양 등록, 수정
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/modify")
	public Map<String, Object> modifyProductSpec(RequestMap reqMap, MultipartHttpServletRequest mRequest) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return productService.modifyProductSpec(param, mRequest);
	}
	
	/**
	 * 상품 상세보기
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/detail")
	public Map<String, Object> getProductDetail(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return productService.getProductDetail(param);
	}
	
	/**
	 * 상품 삭제
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/remove")
	public Map<String, Object> removeProduct(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return productService.removeProduct(param);
	}
	
	/**
	 * 상품 판매 시작
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/sale/start")
	public Map<String, Object> startProductSale(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return productService.startProductSale(param);
	}
	
	/**
	 * CPP 진행 상품 리스트
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/cpp/list")
	public Map<String, Object> cppProgressProductList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return productService.getCppProductList(param);
	}

	/**
	 * 분류 상세
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/category/detail")
	public Map<String, Object> getCategoryDetail(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "ssp_category_id");
		
		return productService.getCategoryDetail(param);
	}
	
}
