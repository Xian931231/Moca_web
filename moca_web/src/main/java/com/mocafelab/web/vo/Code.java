package com.mocafelab.web.vo;

public enum Code {
	/**
	 * 정상 
	 */
	OK(200, "code.ok"),
	
	/**
	 * 데이터 미존재 
	 */
	NOT_EXIST_DATA(404, "code.not.exist.data"),
	
	/**
	 * 에러
	 */
	ERROR(500, "code.error"),
	ERROR_SQL(501, "code.error.sql"),
	
	FILE_UPLOAD_FAIL(520, "code.file.upload.fail"), // 안쓰임
	FILE_DELETE_FAIL(521, "code.file.delete.fail"), // 안쓰임
	FILE_DOWNLOAD_FAIL(522, "code.file.download.fail"), // 안쓰임
	LOGOUT_FAIL(523, "code.logout.fail"), // 삭제 필요 
	
	/**
	 * login
	 */
	LOGIN_FAIL(1000, "code.login.fail"),
	LOGIN_FAIL_LEAVE(1001, "code.login.fail.leave"),
	LOGIN_FAIL_WAITING(1002, "code.login.fail.waiting"),
	LOGIN_FAIL_HALT(1003, "code.login.fail.halt"),
	LOGIN_FAIL_DORMANCY(1004, "code.login.fail.dormancy"),
	LOGIN_FAIL_COUNT_5(1005, "code.login.fail.count.5"),
	LOGIN_INFO_IS_NULL(1008, "code.login.info.is.null"),
	LOGIN_IS_DUPLICATED(1009, "code.login.is.duplicated"),
	LOGIN_FAIL_APPROVE_WAIT(1010, "code.login.fail.approve.wait"),
	LOGIN_FAIL_APPROVE_REJECT(1011, "code.login.fail.approve.reject"),
	LOGIN_FAIL_EXTERNAL(1012, "code.login.fail.external"),
	LOGIN_FAIL_UNAUTHORIZED(1013, "code.login.fail"),
	
	/**
	 * Member
	 */
	MEMBER_DUPLICATE_ID(2200, "code.member.duplicate.id"),
	MEMBER_ID_IS_NOT_VALID(2201, "code.member.id.is.not.valid"),
	MEMBER_DUPLICATE_EMAIL(2202, "code.member.duplicate.email"),
	MEMBER_EMAIL_IS_NOT_VALID(2203, "code.member.email.is.not.valid"),
	MEMBER_MOBILE_IS_NOT_VALID(2205, "code.member.mobile.is.not.valid"),
	MEMBER_DUPLICATE_BIZNUM(2206, "code.member.duplicate.biznum"),
	MEMBER_AUTH_VALUE_IS_EXPIRED(2207, "code.member.auth.value.is.expired"),
	MEMBER_AUTH_IS_NOT_MATCH(2208, "code.member.auth.is.not.match"),
	MEMBER_INFO_IS_NOT_MATCH(2209, "code.member.info.is.not.match"),
	MEMBER_PW_IS_NOT_VALID(2210, "code.member.pw.is.not.valid"),
	MEMBER_PW_IS_EXIST(2211, "code.member.pw.is.exist"),
	MEMBER_PW_IS_NOT_MATCH(2212, "code.member.pw.is.not.match"),
	MEMBER_NEW_PW_IS_NOT_MATCH(2213, "code.member.new.pw.is.not.match"),
	MEMBER_INFO_IS_NULL(2214, "code.member.info.is.null"),
	MEMBER_LOGIN_HISTORY_IS_NULL(2215, "code.member.login.history.is.null"), // 삭제 필요 
	MEMBER_EQUALS_EMAIL(2216, "code.member.equals.email"),
	MEMBER_REGNUM_IS_NOT_VALID(2217, "code.member.regnum.is.not.valid"),
	MEMBER_ROLE_NOT_ACCEPT(2300, "code.member.role.not.accept"), 
	MEMBER_PROCESSING_SG(2301,"code.member.processing.sg"),
	MEMBER_PROCESSING_PRODUCT(2302,"code.member.processing.product"),

	
	/**
	 * api key(o)
	 */
	API_KEY_PARAM_IS_NOT_MATCH(3000, "code.api.key.param.is.not.match"),
	API_KEY_IS_EXIST(3001, "code.api.key.is.exist"),
	
	/**
	 * Menu
	 */
	MENU_DENY_IP(4004, "code.menu.deny.ip"), // 삭제 필요 
	
	/**
	 * Batch
	 */
	BATCH_CODE_NOT_EXIST(5001, "code.batch.monitor.not.exist"), // 안쓰임
	BATCH_LOG_NOT_READ(5002, "code.batch.log.not.read"), // 안쓰임

