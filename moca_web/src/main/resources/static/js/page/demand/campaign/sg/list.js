const sgList = (function () {
	
	let urlParam = util.getUrlParam();
	// 해당 페이지 초기화 함수
	function init(){
		_campaign.getDetail(null, _initTab);
		_evInit();
	}
	
	// 이벤트 초기화 
	function _evInit() {
		let evo = $("[data-src='list'][data-act]").off();
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
			if(act_v == "moveCampaignModify") {
				event.moveCampaignModify();
			} else if(act_v == "moveSgAdd") {
				event.moveSgAdd();
			} else if(act_v == "getSgList") {
				let status = $(evo).find("p").attr("data-status");
				_campaign.getDetail(status);
			} else if(act_v == "pauseSg") {
				event.modifySgStatus(1);
			} else if(act_v == "proceedSg") {
				event.modifySgStatus(2);
			} else if(act_v == "removeRequestSg") {
				event.removeSg(0);
			} else if(act_v == "removeRejectSg") {
				event.removeSg(9);
			} else if(act_v == "removeEndSg") {
				event.removeSg(8);
			} else if(act_v == "removeCampaign") {
				event.removeCampaign();
			} else if(act_v == "moveCampaignList") {
				event.moveCampaignList();
			}
		} else if(type_v == "change") {
			if(act_v == "changeAllCheckbox") {
				util.setCheckBox(evo);
			}
		}
	}
	
	const _campaign = {
		getDetail: function(status, callback) {
			let url_v = "/campaign/demand/detail";
			
			if(util.valNullChk(urlParam) || !urlParam.id || isNaN(urlParam.id)){ 
				location.href = "/demand/campaign/list";
				return;
			}
			
			let data_v = {
				campaign_id : urlParam.id,
			}
			
			if(status || status == 0) {
				data_v.status = status;
			}
			
			comm.send(url_v, data_v, "POST", function(resp) {
				if(resp.result) {
					dev.log(resp.data);
					let payType = resp.data.pay_type;
					if(typeof(callback) == "function") {
						callback(payType);
					}
					_campaign.drawDetail(resp.data);
					
					if(payType == "CPM") {
						if(status == 0) { // 승인 대기
							_sg.drawRequestCpmList(resp.data);
						} else if(status == 1 || status == 2) { // 진행중, 일시정지
							_sg.drawProceedPauseCpmList(resp.data, status);
						} else if(status == 8) { // 종료
							_sg.drawEndCpmList(resp.data);
						} else if(status == 9) { // 승인거부
							_sg.drawRejectList(resp.data);
						} else { // 전체
							_sg.drawAllCpmList(resp.data);
						}
					} else if(payType == "CPP") {
						if(status == 0) { // 승인 대기
							_sg.drawRequestCppList(resp.data);
						} else if(status == 1) { // 진행중
							_sg.drawProceedCppList(resp.data);
						} else if(status == 8) { // 종료
							_sg.drawEndCppList(resp.data);
						} else if(status == 9) { // 승인거부
							_sg.drawRejectList(resp.data);
						} else {
							_sg.drawAllCppList(resp.data);
						}
					}
				} else {
					location.href = "/demand/campaign/list";
				}
			});
		},
		
		drawDetail: function(data) {
			$("table thead input").prop("checked", false);	
			$("#campaignName").text(data.name);
			$("#payType").attr({
				"data-type" : data.pay_type, 
			}).text("과금 방식 : " + data.pay_type);
			if(data.pay_type == "CPM") {
				$("#pause").text("일시중지 : " + data.pause).attr({
					"data-cnt"	:	data.pause,
				});
				$("#secondInfo").text("전체 목표 노출 수 : " + util.numberWithComma(data.exposure_target_total));
				$("#thirdInfo").text("전체 총 노출 수 : " + util.numberWithComma(data.exposure_total_count));
			} else if(data.pay_type == "CPP") {
				$("#pauseTab").hide();
				$("#secondInfo").text("진행중인 상품 수 : " + data.proceed);
				$("#thirdInfo").text("총 집행 금액 : " + util.numberWithComma(data.total_price) +"원");
			}
			
			$("#total").text("전체 : " + data.total);
			$("#proceed").text("진행중 : " + data.proceed).attr({
				"data-cnt"	:	data.proceed,
			});
			$("#request").text("승인대기 : " + data.request);
			$("#reject").text("승인거부 : " + data.reject);
			$("#end").text("종료 : " + data.end);
		}
	}
	
	const _sg = {
		// CPM 전체 탭
		drawAllCpmList: function(data) {
			let list = data.list;
			let body_o = $("#allCpmListBody").empty();
			for(item of list) {
				let tr_o = $("<tr>");
				{
					// NO
					let td_o = $("<td>").text(item.seq);
					tr_o.append(td_o);
				}
				{
					// 상태
					let td_o = $("<td>");
					let p_o = $("<p>").addClass("stateBox");
					if(item.status == 0) { // 승인 대기
						p_o.addClass("state-delay").text("승인대기");
					} else if(item.status == 1) { // 진행중
						p_o.addClass("state-ongoing").text("진행중");
					} else if(item.status == 2) { //cpm 경우 일시정지
						p_o.addClass("state-delay").text("일시중지");
					} else if(item.status == 8 || item.status == 7){ // 종료
						p_o.addClass("state-end").text("종료");
					} else if(item.status == 9) { // 승인 거부
						p_o.addClass("state-end").text("승인거부");
					}
					td_o.append(p_o);
					tr_o.append(td_o);
				}
				{
					// 광고제목
					let td_o = $("<td>");
					let a_o = $("<a>").attr({
						"href" : "/demand/campaign/sg/detail?id=" + item.sg_id
					}).text(item.name);
					td_o.append(a_o);
					tr_o.append(td_o);
				}
				{
					// 광고 소재
					let td_o = $("<td>");
					// 가로 or 세로 or 1:1 (hori, verti, eq)
					let div_o = $("<div>").addClass("table-thumbWrap");
					if(item.ratio_type == "H") { // 가로
						div_o.addClass("hori");
					} else if(item.ratio_type == "V") { // 세로
						div_o.addClass("verti");
					} else if(item.ratio_type == "S") { // 1:1
						div_o.addClass("eq");
					}
					{
						// 단일 or 복수
						if(item.material_count > 1) {
							let i_o = $("<i>").addClass("overlap").text("+" + (item.material_count - 1));
							div_o.append(i_o);
						}
						// 이미지 or 영상
						let divFileType_o = $("<div>").addClass("filetype");
						let src = globalConfig.getS3Url() + item.file_path;
						let file_o = "";
						if(item.file_type == "IMAGE") {
							divFileType_o.addClass("type-img");
							file_o = $("<img>").attr({
								"src" : src
							});
						} else if(item.file_type == "VIDEO") {
							divFileType_o.addClass("type-vid");
							file_o = $("<video>").attr({
								"src" : src
							});
							fileUtil.loadVideo(file_o, null);
						}
						div_o.append(divFileType_o);
						div_o.append(file_o);
					}
					td_o.append(div_o);
					tr_o.append(td_o);
				}
				{
					// 시작일
					let td_o = $("<td>").text(item.start_ymd);
					tr_o.append(td_o);
				}
				{
					// 목표 노출 수
					let td_o = $("<td>").text(util.numberWithComma(item.exposure_target));
					tr_o.append(td_o);
				}
				{
					// 일 노출 제한
					let td_o = $("<td>").text(util.numberWithComma(item.exposure_limit));
					tr_o.append(td_o);
				}
				{
					// 금일 노출 수
					let td_o = $("<td>").text(util.numberWithComma(item.today_exposure_count));
					tr_o.append(td_o);
				}
				{
					// 미 노출 수
					let td_o = $("<td>").text(util.numberWithComma(item.remain_exposure_count));
					tr_o.append(td_o);
				}
				{
					// 총 노출 수
					let td_o = $("<td>").text(util.numberWithComma(item.total_exposure_count));
					tr_o.append(td_o);
				}
				body_o.append(tr_o);
			}
		},
		
		// 진행중 또는 일시중지 리스트 그리기
		drawProceedPauseCpmList: function(data, status) {
			let body_o = "";
			let checkboxName = "";
			if(status == 1) { // 진행중
				body_o = $("#proceedCpmListBody").empty();
				checkboxName = "proceedCpm";
			} else if(status == 2) { // 일시중지
				body_o = $("#pauseCpmListBody").empty();
				checkboxName = "pauseCpm";
			}
			let list = data.list;
			
			for(item of list) {
				let tr_o = $("<tr>");
				{
					// 체크박스
					let td_o = $("<td>");
					let span_o = $("<span>").addClass("chk");
					let input_o = $("<input>").attr({
						"type"		: "checkbox",
						"name"		: checkboxName,
						"id"		: checkboxName + item.sg_id,
						"data-id"	: item.sg_id,
					});
					let label_o = $("<label>").attr({
						"for"	: checkboxName + item.sg_id	
					});
					span_o.append(input_o);
					span_o.append(label_o);
					td_o.append(span_o);
					tr_o.append(td_o);
				}
				_commonDrawList(tr_o, item);
				{
					// 시작일
					let td_o = $("<td>").text(item.start_ymd);
					tr_o.append(td_o);
				}
				{
					// 목표 노출 수
					let td_o = $("<td>").text(util.numberWithComma(item.exposure_target));
					tr_o.append(td_o);
				}
				{
					// 일 노출 제한
					let td_o = $("<td>").text(util.numberWithComma(item.exposure_limit));
					tr_o.append(td_o);
				}
				{
					// 금일 노출 수
					let td_o = $("<td>").text(util.numberWithComma(item.today_exposure_count));
					tr_o.append(td_o);
				}
				{
					// 미 노출 수
					let td_o = $("<td>").text(util.numberWithComma(item.remain_exposure_count));
					tr_o.append(td_o);
				}
				{
					// 총 노출 수
					let td_o = $("<td>").text(util.numberWithComma(item.total_exposure_count));
					tr_o.append(td_o);
				}
				body_o.append(tr_o);
			}
		},
		
		// 승인대기 중인 광고 리스트 그리기
		drawRequestCpmList: function(data) {
			let body_o = $("#requestCpmListBody").empty();
			let list = data.list;
			
			for(item of list) {
				let tr_o = $("<tr>");
				{
					// 체크박스
					let td_o = $("<td>");
					let span_o = $("<span>").addClass("chk");
					let input_o = $("<input>").attr({
						"type"		: "checkbox",
						"name"		: "requestCpm",
						"id"		: "requestCpm" + item.sg_id,
						"data-id"	: item.sg_id,
					});
					let label_o = $("<label>").attr({
						"for"	: "requestCpm" + item.sg_id	
					});
					span_o.append(input_o);
					span_o.append(label_o);
					td_o.append(span_o);
					tr_o.append(td_o);
				}
				_commonDrawList(tr_o, item);
				{
					// 광고시작 요청일
					let td_o = $("<td>").text(item.start_ymd);
					tr_o.append(td_o);
				}
				{
					// 승인요청 일시
					let requestDate = item.request_date;
					if(requestDate) {
						requestDate = requestDate.replace(" ", "<br>");
					}
					let td_o = $("<td>").html(requestDate);
					tr_o.append(td_o);
				}
				{
					// 목표노출 수
					let td_o = $("<td>").text(util.numberWithComma(item.exposure_target));
					tr_o.append(td_o);
				}
				{
					// 일 노출 제한 설정 금액
					let td_o = $("<td>").text(util.numberWithComma(item.exposure_limit));
					tr_o.append(td_o);
				}
				body_o.append(tr_o);
			}
		},
		
		// 승인 거부 리스트 그리기
		drawRejectList: function(data) {
			let list = data.list;
			let body_o = "";
			let checkboxName = "";
			if(data.pay_type == "CPM") {
				body_o = $("#rejectCpmListBody").empty();
				checkboxName = "rejectCpm";
			} else if(data.pay_type == "CPP"){
				body_o = $("#rejectCppListBody").empty();
				checkboxName = "rejectCpp";
			}
			
			for(item of list) {
				let tr_o = $("<tr>");
				{
					// 체크박스
					let td_o = $("<td>");
					let span_o = $("<span>").addClass("chk");
					
					
					let input_o = $("<input>").attr({
						"type"		: "checkbox",
						"name"		: checkboxName,
						"id"		: checkboxName + item.sg_id,
						"data-id"	: item.sg_id,
					});
					let label_o = $("<label>").attr({
						"for"	: checkboxName + item.sg_id	
					});
					span_o.append(input_o);
					span_o.append(label_o);
					td_o.append(span_o);
					tr_o.append(td_o);
				}
				_commonDrawList(tr_o, item);
				{
					// 승인요청 일시
					let requestDate = item.request_date;
					if(requestDate) {
						requestDate = requestDate.replace(" ", "<br>");
					}
					let td_o = $("<td>").html(requestDate);
					tr_o.append(td_o);
				}
				{
					// 입금확인 일시
					let payDate = item.pay_date;
					let txt = "-";
					if(payDate) {
						txt = payDate.replace(" ", "<br>");
					}
					let td_o = $("<td>").html(txt);
					tr_o.append(td_o);
				}
				{
					// 승인거부 일시
					let rejectDate = item.approve_date;
					if(rejectDate) {
						rejectDate = rejectDate.replace(" ", "<br>");
					}
					let td_o = $("<td>").html(rejectDate);
					tr_o.append(td_o);
				}
				{
					// 승인거부 사유
					let td_o = $("<td>").html(item.reject_reason);
					tr_o.append(td_o);
				}
				body_o.append(tr_o);
			}
		},
		
		drawEndCpmList: function(data) {
			let list = data.list;
			let body_o = $("#endCpmListBody").empty();
			
			for(item of list) {
				let tr_o = $("<tr>");
				{
					// 체크박스
					let td_o = $("<td>");
					let span_o = $("<span>").addClass("chk");
					let input_o = $("<input>").attr({
						"type"		: "checkbox",
						"name"		: "endCpm",
						"id"		: "endCpm" + item.sg_id,
						"data-id"	: item.sg_id,
					});
					let label_o = $("<label>").attr({
						"for"	: "endCpm" + item.sg_id	
					});
					span_o.append(input_o);
					span_o.append(label_o);
					td_o.append(span_o);
					tr_o.append(td_o);
				}
				_commonDrawList(tr_o, item);
				{
					// 시작일
					let td_o = $("<td>").text(item.start_ymd);
					tr_o.append(td_o);
				}
				{
					// 종료 시점
					let date = item.display_end_date
					if(item.status == 7) {
						date = item.stop_date;
					}
					if(date) {
						date = date.replace(" ", "<br>");
					}
					let td_o = $("<td>").html(date);
					tr_o.append(td_o);
				}
				{
					// 목표 노출 수
					let td_o = $("<td>").text(util.numberWithComma(item.exposure_target));
					tr_o.append(td_o);
				}
				{
					// 총 노출 수
					let td_o = $("<td>").text(util.numberWithComma(item.total_exposure_count));
					tr_o.append(td_o);
				}
				{
					// 종료 사유
					let txt = "";
					if(item.status == 7) {
						txt = "긴급종료<br>시행자 : " + item.stop_member_uid + "<br>";
					}
					if(item.stop_reason) {
						txt += item.stop_reason;
					} else {
						txt += "-";
					}		
					let td_o = $("<td>").html(txt);
					tr_o.append(td_o);					
				}
				body_o.append(tr_o);
			}
		},
		
		drawAllCppList: function(data) {
			let list = data.list;
			let body_o = $("#allCppListBody").empty();
			
			for(item of list) {
				let tr_o = $("<tr>");
				{
					// NO
					let td_o = $("<td>").text(item.seq);
					tr_o.append(td_o);
				}
				{
					// 상태
					let td_o = $("<td>");
					let p_o = $("<p>").addClass("stateBox");
					if(item.status == 0) { // 승인 대기
						p_o.addClass("state-delay").text("승인대기");
					} else if(item.status == 1) { // 진행중
						p_o.addClass("state-ongoing").text("진행중");
					} else if(item.status == 8 || item.status == 7){ // 종료
						p_o.addClass("state-end").text("종료");
					} else if(item.status == 9) { // 승인 거부
						p_o.addClass("state-end").text("승인거부");
					}
					td_o.append(p_o);
					tr_o.append(td_o);
				}
				{
					// 광고제목
					let td_o = $("<td>");
					let a_o = $("<a>").attr({
						"href" : "/demand/campaign/sg/detail?id=" + item.sg_id
					}).text(item.name);
					td_o.append(a_o);
					tr_o.append(td_o);
				}
				{
					// 광고 소재
					let td_o = $("<td>");
					// 가로 or 세로 or 1:1 (hori, verti, eq)
					let div_o = $("<div>").addClass("table-thumbWrap");
					if(item.ratio_type == "H") { // 가로
						div_o.addClass("hori");
					} else if(item.ratio_type == "V") { // 세로
						div_o.addClass("verti");
					} else if(item.ratio_type == "S") { // 1:1
						div_o.addClass("eq");
					}
					{
						// 단일 or 복수
						if(item.material_count > 1) {
							let i_o = $("<i>").addClass("overlap").text("+" + (item.material_count - 1));
							div_o.append(i_o);
						}
						// 이미지 or 영상
						let divFileType_o = $("<div>").addClass("filetype");
						let src = globalConfig.getS3Url() + item.file_path;
						let file_o = "";
						if(item.file_type == "IMAGE") {
							divFileType_o.addClass("type-img");
							file_o = $("<img>").attr({
								"src" : src
							});
						} else if(item.file_type == "VIDEO") {
							divFileType_o.addClass("type-vid");
							file_o = $("<video>").attr({
								"src" : src
							});
							fileUtil.loadVideo(file_o, null);
						}
						div_o.append(divFileType_o);
						div_o.append(file_o);
					}
					td_o.append(div_o);
					tr_o.append(td_o);
				}
				{
					// 진행 상품
					let td_o = $("<td>").text(item.product_name);
					tr_o.append(td_o);
				}
				{
					// 시작일 ~ 종료일
					let td_o = $("<td>").html(item.start_ymd + "<br>~<br>" + item.end_ymd);
					tr_o.append(td_o);
				}
				{
					// 집행 금액
					let td_o = $("<td>").text(util.numberWithComma(item.price));
					tr_o.append(td_o);
				}
				body_o.append(tr_o);
			}
		},
		
		drawProceedCppList: function(data) {
			let list = data.list;
			let body_o = $("#proceedCppListBody").empty();
			
			for(item of list) {
				let tr_o = $("<tr>");
				_commonDrawList(tr_o, item);
				{
					// 진행 상품
					let td_o = $("<td>").text(item.product_name);
					tr_o.append(td_o);
				}
				{
					// 시작일 ~ 종료일
					let td_o = $("<td>").html(item.start_ymd + "<br>~<br>" + item.end_ymd);
					tr_o.append(td_o);
				}
				{
					// 집행 금액
					let td_o = $("<td>").text(util.numberWithComma(item.price));
					tr_o.append(td_o);
				}
				body_o.append(tr_o);
			}
		},
		
		// CPP 승인대기
		drawRequestCppList: function(data) {
			let list = data.list;
			let body_o = $("#requestCppListBody").empty();
			
			for(item of list) {
				let tr_o = $("<tr>");
				{
					// 체크박스
					let td_o = $("<td>");
					let span_o = $("<span>").addClass("chk");
					let input_o = $("<input>").attr({
						"type"		: "checkbox",
						"name"		: "requestCpp",
						"id"		: "requestCpp" + item.sg_id,
						"data-id"	: item.sg_id,
					});
					let label_o = $("<label>").attr({
						"for"	: "requestCpp" + item.sg_id	
					});
					span_o.append(input_o);
					span_o.append(label_o);
					td_o.append(span_o);
					tr_o.append(td_o);
				}
				_commonDrawList(tr_o, item);
				{
					// 진행 상품
					let td_o = $("<td>").text(item.product_name);
					tr_o.append(td_o);
				}
				{
					// 시작일 ~ 종료일
					let td_o = $("<td>").html(item.start_ymd + "<br>~<br>" + item.end_ymd);
					tr_o.append(td_o);
				}
				{
					// 승인 요청 일시
					let requestDate = item.request_date;
					if(requestDate) {
						requestDate = requestDate.replace(" ", "<br>");
					}
					let td_o = $("<td>").html(requestDate);
					tr_o.append(td_o);
				}
				body_o.append(tr_o);
			}
		},
		
		// CPP 종료
		drawEndCppList: function(data) {
			let list = data.list;
			let body_o = $("#endCppListBody").empty();
			
			for(item of list) {
				let tr_o = $("<tr>");
				{
					// 체크박스
					let td_o = $("<td>");
					let span_o = $("<span>").addClass("chk");
					let input_o = $("<input>").attr({
						"type"		: "checkbox",
						"name"		: "endCpp",
						"id"		: "endCpp" + item.sg_id,
						"data-id"	: item.sg_id,
					});
					let label_o = $("<label>").attr({
						"for"	: "endCpp" + item.sg_id	
					});
					span_o.append(input_o);
					span_o.append(label_o);
					td_o.append(span_o);
					tr_o.append(td_o);
				}
				_commonDrawList(tr_o, item);
				{
					// 진행 상품
					let td_o = $("<td>").text(item.product_name);
					tr_o.append(td_o);
				}
				{
					// 시작일 ~ 종료일
					let td_o = $("<td>").html(item.start_ymd + "<br>~<br>" + item.end_ymd);
					tr_o.append(td_o);
				}
				{
					// 진행기간
					let endYmd = item.end_ymd;
					if(item.status == 7) { // 긴급종료 경우
						endYmd = item.stop_ymd;
					}
					let start = new Date(item.start_ymd);
					let end = new Date(endYmd);
					let diffDay = Math.abs((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24));
					let td_o = $("<td>").text((diffDay + 1) + "일");
					tr_o.append(td_o);
				}
				{
					// 집행 금액
					let td_o = $("<td>").text(util.numberWithComma(item.price));
					tr_o.append(td_o);
				}
				{
					// 종료 사유
					let txt = "";
					if(item.status == 7) {
						txt = "긴급종료<br>시행자 : " + item.stop_member_uid + "<br>";
					}
					if(item.stop_reason) {
						txt += item.stop_reason;
					} else {
						txt += "-";
					}
					let td_o = $("<td>").html(txt);
					tr_o.append(td_o);
				}
				body_o.append(tr_o);
			}
		}
	}
	
	// 이벤트 담당
	const _event = {
		// 캠페인 내 광고 리스트로 이동		
		moveCampaignModify: function() {
			location.href = "/demand/campaign/modify?id=" + urlParam.id;;
		},
		
		// 광고 등록 페이지로 이동
		moveSgAdd: function() {
			let payType = $("#payType").attr("data-type");
			if(payType == "CPM") {
				location.href = "/demand/campaign/sg/addcpm?id=" + urlParam.id;;
			} else if(payType == "CPP") {
				location.href = "/demand/campaign/sg/addcpp?id=" + urlParam.id;;
			}
		},
		
		// cpm 광고 진행상태 변경(일시중지/진행중)
		modifySgStatus: function(currentStatus) {
			let url_v = "/sg/demand/modifyStatus";
			
			let checkboxName = "pauseCpm";
			let modifyStatus = "1";
			if(currentStatus == 1) {
				checkboxName = "proceedCpm";
				modifyStatus = 2;
			}
			
			let data_v = {
				status : modifyStatus
			};
			
			let sgIdList = [];
			let checkList = $("input[type=checkbox][name=" + checkboxName + "]:checked");
			checkList.each(function() {
				sgIdList.push($(this).attr("data-id"));
			});
			
			if(sgIdList.length < 1) {
				return;
			}
			data_v.sg_id_list = sgIdList;
			
			comm.send(url_v, data_v, "POST", function(resp) {
				if(resp.result) {
					_campaign.getDetail(currentStatus);
				}
			});
		},
		
		// 캠페인 내 광고리스트 삭제
		removeSg: function(status) {
			let url_v = "/sg/demand/remove";
			let payType = $("#payType").attr("data-type");
			
			let data_v = {
				pay_type : payType,
			};
			let checkboxName = "";
			
			if(payType == "CPM") {
				if(status == 0) {
					checkboxName = "requestCpm";
				} else if(status == 9) {
					checkboxName = "rejectCpm";
				} else if(status == 8) {
					checkboxName = "endCpm";
				}
			} else if(payType == "CPP") {
				if(status == 0) {
					checkboxName = "requestCpp";
				} else if(status == 9) {
					checkboxName = "rejectCpp";
				} else if(status == 8) {
					checkboxName = "endCpp";
				}
			}
			
			let sgIdList = [];
			let checkList = $("input[type=checkbox][name=" + checkboxName + "]:checked");
			checkList.each(function() {
				sgIdList.push($(this).attr("data-id"));
			});
			
			if(sgIdList.length < 1) {
				return;
			}
			data_v.sg_id_list = sgIdList;
			
			let confirmTxt = "광고가 삭제됩니다. 계속 하시겠습니까?";
			if(status == 0) {
				confirmTxt = "요청하신 광고 승인이 취소되고 해당 광고를 삭제합니다.";
			}
			
			customModal.confirm({
				"content" : confirmTxt,
				"confirmText" : "삭제",
				"confirmCallback" : function() {
					comm.send(url_v, data_v, "POST", function(resp) {
						if(resp.result) {
							_campaign.getDetail(status);
						}
					});
				}
			});
		},
		
		removeCampaign: function() {
			let proceed = parseInt($("#proceed").attr("data-cnt"));
//			let pause = parseInt($("#pause").attr("data-cnt"));
			if(proceed < 1) {
				let url_v = "/campaign/demand/remove";
	
				let data_v = {};
				let campaignIdList = [String(urlParam.id)];
				data_v.campaign_id_list = campaignIdList;
				
				customModal.confirm({
					"content" : "캠페인이 삭제됩니다. 계속 하시겠습니까?",
					"confirmText" : "삭제",
					"confirmCallback" : function() {
						comm.send(url_v, data_v, "POST", function(resp) {
							if(resp.result) {
								location.href = "/demand/campaign/list";
							}
						});
					}
				});
			} else {
				customModal.alert({
					"content" : "광고가 진행중인 캠페인은 삭제하실 수 없습니다",
				});
			}
		},
		
		moveCampaignList: function() {
			location.href = "/demand/campaign/list";
		}
	}
	
	function _commonDrawList(tr_o, item) {
		{
			// NO
			let td_o = $("<td>").text(item.seq);
			tr_o.append(td_o);
		}
		{
			// 광고 제목
			let td_o = $("<td>");
			let a_o = $("<a>").attr({
				"href" : "/demand/campaign/sg/detail?id=" + item.sg_id	
			}).text(item.name);
			td_o.append(a_o);
			tr_o.append(td_o);
		}
		{
			// 광고 소재
			let td_o = $("<td>");
			// 가로 or 세로 or 1:1 (hori, verti, eq)
			let div_o = $("<div>").addClass("table-thumbWrap");
			if(item.ratio_type == "H") { // 가로
				div_o.addClass("hori");
			} else if(item.ratio_type == "V") { // 세로
				div_o.addClass("verti");
			} else if(item.ratio_type == "S") { // 1:1
				div_o.addClass("eq");
			}
			{
				// 단일 or 복수
				if(item.material_count > 1) {
					let i_o = $("<i>").addClass("overlap").text("+" + (item.material_count - 1));
					div_o.append(i_o);
				}
				// 이미지 or 영상
				let divFileType_o = $("<div>").addClass("filetype");
				let src = globalConfig.getS3Url() + item.file_path;
				let file_o = "";
				if(item.file_type == "IMAGE") {
					divFileType_o.addClass("type-img");
					file_o = $("<img>").attr({
						"src" : src
					});
				} else if(item.file_type == "VIDEO") {
					divFileType_o.addClass("type-vid");
					file_o = $("<video>").attr({
						"src" : src
					});
					
					fileUtil.loadVideo(file_o, null);
				}
				
				div_o.append(divFileType_o);
				div_o.append(file_o);
			}
			td_o.append(div_o);
			tr_o.append(td_o);
		}
	}
	
	function _initTab(payType) {
		if(payType == "CPM") {
			$("#menuTab a").each(function(index, item) {
				$(item).attr({
					"href" : "#advCampTabs" + (index + 1)
				});
			});
			$("#advCampTabs1").addClass("show active");
		} else if(payType == "CPP") {
			$("#menuTab li").not("li#pauseTab").each(function(index, item) {
				$(item).children().attr({
					"href" : "#advCppCampTabs" + (index + 1),
				});
			});
			$("#advCppCampTabs1").addClass("show active");
		}
	}
	
	return {
		init
	}
	
})();