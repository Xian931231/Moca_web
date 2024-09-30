package com.mocafelab.web.email.template;

public class DeviceErrorNoticeTemplate extends Template {

	private String name;
	private String deviceName;
	private String serialNumber;
	private String date;
	
	public DeviceErrorNoticeTemplate() {
		setTitle("[MoCAFE] 디바이스 상태 이상 알림");
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
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
												content.append("상태 이상 디바이스가 접수되어 안내 드립니다.");
											content.append("</p>");
										content.append("</div>");
										content.append("<div class='account_box' style='width:680px; height:130px; background:#f9f9f9; display:flex; flex-direction:column; justify-content:center; line-height:30px;'>");
											content.append("<p style='margin-left: 21px; font-weight: bold;'>");
												content.append("기기명: " + deviceName + "<br>");
												content.append("기기번호: " + serialNumber + "<br>");
												content.append("접수 일시: " + date);
											content.append("</p>");
										content.append("</div>");
										content.append("<div class='issuance_box2' style='margin-top: 30px; line-height: 40px;'>");
											content.append("<p>");
												content.append("모카페 플랫폼의 많은 이용 부탁드립니다.");
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
