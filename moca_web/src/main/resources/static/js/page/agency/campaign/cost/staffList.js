const staffCostList = (function() {
	
	function init() {
		_config.setUpPage();
	}
	
	function _evInit() {
		let evo = $("[data-src='costList'][data-act]").off();
		
		evo.on("click keyup", function(e) {
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
			} else if (act_v == "loginDemand") {
				event.loginDemand(evo);
			} else if (act_v == "moveDemandSg") {
				event.moveDemandSg(evo);
			}
		} else if (type_v == "change") {
			
		} else if (type_v == "keyup") {
			if (act_v == "enterSearch") {
				if (e.keyCode == 13) {
					event.listSearch();
				}
			}
		}
	}
	
	let _config = {
		setUpPage: function() {
			_config.initModal();
			_config.setAgencyDemand();
			_list.setDefaultDate();
			_list.getList();
		},
		
		initModal: function() {
			$("#approval-modal").on("hidden.bs.modal", function(e) {
				$("#reject-reason").html("");
			});
			
			$("#diposit-modal").on("hidden.bs.modal", function(e) {
				$("#depositor").html("");
				$("#deposit-price").html("");
			});
		},
		
		setAgencyDemand: function() {
			let url_v = "/member/agency/sg/manage/demand/list";
				
			comm.send(url_v, {}, "POST", function(response) {
				_config.drawSelect("agency-demand", response.list);
			});
		},
		
		drawSelect: function(elementId, list, callback) {
			let select_o = $("#" + elementId);
			$("#" + elementId + " option").not("[value='']").remove();
			
			for (item of list) {
				let option_o = $("<option>");
				
				if (elementId == "agency-role") {
					option_o.val(item.id).html(item.name);
				} else if (elementId == "agency-demand") {
					option_o.val(item.member_id).html(item.company_name + "(" + item.uid + ")");
				} else {
					option_o.val(item.member_id).html(item.uname + "(" + item.uid + ")");
				}
				
				select_o.append(option_o);
			}
			
			if (typeof(callback) == "function") {
				callback();
			}
			
			$("#" + elementId).selectpicker("refresh");
		},
	}
	
	let _event = {
		listSearch: function() {
			let sDate = $("#s-date").val();
			let eDate = $("#e-date").val();
			let diff = util.getDiffDate(sDate, eDate, "month");
			
			if (moment(sDate).isAfter(eDate)) {
				customModal.alert({
					content: "시작일이 종료일 이후일 수 없습니다.",
				});
				
				return;
			}
			
			//if (endDate > afterThreeMonth) {}
			if (diff >= 3) {
				// alert 최대 조회 기간은 3개월입니다.
				customModal.alert({
					content: "최대 조회 기간은 3개월입니다.",
				});
				
				return;
			}
			
			_list.getList();
		},
		
		loginDemand: function(evo) {
			let parent = evo.closest("tr");
			let memberId = parent.attr("data-demand-id");
			let memberUid = parent.attr("data-demand-uid");
			let memberUtype = globalConfig.memberType.DEMAND.utype;
			util.staffLogin({
				memberId,
				memberUid,
				memberUtype
			});
		},
		
		moveDemandSg: function(evo) {
			let parent = evo.closest("tr");
			let sgId = parent.attr("data-sg-id");
			let memberId = parent.attr("data-demand-id");
			let memberUid = parent.attr("data-demand-uid");
			let memberUtype = globalConfig.memberType.DEMAND.utype;
			let callback = function(response) {
				if (response.result) {
					location.href = "/demand/campaign/sg/detail?id=" + sgId;
				}
			};
			
			util.staffLogin({
				memberId,
				memberUid,
				memberUtype,
				callback
			});
		},
		
		confirmApproval: function(evo) {
			let parent = evo.parent();
			let rejectReason = parent.attr("data-content");
			
			$("#reject-reason").html(rejectReason);
		},
	}
	
	let _list = {
		setDefaultDate: function() {
			customDatePicker.init("s-date");
			customDatePicker.init("e-date");
		},
		
		getSearchData: function() {
			let data = {}
			
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
			
			let agencyDemand = $("#agency-demand").val();
			if (agencyDemand) {
				data.demand_id = agencyDemand
			}
			
			// 광고 상태
			let sgStatus = $("#sg-status").val();
			
			if (sgStatus == "전체 상태") {
				
			} else if (isNaN(parseInt(sgStatus)))  {
				data.pay_status_code = sgStatus;
			}
			else {
				data.sg_status = sgStatus;
			}
			
			// 검색어 타입
			let search_type = $("#search_type").val();
			if (search_type) {
				data.search_type = search_type;
			}
			
			// 검색어
			let search_value = $("#search_value").val();
			if (search_value) {
				data.search_value = search_value;
			}
			
			return data;
		},
		
		getList: function(currPage = 1) {
			let url_v = "/member/agency/sg/manage/cost/list";
			
			let data_v = _list.getSearchData();
			
			let page_o = $("#list-page").customPaging({}, function(_currPage) {
				_list.getList(_currPage);
			});
			
			let pageParam = page_o.getParam(currPage);
			
			if (pageParam) {
				data_v.offset = pageParam.offset;
				data_v.limit = pageParam.limit;
			}
			
			comm.send(url_v, data_v, "POST", function(response) {
				dev.log(response);
				
				_list.drawList(response.list);
				page_o.drawPage(response.total_count);
				
				_evInit();
			});
		},
		
		drawList: function(list) {
			let body_o = $("#list-body").empty(); 
			
			if (list.length > 0) {
				for (item of list) {
					let tr_o = $("<tr>").attr({
						"data-demand-id": item.demand_id,
						"data-sg-id": item.sg_id,
						"data-demand-uid": item.demand_uid
					});
					
					// 광고주
					{
						let td_o = $("<td>");
						
						let first_span_o = $("<span>").html(item.demand_name + "<br/>");
						let second_span_o = $("<span>").html("(");
						let a_o = $("<a>").attr({
							"href": "javascript:;",
							"data-src": "costList",
							"data-act": "loginDemand"
						})
						.html(item.demand_uid);
						second_span_o.append(a_o).html(second_span_o.html() + ")");
						
						td_o.append(
							first_span_o,
							second_span_o
						);
						
						tr_o.append(td_o);
					}
					// 담당자
					{
						let td_o = $("<td>");
						
						if (item.staff_id) {
							let staff = item.staff_name;
							let splitStaff = staff.split(",");
							
							if (splitStaff.length > 1) {
								td_o.html(splitStaff[0]);
								
								let outher_div_o = $("<div>").addClass("many-people").html(splitStaff.length);
								let other_div_o = $("<div>").html(splitStaff.join());
								outher_div_o.append(other_div_o);
								td_o.append(outher_div_o);
							} else {
								td_o.html(item.staff_name);
							}
						} else {
							td_o.html("미지정");
						}
						
						tr_o.append(td_o);
					}
					// 과금방식
					{
						let td_o = $("<td>").html(item.pay_type);
						
						tr_o.append(td_o);
					}
					// 광고명
					{
						let td_o = $("<td>");
						let a_o = $("<a>").attr({
							"href": "javascript:;",
							"data-src": "costList",
							"data-act": "moveDemandSg"
						}).html(item.sg_name);
						
						td_o.append(a_o);
						tr_o.append(td_o);
					}
					// 광고신청 일자
					{
						let td_o = $("<td>").html(item.request_date);
						
						tr_o.append(td_o);
					}
					// 광고시작 요청일
					{
						let td_o = $("<td>").html(item.start_ymd);
						
						tr_o.append(td_o);
					}
					// 집행금액
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
								break;
							case "PAY_WAIT":
								td_o.css("color", "red");
								break;
							case "REFUND_COMPLETE":
								payDate = item.pay_status_date;
								break;
							case "REFUND_WAIT":
								td_o.css("color", "green");
								break;
							default:
								break;
						}
						
						td_o.html(item.pay_code_name);
						
						if (payDate) {
							td_o.html(payDate + "<br/>" + td_o.html());	
						}
						
						tr_o.append(td_o);
					}
					// 승인현황
					{
						let td_o = $("<td>").attr("data-content", "");
						
						switch(item.status) {
							case 0:
								p_o = $("<p>").attr({
									"class": "stateBox state-delay"
								}).html("승인 대기");
								
								td_o.append(p_o);
								break;
							case 1: case 2: case 3: case 4: case 7: case 8:
								p_o = $("<p>").attr({
									"class": "stateBox state-end"
								}).html("승인 완료");
								
								td_o.append(p_o);
								break;
							case 9:
								btn_o = $("<button>").attr({
									"class": "btn btn-refusal",
									"type": "button",
									"data-toggle": "modal",
									"data-target": "#approval-modal",
									"data-src": "costList",
									"data-act": "confirmApproval",
								}).html("승인 거부");
								
								if (item.reject_reason) {
									td_o.attr("data-content", item.reject_reason);
								}
								
								td_o.append(btn_o);
								break;
							default:
								break;
						}
						
						tr_o.append(td_o);
					}
					
					body_o.append(tr_o);
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