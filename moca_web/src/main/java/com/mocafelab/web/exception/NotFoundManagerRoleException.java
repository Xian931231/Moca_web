package com.mocafelab.web.exception;

import com.mocafelab.web.vo.Code;

/**
 * 접근 메뉴에 대한 권힌이 없을때 발생하는 에러 
 * @author mure96
 *
 */
public class NotFoundManagerRoleException extends RuntimeException{
	private static final long serialVersionUID = -7304673580997556644L;
	public NotFoundManagerRoleException(Exception e) {
		super(e);
	}
	public NotFoundManagerRoleException(String msg, Exception e) {
		super(msg, e);
	}
	public NotFoundManagerRoleException(String msg) {
		super(msg);
	}
	public NotFoundManagerRoleException() {
		super(Code.MEMBER_ROLE_NOT_ACCEPT.msg);
	}
}
