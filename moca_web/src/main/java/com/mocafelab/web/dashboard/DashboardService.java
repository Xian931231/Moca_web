package com.mocafelab.web.dashboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mocafelab.web.vo.BeanFactory;
import com.mocafelab.web.vo.ResponseMap;

@Service
public class DashboardService {
	
	@Autowired
	private BeanFactory beanFactory;
	
	@Autowired
	private DashboardMapper dashboardMapper;
	
	/**
	 * 대시보드 정보 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getDashboardDetail(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		Map<String, Object> count = dashboardMapper.getDashboardDetail(param);
		respMap.setBody("data", count);
		
		return respMap.getResponse();
	}
	
	/**
	 * 대시보드 노출 수 정보 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getDashboardExposure(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		int totalExposrue = dashboardMapper.getExposureTotal(param);
		List<Map<String, Object>> list = dashboardMapper.getExposureData(param);
		respMap.setBody("tot_exposure", totalExposrue);
		respMap.setBody("list", list);
		
		return respMap.getResponse();
	}
	
	/**
	 * 대시보드 집행금액 정보 조
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getDashboardPrice(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		int totalPrice = dashboardMapper.getPriceTotal(param);
		List<Map<String, Object>> list = dashboardMapper.getPriceData(param);
		respMap.setBody("tot_price", totalPrice);
		respMap.setBody("list", list);
		
		return respMap.getResponse();
	}
	
	/**
	 * 대시보드 매체, 구분, 상품 정보 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getDashboardSupply(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 전체 매체수
		int supplyCount = dashboardMapper.getSupplyCount(param);
		respMap.setBody("supply_cnt", supplyCount);
		
		// 운영 중인 상품 수
		int productCount = dashboardMapper.getProductCount(param);
		respMap.setBody("product_cnt", productCount);
		
		// 매체별 노출 수
		List<Map<String, Object>> supplyExposureList = dashboardMapper.getSupplyExpousreTotal(param);
		respMap.setBody("list", supplyExposureList);
		
		// 구분별 노출 수
		List<Map<String, Object>> categoryExposureList = dashboardMapper.getCategoryExposureTotal(param);
		
		for(Map<String, Object> supply : supplyExposureList) {
			List<Map<String, Object>> list = new ArrayList<>();
			
			String supplyId = String.valueOf(supply.get("member_id"));
			
			for(Map<String, Object> category : categoryExposureList) {
				String memberId = String.valueOf(category.get("member_id"));
				if(supplyId.equals(memberId)) {
					list.add(category);
				}
			}
			supply.put("categoryList", list);
		}
		respMap.setBody("list", supplyExposureList);
		
		return respMap.getResponse();
	}
	
	/**
	 * 대시보드 지도 zoomlevel에 따른 노출량 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getDashboardAreaList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		int level = Integer.valueOf(String.valueOf(param.get("map_level")));
		
		List<Map<String, Object>> list = new ArrayList<>();
		if(level <= 10) {
			list = dashboardMapper.getSiExposureTotal(param);
		} else if(level > 10 && level <= 13){
			list = dashboardMapper.getGuExposureTotal(param);
		} else {
			list = dashboardMapper.getMotorExposureTotal(param);		
			
			// 차량이 없는 경우
			if(list.size() == 0) {
				respMap.setBody("list", list);
				return respMap.getResponse();
			} else {
				param.put("motorList", list);
			}
			
			List<Map<String, Object>> productList = dashboardMapper.getProductExposureTotal(param);
			
			for(Map<String, Object> motor : list) {
				String mMotor_id = String.valueOf(motor.get("motor_id"));
				
				List<Map<String, Object>> tempProductList = new ArrayList<>();
				for(Map<String, Object> product : productList) {
					String dMotor_id = String.valueOf(product.get("motor_id"));
					
					if(mMotor_id.equals(dMotor_id)) {
						tempProductList.add(product);
					}
				}
				motor.put("product_list", tempProductList);
			}
		}
		respMap.setBody("list", list);
		
		return respMap.getResponse();
	}
}
