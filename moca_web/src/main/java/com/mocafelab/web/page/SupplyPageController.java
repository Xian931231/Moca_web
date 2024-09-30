package com.mocafelab.web.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/supply")
public class SupplyPageController {

	/*
	 * 매체사
	 */
	// 매체 현황 (매체사 메인)
	@GetMapping("/main") 
	public String supplyMain() {
		return "supply/main";
	}
	
	// 상품관리 > 상품관리
	@GetMapping("/product/list") 
	public String supplyProductList() {
		return "supply/product/list";
	}
	
	// 상품관리 > 디바이스 관리
	@GetMapping("/product/device") 
	public String supplyProductDeviceList() {
		return "supply/product/device/list";
	}
	
	// 정산관리
	@GetMapping("/balance")
	public String supplyBalance() { 
		return "supply/balance/list";
	}
	
	//회원 정보 수정
	@GetMapping("/member/info/modify") 
	public String memberModify() {
		return "supply/member/info/modify";
	}
}
