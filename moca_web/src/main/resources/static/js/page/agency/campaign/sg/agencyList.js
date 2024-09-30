const agencyCampaignList = (function() {
	
	function init() {
		_config.setUpPage();
	}
	
	function _evInit() {
		let evo = $("[data-src='campaignList'][data-act]").off();
		
		$(".selectpicker").selectpicker("refresh");
		
		evo.on("click change keyup", function(e) {
			_action(e);
		});
	}
	
	function _action(e) {
		let evo = $(e.currentTarget);
		
		let act_v = evo.attr("data-act");
		
		let type_v = e.type;
		
		let event = _evnet;
		
		if (type_v == "click") {
			if (act_v == "clickSearch") {
				event.listSearch();
			} else if (act_v == "loginDemand") {
				event.loginDemand(evo);
			} else if (act_v == "moveDemandCampaign") {
				event.moveDemandCampaign(evo);
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
			_config.setAgencyRole();
			_config.setAgencyStaff();
			_config.setAgencyDemand();
			_list.setDefaultDate();
			_list.getList();
		},
		
		// 구뷴 set
		setAgencyRole: function() {
			let url_v = "/role/agency/staff/list";
			
			comm.send(url_v, {}, "POST", function(response) {
				dev.log(response);
				_config.drawSelect("agency-role", response.list);
	
			});
		},
		
		// 직원 set
		setAgencyStaff: function() {
			let url_v = "/member/agency/sg/manage/staff/list";
			let data_v = {
				staff_role_id: $("#agency-role").val()
			}
			
			comm.send(url_v, data_v, "POST", function(response) {
				_config.drawSelect("agency-staff", response.list);
			});
		},
		
		// 광고주 set
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
	
	let _evnet = {
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
			let diff = util.getDiffDate(sDate, eDate, "month");
			
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
		
		// 광고주 계정으로 로그인
		loginDemand: function(evo) {
			let parent = $(evo).closest("button");
			let memberId = $(parent).attr("data-demand-id");
			let memberUid = $(parent).attr("data-demand-uid");
			let memberUtype = globalConfig.memberType.DEMAND.utype;
			
			util.staffLogin({
				memberId,
				memberUid,
				memberUtype,
			});
		},
		
		// 광고주 캠페인 내 광고 목록 페이지
		moveDemandCampaign: function(evo) {
			let parent = $(evo).closest("tr");
			let memberId = $(parent).attr("data-demand-id");
			let memberUid = $(parent).attr("data-demand-uid");
			let memberUtype = globalConfig.memberType.DEMAND.utype;
			let campaignId = $(parent).attr("data-campaign-id");
			
			let callback = function(response) {
				if (response.result) {
					location.href = "/demand/campaign/sg/list?id=" + campaignId;
				}
			}
			
			util.staffLogin({
				memberId,
				memberUid,
				memberUtype,
				callback
			});
		},
		
		// 광고주 광고 상세 페이지
		moveDemandSg: function(evo) {
			let parent = $(evo).closest("tr");
			let memberId = $(parent).attr("data-demand-id");
			let memberUid = $(parent).attr("data-demand-uid");
			let memberUtype = globalConfig.memberType.DEMAND.utype;
			let sgId = $(parent).attr("data-sg-id");
			
			let callback = function(response) {
				if (response.result) {
					location.href = "/demand/campaign/sg/detail?id=" + sgId;
				}
			}

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
			//customDatePicker.init("s-date").datepicker("setDate",moment().subtract(7, "days").toDate());
			//customDatePicker.init("e-date").datepicker("setDate", moment().toDate());
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
			
			let sgStatus = $("#sg-status").val();
			if (sgStatus) {
				data.sg_status = sgStatus
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
			let url_v = "/member/agency/sg/manage/campaign/list";
			
			let data_v = _list.getSearchData();
			
			let option = {
				limit: 10
			};
			
			let page_o = $("#list-page").customPaging(option, function(_currPage) {
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
			})
		}, 
		
		drawList: function(list) {
			dev.log(list);
			
			let list_o = $("#accoTableList").empty();
			
			if (list.length > 0) {
				for (item of list) {
					let totalExposureCount = 0; // 총 노출량
					let totalSgPrice = 0; // 집행 금액
					
					let campaignList = item.campaign_list;
					
					/**
						광고주 목록 계층 시작
					 */
					
					// 광고주 담당자 표시 버튼 (광고주 목록 최상위 태그)
					let btn_o = $("<button>").attr({
						"class": "btn btn-block mb-2 text-left accordion-btn",
						"data-toggle": "collapse",
						"data-target": "#acco" + item.demand_id,
						"data-demand-id": item.demand_id,
						"data-demand-uid": item.demand_uid,
					});
					
					// 광고주명(ID)
					{
						let span_o = $("<span>").html(item.demand_name + "(");
						
						let a_o = $("<a>").attr({
							"href": "javascript:;",
							"data-src": "campaignList",
							"data-act": "loginDemand"
						}).html(item.demand_uid);
						
						span_o.append(a_o);
						span_o.html(span_o.html() + ")");
						btn_o.append(span_o);
					}
					
					//광고주 담당자
					{
						let parent_span_o = $("<span>");
						
						if (item.staff_id) {
							let first_child_span_o = $("<span>").attr({
								"id": "staff" + item.demand_id
							}).html("담당 : " + item.staff_name + " | ");
	
							parent_span_o.append(first_child_span_o);
							
						}
						
						let second_child_span_o = $("<span>").attr({
							"id": "status" + item.demand_id
						}).html("진행중 " + item.processing_sg_count + " / 전체 " + item.total_sg_count);
						
						parent_span_o.append(second_child_span_o);
						btn_o.append(parent_span_o);
					}
					
					// 상위 광고주 append
					list_o.append(btn_o);
					
					/**
						캠페인 및 광고 목록 계층 시작
					 */
					
					// 캠페인 및 광고 목록 최상위 태그
					let top_div_o = $("<div>").attr({
						"id": "acco" + item.demand_id,
						"class": "collapse",
						"data-parent": "#accoTableList"
					});
					
					// 테이블 wrap
					{
						let table_wrap_div_o = $("<div>").addClass("tableWrap agencyCamp");
						let table_inner_div_o = $("<div>").addClass("tableInner");
						table_wrap_div_o.append(table_inner_div_o);
						
						// 테이블
						let table_o = $("<table>").addClass("table");
						
						// colgroup
						{
							let colgroup_o = $("<colgroup>");
							
							colgroup_o.append(
								$("<col>").css("width", "80px"),
								$("<col>").css("width", "230px"),
								$("<col>").css("width", "230px"),
								$("<col>").css("width", "100px"),
								$("<col>").css("width", "140px"),
								$("<col>").css("width", "140px"),
								$("<col>").css("width", "140px"),
								$("<col>").css("width", "*"),
							);
							
							table_o.append(colgroup_o);
						}
						
						// thead
						{
							let thead_o = $("<thead>");
							{
								let tr_o = $("<tr>");
								
								tr_o.append(
									$("<th>").html("과금방식"),
									$("<th>").html("캠페인명"),
									$("<th>").html("광고명"),
									$("<th>").html("총 노출량"),
									$("<th>").html("집행 금액"),
									$("<th>").html("광고 시작"),
									$("<th>").html("광고 종료"),
									$("<th>").html("진행 상태"),	
								);
								
								thead_o.append(tr_o);
							}
							
							table_o.append(thead_o);
						}
						
						// tbody
						{
							let tbody_o = $("<tbody>");
							
							let sgTotalCount = 0; // 전체 광고 수
							if (campaignList.length > 0) {
								// 캠페인 목록 Loop
								for (campaign of campaignList) {
									let sgList = campaign.sg_list;
									let sgListCount = 0; // 광고 목록 중 처음 광고가 아니면 tr을 생성해야 함
									let tr_o = $("<tr>")
									
									// 과금방식
									{
										let td_o = $("<td>").html(campaign.pay_type);
										if (sgList.length != 0) {
											td_o.attr({
												"rowspan": sgList.length
											});
										}
										
										tr_o.append(td_o);
									}
									// 캠페인명
									{
										let td_o = $("<td>");
										if (sgList.length != 0) {
											td_o.attr({
												"rowspan": sgList.length
											});
										}
										
										let a_o = $("<a>").attr({
											"href": "javascript:;",
											"data-src": "campaignList",
											"data-act": "moveDemandCampaign"
										}).html(campaign.campaign_name);
										
										td_o.append(a_o);
										tr_o.append(td_o);
									}
									
									// 광고 목록 Loop
									if (sgList.length > 0) {
										for (sg of sgList) {
											sgTotalCount++;
											totalSgPrice = totalSgPrice + parseInt(sg.price);
											
											let sg_tr_o = "";
											
											if (sgListCount == 0) {
												sg_tr_o = tr_o;
											} else {
												sg_tr_o = $("<tr>");
											}
											
											sg_tr_o.attr({
												"data-sg-id": sg.sg_id,
												"data-campaign-id": campaign.campaign_id,
												"data-demand-id": item.demand_id,
												"data-demand-uid": item.demand_uid,
											});
											
											// 광고명
											{
												let td_o = $("<td>");
												
												let a_o = $("<a>").attr({
													"href": "javascript:;",
													"data-src": "campaignList",
													"data-act": "moveDemandSg"
												}).html(sg.sg_name);
												
												td_o.append(a_o);
												sg_tr_o.append(td_o);
											}
											// 총 노출량
											{
												let td_o = $("<td>").html(sg.sg_total_exposure_count);
												totalExposureCount += sg.sg_total_exposure_count;
												
												sg_tr_o.append(td_o);
											}
											// 집행 금액
											{
												let td_o = $("<td>").html("\\" + util.numberWithComma(sg.price));
												
												sg_tr_o.append(td_o);
											}
											// 광고 시작
											{
												let td_o = $("<td>");
												
												if (sg.display_start_date) {
													td_o.html(sg.display_start_date);
												} else {
													td_o.html("-");
												}
												
												sg_tr_o.append(td_o);
											}
											// 광고 종료
											{
												let td_o = $("<td>");
												
												if (sg.status == 7) {
													td_o.html(sg.stop_date);
												} else {
													if (sg.display_end_date) {
														td_o.html(sg.display_end_date);
													} else {
														td_o.html("-");
													}
												}
												
												sg_tr_o.append(td_o);
											}
											// 진행 상태
											{
												let td_o = $("<td>");
												let span_o = $("<span>");
												
												switch(sg.status) {
													case 0:
														span_o.html("승인대기");
														break;
													case 1: 
														span_o.html("진행중");
														break;
													 case 2: 
														span_o.html("일시중지");
														break;
													case 7: case 8:
														span_o.html("종료");
														break;
													case 9:
														span_o.html("승인거부").css("color", "red");
														break;
													default:
														break;
												}
												
												td_o.append(span_o);
												sg_tr_o.append(td_o);
											}
											
											sgListCount++;
											
											tbody_o.append(sg_tr_o);
										// end sgList for
										}
									}
								// end campaignList for
								}
							}
							
							table_o.append(tbody_o);
							table_inner_div_o.append(table_o);
							
							if (sgTotalCount == 0) {
								$("<div>").addClass("notnotnot")
									.html("광고가 없습니다.")
									.prependTo(table_wrap_div_o);
								
								let tr_o = $("<tr>").append(
									$("<td>"),
									$("<td>"),
									$("<td>"),
									$("<td>"),
									$("<td>"),
									$("<td>"),
									$("<td>"),
									$("<td>")
								);
								
								tbody_o.append(tr_o);
							}
						// end tbody
						}
						
						// 총 노출량, 집행 금액 total
						let table_footer_div_o = $("<div>").addClass("tableFooterFix cpm1");
						
						if (totalExposureCount != 0 || totalSgPrice != 0) {
							let ul_o = $("<ul>");
							
							ul_o.append(
								$("<li>"),
								$("<li>"),
								$("<li>"),
								$("<li>").html(util.numberWithComma(totalExposureCount)),
								$("<li>").html("\\" + util.numberWithComma(totalSgPrice)),
								$("<li>"),
								$("<li>"),
								$("<li>"),	
							);
							
							table_footer_div_o.append(ul_o);
						} else {
							let ul_o = $("<ul>");
							
							ul_o.append(
								$("<li>"),
								$("<li>"),
								$("<li>"),
								$("<li>"),
								$("<li>"),
								$("<li>"),
								$("<li>"),
								$("<li>"),	
							);
							
							table_footer_div_o.append(ul_o);
						}
						
						table_wrap_div_o.append(table_footer_div_o);
	
						top_div_o.append(table_wrap_div_o);
					// end table wrap
					}
					list_o.append(top_div_o);
				}
				$("#accoWrap").removeClass("blank");
				$("#accoTableList").removeClass("notnotnot");
				$("#list-page").show();
			} else {
				$("#accoWrap").addClass("blank");
				$("#accoTableList").addClass("notnotnot").html("검색 결과가 없습니다.");
				$("#list-page").hide();
			}
		}
	}
	
	return {
		init
	}
})();