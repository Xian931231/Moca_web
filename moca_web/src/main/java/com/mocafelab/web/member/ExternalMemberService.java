package com.mocafelab.web.member;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mocafelab.web.login.ExternalSessionUtil;
import com.mocafelab.web.menu.MenuService;
import com.mocafelab.web.vo.BeanFactory;
import com.mocafelab.web.vo.Code;
import com.mocafelab.web.vo.ResponseMap;

import net.newfrom.lib.util.CommonUtil;

@Service
public class ExternalMemberService {

	@Autowired
	private BeanFactory beanFactory;
	
	@Autowired
	private ExternalSessionUtil ExternalSessionUtil;
	
	@Autowired
	private MenuService menuService;
	
	@Autowired
	private ExternalMemberMapper externalMemberMapper;
	
	/**
	 * 로그인 정보 조회
	 * @param request
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getMyData(HttpServletRequest request, Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		Map<String, Object> myData = externalMemberMapper.getMyData(param);
		if (CommonUtil.checkIsNull(myData)) {
			return respMap.getResponse(Code.NOT_EXIST_DATA);
		} else {
			respMap.setBody("data",myData);
		}
		return respMap.getResponse();
	}
	
	/**
	 * 로그인한 사용자의 메뉴 목록 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getMenuList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> menuList = ExternalSessionUtil.getLoginMenuList();
		menuList = menuService.getTreeMenuList(menuList);
		
		respMap.setBody("list", menuList);
		
		return respMap.getResponse();
	}
}
