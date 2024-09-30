const sgDetail = (function () {
	
	let urlParam = util.getUrlParam();
	// 해당 페이지 초기화 함수
	function init(){
		_time.getList();
		_sg.getDetail();
	}
	
	// 이벤트 초기화 
	function _evInit() {
		let evo = $("[data-src='detail'][data-act]").off();
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
			if(act_v == "modifySg") {
				event.modifySg(evo);
			} else if(act_v == "showMaterial") {
				event.showMaterial(evo);
			} else if(act_v == "moveSgList") {
				event.moveSgList();
			} else if(act_v == "hideMaterial") {
				event.hideMaterial();
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
					_evInit();
				} else {
					location.href = "/demand/campaign/list";
				}
			});
		},
		
		drawDetail: function(data) {
			urlParam.campaignId = data.campaign_id;

			let status = "[승인 대기] ";
			if(data.status == 0) {
				if(data.pay_status_code == "PAY_WAIT") {
					$("#sgModify").show();
				}
			} else if(data.status == 1){
				status = "[진행중] ";
			} else if(data.status == 2) {
				status = "[일시 중지] ";
			} else if(data.status == 8 || data.status == 7) {
				status = "[종료] ";
			} else if(data.status == 9) {
				status = "[승인 거부] ";
				$("#sgModify").show();
			}
			$("#sgName").text(status + data.name);
			
			let payType = "CPP";
			if(!data.ssp_product) {
				payType = "CPM";
			}
			
			$("#sgModify").attr({
				"data-type"	 	: payType,
				"data-status"	: data.status,
				"data-pay"		: data.pay_status_code
			});
			
			$("#category").text("대분류 - " + data.main_category_name + " / 중분류 - " + data.middle_category_name + " / 소분류 - " + data.sub_category_name);
			let body_o = $("#materialList");
			
			if(payType == "CPM"){ // CPM
				$(".payTypeCpp").hide();
				$("#typeCategory").text("광고업종 카테고리");
				$("#typeDate").text("광고 시작일");
				$("#sgDate").text(data.start_ymd);
				
				if(data.target_area_yn == "Y") { // 노출 옵션 지역
					$("#targetOption").text("지역");
					$("#areaOption").show();
					
					let area = data.sg_area;
					$("#siArea").append($("<option>").text(area.si_name).val(area.area_id));
					$("#siArea").selectpicker("refresh");
					$("#siArea").selectpicker("val", area.area_id);
					$("#siArea").prop('disabled', true);
					$("#siArea").selectpicker("refresh");
					
					$("#guArea").append($("<option>").text(area.gu_name).val(area.area_id));
					$("#guArea").selectpicker("refresh");
					$("#guArea").selectpicker("val", area.area_id);
					$("#guArea").prop('disabled', true);
					$("#guArea").selectpicker("refresh");
					
				} else if(data.target_week_yn == "Y") { // 시간
					$("#targetOption").text("시간");
					$("#weekOption").show();
					
					let scheduleList = data.sg_schedule_list;
					let week = $("#scheduleTable thead .day input")
					for(let i=0; i<week.length ; i++) {
						if(scheduleList[i].use_yn == "Y") {
							$(week[i]).prop("checked", true);
						} else if(scheduleList[i].use_yn == "N") {
							$(week[i]).prop("checked", false);
						}
						
						for(let j=0; j<24; j++) {
							let keyHour = "hour_" + j.toString().padStart(2, 0)
							if(scheduleList[i].use_yn == "N") {
								$($($("tbody tr")[j]).children("td")[i]).addClass("on");
							}
							
							if(scheduleList[i][keyHour] == 1) {
								let td_o = $($($("tbody tr")[j]).children("td")[i]);
								if(!td_o.hasClass("on")) {
									td_o.addClass("pick");
								}
							}
						}
					}
				} else {
					$("#targetOption").text("사용 안함");					
				}
				
				if(data.material_kind == "IMAGE") {
					$("#materialKind").text("이미지");
				} else if(data.material_kind == "VIDEO") {
					$("#materialKind").text("동영상");
				}
				
				$("#exposureTime").text(data.exposure_time + "초");
				$("#exposureTarget").text(util.numberWithComma(data.exposure_target) + "회");
				$("#exposureLimit").text(util.numberWithComma(data.exposure_limit) + "회");
				
				let txt = "";
				if(data.material_ratio == "S") { // 1:1
					txt = "1:1";
				} else if(data.material_ratio == "D") { // 가로/세로
					txt = "가로/세로 : 가로-";
					if(data.exposure_horizon_type == "N") {
						txt += "사용 안함 / 세로-"
					} else if(data.exposure_horizon_type == "S") {
						txt += "선택 사이즈에만 노출 / 세로-";
					} else if(data.exposure_horizon_type == "D") {
						txt += "유동적 소재 사용 / 세로-";
					}
					
					if(data.exposure_vertical_type == "N") {
						txt += "사용 안함";
					} else if(data.exposure_vertical_type == "S") {
						txt += "선택 사이즈에만 노출";
					} else if(data.exposure_vertical_type == "D") {
						txt += "유동적 소재 사용";
					}
				}
				let p_o = $("<p>").text(txt);
				body_o.append(p_o);
			} else if(payType == "CPP") { // CPP
				$(".payTypeCpm").hide();
				$("#typeCategory").text("카테고리");
				$("#typeDate").text("광고 진행기간");
				$("#sgDate").text(data.start_ymd + " ~ " + data.end_ymd);
				
				$("#productName").text(data.ssp_product.product_name);
				
				$("#price").text(util.numberWithComma(data.price) + " 원");
				let span_o = $("<span>").html("<i>※</i> VAT 별도");
				
				$("#price").append(span_o);
				
			}
			// 광고 소재 그리기 (cpp, cpm 동일)
			let fileList = data.sg_material_list;
			for(item of fileList) {
				let file_o = $("<div>").addClass("existingFile");
				if(fileList.ratio_type == "V") {
					file_o.addClass("vertiSize");
				}
				{
					let src = globalConfig.getS3Url() + item.file_path;
					let btn_o = $("<button>").attr({
						"type"	: "button",
						"data-src"	: "detail",
						"data-act"	: "showMaterial",
						"data-path"	: src,
						"data-type" : item.file_type,
					});
					
					let strong_o = $("<strong>");
					let img_o = "";
					if(item.file_type == "IMAGE") {
						strong_o.addClass("filetype type-img");
						img_o = $("<img>").attr({
							"src"	: src,
						});
					} else if(item.file_type == "VIDEO") {
						strong_o.addClass("filetype type-vid");
						img_o = $("<video>").attr({
							"src"	: src,
						});
						fileUtil.loadVideo(img_o, null);
					}
					btn_o.append(strong_o);
					btn_o.append(img_o);
					file_o.append(btn_o);
				}
				{
					let div_o = $("<div>");
					let p1_o = $("<p>").text(item.size_descriptione);
					if((data.exposure_horizon_type == "D" && item.ratio_type == "H") || data.exposure_vertical_type == "D" && item.ratio_type == "V") {
						let span_o = $("<span>").html("&nbsp;유동적 소재 사용").addClass("colorRed");
						p1_o.append(span_o);
					}
					
					let p2_o = $("<p>").text(item.file_name);
					let p3_o = $("<p>");
					let span1_o = $("<span>").text("0:" + data.exposure_time);
					let span2_o = $("<span>").text((item.file_size / 1024 / 1024).toFixed(1) + " mb");
					
					p3_o.append(span1_o);
					p3_o.append(span2_o);
					div_o.append(p1_o);
					div_o.append(p2_o);
					div_o.append(p3_o);
					file_o.append(div_o);
				}
				body_o.append(file_o);
			}
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
	}
	
	const _event = {
		modifySg: function(evo) {
			let payType = evo.attr("data-type");
			let status = evo.attr("data-status");
			let payStatus = evo.attr("data-pay");
			
			let txt = "";
			let result = true;
			if(status == 1) {
				txt = "진행중인 광고는 수정할 수 없습니다";
				result = false;
			} else if((status == 0 && payStatus == "PAY_COMPLETE") || status == 2) {
				txt = "신청하신 광고의 입금이 완료되어<br>수정할 수 없습니다.";
				result = false;
			}
			
			if(!result) {
				customModal.alert({
					"content" : txt,
				});
			} else {
				if(payType == "CPM") {
					location.href = "/demand/campaign/sg/modifycpm?id=" + urlParam.id;
				} else if(payType == "CPP") {
					location.href = "/demand/campaign/sg/modifycpp?id=" + urlParam.id;
				}
			}
		},
		
		moveSgList: function() {
			location.href = "/demand/campaign/sg/list?id=" + urlParam.campaignId;
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
		}
	}
	
	return {
		init
	}
	
})();