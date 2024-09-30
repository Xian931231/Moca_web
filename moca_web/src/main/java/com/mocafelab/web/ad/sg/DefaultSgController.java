package com.mocafelab.web.ad.sg;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartRequest;

import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.vo.RequestMap;

/**
 * 디폴트 광고 관리
 * @author mure96
 *
 */
@RestController
@RequestMapping("${apiPrefix}/sg/default")
public class DefaultSgController {
	
	@Autowired
	private DefaultSgService defaultSgService;
	
	
	
	@PostMapping("/list")
	public Map<String, Object> getDefaultSgList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		return defaultSgService.getDefaultSgList(param);
	}
	
	@PostMapping("/detail")
	public Map<String, Object> getDefaultSgDetail(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "dsp_service_ad_id");
		
		return defaultSgService.getDefaultSgDetail(param);
	}
	
	@PostMapping("/add")
	public Map<String, Object> addDefaultSg(RequestMap reqMap, MultipartHttpServletRequest mRequest) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "page_size_code");
		CommonUtil.checkNullThrowException(param, "ad_type");
		CommonUtil.checkNullThrowException(param, "name");
		CommonUtil.checkNullThrowException(mRequest, "dsp_service_ad_file");
		
		MultipartFile mFile = mRequest.getFile("dsp_service_ad_file");
		
		return defaultSgService.addDefaultSg(param, mFile);
	}
	
	@PostMapping("/modify")
	public Map<String, Object> modifyDefaultSg(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		return defaultSgService.modifyDefaultSg(param);
	}
	
	@PostMapping("/remove")
	public Map<String, Object> removeDefaultSg(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "id_list");
		
		return defaultSgService.removeDefaultSg(param);
	}
}
