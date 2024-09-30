package com.mocafelab.web.role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mocafelab.web.enums.MemberType;
import com.mocafelab.web.member.AgencyMemberMapper;
import com.mocafelab.web.member.AgencyMemberService;
import com.mocafelab.web.member.MemberMapper;
import com.mocafelab.web.menu.MenuMapper;
import com.mocafelab.web.menu.MenuService;
import com.mocafelab.web.vo.BeanFactory;
import com.mocafelab.web.vo.Code;
import com.mocafelab.web.vo.ResponseMap;

@Transactional(rollbackFor = {Exception.class})
@Service
public class AgencyRoleService {
	
	@Autowired
	private BeanFactory beanFactory;
	
	@Autowired
	private AgencyRoleMapper agencyRoleMapper;
	
	@Autowired
	private AgencyMemberMapper agencyMemberMapper;
	
	@Autowired
	private MenuMapper menuMapper;
	
	@Autowired
	private RoleMapper roleMapper;
	
	@Autowired
	private MenuService menuService;
	
	@Autowired
	private MemberMapper memberMapper;
	
	@Autowired
	private AgencyMemberService agencyMemberService;
	
	/**
	 * 권한구분 리스트 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getRoleList(Map<String, Object> param) throws Exception{
		ResponseMap respMap = beanFactory.getResponseMap();
		
		//대행사 최고 관리자 권한 정보 조회 
		Map<String, Object> getAgencySuperRole = agencyRoleMapper.getAgencySuperRole(param);
		param.put("super_id", getAgencySuperRole.get("role_id"));
		
		List<Map<String, Object>> list = agencyRoleMapper.getRoleList(param);
		respMap.setBody("list", list);
		
		return respMap.getResponse();
	}
	
	/**
	 * 권한구분의 권한 메뉴 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getRoleMenuList(Map<String, Object> param) throws Exception{
		ResponseMap respMap = beanFactory.getResponseMap();
		
		//권한구분의 메뉴 조회
		param.put("utype", MemberType.AGENCY.getType());
		List<Map<String, Object>> menuList = roleMapper.getRoleMenuList(param);
		List<Map<String, Object>> menuTreeList = menuService.getTreeMenuList(menuList);
		
		respMap.setBody("list", menuTreeList);
		
		return respMap.getResponse();
	}
	
	/**
	 * 직원의 담당 광고주 리스트, 권한구분 리스트 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getStaffRoleMenuList(Map<String, Object> param) throws Exception{
		ResponseMap respMap = beanFactory.getResponseMap();
		
		//메뉴 리스트 
		List<Map<String, Object>> menuList = new ArrayList<>();
		//담당 광고주 리스트
		List<Map<String, Object>> demandList = new ArrayList<>();
		
		//조회 하려는 member_id(직원)의 데이터 조회
		Map<String, Object> memberData = agencyRoleMapper.getStaffData(param);
		
		//없으면 직원이 아니거나 대행사 마스터 계정
		if(memberData != null && !memberData.isEmpty()) {
			param.put("role_id", memberData.get("role_id"));
			param.put("utype", MemberType.AGENCY.getType());
			menuList = roleMapper.getRoleMenuList(param);
			menuList = menuService.getTreeMenuList(menuList);
			
			demandList = agencyMemberMapper.getPersonalDemandList(param);
		}
		
		respMap.setBody("menu_list", menuList);
		respMap.setBody("demand_list", demandList);
		
		return respMap.getResponse();
	}
	
	/**
	 * 권한구분 등록
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> addRole(Map<String, Object> param) throws Exception{
		ResponseMap respMap = beanFactory.getResponseMap();
		
		//권한구분명 중복체크 
		if (agencyRoleMapper.hasDuplicateName(param) > 0) {
			return respMap.getResponse(Code.AGENCY_ROLE_DUPLICATE_NAME);
		}
		
		//role_menu 등록
		int addRoleId = agencyRoleMapper.addRole(param);
		
		param.put("id", addRoleId);
		
		if(addRoleId <= 0) {
			throw new RuntimeException();
		}
		
		Map<String, Object> roleInfo = agencyRoleMapper.getRoleDetail(param);
		
		//menu에 기본적인 default 값 가져오기
		List<Map<String, Object>> getUtypeList = agencyRoleMapper.getUtypeList(param);
		if(getUtypeList != null && !getUtypeList.isEmpty()) {
			
			Map<String, Object> utypeParam = new HashMap<String, Object>();
			utypeParam.put("role_id", addRoleId);
			
			//default_url 등록
			for (Map<String, Object> utypeData : getUtypeList) {
				String defaultUrl = String.valueOf(utypeData.get("url"));
				String accessYn = String.valueOf(utypeData.get("default_yn"));
				long menuId = (long) utypeData.get("id");
				
				if (!defaultUrl.equals("/") && defaultUrl != "" && !accessYn.equals("N") && (int)utypeData.get("step") > 1) {
					param.put("role_id", addRoleId);
					param.put("default_menu_id", menuId);
					roleMapper.modifyStaffDefaultMenuId(param);
					break;
				}
			}
			
			
			//role_menu에 등록
			for(Map<String, Object> utypeData:getUtypeList) {
				utypeParam.put("menu_id", utypeData.get("id"));
				utypeParam.put("access_yn", utypeData.get("default_yn"));
				
				if(agencyRoleMapper.addRoleMenu(utypeParam) <= 0) {
					throw new RuntimeException();
				}
			}
		}else {
			throw new RuntimeException();
		}
		
		respMap.setBody("data", roleInfo);
		
		return respMap.getResponse();
	}
	
	/**
	 * 권한구분 명 수정
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> modifyRoleName(Map<String, Object> param) throws Exception{
		ResponseMap respMap = beanFactory.getResponseMap();
		
		//권한구분명 중복체크 
		if (agencyRoleMapper.hasDuplicateName(param) > 0) {
			return respMap.getResponse(Code.AGENCY_ROLE_DUPLICATE_NAME);
		}
		
		if(agencyRoleMapper.modifyRoleName(param) <= 0) {
			throw new RuntimeException();
		}
		
		return respMap.getResponse();
	}
	
	/**
	 * 권한구분 순서 수정
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> modifyRoleSort(Map<String, Object> param) throws Exception{
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<String> roleIdList = (List<String>) param.get("role_id_list");
		int idx = 5;
		
		for(String roleId : roleIdList) {
			param.put("role_id", roleId);
			param.put("sort", idx);
			if(agencyRoleMapper.modifyRoleSort(param) <= 0) {
				throw new RuntimeException();
			}
			idx++;
		}
		
		return respMap.getResponse();
	}
	
	/**
	 * 권한구분 수정
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> modifyRole(Map<String, Object> param) throws Exception{
		ResponseMap respMap = beanFactory.getResponseMap();
		param.put("role_modify_yn", "Y");
		//modify_yn 체크
		if(agencyRoleMapper.hasRoleManager(param) <= 0) {
			throw new RuntimeException();
		}
		
		if(agencyRoleMapper.modifyRoleremoveMenu(param) <= 0) {
			throw new RuntimeException();
		}
		
		List<Map<String, Object>> roleList = (List<Map<String,Object>>) param.get("role_json");
		// default_menu_id 수정
		for (Map<String, Object> roleInfo : roleList) {
			String defaultUrl = String.valueOf(roleInfo.get("url"));
			String accessYn = String.valueOf(roleInfo.get("access_yn"));
			int menuId = (int) roleInfo.get("menu_id");
			param.put("menu_id", menuId);
			
			if (!defaultUrl.equals("/") && defaultUrl != "" && !accessYn.equals("N")) {
				param.put("default_menu_id", menuId);
				roleMapper.modifyStaffDefaultMenuId(param);
				break;
			}
		}
		
		for(Map<String, Object> roleInfo : roleList) {
			param.put("menu_id", roleInfo.get("menu_id"));
			param.put("access_yn", roleInfo.get("access_yn"));
			
			//메뉴가 있는지 체크
			if(menuMapper.hasMenu(roleInfo) <= 0) {
				throw new RuntimeException();
			}
			
			//등록
			if(agencyRoleMapper.addRoleMenu(param) <= 0) {
				throw new RuntimeException();
			}
		}
		
		return respMap.getResponse();
	}

	/**
	 * 직원별 수정
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> modifyRolePermission(Map<String, Object> param) throws Exception{
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 변경사항 히스토리 등록 
		addModificationHistory(param);
		
		//member_assess_dsp에 있는 데이터 삭제
		agencyRoleMapper.removeStaffAccessDsp(param);
		
		//role_id 유효성 체크
		if(agencyRoleMapper.hasRoleManager(param) <= 0) {
			throw new RuntimeException();
		}
		//직원의 role_id 수정
		if(agencyRoleMapper.modifyStaffRoleId(param) <= 0) {
			throw new RuntimeException();
		}
		
		//member_assess_dsp 등록
		List<Map<String, Object>> dspList = (List<Map<String, Object>>) param.get("demand_id_list");
		
		for(Map<String, Object> dspInfo : dspList) {
			param.put("demand_id", dspInfo.get("demand_id"));
			
			//회원이 있는지, 광고주인지 체크
			if(agencyRoleMapper.hasDsp(param) <= 0) {
				throw new RuntimeException();
			}
			
			//등록
			if(agencyRoleMapper.addStaffAccessDsp(param) <= 0) {
				throw new RuntimeException();
			}
		}
	
		return respMap.getResponse();
	}
	
	/**
	 * 변경사항 로그 등록
	 * @param param
	 * @throws Exception
	 */
	private void addModificationHistory(Map<String, Object> param) throws Exception {
		// 구분 변경 로그
		Map<String, Object> roleChangeHistory = createRoleChangeHistory(param);
		
		// 담당 광고주 변경 로그
		List<Map<String, Object>> demandChangeHistoryList = createDemandChangeHistory(param);
		
		if(!roleChangeHistory.isEmpty()) {
			demandChangeHistoryList.add(roleChangeHistory);
		}
		
		if(!demandChangeHistoryList.isEmpty()) {
			// 로그 메시지 생성
			String historyMsg = agencyMemberService.createHistoryMsg(demandChangeHistoryList);
			param.put("message", historyMsg);
			 
			// 히스토리 등록
			if(agencyMemberMapper.addMemberModifyHistory(param) < 1) {
				throw new RuntimeException();
			}
		}
	}
	
