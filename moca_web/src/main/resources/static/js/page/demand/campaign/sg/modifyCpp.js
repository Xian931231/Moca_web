const modifyCpp = (function () {
	
	let urlParam = util.getUrlParam();
	let priceData = {};
	
	let start_o = null;
	let end_o = null;
	let productStart_o = null;
	let productEnd_o = null;
	
	// datepicker 초기화
	function _intiDatePicker() {
		let date = new Date();
		date.setDate(date.getDate() + 3);
		
		let option = {
	        startDate: date,
	        todayHighlight: true,
	        autoclose: false,
		}
		
		start_o = customDatePicker.init("startYmd", option).on("changeDate", function() {
			let startInit_v = moment($("#startYmd").val()).add(6, "days").format("YYYY-MM-DD");
			customDatePicker.init("endYmd")
				.datepicker("setStartDate", startInit_v)
				.datepicker("update", startInit_v);
		});
		end_o = customDatePicker.init("endYmd");
		
		productStart_o = customDatePicker.init("productStartYmd", option).on("changeDate", function() {
			let startInit_v = moment($("#productStartYmd").val()).add(6, "days").format("YYYY-MM-DD");
			customDatePicker.init("productEndYmd")
				.datepicker("setStartDate", startInit_v)
				.datepicker("update", startInit_v);
		});
		productEnd_o = customDatePicker.init("productEndYmd");
	}
	
	// 해당 페이지 초기화 함수
	function init(){
		_intiDatePicker();
		_category.getList("AD_CATE", "mainCategory");
		_product.getCompanyList();
		_sg.getDetail();
		_evInit();
	}
	
	// 이벤트 초기화 
	function _evInit() {
		let evo = $("[data-src='modifyCpp'][data-act]").off();
		evo.on("click change keyup", function(ev){
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
			if(act_v == "modalCategory") { // 광고업종 카테고리 모달
				$("#codeName").val("");
				_category.getModalList();
			} else if(act_v == "searchCategory") { // 카테고리 검색
				_category.getModalList();
			} else if(act_v == "selectCategory") { // 모달 광고업종 카테고리 선택
				event.selectCategory(evo);
			} else if(act_v == "moveSgDetail") {
				event.moveSgDetail();
			} else if(act_v == "modalProduct") { // 진행 상품 리스트 모달
				event.modalProduct(evo);
			} else if(act_v == "searchProduct") {
				_product.getList();
			} else if(act_v == "selectProduct") { // 상품 선택
				event.selectProduct(evo);
			} else if(act_v == "showMaterial") {
				event.showMaterial(evo);
			} else if(act_v == "modifySg") {
				event.modifySg();
			} else if(act_v == "calculatePrice") {
				event.calculatePrice();
			} else if(act_v == "hideMaterial") {
				event.hideMaterial();
			}
		} else if(type_v == "change") {
			if(act_v == "changeSelect") {
				event.changeSelect(evo);
			} else if(act_v == "changeMaterialFile") {
				event.changeMaterialFile(evo);
			} else if(act_v == "changeCompany") {
				_product.getCategoryList(evo);
			} else if(act_v == "changeCategory") {
				_product.getProductList(evo);
			} else if(act_v == "changeSubSelect") { // 소분류 변경 시 초기화
				$("#product").removeAttr("data-pid").val("");
				$("#materialFile").val("");
				$("#materialFile").next().text("파일을 선택하세요");
				$("select[name='category']").selectpicker("refresh");
			} else if(act_v == "changeDate") {
				$("#product").removeAttr("data-pid").val("");
				$("#materialFile").val("");
				$("#materialFile").next().text("파일을 선택하세요");
			}
		} else if(type_v == "keyup") {
			if(act_v == "enterCategory") {
				if(ev.keyCode == 13) {
					_category.getModalList();
				}
			}
		}
	}
	
	const _sg = {
		getDetail: function() {
			let url_v = "/sg/demand/detail";
			
			if(util.valNullChk(urlParam) || !urlParam.id || isNaN(urlParam.id)){ 
				location.href = "/demand/campaign/list";
				return;
			}
			
			let data_v = {
				sg_id : urlParam.id,
			}
			
			comm.send(url_v, data_v, "POST", function(resp) {
				if(resp.result) {
					_sg.drawDetail(resp.sg_manager);
					_category.setData(resp.sg_manager);
					_evInit();
				} else {
					location.href = "/demand/campaign/list";
				}
			});
		},
		
		drawDetail: function(data) {
			if(data.pay_type != "CPP" || data.status == 1 || data.pay_status == "PAY_COMPLETE") {
				location.href = "/demand/campaign/list";
				return;
			}
			let material = data.sg_material_list[0];
			let product = data.ssp_product;
			
			let txt = "현재 상태 : ";
			if(data.status == 0) {
				txt += "승인 대기";
			} else if(data.status == 7 || data.status == 8) {
				txt += "종료";
			} else if(data.status == 9) {
				txt += "승인 거부";
			}
			
			$("#status").text(txt);
			urlParam.campaign_id = data.campaign_id;
			
			$("#sgName").val(util.unescapeData(data.name));
			
			let material_o = $("#existMaterialFile").empty();
			
			// 기존 광고 시작일이 현재 선택 가능한 광고 시작일보다 미래인 경우에만 광고 기간, 매체 / 상품 선택 값 셋팅	
			if(moment(moment().add(3, "days").format("YYYY-MM-DD")) <= moment(data.start_ymd)) {
				start_o.datepicker("setDate", data.start_ymd);
				end_o.datepicker("setDate", data.end_ymd);
				
				let product_o = $("#product").val(product.product_name);
				if(data.status == 0) {
					$("#product").attr({
						"data-pid"	: product.id,
						"data-time" : data.exposure_time,
						"data-size" : product.screen_resolution,
					});
				}
			}
			material_o.attr({
				"data-fid" : material.id,
				"data-matsize" : material.page_size_code,
			});
			
			$("#productButton").attr({
				"data-time"	: data.exposure_time,
				"data-type"	: data.material_kind,
			});
			
			if(material.ratio_type == "V") {
				material_o.addClass("vertiSize");
			}
			{
				let src = globalConfig.getS3Url() + material.file_path;
				let btn_o = $("<button>").attr({
					"type": "button",
					"data-src"	: "modifyCpp",
					"data-act"	: "showMaterial",
					"data-path"	: src,
					"data-type"	: material.file_type,
				});
				{
					let strong_o = $("<strong>");
					let img_o = "";
					if(material.file_type == "IMAGE") {
						strong_o.addClass("filetype type-img");
						img_o = $("<img>").attr({
							"src"	: src,
						});
					} else if(material.file_type == "VIDEO") {
						strong_o.addClass("filetype type-vid");
						img_o = $("<video>").attr({
							"src"	: src
						});
					}
					btn_o.append(strong_o);
					btn_o.append(img_o);
					material_o.append(btn_o);
				}
				{
					let div_o = $("<div>");
					let p1_o = $("<p>").text(material.size_descriptione);
					if((data.exposure_horizon_type == "D" && material.ratio_type == "H") || data.exposure_vertical_type == "D" && material.ratio_type == "V") {
						let span_o = $("<span>").html("&nbsp;유동적 소재 사용").addClass("colorRed");
						p1_o.append(span_o);
					}
					
					let p2_o = $("<p>").text(material.file_name);
					let p3_o = $("<p>");
					let span1_o = $("<span>").text("0:" + data.exposure_time);
					let span2_o = $("<span>").text((material.file_size / 1024 / 1024).toFixed(1) + " mb");
					
					p3_o.append(span1_o);
					p3_o.append(span2_o);
					div_o.append(p1_o);
					div_o.append(p2_o);
					div_o.append(p3_o);
					material_o.append(div_o);
				}
			}
			if(data.material_kind == "IMAGE") {
				$("#materialFile").attr("accept", ".jpg, .png");			
			} else if(data.material_kind == "VIDEO") {
				$("#materialFile").attr("accept", ".mp4");			
			}
			$("#price").html("&nbsp;기존 신청 금액&nbsp;" + util.numberWithComma(data.price) + "원&nbsp;</strong>");
		}
	}
	
	// 광고업종 카테고리 관련 함수
	const _category = {
		setData: function(data) {
			let mainCode = data.main_category_code;
			let middleCode = data.middle_category_code;
			let subCode = data.sub_category_code;
			
			$("#mainCategory").selectpicker("val", mainCode);
			_category.getList(mainCode, "middleCategory", function() {
				$("#middleCategory").selectpicker("val", middleCode);
			});
			_category.getList(middleCode, "subCategory", function() {
				$("#subCategory").selectpicker("val", subCode);
			});
		},
		
		getList: function(code, selectId, callback) {
			let url_v = "/common/code/list";
			
			let data_v = {
				parent_code : code
			}
			
			comm.send(url_v, data_v, "POST", function(resp) {
				if(resp.result) {
					dev.log(resp);
					_category.drawList(resp, selectId, callback)
				}
			});
		},
		
		drawList: function(data, selectId, callback) {
			let list = data.list;
			$("#" + selectId + " option").not("[value='']").remove();
			$("#subCategory option").not("[value='']").remove();
			
			for(let item of list) {
				let option_o = $("<option>").val(item.code).text(item.code_name);
				$("#" + selectId).append(option_o);
			}
			
			if(typeof(callback) == "function") {
				callback();
				$("select[name='category']").selectpicker("refresh");
			}
			$("select[name='category']").selectpicker("refresh");
		},
		
		getModalList: function() {
			let url_v = "/common/code/sg/list";
			
			let data_v = {};
			
			let codeName_v = $("#codeName").val();
			
			if(codeName_v) {
				data_v.code_name = codeName_v;
			}
			
			comm.send(url_v, data_v, "POST", function(resp) {
				if(resp.result) {
					$("#allCategory").modal();
					_category.drawModalList(resp);
					_evInit();
				}
			});
		},
		
		drawModalList: function(data) {
			let list = data.list;
			let body_o = $("#categoryBody").empty();
			
			for(let item of list) {
				let tr_o = $("<tr>");
				{
					// 대분류
					let td_o = $("<td>").text(item.main_category_name);
					tr_o.append(td_o);
				}
				{
					// 중분류
					let td_o = $("<td>").text(item.middle_category_name);
					tr_o.append(td_o);
				}
				{
					// 소분류
					let td_o = $("<td>").text(item.sub_category_name);
					tr_o.append(td_o);
				}
				{
					let td_o = $("<td>");
					let a_o = $("<a>").attr({
						"href" : "javascript:;",
						"data-src" : "modifyCpp",
						"data-act" : "selectCategory",
						"data-main" : item.main_category,
						"data-middle" : item.middle_category,
						"data-sub" : item.sub_category,
					}).text("선택");
					td_o.append(a_o);
					tr_o.append(td_o);
				}
				body_o.append(tr_o);
			}
		},
	}
	
	// 매체/상품 관련 함수
	const _product = {
		getCompanyList: function() {
			let url_v = "/member/demand/supply/list";
			
			comm.send(url_v, null, "POST", function(resp) {
				if(resp.result) {
					_product.drawCompanyList(resp);
				}
			});
		},
		
		drawCompanyList: function(data) {
			let list = data.list;
			$("#companySelect option").not("[value='']").remove();
			
			let select_o = $("#companySelect");
			select_o.append($("<option>").val(0).text("전체"));
			for(let item of list) {
				let option_o = $("<option>").val(item.member_id).text(item.company_name);
				select_o.append(option_o);
			}
			
			$("select[name='product']").selectpicker("refresh");
		},
		
		getCategoryList: function(evo) {
			let url_v = "/product/category/list";
			
			let data_v = {
				member_id : evo.val() 
			}
			
			comm.send(url_v, data_v, "POST", function(resp) {
				if(resp.result) {
					_product.drawCategoryList(resp, evo);
				}
			});
		},
		
		drawCategoryList: function(data, evo) {
			let list = data.list;
			$("#categorySelect option").not("[value='']").remove();
			$("#productSelect option").not("[value='']").remove();
			
			for(let item of list) {
				let option_o = $("<option>").val(item.category_id).text(item.category_name);
				$("#categorySelect").append(option_o);
			}
			
			$("#companySelect").selectpicker("val", evo.val());
			$("#categorySelect").selectpicker("val", "");
			$("#productSelect").selectpicker("val", "");
			
			$("select[name='product']").selectpicker("refresh");
		},
		
		getProductList: function(evo) {
			let url_v = "/product/list";
			
			let data_v = {
				"ssp_category_id" : evo.val()
			}
			
			comm.send(url_v, data_v, "POST", function(resp) {
				if(resp.result) {
					dev.log(resp);
					_product.drawProductList(resp, evo);
				}
			});
		},
		
		drawProductList: function(data, evo) {
			let list = data.list;
			$("#productSelect option").not("[value='']").remove();
			
			let select_o = $("#productSelect");
			select_o.append($("<option>").val(0).text("전체"));
			for(let item of list) {
				let option_o = $("<option>").val(item.product_id).text(item.product_name);
				select_o.append(option_o);
			}
			
			$("#categorySelect").selectpicker("val", evo.val());
			$("#productSelect").selectpicker("val", "");
			
			$("select[name='product']").selectpicker("refresh");
		},
		
		
		getSearchData: function() {
			let data = {};
			
			let isPossible_v = $("#isPossible").is(":checked");
			if(isPossible_v) {
				data.select_possible_yn = "Y";
			}
			
			let startYmd_v = $("#productStartYmd").val();
			if(startYmd_v) {
				data.start_ymd = startYmd_v;
			}
			
			let endYmd_v = $("#productEndYmd").val();
			if(endYmd_v) {
				data.end_ymd = endYmd_v;
			}
			
			let materialKind_v = $("#productButton").attr("data-type");
			if(materialKind_v) {
				data.material_kind = materialKind_v;
			}
			
			let playtime_v = $("#productButton").attr("data-time");
			if(playtime_v) {
				data.play_time = playtime_v;
			}
			
			let subCategory_v = $("#subCategory").val();
			if(subCategory_v) {
				data.sub_category_code = subCategory_v;
			}
			
			let company_v = $("#companySelect").val();
			if(company_v) {
				data.member_id = company_v;
			}
			
			let category_v = $("#categorySelect").val();
			if(category_v) {
				data.ssp_category_id = category_v;
			}
			
			let product_v = $("#productSelect").val();
			if(product_v) {
				data.ssp_product_id = product_v;
			}
			
			let screenResolution_v = $("#existMaterialFile").attr("data-matsize");
			if(screenResolution_v) {
				data.screen_resolution = screenResolution_v;
			}
			
			dev.log(data);
			return data;
		},
		
		// cpp 진행 상품 리스트
		getList: function(curPage = 1) {
			let url_v = "/product/cpp/list";
			
			let data_v = _product.getSearchData();
			
			let pageOption = {
				"limit": 30
			}
			
			let page_o = $("#listPage").customPaging(pageOption, function(_curPage) {
				_product.getList(_curPage);
			});
			
			let pageParam = page_o.getParam(curPage);
			
			if(pageParam) {
				data_v.offset = pageParam.offset;
				data_v.limit = pageParam.limit;
			}
			
			comm.send(url_v, data_v, "POST", function(resp) {
				if(resp.result) {
					dev.log(resp);
					page_o.drawPage(resp.tot_cnt);
					_product.drawList(resp);
					_evInit();
				}
			});
		},
		
		drawList: function(data) {
			let list = data.list;
			let body_o = $("#productBody").empty();
			for(let item of list) {
				let tr_o = $("<tr>");
				if(item.select_yn != "Y") {
					tr_o.addClass("onGoing");
				}
				{
					// 상태
					let txt = "";
					if(item.select_yn == "P") {
						txt = "진행중";						
					} else if(item.select_yn == "N") {
						txt = "선택불가";
					} else if(item.select_yn == "Y") {
						txt = "선택가능";
					}
					let td_o = $("<td>").text(txt);
					tr_o.append(td_o);
				}
				{
					// 매체
					let td_o = $("<td>").text(item.company_name);
					tr_o.append(td_o);
				}
				{
					// 분류
					let td_o = $("<td>").text(item.category_name);
					tr_o.append(td_o);
				}
				{
					// 상품
					let td_o = $("<td>").text(item.product_name);
					tr_o.append(td_o);
				}
				{
					// 화면크기
					let td_o = $("<td>").html(item.screen_resolution);
					tr_o.append(td_o);
				}
				{
					// 비율
					let txt = "";
					if(item.install_direction == "H") {
						txt += "가로";
					} else if(item.install_direction == "V") {
						txt += "세로";
					}
					let td_o = $("<td>").text(txt);
					tr_o.append(td_o);
				}
				{
					// 시간
					let td_o = $("<td>").text(item.play_time);
					tr_o.append(td_o);
				}
				{
					// 종료일
					let td_o = $("<td>").text(item.sg_end_ymd);
					tr_o.append(td_o);
				}
				{
					// 일일 금액
					let td_o = $("<td>").text("\\" + util.numberWithComma(item.price));
					tr_o.append(td_o);
				}
				{
					let td_o = $("<td>");
					if(item.select_yn == "Y") {
						// 선택
						let a_o = $("<a>").attr({
							"href"		: "javascript:;",
							"data-src"	: "modifyCpp",
							"data-act"	: "selectProduct",
							"data-pid"	: item.product_id,
							"data-name"	: item.product_name,
							"data-size"	: item.screen_resolution,
							"data-time"	: item.play_time,
							"data-start" : $("#productStartYmd").val(),
							"data-end"	: $("#productEndYmd").val(),
						}).text("선택");
						td_o.append(a_o);
					}
					tr_o.append(td_o);
				}
				body_o.append(tr_o);
			}
		}
	}
	
	const _event = {
		selectCategory: function(evo) {
			$("#materialFile").val("");
			$("#materialFile").next().text("파일을 선택하세요");
			$("#product").removeAttr("data-pid").val("");
			
			$("#allCategory").modal("hide");
			$("#middleCategory option").not("[value='']").remove();
			$("#subCategory option").not("[value='']").remove();
			
			let main = evo.attr("data-main");
			let middle = evo.attr("data-middle");
			let sub = evo.attr("data-sub");
			
			$("#mainCategory").selectpicker("val", main);
			_category.getList(main, "middleCategory", function() {
				$("#middleCategory").selectpicker("val", middle);
			});
			_category.getList(middle, "subCategory", function() {
				$("#subCategory").selectpicker("val", sub);
			});
		},
		
		changeSelect: function(evo) {
			if(evo.attr("id") == "mainCategory") {
				_category.getList(evo.val(), "middleCategory", function() {
					$("#mainCategory").selectpicker("val", $("#mainCategory").val());
					$("#middleCategory").selectpicker("val", "");
				});
			} else if(evo.attr("id") == "middleCategory") {
				_category.getList(evo.val(), "subCategory", function() {
					$("#middleCategory").selectpicker("val", $("#middleCategory").val());
				});
			}
			$("#subCategory").selectpicker("val", "");
		},
		
		moveSgDetail: function() {
			location.href = "/demand/campaign/sg/detail?id=" + urlParam.id;
		},
		
		modalProduct: function() {
			let result = true;
			
			let subCategory_v = $("#subCategory").val();
			if(!subCategory_v) {
				result = false;
			}
			
			let startYmd_v = $("#startYmd").val();
			if(!startYmd_v) {
				result = false;
			}
			
			let endYmd_v = $("#endYmd").val();
			if(!endYmd_v) {
				result = false;
			}
			
			let materialKind_v = $("#productButton").attr("data-type");
			if(!materialKind_v) {
				result = false;
			}
			
			let playtime_v = $("#productButton").attr("data-time");
			if(!playtime_v) {
				result = false;
			}
			
			let size_v = $("#existMaterialFile").attr("data-matsize");
			if(!size_v) {
				result = false;
			}
			
			if(!result) {
				customModal.alert({
					"content" : "입력되지 않은 항목이 있습니다.<br>모든 정보를 입력해주세요.",
				});
			} else {
				productStart_o.datepicker("setDate", moment($("#startYmd").val()).format("YYYY-MM-DD"));
				productEnd_o.datepicker("setDate", moment($("#endYmd").val()).format("YYYY-MM-DD"));
				
				$("#companySelect").selectpicker("val", "");
				$("#categorySelect").selectpicker("val", "");
				$("#productSelect").selectpicker("val", "");
				
				$("#categorySelect option").not("[value='']").remove();
				$("#productSelect option").not("[value='']").remove();
				$("select[name='product']").selectpicker("refresh");
				
				$("#isPossible").prop("checked", true);
				
				$("#mediaPaperCallUp").modal();
				_product.getList();
			}
		},
		
		changeMaterialFile: function(evo) {
			let playTime = $("#product").attr("data-time");
			let screenSize = $("#product").attr("data-size");
			let size = screenSize.split("x");
						
			let file = evo.get(0).files[0];
			let fileMsg_o = evo.siblings("p").empty();
			evo.siblings("label").text("파일을 선택하세요");
			if(!file) {
				return;
			}
			
			let type = file.type;
			
			if(!fileUtil.isUploadableSize(file)) {
				evo.val("");
				fileMsg_o.text("용량이 초과됐습니다.");
				return;
			}
			
			fileUtil.getFileInfo(file, function(data) {
				if(data.width == size[0] && data.height == size[1]) {
					if(type.startsWith("video")) {
						if(fileUtil.floorDuration(data.duration) != parseFloat(playTime)) {
							evo.val("");
							fileMsg_o.text("재생 시간이 일치하지 않습니다.");
							return;
						}
						evo.attr("data-width", data.width);
						evo.attr("data-height", data.height);
						evo.attr("data-time", Math.floor(data.duration));
					}
					fileUtil.setUploadFile(file, "materialFile");
					fileMsg_o.text("소재가 변경되었습니다.");
				} else {
					evo.val("");
					if(type.startsWith("image")) {
						fileMsg_o.text("사이즈가 일치하지 않습니다.");
					} else if(type.startsWith("video")) {
						fileMsg_o.text("해상도가 일치하지 않습니다.");
					}
				}
			});
		},
		
		selectProduct: function(evo) {
			start_o.datepicker("setDate", moment(evo.attr("data-start")).format("YYYY-MM-DD"));
			end_o.datepicker("setDate", moment(evo.attr("data-end")).format("YYYY-MM-DD"));
			
			$("#materialFile").val("");
			$("#materialFile").next().text("파일을 선택하세요");
				
			$("#mediaPaperCallUp").modal("hide");			
			let productName = evo.attr("data-name");
			
			let product_o = $("#product");
			product_o.val(productName);
			product_o.attr({
				"data-pid"	: evo.attr("data-pid"),
				"data-size"	: evo.attr("data-size"),
				"data-time" : evo.attr("data-time"),
			});
			
		},
		
		showMaterial: function(evo) {
			$("#videoPopUp").modal();
			let filePath = evo.attr("data-path");
			let fileType = evo.attr("data-type");
			let body_o = $("#showFileBody").empty();
			
			let file_o = "";
			if(fileType == "IMAGE") {
				file_o = $("<img>").attr({
					"src": filePath,
					"width"	: "100%"
				});
			} else if(fileType == "VIDEO") {
				file_o = $("<video>").attr({
					"src" : filePath,
					"width"	: "100%",
					"controls": true,
				});
				fileUtil.loadVideo(file_o, function() {
					file_o.get(0).play();
				});
			}
			body_o.append(file_o);
		},
		
		hideMaterial: function() {
			let video_o = $("#showFileBody").find("video");
			if(video_o.length > 0) {
				video_o.get(0).pause();
			}
		},
		
		calculatePrice: function() {
			let url_v = "/sg/demand/calculate/cpp";
			
			if(_validatePriceData()) {
				let data_v = _getSaveData();
			
				comm.send(url_v, data_v, "POST", function(resp) {
					if(resp.result) {
						priceData = data_v;
						$("#price").attr("data-price", resp.price);
						$("#costBox").contents()[4].textContent = " ▶ 수정 금액 " + util.numberWithComma(resp.price) + "원";
						$("#price").next().show();
					}
				});
			} else {
				customModal.alert({
					"content" : "입력되지 않은 항목이 있습니다.<br>모든 정보를 입력해주세요.",
				});
				$("#costBox").contents()[4].textContent = "";
				$("#price").removeAttr("data-price").next().hide();
			}
		},
		
		modifySg: function() {
			let url_v = "/sg/demand/modify/info";
		
			let data_v = _getSaveData();
			
			if(_validateSgData()) {
				if(Object.keys(priceData).length === 0) {
					customModal.alert({
						"content" : "신청금액을 계산해주세요.",
					});
					return;
				}
				
				if(!_validateIsChangeData(data_v)) {
					customModal.alert({
						"content" : "변경된 항목이 있습니다.<br>신청금액을 다시 계산해주세요.",
					});
					return;
				}
				
				let formData = util.getFormData(data_v);
				
				customModal.confirm({
					"content" : "수정 내용을 저장하고 승인대기 상태가 됩니다.",
					"confirmCallback" : function() {
						comm.sendFile(url_v, formData, "POST", function(resp) {
							if(resp.result) {
								location.href = "/demand/campaign/sg/list?id=" + urlParam.campaign_id;
							}
						});
					}
				});
			} else {
				customModal.alert({
					"content" : "입력되지 않은 항목이 있습니다.<br>모든 정보를 입력해주세요.",
				});
			}
		}
	}
	
	function _validatePriceData() {
		let result = true;

		let startYmd_v = $("#startYmd").val();
		if(!startYmd_v) {
			result = false;
		}
		
		let endYmd_v = $("#endYmd").val();
		if(!endYmd_v) {
			result = false;
		}
		
		let start = new Date(startYmd_v);
		let end = new Date(endYmd_v);
		
		let diffTime = end.getTime() - start.getTime();
		let diffDay = Math.abs(diffTime / (1000 * 60 * 60 * 24));
		if(diffDay < 6) {
			result = false;
		}
		
		let productId_v = $("#product").attr("data-pid");
		if(!productId_v) {
			result = false;
		}
		
		let materialKind_v = $("#productButton").attr("data-type");
		if(!materialKind_v) {
			result = false;
		}
		
		let exposureTime_v = $("#productButton").attr("data-time");
		if(!exposureTime_v) {
			result = false;
		}
		
		return result;	
	}
	
	function _validateSgData() {
		let result = true;
		
		result = _validatePriceData();
		
		let name_v = $("#sgName").val();
		if(!name_v) {
			result = false;
		}
		
		let mainCategory_v = $("#mainCategory").val();
		if(!mainCategory_v) {
			result = false;
		}
		
		let middleCategory_v = $("#middleCategory").val();
		if(!middleCategory_v) {
			result = false;
		}
		
		let subCategory_v = $("#subCategory").val();
		if(!subCategory_v) {
			result = false;
		}
		
		return result;
	}
	
	function _getSaveData() {
		let data = {};
		
		// -- 계산할 때 필요한 파라미터
		let startYmd_v = $("#startYmd").val();
		if(startYmd_v) {
			data.start_ymd = startYmd_v;
		}
		
		let endYmd_v = $("#endYmd").val();
		if(endYmd_v) {
			data.end_ymd = endYmd_v;
		}
		
		let sspProductId_v = $("#product").attr("data-pid");
		if(sspProductId_v) {
			data.ssp_product_id = sspProductId_v;
		}
		
		let materialKind_v = $("#productButton").attr("data-type");
		if(materialKind_v) {
			data.material_kind = materialKind_v;
		}
		
		let exposureTime_v = $("#productButton").attr("data-time");
		if(exposureTime_v) {
			data.exposure_time = exposureTime_v;
		}
		// --
		// -- 광고 등록할 때 추가로 필요한 파라미터
		let sgId_v = urlParam.id;
		if(sgId_v) {
			data.sg_id = sgId_v; 
		}
		
		let sgName_v = $("#sgName").val();
		if(sgName_v) {
			data.name = sgName_v;
		}
		
		let mainCategory_v = $("#mainCategory").val();
		if(mainCategory_v) {
			data.main_category_code = mainCategory_v;
		}
		
		let middleCategory_v = $("#middleCategory").val();
		if(middleCategory_v) {
			data.middle_category_code = middleCategory_v;
		}
		
		let subCategory_v = $("#subCategory").val();
		if(subCategory_v) {
			data.sub_category_code = subCategory_v;
		}
		
		let materialFile_o = $("#materialFile");
		let materialFile_v =  materialFile_o.val();
		
		let size = $("#product").attr("data-size");		
		
		if(materialFile_v) {
			let fileName = "";
			let fileDataName = "";
			if(size == "1920x1080") {
				fileName = "file_w_1920";
				fileDataName = "file_data_w_1920";
			} else if(size == "1600x1200") {
				fileName = "file_w_1600";
				fileDataName = "file_data_w_1600";
			} else if(size == "2560x1080") {
				fileName = "file_w_2560";
				fileDataName = "file_data_w_2560";
			} else if(size == "1080x1920") {
				fileName = "file_h_1920";
				fileDataName = "file_data_h_1920";
			} else if(size == "1200x1600") {
				fileName = "file_h_1600";
				fileDataName = "file_data_h_1600";
			} else if(size == "1080x2560") {
				fileName = "file_h_2560";
				fileDataName = "file_data_h_2560";
			} else if(size == "1080x1080") {
				fileName = "file_o";
				fileDataName = "file_data_o";
			}
			data[fileName] = materialFile_o.get(0).files[0];
			data.material_list = [String($("#existMaterialFile").attr("data-fid"))];
			if(materialKind_v == "VIDEO") {
				data[fileDataName] = JSON.stringify({
					"height" : materialFile_o.attr("data-height"),
					"width"	 : materialFile_o.attr("data-width"),
					"playtime"	: materialFile_o.attr("data-time"),
				});
			}
		}
		
		let price_v = $("#price").attr("data-price");
		if(price_v) {
			data.price = price_v;
		}
		
		// --
		return data;
	}
	
	// 금액 계산 후 변경된 데이터가 있는지 검사
	function _validateIsChangeData(data) {
		let result = true;
		
		if(data.start_ymd !== priceData.start_ymd) {
			result = false;
		} else if(data.end_ymd !== priceData.end_ymd) {
			result = false;
		} else if(data.ssp_product_id !== priceData.ssp_product_id) {
			result = false;
		} else if(data.material_kind !== priceData.material_kind) {
			result = false;
		} else if(data.exposure_time !== priceData.exposure_time) {
			result = false;
		}
		
		return result;
	}
	
	return {
		init
	}
})();