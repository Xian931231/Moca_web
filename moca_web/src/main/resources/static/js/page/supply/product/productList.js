const supplyProductList = (function () {
	
	// 해당 페이지 초기화 함수
	function init(){
		_setDate();
		_list.getList();
		_evInit();
	}
	
	// 이벤트 초기화 
	function _evInit() {
		let evo = $("[data-src='list'][data-act]").off();
		evo.on("click", function(ev){
			_action(ev);
		});
	}
	
	// 이벤트 분기 함수
	function _action(ev){
		let evo = $(ev.currentTarget);
		
		let act_v = evo.attr("data-act");
				
		let type_v = ev.type;
		
		if(type_v == "click") {
			if(act_v == "clickSearch") {
				_list.getList();
			}
		}
	}
	
	// datepicker 디폴트 날짜 셋팅
	function _setDate() {
		customDatePicker.init("endYmd").datepicker("setDate", moment().toDate());
		customDatePicker.init("startYmd").datepicker("setDate", moment().subtract(6, "days").toDate());
	}
	
	// 기준 시간
	function _setStandardTime() {
		let now = moment().format("YYYY-MM-DD HH:mm분 기준");
		$("#standardTime").html(now);
	}
	
	// 리스트
	let _list = {
		getList: function(curPage = 1) {
			_setStandardTime();
			
			let startDate = $("#startYmd").val();
			let endDate = $("#endYmd").val();
			
			if(util.getDiffDate(startDate, endDate, "months") >= 3) {
				customModal.alert({
					content: "최대 조회 기간은 3개월입니다."
				});
				return;
			}
			
			if(startDate > endDate) {
				customModal.alert({
					content: "시작일이 종료일 이후일 수 없습니다."
				});
				return;
			}
			
			let url_v = "/supply/product/manage/list";
			
			let data_v = {
				"start_ymd": startDate,
				"end_ymd": endDate,
			};
			
			let pageOption = {
				"limit": 10
			}
			
			let page_o = $("#listPage").customPaging(pageOption, function(_curPage){
				_list.getList(_curPage);
			});
			
			let pageParam = page_o.getParam(curPage);
			
			if(pageParam) {
				data_v.offset = pageParam.offset;
				data_v.limit = pageParam.limit;
			}	
			
			comm.send(url_v, data_v, "POST", function(resp) {
				if(resp) {
					_list.drawList(resp.list);
					page_o.drawPage(resp.tot_cnt);
					_evInit();
				}
			});
		},
		
		drawList: function(list) {
			let tbody_o = $("#tableList").empty();
			
			if(list && list.length > 0) {
				for(let item of list) {
					for(let i = 0; i < item.product_list.length; i++) {
						let tr_o = $("<tr>");
						tbody_o.append(tr_o);
						
						let product = item.product_list[i];
						
						// 분류명
						if(i == 0) {
							let td_o = $("<td>").attr({"rowspan": item.product_list.length}).html(item.category_name);
							tr_o.append(td_o);
						}
						
						{
							// 상품명
							let td_o = $("<td>").html(product.product_name);
							tr_o.append(td_o);
						}
						{
							// 해상도
							let td_o = $("<td>");
							if(util.valNullChk(product.screen_resolution)) {
								td_o.html("-");
							} else {
								td_o.html(product.screen_resolution);
							}
							tr_o.append(td_o);
						}
						{
							// 디바이스 수
							let td_o = $("<td>").html(product.device_cnt);
							tr_o.append(td_o);
						}
						{
							// 슬롯수
							let td_o = $("<td>");
							if(product.slot_sum != 0) {
								td_o.html(product.slot_sum + "개 ( CPP : " + product.cpp_slot_count + " / Block : " + product.cpm_slot_count + " ) ");
							} else {
								td_o.html("슬롯 없음")
							}
							tr_o.append(td_o);
						}
						{
							// 노출수
							let td_o = $("<td>")
							if(product.impressions) {
								td_o.html(util.numberWithComma(product.impressions));
							} else {
								td_o.html("0");
							}
							tr_o.append(td_o);
						}
					}
				}
			} else {
				let div_o = $("<div>").addClass("notnotnot").html("상품이 없습니다.");
				tbody_o.append(div_o);
				
				for(let i = 0; i < 11; i++) {
					let tr_o = $("<tr>");
					tbody_o.append(tr_o);
					
					for(let j = 0; j < 6; j++) {
						let td_o = $("<td>");
						tr_o.append(td_o);
					}
				}
			}
		}
	}

	return {
		init,
	}
	
})();