	/**
	 * 구분 수정 로그 생성
	 * @param param
	 * @throws Exception
	 */
	private Map<String, Object> createRoleChangeHistory(Map<String, Object> param) throws Exception {
		Map<String, Object> historyMap = new HashMap<>();
		
		// 기존 직원 정보 조회
		Map<String, Object> agencyMember = agencyRoleMapper.getStaffData(param);
		
		long originRoleId = (long)agencyMember.get("role_id");
		long modifyRoleId = (int)param.get("role_id");
		
		// 변경여부 
		if(originRoleId == modifyRoleId) {
			return historyMap;
		}
		String originRole = "미지정";
		String modifyRole = "미지정";
		
		// 권한 정보 
		List<Map<String ,Object>> roleList = agencyRoleMapper.getRoleList(null);
		
		// 변경된 권한 이름 맵핑
		for(Map<String, Object> role : roleList) {
			long roleId = (long)role.get("id");
			String roleName = (String)role.get("name");
			
			if(originRoleId == roleId) {
				originRole = roleName;
			} else if(modifyRoleId == roleId) {
				modifyRole = roleName;
			}
		}
		
		String msg = "[" + originRole + "]" + " ▶ " + "[" + modifyRole + "]";
		historyMap.put("type", "C");
		historyMap.put("kind", "MHK004");
		historyMap.put("message", msg);
		
		return historyMap;
	}
	
