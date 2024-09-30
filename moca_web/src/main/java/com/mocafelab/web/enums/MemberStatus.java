package com.mocafelab.web.enums;

/**
 * 회원 상태 enum
 * @author mure96
 *
 */
public enum MemberStatus {
	// A(정상활동), R(탈퇴 요청), L(탈퇴), W(인증대기), H(중단), D(휴면), V(승인대기), X(승인거부)
	OK("A"),
	LEAVE_REQUEST("R"),
	LEAVE("L"),
	AUTH_WAIT("W"),
	SERVICE_HOLD("H"),
	HUMAN_MEMBER("D"),
	APPROVE_WAIT("V"),
	APPROVE_REJECT("X")
	;
	
	String status;
	
	MemberStatus(String status) {
		this.status = status;
	}
	
	public String getStatus() {
		return status;
	}
	
	public static MemberStatus getStatus(String status) {
		for(MemberStatus ms : MemberStatus.values()) {
			if(ms.getStatus().equals(status)) {
				return ms;
			}
		}
		
		throw new RuntimeException();
	}
}
