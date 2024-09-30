package com.mocafelab.web.page;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import net.newfrom.lib.util.CookieUtil;

@Controller
public class PageController {
	@Value("${session.key.login.utype}")
	private String SESSION_KEY_LOGIN_UTYPE;
	
	/**
	 * 공통 페이지 컨트롤러
	 */
	
	@GetMapping("/") 
	public String home() { 
		return "/";
	}
	
	@GetMapping("/report") 
	public String report() {
		return "report/report";
	}
	
	// 로그인
	@GetMapping("/login") 
	public String login(HttpServletRequest request, HttpServletResponse response) {
		String localIp = request.getLocalAddr();
		localIp = localIp.substring(localIp.length()-1);
		
		CookieUtil.addCookie(request, response, "server_ip", localIp, 30, true);
		
		return "login/login";
	}
	
	// 아이디 찾기
	@GetMapping("/login/findId") 
	public String findId() {
		return "login/findId";
	}
	
	// 비밀번호 찾기
	@GetMapping("/login/findPw") 
	public String findPw() {
		return "login/findPw";
	}
	
	// 회원가입 약관
	@GetMapping("/signup/join") 
	public String join() {
		return "signup/join";
	}
	
	// 이용약관
	@GetMapping("/support/policy") 
	public String policy() {
		return "support/policy";
	}
	
	// 공지사항
	@GetMapping("/board/notice")
	public String boardNotice() {
		return "board/notice/list";
	}
	
	// 공지사항 상세
	@GetMapping("/board/notice/detail")
	public String boardNoticeDetail() {
		return "board/notice/detail";
	}
}
