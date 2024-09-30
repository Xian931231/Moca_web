package com.mocafelab.web.board.notice;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.newfrom.lib.vo.RequestMap;

@RequestMapping("${apiPrefix}/board/notice")
@RestController
public class BoardNoticeController {
	
	@Autowired
	private BoardNoticeService boardNoticeService; 
	
	@PostMapping("/list")
	public Map<String, Object> getList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return boardNoticeService.getList(param);
	}
	
	@PostMapping("/detail")
	public Map<String, Object> getDetail(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return boardNoticeService.getDetail(param);
	}
}
