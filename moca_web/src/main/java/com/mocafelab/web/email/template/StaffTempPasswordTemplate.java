package com.mocafelab.web.email.template;

/**
 * 관리자, 대행사 직원 생성 임시 비밀번호 이메일 템플릿 
 */
public class StaffTempPasswordTemplate extends Template {

	private String name;
	private String uid;
	private String tempPassword;
	private String returnUrl;
	
	public StaffTempPasswordTemplate() {
		setTitle("[MoCAFE] 임시 비밀번호가 생성되었습니다.");
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	public void setTempPassword(String tempPassword) {
		this.tempPassword = tempPassword;
	}

	public String getName() {
		return name;
	}

	public String getUid() {
		return uid;
	}

	public String getTempPassword() {
		return tempPassword;
	}
	
	public String getReturnUrl() {
		return returnUrl;
	}

	public void setReturnUrl(String returnUrl) {
		this.returnUrl = returnUrl;
	}
	
	@Override
	public String getContent() {
		StringBuilder content = new StringBuilder();
		content.append("<html lang='ko'>");
			content.append("<body style='background-color:#fff; width:100%; height:100%;'>");
				content.append("<div class='wrapper_join'>");
					content.append("<section id='login'>");
						content.append("<div class='all' style='width:800px; background: #fff; margin: 0 auto; padding-top: 50px;'>");
							content.append("<div style='flex-direction: column; align-items: center; width:800px;'>");
								content.append("<div class='all-box' style='margin-top: 40px; font-size: 18px;'>");
									content.append("<div class='password_text_box' style='padding:60px; line-height:30px; border: 1px solid #adadad;'>");
										content.append("<div class='password_issuance_box' style='margin-bottom: 1em;'>");
											content.append("<p style='margin-bottom: 0; line-height:40px;'>");
												content.append("안녕하세요.<span style='font-weight: bold;'>" + name + "</span>님<br>계정생성 요청에 따라 임시 비밀번호를 발급해 드렸습니다.");
											content.append("</p>");
										content.append("</div>");
										content.append("<div class='account_box' style='width:680px; height:140px; background:#f9f9f9; display:flex; flex-direction:column; justify-content:center; line-height:30px;'>");
											content.append("<p style='margin-left: 21px; font-weight: bold;'>");
												content.append("아이디: " + uid + "<br>");
												content.append("임시 비밀번호: " + tempPassword);
											content.append("</p>");
										content.append("</div>");
										content.append("<div class='issuance_box2' style='margin-top: 30px; line-height: 40px;'>");
											content.append("<p>");
												content.append("로그인 하신 후 우측 상단<span style='font-weight: bold; color: #f15a22;'> [정보 수정]</span>에서<br>새로운 비밀번호로 변경해 주시기 바랍니다.");
											content.append("</p>");
											content.append("<p style='margin-top: 10px;'>");
												content.append("감사합니다.");
											content.append("</p>");
										content.append("</div>");
										content.append("<div style='text-align: center;'>");
											content.append("<a href='" + BASIC_URL + returnUrl + "'>");
												content.append("<button class='btn btn-login' style='border:0; padding:15px 25px; font-size: 16px; color: #fff; background: #f15a22; margin-top: 30px;'>로그인 페이지로 이동</button>");
											content.append("</a>");
										content.append("</div>");
									content.append("</div>");
									content.append("<div class='questions_box' style='padding:35px 60px; font-size: 14px; line-height: 28px;background:#000; color:#fff;'>");
										content.append("<p>");
											content.append("※ 본 이메일은 발신 전용이므로 회신할 수 없습니다. 궁금하신 사항은<br>");
											content.append("E-mail : <a href=''>" + ADMIN_EMAIL + "</a> 으로 문의해주시기 바랍니다.");
										content.append("</p>");
									content.append("</div>");
								content.append("</div>");
							content.append("</div>");
						content.append("</div>");
					content.append("</section>");
				content.append("</div>");
			content.append("</body>");
		content.append("</html>");
		
	 	return content.toString();
	}
}
