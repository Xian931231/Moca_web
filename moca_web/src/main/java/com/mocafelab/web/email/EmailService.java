package com.mocafelab.web.email;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailService {

	@Autowired
	private JavaMailSender mailSender;
	
	@Value("${spring.mail.from}")
	private String FROM_MAIL;
	
	/**
	 * 이메일 전송
	 * @param receiveEmail
	 * @param title
	 * @param content
	 * @throws Exception
	 */
	public void sendEmail(String receiverEmail, String title, String content) throws Exception {
		// 메일 설정
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "UTF-8");
		
		// 발신자 설정
		messageHelper.setFrom(FROM_MAIL);
		
		// 수신자 설정
		messageHelper.setTo(receiverEmail);
		
		// 제목 설정
		messageHelper.setSubject(title);
		
		// 내용 설정
		messageHelper.setText(content, true);
		
		// 메일 발송
		mailSender.send(message);
	}

	/**
	 * html의 지정 부분을 변환하여 리턴
	 * @param param 
	 * @param filePath
	 * @return
	 * @throws Exception
	 */
	public String replaceHtml(Map<String, Object> param, String filePath) throws Exception {
		
		ClassPathResource resource= new ClassPathResource(filePath);
		
		BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()));
		
		StringBuilder sb = new StringBuilder();
		
		// html 읽어오기
		while (true) {
			String line = br.readLine();
			
			if (line == null) {
				break;
			}
			
			sb.append(line);
		}
		
		Set<Entry<String, Object>> entrySet = param.entrySet();
		
		// 지정 문자열 치환
		String result = sb.toString();
		for (Map.Entry<String, Object> entry : entrySet) {
			result = result.replace("###" + entry.getKey() + "###", String.valueOf(entry.getValue()));
		}
		
		br.close();
		
		return result;
	}
}
