const addCpm = (function () {
	
	let urlParam = util.getUrlParam();
	let priceData = {};
	
	// datepicker 초기화
	function _initDatePicker() {
		let date = new Date();
		date.setDate(date.getDate() + 3);
		// + 3일 이후부터 시작일 선택 가능

		let option = {
	        startDate: date,
	        todayHighlight: true,
		}
		customDatePicker.init("startYmd", option);
	}
	
	// 해당 페이지 초기화 함수
	function init(){
		_initDatePicker();
		_campaign.getDetail();
		_area.getSiList();
		_category.getList("AD_CATE", "mainCategory");
		_time.getList();
		_event.changeMaterialType();
	}
	
	// 이벤트 초기화 
	function _evInit() {
		let evo = $("[data-src='addCpm'][data-act]").off();
		evo.on("click change mousedown keyup", function(ev){
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
				_category.getModalList();
			} else if(act_v == "searchCategory") { // 카테고리 검색
				_category.getModalList();
			} else if(act_v == "selectCategory") {
				event.selectCategory(evo);
			} else if(act_v == "clickTime") {
				event.clickTime(evo);
				evo.off("mouseleave");
			} else if(act_v == "calculateTarget") { // 목표 노출 수 계산
				event.calculateTarget(evo);
			} else if(act_v == "addSg") {
				event.addSg();
			} else if(act_v == "weekUseYn") {
				event.weekUseYn(evo);
			} else if(act_v == "calculatePrice") { // 계산하기
				event.calculatePrice();
			} else if(act_v == "cancelSg") {
				event.moveSgList();
			} else if(act_v == "resetTimeOption") {
				$("#timeBody td").removeClass("pick");
			}
		} else if(type_v == "change") {
			if(act_v == "changeSelect") {
				event.changeCategory(evo);
			} else if(act_v == "changeArea") {
				_area.getGuList();
			}else if(act_v == "changeExpOption") { // 노출 옵션 추가
				event.changeExpOption();
			} else if(act_v == "changeSizeBinary") { // 가로/세로, 1:1
				event.changeSizeBinary();
			} else if(act_v == "horizontalOption") { // 가로 옵션
				event.changeHorizontalOption();
			} else if(act_v == "changeMaterialOption") { // 광고소재 체크박스 토글
				event.toggleMaterialFile(evo);
			} else if(act_v == "verticalOption") { // 세로 옵션
				event.changeVerticalOption();
			} else if(act_v == "changeMaterialFile") { // 파일 첨부
				event.changeMaterialFile(evo);
			} else if(act_v == "changeMaterialType") { // 소재 옵션 변경
				event.changeMaterialType();
			} else if(act_v == "targetChange") { // 목표 노출 수 변경
				event.changeExposureTarget(evo);
			} else if(act_v == "limitChange") {
				event.changeExposureLimit(evo);
			} else if(act_v == "changeTimeOption") { //소재 노출시간 변경
				event.changeTimeOption();
			} else if(act_v == "notUseLimit") {
				event.notUseLimit(evo);
			}
		} else if(type_v == "mousedown") {
			if(act_v == "clickTime") {
				event.mouseDragEvent(evo);
			}
		} else if(type_v == "keyup") {
			if(act_v == "enterCategory") {
				if(ev.keyCode == 13) {
					_category.getModalList();
				}
			}
		}
	}
	
	// 캠페인 제목
	const _campaign = {
		getDetail: function() {
			let url_v = "/campaign/demand/min/detail";
			
			if(util.valNullChk(urlParam) || !urlParam.id || isNaN(urlParam.id)){ 
				location.href = "/demand/campaign/list";
				return;
			}
			
			let data_v = {
				campaign_id : urlParam.id,
			}
			
			comm.send(url_v, data_v, "POST", function(resp) {
				if(resp.result) {
					_campaign.drawDetail(resp.data);
				} else {
					location.href = "/demand/campaign/list";
				}
			});
		},
		
		drawDetail: function(data) {
			if(data.pay_type == "CPM") {
				$("#campaignName").text(data.name);
			} else {
				location.href = "/demand/campaign/list";
			}
		},
	}
	
	// 지역 관련 함수
	const _area = {
		getSiList: function() {
			let url_v = "/common/areacode/list";
			
			comm.send(url_v, null, "POST", function(resp) {
				if(resp.result) {
					_area.drawSiList(resp);
				}
			});
		},
		
		drawSiList: function(data) {
			let list = data.list;
			$("#siCategory option").not("[value='']").remove();

			for(item of list) {
				let option_o = $("<option>").val(item.si_code).text(item.si_name);
				$("#siCategory").append(option_o);
			}
			
			$("select[name='area']").selectpicker("refresh");
		},
		
		getGuList: function() {
			let url_v = "/sg/demand/areaGu/list";
			
			let data_v = {
				si_code : $("#siCategory").val()
			}
			
			comm.send(url_v, data_v, "POST", function(resp) {
				if(resp.result) {
					_area.drawGuList(resp, data_v);
				}
			});
		},
		
		drawGuList: function(data, param) {
			let list = data.list;
			
			$("#guCategory option").not("[value='']").remove();

			for(item of list) {
				let option_o = $("<option>").val(item.area_id).text(item.gu_name);
				$("#guCategory").append(option_o);
			}
			
			$("#siCategory").selectpicker("val", param.si_code);
			$("#guCategory").selectpicker("val", "");
			
			$("select[name='area']").selectpicker("refresh");
		}
	}
	
	// 카테고리 관련 함수
	const _category = {
		
		// 전체 카테고리 모달 > 전체 카테고리 목록
		getModalList: function() {
			let url_v = "/common/code/sg/list";
			
			let data_v = {};
			
			let codeName_v = $("#codeName").val();
			
			if(codeName_v) {
				data_v.code_name = codeName_v;
			}
			
			comm.send(url_v, data_v, "POST", function(resp) {
				if(resp.result) {
					_category.drawModalList(resp);
					_evInit();
				}
			});
		},
		
		// 전체 카테고리 모달 > 목록 그리기
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
						"data-src" : "addCpm",
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
		
		// 카테고리 분류별 단일 목록 조회
		getList: function(code, changeSelectId, callback) {
			let url_v = "/common/code/list";
			
			let data_v = {
				parent_code : code
			}
			
			comm.send(url_v, data_v, "POST", function(resp) {
				if(resp.result) {
					_category.drawList(resp, changeSelectId, callback);
					_evInit();
				}
			});
		},
		
		// 카테고리 분류별 그리기
		drawList: function(data, changeSelectId, callback) {
			let list = data.list;
			$("#" + changeSelectId + " option").not("[value='']").remove();
			$("#subCategory option").not("[value='']").remove();
			let changeSelect_o = $("#" + changeSelectId);

			for(let item of list) { // 상위 코드에 맞는 하위 select option 그려주기
				let option_o = $("<option>").val(item.code).text(item.code_name);
				changeSelect_o.append(option_o);
			}
			
			if(typeof(callback) == "function") {
				callback();
				$("select[name='category']").selectpicker("refresh");
			}
			
			$("select[name='category']").selectpicker("refresh");
		}
	}
	
	const _time = {
		getList: function() {
			let url_v = "/common/weektime/list";
			
			comm.send(url_v, null, "POST", function(resp) {
				dev.log(resp);
				if(resp.result) {
					_time.drawList(resp.list);
					_evInit();
				}

			});
		},
		
		drawList: function(list) {
			let body_o = $("#timeBody");
			
			body_o.css({
	          '-webkit-user-select': 'none',
	          '-moz-user-select': 'none',
	          '-ms-user-select': 'none',
	          '-o-user-select': 'none',
	          'user-select': 'none'
	        });
	        
			for(let i=0; i<24; i++) {
				let hourName = "hour_" + i.toString().padStart(2, 0);
				let timeText = i.toString().padStart(2, 0) + ":00 ~ " + (i+1).toString().padStart(2, 0) + ":00"
				
				let tr_o = $("<tr>").attr("data-time", hourName);
				{
					let th_o = $("<th>").text(timeText);
					tr_o.append(th_o);
				}
				
				$.each(list, function(index, item) {
					let td_o = $("<td>").attr({
						"data-week" : item.week_code,
					}).text(timeText);
					
					if(item[hourName] == "1") {
						td_o.attr({
							"data-src" : "addCpm",
							"data-act" : "clickTime",
						});
					} else {
						td_o.addClass("on");
					}
					tr_o.append(td_o);
				});
				body_o.append(tr_o);
			}
		}
	}
	
	// 이벤트 담당
	const _event = {
		changeExposureTarget: function() {
			let target = $("#exposureTarget").val();
			if(isNaN(target) || !target) {
				$("#exposureTarget").val(1000);
				return;
			} else {
				target = parseInt(target);
				$("#exposureTarget").val(target);
			}
			
			let limit = $("#exposureLimit").val();
			let modal = false;
			
			if(target < 1000) {
				$("#exposureTargetModal").modal();
				if(!$("#notUseLimit").prop("checked")) {
					$("#exposureLimit").val(0);
				}
				target = 1000;
				modal = true;
			} else if(target >= 10000) {
				target = 10000;
			}
			
			target = Math.floor(target/1000) * 1000;
			if(limit > target && !modal) {
				$("#exposureLimitModal").modal();
				$("#exposureLimit").val(target);
			}
			$("#exposureTarget").val(target);
		},
		
		changeExposureLimit: function() {
			let target = parseInt($("#exposureTarget").val());
			let limit = parseInt($("#exposureLimit").val());
			
			if(isNaN(limit) || !limit || limit < 0) {
				$("#exposureLimit").val(0);
				return;
			}
			
			if(limit > target) {
				$("#exposureLimitModal").modal();
				limit = target;
			}
			$("#exposureLimit").val(limit);
		},
		
		selectCategory: function(evo) {
			$("#modalCategory").modal("hide");
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
		
		changeCategory: function(evo) {
			if(evo.attr("id") == "mainCategory") {
				_category.getList(evo.val(), "middleCategory", function() {
					$("#mainCategory").selectpicker("val", $("#mainCategory").val());
				});
			} else if(evo.attr("id") == "middleCategory") {
				_category.getList(evo.val(), "subCategory", function() {
					$("#middleCategory").selectpicker("val", $("#middleCategory").val());
				});
			}
		},
				
		changeExpOption: function() {
			let optionType = $("input:radio[name=expOption]:checked").val();
			
			if(optionType == 'N') {
				$("#areaOption").slideUp(150);
				$("#timeOption").slideUp(150);
			} else if(optionType == 'T') {
				$("#areaOption").slideUp(150);
				$("#timeOption").slideDown(150);
			} else if(optionType == 'A') {      
				$("#timeOption").slideUp(150);
				$("#areaOption").slideDown(150);
			}
		},
		
		changeSizeBinary: function() {
			let optionType = $("input:radio[name=sizeBinary]:checked").val();
			
			if(optionType == "D") {
				$("#horivert").slideDown(150);
				$("#oneToOne").slideUp(150);
			} else if(optionType == "S") {
				$("#horivert").slideUp(150);
				$("#oneToOne").slideDown(150);
			}
		},
		
		changeHorizontalOption: function() {
			let optionType = $("input:radio[name=horizOption]:checked").val();
			$("#horizontalFluid").removeClass("d-none");
			
			if(optionType == "fix") {
				$("#horizontalFluid").slideUp(150);
				$("#horizontalFix").slideDown(150);
			} else if(optionType == "fluid") {
				$("#horizontalFix").slideUp(150);
				$("#horizontalFluid").slideDown(150);
			}
		},
		
		changeVerticalOption: function() {
			let optionType = $("input:radio[name=vertiOption]:checked").val();
			$("#verticalFluid").removeClass("d-none");
			
			if(optionType == "fix") {
				$("#verticalFluid").slideUp(150);
				$("#verticalFix").slideDown(150);
			} else if(optionType == "fluid") {
				$("#verticalFix").slideUp(150);
				$("#verticalFluid").slideDown(150);
			}
		},
		
		calculateTarget: function(evo) {
			let cnt = $("#exposureTarget").val();
			let amount = evo.attr("data-cnt");
			
			if(!cnt) {
				cnt = 0;
			}
			let targetCnt = parseInt(cnt) + parseInt(amount);
			if(targetCnt < 1000 || !targetCnt) {
				targetCnt = 1000;
			}
			
			if(targetCnt >= 10000) {
				targetCnt = 10000;
			}
			
			$("#exposureTarget").val(targetCnt);
			_event.changeExposureTarget();
		},
		
		addSg: function() {
			let url_v = "/sg/demand/addInfo";

			let data_v = _getSaveData();
			
			if(_validateAddSgData()) {
				if(Object.keys(priceData).length === 0) {
					customModal.alert({
						"content" : "신청금액을 계산해주세요."
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
				
				comm.sendFile(url_v, formData, "POST", function(resp) {
					if(resp.result) {
						customModal.alert({
							"content" : "저장 후 광고 승인을 요청했습니다. <br>익일까지 입금이 확인되지 않으면 광고 승인이 자동취소됩니다.",
							"confirmCallback" : function() {
								location.href = "/demand/campaign/sg/list?id=" + urlParam.id;
							}
						});
					}
				});
			} else {
				customModal.alert({
					"content" : "입력되지 않은 항목이 있습니다.<br>모든 정보를 입력해주세요."
				});
			}
			
		},
		
		calculatePrice: function() {
			let url_v = "/sg/demand/calculate/cpm";
		
			if(_validatePriceData(function(txt) {
				customModal.alert({
					"content" : txt,
				});
				$("#price").removeAttr("data-price").empty();
			})) {
				let data_v = _getSaveData();
				comm.send(url_v, data_v, "POST", function(resp) {
					if(resp.result) {
						priceData = data_v;
						$("#price").text(util.numberWithComma(resp.price) + " 원");
						$("#price").attr("data-price", resp.price);
					}
				});
			}
		},
		
		// 파일 첨부
		changeMaterialFile: function(evo) {
			let file = evo.get(0).files[0];
			let errMsg_o = evo.siblings("p").children("span").empty();
			evo.siblings("label").text("파일을 선택하세요");
			
			if(!file) {
				return;
			}
			
			let type = file.type;
			
			if(!fileUtil.isUploadableSize(file)) {
				evo.val("");
				errMsg_o.text("용량이 초과됐습니다.");
				return;	
			}
			
			fileUtil.getFileInfo(file, function(data) {
				let screenSize = evo.attr("data-size");
				let size = screenSize.split("x");
				if(data.width == size[0] && data.height == size[1]) {
					if(type.startsWith("video")) {
						let playTime = $("input:radio[name=matTimeOption]:checked").val();
						if(fileUtil.floorDuration(data.duration) != parseFloat(playTime)) {
							evo.val("");
							errMsg_o.text("재생 시간이 일치하지 않습니다.");
							return;
						}
						evo.attr("data-width", data.width);
						evo.attr("data-height", data.height);
						evo.attr("data-time", Math.floor(data.duration));
					}
					fileUtil.setUploadFile(file, evo.attr("id"));
				} else {
					evo.val("");
					if(type.startsWith("image")) {
						errMsg_o.text("사이즈가 일치하지 않습니다.");
					} else if(type.startsWith("video")) {
						errMsg_o.text("해상도가 일치하지 않습니다.");
					}
				}
			});
		},
		
		// 소재 분류 별 첨부 가능 파일 설정
		changeMaterialType: function() {
			let materialType = $("input:radio[name=matOption]:checked").val();
			let materialList = $("#material input[type=file]");
			if(materialType == "IMAGE") {
				for(let item of materialList) {
					$(item).attr("accept", ".jpg, .png");
				}
			} else if(materialType == "VIDEO") {
				for(let item of materialList) {
					$(item).attr("accept", ".mp4");
				}
			}
			_clearAllFileData();
		},
		
		moveSgList: function() {
			location.href = "/demand/campaign/sg/list?id=" + urlParam.id;
		},
		
		clickTime: function(evo) {
			let dataId = evo.attr("data-week");
			let dayYn = $("#week th[data-id=" + dataId + "] input").prop("checked");
			
			if(dayYn) {
				evo.toggleClass("pick");
			}
		},
		
		weekUseYn: function(evo) {
			let dataId = evo.closest("th").attr("data-id");
			if(!evo.prev().prop("checked")) {
				evo.prev().prop("checked");
				$("#timeBody td[data-week=" + dataId + "][data-src=addCpm]").removeClass("on");
			} else {
				$("#timeBody td[data-week=" + dataId + "]").removeClass("pick");
				$("#timeBody td[data-week=" + dataId + "]").addClass("on");
			}
		},
		
		mouseDragEvent: function(evo) {
			evo.on("mouseleave", function() {
				_event.clickTime(evo);
			});
			$(window).on("mouseenter", function(e) {
				evo.off("mouseleave");
				
				if(e.target.tagName == "TD") {
					if($(e.target).attr("data-act") == "clickTime") {
						_event.clickTime($(e.target));
					}
				} else {
					$(window).off("mouseenter mouseleave");
				}
			});
			
			$(window).on("mouseup", function(e) {
				$(window).off("mouseenter mouseleave mouseup");
			});
		},
		
		changeTimeOption: function() {
			let materialType = $("input:radio[name=matOption]:checked").val();
			if(materialType == "VIDEO") {
				_clearAllFileData();
			}
		},
		
		notUseLimit: function() {
			if($("#notUseLimit").prop("checked")) {
				$("#exposureLimit").val("");
				$("#exposureLimit").attr("disabled", true);
			} else {
				$("#exposureLimit").val(0);
				$("#exposureLimit").attr("disabled", false);
			}
		},
		
		toggleMaterialFile: function(evo) {
			$("#" + $(evo).attr("data-id")).toggle();
		}
	}
	
	// 시간 옵션 시 schedule JSON 데이터 가져오기
	function _getTimeData() {
		let scheduleList = [];

		let week = $("#week .day");
		let weekUseYn = $("#week .day input");
		
		// 주 > 시간 만큼 반복
		for(let i=0; i<week.length; i++) {
			let weekData = {};
			let weekCode = $(week[i]).attr("data-id");
			weekData.week_code = weekCode;
			let useYn = "Y";
			if(!$(weekUseYn[i]).prop("checked")) {
				useYn = "N";
			}
			weekData.use_yn = useYn;
			weekData.schedule = [];
			
			let time = $("#timeBody tr");
			let partTime = $("#timeBody td[data-week=" + weekCode + "]");
			for(let j=0; j<time.length; j++) {
				let timeData = {};
				timeData.name = $(time[j]).attr("data-time");
				
				let value = 0;
				if($(partTime[j]).hasClass("pick")) {
					value = 1;
				}
				timeData.value = value;
				weekData.schedule.push(timeData);
			}
			scheduleList.push(weekData);
		}
		return JSON.stringify(scheduleList);
	}
	
	function _getSaveData() {
		let data = {};
		
		// -- 광고 신청 금액 계산할 때 필요한 파라미터
		let target_v = $("#exposureTarget").val();
		if(target_v) {
			data.exposure_target = target_v;
		}
		
		let optionType_v = $("input:radio[name=expOption]:checked").val();
		
		
		if(optionType_v == "N") { // 사용 안함
			data.target_week_yn = "N";
			data.target_area_yn = "N";
		} else if(optionType_v == "T") { //시간 옵션
			if($("#week .day input").prop("checked")) {
				data.target_week_yn = "Y";
				data.target_area_yn = "N";
				data.schedule_list = _getTimeData();
			} else {
				data.target_week_yn = "N";
				data.target_area_yn = "N";
			}
		} else if(optionType_v = "A") { // 지역 옵션
			data.target_week_yn = "N";
			data.target_area_yn = "Y";
			let areaId_v = $("#guCategory").val();
			if(areaId_v) {
				data.area_id = areaId_v;
			}
		}

		let materialKind_v = $("input:radio[name=matOption]:checked").val();
		if(materialKind_v) {
			data.material_kind = materialKind_v;
		}
		
		let exposureTime_v = $("input:radio[name=matTimeOption]:checked").val();
		if(exposureTime_v) {
			data.exposure_time = exposureTime_v;
		}
		
		// -- 광고 등록할 때 추가로 필요한 파라미터
		let campaignId_v = urlParam.id;
		if(campaignId_v) {
			data.campaign_id = campaignId_v; 
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
		
		let startYmd_v = $("#startYmd").val();
		if(startYmd_v) {
			data.start_ymd = startYmd_v;
		}
		
		if($("#notUseLimit").prop("checked")) {
			data.exposure_limit = 0;
		} else {
			let exposureLimit_v = $("#exposureLimit").val();
			if(exposureLimit_v) {
				data.exposure_limit = exposureLimit_v;
			}
		}
		
		let sizeBinary_v = $("input:radio[name=sizeBinary]:checked").val();
		if(sizeBinary_v == "D") { // 가로/세로
			data.material_ratio = "D"
					
			if($("#horizontal").prop("checked")) { // 가로
				let horizOption_v = $("input:radio[name=horizOption]:checked").val();
				if(horizOption_v == "fix") { // 선택 사이즈에만 노출
					data.exposure_horizon_type = "S";
					
					let selectFile1920_v = $("#hFixFile1920").val();
					
					if(selectFile1920_v) {
						data.file_w_1920 = $("#hFixFile1920").get(0).files[0];;
						if(materialKind_v == "VIDEO") {
							data.file_data_w_1920 = _getFileData("hFixFile1920");
						}
					}
					
					if($("#matSizeOpt2").prop("checked")) {
						let selectFile1600_v = $("#hFixFile1600").val();
						
						if(selectFile1600_v) {
							data.file_w_1600 = $("#hFixFile1600").get(0).files[0];;
							if(materialKind_v == "VIDEO") {
								data.file_data_w_1600 = _getFileData("hFixFile1600");
							}
						}
					}
					
					if($("#matSizeOpt3").prop("checked")) {
						let selectFile2560_v = $("#hFixFile2560").val();
						
						if(selectFile2560_v) {
							data.file_w_2560 = $("#hFixFile2560").get(0).files[0];
							if(materialKind_v == "VIDEO") {
								data.file_data_w_2560 = _getFileData("hFixFile2560");
							}
						}
					}
				} else if(horizOption_v == "fluid") { // 유동적 소재 사용
					data.exposure_horizon_type = "D";
					let selectFile1920_v = $("#hFluFile1920").val();
					
					if(selectFile1920_v) {
						data.file_w_1920 = $("#hFluFile1920").get(0).files[0];
						if(materialKind_v == "VIDEO") {
							data.file_data_w_1920 = _getFileData("hFluFile1920");
						}
					}
				}
			} else {
				data.exposure_horizon_type = "N";
			}
			
			if($("#vertical").prop("checked")) { // 세로
				let vertiOption_v = $("input:radio[name=vertiOption]:checked").val();
				if(vertiOption_v == "fix") { // 선택 사이즈에만 노출
					data.exposure_vertical_type = "S";
				
					let selectFile1920_v = $("#vFixFile1920").val();
					
					if(selectFile1920_v) {
						data.file_h_1920 = $("#vFixFile1920").get(0).files[0];
						if(materialKind_v == "VIDEO") {
							data.file_data_h_1920 = _getFileData("vFixFile1920");
						}
					}
					
					if($("#matSizeOpt4").prop("checked")) {
						let selectFile1600_v = $("#vFixFile1600").val();
						
						if(selectFile1600_v) {
							data.file_h_1600 = $("#vFixFile1600").get(0).files[0];
							if(materialKind_v == "VIDEO") {
								data.file_data_h_1600 = _getFileData("vFixFile1600");
							}
						}
					}
					
					if($("#matSizeOpt5").prop("checked")) {
						let selectFile2560_v = $("#vFixFile2560").val();
						
						if(selectFile2560_v) {
							data.file_h_2560 = $("#vFixFile2560").get(0).files[0];
							if(materialKind_v == "VIDEO") {
								data.file_data_h_2560 = _getFileData("vFixFile2560");
							}
						}
					}
				} else if(vertiOption_v == "fluid") { // 유동적 소재 사용
					data.exposure_vertical_type = "D";
				
					let selectFile1920_v = $("#vFluFile1920").val();
					
					if(selectFile1920_v) {
						data.file_h_1920 = $("#vFluFile1920").get(0).files[0];
						if(materialKind_v == "VIDEO") {
							data.file_data_h_1920 = _getFileData("vFluFile1920");
						}
					}
				}
			} else {
				data.exposure_vertical_type = "N";
			}
		} else if(sizeBinary_v == "S") { // 1:1
			data.exposure_horizon_type = "N";
			data.exposure_vertical_type = "N";
			data.material_ratio = "S";		
			
			let selectFile1080_v = $("#oneFile1080").val();
			
			if(selectFile1080_v) {
				data.file_o = $("#oneFile1080").get(0).files[0];
				if(materialKind_v == "VIDEO") {
					data.file_data_o = _getFileData("oneFile1080");
				}
			}
		}
		
		let price_v = $("#price").attr("data-price");
		if(price_v) {
			data.price = price_v;
		}
		
		return data;
	}
	function _getFileData(fileId) {
		let fileData = JSON.stringify({
			"height" : $("#" + fileId).attr("data-height"),
			"width"	 : $("#" + fileId).attr("data-width"),
			"playtime"	: $("#" + fileId).attr("data-time"),
		});
		
		return fileData;
	}
	
	function _validatePriceData(callback) {
		let result = true;
		let txt = "입력되지 않은 항목이 있습니다.<br>모든 정보를 입력해주세요.";
		
		let target_v = parseInt($("#exposureTarget").val());
		if(!target_v || target_v < 100 || target_v > 10000 || isNaN(target_v)) {
			result = false;
		}
		
		let optionType_v = $("input:radio[name=expOption]:checked").val();
		if((optionType_v != "N" && optionType_v != "T" && optionType_v != "A") || !optionType_v) {
			result = false;
		}
		
		if(optionType_v == "T") {
			let pickLength = $("#timeBody td[class=pick]").length;
			if(pickLength < 5) {
				result = false;
				txt = "시간 옵션은 최소 5개 이상의 시간 설정이 필요합니다.";
			}
		} else if(optionType_v == "A") {
			let si_v = $("#siCategory").val();
			if(!si_v) {
				result = false;
			}
			
			let gu_v = $("#guCategory").val();
			if(!gu_v) {
				result = false;
			}
		}
	
		let materialKind_v = $("input:radio[name=matOption]:checked").val();
		if(!materialKind_v) {
			result = false;
		}
		
		let exposureTime_v = $("input:radio[name=matTimeOption]:checked").val();
		if(!exposureTime_v) {
			result = false;
		}
		
		if(!result && txt) {
			if(typeof(callback) == "function") {
				callback(txt);
			}
		}
		
		return result;
	}
	
	function _validateAddSgData() {
		let result = true;
		
		result = _validatePriceData();
		
		let name_v = $("#sgName").val();
		if(!name_v) {
			result = false;
		}
		
		let startYmd_v = $("#startYmd").val();
		if(!startYmd_v) {
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
		
		if(!$("#notUseLimit").prop("checked")) {
			let exposureLimit_v = $("#exposureLimit").val();
			if(!exposureLimit_v || exposureLimit_v < 0 || isNaN(exposureLimit_v)) {
				result = false;
			}
		}
		
		let sizeBinary_v = $("input:radio[name=sizeBinary]:checked").val();
		if(sizeBinary_v == "S") { // 1:1
			let file_v = $("#oneFile1080").val();
			if(!file_v) {
				result = false;
			}
		} else if(sizeBinary_v == "D") { // 가로/세로
			let hvCheck_o = $("input:checkbox[name=hvCheckBox]:checked");
			if(hvCheck_o.length == 0){
				result = false;
			} else {
				if($("#horizontal").prop("checked")) { // 가로
					let horizOpt_v = $("input:radio[name=horizOption]:checked").val();
					if(horizOpt_v == "fix") { // 선택 사이즈 노출
						let file_v = $("#hFixFile1920").val();
						if(!file_v) {
							result = false;
						}
						
						let horiOption_o = $("input:checkbox[name=horiMatSizeOption]:checked")
						for(horiOptItem of horiOption_o) {
							if($(horiOptItem).attr("id") == "matSizeOpt2") { // 1600
								let file_v = $("#hFixFile1600").val();
								if(!file_v) {
									result = false;
								}
							}
							if($(horiOptItem).attr("id") == "matSizeOpt3") { // 2560
								let file_v = $("#hFixFile2560").val();
								if(!file_v) {
									result = false;
								}
							}
						}
						
					} else if(horizOpt_v == "fluid") { // 유동적 소재
						let file_v = $("#hFluFile1920").val();
						if(!file_v) {
							result = false;
						}
					} else {
						result = false;
					}
				} 
				
				if($("#vertical").prop("checked")) { //세로
					let vertiOpt_v = $("input:radio[name=vertiOption]:checked").val();
					if(vertiOpt_v == "fix") { // 선택 사이즈 노출
						let file_v = $("#vFixFile1920").val();
						if(!file_v) {
							result = false;
						}
						
						let vertiOption_o = $("input:checkbox[name=vertiMatSizeOption]:checked")
						for(vertiOptItem of vertiOption_o) {
							if($(vertiOptItem).attr("id") == "matSizeOpt4") { // 1600
								let file_v = $("#vFixFile1600").val();
								if(!file_v) {
									result = false;
								}
							}
							if($(vertiOptItem).attr("id") == "matSizeOpt5") { // 2560
								let file_v = $("#vFixFile2560").val();
								if(!file_v) {
									result = false;
								}
							}
						}
					} else if(vertiOpt_v == "fluid") { // 유동적 소재
						let file_v = $("#vFluFile1920").val();
						if(!file_v) {
							result = false;
						}
					} else {
						result = false;
					}
				}
			}
		} else {
			result = false;
		}
		return result;
	}
	
	function _clearAllFileData() {
		$("#material input[type=file]").each(function(index, item) {
			$(item).val("");
			$("label[for='" + item.id + "']").text("파일을 선택하세요");
		});
	}
	
	// 금액 계산 후 변경된 데이터가 있는지 검사
	function _validateIsChangeData(data) {
		let result = true;
		
		if(data.exposure_target !== priceData.exposure_target) {
			result = false;
		} else if(data.target_week_yn !== priceData.target_week_yn) {
			result = false;
		} else if(data.target_area_yn !== priceData.target_area_yn) {
			result = false;
		} else if(data.material_kind !== priceData.material_kind) {
			result = false
		} else if(data.exposure_time !== priceData.exposure_time) {
			result = false;
		}
		
		return result;
	}
	
	return {
		init
	}
	
})();