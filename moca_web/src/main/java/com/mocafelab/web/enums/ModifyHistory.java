package com.mocafelab.web.enums;

/**
 * 광고주 활동 이력 > 활동 상태 타입
 * @author just3377
 *
 */
public enum ModifyHistory {
	// MHK001 (광고), MHK002 (캠페인), MHK003(회원정보)
	// MHS001(활동이력 - 등록), MHS003(활동이력 - 수정), MHS005(활동이력 - 삭제)
	CAMPAIGN_ADD("MHK002", "MHS001"),
	CAMPAIGN_MODIFY("MHK002", "MHS003"),
	CAMPAIGN_REMOVE("MHK002", "MHS005"),
	
	DEMAND_ADD("MHK001", "MHS001"),
	DEMAND_MODIFY("MHK001", "MHS003"),
	DEMAND_REMOVE("MHK001", "MHS005"),
	
	MEMBER_INFO_MODIFY("MHK003", "MHS003"),
	
	;
	
	String type;
	String kind;
	
	ModifyHistory(String kind, String type){
		this.type = type;
		this.kind = kind;
	}
	
	public String getType() {
		return type;
	}
	public String getKind() {
		return kind;
	}
	
	
}
