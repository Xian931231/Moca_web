const demandReportList = (() => {
	let _total = 0;
	
	//조회한 data 보관용
	let _old_data_v = {};
	// 해당 페이지 초기화 함수
	function init() {
		_evInit();
		_setDatepicker();
		_list.getDemandMemberList(function(){
			_list.getDemandReportList();
		});
	}
	
	// 이벤트 초기화 
	function _evInit() {
		let evo = $("[data-src='demandReportList'][data-act]").off();
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
				_list.getDemandReportList();
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
			let url_v = "/report/agency/excel/byDemand";
			
			util.blobFileDownload(url_v, _old_data_v, function(){
				console.log("done")				
			});
		}
	}
	
	let _list = {
		//매체사 담당 광고주 조회
		getDemandMemberList: function(callback){
			let url_v = "/report/agency/access/demand/list";
			
			comm.send(url_v, null, "POST", function(resp){
				let option = {
					element: $("#searchDemandSelect")
					, dataList: resp.list
					, isAllOption: true
					, textAllOption: "전체 광고주"
					, valueKey: "member_id"
					, textKey: "company_name"
				}
				
				multiSelectPicker.init(option);
				
				let memberArr = [0];
				for(let item of resp.list){
					memberArr.push(item.member_id);
				}
				
				$("#searchDemandSelect").val(memberArr);
				$("#searchDemandSelect").selectpicker("refresh");	
				
				if(typeof(callback) == "function"){
					callback();
				}
					
			})
		},
		
		// 매체사 담당 광고주 selectBox setting
		drawMemberList: function(list){
			let memberArr = [];
			let select_o = $("#searchDemandSelect").html("");
			select_o.selectpicker("destroy");
			
			select_o.append(new Option("전체 광고주", ""));
			
			for(let item of list){
				select_o.append(new Option(item.company_name, item.member_id));
				memberArr.push(item.member_id);
			}
			
			//전체선택 
			select_o.val(memberArr);
			
			select_o.selectpicker("refresh");
			
			_evInit();
			
		},
		
		//광고주별 리포트 조회
		getDemandReportList: function(){
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
			
			
			
			let url_v = "/report/agency/list/byDemand";
			
			let data_v = {
				str_dt: startDate_v
				, end_dt: endDate_v
				, member_list: $("#searchDemandSelect").val()
			};
			
			_old_data_v = data_v;
			
			comm.send(url_v, data_v, "POST", function(resp){
				//data가 있으면 엑셀 버튼 노출
				if(resp.list.length > 0){
					$("#excelBtn").show();
				}else{
					$("#excelBtn").hide();
				}
				
				_list.drawDemandReportList(resp.list);
				_list.drawChartTable(resp.list);
			});
		},
		//광고주별 리포트 조회
		drawDemandReportList: function(list){
			let body_o = $("#listBody").html("");
			let total_o = $("#listBodyTotal").html("");
			let rowspan = 0;
			_total = 0;
			for(let item of list){
				let tr_o = $("<tr>");
				body_o.append(tr_o);
				
				//광고주
				if(item.sg_list.length > 0){
					rowspan = item.sg_list.length
					
				}else{
					rowspan = 1;
				}
				
				{
					let th_o=$("<th>").attr({
						"rowspan": rowspan
					});
					th_o.html(item.company_name);
					
					tr_o.append(th_o);	
				}
				
				if(item.sg_list && item.sg_list.length > 0){
					for(let i=0; i<item.sg_list.length; i++){
						let sgItem = item.sg_list[i];
						
						if(i > 0){
							tr_o = $("<tr>");
							body_o.append(tr_o);
						}
						//광고명
						{
							let td_o = $("<td>").html(sgItem.sg_name);
							tr_o.append(td_o);
						}
						
						//노출량
						{
							let td_o = $("<td>").html(util.numberWithComma(sgItem.cnt));
							tr_o.append(td_o);
							_total += sgItem.cnt;
						}
						
					}
				}else{
					tr_o.append($("<td>").html("-"));
					tr_o.append($("<td>").html("-"));
				}
			}
			total_o.html(util.numberWithComma(_total));
		},
		//광고주별 차트 그리기
		drawChartTable: function(list){
			let chart_o = $("#chartTableList").html("");
			
			for(let item of list){
				
				let section_o = $("<section>").addClass("bar-graph bar-graph-horizontal notMedia-cate");
					chart_o.append(section_o);
				{
					// 광고주명
					let media_o = $("<div>").addClass("media").html("<p>" + item.company_name + "</p>");
					section_o.append(media_o);
				}
				
				let sgList = item.sg_list;
				if(sgList.length >0){
					let wrap_o = $("<div>").addClass("m_cate_wrap");
					section_o.append(wrap_o);
					
					{
						let allBarWrap_o = $("<div>").addClass("all_bar_wrap");
						wrap_o.append(allBarWrap_o);
						
						
						if(sgList){
							for(let sgItem of sgList){
								let barWrap_o = $("<div>").addClass("bar_wrap");
								allBarWrap_o.append(barWrap_o);
								
								let product_o = $("<div>").addClass("product").html(sgItem.sg_name);
								barWrap_o.append(product_o);
								
								let figure_o = $("<span>").addClass("figure").html(util.numberWithComma(sgItem.cnt));
								barWrap_o.append(figure_o);
								
								let div_o = $("<div>");
								barWrap_o.append(div_o);

								if(sgItem.cnt >0){
									let percentage =0;
									//chart 백분율
									if(_total != 0){
										percentage = (sgItem.cnt/_total)*100;
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
				}else{
					//캠페인이 없을때
					let cate_not = $("<div>").addClass("m_cate_not").html("광고가 없습니다.");
					section_o.append(cate_not);
				}
			}
		}
	}
	
	
	return {
		init,
	}
	
	
})();