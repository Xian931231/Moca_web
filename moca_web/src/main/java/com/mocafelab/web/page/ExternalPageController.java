package com.mocafelab.web.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ExternalPageController {
	/*
	 * 관리자 별도 페이지 컨트롤러
	 */

	// 로그인
	@GetMapping("/external/login")
	public String externalLogin() {
		return "external/login/login";
	}
	
	// 상품 목록
	@GetMapping("/external/product/list")
	public String productList() {
		return "external/product/list";
	}
	
	// 상품 사양 등록
	@GetMapping("/external/product/spec/add")
	public String productAdd() {
		return "external/product/specAdd";
	}
	
	// 기기 목록
	@GetMapping("/external/device/list")
	public String deviceList() {
		return "external/device/list";
	}
	
	// 기기 등록
	@GetMapping("/external/device/add")
	public String deviceAdd() {
		return "external/device/add";
	}
	
	// 측정장비 목록
	@GetMapping("/external/sensor/list")
	public String sensorList() {
		return "external/sensor/list";
	}
	
	// 측정장비 등록
	@GetMapping("/external/sensor/add")
	public String sensorAdd() {
		return "external/sensor/add";
	}
	
	// 측정장비 수정
	@GetMapping("/external/sensor/modify")
	public String sensorModify() {
		return "external/sensor/modify";
	}
	
	// 측정장비 시리얼번호 등록
	@GetMapping("/external/sensor/device/add")
	public String sensorSerialAdd() {
		return "external/sensor/serialAdd";
	}
	
	// 게재위치 목록
	@GetMapping("/external/motor/list")
	public String MotorList() {
		return "external/motor/list";
	}
	
	// 구분 아이디 등록
	@GetMapping("/external/motor/id/add")
	public String motorIdAdd() {
		return "external/motor/idAdd";
	}
	
	// 기기/위치 매칭 목록
	@GetMapping("/external/matching/list")
	public String matchingList() {
		return "external/matching/list";
	}
	
	// 기기/위치 매칭 등록
	@GetMapping("/external/matching/add")
	public String matchingAdd() {
		return "external/matching/add";
	}
}
