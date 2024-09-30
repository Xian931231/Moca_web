const motorPositionList = (function () {

	let _sessionParam = util.getSessionParam("external", "motor");
	
	// 해당 페이지 초기화 함수
	function init(){
		return	_data.getSupplyList()
			.then(() => _data.getCategoryList())
			.then(() => _list.getList())
			.then(() => {
				_initAddMotorPositionModal();
				_evInit();
			});
	}
	
	// 이벤트 초기화 
	function _evInit() {
		let evo = $("[data-src='motorPositionList'][data-act]").off();
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
			if(act_v == "clickAddMotorPosition") {
				event.clickAddMotorPosition();
			} else if(act_v == "clickAddMotorPositionIdPage") {
				event.clickAddMotorPositionIdPage(evo);
			} else if(act_v == "clickAddMotorPositionModal"){
				event.clickAddMotorPositionModal(evo);
			}
		} else if(type_v == "change") {
			if(act_v == "changeSupply") {
				comm.sendPromise(() => {
					return _data.getCategoryList()
						.then(() => _list.getList());
				});
			} else if(act_v == "changeCategory") {
				_list.getList();
			}
		}
	}
	
	const _data = {
		// 분류 목록
		getSupplyList: function() {
			return new Promise((resolve) => {
				let url_v = "/external/product/supply/list";
				
				let data_v = {
					is_exist_product : "Y",	
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
//						_data.getCategoryList();
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
					member_id			: $("#supplySb").val(),
					is_exist_product 	: "Y",
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
//						_list.getList();
						resolve();
					}
				});
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
		
		// 등록 데이터
		getSubmitData: function() {
			let data_v = {};
			
			// 분류 아이디
			data_v.category_id = $("#categorySb").val();
			
			// 게재 위치 명
			data_v.position_name = $("#positionName").val().trim();
			
			return data_v;
		}
	}
	
	// 리스트 담당
	const _list = {
		// 게재 위치 리스트
		getList: function() {
			return new Promise((resolve) => {
				let url_v = "/external/motor/list";
				let categoryId = $("#categorySb").val();
				
				let data_v = {
					category_id: categoryId ? categoryId : 0
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
						// 게재위치 이름
						let td_o = $("<td>").text(item.position_name);
						tr_o.append(td_o);
					}
					{
						// 구분 아이디 관리 버튼
						let td_o = $("<td>");
						let btn_o = $("<button>").addClass("btn btn-approved")
							.attr({
								"data-src" 					: "motorPositionList",
								"data-act" 					: "clickAddMotorPositionIdPage",
								"data-motor-position-id"	: item.motor_position_id,
							}).text("관리");
						td_o.append(btn_o);
						tr_o.append(td_o);
					}
				}
			}
		}
	}
	
	// 이벤트 담당
	const _event = {
		// 게재 위치 저장 
		clickAddMotorPosition: function() {
			let data_v = _data.getSubmitData();
			
			if(!_validateSubmitData(data_v)) {
				return;
			}
			let url_v = "/external/motor/add";
			
			comm.send(url_v, data_v, "POST", function(resp) {
				if(resp.result) {
					$("#motorPositionModal").modal("hide");
					_list.getList();
				} else if(resp.code == "9102") {
					customModal.alert({
						content: "중복되는 게재 위치명이 있습니다.",
					});
				}
			});
		},
	
		// 구분 아이디 저장 페이지
		clickAddMotorPositionIdPage: function(evo) {
			let motorPositionId = evo.attr("data-motor-position-id");
			location.href = "/external/motor/id/add?motor_position_id=" + motorPositionId;
		},
		
		// 게재 위치 등록 모달
		clickAddMotorPositionModal: function(evo) {
			$("#motorPositionModal").modal("show");
		}
	}
	
	// 게재 위치 모달 초기화
	function _initAddMotorPositionModal() {
		$('#motorPositionModal').on('hidden.bs.modal', function (e) {
			  $("#positionName").val("");
		});
	}
	
	// 셀렉트박스 초기화
	function _initSelectBox() { 
		$("select").each(function(i, o) {
			$(o).selectpicker("destroy");
			$(o).selectpicker();
		});
	} 
	
	// 등록 데이터 유효성 검사
	function _validateSubmitData(data) {
		let msg = "";
		
		if(!data.category_id) {
			msg = "분류를 선택해주세요.";
		} else if(!data.position_name) {
			msg = "입력되지 않은 정보가 있습니다.";
		}
		
		if(msg) {
			customModal.alert({
				content: msg,
			});
			return false;
		}
		return true;
	}
	
	return {
		init
	}
	
})();