const campaignList = (function () {
	
	// 해당 페이지 초기화 함수
	function init(){
		_list.getList(1, true);
	}
	
	// 이벤트 초기화 
	function _evInit() {
		let evo = $("[data-src='campaignList'][data-act]").off();
		evo.on("click change keyup", function(ev){
			_action(ev);
		});
	}
	customDatePicker.init("startYmd");
	customDatePicker.init("endYmd");
	
	// 이벤트 분기 함수
	function _action(ev){
		let evo = $(ev.currentTarget);
		
		let act_v = evo.attr("data-act");
		
		let type_v = ev.type;
		
		let event = _event;
		
		if(type_v == "click") {
			if(act_v == "search") {
				_list.getList();
			} else if(act_v == "removeCampaign") {
				event.removeCampaign();
			} else if(act_v == "moveAddCampaign") {
				event.moveAddCampaign();
			} else if(act_v == "listSort") {
				event.listSort(evo);
			} else if(act_v == "moveSgList") {
				event.moveSgList(evo);
			}
		} else if(type_v == "change") {
			if(act_v == "changeAllCheckbox") {
				util.setCheckBox(evo);
			}
		} else if(type_v == "keyup") {
			if(act_v == "searchName") {
				if(ev.keyCode == 13) {
					_list.getList();
				}
			}
		}
	}
	
	const _list = {
		getSearchData: function() {
			let data = {};
			
			let searchName_v = $("#searchName").val();
			if(searchName_v) {
				data.search_name = searchName_v;
			}
			
			let startYmd_v = $("#startYmd").val();
			if(startYmd_v) {
				data.start_ymd = startYmd_v;
			}
			
			let endYmd_v = $("#endYmd").val();
			if(endYmd_v) {
				data.end_ymd = endYmd_v;
			}
			
			return data;	
		},
		
		getList: function(curPage = 1, setGuide, sortType) {
			let startYmd = $("#startYmd").val();
			let endYmd = $("#endYmd").val();
			let sDate = new Date(startYmd);
			let eDate = new Date(endYmd);
			
			if(util.getDiffDate(sDate, eDate, "months") >= 3) {
				customModal.alert({
					content: "최대 조회 기간은 3개월입니다."
				});
				return;
			}
			
			if(sDate > eDate) {
				customModal.alert({
					content: "시작일이 종료일 이후일 수 없습니다."
				});
				return;
			}
			
			let url_v = "/campaign/demand/list";
			
			let data_v = _list.getSearchData();
			
			if(sortType) {
				data_v.sort_type = sortType;
			}
			
			let page_o = $("#listPage").customPaging(null, function(_curPage){
				_list.getList(_curPage);
			});
			
			let pageParam = page_o.getParam(curPage);
			
			if(pageParam) {
				data_v.offset = pageParam.offset;
				data_v.limit = pageParam.limit;
			}	
			
			comm.send(url_v, data_v, "POST", function(resp) {
				dev.log(resp);
				if(resp.result) {
					_list.drawList(resp);
					page_o.drawPage(resp.tot_cnt);
					if(setGuide) {
						_list.drawGuide(resp.tot_cnt);
					}
					_evInit();
				}
			});
		},
		
		drawGuide: function(totCnt) {
			if(totCnt == 0) {
				$("#guideText").html("현재 생성된 캠페인이 없습니다.<br/>신청하시려면 먼저 새로운 캠페인을 만들어 주세요.");
			} else {
				$("#guideText").html("캠페인을 선택하시면 신규 광고를 신청하고 관리하실 수 있습니다.");
			}
		},
		
		drawList: function(data) {
			let list = data.list;
			let body_o = $("#listBody").empty();
			for (let item of list) {
				let tr_o = $("<tr>").attr({
					"data-id" : item.campaign_id,
					"data-pay" : item.pay_type
				});
				body_o.append(tr_o);
				{
					// 체크박스
					let td_o = $("<td>");
					let span_o = $("<span>").addClass("chk");
					let label_o = $("<label>").attr("for",  "chk" + item.campaign_id);
					let input_o = $("<input>").attr({
						"type"		: "checkbox",
						"name"		: "campaignCheck",
						"id"		: "chk" + item.campaign_id,
						"data-id"	: item.campaign_id,
					});
					
					if(item.proceed_count) {
						input_o.attr({"data-prc" : true});
					}
						
					span_o.append(input_o);
					span_o.append(label_o);
					td_o.append(span_o);
					tr_o.append(td_o);
				}
				{
					// 과금방식
					let td_o = $("<td>").text(item.pay_type);
					tr_o.append(td_o);
				}
				{
					// 캠페인 이름
					let td_o = $("<td>");
					let a_o = $("<a>").attr({
						"href"		: "javascript:;",
						"data-src"	: "campaignList",
						"data-act"	: "moveSgList",
					}).text(item.name);
					td_o.append(a_o);
					tr_o.append(td_o);
				}
				{
					// 집행 금액
					let price = util.numberWithComma(item.price);
					let td_o = $("<td>").text(price + "원");
					tr_o.append(td_o);
				}
				{
					// 진행중/전체광고 수
					let td_o = $("<td>").text(item.proceed_count +  "/" + item.total_count);
					tr_o.append(td_o);
				}
				{
					// 최종 생성/수정 일시
					let date = item.update_date;
					if(!date) {
						date = item.insert_date;
					}
					let td_o = $("<td>").html(date.replace(" ", "<br>"));
					tr_o.append(td_o);
				}
			}
		}
	}
	
	// 이벤트 담당
	const _event = {
		moveAddCampaign: function() {
			location.href = "/demand/campaign/add";
		},
		
		moveSgList: function(evo) {
			let e = evo.parent().parent();
			let campaignId = e.attr("data-id");
			location.href = "/demand/campaign/sg/list?id=" + campaignId;
		},
		
		removeCampaign: function() {
			let result = _validateData();
			if(result == 1) {
				customModal.confirm({	
					"content" : "캠페인이 삭제됩니다. 계속 하시겠습니까?",
					"confirmText" : "삭제",
					"confirmCallback" : function() {				
						let checkList = $("input[type=checkbox][name=campaignCheck]:checked");
						
						let url_v = "/campaign/demand/remove";
			
						let data_v = {};
						
						let campaignIdList = [];
						checkList.each(function(){
							campaignIdList.push($(this).attr("data-id"));
						});
					
						data_v.campaign_id_list = campaignIdList;
						
						comm.send(url_v, data_v, "POST", function(resp) {
							if(resp.result) {
								$("#allCampaignCheck").prop("checked", false);
								_list.getList();
							} 
						});
					}
				});
			} else if(result == -2) {
				customModal.alert({
					content: "광고가 진행중인 캠페인은 삭제하실 수 없습니다.",
				});
			}
		},
		
		listSort: function(evo) {
			 /*sorting 설정*/
	    	evo.toggleClass("turningA");
			
			let value = evo.attr("data-val");
			let sortType = "";
			if(value == "name") {
				if(evo.hasClass("turningA")) {
					sortType = "ND";
				} else {
					sortType = "NA";
				}
			} else if(value == "price") {
				if(evo.hasClass("turningA")) {
					sortType = "PD";
				} else {
					sortType = "PA";
				}
			}
			_list.getList(1, false, sortType);
		}
	}
	
	function _validateData() {
		let checkList = $("input[type=checkbox][name=campaignCheck]:checked");
		
		let result = 1;
		
		if(checkList.length == 0) {
			result = -1;
		} else {
			checkList.each(function() {
				if($(this).attr("data-prc")) {
					result = -2;
				}
			});
		}
		return result;
	}
	
	return {
		init
	}
	
})();