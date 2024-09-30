package com.mocafelab.web.menu;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MenuMapper {
	
	//전체 메뉴 조회
	public List<Map<String, Object>> getMenuList(Map<String,Object> param);

	//메뉴 등록
	public int insertMenu(Map<String,Object> param);
	
	//메뉴 수정
	public int updateMenu(Map<String,Object> param);
	
	//상위 메뉴 정보 조회
	public Map<String, Object> getParentMenuInfo(Map<String, Object> param);
	
	//메뉴 보유 여부 체크
	public int hasMenu(Map<String, Object> param);

}
