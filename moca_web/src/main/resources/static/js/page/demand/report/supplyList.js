const supplyReportList = (() => {
	let _total = 0;
	
	//조회한 data 보관용
	let _old_data_v = {};
	
	// 해당 페이지 초기화 함수
	function init() {
		_evInit();
		_setDatepicker();
		_list.getSupplyMemberList();
	}
	
	// 이벤트 초기화 
	function _evInit() {
		let evo = $("[data-src='supplyReportList'][data-act]").off();
		evo.on("click keyup", function(ev){
			_action(ev);
		});
	}
	
	// 이벤트 분기 함수
	function _action(ev) {
		let evo = $(ev.currentTarget);
		
		let act_v = evo.attr("data-act");
		
		let type_v = ev.type;
		
		let event = _event;
		
		if(type_v == "click") {
			if(act_v == "getSearch"){
				_list.getSupplyReportList();
			}else if(act_v == "excelDownload"){
				event.excelDownload();
			}
		} else if(type_v == "keyup") {
			
		}
	}
	// datepicker 설정
	function _setDatepicker() {
		customDatePicker.init("sDate").datepicker("setDate",moment().subtract(7, "days").format("YYYY-MM-DD"));
		customDatePicker.init("eDate").datepicker("setDate",moment().format("YYYY-MM-DD"));
	}
	
	let _event = {
		//엑셀 다운로드
		excelDownload: function(){
			let url_v = "/report/demand/excel/bySupply";
			
			util.blobFileDownload(url_v, _old_data_v, function(){
				console.log("done")				
			});
		}
	}
	
	let _list = {
		//매체사 조회
		getSupplyMemberList: function(callback){
			let url_v = "/report/demand/list/supply/member";
			
			comm.send(url_v, null, "POST", function(resp){
				let option = {
					element: $("#searchSupplySelect")
					, dataList: resp.list
					, isAllOption: true
					, textAllOption: "전체 매체"
					, valueKey: "member_id"
					, textKey: "company_name"
				}
				
				
				multiSelectPicker.init(option);
				
				let memberArr = [0];
				for(let item of resp.list){
					memberArr.push(item.member_id);
				}
				
				//$("#searchSupplySelect").val(memberArr);
				$("#searchSupplySelect").selectpicker("refresh");	
				
			})
		},
		
		// 매체사 담당 광고주 selectBox setting
		drawMemberList: function(list){
			let memberArr = [];
			let select_o = $("#searchSupplySelect").html("");
			select_o.selectpicker("destroy");
			
			select_o.append(new Option("전체 매체", ""));
			
			for(let item of list){
				select_o.append(new Option(item.company_name, item.member_id));
				memberArr.push(item.member_id);
			}
			
			//전체선택 
			$("#searchSupplySelect").val(memberArr);
			
			select_o.selectpicker("refresh");
			
			_evInit();
			
		},
		
		//매체별 리포트 조회
		getSupplyReportList: function(){
			let startDate_v = $("#sDate").val();
			let endDate_v = $("#eDate").val();
			
			if(util.getDiffDate(startDate_v, endDate_v, "months") >= 3) {
				customModal.alert({
					content: "최대 조회 기간은 3개월입니다.",
				});
				return;
			}
			
			if(moment(startDate_v).isAfter(endDate_v)) {
				customModal.alert({
					content: "시작일이 종료일 이후일 수 없습니다.",
				});
				return;
			}			
			
			let url_v = "/report/demand/list/bySupply";
			
			let data_v = {
				str_dt: startDate_v
				, end_dt: endDate_v
			};
			
			let memberList = $("#searchSupplySelect").selectpicker("val");
			
			if(memberList.length > 0 ){
				memberList = memberList.map(member => {
					return parseInt(member);
				});
			}
			
			data_v.member_list = memberList; 
			
			_old_data_v = data_v;
			comm.send(url_v, data_v, "POST", function(resp){
				//data가 있으면 엑셀 버튼 노출
				if(resp.list.length > 0){
					$("#excelBtn").show();
				}else{
					$("#excelBtn").hide();
				}
				
				_list.drawSupplyReportList(resp.list);
				_list.drawChartTable(resp.list);
			});
			
		},
		//매체별 리포트 그리기
		drawSupplyReportList: function(list){
			let body_o = $("#listBody").html("");
			let total_o = $("#listBodyTotal").html("");
			
			_total = 0;
			for(let item of list){
				let tr_o = $("<tr>");
				body_o.append(tr_o);
				
				//매체사
				let th1_o = $("<th>").html(item.company_name).attr({
					"rowspan": item.rowspan
				});
				tr_o.append(th1_o);
				
				//카테고리
				let categoryList = item.category_list;
				if(categoryList.length > 0){
					for(let c = 0; c < categoryList.length; c++){
						let categoryItem = categoryList[c];
						if(c > 0){
							tr_o = $("<tr>");
							body_o.append(tr_o);
						}
						let th2_o = $("<th>").html(categoryItem.category_name).attr({
							"rowspan": categoryItem.rowspan
						})
						tr_o.append(th2_o);
						
						let productList = categoryItem.product_list;
						if(productList.length > 0){
							for(let p=0; p < productList.length; p++){
								let productItem = productList[p];
								if(p > 0){
									tr_o = $("<tr>");
									body_o.append(tr_o);
								}
								//상품
								{
									let td_o = $("<td>").html(productItem.product_name);
									tr_o.append(td_o);
								}
								//노출량
								{
									let td_o = $("<td>").html(util.numberWithComma(productItem.exposure_cnt));
									tr_o.append(td_o);
								}
								_total += productItem.exposure_cnt;
							}
						}else{
							tr_o.append($("<td>").html("-"));
							tr_o.append($("<td>").html("-"));
						}
						
					}
				}else{
					tr_o.append($("<th>").html("-"));
					tr_o.append($("<td>").html("-"));
					tr_o.append($("<td>").html("-"));
				}
				
			}
			total_o.html(util.numberWithComma(_total));
		},
		//매체별 차트 그리기
		drawChartTable: function(list){
			let list_o = $("#chartTableList").html("");
			
			for(let item of list){
				let section_o = $("<section>").addClass("bar-graph bar-graph-horizontal");
					list_o.append(section_o);
				{
					// 매체명
					let media_o = $("<div>").addClass("media").html("<p>" + item.company_name + "</p>");
					section_o.append(media_o);
				}
				{
					let categoryList = item.category_list;
					if(categoryList.length > 0) {
						for(let categoryItem of categoryList){
							let wrap_o = $("<div>").addClass("m_cate_wrap");
							section_o.append(wrap_o);
							
							{
								// 구분 
								let mediaCate_o = $("<div>").addClass("media_cate").html(categoryItem.category_name);
								wrap_o.append(mediaCate_o);
							}
							{
								// bar
								let allBarWrap_o = $("<div>").addClass("all_bar_wrap");
								wrap_o.append(allBarWrap_o);
								
								let productList = categoryItem.product_list;
								if(productList) {
									for(let productItem of productList) {
										let barWrap_o = $("<div>").addClass("bar_wrap");
										allBarWrap_o.append(barWrap_o);
										
										let product_o = $("<span>").addClass("product").html(productItem.product_name);
										barWrap_o.append(product_o);
										
										let figure_o = $("<span>").addClass("figure").html(util.numberWithComma(productItem.exposure_cnt));
										barWrap_o.append(figure_o);
										
										let div_o = $("<div>");
										barWrap_o.append(div_o);
										
										if(productItem.exposure_cnt > 0){
											let percentage =0;
								
											//chart 백분율
											if(_total != 0){
												percentage = (productItem.exposure_cnt/_total)*100;
											}
											let bar_o = $("<div>").addClass("bar").css("width", Math.ceil(percentage) + "%");
											div_o.append(bar_o);	
										}else{
											let bar_o = $("<div>").addClass("not-bar");
											div_o.append(bar_o);	
										}
									}
								}
							}
						}
					}else{
						//캠페인이 없을때
						let cate_not = $("<div>").addClass("m_cate_not").html("광고가 없습니다.");
						section_o.append(cate_not);
					}
				}
			}
				
		}
	}
	
	return {
		init,
	}
	
	
})();