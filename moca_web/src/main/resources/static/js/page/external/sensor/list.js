const sensorList = (function () {
	
	let _sessionParam = util.getSessionParam("external", "sensor");
	
	// 해당 페이지 초기화 함수
	function init(){
		return _data.getSupplyList()
			.then(() => _data.getCategoryList())
			.then(() => _list.getList())
			.then(() => _evInit());
	}
	
	// 이벤트 초기화 
	function _evInit() {
		let evo = $("[data-src='sensorList'][data-act]").off();
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
			if(act_v == "clickAddSensor") {
				event.clickAddSensor();
			} else if(act_v == "clickAddSerialNumPage") {
				event.clickAddSerialNumPage(evo);
			}
		} else if(type_v == "change") {
			if(act_v == "changeSupply") {
				comm.sendPromise(() => {
					return _data.getCategoryList()
						.then(() => _list.getList());
				})
				
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
	}
	
	// 리스트 담당
	const _list = {
		// 측정 장비 리스트
		getList: function() {
			return new Promise((resolve) => {
				let url_v = "/external/sensor/list";
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
						// 측정장비 이름
						let td_o = $("<td>");
						let a_o = $("<a>").attr("href", "/external/sensor/modify?sensor_id=" + item.sensor_id)
							.text(item.sensor_name);
						td_o.append(a_o);
						tr_o.append(td_o);
					}
					{
						// 시리얼 넘버 관리 버튼
						let td_o = $("<td>");
						let btn_o = $("<button>").addClass("btn btn-approved")
							.attr({
								"data-src" 			: "sensorList",
								"data-act" 			: "clickAddSerialNumPage",
								"data-sensor-id"	: item.sensor_id,
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
		// 장비 등록
		clickAddSensor: function() {
			let categoryId = $("#categorySb").val();

			if(!categoryId) {
				customModal.alert({
					content: "분류를 선택해주세요."
				});
				return false;
			}
			location.href = "/external/sensor/add?category_id=" + categoryId;
		},
	
		// 시리얼 번호 등록 페이지
		clickAddSerialNumPage: function(evo) {
			let sensorId = evo.attr("data-sensor-id");
			location.href = "/external/sensor/device/add?sensor_id=" + sensorId;
		}
	}
	
	// 셀렉트박스 초기화
	function _initSelectBox() { 
		$("select").each(function(i, o) {
			$(o).selectpicker("destroy");
			$(o).selectpicker();
		});
	} 
	
	return {
		init
	}
	
})();