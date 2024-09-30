package com.mocafelab.web.menu;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mocafelab.web.vo.BeanFactory;
import com.mocafelab.web.vo.Code;
import com.mocafelab.web.vo.ResponseMap;

@Service
public class MenuService {
	
	@Autowired
	private BeanFactory beanFactory;
	
	@Autowired
	private MenuMapper menuMapper;
	
	/**
	 * 메뉴 등록
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> insertMenu(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		//메뉴 정보 유효성 체크
		if(getCheckMenuInfo(param)) {
			int addMenu = menuMapper.insertMenu(param);
			
			//등록 실패 시
			if(addMenu <= 0) {
				throw new RuntimeException();
			}
		}else {
			return respMap.getResponse(Code.NOT_EXIST_DATA);
		}
		
		return respMap.getResponse();
	}
	
	/**
	 * 메뉴 수정
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> updateMenu(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
			List<Map<String, Object>> list = (List<Map<String, Object>>) param.get("menu_json");
			
			for(Map<String, Object> info : list) {
				
				if(getCheckMenuInfo(info)) {
					if(menuMapper.updateMenu(info) <= 0) {
						throw new RuntimeException();
					}
				}else {
					return respMap.getResponse(Code.NOT_EXIST_DATA);
				}
			}
		
		return respMap.getResponse();
	}
	/**
	 * 메뉴 조회 (재귀함수)
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getMenuList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		//메뉴 리스트
		List<Map<String, Object>> menuList = menuMapper.getMenuList(param);
		//트리구조 재귀함수 사용
		List<Map<String, Object>> treeList = getAllTreeMenuList(menuList);
		respMap.setBody("list", treeList);
		
		return respMap.getResponse();
	}
	
	
	
	/**
	 * 등록, 수정 시 값 유효성 체크
	 * @param param
	 * @return
	 * @throws Exception
	 */
	private boolean getCheckMenuInfo(Map<String, Object> param) throws Exception{
		int step = (int)param.get("step");
		int parent_id = (int)param.get("parent_id");
		
		boolean chk = false;
		
		if(step !=1 && parent_id != 0) {
			Map<String, Object> getParentMenuInfo = menuMapper.getParentMenuInfo(param);
			
			//parent_id가 맞는지 체크
			if(getParentMenuInfo != null) {
				int oldMenu_step = (int)getParentMenuInfo.get("step");
				//step이 맞는지 체크
				if(step == (oldMenu_step+1)) {
					chk = true;
				}
			}
		}
		
		return chk;
	}
	
	/**
	 * 트리 구조 셋팅  (use_yn이 'Y'인 것들만, 사이드 바용)
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getTreeMenuList(List<Map<String, Object>> menuList)throws Exception{
		List<Map<String, Object>> mainList = menuList.stream().filter(item -> {
			return Long.valueOf(String.valueOf(item.get("parent_id"))) == 0 ;
		}).collect(Collectors.toList());
		
		mainList = recursiveTreeList(menuList, mainList);
		
		return mainList;
	}
	
	/**
	 * 메뉴리스트를 트리형 구조로 변경해주는 재귀함수 (use_yn이 'Y'인 것들만, 사이드 바용)
	 * @param fileList
	 * @param path
	 * @return
	 */
	private List<Map<String, Object>> recursiveTreeList(List<Map<String, Object>> dataList, List<Map<String, Object>> list)throws Exception{
		for(Map<String, Object> item : list) {
			if(item.containsKey("sub_list")) {
				continue;
			} else {
				List<Map<String, Object>> subList = dataList.stream().filter(dataItem -> dataItem.get("parent_id").equals(item.get("menu_id")) && dataItem.get("use_yn").equals("Y")).collect(Collectors.toList()); 
				subList = recursiveTreeList(dataList, subList);
				
				if(subList.size() > 0) {
					item.put("sub_list", subList);
				}
			}
		}
		
		return list;
	}
	
	/**
	 * 트리 구조 셋팅 (전체메뉴용)
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getAllTreeMenuList(List<Map<String, Object>> menuList)throws Exception{
		List<Map<String, Object>> mainList = menuList.stream().filter(item -> {
			return Long.valueOf(String.valueOf(item.get("parent_id"))) == 0 ;
		}).collect(Collectors.toList());
		
		mainList = recursiveAllTreeList(menuList, mainList);
		
		return mainList;
	}
	
	/**
	 * 메뉴리스트를 트리형 구조로 변경해주는 재귀함수 (전체메뉴용)
	 * @param fileList
	 * @param path
	 * @return
	 */
	private List<Map<String, Object>> recursiveAllTreeList(List<Map<String, Object>> dataList, List<Map<String, Object>> list)throws Exception{
		for(Map<String, Object> item : list) {
			if(item.containsKey("sub_list")) {
				continue;
			} else {
				List<Map<String, Object>> subList = dataList.stream().filter(dataItem -> dataItem.get("parent_id").equals(item.get("menu_id"))).collect(Collectors.toList()); 
				subList = recursiveAllTreeList(dataList, subList);
				
				if(subList.size() > 0) {
					item.put("sub_list", subList);
				}
			}
		}
		
		return list;
	}
	
	/**
	 * 상위 메뉴의 accessYn에 따라 하위 메뉴들의 access_yn 설정
	 * @param menuList
	 * @throws Exception
	 */
	public void setAccessYn(List<Map<String, Object>> menuList) throws Exception {
		for(Map<String, Object> menuItem : menuList) {
			Object parentId = menuItem.get("parent_id");
			// parent 찾기 
			if(parentId != null && !parentId.toString().equals("0")) {
				Optional<Map<String, Object>> opt = menuList.stream().filter(item -> {
					return item.get("menu_id").equals(parentId);
				}).findFirst();
				
				if(opt.isPresent()) {
					// 부모 객체가 있을 경우 
					Map<String, Object> parentObject = opt.get();
					Object parentAccessYn = parentObject.get("access_yn");
					
					if(parentAccessYn == null || parentAccessYn.equals("N")) {
						menuItem.put("access_yn", "N");
					}
				} else {
					// 부모 객체가 없는 경우 
					menuItem.put("access_yn", "N");
				}
			}
		}
	}
}