	/**
	 * sg
	 */
	SG_ALREADY_STOP(6000, "code.sg.already.stop"),
	SG_REASON_IS_NULL(6005, "code.sg.reason.is.null"),
	SG_ADD_FAIL(6001, "code.sg.add.fail"), // 안쓰임
	SG_BUDGET_DAY_IS_NOT_NUMBER(6002, "code.sg.budget.day.is.not.number"), // 삭제 필요 
	SG_PRICE_IS_OVER(6003, "code.sg.price.is.over"), // 삭제 필요 
	SG_FILE_IS_NOT_SUPPORT(6004, "code.sg.file.is.not.support"), // 삭제 필요 
	
	SG_MODIFY_FAIL(6100, "code.sg.modify.fail"), // 안쓰임
	
	/**
	 * campaign
	 */
	CAMPAIGN_ADD_MORE_PRICE(7003, "code.campaign.add.more.price"), 
	CAMPAIGN_MODIFY_MORE_PRICE(7004, "code.campaign.modify.more.price"),
	CAMPAIGN_MODIFY_LESS_PRICE(7005, "code.campaign.modify.less.price"),
	
	CAMPAIGN_EXIST_PROCEED_SG(7007, "code.campaign.exist.proceed.sg"),
	CAMPAIGN_NOT_NEED_CHECK(7009, "code.campaign.not.need.check"),
	
	/**
	 * admin
	 */
	ADMIN_STAFF_ROLE_NOT_ACCEPT(8001, "code.admin.staff.role.not.accept"),
	ADMIN_STAFF_NOT_EXIST(8002, "code.admin.staff.not.exist"),
	ADMIN_SCHEDULE_REMOVE_FAIL_PROGRESS_CPP(8003, "code.admin.schedule.remove.fail.progress.cpp"),
	ADMIN_CATEGORY_HAS_PRODUCT(8004, "code.admin.category.has.product"),
	ADMIN_SCHEDULE_REGISTERED_PRODUCT(8005, "code.admin.schedule.registered.product"),
	ADMIN_SCHEDULE_INVALID_PRODUCT(8006, "code.admin.schedule.invalid.product"),
	ADMIN_PRODUCT_HAS_SG(8007, "code.admin.product.has.sg"),
	ADMIN_PRODUCT_SPEC_IS_NULL(8008, "code.admin.product.spec.is.null"),
	ADMIN_PRODUCT_NOT_EXIST_DEVICE(8009, "code.admin.product.not.exist.device"),
	
	/**
	 * agency
	 */
	AGENCY_ROLE_DUPLICATE_NAME(8100, "code.agency.role.duplicate.name"),
	
	/**
	 * external
	 */
	EXTERNAL_DUPLICATE_SERIALNUM(9100, "code.external.duplicate.serialnum"),
	EXTERNAL_DUPLICATE_SENSOR_NAME(9101, "code.external.duplicate.sensor.name"),
	EXTERNAL_DUPLICATE_MOTOR_POSITION_NAME(9102, "code.external.duplicate.motor.position.name"),
	EXTERNAL_DUPLICATE_MOTOR_POSITION_ID(9103, "code.external.duplicate.motor.position.id"),
	EXTERNAL_DUPLICATE_MOTOR_POSITION_IP(9104, "code.external.duplicate.motor.position.ip"),
	EXTERNAL_ALREADY_MATCHING_DEVICE(9105, "code.external.already.matching.device"),
	EXTERNAL_NOT_IN_CATEGORY_DEVICE(9106, "code.external.not.in.category.device"),
	EXTERNAL_ALREADY_MATCHING_PRODUCT(9107, "code.external.already.matching.proudct"),
	EXTERNAL_ALREADY_MATCHING_SENSOR_DIVECE(9108, "code.external.already.matching.sensor.device"),
	EXTERNAL_NOT_IN_CATEGORY_SENSOR_DEVICE(9109, "code.external.not.in.category.sensor.device"),
	EXTERNAL_ALREADY_MATCHING_SENSOR(9110, "code.external.already.matching.sensor"),
	EXTERNAL_INVALID_MOTOR_POSITION_IP(9111, "code.external.invalid.motor.position.ip"),
	EXTERNAL_NOT_IN_SENSOR_DEVICE(9112, "code.external.invalid.not.in.sensor.device"),
	EXTERNAL_NOT_IN_PRODUCT_DEVICE(9113, "code.external.invalid.not.in.product.device"),
	EXTERNAL_MOTOR_REMOVE_FAIL_HAS_PROGRESS_CPP(9114, "code.external.motor.remove.fail.has.progress.cpp"),
	EXTERNAL_DEVICE_REMOVE_FAIL_HAS_PROGRESS_CPP(9115, "code.external.device.remove.fail.has.progress.cpp");
		
	public final int code;
	public final String msg;
	
	Code(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}
	
}