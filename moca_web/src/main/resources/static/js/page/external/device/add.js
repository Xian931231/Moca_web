const deviceAdd = (function () {
	
	const _urlParam = util.getUrlParam();
	
	// 삭제되는 기기 아이디 목록
	let _removeDeviceIdList = [];
	
	// 해당 페이지 초기화 함수
	function init() {
		if(!_urlParam || !_urlParam.product_id) {
			location.href = "/external/product/list";
		} else {
			return Promise.all([
				_data.getProduct(),
				_list.getList()
			]).then(() => {
				_evInit();
				_initAddModelNameModal();
			});
		}
	}
	
	// 이벤트 초기화 
	function _evInit() {
		let evo = $("[data-src='deviceAdd'][data-act]").off();
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
			if(act_v == "clickAddDeviceForm") {
				event.clickAddDeviceForm();
			} else if(act_v == "clickBulkAddMoadlName") {
				event.clickBulkAddMoadlName();
			} else if(act_v == "clickModelNameAddModal") {
				event.clickModelNameAddModal();
			} else if(act_v == "clickRemoveDeviceForm") {
				event.clickRemoveDeviceForm(evo);
			} else if(act_v == "clickAddDevice") {
				event.clickAddDevice();
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
		getProduct: function() {
			return new Promise((resolve) => {
				let url_v = "/external/product/spec/detail";
				let productId = _urlParam.product_id;
				
				let data_v = {
					product_id: productId,
				}
				comm.send(url_v, data_v, "POST", function(resp) {
					if(resp.result) {
						_data.setProductInfo(resp.data);
						
						let sessionParam = {
							supply_id: resp.data.supply_id,
							category_id: resp.data.category_id
						};
						// 세션 파라미터 저장
						util.setSessionParam("external", "device", sessionParam);
						resolve();
					} else {
						location.href = "/external/product/list";
					}
				});
			});
		},
			
		// 상품 정보 설정
		setProductInfo: function(data) {
			$("#productName").text(data.product_name);
		},
		
		// 등록 데이터 
		getSubmitData: function() {
			let data_v = {};
			
			// 상품 아이디
			data_v.product_id = _urlParam.product_id;
			
			// 등록 또는 수정되는 모델명 및 시리얼 번호
			// device_id가 있을 경우 수정 없을 경우 등록
			data_v.add_device_list = [];
			
			$("#listBody tr").get().reverse().forEach(function(o) {
				let deviceInfo = {};
				
				// 디바이스 아이디
				let deviceId = $(o).attr("data-device-id");
				if(deviceId) {
					deviceInfo.device_id = deviceId;
				}
				
				// 모델 이름 
				deviceInfo.model_name = $(o).find("input[name='modelName']").val().trim();
				
				// 시리얼 번호
				deviceInfo.serial_number = $(o).find("input[name='serialNum']").val().trim();
				
				data_v.add_device_list.push(deviceInfo);
			});
			
			// 삭제되는 디바이스 목록
			data_v.remove_device_id_list = _removeDeviceIdList;
			
			return data_v;
		}
	}
	
	// 리스트 담당
	const _list = {
		getList: function() {
			return new Promise((resolve) => {
				let url_v = "/external/device/list";
	
				let productId = _urlParam.product_id;
				
				let data_v = {
					product_id: productId,
				}
				comm.send(url_v, data_v, "POST", function(resp) {
					if(resp.result) {
						_list.drawList(resp.list);
						resolve();
					} else {
						location.href = "/external/product/list";
					}
				});
			});
		},
	
		drawList: function(list) { 
			if(!list) {
				return;
			}
			_addDeviceForm(list.reverse());
		}
	}
	
	// 이벤트 담당
	const _event = {
		// 기기 등록 폼 추가
		clickAddDeviceForm: function() {
			$("listBody").append(_addDeviceForm());
		},
		
		// 모델명 일괄 등록 모달
		clickModelNameAddModal: function() {
			$("#modelNameAddModal").modal("show");
		},
		
		// 기기 등록 폼 일괄 추가
		clickBulkAddMoadlName: function() {
			let bulkAddModelName = $("#bulkAddModelName").val();
			let bulkAddCnt = Number($("#bulkAddCnt").val());
			
			if(!bulkAddCnt) {
				return false;
			}
			let addList = new Array(bulkAddCnt).fill({model_name: bulkAddModelName});
			
			_addDeviceForm(addList);
			
			$("#modelNameAddModal").modal("hide");
		},

		// 기기 등록 폼 삭제
		clickRemoveDeviceForm: function(evo) {
			let tr_o = evo.parent().parent();
			
			let deviceId = tr_o.attr("data-device-id");
			if(deviceId) {
				let progressCppCnt = tr_o.attr("data-progress-cpp-cnt");
				if(progressCppCnt > 0) {
					customModal.alert({
						content: "CPP 광고가 진행중인 기기는 삭제하실 수 없습니다."
					});
					return false;
				}
				_removeDeviceIdList.push(Number(deviceId));
			}
			tr_o.remove();
			
			_deviceAddFormReCount();
		},
		
		// 작성 취소
		clickCancel: function() {
			location.href="/external/device/list";
		}, 
		
		// 기기 등록 
		clickAddDevice: function() {
			let data_v = _data.getSubmitData();
			
			if(!_validateSubmitData(data_v)) {
				return;
			}
			let url_v = "/external/device/add";
			
			comm.send(url_v, data_v, "POST", function(resp) {
				if(resp.result) {
					location.href = "/external/device/list";
				} else if(resp.code == 9100) {
					customModal.alert({
						content: "중복되는 시리얼넘버가 있습니다."
					});
				} else if(resp.code == 9115) {
					customModal.alert({
						content: "CPP 광고가 진행중인 기기는 삭제하실 수 없습니다."
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
			_readExcel(file, ["model_name","serial_number"]);
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

            _addDeviceForm(rows);
        };
        reader.readAsBinaryString(excelFile);
    }
	
	// 기기 등록 폼 추가
	function _addDeviceForm(list = new Array(1)) {
		let documentFragment = $(document.createDocumentFragment());
		let appendTarget = $("#listBody");
		
		for(let data of list) {
			documentFragment.append(_createDeviceForm(data));
		}
		appendTarget.prepend(documentFragment);
		
		_deviceAddFormReCount();
		_evInit();
	}
	
	// 기기 등록 폼 생성
	function _createDeviceForm(data = {}) {
		let tr_o = $("<tr>").attr({
			"data-device-id"		: data.device_id ? data.device_id : null,
			"data-progress-cpp-cnt" : data.in_progress_cpp_cnt,
		});
		{
			// 번호
			let td_o = $("<td>");
			tr_o.append(td_o);
		}
		{
			// 모델명
			let td_o = $("<td>");
			let input_o = $("<input>").attr({
				"type"			: "text",
				"name"			: "modelName",
				"placeholder" 	: "모델명 입력",
				"value"			: data.model_name ? data.model_name : "",
			});
			td_o.append(input_o);
			tr_o.append(td_o);
		}
		{
			// 모델명
			let td_o = $("<td>");
			let input_o = $("<input>").attr({
				"type"			: "text",
				"name"			: "serialNum",
				"placeholder" 	: "시리얼넘버 입력",
				"value"			: data.serial_number ? data.serial_number : "",
			});
			let span_o = $("<span>").addClass("slotDel")
				.attr({
					"data-src"	: "deviceAdd",
					"data-act"	: "clickRemoveDeviceForm",
				})
				.text("x");
			
			td_o.append(input_o);
			td_o.append(span_o);
			tr_o.append(td_o);
		}
		return tr_o;
	}
	
	// 기기 등록 폼 재 카운트 
	function _deviceAddFormReCount() {
		$("#listBody").find("tr").each(function(i,o) {
			$(o).find("td").eq(0).text(i + 1);
		});
	}
	
	// 모델명 일괄 등록 모달 초기화
	function _initAddModelNameModal() {
		$('#modelNameAddModal').on('hidden.bs.modal', function (e) {
			  $("#bulkAddModelName").val("");
			  $("#bulkAddCnt").val("");
		});
	}
	
	// 등록 데이터 유효성 검사
	function _validateSubmitData(data) {
		let msg = "";
		let deviceList = data.add_device_list;
		let dulicateCheckList = [];

		$.each(deviceList, function(i, o) {
			let modelName = o.model_name;
			let serialNum = o.serial_number;
			
			if(!modelName || !serialNum) {
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