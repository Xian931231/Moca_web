package com.mocafelab.web.product;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mocafelab.web.device.SupplyDeviceMapper;
import com.mocafelab.web.vo.BeanFactory;
import com.mocafelab.web.vo.ResponseMap;

@Service
public class SupplyProductService {

	@Autowired
	private BeanFactory beanFactory;
	
	@Autowired
	private SupplyProductMapper supplyProductMapper;
	
	@Autowired
	private SupplyDeviceMapper supplyDeviceMapper;
	
	/**
	 * 매체현황 > 전체 합계 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getProductTotalCnt(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 전체 노출 수
		int impressionsTotal = supplyProductMapper.getProductImpressionsCnt(param);
		respMap.setBody("impression_cnt", impressionsTotal);
		
		// 운영 중 상품 수
		int productTotal = supplyProductMapper.getProductCnt(param);
		respMap.setBody("product_cnt", productTotal);
		
		// 오류 장비 수
		int errorTotal = supplyDeviceMapper.getDeviceErrorCnt(param);
		respMap.setBody("error_cnt", errorTotal);
				
		return respMap.getResponse();
	}
	
	/**
	 * 매체현황 > 총 노출 수 리스트
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getProductImpressionsList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> list = supplyProductMapper.getProductImpressionsList(param);
		respMap.setBody("list", list);
		
		// 전체 노출 수
		int total = supplyProductMapper.getProductImpressionsCnt(param);
		respMap.setBody("tot_cnt", total);
		
		return respMap.getResponse();
	}
	
	/**
	 * 매체현황 > 운영 중 상품 리스트
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getProductProgressList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 분류
		List<Map<String, Object>> categoryList = supplyProductMapper.getCategoryList(param);
		
		// 상품
		List<Map<String, Object>> productList = supplyProductMapper.getProductProgressList(param);
		
		respMap.setBody("list",	groupingProduct(categoryList, productList));
		
		// 운영 중 상품 수
		int total = supplyProductMapper.getProductCnt(param);
		respMap.setBody("tot_cnt", total);

		return respMap.getResponse();
	}
	
	/**
	 * 상품관리 > 상품관리
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getProductManageList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 분류 리스트
		List<Map<String, Object>> categoryList = supplyProductMapper.getCategoryList(param);
		
		// 상품 리스트
		List<Map<String, Object>> productList = supplyProductMapper.getProductManageList(param);
		
		respMap.setBody("list",	groupingProduct(categoryList, productList));

		int total = supplyProductMapper.getCategoryCnt(param);
		respMap.setBody("tot_cnt",	total);
		
		return respMap.getResponse();
	}
	
	/**
	 * 분류 > 상품 그룹화
	 * @param categoryList
	 * @param productList
	 * @return
	 * @throws Exception
	 */
	private List<Map<String, Object>> groupingProduct(List<Map<String, Object>> categoryList, List<Map<String, Object>> productList) throws Exception {
		
		List<Map<String, Object>> resultList = new ArrayList<>();
		
		for(Map<String, Object> category : categoryList) { // 분류
			String categoryId = String.valueOf(category.get("category_id"));
			
			List<Map<String, Object>> productListWithCategory = new ArrayList<>();
			for(Map<String, Object> product : productList) { // 상품
				String categoryIdInProduct = String.valueOf(product.get("ssp_category_id"));
				if(categoryId.equals(categoryIdInProduct)) {
					productListWithCategory.add(product);
				}
			}
			if(productListWithCategory.size() > 0) {
				category.put("product_list", productListWithCategory);
				resultList.add(category);
			}
		}
		
		return resultList;
	}
	
}
