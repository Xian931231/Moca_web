package com.mocafelab.web.enums;

/**
 * 사용자 권한구분 타입 
 * A: 관리자 admin
 * B: 대행사 agency
 * D: 광고주 demand
 * S: 매체사 supply
 * E: 현장매체관리자 external
 * @author mure96
 *
 */
public enum MemberType {
	ADMIN("A", ""),
	AGENCY("B", "/agency"),
	DEMAND("D", "/demand"),
	SUPPLY("S", "/supply"),
	EXTERNAL("E", "");
	
	private String utype;
	private String modifyUrl;
	
	MemberType(String utype, String modifyUrl) {
		// TODO Auto-generated constructor stub
		this.utype = utype;
		this.modifyUrl = modifyUrl;
	}
	
	public String getType() {
		return utype;
	}
	
	public String getModifyUrl() {
		return "/login?returnUrl=" + modifyUrl + "/member/info/modify";
	}
	
	public static MemberType getType(String utype) {
		for(MemberType mType : MemberType.values()) {
			if(mType.getType().equals(utype)) {
				return mType;
			}
		}
		throw new RuntimeException();
	}
}