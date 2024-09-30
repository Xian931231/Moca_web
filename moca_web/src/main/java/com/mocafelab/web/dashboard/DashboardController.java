package com.mocafelab.web.dashboard;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.newfrom.lib.vo.RequestMap;

/**
 * 관리자 dashboard controller
 */

@RestController
@RequestMapping("${apiPrefix}/dashboard")
public class DashboardController {
	
	@Autowired
	private DashboardService dashboardService;
	
	/**
	 * 대시보드 카드정보 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/detail")
	public Map<String, Object> getDashboardDetail(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return dashboardService.getDashboardDetail(param);
	}
	
	/**
	 * 대시보드 노출 수 정보 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/exposure")
	public Map<String, Object> getDashboardExposure(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return dashboardService.getDashboardExposure(param);
	}
	
	/**
	 * 대시보드 집행금액 정보 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/price")
	public Map<String, Object> getDashboardPrice(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return dashboardService.getDashboardPrice(param);
	}
	
	/**
	 * 대시보드 매체, 구분, 상품 정보 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/supply")
	public Map<String, Object> getDashboardSupply(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return dashboardService.getDashboardSupply(param);
	}
	
	/**
	 * 대시보드 지도 zoomlevel에 따른 노출량 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/area/list")
	public Map<String, Object> getDashboardAreaList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return dashboardService.getDashboardAreaList(param);
	}
}
