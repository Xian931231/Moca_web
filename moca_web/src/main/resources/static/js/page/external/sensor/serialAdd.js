const serialAdd = (function () {
	
	const _urlParam = util.getUrlParam();
	
	// 삭제되는 측정장비 시리얼 번호 아이디
	let _removesensorDeviceIdList = [];
	
	// 해당 페이지 초기화 함수
	function init() {
		if(!_urlParam || !_urlParam.sensor_id) {
			location.href = "/external/sensor/list";
		} else {
			return Promise.all([
				_data.getSensor(),
				_list.getList()
			]).then(() => {
				_evInit();
				_initAddModelNameModal();
			});
		} 
	}
	
	// 이벤트 초기화 
	function _evInit() {
		let evo = $("[data-src='serialAdd'][data-act]").off();
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
			if(act_v == "clickAddSerialForm") {
				event.clickAddSerialForm();
			} else if(act_v == "clickBulkAddSerialNum") {
				event.clickBulkAddSerialNum();
			} else if(act_v == "clickSerialAddModal") {
				event.clickSerialAddModal();
			} else if(act_v == "clickRemoveSerialForm") {
				event.clickRemoveSerialForm(evo);
			} else if(act_v == "clickaddSensorDevice") {
				event.clickaddSensorDevice();
			} else if(act_v == "clickCancel") {
				event.clickCancel();
			} else if(act_v == "clickAddExcel") {
				event.clickAddExcel();
			}
		} else if(type_v == "change") {
			if(act_v == "changeFile") {
				event.changeFile(evo);
			}
		}
	}
	
	const _data = {
		// 상품 정보 
		getSensor: function() {
			return new Promise((resolve) => {
				let url_v = "/external/sensor/detail";

				let sensorId = _urlParam.sensor_id;
				
				let data_v = {
						sensor_id: sensorId,
				}
				comm.send(url_v, data_v, "POST", function(resp) {
					if(resp.result) {
						_data.setSensorInfo(resp.data);
						
						let sessionParam = {
							supply_id: resp.data.supply_id,
							category_id: resp.data.category_id
						};
						
						// 세션 파라미터 저장
						util.setSessionParam("external", "sensor", sessionParam);
						resolve();
					} else {
						location.href = "/external/sensor/list";
					}
				});
			});
		},
			
		// 상품 정보 설정
		setSensorInfo: function(data) {
			// 측정장비 이름 
			$("#sensorName").text(data.sensor_name);
			
			// 모델 이름
			$("#modelName").text(data.model_name);
		},
		
		// 등록 데이터 
		getSubmitData: function() {
			let data_v = {};
			
			// 측정장비 아이디
			data_v.sensor_id = _urlParam.sensor_id;
		
			data_v.add_sensor_device_list = [];
			
			$("#listBody tr").get().reverse().forEach(function(o) {
				let sensorSerialInfo = {};
				
				// 측정장비 시리얼 번호 아이디 
				let sensorDeviceId = $(o).attr("data-sensor-device-id");
				if(sensorDeviceId) {
					sensorSerialInfo.sensor_device_id = sensorDeviceId;
				}
				
				// 시리얼 번호
				sensorSerialInfo.serial_number = $(o).find("input[type='text']").val().trim();
				
				data_v.add_sensor_device_list.push(sensorSerialInfo);
			});
			
			// 삭제되는 디바이스 목록
			data_v.remove_sensor_device_id_list = _removesensorDeviceIdList;
			
			return data_v;
		}
	}
	
	// 리스트 담당
	const _list = {
		getList: function() {
			return new Promise((resolve) => {
				let url_v = "/external/sensor/device/list";
	
				let sensorId = _urlParam.sensor_id;
				
				let data_v = {
					sensor_id: sensorId,
				}
				comm.send(url_v, data_v, "POST", function(resp) {
					if(resp.result) {
						_list.drawList(resp.list);
						resolve();
					}
				});
			});
		},
	
		drawList: function(list) { 
			if(!list) {
				return;
			}
			
			// 시리얼 번호 정보
			_addSerialForm(list.reverse());
		}
	}
	
	// 이벤트 담당
	const _event = {
		// 시리얼 번호 등록 폼 추가
		clickAddSerialForm: function() {
			_addSerialForm();
		},
		
		// 시리얼 번호 일괄 등록 모달
		clickSerialAddModal: function() {
			$("#serialNumAddModal").modal("show");
		},
		
		// 시리얼 번호 등록 폼 일괄 추가
		clickBulkAddSerialNum: function() {
			let bulkAddCnt = Number($("#bulkAddCnt").val());
			
			if(!bulkAddCnt) {
				return false;
			}
			let list = new Array(bulkAddCnt);
			
			_addSerialForm(list);
			
			$("#modelNameAddModal").modal("hide");
		},

		// 시리얼 번호 등록 폼 삭제
		clickRemoveSerialForm: function(evo) {
			let tr_o = evo.parent().parent();
			
			let sensorDeviceId = tr_o.attr("data-sensor-device-id");
			if(sensorDeviceId) {
				_removesensorDeviceIdList.push(Number(sensorDeviceId));
			}
			tr_o.remove();
			
			_serialAddFormReCount();
		},
		
		// 작성 취소
		clickCancel: function() {
			location.href="/external/sensor/list";
		}, 
		
		// 시리얼번호 등록 
		clickaddSensorDevice: function() {
			let data_v = _data.getSubmitData();
			
			let isValid = _validateSubmitData(data_v);
			if(!isValid) {
				return;
			}
			let url_v = "/external/sensor/device/add";
			
			comm.send(url_v, data_v, "POST", function(resp) {
				if(resp.result) {
					location.href = "/external/sensor/list";
				} else if(resp.code == "9100") {
					customModal.alert({
						content: "중복되는 시리얼넘버가 있습니다."
					});
				}
			});
		},
		
		// 엑셀로 등록하기
		clickAddExcel: function() {
			$("#excelFile").click();
		},
		
		// 파일 변경
		changeFile: function(evo) {
			let file = evo[0].files[0];
			_readExcel(file, ["serial_number"]);
		}
	}
	
	// 엑셀 읽기
	function _readExcel(excelFile, headers) {
		if(!excelFile) {
			return;
		}
		// 엑셀 파일 체크
		if(!fileUtil.isUploadableExt(excelFile, ["xlsx", "xls"])) {
			customModal.alert({
				content: "확장자가 올바르지 않습니다."
			});
			return;
		}
		let reader = new FileReader();
        
        reader.onload = function () {
        	let data = reader.result;
            let workBook = XLSX.read(data, {type: "binary"});
            let sheet = workBook.Sheets[workBook.SheetNames[0]]; 
            let rows = XLSX.utils.sheet_to_json(sheet, {header: headers});

            // 시리얼 번호 등록 폼 추가
            _addSerialForm(rows);
        };
        reader.readAsBinaryString(excelFile);
    }
	
	// 시리얼 번호 등록 폼 추가
	function _addSerialForm(list = new Array(1)) {
		let documentFragment = $(document.createDocumentFragment());
		
		for(let data of list) {
			documentFragment.append(_createSerialForm(data));
		}
		$("#listBody").prepend(documentFragment);
		
		_serialAddFormReCount();
		_evInit();
	}
	
	// 시리얼 번호 등록 폼 추가
	function _createSerialForm(data = {}) {
		let tr_o = $("<tr>").attr("data-sensor-device-id", data.sensor_device_id ? data.sensor_device_id : null);
		{
			// 번호
			let td_o = $("<td>");
			tr_o.append(td_o);
		}
		{
			// 시리얼 번호 
			let td_o = $("<td>");
			let input_o = $("<input>").attr({
				"type"			: "text",
				"placeholder" 	: "시리얼넘버 입력",
				"value"			: data.serial_number ? data.serial_number : "",
			});
			let span_o = $("<span>").addClass("slotDel")
				.attr({
					"data-src"	: "serialAdd",
					"data-act"	: "clickRemoveSerialForm",
				})
				.text("x");
			
			td_o.append(input_o);
			td_o.append(span_o);
			tr_o.append(td_o);
		}
		return tr_o;
	}
	
	// 시리얼 번호 등록 폼 재 카운트 
	function _serialAddFormReCount() {
		$("#listBody").find("tr").each(function(i,o) {
			$(o).find("td").eq(0).text(i + 1);
		});
	}
	
	// 모델명 일괄 등록 모달 초기화
	function _initAddModelNameModal() {
		$('#serialNumAddModal').on('hidden.bs.modal', function (e) {
			  $("#bulkAddCnt").val("");
		});
	}
	
	// 시리얼 번호 데이터 유효성 검사
	function _validateSubmitData(data) {
		
		let msg = "";
		let serialList = data.add_sensor_device_list;
		let dulicateCheckList = [];
		
		$.each(serialList, function(i, o) {
			let serialNum = o.serial_number;
			
			if(!serialNum) {
				msg = "입력되지 않은 정보가 있습니다.";
				return false;
			} else {
				$.each(dulicateCheckList, function(i, dupl_o) {
					if(dupl_o.serial_number == serialNum) {
						msg = "중복되는 시리얼 넘버가 있습니다.";
						return false;
					}
				});
				if(msg) {
					return false;
				}
			}
			dulicateCheckList.push(o);
		});
		
		if(msg) {
			customModal.alert({
				content: msg
			});
			return false;
		} 
		return true;
	}
	
	return {
		init
	}
	
})();