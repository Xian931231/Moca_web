const motorIdAdd = (function () {
	
	const _urlParam = util.getUrlParam();
	
	// 삭제되는 구분 아이디 목록
	let _removeMotorIdList = [];
	
	// 해당 페이지 초기화 함수
	function init() {
		if(!_urlParam || !_urlParam.motor_position_id) {
			location.href = "/external/motor/list";
		} else {
			return Promise.all([
				_data.getMotorPosition(),
				_list.getList()
			]).then(() => {
				_initAddIdModal();
				_evInit();
			});
		}
	}
	
	// 이벤트 초기화 
	function _evInit() {
		let evo = $("[data-src='motorIdAdd'][data-act]").off();
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
			if(act_v == "clickAddIdForm") {
				event.clickAddIdForm();
			} else if(act_v == "clickBulkAddIdForm") {
				event.clickBulkAddIdForm();
			} else if(act_v == "clickIdAddModal") {
				event.clickIdAddModal();
			} else if(act_v == "clickRemoveIdForm") {
				event.clickRemoveIdForm(evo);
			} else if(act_v == "clickAddMortorId") {
				event.clickAddMortorId();
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
		// 게재 위치 정보 
		getMotorPosition: function() {
			return new Promise((resovle) => {
				let url_v = "/external/motor/detail";

				let motorPositionId = _urlParam.motor_position_id;
				
				let data_v = {
					motor_position_id: motorPositionId,
				}

				comm.send(url_v, data_v, "POST", function(resp) {
					if(resp.result) {
						_data.setMotorPositionInfo(resp.data);
						
						let sessionParam = {
							supply_id: resp.data.supply_id,
							category_id: resp.data.category_id
						};
						// 세션 파라미터 저장
						util.setSessionParam("external", "motor", sessionParam);
						resovle();
					} else {
						location.href = "/external/motor/list";
					}
				});
			});
		},
			
		// 게재 위치 정보 설정
		setMotorPositionInfo: function(data) {
			$("#positionName").text(data.position_name + " 구분 ID 관리");
		},
		
		// 등록 데이터 
		getSubmitData: function() {
			let data_v = {};
			
			// 게재 위치 아이디
			data_v.motor_position_id = _urlParam.motor_position_id;
			
			data_v.add_motor_id_list = [];
			
			$("#listBody tr").get().reverse().forEach(function(o) {
				let motorInfo = {};
				
				// 아이디 값
				let motorId = $(o).attr("data-motor-id");
				if(motorId) {
					motorInfo.motor_id = Number(motorId);
				}
				
				// 구분 아이디 
				motorInfo.car_number = $(o).find("input[name='id']").val().trim();
				
				// 시리얼 번호
				motorInfo.ip_address = $(o).find("input[name='ip']").val().trim();
				
				data_v.add_motor_id_list.push(motorInfo);
			});
			
			// 삭제되는 디바이스 목록
			data_v.remove_motor_id_list = _removeMotorIdList;
			
			return data_v;
		}
	}
	
	// 리스트 담당
	const _list = {
		getList: function() {
			return new Promise((resolve) => {
				let url_v = "/external/motor/id/list";
	
				let motorPositionId = _urlParam.motor_position_id;
				
				let data_v = {
					motor_position_id: motorPositionId,
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
			// 디바이스 정보
			_addIdForm(list.reverse());
		}
	}
	
	// 이벤트 담당
	const _event = {
		// 구분 아이디 등록 폼 추가
		clickAddIdForm: function() {
			_addIdForm();
		},
		
		// 구분 아이디 일괄 등록 모달
		clickIdAddModal: function() {
			$("#idAddModal").modal("show");
		},
		
		// 구분 아이디 등록 폼 일괄 추가
		clickBulkAddIdForm: function() {
			let bulkAddCnt = Number($("#bulkAddCnt").val());
			
			if(!bulkAddCnt) {
				return false;
			}
			_addIdForm(new Array(bulkAddCnt));
			
			$("#idAddModal").modal("hide");
		},

		// 구분 아이디 폼 삭제
		clickRemoveIdForm: function(evo) {
			let tr_o = evo.parent().parent();
			
			let motorId = tr_o.attr("data-motor-id");
			if(motorId) {
				let progressCppCnt = tr_o.attr("data-progress-cpp-cnt");
				if(progressCppCnt > 0) {
					customModal.alert({
						content: "CPP 광고가 진행중인 구분 ID는 삭제하실 수 없습니다."
					});
					return;
				}
				_removeMotorIdList.push(Number(motorId));
			}
			tr_o.remove();
			
			_idAddFormReCount();
		},
		
		// 작성 취소
		clickCancel: function() {
			location.href="/external/motor/list";
		}, 
		
		// 구분 아이디 등록 
		clickAddMortorId: function() {
			let data_v = _data.getSubmitData();

			if(!_validateSubmitData(data_v)) {
				return;
			}
			let url_v = "/external/motor/id/add";
			
			comm.send(url_v, data_v, "POST", function(resp) {
				if(resp.result) {
					location.href = "/external/motor/list";
				} else {
					let msg = "";
					
					if(resp.code == 9103) {
						msg = "중복되는 구분ID가 있습니다.";
					} else if(resp.code == 9104) {
						msg = "중복되는 IP가 있습니다.";
					} else if(resp.code == 9111) {
						msg = "형식이 맞지 않는 IP가 있습니다.";
					} else if(resp.code == 9114) {
						msg = "CPP 광고가 진행중인 구분 ID는 삭제하실 수 없습니다.";
					}
					
					if(msg) {
						customModal.alert({
							content: msg
						});
					}
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
			_readExcel(file, ["car_number", "ip_address"]);
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

            if(rows.length > 0) {
        		// 구분 아이디 등록 폼 추가
        		_addIdForm(rows.reverse());
            }
        };
        reader.readAsBinaryString(excelFile);
    }
	
	// 구분 아이디 등록 폼 추가
	function _addIdForm(list = new Array(1)) {
		let documentFragment = $(document.createDocumentFragment());
		
		for(let data of list) {
			documentFragment.append(_createIdForm(data));
		}
		$("#listBody").prepend(documentFragment);
		
		_idAddFormReCount();
		_evInit();
	}
	
	function _createIdForm(data = {}) {
		let tr_o = $("<tr>").attr({
			"data-motor-id"			: data.motor_id ? data.motor_id : null,
			"data-progress-cpp-cnt" : data.in_progress_cpp_cnt, 
		});
		{
			// 번호
			let td_o = $("<td>");
			tr_o.append(td_o);
		}
		{
			// 구분 아이디
			let td_o = $("<td>");
			let input_o = $("<input>").attr({
				"type"			: "text",
				"name"			: "id",
				"placeholder" 	: "차량번호, 차대번호, 지점명 등",
				"value"			: data.car_number ? data.car_number : "",
			});
			td_o.append(input_o);
			tr_o.append(td_o);
		}
		{
			// IP
			let td_o = $("<td>");
			let input_o = $("<input>").attr({
				"type"			: "text",
				"name"			: "ip",
				"placeholder" 	: "000.000.000.000",
				"value"			: data.ip_address ? data.ip_address : "",
			});
			let span_o = $("<span>").addClass("slotDel")
				.attr({
					"data-src"	: "motorIdAdd",
					"data-act"	: "clickRemoveIdForm",
				})
				.text("x");
			
			td_o.append(input_o);
			td_o.append(span_o);
			tr_o.append(td_o);
		}
		return tr_o;
	}
	
	// 구분 아이디 등록 폼 재 카운트 
	function _idAddFormReCount() {
		$("#listBody").find("tr").each(function(i,o) {
			$(o).find("td").eq(0).text(i + 1);
		});
	}
	
	// 일괄 등록 모달 초기화
	function _initAddIdModal() {
		$('#idAddModal').on('hidden.bs.modal', function (e) {
			  $("#bulkAddCnt").val("");
		});
	}
	
	// 등록 데이터 유효성 검사
	function _validateSubmitData(data) {
		let msg = "";
		let motorIdList = data.add_motor_id_list;
		let dulicateCheckList = [];
		
		$.each(motorIdList, function(i, o) {
			let carNumber = o.car_number;
			let ipAddress = o.ip_address;
			
			if(!carNumber || !ipAddress) {
				msg = "입력되지 않은 정보가 있습니다.";
				return false;
			} else {
				$.each(dulicateCheckList, function(i, dupl_o) {
					if(dupl_o.car_number == carNumber) {
						msg = "중복되는 구분ID가 있습니다.";
						return false;
					} else if(dupl_o.ip_address == ipAddress) {
						msg = "중복되는 IP가 있습니다.";
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