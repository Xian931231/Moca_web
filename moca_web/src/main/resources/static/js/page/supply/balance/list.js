let balanceList = (function(){
	
	let _listData = null;
	let _startDt_o = null;
	let _endDt_o = null;
	let _balanceInfo = null;
	
	function init() {
		_setSearchDate();
		_getBalanceInfo();
		_evInit();
	}
	
	function _evInit() {
		let evo = $("[data-src='list'][data-act]").off();
		$(evo).on("click", function(ev){
			_action(ev);
		});
	}
	
	function _action(ev) {
		let evo = $(ev.currentTarget);
		
		let type_v = ev.type;
		
		let act_v = evo.attr("data-act");
		
		let event = _event;
		
		if(type_v == "click") {
			if(act_v == "clickSearch") {
				_list.getList();
			} else if(act_v == "clickPay") {
				event.clickPay(evo);
			}
		}
	}
	
	function _setSearchDate(){
		let startYear = globalConfig.serviceYear;
		let endYear = moment().format("YYYY");
//		let endYear = 2025;
		
		for(let i=startYear; i<=endYear; i++) {
			
			let option_o = $("<option>").attr({
				"value": i,
			}).html(i + "년");
			
			if(i==startYear) {
				option_o.attr("selected", "selected");
			}
			
			$("#searchStartYear").append(option_o);
			
			option_o = $("<option>").attr({
				"value": i,
			}).html(i + "년");
			
			if(i==endYear) {
				option_o.attr("selected", "selected");
			}
			$("#searchEndYear").append(option_o);
		}
		
		$("#searchStartYear").selectpicker();
		$("#searchEndYear").selectpicker();
		
		$("#searchStartMonth").selectpicker("val", moment().subtract(2, "months").format("MM"));
		$("#searchEndMonth").selectpicker("val", moment().format("MM"));
	}
	
	function _getBalanceInfo() {
		let url_v = "/balance/supply/info";
		
		let data_v = null;
		
		comm.send(url_v, data_v, "POST", function(resp){
			if(resp.result) {
				let data = resp.data;
				
				let info_o = $("#balanceMemberInfo").html("");
				
				if(data.bank_name && data.bank_account_number && data.bank_account_holder && data.balance_day && data.balance_rate) {
					info_o.append("<span>입금 은행 : " + data.bank_name + "</span>");
					info_o.append("<span>계좌 번호 : " + data.bank_account_number + "</span>");
					info_o.append("<span>예금주 : " + data.bank_account_holder + "</span>");
					info_o.append("<span>정산일 : 매월 " + data.balance_day + "일</span>");
					info_o.append("<span>요율 : " + data.balance_rate + "%</span>");
				}
				
				_balanceInfo = data;
				
				_list.getList();
			} else {
				customModal.alert({
					content: "매체 정산정보가 없습니다.",
					confirmCallback: () => {
						location.href = "/";
					}
				});
			}
		});
	}
	
	// 목록 관련
	let _list = {
		// 목록 조회
		getList: (curPage = 1) => {
			
			let startDate = $("#searchStartYear").val() + "-" + $("#searchStartMonth").val();
			let endDate = $("#searchEndYear").val() + "-" + $("#searchEndMonth").val();
			
			if(util.getDiffDate(startDate, endDate, "months") >= 3) {
				customModal.alert({
					content: "최대 조회 기간은 3개월입니다.",
				});
				return;
			}
			
			let mSdate = moment(startDate);
			let mEdate = moment(endDate);
			if(mSdate.isAfter(mEdate)) {
				customModal.alert({
					content: "시작일이 종료일 이후일 수 없습니다.",
				});
				return;
			}
			
			let url_v = "/balance/supply/list";
			
			let data_v = {
				search_start_date: startDate,
				search_end_date: endDate,
			};
			
			let searchType_v = $("#searchType").val();
			let searchValue_v = $("#searchValue").val();
			if(searchValue_v) {
				data_v.search_type = searchType_v;
				data_v.search_value = searchValue_v;
			}
			
			let searchStatus_v = $("#searchStatus").val()
			if(searchStatus_v != "A" ){
				data_v.search_status = searchStatus_v;
			}
			
			
			let page_o = $("#listPage").customPaging(null, (_curPage) => {
				_list.getList(_curPage);
			});
			
			let pageParam = page_o.getParam(curPage);
			
			data_v = $.extend(true, data_v, pageParam);
			
			comm.send(url_v, data_v, "POST", function(resp){
				_listData = resp.list;
				_list.drawList(resp.list);
				page_o.drawPage(resp.tot_cnt);
				_evInit();
			});
		},
		
		// 목록 그리기
		drawList: (list) => {
			let blank_o = $("#blankDiv").hide();
			blank_o.parent().removeClass("blank");
			let listDiv_o = $("#listDiv").show();
			let page_o = $("#listPage").show();
			
			if(list.length === 0) {
				blank_o.show().parent().addClass("blank");
				listDiv_o.hide();
				page_o.hide();
			} else {
				let sumPay = 0;
				let list_o = $("#listBody").html("");
				for(let item of list) {
					
					let tr_o = $("<tr>");
					list_o.append(tr_o);
					
					{
						let td_o = $("<td>").html(item.balance_year + "년 " + item.balance_month + "월");
						tr_o.append(td_o);
					}
					{
						let text = "";
						if(!isNaN(parseInt(item.price))) {
							text = util.numberWithComma(item.price) + " 원"
						} else {
							text = item.price;
						}
						let td_o = $("<td>").html(text);
						tr_o.append(td_o);
					}
					{
						let td_o = $("<td>")
						if(item.status === "R") {
							// 진행중 
							td_o.html("진행중");
						} else {
							// 지급 완료
							let btn_o = $("<button>").attr({
								"type": "button",
								"data-src": "list", 
								"data-act": "clickPay",
								"data-id": item.balance_info_id,
							}).addClass("btn btn-approved").html("지급 완료");
							td_o.append(btn_o);
						}
						
						
						tr_o.append(td_o);
					}
					
					if(!isNaN(parseInt(item.price))) {
						sumPay += parseInt(item.price);
					}
				}
				
				$("#listSumPay").html(util.numberWithComma(sumPay) + "원");
			}
		},
	};
	
	let _event = {
		// 정산금 지급 / 지급완료 버튼 클릭시 이벤트
		clickPay: (evo) => {
			let balanceInfoId = $(evo).attr("data-id");
			let item = _listData.filter((item) => item.balance_info_id == balanceInfoId)[0];
			if(item && item.status === "C") {
				let content = _balanceInfo.bank_name + " / " + _balanceInfo.bank_account_number + " / " + _balanceInfo.bank_account_holder;
				content += "<p> 지급일자 " + item.update_date_str + "</p>";
				customModal.alert({
					content,
				});
			}
		},
	}
	
	return {
		init,
	}
})();