const demandCostList = (function() {
	function init() {
		_list.setDefaultDate();
		
		_list.getList();
	}
	
	function _evInit() {
		let evo = $("[data-src='costList'][data-act]").off();
		
		$('#sg-status').selectpicker('refresh');
		
		evo.on("click change", function(e) {
			_action(e);
		});
	}
	
	function _action(e) {
		let evo = $(e.currentTarget);
		
		let act_v = evo.attr("data-act");
		
		let type_v = e.type;
		
		let event = _event;
		
		if (type_v == "click") {
			if (act_v == "clickSearch") {
				event.listSearch();
			} else if (act_v == "confirmApproval") {
				event.confirmApproval(evo);
			} else if (act_v == "moveCampaignDetail") {
				event.moveCampaignDetail(evo);
			} else if (act_v == "moveSgDetail") {
				event.moveSgDetail(evo);
			}
		} else if (type_v == "change") {

		}  
	}
	
	let _event = {
		listSearch: function() {
			let sDate = $("#s-date").val();
			let eDate = $("#e-date").val();
			let diff = util.getDiffDate(sDate, eDate, "months");
			
			if (moment(sDate).isAfter(eDate)) {
				customModal.alert({
					content: "시작일이 종료일 이후일 수 없습니다.",
				});
				
				return;
			}
			
			if (diff >= 3) {
				customModal.alert({
					content: "최대 조회 기간은 3개월입니다.",
				});
				
				return;
			}
			
			_list.getList();
		},
		
		initModal: function() {
			$("#refusal-modal").on("hidden.bs.modal", function(e) {
				$("#reject-reason").html("");
			});
		},
		
		moveCampaignDetail: function(evo) {
			let parent = $(evo).parent();
			let campaignId = parent.attr("data-id");
			
			location.href = "/demand/campaign/sg/list?id=" + campaignId;
		},
		
		moveSgDetail: function(evo) {
			let parent = $(evo).parent();
			let sgId = parent.attr("data-id");
			
			location.href = "/demand/campaign/sg/detail?id=" + sgId;
		},
		
		confirmApproval: function(evo) {
			let modalContent = $(evo).attr("data-content");
			if (!modalContent) {
				mocalContent = "";
			}
			
			$("#reject-reason").html(modalContent);
		}
	}
	
	let _list = {
		setDefaultDate: function() {
			customDatePicker.init("s-date").datepicker("setDate",moment().subtract(7, "days").toDate());
			customDatePicker.init("e-date").datepicker("setDate", moment().toDate());
		},
			
		getSearchData: function() {
			let data = {};
			
			
			let payStatus = $("#pay-status").val();
			
			if (payStatus) {
				data.pay_status_code = payStatus;
			}
			
			// 광고 상태
			let sgStatus = $("#sg-status").val();
			
			if (sgStatus) {
				data.status = sgStatus;
			}
			
			// 광고신청일자 시작 범위
			let sDate = $("#s-date").val();
			if (sDate) {
				data.str_dt = sDate;
			}
			
			// 광고신청일자 종료 범위
			let eDate = $("#e-date").val();
			if (eDate) {
				data.end_dt = eDate;
			}
			
			return data;
		},
		
		getList: function(currPage = 1) {
			let url_v = "/sg/demand/member/amount/list";
			
			let data_v = _list.getSearchData();
			
			let page_o = $("#list-page").customPaging(null, function(_currPage) {
				_list.getList(_currPage);
			});
			
			let pageParam = page_o.getParam(currPage);
			
			if(pageParam) {
				data_v.offset = pageParam.offset;
				data_v.limit = pageParam.limit;
			}	
			
			comm.send(url_v, data_v, "POST", function(response) {
				
				_list.drawList(response.list);
				page_o.drawPage(response.total_count);
				
				_evInit();
			});
		},
		
		drawList: function(list) {
			//dev.log(list);
			
			let body_o = $("#list-body").empty();
			
			let payWaitCount = 0; // 입금 대기
			let payCompleteCount = 0; // 입금 완료
			let refundWaitCount = 0; // 환불 대기
			let refundCompleteCount = 0; // 환불 완료
			let approveWaitCount = 0; // 승인대기
			let approveDeniedCount = 0; // 승인 거부
			let approveCompleteCount = 0; // 승인 완료
			
			if (list.length > 0) {
				for (item of list) {
					let tr_o = $("<tr>");
					body_o.append(tr_o);
					
					// 과금방식
					{
						let td_o = $("<td>").html(item.pay_type);
						
						tr_o.append(td_o);
					}
					
					// 캠페인명
					{
						let td_o = $("<td>").attr({
							"data-id": item.campaign_id
						});
						
						let a_o = $("<a>").attr({
							"href": "javascript:;",
							"data-src": "costList",
							"data-act": "moveCampaignDetail"		
						}).html(item.campaign_name);
						
						td_o.append(a_o);
						tr_o.append(td_o);
					}
					
					// 광고명
					{
						let td_o = $("<td>").attr({
							"data-id": item.sg_id,
						});
						
						let a_o = $("<a>").attr({
							"href": "javascript:;",
							"data-src": "costList",
							"data-act": "moveSgDetail"		
						}).html(item.sg_name);
						
						td_o.append(a_o);
						tr_o.append(td_o);
					}
					
					// 광고신청 일자
					{
						let td_o = $("<td>").html(item.insert_date);
						
						tr_o.append(td_o);
					}
					
					// 광고시작 요청일
					{
						let td_o = $("<td>").html(item.start_ymd);
						
						tr_o.append(td_o);
					}
					
					// 광고종료 요청일
					{
						let td_o = $("<td>");
						if (item.pay_type == "CPM") {
							td_o.html("-");
						} else {
							td_o.html(item.end_ymd);
						}
						
						tr_o.append(td_o);
					}
					
					// 집행요청금액(VAT포함)
					{
						let td_o = $("<td>").html("₩" + util.numberWithComma(item.price));
						
						tr_o.append(td_o);
					}
					
					// 입금상태
					{
						let td_o = $("<td>");
						
						let payDate = "";
						switch(item.pay_status_code) {
							case "PAY_COMPLETE":
								payDate = item.pay_status_date;
								payCompleteCount++;
								break;
							case "PAY_WAIT":
								payWaitCount++;
								td_o.css("color", "red");
								break;
							case "REFUND_COMPLETE":
								payDate = item.pay_status_date;
								refundCompleteCount++;
								break;
							case "REFUND_WAIT":
								refundWaitCount++;
								td_o.css("color", "green");
								break;
							default:
								break;
						}
						
						if (!payDate) {
							td_o.html(item.pay_code_name);	
						} else {
							td_o.html(payDate + "<br/>" + item.pay_code_name);
						}
						
						tr_o.append(td_o);
					}
					
					// 승인현황
					{
						let child_o = $("<button>").attr({
							"class": "btn btn-sm",
							"type": "button",
							"data-src": "costList",
							"data-act": "confirmApproval"
						});
						
						switch(item.status) {
							case 0:
								approveWaitCount++;
								child_o = $("<p>").attr({
									"class": "stateBox state-delay"
								}).html("승인 대기");
								break;
							case 1: case 2: case 3: case 4: case 7: case 8:
								approveCompleteCount++;
								child_o = $("<button>").attr({
									"class": "btn btn-sm btn-app",
									"type": "button"
								}).html("승인 완료");
								break;
							case 9:
								approveDeniedCount++;
								child_o = $("<button>").attr({
									"class": "btn btn-refusal",
									"type": "button",
									"data-toggle": "modal",
									"data-target": "#refusal-modal",
									"data-src": "costList",
									"data-act": "confirmApproval",
									"data-content": item.reject_reason
								}).html("승인 거부");
								break;
							default:
								break;
						}
						
						let td_o = $("<td>").append(child_o);
						
						tr_o.append(td_o);
					}
					// 광고 상태 카운트
					{
						let p_o = $("#print-status").empty();
						
						let pay_complete_span_o = $("<span>").html("입금 완료 : " + payCompleteCount + "건");
						let pay_wait_span_o = $("<span>").html("입금 대기 : " + payWaitCount + "건");
						let refund_complete_span_o = $("<span>").html("환불 완료 : " + refundCompleteCount + "건");
						let refund_wait_span_o = $("<span>").html("환불 대기 : " + refundWaitCount + "건");
						let approve_complete_span_o = $("<span>").html("승인 완료 : " + approveCompleteCount + "건");
						let approve_denied_span_o = $("<span>").html("승인 거부 : " + approveDeniedCount + "건");
						let approve_wait_span_o = $("<span>").html("승인 대기 : " + approveWaitCount + "건");
						
						p_o.append(
							pay_complete_span_o
							, pay_wait_span_o
							, refund_complete_span_o
							, refund_wait_span_o
							, approve_complete_span_o
							, approve_denied_span_o
							, approve_wait_span_o
						);
					}
				}
				$("#list-page").show();
			} else {
				$("#list-page").hide();
			}
				
		}
	}
	
	return {
		init
	}
})();