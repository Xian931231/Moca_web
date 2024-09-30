package com.mocafelab.web.enums;

/**
 * 광고 소재 화면 비율
 * @author cher1605
 *
 */
public enum SgRatio {
	// 1:1(S), 가로소재(H), 세로소재(V)
	RATIO_S("S"),
	RATIO_H("H"),
	RATIO_V("V"),
	;
	
	String ratio;
	
	SgRatio(String ratio) {
		this.ratio = ratio;
	}
	
	public String getRatio() {
		return ratio;
	}
	
	public static SgRatio getRatio(String ratio) {
		for(SgRatio sr : SgRatio.values()) {
			if(sr.getRatio().equals(ratio)) {
				return sr;
			}
		}
		
		throw new RuntimeException();
	}
}
