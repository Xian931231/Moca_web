package com.mocafelab.web.ad.sg;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.vo.RequestMap;

/**
 * 광고 전략 (운영자)
 * @author mure96
 *
 */
@RestController
@RequestMapping("${apiPrefix}/sg")
public class SgController {

	@Autowired
	private SgService sgService;
	
	// 관리자
	/**
	 * 광고 리스트 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/list")
	public Map<String, Object> getList(RequestMap reqMap)throws Exception{
		Map<String, Object> param = reqMap.getMap();
		
		return sgService.getList(param);
	}
	
	/**
	 * 광고 상세 정보 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/detail")
	public Map<String, Object> getDetail(RequestMap reqMap)throws Exception{
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "sg_id");
		
		return sgService.getDetail(param);
	}
	
	/**
	 * 광고 승인 정보 업데이트
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/approvalStatus/modify")
	public Map<String, Object> modifyApprovalStatus(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "sg_id");
		CommonUtil.checkNullThrowException(param, "approval");
		
		String approval = (String) param.get("approval");
		if(approval.equals("N")) {
			CommonUtil.checkNullThrowException(param, "reject_reason");
		}
		return sgService.modifyApprovalStatus(param);
	}
	
	/**
	 * 승인거부 사유 수정
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/rejectReason/modify")
	public Map<String, Object> modifyRejectReason(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "sg_id");
		
		return sgService.modifyRejectReason(param);
	}
	
	/**
	 * 광고 선택 매체 비고 존재하는지 검사
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/hasNote")
	public Map<String, Object> sgHasNote(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "sg_id");
		
		return sgService.sgHasNote(param);
	}
	
	//캠페인 내 광고 삭제
	@PostMapping("/remove")
	public Map<String, Object> removeSg(RequestMap reqMap)throws Exception{
		Map<String, Object> param = reqMap.getMap();
		
		return sgService.removeSg(param);
	}
	
	/**
	 * 광고 긴급 종료
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/stop")
	public Map<String, Object> stopSg(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "sg_id");
		
		return sgService.stopSg(param);
	}
	
	/**
	 * 종료 광고 조회
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/end/list")
	public Map<String, Object> getEndSgList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return sgService.getEndSgList(param);
	}
	/**
	 * 
	 * 입금 상태 변경
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/payStatus/modify")
	public Map<String, Object> sgPayStatusModify(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		CommonUtil.checkNullThrowException(param, "sg_id");
		
		return sgService.sgPayStatusModify(param);
	}
	
	/**
	 * 캠페인/광고 관리 > 광고 목록 조회 (광고주, 캠페인 정보 포함)
	 * @param reqMap
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/withDemand/list")
	public Map<String, Object> sgWithDemandList(RequestMap reqMap) throws Exception {
		Map<String, Object> param = reqMap.getMap();
		
		return sgService.getSgWithDemandList(param);
	}
	
}