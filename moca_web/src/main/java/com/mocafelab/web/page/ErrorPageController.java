package com.mocafelab.web.page;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorPageController implements ErrorController {
	
	@GetMapping("/error")
	public String error() {
		return "error";
	}
	
	@GetMapping("/error/role")
	public String errorRole() {
		return "errorRole";
	}
	
}
