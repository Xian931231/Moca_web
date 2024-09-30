package com.mocafelab.web.alarm;

import com.mocafelab.web.email.template.Template;

/**
 * 알람 데이터
 * @author cher1605
 *
 */
public class AlarmData {

	private String email;
	private String title;
	private String content;
	private Template template;
	
	public AlarmData() {
		
	}
	
	public AlarmData(String email, Template template) {
		this.email = email;
		this.template = template;
		this.title = template.getTitle();
		this.content = template.getContent();
	}
	
	public String getEmail() {
		return email;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getContent() {
		return content;
	}
	
	public Template getTemplate() {
		return template;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setTemplate(Template template) {
		this.template = template;
	}
}
