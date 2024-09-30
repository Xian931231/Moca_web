package com.mocafelab.web.enums;

/**
 * 광고 소재 해상도 사이즈
 * @author cher1605
 *
 */
public enum SgPageSize {
	// 가로(1920x1080, 1600x1200, 2560x1080), 세로(1080x1920, 1200x1600, 1080x2560), 1:1(1080x1080)
	W_1920("1920x1080"),
	W_1600("1600x1200"),
	W_2560("2560x1080"),
	H_1920("1080x1920"),
	H_1600("1200x1600"),
	H_2560("1080x2560"),
	O_1080("1080x1080"),
	;
	
	String pageSize;
	
	SgPageSize(String pageSize) {
		this.pageSize = pageSize;
	}
	
	public String getPageSize() {
		return pageSize;
	}
	
	public static SgPageSize getPageSize(String pageSize) {
		for(SgPageSize ps : SgPageSize.values()) {
			if(ps.getPageSize().equals(pageSize)) {
				return ps;
			}
		}
		
		throw new RuntimeException();
	}
}
