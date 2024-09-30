const matchingAdd = (function () {
	
	let _urlParam = util.getUrlParam();
	
	let _motorDetail = null;
	
	// 모든 메뉴(상품, 측정장비) 리스트 
	// 메뉴에 속한 디바이스 포함 
	let _allMenuList = [];
	
	// 매칭 추가되는 디바이스
	let _addDeviceList = [];
	
	// 매칭 해제되는 디바이스
	let _removeDeviceList = [];
	
	// 매칭 추가되는 측정 장비
	let _addSensorDeviceList = [];
	
	// 매칭 해제되는 측정 장비
	let _removeSensorDeviceList = [];
	
	// 검색어
	let _searchValue = "";
	
	// 필터 처리된 셀렉트 메뉴
	const _selectFilter = {
		product: [], 
		sensor: [],
	}
	
	// 기기 필터
	const _deviceFilter = {
		device: [],
		sensor: [],
	}
	
	// 해당 페이지 초기화 함수
	function init() {
		if(!_urlParam || !_urlParam.motor_id) {
			location.href = "/external/matching/list";
		} else {
			return _detail.getDetail()
				.then(() => _data.getProductList())
				.then(() => _evInit());
		}
	}
	
	// 이벤트 초기화 
	function _evInit() {
		let evo = $("[data-src='matchingAdd'][data-act]").off();
		evo.on("click change keyup", function(ev) {
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
			if(act_v == "clickProductTab") {
				event.clickProductTab();
			} else if(act_v == "clickSensorDeviceTab") {
				event.clickSensorDeviceTab();
			} else if(act_v == "clickAddDevice") {
				event.clickAddDevice(evo);
			} else if(act_v == "clickRemoveMatching") {
				event.clickRemoveMatching(evo);
			} else if(act_v == "clickCancel") {
				event.clickCancel();
			} else if(act_v == "clickAddMatching") {
				event.clickAddMatching();
			} else if(act_v == "clickSearch") {
				event.clickSearch(evo);
			}
		} else if(type_v == "change") {
			if(act_v == "changeProduct") {
				event.changeProduct();
			} else if(act_v == "changeSensor") {
				event.changeSensor();
			}
		}
	}
	
	const _data = {
		// 상품 목록
		getProductList: function() {
			return new Promise((resolve) => {
				let url_v = "/external/product/list";
				
				let data_v = {
					is_exist_not_matching_device : "Y",
					is_with_device			: "Y",
					category_id				: _motorDetail.category_id,
					except_motor_id 		: _motorDetail.motor_id,
				}
				comm.send(url_v, data_v, "POST", function(resp) {
					if(resp.result) {
						_selectFilter.product = [];
						_allMenuList = resp.list;
						_data.setSelectBox("device");
						resolve();
					}
				});
			});
		},
		
		// 측정 장비명 목록
		getSensorList: function() {
			let url_v = "/external/sensor/list";
			
			let data_v = {
				is_exist_not_matching_device : "Y",
				is_with_device			: "Y",
				category_id				: _motorDetail.category_id,
				except_motor_id 		: _motorDetail.motor_id,
			}
			comm.send(url_v, data_v, "POST", function(resp) {
				if(resp.result) {
					_selectFilter.sensor = [];
					_allMenuList = resp.list;
					_data.setSelectBox("sensor");
				}
			});
		},
		
		// 셀렉트박스 옵션 설정
		setSelectBoxOption: function(targetId, list, selectedValue) {
			if(targetId && list) {
				let select_o = $("#" + targetId).empty();
				select_o.selectpicker("destroy");
				
				for(let item of list) {
					let filter = targetId == "productSb" ? _selectFilter.product : _selectFilter.sensor;
					
					// 필터 처리
					if(!filter.includes(Number(item.value))) {
						let option_o = $("<option>").val(item.value).text(item.name);
						select_o.append(option_o);
					}
				}
				if(selectedValue) {
					select_o.val(selectedValue);
				}
				select_o.selectpicker("refresh");
			}
		},
		
		// 셀렉트 박스 설정
		setSelectBox: function(type, selectedVal = null) {
			let typeInfo = _getTypeInfo(type);
			
			let menuIdKey = typeInfo.menu_id_key;
			let menuNameKey = typeInfo.menu_name_key;
			let selectId = typeInfo.select_id;
			
			let list = _allMenuList.map(o => {
				return {value: o[menuIdKey], name: o[menuNameKey]}
			});
			
			_addSelectFilter(type);
			
			_data.setSelectBoxOption(selectId, list, selectedVal);
			
			_data.setDeviceList(type);
		},
		
		
		// 디바이스 목록 
		setDeviceList: function(type) {
			let typeInfo = _getTypeInfo(type);
			
			let menuIdKey = typeInfo.menu_id_key;
			let selectId = typeInfo.select_id;
			let listBodyId = typeInfo.list_body_id;
			
			$("#" + listBodyId).empty();
			
			let selectValue = $("#" + selectId).val();
			
			let deviceList = [];
			
			if(_allMenuList.filter(menu => menu[menuIdKey] == selectValue)[0]) {
				deviceList = _allMenuList.filter(menu => menu[menuIdKey] == selectValue)[0].device_list;
			}
			
			let filterList = [];
			
			$.each(deviceList, function(i, o) {
				let isFilter = false;
				
				if(type == "device") {
					o.id = o.device_id;

					// 이미 추가된 디바이스 필터링
					if(_deviceFilter.device.includes(Number(o.id))) {
						isFilter = true;
					}
				} else {
					o.id = o.sensor_device_id;
					
					// 이미 추가된 측정장비 필터링
					if(_deviceFilter.sensor.includes(Number(o.id))) {
						isFilter = true;
					}
				}
				
				// 필터 확인
				if(!isFilter) {
					// 검색어 적용
					if(o.serial_number.includes(_searchValue)){
						// 디바이스 폼 추가
						o.type = type;
						filterList.push(o);
					}
				}
			});
			
			_addDeviceForm(listBodyId, filterList);
		},
		
		// 등록 데이터
		getSubmitData: function() {
			let data_v = {};
			
			// 게재 위치 구분 아이디
			data_v.motor_id = _motorDetail.motor_id;
			
			// 매칭되는 디바이스 리스트
			data_v.add_device_id_list = _addDeviceList;
			
			// 매칭 해재되는 디바이스 리스트
			data_v.remove_device_id_list = _removeDeviceList;
			
			// 매칭 되는 측정장비 리스트
			data_v.add_sensor_device_id_list = _addSensorDeviceList;
			
			// 매칭 해제 되는 측정장비 리스트
			data_v.remove_sensor_device_id_list = _removeSensorDeviceList;
			
			return data_v;
		}
	}
	
	// 상세 담당
	const _detail = {
		// 게재 위치 구분 아이디 상세
		getDetail: function() {
			return new Promise((resolve) => {
				let url_v = "/external/matching/detail";
				let motorPositionId = _urlParam.motor_id;
				
				let data_v = {
					motor_id: motorPositionId,
				}
				comm.send(url_v, data_v, "POST", function(resp) {
					if(resp.result) {
						_motorDetail = resp.data;
						_detail.drawDetail(resp.data);
						
						let sessionParam = {
							supply_id: _motorDetail.supply_id,
							category_id: _motorDetail.category_id,
							ssp_motor_position_id: _motorDetail.ssp_motor_position_id,
						};
						// 세션 파라미터 저장
						util.setSessionParam("external", "matching", sessionParam);
						resolve();
					} else {
						location.href = "/external/matching/list";
					}
				});
			});
		},
	
		drawDetail: function(data) { 
			if(data) {
				// 게재 위치 구분 아이디
				$("#carNumber").text(data.car_number + " 매칭 관리");
				
				// 매칭 기기 정보
				data.matching_device_list.forEach(o => {
					o.type = "device";
					o.id = o.device_id;
					o.motor_id = data.motor_id;
					
					_deviceFilter.device.push(Number(o.id));
					
					_addMatchingBtn("deviceList", o);
				});
			
				// 매칭 측정 장비 정보
				data.matching_sensor_device_list.forEach(o => {
					o.type = "sensor";
					o.id = o.sensor_device_id;
					o.motor_id = data.motor_id;

					_deviceFilter.sensor.push(Number(o.id));

					_addMatchingBtn("sensorDeviceList", o);
				});
			}
		}
	}
	
	// 이벤트 담당
	const _event = {
		// 작성 취소
		clickCancel: function() {
			location.href = "/external/matching/list";
		},
			
		// 상품 탭 클릭
		clickProductTab: function() {
			_searchValue = "";
			_data.getProductList();
		}, 
		
		// 측정 장비 탭 클릭
		clickSensorDeviceTab: function() {
			_searchValue = "";
			_data.getSensorList();
		},
		
		// 상품 셀렉트박스 변경 
		changeProduct: function() {
			_data.setDeviceList("device");
		},
		
		// 측정장비 셀렉트박스 변경
		changeSensor: function() {
			_data.setDeviceList("sensor");
		},
		
		// 기기/위치 매칭 추가
		clickAddDevice: function(evo) {
			let id = evo.attr("data-id");
			let type = evo.attr("data-type");
			let serial = evo.find("div").text();
			
			// 이미 같은 메뉴의 기기가 추가 되었는지 확인
			if(_isAlreayAddedMenu(type)) {
				let menu = type == "device" ? "상품" : "장비";
				
				customModal.alert({
					content: "동일 " +menu+ "에서는 1개의 기기만 선택 가능합니다."
				});
				return;
			}
			
			// 클릭한 기기 리스트에서 삭제
			evo.remove();
			
			let appendId = type == "device" ? "deviceList" : "sensorDeviceList";
			
			let data_v = {
				id	: id, 
				type: type, 
				serial_number: serial	
			}
			
			// 매칭 버튼 추가
			_addMatchingBtn(appendId, data_v);
			
			_addMatchingDeviceList(data_v);
		},
		
		// 기기/위치 매칭 삭제
		clickRemoveMatching: function(evo) {
			let parent_o = evo.parent();
			let id = Number($(parent_o).attr("data-id"));
			let motorId = $(parent_o).attr("data-motor-id");
			let type = $(parent_o).attr("data-type");
			let typeInfo = _getTypeInfo(type);
			
			let addDeviceList = typeInfo.add_device_list;
			let deviceFilter = typeInfo.device_filter;
			
			// motorId가 있으면 현재 매칭중인 디바이스이므로 삭제 리스트에 추가
			if(motorId) {
				typeInfo.remove_device_list.push(id);
			}
			
			// 기기 추가 삭제 
			addDeviceList = addDeviceList.filter(addId => addId != id);
			
			// 기기 필터 삭제
			deviceFilter = deviceFilter.filter(addId => addId != id);
			
			
			if(type == "device") {
				_addDeviceList = addDeviceList;
				_deviceFilter.device = deviceFilter;
			} else {
				_addSensorDeviceList = addDeviceList;
				_deviceFilter.sensor = deviceFilter;
			}
			
			// 셀렉트 메뉴 필터링
			_removeSelectFilter(type, id);
			
			$(parent_o).remove();
		}, 
		
		// 매칭 등록
		clickAddMatching: function() {
			let data_v = _data.getSubmitData();
			
			let url_v = "/external/matching/add";
			
			comm.send(url_v, data_v, "POST", function(resp) {
				if(resp.result) {
					location.href = "/external/matching/list";
				} else {
					customModal.alert({
						content: resp.msg,
					});
				}
			});
		},
		
		// 검색
		clickSearch: function(evo) {
			let type = evo.attr("data-type");
			
			let searchId = type == "device" ? "deviceSearchValue" : "sensorSearchValue";
			
			_searchValue = $("#" + searchId).val().trim();
			
			_data.setDeviceList(type);
		},
	}
	
	// 기기 목록 추가
	function _addDeviceForm(appendId, list = new Array(1)) {
		if(!appendId) {
			return;
		}
		let documentFragment =  $(document.createDocumentFragment());
		
		for(let data of list) {
			documentFragment.append(_createDeviceForm(data));
		}
		$("#" + appendId).append(documentFragment);
		
		_evInit();
      	_initSelectBox();
	}
	
	// 기기 목록 추가
	function _createDeviceForm(data) {
		if(!data) {
			return;
		}
      	
      	let tr_o = $("<tr>").attr({
      		"data-src"	: "matchingAdd",
      		"data-act"	: "clickAddDevice",
      		"data-id"	: data.id,
      		"data-type"	: data.type, 
      	});

      	{
      		let td_o = $("<td>");
      		let div_o = $("<div>").text(data.serial_number);
      		td_o.append(div_o);
      		tr_o.append(td_o);
      	}
      	return tr_o;
	}

	// 매칭 버튼 추가
	function _addMatchingBtn(appendId, data) {
		if(!appendId || !data) {
			return;
		}
		
		let btn_o = $("<button>").addClass("btn btn-def")
			.attr({
				"type"			: "button",
				"data-id"		: data.id,
				"data-type"		: data.type,
				"data-motor-id"	: data.motor_id ? data.motor_id : null,
			}).text(data.serial_number);
		
		let i_o = $("<i>").attr({
			"data-src": "matchingAdd",
			"data-act": "clickRemoveMatching",
		});
		
		btn_o.append(i_o);
		
		$("#" + appendId).append(btn_o, " ");    
		
		_addSelectFilter(data.type);
		
		_evInit();
		
		_initSelectBox();
	}
	
	// 이미 추가된 메뉴의 기기인지 체크
	// 메뉴( 상품, 측정장비 )당 등록할 수 있는 기기는 하나
	function _isAlreayAddedMenu(type){
		let typeInfo = _getTypeInfo(type);
		
		let menuIdKey = typeInfo.menu_id_key;
		let deviceIdKey = typeInfo.device_id_key;
		let deviceFilter = typeInfo.device_filter;
		let selectId = typeInfo.select_id;
		
		let menuVal = Number($("#" + selectId).val());
		
		let menuDeviceList = _allMenuList.filter(menu => menu[menuIdKey] == menuVal)[0].device_list;
		
		let isAddedMunu = false;
		
		menuDeviceList.forEach(device => {
			if(deviceFilter.includes(device[deviceIdKey])){
				isAddedMunu = true;
				return false;
			}
		});
		return isAddedMunu;
	}
	
	// 매칭 기기 정보 추가
	function _addMatchingDeviceList(data) {
		if(!data) {
			return false;
		}
		
		let id = Number(data.id);
		let type = data.type;
		
		let typeInfo = _getTypeInfo(type);
		
		typeInfo.add_device_list.push(id);

		typeInfo.device_filter.push(id);
		
		_addSelectFilter(type);
	}
	
	// 셀렉트 박스 메뉴 필터 추가
	function _addSelectFilter(type) {
		if(!type) {
			return false;
		}
		
		let typeInfo = _getTypeInfo(type);
		
		let menuIdKey = typeInfo.menu_id_key;
		let deviceIdKey = typeInfo.device_id_key;
		let deviceFilter = typeInfo.device_filter;
		
		// 선택된 셀렉트 옵션의 하위 메뉴가 모두 추가 되면 해당 셀렉트 옵션은 필터 처리
		_allMenuList.forEach(menu => {
			let deviceList = menu.device_list;
			
			let allFilter = true;
			
			deviceList.forEach(device => {
				let id = device[deviceIdKey];
				
				if(!deviceFilter.includes(id)) {
					allFilter = false;
					return false;
				}
			});

			// 모두 필터링 된 디바이스의 메뉴 필터링 추가
			if(allFilter) {
				let menuId = Number(menu[menuIdKey]);
				
				let menuFilter = typeInfo.menu_filter;

				if(!menuFilter.includes(menuId)) {
					menuFilter.push(menuId);
					
					_data.setSelectBox(type);
				}
			}
		});
	}
	
	// 셀렉트 박스 필터 제거
	function _removeSelectFilter(type, id) {
		let typeInfo = _getTypeInfo(type);
		
		let menuIdKey = typeInfo.menu_id_key;
		let deviceIdKey = typeInfo.device_id_key;
		let menuFilter = typeInfo.menu_filter;
		let selectId = typeInfo.select_id;

		let selectVal = $("#" + selectId).val();
		
		_allMenuList.forEach(menu => {
			let menuId = Number(menu[menuIdKey]);
			
			let deviceList = menu.device_list;
			
			deviceList.forEach(device => {
				let deviceId = device[deviceIdKey];
				
				// 삭제한 디바이스 아이디 상위 메뉴가 필터링 목록에 있으면 제거
				if(id == deviceId) {
					if(menuFilter.includes(menuId)) {
						menuFilter = menuFilter.filter(filterId => filterId != menuId);
						
						if(type == "device") {
							_selectFilter.product = menuFilter; 
						} else {
							_selectFilter.sensor = menuFilter;
						}
					}
					_data.setSelectBox(type, selectVal);
				}
			});
		});
	}
	
	// 셀렉트박스 초기화
	function _initSelectBox() { 
		$("select").each(function(i, o) {
			$(o).selectpicker("destroy");
			$(o).selectpicker();
		});
	}
	
	// 타입 정보 리턴
	function _getTypeInfo(type) {
		if(!type) {
			return false;
		}
		
		return info_v = {
			menu_id_key			: type == "device" ? "product_id" 			: "sensor_id",
			menu_name_key		: type == "device" ? "product_name" 		: "sensor_name",					
			device_id_key		: type == "device" ? "device_id" 			: "sensor_device_id",
			select_id			: type == "device" ? "productSb" 			: "sensorSb",		
			list_body_id 		: type == "device" ? "deviceListBody" 		: "sensorListBody",
			add_device_list		: type == "device" ? _addDeviceList 		: _addSensorDeviceList,
			remove_device_list	: type == "device" ? _removeDeviceList 		: _removeSensorDeviceList,
			menu_filter 		: type == "device" ? _selectFilter.product 	: _selectFilter.sensor,
			device_filter 		: type == "device" ? _deviceFilter.device 	: _deviceFilter.sensor,
		}
	}
	
	return {
		init
	}
	
})();