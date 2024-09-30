package com.mocafelab.web.menu;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.vo.RequestMap;


/**
 * 메뉴 설정 
 * @author just3377 
 *
 */
@RestController
@RequestMapping("${apiPrefix}/menu")
public class MenuController {

	@Autowired
	private MenuService menuService;
	
	/**
	 * 메뉴 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/list")
	public Map<String, Object> menuList(RequestMap reqMap) throws Exception{
		Map<String, Object> param = reqMap.getMap();
		
		return menuService.getMenuList(param); 
	}
	/**
	 * 메뉴 등록
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/insert")
	public Map<String, Object> insertMenu(RequestMap reqMap) throws Exception{
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "name");
		CommonUtil.checkNullThrowException(param, "parent_id");
		CommonUtil.checkNullThrowException(param, "step");
		CommonUtil.checkNullThrowException(param, "sort");
		
		return menuService.insertMenu(param); 
	}
	
	/**
	 * 메뉴 수정
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/update")
	public Map<String, Object> updateMenu(RequestMap reqMap) throws Exception{
		Map<String, Object> param = reqMap.getMap();
		
		return menuService.updateMenu(param); 
	}
}
