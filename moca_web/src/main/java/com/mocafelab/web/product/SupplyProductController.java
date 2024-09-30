package com.mocafelab.web.product;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.newfrom.lib.vo.RequestMap;

@RestController
@RequestMapping("${apiPrefix}/supply/product")
public class SupplyProductController {

	@Autowired
	private SupplyProductService supplyProductService;
	
	/**
	 * 매체현황 > 총 노출 수 리스트
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/impressions/list")
	public Map<String, Object> getProductImpressionsList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return supplyProductService.getProductImpressionsList(param);
	}
	
	/**
	 * 매체현황 > 전체 합계
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/total")
	public Map<String, Object> getProductTotalCnt(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return supplyProductService.getProductTotalCnt(param);
	}
	
	/**
	 * 매체현황 > 운영 중 상품 리스트
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/progress/list")
	public Map<String, Object> getProductProgressList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return supplyProductService.getProductProgressList(param);
	}
	
	/**
	 * 상품관리 > 상품관리
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/manage/list")
	public Map<String, Object> getProductManageList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return supplyProductService.getProductManageList(param);
	}
	
}
