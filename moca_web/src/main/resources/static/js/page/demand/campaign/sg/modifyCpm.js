const modifyCpm = (function () {
	
	let urlParam = util.getUrlParam();
	let priceData = {};
	
	// 해당 페이지 초기화 함수
	function init(){
		_category.getList("AD_CATE", "mainCategory");
		_time.getList();
		_area.getSiList();
		_sg.getDetail();
		_evInit();
	}
	
	// 이벤트 초기화 
	function _evInit() {
		let evo = $("[data-src='modifyCpm'][data-act]").off();
		evo.on("click change blur mousedown keyup", function(ev){
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
			if(act_v == "modalCategory") {
				$("#codeName").val("");
				_category.getModalList();
			} else if(act_v == "searchCategory") {
				_category.getModalList();
			} else if(act_v == "selectCategory") {
				event.selectCategory(evo);
			} else if(act_v == "clickTime") {
				event.clickTime(evo);
				evo.off("mouseleave");
			} else if(act_v == "calExposureTarget") {
				event.calExposureTarget(evo);
			} else if(act_v == "calculatePrice") {
				event.calculatePrice();
			} else if(act_v == "moveSgDetail") {
				event.moveSgDetail();
			} else if(act_v == "modifySg") {
				event.modifySg();
			} else if(act_v == "modalMaterial") {
				event.showMaterial(evo);
			} else if(act_v == "hideMaterial") {
				event.hideMaterial();
			}
		} else if(type_v == "change") {
			if(act_v == "changeExpOption") {
				event.changeExpOption();
			} else if(act_v == "changeSizeBinary") {
				event.changeSizeBinary();
			} else if(act_v == "changeMaterialOption") { // 광고 소재 체크박스 토글
				event.toggleMaterialFile(evo);
			} else if(act_v == "changeHorizontalOption") {
				event.changeHorizontalOption();
			} else if(act_v == "changeVerticalOption") {
				event.changeVerticalOption();
			} else if(act_v == "dayUseYn") {
				event.changeDayUseYn(evo);
			} else if(act_v == "changeSiArea") {
				_area.getGuList();
			} else if(act_v == "changeCategory") {
				event.changeCategory(evo);
			} else if(act_v == "changeMaterialFile") {
				event.changeMaterialFile(evo);
			} else if(act_v == "changeMaterialType") {
				event.changeMaterialType();
			} else if(act_v == "changeMaterialTime") {
				event.changeMaterialTime();
			} else if(act_v == "targetChange") { // 목표 노출 수 변경
				event.changeExposureTarget(evo);
			} else if(act_v == "limitChange") {
				event.changeExposureLimit(evo);
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
					dev.log(resp);
					_sg.drawDetail(resp.sg_manager);
					_sg.drawFileList(resp.sg_manager);
					_category.setData(resp.sg_manager);
					_evInit();
				} else {
					location.href = "/demand/campaign/list";
				}
			});
		},
		
		drawDetail: function(data) {
			if(data.pay_type != "CPM" || data.status == 1 || data.pay_status == "PAY_COMPLETE") {
				location.href = "/demand/campaign/list";
				return;
			}
			
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
			
			_setDatePicker(data.start_ymd);
			
			if(data.target_week_yn == "Y") { // 시간
				$("#targetWeek").prop("checked", true);
				$("#timeOption").show();
				
				_time.setData(data.sg_schedule_list);
			} else if(data.target_area_yn == "Y") { // 지역
				$("#targetArea").prop("checked", true);
				$("#areaOption").show();
				
				_area.setData(data.sg_area);
			} else {
				$("#targetNo").prop("checked", true);
			}
			
			$("input:radio[name='matOption']:input[value='" + data.material_kind + "']").prop("checked", true);
			_event.changeMaterialType(); // 소재 분류에 맞는 등록 가능 파일 설정
			
			$("input:radio[name='matTimeOption']:input[value='" + data.exposure_time + "']").prop("checked", true);
			
			$("#exposureTarget").val(data.exposure_target);
			
			$("#exposureLimit").val(data.exposure_limit);
			
			$("input:radio[name='sizeBinary']:input[value='" + data.material_ratio + "']").prop("checked", true);
			if(data.material_ratio == "D") {
				$("#horiVerti").show();
				$("#oneToOne").hide();
			} else if(data.material_ratio == "S") {
				$("#horiVerti").hide();
				$("#oneToOne").show();
			}
		
			if(data.exposure_horizon_type != "N") {
				$("#horizontal").show();
				$("#horizontalCheck").prop("checked", true);
				$("input:radio[name='horiOption']:input[value='" + data.exposure_horizon_type +"']").prop("checked", true);	
			} else {
				$("#horizontal").hide();
			}
			
			if(data.exposure_vertical_type != "N") {
				$("#vertical").show();
				$("#verticalCheck").prop("checked", true);
				$("input:radio[name='vertiOption']:input[value='" + data.exposure_vertical_type +"']").prop("checked", true);	
			} else {
				$("#vertical").hide();
			}
			
			$("#price").html("&nbsp;기존 신청 금액&nbsp;" + util.numberWithComma(data.price) + "원&nbsp;</strong>");
		},
		
		drawFileList: function(data) {
			let fileList = data.sg_material_list;
			for(let i=0; i<fileList.length; i++) {
				let elementId = "";
				let sizeCode = fileList[i].page_size_code;
				if(sizeCode == "1080x1080") {
					elementId = "oneToOneExistFile";
				} else if(sizeCode == "1920x1080") {
					if(data.exposure_horizon_type == "S") { // 선택
						elementId = "horiFixExist1920";
						$("#horiFix").show();
						$("#horiFluid").hide();
					} else if(data.exposure_horizon_type == "D") { // 유동
						elementId = "horiFluidExist1920";
						$("#horiFluid").show();
						$("#horiFix").hide();
					}
				} else if(sizeCode == "1600x1200") {
					elementId = "horiFixExist1600";
					$("#matSizeOpt2").prop("checked", true);
					$("#horiSize1600").show();
				} else if(sizeCode == "2560x1080") {
					elementId = "horiFixExist2560";
					$("#horiSize2560").show();
					$("#matSizeOpt3").prop("checked", true);
				} else if(sizeCode == "1080x1920") {
					if(data.exposure_vertical_type == "S") {
						elementId = "vertiFixExist1920";
						$("#vertiFix").show();
						$("#vertiFluid").hide();
					} else if(data.exposure_vertical_type == "D") {
						elementId = "vertiFluidExist1920";
						$("#vertiFix").hide();
						$("#vertiFluid").show();
					}
				} else if(sizeCode == "1200x1600") {
					elementId = "vertiFixExist1600";
					$("#matSizeOpt4").prop("checked", true);
					$("#vertiSize1600").show();
				} else if(sizeCode == "1080x2560") {
					elementId = "vertiFixExist2560"
					$("#matSizeOpt5").prop("checked", true);
					$("#vertiSize2560").show();
				}
				_commonDrawFile(elementId, fileList[i], data);
			}
			
		}
	}
	
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
						"data-src" : "modifyCpm",
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
	const _area = {
		setData: function(data) {
			$("#siCategory").selectpicker("val", data.si_code);
			_area.getGuList(function() {
				$("#guCategory").selectpicker("val", data.area_id);
			});
		},
		
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
		
		getGuList: function(callback) {
			let url_v = "/sg/demand/areaGu/list";
			
			let data_v = {
				si_code : $("#siCategory").val()
			}
			
			comm.send(url_v, data_v, "POST", function(resp) {
				if(resp.result) {
					_area.drawGuList(resp, data_v, callback);
				}
			});
		},
		
		drawGuList: function(data, param, callback) {
			let list = data.list;
			
			$("#guCategory option").not("[value='']").remove();

			for(item of list) {
				let option_o = $("<option>").val(item.area_id).text(item.gu_name);
				$("#guCategory").append(option_o);
			}
			
			$("#siCategory").selectpicker("val", param.si_code);
			$("#guCategory").selectpicker("val", "");
			
			if(typeof(callback) == "function") {
				callback();
				$("select[name='area']").selectpicker("refresh");
			}
			$("select[name='area']").selectpicker("refresh");
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
							"data-src" : "modifyCpm",
							"data-act" : "clickTime",
						});
					} else {
						td_o.addClass("on");
					}
					tr_o.append(td_o);
				});
				body_o.append(tr_o);
			}
		},
		
		setData: function(scheduleList) {
			let week = $("#timeOption thead .day input")
			for(let i=0; i<week.length ; i++) {
				if(scheduleList[i].use_yn == "Y") {
					$(week[i]).prop("checked", true);
				} else if(scheduleList[i].use_yn == "N") {
					$(week[i]).prop("checked", false);
				}
				
				for(let j=0; j<24; j++) {
					let keyHour = "hour_" + j.toString().padStart(2, 0);
					let timeTd = $($($("tbody tr")[j]).children("td")[i]);
					
					if(scheduleList[i].use_yn == "N") {
						timeTd.addClass("on");
					}
					
					if(scheduleList[i][keyHour] == 1) {
						if(!timeTd.hasClass("on")) {
							timeTd.addClass("pick");
						}
					}
				}
			}
		},
	}
	
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
		
		changeExpOption: function() {
			let optionType = $("input:radio[name=expOption]:checked").val();
			
			if(optionType == "N") {
				$("#areaOption").slideUp(150);
				$("#timeOption").slideUp(150);
			} else if(optionType == "T") {
				$("#areaOption").slideUp(150);
				$("#timeOption").slideDown(150);
			} else if(optionType == "A") {      
				$("#timeOption").slideUp(150);
				$("#areaOption").slideDown(150);
			}
		},
		
		changeSizeBinary: function() {
			let optionType = $("input:radio[name=sizeBinary]:checked").val();
			
			if(optionType == "D") {
				$("#horiVerti").slideDown(150);
				$("#oneToOne").slideUp(150);
			} else if(optionType == "S") {
				$("#horiVerti").slideUp(150);
				$("#oneToOne").slideDown(150);
			}		
		},
		
		changeHorizontalOption: function() {
			let optionType = $("input:radio[name=horiOption]:checked").val();
			
			if(optionType == "S") {
				$("#horiFluid").slideUp(150);
				$("#horiFix").slideDown(150);
			} else if(optionType == "D") {
				$("#horiFix").slideUp(150);
				$("#horiFluid").slideDown(150);
			}
		},
		
		changeVerticalOption: function() {
			let optionType = $("input:radio[name=vertiOption]:checked").val();
			$("#verticalFluid").removeClass("d-none");
			
			if(optionType == "S") {
				$("#vertiFluid").slideUp(150);
				$("#vertiFix").slideDown(150);
			} else if(optionType == "D") {
				$("#vertiFix").slideUp(150);
				$("#vertiFluid").slideDown(150);
			}
		},
		
		clickTime: function(evo) {
			let dataId = evo.attr("data-week");
			let dayYn = $("#timeOption thead th[data-day=" + dataId + "] input").prop("checked");
			
			if(dayYn) {
				evo.toggleClass("pick");
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
		
		changeDayUseYn: function(evo) {
			let dayId = $(evo).closest("th").attr("data-day");
			if(evo.prop("checked")) {
				$("#timeBody td[data-week=" + dayId + "][data-src=modifyCpm]").removeClass("on");
			} else {
				$("#timeOption tbody td[data-week=" + dayId + "]").removeClass("pick");
				$("#timeOption tbody td[data-week=" + dayId + "]").addClass("on");
			}
		},
		
		changeCategory: function(evo) {
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
		
		calExposureTarget: function(evo) {
			let target_v = parseInt($("#exposureTarget").val());
			let amount = parseInt(evo.attr("data-val"));
			
			if(!target_v) {
				target_v = 0;
			}
			target_v = target_v + amount;
			if(target_v < 1000 || !target_v) {
				target_v = 1000;
			} else if(target_v > 10000) {
				target_v = 10000;
			}
			
			$("#exposureTarget").val(target_v);
			_event.changeExposureTarget();
		},
		
		calculatePrice: function() {
			let url_v = "/sg/demand/calculate/cpm";
			
			if(_validatePriceData(function(txt) {
				customModal.alert({
					"content" : txt,
				});
				$("#costBox").contents()[4].textContent = "";
				$("#price").removeAttr("data-price").next().hide();
			})) {
				let data_v = _getSaveData();
			
				comm.send(url_v, data_v, "POST", function(resp) {
					if(resp.result) {
						priceData = data_v;
						$("#price").attr("data-price", resp.price);
						$("#costBox").contents()[4].textContent = " ▶ 수정 금액 " + util.numberWithComma(resp.price) + "원";
					}	$("#price").next().show();
				});
			}
		},
		
		changeMaterialFile: function(evo) {
			let file = evo.get(0).files[0];
			let fileMsg_o = evo.siblings("p").children("span").empty();
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
				let screenSize = evo.attr("data-size");
				let size = screenSize.split("x");
				if(data.width == size[0] && data.height == size[1]) {
					if(type.startsWith("video")) {
						let playTime = $("input:radio[name=matTimeOption]:checked").val();
						if(fileUtil.floorDuration(data.duration) != parseFloat(playTime)) {
							evo.val("");
							fileMsg_o.text("재생 시간이 일치하지 않습니다.");
							return;
						}
						evo.attr("data-width", data.width);
						evo.attr("data-height", data.height);
						evo.attr("data-time", Math.floor(data.duration));
					}
					fileUtil.setUploadFile(file, evo.attr("id"));
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
		
		changeMaterialTime: function() {
			let materialType = $("input:radio[name=matOption]:checked").val();
			if(materialType == "VIDEO") {
				_clearAllFileData();
			}
		},
		
		moveSgDetail: function() {
			location.href = "/demand/campaign/sg/detail?id=" + urlParam.id;
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
				
				data_v.material_list = _getRemoveFileList();
				
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
					"content" : "입력되지 않은 항목이 있습니다. 모든 정보를 입력해주세요.",
				});;
			}
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
	
	function _commonDrawFile(elementId, fileItem, data) {
		let body_o = $("#" + elementId).attr({
			"data-id"	: fileItem.id,
			"data-type" : fileItem.file_type,
			"data-time" : fileItem.playtime,
		});
		if(fileItem.ratio_type == "V") {
			body_o.addClass("vertiSize");
		}
		let src = globalConfig.getS3Url() + fileItem.file_path;
		let btn_o = $("<button>").attr({
			"type"		: "button",
			"data-src"	: "modifyCpm",
			"data-act"	: "modalMaterial",
			"data-path" : src,
			"data-type"	: fileItem.file_type,
		});
		{
			let strong_o = $("<strong>").addClass("filetype")
			let file_o = "";
			if(fileItem.file_type == "IMAGE") {
				strong_o.addClass("type-img");
				file_o = $("<img>").attr({
					"src" : src
				});
			} else if(fileItem.file_type == "VIDEO") {
				strong_o.addClass("type-vid");
				file_o = $("<video>").attr({
					"src" : src
				});
				fileUtil.loadVideo(file_o);
			}
			btn_o.append(strong_o);
			btn_o.append(file_o);
		}
		body_o.append(btn_o);
		let div_o = $("<div>");
		{
			let p1_o = $("<p>").text(fileItem.file_name);
			let p2_o = $("<p>");
			let span1_o = $("<span>").text("0:" + data.exposure_time);
			let span2_o = $("<span>").text((fileItem.file_size / 1024 / 1024).toFixed(1) + " mb");
			
			p2_o.append(span1_o);
			p2_o.append(span2_o);
			div_o.append(p1_o);
			div_o.append(p2_o);
		}
		body_o.append(div_o);
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
			let pickLength = $("#timeOption tbody td[class=pick]").length;
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
	
	function _validateSgData() {
		let result = _validatePriceData();
	
		let sgName_v = $("#sgName").val();
		if(!sgName_v) {
			result = false;
		}
	
		let startYmd_v = $("#startYmd").val();
		if (!startYmd_v) {
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
		let materialKind = $("input:radio[name=matOption]:checked").val();
		let exposureTime = $("input:radio[name=matTimeOption]:checked").val();
	
		if (sizeBinary_v == "S") { // 1:1
			if (!$("#oneToOneExistFile").attr("data-id")) { // 기존 파일이 없는 경우
				let file_v = $("#oneFile1080").val();
				if (!file_v) {
					result = false;
				}
			}
		} else if (sizeBinary_v == "D") { // 가로/세로
			let hvCheck_o = $("input:checkbox[name=hvCheckBox]:checked");
			if (hvCheck_o.length == 0) {
				result = false;
			} else {
				if ($("#horizontalCheck").prop("checked")) { // 가로
					let horiOpt_v = $("input:radio[name=horiOption]:checked").val();
					if (horiOpt_v == "S") { // 선택 사이즈 노출
						let existFile_o = $("#horiFixExist1920");
						let file_v = $("#horiFixFile1920").val();
						// input파일이 없을 때
						if(!file_v) {
							// 기존 파일이 있는지 검사
							if (existFile_o.attr("data-type") != materialKind) {
								result = false;
							} else {
								if (materialKind == "VIDEO") {
									if (existFile_o.attr("data-time") != exposureTime) {
										result = false;
									}
								}
							}
						}					
	
						if ($("#matSizeOpt2").prop("checked")) { // 1600
							let existFile_o = $("#horiFixExist1600");
							let file_v = $("#horiFixFile1600").val();
							if(!file_v) {
								if (existFile_o.attr("data-type") != materialKind) {
									result = false;
								} else {
									if (materialKind == "VIDEO") {
										if (existFile_o.attr("data-time") != exposureTime) {
											result = false;
										}
									}
								}
							}
						}
	
						if ($("#matSizeOpt3").prop("checked")) { // 2560
							let existFile_o = $("#horiFixExist2560");
							let file_v = $("#horiFixFile2560").val();
							if(!file_v) {
								if (existFile_o.attr("data-type") != materialKind) {
									result = false;
								} else {
									if (materialKind == "VIDEO") {
										if (existFile_o.attr("data-time") != exposureTime) {
											result = false;
										}
									}
								}
							}
						}
					} else if (horiOpt_v == "D") { // 유동적 소
						let existFile_o = $("#horiFluidExist1920");
						let file_v = $("#horiFluidFile1920").val();
						if(!file_v) {
							if (existFile_o.attr("data-type") != materialKind) {
								result = false;
							} else {
								if (materialKind == "VIDEO") {
									if (existFile_o.attr("data-time") != exposureTime) {
										result = false;
									}
								}
							}
						}
					} else {
						reuslt = false;
					}
				}
	
				if ($("#verticalCheck").prop("checked")) { // 세로
					let vertiOpt_v = $("input:radio[name=vertiOption]:checked").val();
					if (vertiOpt_v == "S") { // 선택 사이즈 노출
	
						let existFile_o = $("#vertiFixExist1920");
						let file_v = $("#vertiFixFile1920").val();
	
						if(!file_v) {
							if (existFile_o.attr("data-type") != materialKind) {
								result = false;
							} else {
								if (materialKind == "VIDEO") {
									if (existFile_o.attr("data-time") != exposureTime) {
										result = false;
									}
								}
							}
						}
	
						if ($("#matSizeOpt4").prop("checked")) { // 1600
							let existFile_o = $("#vertiFixExist1600");
							let file_v = $("#vertiFixFile1600").val();
							if(!file_v) {
								if (existFile_o.attr("data-type") != materialKind) {
									result = false;
								} else {
									if (materialKind == "VIDEO") {
										if (existFile_o.attr("data-time") != exposureTime) {
											result = false;
										}
									}
								}
							}
						}
	
						if ($("#matSizeOpt5").prop("checked")) { // 2560
							let existFile_o = $("#vertiFixExist2560");
							let file_v = $("#vertiFixFile2560").val();
							if(!file_v) {
								if (existFile_o.attr("data-type") != materialKind) {
									result = false;
								} else {
									if (materialKind == "VIDEO") {
										if (existFile_o.attr("data-time") != exposureTime) {
											result = false;
										}
									}
								}
							}
						}
					} else if (vertiOpt_v == "D") { // 유동적 소재
						let existFile_o = $("#vertiFluidExist1920");
						let file_v = $("#vertiFluidFile1920").val();
						if(!file_v) {
							if (existFile_o.attr("data-type") != materialKind) {
								result = false;
							} else {
								if (materialKind == "VIDEO") {
									if (existFile_o.attr("data-time") != exposureTime) {
										result = false;
									}
								}
							}
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
	
	// 시간 옵션 시 schedule JSON 데이터 가져오기
	function _getTimeData() {
		let scheduleList = [];

		let week = $("#timeOption .day");
		let weekUseYn = $("#timeOption .day input");
		
		// 주 > 시간 만큼 반복
		for(let i=0; i<week.length; i++) {
			let weekData = {};
			let weekCode = $(week[i]).attr("data-day");
			weekData.week_code = weekCode;
			let useYn = "Y";
			if(!$(weekUseYn[i]).prop("checked")) {
				useYn = "N";
			}
			weekData.use_yn = useYn;
			weekData.schedule = [];
			
			let time = $("#timeOption tbody tr");
			let partTime = $("#timeOption tbody td[data-week=" + weekCode + "]");
			for(let j=0; j<time.length; j++) {
				let timeData = {};
				timeData.name = "hour_" + j.toString().padStart(2, 0);
				
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
			if($("#timeOption thead .day input").is(":checked")) {
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
		
		// --  광고 등록할 때 추가로 필요한 파라미터
		
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
					
			if($("#horizontalCheck").prop("checked")) { // 가로
				let horiOption_v = $("input:radio[name=horiOption]:checked").val();
				data.exposure_horizon_type = horiOption_v;
				if(horiOption_v == "S") { // 선택 사이즈에만 노출
					let selectFile1920_v = $("#horiFixFile1920").val();
					
					if(selectFile1920_v) {
						data.file_w_1920 = $("#horiFixFile1920").get(0).files[0];;
						if(materialKind_v == "VIDEO") {
							data.file_data_w_1920 = _getFileData("horiFixFile1920");
						}
					}
					
					if($("#matSizeOpt2").prop("checked")) {
						let selectFile1600_v = $("#horiFixFile1600").val();
						
						if(selectFile1600_v) {
							data.file_w_1600 = $("#horiFixFile1600").get(0).files[0];;
							if(materialKind_v == "VIDEO") {
								data.file_data_w_1600 = _getFileData("horiFixFile1600");
							}
						}
					}
					
					if($("#matSizeOpt3").prop("checked")) {
						let selectFile2560_v = $("#horiFixFile2560").val();
						
						if(selectFile2560_v) {
							data.file_w_2560 = $("#horiFixFile2560").get(0).files[0];
							if(materialKind_v == "VIDEO") {
								data.file_data_w_2560 = _getFileData("horiFixFile2560");
							}
						}
					}
				} else if(horiOption_v == "D") { // 유동적 소재 사용
					let selectFile1920_v = $("#horiFluidFile1920").val();
					
					if(selectFile1920_v) {
						data.file_w_1920 = $("#horiFluidFile1920").get(0).files[0];
						if(materialKind_v == "VIDEO") {
							data.file_data_w_1920 = _getFileData("horiFluidFile1920");
						}
					}
				}
			} else {
				data.exposure_horizon_type = "N";
			}
			
			if($("#verticalCheck").prop("checked")) { // 세로
				let vertiOption_v = $("input:radio[name=vertiOption]:checked").val();
				if(vertiOption_v == "S") { // 선택 사이즈에만 노출
					data.exposure_vertical_type = vertiOption_v;
				
					let selectFile1920_v = $("#vertiFixFile1920").val();
					
					if(selectFile1920_v) {
						data.file_h_1920 = $("#vertiFixFile1920").get(0).files[0];
						if(materialKind_v == "VIDEO") {
							data.file_data_h_1920 = _getFileData("vertiFixFile1920");
						}
					}
					
					if($("#matSizeOpt4").prop("checked")) {
						let selectFile1600_v = $("#vertiFixFile1600").val();
						
						if(selectFile1600_v) {
							data.file_h_1600 = $("#vertiFixFile1600").get(0).files[0];
							if(materialKind_v == "VIDEO") {
								data.file_data_h_1600 = _getFileData("vertiFixFile1600");
							}
						}
					}
					
					if($("#matSizeOpt5").prop("checked")) {
						let selectFile2560_v = $("#vertiFixFile2560").val();
						
						if(selectFile2560_v) {
							data.file_h_2560 = $("#vertiFixFile2560").get(0).files[0];
							if(materialKind_v == "VIDEO") {
								data.file_data_h_2560 = _getFileData("vertiFixFile2560");
							}
						}
					}
				} else if(vertiOption_v == "D") { // 유동적 소재 사용
					data.exposure_vertical_type = vertiOption_v;
				
					let selectFile1920_v = $("#vertiFluidFile1920").val();
					
					if(selectFile1920_v) {
						data.file_h_1920 = $("#vertiFluidFile1920").get(0).files[0];
						if(materialKind_v == "VIDEO") {
							data.file_data_h_1920 = _getFileData("vertiFluidFile1920");
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
		
		dev.log(data);
		return data;
	}
	
	function _getRemoveFileList() {
		let removeFileList = [];

		let sizeBinary_v = $("input:radio[name=sizeBinary]:checked").val();
		if(sizeBinary_v == "D") {
			removeFileList.push($("#oneToOneExistFile").attr("data-id"));
			
			if(!$("#horizontalCheck").prop("checked")) {
				removeFileList.push($("#horiFixExist1920").attr("data-id"));
				removeFileList.push($("#horiFixExist1600").attr("data-id"));
				removeFileList.push($("#horiFixExist2560").attr("data-id"));
				removeFileList.push($("#horiFluidExist1920").attr("data-id"));
			} else {
				let horiOption = $("input:radio[name=horiOption]:checked").val();	
				if(horiOption == "S") {
					removeFileList.push($("#horiFluidExist1920").attr("data-id"));
					if(!$("#matSizeOpt2").prop("checked")) {
						removeFileList.push($("#horiFixExist1600").attr("data-id"));
					}
					
					if(!$("#matSizeOpt3").prop("checked")) {
						removeFileList.push($("#horiFixExist2560").attr("data-id"));
					}
					
				} else if(horiOption == "D") {
					removeFileList.push($("#horiFixExist1920").attr("data-id"));
					removeFileList.push($("#horiFixExist1600").attr("data-id"));
					removeFileList.push($("#horiFixExist2560").attr("data-id"));
				}
			}
			
			if(!$("#verticalCheck").prop("checked")) {
				removeFileList.push($("#vertiFixExist1920").attr("data-id"));
				removeFileList.push($("#vertiFixExist1600").attr("data-id"));
				removeFileList.push($("#vertiFixExist2560").attr("data-id"));
				removeFileList.push($("#vertiFluidExist1920").attr("data-id"));
			} else {
				let vertiOption = $("input:Radio[name=vertiOption]:checked").val();
				if(vertiOption == "S") {
					removeFileList.push($("#vertiFluidExist1920").attr("data-id"));
					if(!$("#matSizeOpt4").prop("checked")) {
						removeFileList.push($("#vertiFixExist1600").attr("data-id"));
					}
					
					if(!$("#matSizeOpt5").prop("checked")) {
						removeFileList.push($("#vertiFixExist2560").attr("data-id"));
					}
					
				} else if(vertiOption == "D") {
					removeFileList.push($("#vertiFixExist1920").attr("data-id"));
					removeFileList.push($("#vertiFixExist1600").attr("data-id"));
					removeFileList.push($("#vertiFixExist2560").attr("data-id"));
				}
			}
			
			
		} else if(sizeBinary_v == "S") {
			removeFileList.push($("#horiFixExist1920").attr("data-id"));
			removeFileList.push($("#horiFixExist1600").attr("data-id"));
			removeFileList.push($("#horiFixExist2560").attr("data-id"));
			removeFileList.push($("#horiFluidExist1920").attr("data-id"));
			removeFileList.push($("#vertiFixExist1920").attr("data-id"));
			removeFileList.push($("#vertiFixExist1600").attr("data-id"));
			removeFileList.push($("#vertiFixExist2560").attr("data-id"));
			removeFileList.push($("#vertiFluidExist1920").attr("data-id"));
		}
		
		let materialType = $("input:radio[name=matOption]:checked").val();
		let playtime = $("input:radio[name=matTimeOption]:checked").val();
		
		$(".existingFile").each(function(index, item) {
			let dataId = $(item).attr("data-id");
			if(dataId) {
				if($(item).attr("data-type") != materialType) {
					removeFileList.push(dataId);
				} else {
					if(materialType == "VIDEO") {
						if($(item).attr("data-time") != playtime) { // 영상일 때만 가능
							removeFileList.push(dataId);
						}
					}
				}
				if($(item).next().children("input").val()) {
					removeFileList.push(dataId);
				}
			}
		});
		
		removeFileList = removeFileList.filter(function(item) {
			return item !== undefined;
		});
		dev.log(removeFileList);
		return removeFileList;
	}
	
	function _getFileData(fileId) {
		let fileData = JSON.stringify({
			"height" : $("#" + fileId).attr("data-height"),
			"width"	 : $("#" + fileId).attr("data-width"),
			"playtime"	: $("#" + fileId).attr("data-time"),
		});
		
		return fileData;
	}
	
	function _clearAllFileData() {
		$("#material input[type=file]").each(function(index, item) {
			$(item).val("");
			$("label[for='" + item.id + "']").text("파일을 선택하세요");
			$(item).siblings("p").children("span").text("");
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
	
	// datepicker 초기화
	function _setDatePicker(initDate) {
		let date = new Date();
		date.setDate(date.getDate() + 3);
		
		let option = {
			startDate: date,
	        todayHighlight: true,
		}
		
		customDatePicker.init("startYmd", option).datepicker("setDate", moment(initDate).format("YYYY-MM-DD"));
	}
	
	return {
		init
	}
})();
