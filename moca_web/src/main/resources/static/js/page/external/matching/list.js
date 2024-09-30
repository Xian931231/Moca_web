const matchingList = (function () {
	
	let _sessionParam = util.getSessionParam("external", "matching");
	
	// 해당 페이지 초기화 함수
	function init() {
		return _data.getSupplyList()
			.then(() => _data.getCategoryList())
			.then(() => _data.getMotorPositionList())
			.then(() => _list.getList())
			.then(() => {
				_initAddMotorPositionModal();
				_evInit();
			});
	}
	
	// 이벤트 초기화 
	function _evInit() {
		let evo = $("[data-src='matchingList'][data-act]").off();
		evo.on("click change", function(ev){
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
			if(act_v == "clickInspectionModal") {
				event.clickInspectionModal(evo);
			} else if(act_v == "clickMatchingInspection") {
				event.clickMatchingInspection(evo);
			}
		} else if(type_v == "change") {
			if(act_v == "changeSupply") {
				comm.sendPromise(() => {
					return _data.getCategoryList()
						.then(() => _data.getMotorPositionList())
						.then(() => _list.getList());
				});
			} else if(act_v == "changeCategory") {
				comm.sendPromise(() => {
					return _data.getMotorPositionList()
						.then(() => _list.getList());
				});
			} else if(act_v == "changeMotorPosition") {
				_list.getList();
			}
		}
	}
	
	const _data = {
		// 매체 목록
		getSupplyList: function() {
			return new Promise((resolve) => {
				let url_v = "/external/product/supply/list";
				
				let data_v = {
					is_exist_motor_position: "Y",	
				}
				comm.send(url_v, data_v, "POST", function(resp) {
					if(resp.result && resp.list && resp.list.length > 0) {
						let list = resp.list.map(o => {
							return {value: o.member_id, name: o.company_name}
						});
						
						let supplyId;
						if(_sessionParam && _sessionParam.supply_id) {
							supplyId = _sessionParam.supply_id;
						}
						_data.setSelectBoxOption("supplySb", list, supplyId);
						//_data.getCategoryList();
						resolve();
					}
				});
			});
		},
		
		// 분류 목록
		getCategoryList: function() {
			return new Promise((resolve) => {
				let url_v = "/external/product/category/list";
				
				let data_v = {
					is_exist_motor_position	: "Y",	
					member_id				: $("#supplySb").val(),
				}
				comm.send(url_v, data_v, "POST", function(resp) {
					if(resp.result && resp.list && resp.list.length > 0) {
						let list = resp.list.map(o => {
							return {value: o.category_id, name: o.category_name}
						});
						
						let categoryId;
						if(_sessionParam && _sessionParam.category_id) {
							categoryId = _sessionParam.category_id;
						}
						
						_data.setSelectBoxOption("categorySb", list, categoryId);
						//_data.getMotorPositionList();
						resolve();
					}
				});
			});
		},
		
		// 게재 위치 목록
		getMotorPositionList: function() {
			return new Promise((resolve) => {
				let url_v = "/external/motor/list";
				
				let data_v = {
					is_exist_motor	:	"Y",
					category_id		: $("#categorySb").val(),
				}
				comm.send(url_v, data_v, "POST", function(resp) {
					if(resp.result && resp.list && resp.list.length > 0) {
						let list = resp.list.map(o => {
							return {value: o.motor_position_id, name: o.position_name}
						});
						
						let motorPositionId;
						if(_sessionParam && _sessionParam.ssp_motor_position_id) {
							motorPositionId = _sessionParam.ssp_motor_position_id;
						}
						_data.setSelectBoxOption("motorPositionSb", list, motorPositionId);
						//_list.getList();
						resolve();
					}
				});
			});
		},
		
		// 매칭 정보 조회
		getMatchingDetail: function(motorId, callback) {
			let url_v = "/external/matching/detail";
			
			let data_v = {
				motor_id: motorId
			}
			comm.send(url_v, data_v, "POST", function(resp) {
				if(resp.result) {
					_data.setMatchingDetail(resp.data);
					if(callback && typeof(callback) == "function") {
						callback();
					}
				}
			});
		},
		
		// 셀렉트 박스 옵션 설정
		setSelectBoxOption: function(targetId, list, selectedValue) {
			if(list) {
				let select_o = $("#" + targetId).empty();
				select_o.selectpicker("destroy");
				
				for(let item of list) {
					let option_o = $("<option>").val(item.value).text(item.name);
					select_o.append(option_o);
				}
				
				if(selectedValue) {
					select_o.selectpicker("val", selectedValue);
				}
				
				select_o.selectpicker("refresh");
			}
		},
		
		// 매칭 정보 
		setMatchingDetail: function(data) {
			// 매칭 기기 정보
			let devices = _listObjectValueToString(data.matching_device_list, "serial_number");
			$("#matchingDevices").text(devices);
			
			// 매칭 측정 장비 정보
			let sensorDevices = _listObjectValueToString(data.matching_sensor_device_list, "serial_number");
			$("#matchingSensorDevices").text(sensorDevices);
			
			// 비고
			$("#notes").val(data.notes);
		},
	}
	
	// 리스트 담당
	const _list = {
		// 게재 위치 리스트
		getList: function() {
			return new Promise((resolve) => {
				let url_v = "/external/motor/id/list";
				let motorPositionId = $("#motorPositionSb").val();
				
				let data_v = {
					motor_position_id: motorPositionId,
				}
				comm.send(url_v, data_v, "POST", function(resp) {
					if(resp.result) {
						_list.drawList(resp.list);
						_evInit();
						_initSelectBox();
						resolve();
					}
				});
				util.removeSessionAllParam();
				_sessionParam = null;
			});
		},
	
		drawList: function(list) { 
			let body_o = $("#listBody").empty();

			if(list) {
				for(let item of list) {
					let tr_o = $("<tr>");
					body_o.append(tr_o);
					{
						// 구분 아이디
						let td_o = $("<td>");
						let a_o = $("<a>").attr("href", "/external/matching/add?motor_id=" + item.motor_id)
							.text(item.car_number);
						
						td_o.append(a_o);
						tr_o.append(td_o);
					}
					{
						// 아이피
						let td_o = $("<td>").text(item.ip_address);
						tr_o.append(td_o);
					}
					{
	                    // 검수 버튼
	                    let td_o = $("<td>");
	                    let btn_o = $("<button>").addClass("btn")
	                    	.addClass(item.status == 0 ? "btn-approved" : item.status == 1 ? "btn-sm" : "btn-refusal")
	                    	.attr({
	                    		"data-src" 		: "matchingList",
	                    		"data-act"		: "clickInspectionModal",
	                    		"data-motor-id" : item.motor_id
	                    	}).text(item.status == 0 ? "대기" : item.status == 1 ? "완료" : "오류");

	                    td_o.append(btn_o);
	                    tr_o.append(td_o);
					}
				}
			}
		}
	}
	
	// 이벤트 담당
	const _event = {
		// 매칭 검수 모달
		clickInspectionModal: function(evo) {
			let motorId = evo.attr("data-motor-id");
			
			_data.getMatchingDetail(motorId, function(){
				$("#motorId").val(motorId);
				$("#matchingInspectionModal").modal("show");
			});
		},
	
		// 매칭 검수 
		clickMatchingInspection: function(evo) {
			let motorId = $("#motorId").val();
			let notes = $("#notes").val();
			let status = evo.attr("data-inpection-status");
			
			let url_v = "/external/matching/status/modify";
			
			let data_v = {
				motor_id: motorId,
				notes: notes,
				status: status,
			}
			
			comm.send(url_v, data_v, "POST", function(resp) {
				if(resp.result) {
					_list.getList();
				}
			});
		}
	}
	
	// 게재 위치 모달 초기화
	function _initAddMotorPositionModal() {
		$('#matchingInspectionModal').on('hidden.bs.modal', function (e) {
			$("#notes").val("");
			$("#motorId").val("");
			$("#matchingDeviceList").val("");
			$("#matchingSensorDeviceList").val("");
		});
	}
	
	// 셀렉트박스 초기화
	function _initSelectBox() { 
		$("select").each(function(i, o) {
			$(o).selectpicker("destroy");
			$(o).selectpicker();
		});
	} 
	
	// 리스트 안에 오브젝트의 특정 값을 스트링으로
	function _listObjectValueToString(list, key, separator = ", ") {
		if(!list || list.length <= 0) {
			return "";
		}
		return list.map(o => o[key]).join(separator);
	}
	
	return {
		init
	}
	
})();