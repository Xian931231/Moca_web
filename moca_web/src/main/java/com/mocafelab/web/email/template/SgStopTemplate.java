package com.mocafelab.web.email.template;

public class SgStopTemplate extends Template {

	private String name;
	private String sgName;
	private String date;
	private String reason;
	
	public SgStopTemplate() {
		setTitle("[MoCAFE] 광고가 긴급종료 되었습니다.");
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSgName() {
		return sgName;
	}

	public void setSgName(String sgName) {
		this.sgName = sgName;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
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
												content.append("안녕하세요. <span style='font-weight: bold;'>" + name + "</span>님<br>");
												content.append("광고가 긴급종료 되어 안내드립니다.");
											content.append("</p>");
										content.append("</div>");
										content.append("<div class='account_box' style='width:680px; height:100px; background:#f9f9f9; display:flex; flex-direction:column; justify-content:center; line-height:30px;'>");
											content.append("<p style='margin-left: 21px; font-weight: bold;'>");
												content.append("광고명: " + sgName + "<br>");
												content.append("처리 일시: " + date + "<br>");
												content.append("사유: " + reason);
											content.append("</p>");
										content.append("</div>");
										content.append("<div class='issuance_box2' style='margin-top: 30px; line-height: 40px;'>");
											content.append("<p>");
												content.append("자세한 사항은 담당자에게 문의 부탁드립니다.");
											content.append("</p>");
											content.append("<p style='margin-top: 10px;'>");
												content.append("감사합니다.");
											content.append("</p>");
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
