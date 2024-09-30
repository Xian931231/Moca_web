const agencyCostList = (function() {
	
	function init() {
		_config.setUpPage();
	}
	
	function _evInit() {
		let evo = $("[data-src='costList'][data-act]").off();
		
		evo.on("click change keyup", function(e) {
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
			} else if (act_v == "confirmDiposit") {
				event.confirmDiposit(evo);
			} else if (act_v == "confirmApproval") {
				event.confirmApproval(evo);
			} else if (act_v == "loginDemand") {
				event.loginDemand(evo);
			} else if (act_v == "moveDemandSg") {
				event.moveDemandSg(evo);
			}
		} else if (type_v == "change") {
			if (act_v == "changeAgencyRole") {
				event.changeAgencySelect(evo);
			} else if (act_v == "changeAgencyStaff") {
				event.changeAgencySelect(evo);
			} 
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
			_config.setAgencyRole();
			_config.setAgencyStaff();
			_config.setAgencyDemand();
			_list.getList();
			_list.setDefaultDate();
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
		
		// 구뷴 select
		setAgencyRole: function() {
			let url_v = "/role/agency/staff/list";
			
			comm.send(url_v, {}, "POST", function(response) {
				_config.drawSelect("agency-role", response.list);
			});
		},
		
		// 담당자 select
		setAgencyStaff: function() {
			let url_v = "/member/agency/sg/manage/staff/list";
			let data_v = {
				staff_role_id: $("#agency-role").val()
			}
			
			comm.send(url_v, data_v, "POST", function(response) {
				_config.drawSelect("agency-staff", response.list);
			});
		},
		
		// 광고주 select
		setAgencyDemand: function() {
			let url_v = "/member/agency/sg/manage/demand/list";
			
			let data_v = {
				staff_role_id: $("#agency-role").val(),
				staff_id: $("#agency-staff").val()
			};
			
			comm.send(url_v, data_v, "POST", function(response) {
				_config.drawSelect("agency-demand", response.list);
			});
		},
		
		// select 그리기
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
		// 구분 및 담당자를 선택 시 하위 select 변경
		changeAgencySelect: function(evo) {
			let elementId = $(evo).attr("id");
			let changeValue = $("#" + elementId).val();
			$("#" + elementId).selectpicker("val", changeValue);
			
			if (elementId == "agency-role") {
				_config.setAgencyStaff();
				_config.setAgencyDemand();
			} else {
				_config.setAgencyDemand();
			}
		},
		
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
				// alert 최대 조회 기간은 3개월입니다.
				customModal.alert({
					content: "최대 조회 기간은 3개월입니다.",
				});
				
				return;
			}
			
			_list.getList();
		},
		
		confirmDiposit: function(evo) {
			let parent = evo.parent();
			let depositor = parent.attr("data-name");
			let dipositPrice = parent.attr("data-price");
			
			$("#depositor").html(depositor);
			$("#deposit-price").html(dipositPrice);
		},
		
		confirmApproval: function(evo) {
			let parent = evo.parent();
			let rejectReason = parent.attr("data-content");
			
			$("#reject-reason").html(rejectReason);
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
			
			let agencyStaffRole = $("#agency-role").val();
			if (agencyStaffRole) {
				data.staff_role_id = agencyStaffRole 
			}
			
			let agencyStaff = $("#agency-staff").val();
			if (agencyStaff) {
				data.staff_id = agencyStaff 
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
						"data-demand-uid": item.demand_uid,
						"data-sg-id": item.sg_id
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
						let td_o = $("<td>").attr({
							"data-name": "",
							"data-price": ""
						});
						
						switch(item.pay_status_code) {
							case "PAY_WAIT":
								td_o.html("입금 대기").css("color", "red");
								break;
							case "PAY_COMPLETE":
								let btn_o = $("<button>").attr({
									"type": "button",
									"class": "btn btn-approved",
									"data-toggle": "modal",
									"data-target": "#diposit-modal",
									"data-src": "costList",
									"data-act": "confirmDiposit",
								}).html("입금 완료");
								
								if (item.demand_name) {
									td_o.attr("data-name", item.demand_name); 
								}
								
								if (item.total_pay_price) {
									td_o.attr("data-price", item.total_pay_price); 
								}
								
								td_o.append(btn_o);
								break;
							case "REFUND_WAIT":
								td_o.html("환불 대기");
								break;
							case "REFUND_COMPLETE":
								td_o.html("환불 완료");
								break;
							default: 
								break;
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