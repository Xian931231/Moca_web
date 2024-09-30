const sensorModify = (function () {
	
	const _urlParam = util.getUrlParam();
	
	let _sensorDetail = null;
	
	const _sensorImglimitExt = ["jpg", "jpeg", "png", "gif", "bmp"];
	
	let _objectURL = null;
	
	// 해당 페이지 초기화 함수
	function init(){
		if(!_urlParam || !_urlParam.sensor_id) {
			location.href = "/external/sensor/list";
		} else {
			return _detail.getDetail().then(() => _evInit());
		}
	}
	
	// 이벤트 초기화 
	function _evInit() {
		let evo = $("[data-src='sensorModify'][data-act]").off();
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
			if(act_v == "clickCancel") {
				event.clickCancel();
			} else if(act_v == "clickModifySensor") {
				event.clickModifySensor();
			} else if(act_v == "clickOptionBtn") {
				event.clickOptionBtn(evo);
			} else if(act_v == "clickImgUploadModal") {
				event.clickImgUploadModal();
			} else if(act_v == "clickGallery") {
				event.clickGallery();
			} else if(act_v == "clickCamera") {
				event.clickCamera();
			}
		} else if(type_v == "change") {
			if(act_v == "changeSensorImg") {
				event.changeSensorImg(evo);
			}
		}
	}
	
	const _data = {
		// 저장 데이터
		getSubmitData: function() {
			let data_v = {};
			
			// 측정 장비 아이디
			data_v.sensor_id = _urlParam.sensor_id;  

			// 상품 이미지
			let sensorImg = $("#sensorImgFile").val();
			if(sensorImg) {
				data_v.sensor_image = $("#sensorImgFile")[0].files;
				
				if(_sensorDetail.image_path) {
					// 기존 이미지 삭제 용도
					data_v.saved_sensor_image = _sensorDetail.image_path;
				}
			} 
			
			// 측정 장비 명
			data_v.sensor_name = $("#sensorName").val().trim();

			// 제조사
			data_v.maker = $("#maker").val().trim();
			
			// 모델 명
			data_v.model_name = $("#modelName").val().trim();

			// 설치 위치 
			data_v.install_position = $("button[name='installPosition'].act").val();
			
			// 상세 설치 위치
			data_v.notes = $("#notes").val().trim();
			
			return data_v;
		}
	}
	
	// 상세 담당
	const _detail = {
		// 측정 장비 상세
		getDetail: function() {
			return new Promise((resolve) => {
				let data_v = {
					sensor_id: _urlParam.sensor_id,
				};
				
				let url_v = "/external/sensor/detail";
				
				comm.send(url_v, data_v, "POST", function(resp) {
					if(resp.result) {
						_sensorDetail = resp.data;
						_detail.drawDetail(resp.data);
						
						let sessionParam = {
							supply_id: _sensorDetail.supply_id,
							category_id: _sensorDetail.category_id
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
		
		drawDetail: function(data) {
			if(!data) {
				return;
			}
			
			// 상품 이미지
			if(data.image_path) {
				// 상품 이미지가 있을 경우
				_setSensorImg(globalConfig.getS3Url() + data.image_path);
			} 
          
			// 측정장비 이름
			$("#sensorName").val(data.sensor_name);
			
			// 제조사
			$("#maker").val(data.maker);
			
			// 모델 명
			$("#modelName").val(data.model_name);
			
			// 설치 방향
			_setBtnActive("installPosition", data.install_position);
			
			// 상세 설치 위치
			$("#notes").val(data.notes);
		}
	}
	
	// 이벤트 담당
	const _event = {
		// 작성 취소
		clickCancel: function() {
			location.href="/external/sensor/list";
		}, 
		
		// 측정 장비 수정 
		clickModifySensor: function() {
			let data_v = _data.getSubmitData();
			
			if(!_validateSubmitData(data_v)) {
				return;
			}
			
			let url_v = "/external/sensor/modify";
			
			let formData = util.getFormData(data_v);

			comm.sendFile(url_v, formData, "POST", function(resp) {
				if(resp.result) {
					location.href = "/external/sensor/list";
				}
			});
		},
		
		// 옵션 버튼 (설치 위치) 
		clickOptionBtn: function(evo) {
			evo.siblings().removeClass("act");
			evo.addClass("act");    
		},
		
		// 이미지 업로드 모달
		clickImgUploadModal: function() {
			$("#imgUploadModal").modal("show");
		}, 
		
		// 갤러리 이미지
		clickGallery: function() {
			$("#sensorImgFile").removeAttr("capture").click();
		},
		
		// 사진 촬영
		clickCamera: function() {
			$("#sensorImgFile").attr("capture", "camera").click();
		},
		
		// 상품 이미지 변경
		changeSensorImg: function(evo) {
			let file = evo[0].files[0];

			// 파일 확장자 체크
			let isValidExt = fileUtil.isUploadableExt(file, _sensorImglimitExt);
			
			if(!isValidExt) {
				customModal.alert({
					content: "이미지 파일만 가능합니다."
				});
				return false;
			}
			
			if(_objectURL) {
				URL.revokeObjectURL(_objectURL);
			}
			_objectURL = URL.createObjectURL(file);
			_setSensorImg(_objectURL);
			
			$("#imgUploadModal").modal("hide");
		}
	}
	
	// 상품 이미지 설정
	function _setSensorImg(url) {
		let sensorImg_o = $("#sensorImg").empty();
		sensorImg_o.addClass("completed");
		{
			
			let div_o = $("<div>").addClass("product-in-img completed")
				.css({
					"background"			: "url('" + url + "')", 
					"background-repeat" 	: "no-repeat",
					"background-size" 		: "contain",
					"background-position"	: "50% 50%" 
				});

			sensorImg_o.append(div_o);
		}
	}
	
	// 버튼 활성화 설정
	function _setBtnActive(targetName, value) {
		if(!targetName || !value) {
			return;
		}
		$("button[name='" + targetName + "']").each(function(i, o){
			let btnValue = $(o).val();
			if(btnValue == value) {
				$(o).addClass("act");
			} else {
				$(o).removeClass("act");
			}
		});
	}
	
	// 등록 데이터 유효성 검증
	function _validateSubmitData(data) {
		let msg = "";
		
		if(!data.sensor_name || !data.maker || !data.model_name || !data.install_position || !data.notes) {
			msg = "입력되지 않은 정보가 있습니다.";
		}
		
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