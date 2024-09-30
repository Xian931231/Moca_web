const modifyHistoryList = (function () {
	var _customDatePicker = null;
	
	function init() {
		_setDatePicker();

		_list.getList();
		_evInit();
	}
	
	function _evInit() {
		let evo = $("[data-src='modifyHistoryList'][data-act]").off();
		
		evo.on("click change keyup", function(ev){
			_action(ev);
		});
	}
	// 이벤트 분기
	function _action(ev) {
		let evo = $(ev.currentTarget);
		let act_v = evo.attr("data-act");
		let type_v = ev.type;
		let event = _event;
		
		if(type_v == "click") {
			if(act_v == "search") {
				_list.getList();
			}else if(act_v == "movePage"){
				event.movePage(evo);
			}
			
		}
	}
	//이벤트
	let _event = {
		//상세 페이지 이동 
		movePage: function(evo) {
			let id = evo.attr("data-id");
			let kind = evo.attr("data-kind");
			
			//캠페인 
			if(kind == "MHK002"){
				location.href = "/demand/campaign/sg/list?id="+id;
			//광고
			}else if(kind == "MHK001"){
				location.href = "/demand/campaign/sg/detail?id="+id;
			}
		}
		
	}
	//리스트
	let _list = {
		//리스트 api
		getList: function(curPage = 1) {
			let url_v = "/member/demand/modify/history/list";
			
			let strDt = $("#sDate").val();
			let endDt = $("#eDate").val();
			
			if(util.getDiffDate(strDt, endDt, "months") >= 3) {
				// TODO
//				customModal.alert({
//					content: "날짜는 최대 3개월까지만 조회 가능합니다."
//				});
				return;
			}
			
			
			let data_v = {
				str_dt: strDt
				, end_dt: endDt 
			};
			
			let page_o = $("#listPage").customPaging(null, function(_curPage){
				_list.getList(_curPage);
			});
			
			let pageParam = page_o.getParam(curPage);
			
			if(pageParam) {
				data_v.offset = pageParam.offset;
				data_v.limit = pageParam.limit;
			}
			
			comm.send(url_v, data_v, "POST", function(resp){
				if(resp.result) {
					_list.drawList(resp.list);
					page_o.drawPage(resp.tot_cnt);
					_evInit();
				}
			});
						
		},
		// 리스트 그리기
		drawList: function(list) {
			let body_o = $("#listBody").html("");
			
			for(let item of list) {
				let tr_o = $("<tr>");
				body_o.append(tr_o);
				
				//변경 내용
				{
					let td_o = $("<td>");
					td_o.text(item.kind_name+ " [");
					if(item.kind != "MHK003" && item.modify_type != "MHS005") {
						let a_o = $("<a>").attr({
							"href": "javascript:;",
							"data-src": "modifyHistoryList",
							"data-act": "movePage",
							"data-kind": item.kind,
							"data-id":item.modify_id
						});
						
						a_o.html(item.sg_name);
						td_o.append(a_o);	
					}else {
						td_o.append(item.sg_name);
					}
					
					td_o.append("] "+item.type_name);
					tr_o.append(td_o);
				}
				//변경 일시
				{
					let td_o = $("<td>");
					td_o.html(item.update_date);
					tr_o.append(td_o);
				}
				//변경자 ip
				{
					let td_o = $("<td>");
					td_o.html(item.masked_ip);
					tr_o.append(td_o);
				}
			}
		}
	}
	
	//datePicker 설정
	function _setDatePicker(){
		customDatePicker.init("sDate").datepicker("setDate",moment().subtract(7, "days").format("YYYY-MM-DD"));
		customDatePicker.init("eDate").datepicker("setDate",moment().format("YYYY-MM-DD"));
	}
	
	return {
		init
	}
	
})();