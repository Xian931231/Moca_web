package com.mocafelab.web.ad.sg;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mocafelab.web.ad.campaign.CampaignMapper;
import com.mocafelab.web.alarm.AlarmData;
import com.mocafelab.web.alarm.AlarmSender;
import com.mocafelab.web.email.template.PayCompleteTemplate;
import com.mocafelab.web.email.template.RefundCompleteTemplate;
import com.mocafelab.web.email.template.SgApproveTemplate;
import com.mocafelab.web.email.template.SgStopTemplate;
import com.mocafelab.web.member.MemberMapper;
import com.mocafelab.web.vo.BeanFactory;
import com.mocafelab.web.vo.Code;
import com.mocafelab.web.vo.ResponseMap;

import net.newfrom.lib.util.CommonUtil;
import net.newfrom.lib.util.SHAUtil;

@Service
public class SgService {

	@Autowired
	private BeanFactory beanFactory;
	
	@Autowired
	private SgMapper sgMapper;
	
	@Autowired
	private MemberMapper memberMapper;
	
	@Autowired
	private CampaignMapper campaignMapper;
	
	@Autowired
	private AlarmSender alarmSender;
	
	// 관리자
	/**
	 * 광고 리스트 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getList(Map<String, Object> param){
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> getList = sgMapper.getList(param);
		
		int listCnt = sgMapper.getListCnt(param);
		
		respMap.setBody("list", getList);
		respMap.setBody("tot_cnt", listCnt);
		
		return respMap.getResponse();
	}

	/**
	 * 광고 상세 정보 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getDetail(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		Map<String, Object> sgDetail = sgMapper.getDetail(param);
		List<Map<String, Object>> sgMaterialDetail = sgMapper.getMaterialDetail(param);
		
		Map<String, Object> data = new HashMap<>();
		
		data.put("detail", sgDetail);
		data.put("material", sgMaterialDetail);
		
		respMap.setBody("data", data);
		
		return respMap.getResponse();
	}
	
	/**
	 * 광고 승인 정보 업데이트
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> modifyApprovalStatus(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 대기중인 광고인지 검사
//		if(sgMapper.isWaitSg(param) < 1) {
//			throw new RuntimeException();
//		}
		
		String approvalStatus = (String) param.get("approval");
		
		// 광고 승인시
		if(approvalStatus.equals("Y")) {
			// 입금 완료 상태인지 검사
			if(sgMapper.isPayComplete(param) < 1) {
				throw new RuntimeException();
			}
		}
		
		if(sgMapper.modifyApprovalStatus(param) < 1) {	
			throw new RuntimeException();
		}
		
		Map<String, Object> sgDetail = sgMapper.getDetail(param);
		
		// 이메일 발송
		SgApproveTemplate template = new SgApproveTemplate(approvalStatus);
		template.setName((String) sgDetail.get("uname"));
		template.setSgName((String) sgDetail.get("name"));
		template.setDate((String) sgDetail.get("approve_date"));
		
		// 발송 정보 설정 후 전송
		AlarmData alarmData = new AlarmData((String) sgDetail.get("email"), template);
		alarmSender.sendEmail(alarmData);
		
		return respMap.getResponse();
	}
	
	/**
	 * 승인거부 사유 수정
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> modifyRejectReason(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		if(sgMapper.modifyRejectReason(param) < 1) {
			throw new RuntimeException();
		}
		
		return respMap.getResponse();
	}
	
	/**
	 * 광고 선택 매체 비고 존재하는지 검사
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> sgHasNote(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		
		
		return respMap.getResponse();
	}
	
	/**
	 * 운영자용 캠페인애 광고 삭제
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> removeSg(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Integer> sgIdList = (List<Integer>) param.get("sg_id_list");
		
		List<Integer> statusIngList = new ArrayList<>();
		
		for(Integer sgId : sgIdList) {
			param.put("sg_id", sgId);
			
			Map<String, Object> sgIdDetail = sgMapper.getDetail(param);
			if(sgIdDetail != null && !sgIdDetail.isEmpty()) {
				int sg_status = (int) sgIdDetail.get("status");
				
				//진행중인 광고는 삭제 불가
				if(sg_status != 1 && sg_status != 9 && !sgIdDetail.isEmpty()) {
					// 광고 지역
					sgMapper.removeSgArea(param);
					
					// 광고 날짜
					sgMapper.removeSgWeek(param);
					
					// 소재 삭제
					// 광고 삭제
					if(sgMapper.removeSgMaterial(param) <= 0 || sgMapper.removeSgManager(param) <= 0) {
						throw new Exception();
					}
				}else {
					statusIngList.add(sgId);
				}

			}else {
				statusIngList.add(sgId);
			}
			
		}
		
		respMap.setBody("delete_fail_list", statusIngList);
		
		return respMap.getResponse();
	}
	
	/**
	 * 광고 긴급 종료
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> stopSg(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 관리자 확인
		if(!param.get("login_utype").equals("A")) {
			throw new RuntimeException();
		}

		// 1. 존재하고 진행중인 광고인지 확인
		if(sgMapper.isProceed(param) < 1) {
			return respMap.getResponse(Code.SG_ALREADY_STOP);
		}

		// 2. 비밀번호 맞는지 확인
		String passwd = String.valueOf(param.get("passwd")); 
		param.put("passwd", SHAUtil.encrypt(passwd, "SHA-256"));
		
		if(memberMapper.getSavePassword(param) == null) {
			return respMap.getResponse(Code.MEMBER_PW_IS_NOT_MATCH);
		}
		
		// 3. 사유 null 체크
		if(CommonUtil.checkIsNull(param, "stop_reason")) {
			return respMap.getResponse(Code.SG_REASON_IS_NULL);
		}
		
		// 긴급 종료
		if(sgMapper.stopSg(param) < 1) {
			throw new RuntimeException();
		}
		
		Map<String, Object> sgDetail = sgMapper.getDetail(param);
		
		// 이메일 발송
		SgStopTemplate template = new SgStopTemplate();
		template.setName((String) sgDetail.get("uname"));
		template.setSgName((String) sgDetail.get("name"));
		template.setDate((String) sgDetail.get("stop_date"));
		template.setReason((String) sgDetail.get("stop_reason"));
		
		// 발송 정보 설정 후 전송
		AlarmData alarmData = new AlarmData((String) sgDetail.get("email"), template);
		alarmSender.sendEmail(alarmData);
		
		return respMap.getResponse();
	}
	
	/**
	 * 종료광고 리스트 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getEndSgList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> list = sgMapper.getEndSgList(param);
		int totCnt = sgMapper.getEndSgListCnt(param);
		
		respMap.setBody("list", list);
		respMap.setBody("tot_cnt", totCnt);
		
		return respMap.getResponse();
	}
	
 	/**
	 * 입금 상태 변경
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> sgPayStatusModify(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		// 해당 광고의 입금상태가 어떤지
		Map<String, Object> sgInfo = sgMapper.getDetail(param);
		if(sgInfo.isEmpty()) {
			throw new RuntimeException();
		}
		
		String nowPayStatus = (String) sgInfo.get("pay_status_code");
		String payStatus = (String) param.get("pay_status_code");
		
//		// 광고가 이미 승인 거부 상태이거나 입금 완료 상태로 변경할 예정이면 거절사유 삭제
//		if(sgInfo.get("status").equals(9) || payStatus.equals("PAY_COMPLETE")) {
//			param.remove("reject_reason");
//		} 
		
		String payDate = "";
		
		switch(payStatus) {
		case "PAY_COMPLETE":
			// 입금 대기, 환불대기 -> 입금 완료
			if(nowPayStatus.equals("PAY_WAIT") || nowPayStatus.equals("REFUND_WAIT")) { 
				param.put("price", sgInfo.get("price"));
				param.put("kind", "D");
				
				Map<String, Object> sgPayLog = sgMapper.addSgPayLog(param); 
				if(CommonUtil.checkIsNull(sgPayLog)) { // 입금 로그
					throw new RuntimeException();
				}
				payDate = (String) sgPayLog.get("pay_date");
				
				if(nowPayStatus.equals("REFUND_WAIT")) {
					param.put("status", 0);
				}
				
				param.put("pay_price", 0);
				param.put("total_pay_price", sgInfo.get("price"));
			} else {
				throw new RuntimeException();
			}
			break;
		case "REFUND_WAIT":
			// 입금대기, 입금완료, 환불완료 -> 환불대기
			param.put("pay_price", Integer.parseInt("-" + sgInfo.get("price")));
			param.put("status", 9);
			break;
		case "REFUND_COMPLETE":
			// 입금대기, 입금완료, 환불대기 -> 환불 완료
			if(!nowPayStatus.equals("REFUND_COMPLETE")) {
				param.put("price", sgInfo.get("price"));
				param.put("kind", "R");
				
				Map<String, Object> payLogId = sgMapper.getDepositPayLogId(param);
				if(!CommonUtil.checkIsNull(payLogId)) {
					param.putAll(payLogId);
				}
				
				Map<String, Object> sgPayLog = sgMapper.addSgPayLog(param); 
				if(CommonUtil.checkIsNull(sgPayLog)) { // 환불 로그
					throw new RuntimeException();
				}
				payDate = (String) sgPayLog.get("pay_date");

				param.put("status", 9);
				param.put("pay_price", 0);
			} else {
				throw new RuntimeException();
			}
			break;
		default:
			throw new RuntimeException();
		}
		
		// 수정
		Map<String, Object> modifyResult = sgMapper.modifyPayPrice(param);
		if(CommonUtil.checkIsNull(modifyResult)) {
			throw new RuntimeException();
		}
		
		NumberFormat nf = NumberFormat.getInstance();
		String price = (String) nf.format(sgInfo.get("price"));
		
		// 입금, 환불 이메일
		if(payStatus.equals("PAY_COMPLETE")) { // 입금 완료 이메일
			PayCompleteTemplate template = new PayCompleteTemplate();
			template.setName((String) sgInfo.get("uname"));
			template.setSgName((String) sgInfo.get("name"));
			template.setDate(payDate);
			template.setPay(price);
			
			AlarmData alarmData = new AlarmData((String) sgInfo.get("email"), template);
			alarmSender.sendEmail(alarmData);
		} else if(payStatus.equals("REFUND_COMPLETE")) { // 환불 완료 이메일
			RefundCompleteTemplate template = new RefundCompleteTemplate();
			template.setName((String) sgInfo.get("uname"));
			template.setSgName((String) sgInfo.get("name"));
			template.setDate(payDate);
			template.setPay(price);
			
			if(!CommonUtil.checkIsNull(sgInfo, "bank_name") && !CommonUtil.checkIsNull(sgInfo, "bank_account_number") && !CommonUtil.checkIsNull(sgInfo, "bank_account_holder")) {
				template.setBankInfo((String) sgInfo.get("bank_name") + " " + (String) sgInfo.get("bank_account_number") + " (예금주:" + (String) sgInfo.get("bank_account_holder") + ")");
			} else {
				template.setBankInfo("-");
			}
			
			AlarmData alarmData = new AlarmData((String) sgInfo.get("email"), template);
			alarmSender.sendEmail(alarmData);
		}
		
		// 승인거부 이메일
		if(!CommonUtil.checkIsNull(param, "status") && (int) param.get("status") == 9 && (int) sgInfo.get("status") != 9) {
			if(payDate.equals("") || payDate == null) {
				payDate = (String) modifyResult.get("approve_date");
			}
			
			SgApproveTemplate template = new SgApproveTemplate("N");
			template.setName((String) sgInfo.get("uname"));
			template.setSgName((String) sgInfo.get("name"));
			template.setDate(payDate);
			
			AlarmData alarmData = new AlarmData((String) sgInfo.get("email"), template);
			alarmSender.sendEmail(alarmData);
		}
		
		return respMap.getResponse();
	}
	
	/**
	 * 캠페인/광고 관리 > 목록 조회 (광고주 [ 캠페인 [광고]]])
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getSgWithDemandList(Map<String, Object> param) throws Exception {
		ResponseMap respMap = beanFactory.getResponseMap();
		
		List<Map<String, Object>> demandList = memberMapper.getDemandAgencyList(param);
		List<Map<String, Object>> sgList = sgMapper.getSgListByDemand(param);
	
		if(sgList.size() > 0) {
			List<Long> sgInDemand = new ArrayList<>();
			
			for(Map<String, Object> sg : sgList) {
				sgInDemand.add((long)sg.get("campaign_id"));
			}
			
			sgInDemand = sgInDemand.stream().distinct().collect(Collectors.toList());
			param.put("sg_list", sgInDemand);
		}
		
		List<Map<String, Object>> campaignList = campaignMapper.getCampaignListByDemand(param);
		
		for(Map<String, Object> demand : demandList) { // 광고주
			String demandId = String.valueOf(demand.get("member_id"));
			List<Map<String, Object>> campaignListWithDemand = new ArrayList<>();
		
			for(Map<String, Object> campaign : campaignList) { // 캠페인
				if(sgList.size() > 0) {
					String memberId = String.valueOf(campaign.get("member_id"));
					if(demandId.equals(memberId)) {
						campaignListWithDemand.add(campaign);
					}
					
					String cpId = String.valueOf(campaign.get("campaign_id"));
					List<Map<String, Object>> sgListWithCampaign = new ArrayList<>();
					
					for(Map<String, Object> sg : sgList) { // 광고
						String campaignId = String.valueOf(sg.get("campaign_id"));
						if(cpId.equals(campaignId)) {
							sgListWithCampaign.add(sg);
						}
					}
					campaign.put("sg_list", sgListWithCampaign);
				}
			}
			demand.put("campaign_list", campaignListWithDemand);
		}
		
		int totCnt = memberMapper.getDemandAgencyListCount(param);
		respMap.setBody("list", demandList);
		respMap.setBody("tot_cnt", totCnt);
		
		return respMap.getResponse();
	}
	// -- 관리자 
}