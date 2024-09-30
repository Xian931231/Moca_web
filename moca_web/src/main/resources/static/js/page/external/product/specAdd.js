let productSpecAdd = (function () {
	
	const _urlParam = util.getUrlParam();
	
	const _productImglimitExt = ["jpg", "jpeg", "png", "gif", "bmp"];
	
	let _productDetail = null;
	
	let _objectURL = null;
	
	// 해당 페이지 초기화 함수
	function init(){
		if(!_urlParam || !_urlParam.product_id) {
			location.href = "/external/product/list";
		} else {
			return new Promise((resolve) => {
				_data.getCodeList("SUPPORT_FORMAT", (list) => _data.setSupportFormat(list));
				_data.getCodeList("ANDROID", (list) => _data.setCodeSelectBox("deviceOsSb", list));
				_data.getCodeList("SCREEN_RATE", (list) => _data.setCodeSelectBox("screenRateSb", list));
				_data.getCodeList("PAGE_SIZE", (list) => _data.setCodeSelectBox("screenResolutionSb", list));
				_detail.getDetail(resolve);
			});
		}
	}
	
	// 이벤트 초기화 
	function _evInit() {
		let evo = $("[data-src='productSpecAdd'][data-act]").off();
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
			if(act_v == "clickCancle") {
				event.clickCancle();
			} else if(act_v == "clickAddProductSpec") {
				event.clickAddProductSpec();
			} else if(act_v == "clickSupportFormat") {
				event.clickSupportFormat(evo);
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
			event.changeProductImg(evo);
		}
	}
	
	const _data = {
		// 코드 리스트 조회
		getCodeList: function(parentCode, callback) {
			if(!parentCode) {
				return;
			}
			let url_v = "/common/code/list";
			
			let data_v = {
				parent_code: parentCode,
			}
			comm.send(url_v, data_v, "POST", function(resp) {
				if(callback && typeof(callback) == "function") {
					callback(resp.list);
				}
			});
		},
		
		// 코드정보 셀렉트 박스 설정
		setCodeSelectBox: function(targetId, list) {
			if(targetId) {
				let select_o = $("#" + targetId).empty();
				select_o.selectpicker("destroy");
				
				for(let item of list) {
					let option_o = $("<option>").val(item.code).text(item.code_name);
					select_o.append(option_o);
				}
				select_o.selectpicker("refresh");
			}
		},
		
		// 재생 가능한 포멧 설정
		setSupportFormat: function(list) {
			let appendTartget = $("#formatBtnGroup");
			
			for(let item of list) {
				let btn_o = $("<button>").addClass("btn btn-def dis")
					.attr({
						"type" : "button",
						"value": item.code
					}).text(item.code_name);
				
				let i_o = $("<i>").attr({
						"data-src": "productSpecAdd",
						"data-act": "clickSupportFormat",
					});
				btn_o.append(i_o);
				appendTartget.append(btn_o, " ");
			}
		},
		
		// 저장 데이터
		getSubmitData: function() {
			let data_v = {};
			
			// 상품 아이디
			data_v.product_id = _urlParam.product_id; 

			// 상품 이미지
			let productImg = $("#productImgFile").val();
			if(productImg) {
				data_v.product_image = $("#productImgFile")[0].files;
				
				if(_productDetail.product_image) {
					// 기존 이미지 삭제 용도
					data_v.saved_product_image = _productDetail.product_image;
				}
			} 
			
			// 디바이스 버전
			data_v.product_os = $("#deviceOsSb").val();
			
			// 재생 가능 포멧
			let supportFormatList = [];
			$("#formatBtnGroup button").each(function(i, o) {
				let isNotChoice = $(o).hasClass("dis");
				if(!isNotChoice) {
					supportFormatList.push($(o).val());
				}
			});

			data_v.support_format = supportFormatList.join(",");
			
			// 화면 비율
			data_v.screen_rate = $("#screenRateSb").val();
			
			// 해상도
			data_v.screen_resolution = $("#screenResolutionSb").val();
			
			// 크기
			let screenSize = $("#screenSize").val().trim();
			if(screenSize) {
				data_v.screen_size = screenSize;
			}
			
			// 저장 공간
			let storage = $("#storage").val().trim();
			if(storage) {
				data_v.storage = storage;
			}
			
			// 실내/실외 
			data_v.install_position = $("button[name='installPosition'].act").val();
			
			// 설치 방향
			data_v.install_direction = $("button[name='installDirection'].act").val();
			
			// 음향 지원
			data_v.support_audio = $("button[name='supportAudio'].act").val();
			
			return data_v;
		}
	}
	
	// 상세 담당
	const _detail = {
		getDetail: function(resolve) {
			let url_v = "/external/product/spec/detail";
			let productId = _urlParam.product_id;
			let data_v = {
				product_id: productId,
			}
			comm.send(url_v, data_v, "POST", function(resp) {
				if(resp.result) {
					_productDetail = resp.data;
					_detail.drawDetail(_productDetail);
					_evInit();

					resolve();
					
					let sessionParam = {
						supply_id: _productDetail.supply_id,
						category_id: _productDetail.category_id
					};
					
					// 세션 파라미터 저장
					util.setSessionParam("external", "product", sessionParam);
				} else {
					location.href = "/external/product/list";
				}
			});
		},
	
		drawDetail: function(data) { 
			if(!data) {
				return;
			}
			// 상품 이름
			$("#productName").text(data.product_name);
			
			// 상품 이미지
			let productImg_o = $("#produdctImg");
			if(data.product_image) {
				// 상품 이미지가 있을 경우
				_setProductImg(globalConfig.getS3Url() + data.product_image);
			} else {
				// 상품 이미지가 없을 경우
				{
					let div_o = $("<div>");
					let img_o = $("<img>").attr("src", "/assets/imgs/icon_Photos.png");
					div_o.append(img_o);
					productImg_o.append(div_o);
				}
				{

					let div_o = $("<div>").addClass("product-in-img");
					let img_o = $("<img>").attr("src", "/assets/imgs/icon_add.png");
					let p_o = $("<p>").text("사진 등록");
					div_o.append(img_o);
					div_o.append(p_o);
					productImg_o.append(div_o);
				}
			}
          
			// 디바이스 버전
			_choiceSelectBoxValue("deviceOsSb", data.product_os);
			
			// 재생 가능 포멧
			let supportFormat = data.support_format;
			if(supportFormat) {
				let supportFormatList = supportFormat.split(",");
				$.each(supportFormatList, function(i, format) {
					$("#formatBtnGroup").find("button[value="+format+"]").removeClass("dis");
				});
			}
			
			// 화면 비율
			_choiceSelectBoxValue("screenRateSb", data.screen_rate);
			
			// 해상도
			_choiceSelectBoxValue("screenResolutionSb", data.screen_resolution);
			
			// 크기
			$("#screenSize").val(data.screen_size);
			
			// 저장 공간
			$("#storage").val(data.storage);
			
			// 실내/실외 
			_setBtnActive("installPosition", data.install_position);
			
			// 설치 방향
			_setBtnActive("installDirection", data.install_direction);
			
			// 음향 지원
			_setBtnActive("supportAudio", data.support_audio);
		}
	}
	
	// 이벤트 담당
	const _event = {
		// 작성 취소
		clickCancle: function() {
			location.href="/external/product/list";
		}, 
		
		// 사양 등록 
		clickAddProductSpec: function() {
			let data_v = _data.getSubmitData();
			
			let url_v = "/external/product/spec/add";
			
			let formData = util.getFormData(data_v);

			comm.sendFile(url_v, formData, "POST", function(resp) {
				if(resp.result) {
					location.href = "/external/product/list";
				}
			});
		},
		
		// 지원 포맷 클릭
		clickSupportFormat: function(evo) {
			 evo.parent().toggleClass("dis");
		},
		
		// 옵션 버튼 (실내/실외, 실치방향, 음향지원) 클릭 
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
			$("#productImgFile").removeAttr("capture").click();
		},
		
		// 사진 촬영
		clickCamera: function() {
			$("#productImgFile").attr("capture", "camera").click();
		},
		
		// 상품 이미지 변경
		changeProductImg: function(evo) {
			let file = evo[0].files[0];

			// 파일 확장자 체크
			let isValidExt = fileUtil.isUploadableExt(file, _productImglimitExt);
			
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
			_setProductImg(_objectURL);
			
			$("#imgUploadModal").modal("hide");
		}
	}
	
	// 상품 이미지 설정
	function _setProductImg(url) {
		let productImg_o = $("#produdctImg").empty();
		productImg_o.addClass("completed");
		{
			
			let div_o = $("<div>").addClass("product-in-img completed")
				.css({
					"background"			: "url('" + url + "')", 
					"background-repeat" 	: "no-repeat",
					"background-size" 		: "contain",
					"background-position"	: "50% 50%" 
				});

			productImg_o.append(div_o);
		}
	}
	
	// 셀렉트박스 값 선택
	function _choiceSelectBoxValue(targetId, value) {
		if(!targetId || !value) {
			return;
		}
		let select_o = $("#" + targetId);
		select_o.find("option[value='"+ value + "']").attr("selected", "selected");
		select_o.selectpicker("refresh");
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
	
	return {
		init
	}
	
})();