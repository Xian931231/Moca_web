const supplyMainList = (function () {
	
	// 해당 페이지 초기화 함수
	function init(){
		_setStandardTime();
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
		
		let event = _event;
		
		if(type_v == "click") {
			if(act_v == "clickTab") {
				event.clickTab(evo);
			} else if(act_v == "clickSorting") {
				event.clickSorting(evo);
			}
		}
	}
	
	// 기준 시간
	function _setStandardTime() {
		let now = moment().format("YYYY-MM-DD 기준");
		$("#standardTime").html(now);
	}
	
	// 이벤트 담당
	let _event = {
		// 탭 분기
		clickTab: function(evo) {
			// 화살표 리셋
			$("[data-sort]").removeClass("turningA");
			
			let tab = evo.attr("data-tab");
			
			if(tab == "impressions") {
				$("#tip").attr({"data-original-title":  "매일 자정 전일자 데이터가 반영됩니다."});
				_list.getImpressionList();
			} else if(tab == "product") {
				$("#tip").attr({"data-original-title":  "매일 자정 초기화 됩니다."});
				_list.getProductList();
			} else if(tab == "error") {
				$("#tip").attr({"data-original-title":  "매일 자정 초기화 됩니다."});
				_list.getErrorList();
			}
		},
		
		// 정렬 화살표 클릭
		clickSorting: function(evo) {
			// 화살표 클래스 수정
			evo.toggleClass("turningA");
			
			let sort = evo.attr("data-sort");
			let target = evo.attr("data-target");
			let hasClass = false;
			if(evo.hasClass("turningA") == true) {
				hasClass = true;
			} else {
				hasClass = false;
			}
			
			let sort_v = {};
			
			if(sort == "impressions") {
				if(target == "product") {
					if(hasClass) {
						sort_v.productSort = "PA";
					} else {
						sort_v.productSort = "PD";
					}
					sort_v.impressionsSort = null;
					$("#impressionsI").removeClass("turningA");
				} else {
					if(hasClass) {
						sort_v.impressionsSort = "IA"
					} else {
						sort_v.impressionsSort = "ID"
					}
					sort_v.productSort = null;
					$("#impressionsP").removeClass("turningA");
				}
				_list.getImpressionList(sort_v);
			} else if(sort == "product") {
				if(hasClass) {
					sort_v.dateSort = "DA"
				} else {
					sort_v.dateSort = null
				}
				_list.getProductList(sort_v);
			} else if(sort == "error") {
				if(target == "product") {
					if(hasClass) {
						sort_v.errorProductSort = "PA"
					} else {
						sort_v.errorProductSort = "PD"
					}
					sort_v.errorSort = null;
					$("#errorE").removeClass("turningA");
				} else if(target == "error") {
					if(hasClass) {
						sort_v.errorSort = "EA"
					} else {
						sort_v.errorSort = "ED"
					}
					sort_v.errorProductSort = null;
					$("#errorP").removeClass("turningA");
				}
				_list.getErrorList(sort_v);
			}
		},
	}
	
	let _list = {
		getList: function() {
			// 노출수
			_list.getImpressionList();
			
			// 운영 중 상품
			_list.getProductList();
			
			// 장비 오류
			_list.getErrorList();
		},
		
		
		// 노출 수 리스트 가져오기
		getImpressionList: function(sort) {
			let url_v = "/supply/product/impressions/list";
			
			let data_v = {
				"standard_date": moment().format("YYYY-MM-DD"),
				"impression_sort": null,
				"product_sort": null,
			};
			
			if(sort) {
				data_v.impression_sort = sort.impressionsSort;
				data_v.product_sort = sort.productSort;
			}
			
			comm.send(url_v, data_v, "POST", function(resp) {
				dev.log(resp);
				if(resp) {
					_list.drawImpressionList(resp);
				}
			});
		},
		
		// 운영 중 상품 리스트 가져오기
		getProductList: function(sort) {
			let url_v = "/supply/product/progress/list";
			
			let data_v = {
				"standard_date": moment().format("YYYY-MM-DD"),
				"sort": null
			};
			
			if(sort) {
				data_v.sort = sort.dateSort;
			}
			
			comm.send(url_v, data_v, "POST", function(resp) {
				dev.log(resp);
				if(resp) {
					_list.drawProductList(resp);
				}
			});
		},
		
		// 장비 오류 현황 리스트 가져오기
		getErrorList: function(sort) {
			let url_v = "/supply/device/error/list";
			
			let data_v = {
				"standard_date": moment().format("YYYY-MM-DD"),
				"product_sort": null,
				"error_sort": null
			};
			
			if(sort) {
				data_v.product_sort = sort.errorProductSort;
				data_v.error_sort = sort.errorSort;
			}
			
			comm.send(url_v, data_v, "POST", function(resp) {
				dev.log(resp);
				if(resp) {
					_list.drawErrorList(resp);
				}
			});	
		},
		
		// 노출 수 리스트 그리기
		drawImpressionList: function(resp) {
			// 총 노출수
			$("#totalImpressionCnt").html(util.numberWithComma(resp.tot_cnt));
			
			let tbody_o = $("#impressionTable").empty();
			
			if(resp.list && resp.list.length > 0) {
				for(let item of resp.list) {
					let tr_o = $("<tr>");
					tbody_o.append(tr_o);
					
					{
						// seq
						let td_o = $("<td>").html(item.seq);
						tr_o.append(td_o);
					}
					{
						// 분류
						let td_o = $("<td>").html(item.category_name);
						tr_o.append(td_o);
					}
					{
						// 상품명
						let td_o = $("<td>").html(item.product_name);
						tr_o.append(td_o);
					}
					{
						// 노출 수
						let td_o = $("<td>").html(util.numberWithComma(item.total_cnt));
						tr_o.append(td_o);
					}
				}
				
				if(resp.list.length < 10) {
					_list.drawCommonList(resp.list.length, tbody_o, 4);
				}
			} else {
				$("#impressionsWrapDiv").addClass("notnotnot").html("진행중인 광고가 없습니다.");
				_list.drawCommonList(0, tbody_o, 4);
			}
			
			_evInit();
		},
		
		// 운영 중 상품 리스트 그리기
		drawProductList: function(resp) {
			// 운영 중 상품 수
			$("#totalProductCnt").html(util.numberWithComma(resp.tot_cnt));
			
			let tbody_o = $("#productTable").empty();
			
			if(resp.list && resp.list.length > 0) {
				for(let item of resp.list) {
					for(let i = 0; i < item.product_list.length; i++) {
						let product = item.product_list[i];
						
						let tr_o = $("<tr>");
						tbody_o.append(tr_o);
					
						// 분류
						if(i == 0) {
							let td_o = $("<td>").attr({
								"rowspan": item.product_list.length 
							}).html(item.category_name);
							tr_o.append(td_o);
						}
						
						{
							// 상품명
							let td_o = $("<td>").html(product.product_name);
							tr_o.append(td_o);
						}
						{
							// 화면 규격
							let td_o = $("<td>");
							if(util.valNullChk(product.screen_resolution)) {
								td_o.html("-");
							} else {
								td_o.html(product.screen_resolution);
							}
							tr_o.append(td_o);
						}
						{
							// 등록일자
							let td_o = $("<td>").html(product.insert_date);
							tr_o.append(td_o);
						}
					}
				}
				
				if(resp.tot_cnt < 10) {
					_list.drawCommonList(resp.tot_cnt, tbody_o, 4);
				}
			} else {
				$("#productWrapDiv").addClass("notnotnot").html("운영중인 상품이 없습니다.");
				_list.drawCommonList(0, tbody_o, 4);
			}
			
			_evInit();
		},
		
		// 장비 오류 현황 리스트 그리기
		drawErrorList: function(resp) {
			// 장비 오류 수
			$("#totalErrorCnt").html(util.numberWithComma(resp.tot_cnt));
			
			let tbody_o = $("#errorTable").empty();
			
			if(resp.list && resp.list.length > 0) {
				for(let item of resp.list) {
					let tr_o = $("<tr>");
					tbody_o.append(tr_o);
					
					{
						// seq
						let td_o = $("<td>").html(item.seq);
						tr_o.append(td_o);
					}
					{
						// 상품명
						let td_o = $("<td>").html(item.product_name);
						tr_o.append(td_o);
					}
					{
						// 시리얼번호
						let td_o = $("<td>").html(item.serial_number);
						tr_o.append(td_o);
					}
					{
						// 상태
						let status = item.status;
						let statusText = "";
						if(status == "R") {
							statusText = "수리중";	
						} else if(status == "D") {
							statusText = "폐기";
						}
						let td_o = $("<td>").html(statusText);
						tr_o.append(td_o);
					}
					{
						// 오류 발생 시간
						let td_o = $("<td>").html(item.update_date);
						tr_o.append(td_o);
					}
				}
				
				if(resp.list.length < 10) {
					_list.drawCommonList(resp.list.length, tbody_o, 5);
				}
			} else {
				$("#errorWrapDiv").addClass("notnotnot").html("오류 디바이스가 없습니다.");
				_list.drawCommonList(0, tbody_o, 5);
			}
			
			_evInit();
		},

		// 리스트 row 10개 유지		
		drawCommonList(row, tbody_o, count) {
			if(row < 10) {
				for(let i = row; i < 10; i++) {
					let tr_o = $("<tr>");
					tbody_o.append(tr_o);
					
					for(let j = 0; j < count; j++) {
						let td_o = $("<td>");
						tr_o.append(td_o);
					}
				}
			}
		},
		
	}
	
	return {
		init,
	}
	
})();