package com.mocafelab.web.email.template;

public class Template {

	static final String ADMIN_EMAIL = "moca@innocean.com";
	static final String ADMIN_TEL = "02-1234-1234";
	static final String BASIC_URL = "https://mocafelab.com";
	
	private String title;
	private String content;
	
	public String getTitle() {
		return title;
	};
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content= content;
	}
}

