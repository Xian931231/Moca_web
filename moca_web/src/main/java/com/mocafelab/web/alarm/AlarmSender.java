package com.mocafelab.web.alarm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.mocafelab.web.email.EmailService;

/**
 * 알람 발송에 필요한 데이터 설정
 * @author cher1605
 */
@Component
public class AlarmSender {
	
	@Autowired
	private EmailService emailService;
	
	/**
	 * 이메일 파라미터 값 설정
	 * @param template
	 * @throws Exception
	 */
	@Async
	public void sendEmail(AlarmData alarmData) {
		try {
			String email = alarmData.getEmail();
			String title = alarmData.getTitle();
			String content = alarmData.getContent();
			
			emailService.sendEmail(email, title, content);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