	/**
	 * 담당 광고주 수정 로그 생성
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> createDemandChangeHistory(Map<String, Object> param) throws Exception {
		// 기존 담당 광고주
		List<Map<String, Object>> originDemandList = agencyRoleMapper.getAgencyDemendList(param);
		
		// 변경되는 담당 광고주
		List<Map<String, Object>> modifyDemandList = (List<Map<String, Object>>) param.get("demand_id_list");
		
		// 변경 담당 광고주 정보 맵핑
		if(!modifyDemandList.isEmpty()) {
			param.put("demand_id_list", modifyDemandList);
			modifyDemandList = agencyRoleMapper.getDemendList(param);
		}
		
		List<Map<String, Object>> historyList = agencyMemberService.<Long, Long>partitionByRemoveAdd(originDemandList, modifyDemandList, "dsp_id", (v1, v2) -> {
			return v1.equals(v2);
		});
		
		historyList.forEach(history -> {
			history.put("kind", "MHK008");
			history.put("message", history.get("company_name") + "(" + history.get("uid") + ")");
		});

		return historyList;
	}
	
	/**
	 * 권한구분 삭제
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> removeRole(Map<String, Object> param) throws Exception{
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> roleIdList = (List<Map<String, Object>>) param.get("role_id_list");
		
		for(Map<String, Object> roleId : roleIdList) {
			param.put("role_id", roleId.get("role_id"));
			
			String	oldRole = (String)roleId.get("role_name");
			
			//해당 role_id를 가지고 있는 회원의 role_id 삭제
			int unratedRoleId = agencyRoleMapper.hasUnratedRoleId(param);
			param.put("unrated_role_id", unratedRoleId);
			
			
			// 해당 권한구분을 가지고 있던 계정의 role_id를 미지정으로 변경
			//삭제할 role_id 를 가진 member들 조회
			List<Map<String, Object>> memberRoleList = roleMapper.hasMemberRole(param); 
			if ( memberRoleList.size() > 0) {
				for(Map<String, Object> memberRoleItem : memberRoleList) {
					param.put("member_id", memberRoleItem.get("member_id"));
					
					param.put("message", "구분 [" + oldRole + "] ▶ [ 미지정 ] 변경");
					
					if(memberMapper.addStaffHistory(param) < 1) {
						throw new RuntimeException();
					}
				}
				
				//구분 미지정으로 변경
				if(agencyRoleMapper.removeMemberRoleId(param) <= 0) {
					throw new RuntimeException();
				};
			}
			
			if(agencyRoleMapper.removeRoleMenu(param) <= 0 || agencyRoleMapper.removeRoleManager(param) <= 0) {
				throw new RuntimeException();
			}
		}
		
		return respMap.getResponse();
	}
	
}
