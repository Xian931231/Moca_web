const supplyProductDeviceList = (function () {
	
	// 해당 페이지 초기화 함수
	function init(){
		_list.getList();
		_evInit();
	}
	
	// 이벤트 초기화 
	function _evInit() {
		let evo = $("[data-src='list'][data-act]").off();
		evo.on("click keyup", function(ev){
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
			if(act_v == "clickSearch") {
				event.clickSearch();
			} else if(act_v == "clickStatusSearch") {
				event.clickStatusSearch(evo);
			} else if(act_v == "clickStatusChange") {
				event.clickStatusChange(evo);
			} else if(act_v == "clickStatusSave") {
				event.clickStatusSave();
			}
		} else if(type_v == "keyup") {
			if(act_v == "inputSearch") {
				if(ev.keyCode === 13) {
					_list.getList();
				}
			}
		}
	}
	
	// 기준 시간
	function _setStandardTime() {
		let now = moment().format("YYYY-MM-DD HH:mm분 기준");
		$("#standardTime").html(now);
	}
	
	// 이벤트
	let _event = {
		// 검색
		clickSearch: function() {
			_list.getList();
		},
		
		// 상태 검색
		clickStatusSearch: function(evo) {
			$("#searchValue").attr({"data-status": evo.attr("data-status")});
			evo.addClass("active");
			evo.siblings("button").removeClass("active");
			
			_list.getList();
		},
		
		// 상태 클릭
		clickStatusChange: function(evo) {
			$("#statusNotes").attr({"data-device-id": evo.attr("data-device-id")});
			$("#deviceStatus").val(evo.attr("data-status"));
			$("#deviceStatus").selectpicker("refresh");
			$("#statusNotes").val(util.unescapeData(evo.attr("data-notes")));
			$("#statusChangeModal").modal("show");			
		},
		
		// 상태 변경 저장
		clickStatusSave: function() {
			$("#deviceStatus").selectpicker("refresh");
		
			let note_o =  $("#statusNotes");
			let note_v =  note_o.val();
			if(util.valNullChk(note_v)) {
				customModal.alert({
					content: "변경 사유가 입력되지 않았습니다."
				});
				return;
			}		
			
			let url_v = "/device/modify/status";
			
			let data_v = {
				"ssp_device_id": note_o.attr("data-device-id"),
				"status": $("#deviceStatus option:selected").val(),
				"notes": note_v		
			}
			
			comm.send(url_v, data_v, "POST", function(resp) {
				if(resp) {
					_list.getList();
					$("#statusChangeModal").modal("hide");
				}
			});
		},
	}
	
	// 리스트
	let _list = {
		getList: function(curPage = 1) {
			_setStandardTime();
			$("#searchStatus").selectpicker("refresh");
			
			let url_v = "/supply/device/list";
			
			let data_v = {
				"search_type": $("#searchStatus option:selected").val(),
				"status": $("#searchValue").attr("data-status"),
				"search_value": $("#searchValue").val()
			};
			
			let pageOption = {
				"limit": 20
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
					dev.log(resp);
					_list.drawList(resp.list);
					page_o.drawPage(resp.tot_cnt);
					_evInit();
				}
			});
		},
		
		drawList: function(list) {
			let tbody_o = $("#tableList").empty();
			let motorCnt = 0;
			
			if(list && list.length > 0) {
				for(let motor of list) {
					if(motor.device_list && motor.device_list.length > 0) {
						for(let i = 0; i < motor.device_list.length; i++) {
							let item = motor.device_list[i];
							
							let tr_o = $("<tr>");
							tbody_o.append(tr_o);
		
							if(i == 0) {
								let td_o = $("<td>").attr({
									"rowspan": motor.device_list.length,
									"class": "locaGroup"
								}).html(item.car_number);
								if(motor.motor_id == 0) {
									td_o.html("-");
								}
								tr_o.append(td_o);
							}
							{
								let td_o = $("<td>").html(item.product_name);
								tr_o.append(td_o);
							}
							{
								let td_o = $("<td>").html(item.serial_number);
								tr_o.append(td_o);
							}
							{
								let td_o = $("<td>").html(item.screen_resolution);
								tr_o.append(td_o);
							}
							{
								let td_o = $("<td>").html(item.slot_cnt);
								tr_o.append(td_o);
							}
							{
								let td_o = $("<td>").html(item.device_insert_date);
								tr_o.append(td_o);
							}
							{
								let td_o = $("<td>");
								tr_o.append(td_o);
								
								let status = item.status;
								let button_o = $("<button>").attr({
									"type": "button",
									"class": "btn stateBox state-ongoing",
									"data-src": "list",
									"data-act": "clickStatusChange",
									"data-status": item.status,
									"data-notes": item.notes,
									"data-device-id": item.device_id
								});
								if(status == "Y") {
									button_o.html("정상");
								} else if(status == "R") {
									button_o.html("수리중");
								} else if(status == "D") {
									button_o.html("폐기");
								}
								td_o.append(button_o);
							}
						}
					} else {
						motorCnt++;
					}
					
					if(motorCnt == list.length) {
						let div_o = $("<div>").addClass("notnotnot").html("장비가 없습니다.");
						tbody_o.append(div_o);
						
						for(let i = 0; i < 11; i++) {
							let tr_o = $("<tr>");
							tbody_o.append(tr_o);
							
							for(let j = 0; j < 7; j++) {
								let td_o = $("<td>");
								tr_o.append(td_o);
							}
						}
					}
				}
			} else {
				let div_o = $("<div>").addClass("notnotnot").html("장비가 없습니다.");
				tbody_o.append(div_o);
				
				for(let i = 0; i < 11; i++) {
					let tr_o = $("<tr>");
					tbody_o.append(tr_o);
					
					for(let j = 0; j < 7; j++) {
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