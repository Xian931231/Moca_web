const demandMain = (function () {
	
	// 해당 페이지 초기화 함수
	function init(){
		_campaign.getList(1, "EA");
	}
	
	// 이벤트 초기화 
	function _evInit() {
		let evo = $("[data-src='demandMain'][data-act]").off();
		evo.on("click", function(ev){
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
			if(act_v == "clickTab") {
				event.clickTab(evo);
			} else if(act_v == "clickSort") {
				event.clickSort(evo);
			} else if(act_v == "moveReport") {
				event.moveReport(evo);
			}
		}
	}
	
	const _campaign = {
		getList: function(status, sortType) {
			let url_v = "/sg/demand/list";
			
			let data_v = {
				status		: status,
				sort_type	: sortType,
			};

			comm.send(url_v, data_v, "POST", function(resp) {
				if(resp.result) {
					dev.log(resp);
					_campaign.drawCount(resp.data);
					
					let tabId = $("#sgTab .active span").attr("id");
					$(".notnotnot").remove();					
					if(tabId == "exposure") {
						_sg.drawExposureList(resp.data);
					} else if(tabId == "price") {
						_sg.drawPriceList(resp.data);
					} else if(tabId == "proceed") {
						_sg.drawProceedList(resp.data);
					} else if(tabId == "request") {
						_sg.drawRequestList(resp.data);
					}
					_evInit();	
				}
			});
		},
		
		drawCount: function(data) {
			$("#standardDate").text(moment().subtract(1, "days").format("YYYY-MM-DD") + "일 기준");
			$("#exposure").text(util.numberWithComma(data.exposure_total));
			$("#price").text(util.numberWithComma(data.price_total));
			$("#proceed").html("<i>" + data.soon_end_count + "</i> / " + data.proceed);
			$("#request").text(util.numberWithComma(data.request));
		},
	}
	
	const _sg = {
		// 총 노출 수 탭
		drawExposureList: function(data) {
			let body_o = $("#exposureBody").empty();
			let list = data.list;
			if(list.length == 0) {
				let div_o = $("<div>").addClass("notnotnot").text("진행중인 광고가 없습니다.");
				$("#advTabs1 .tableWrap").append(div_o);
				return;
			} 
			for(let item of list) {
				let tr_o = $("<tr>");
				{
					// NO
					let td_o = $("<td>").text(item.seq);
					tr_o.append(td_o);
				}
				{
					// 과금방식
					let td_o = $("<td>").text(item.pay_type);
					tr_o.append(td_o);
				}
				{
					// 캠페인명
					let td_o = $("<td>");
					let a_o = $("<a>").attr({
						"href" : "/demand/campaign/sg/list?id=" + item.campaign_id,
					}).text(item.campaign_name);
					td_o.append(a_o);
					tr_o.append(td_o);
				}
				{
					// 광고명
					let td_o = $("<td>");
					let a_o = $("<a>").attr({
						"href" : "/demand/campaign/sg/detail?id=" + item.sg_id,
					}).text(item.sg_name);
					td_o.append(a_o);
					tr_o.append(td_o);
				}
				{
					// 리포트
					let td_o = $("<td>");
					let button_o = $("<button>").attr({
						"type" : "button",
						"data-src" : "demandMain",
						"data-act" : "moveReport",
					}).addClass("btn-report");
					
					if(item.exposure_count > 0) {
						button_o.attr({
							"data-cid" : item.campaign_id,
							"data-sid" : item.sg_id,
						});
					}
					let img_o = $("<img>").attr({
						"src" : "/assets/imgs/icon_report.png",
					});
					button_o.append(img_o);
					td_o.append(button_o);
					tr_o.append(td_o);
				}
				{
					// 노출 수
					let td_o = $("<td>").text(item.exposure_count);
					tr_o.append(td_o);
				}
				body_o.append(tr_o);
			}
		},
		
		// 총 집행금액 탭
		drawPriceList: function(data) {
			let body_o = $("#priceBody").empty();
			let list = data.list;
			if(list.length == 0) {
				let div_o = $("<div>").addClass("notnotnot").text("진행중인 광고가 없습니다.");
				$("#advTabs2 .tableWrap").append(div_o);
				return;
			} 
			for(let item of list) {
				let tr_o = $("<tr>");
				{
					// NO
					let td_o = $("<td>").text(item.seq);
					tr_o.append(td_o);
				}
				{
					// 과금방식
					let td_o = $("<td>").text(item.pay_type);
					tr_o.append(td_o);
				}
				{
					// 광고명
					let td_o = $("<td>");
					let a_o = $("<a>").attr({
						"href" : "/demand/campaign/sg/detail?id=" + item.sg_id,
					}).text(item.sg_name);
					td_o.append(a_o);
					tr_o.append(td_o);
				}
				{
					// 리포트
					let td_o = $("<td>");
					let button_o = $("<button>").attr({
						"type" : "button",
						"data-src" : "demandMain",
						"data-act" : "moveReport",
					}).addClass("btn-report");
					
					if(item.exposure_count > 0) {
						button_o.attr({
							"data-cid" : item.campaign_id,
							"data-sid" : item.sg_id,
						});
					}
					let img_o = $("<img>").attr({
						"src" : "/assets/imgs/icon_report.png",
					});
					button_o.append(img_o);
					td_o.append(button_o);
					tr_o.append(td_o);
				}
				{
					// 집행금액
					let td_o = $("<td>").text(util.numberWithComma(item.price));
					tr_o.append(td_o);
				}
				body_o.append(tr_o);
			}
		},
		
		// 진행중인 광고 탭
		drawProceedList: function(data) {
			let body_o = $("#proceedBody").empty();
			let list = data.list;
			if(list.length == 0) {
				let div_o = $("<div>").addClass("notnotnot").text("진행중인 광고가 없습니다.");
				$("#advTabs3 .tableWrap").append(div_o);
				return;
			} 
			for(let item of list) {
				let tr_o = $("<tr>");
				{
					// NO
					let td_o = $("<td>").text(item.seq);
					tr_o.append(td_o);
				}
				{
					// 과금방식
					let td_o = $("<td>").text(item.pay_type);
					tr_o.append(td_o);
				}
				{
					// 캠페인명
					let td_o = $("<td>");
					let a_o = $("<a>").attr({
						"href" : "/demand/campaign/sg/list?id=" + item.campaign_id,
					}).text(item.campaign_name);
					td_o.append(a_o);
					tr_o.append(td_o);
				}
				{
					// 광고명
					let td_o = $("<td>");
					let a_o = $("<a>").attr({
						"href" : "/demand/campaign/sg/detail?id=" + item.sg_id,
					}).text(item.sg_name);
					td_o.append(a_o);
					tr_o.append(td_o);
				}
				{
					// 리포트
					let td_o = $("<td>");
					let button_o = $("<button>").attr({
						"type" : "button",
						"data-src" : "demandMain",
						"data-act" : "moveReport",
					}).addClass("btn-report");
					
					if(item.exposure_count > 0) {
						button_o.attr({
							"data-cid" : item.campaign_id,
							"data-sid" : item.sg_id,
						});
					}
					let img_o = $("<img>").attr({
						"src" : "/assets/imgs/icon_report.png",
					});
					button_o.append(img_o);
					td_o.append(button_o);
					tr_o.append(td_o);
				}
				{
					// 진행상황
					let td_o = $("<td>");
					let span_o = $("<span>");
					let txt = "";
					if(item.pay_type == "CPM") {
						txt = item.exposure_count + "/" + item.exposure_target + "회";
						if(item.exposure_target - item.exposure_count < 100) {
							span_o.addClass("deadLine");
						}
						
					} else if(item.pay_type == "CPP") {
						txt = item.start_ymd + " ~ " + item.end_ymd;
						let diff = moment(item.end_ymd).diff(moment().format("YYYY-MM-DD 00:00:00"), "days");
						
						if(diff <= 7 && diff >= 0) {
							span_o.addClass("deadLine");
						}
					}
					span_o.text(txt);
					td_o.append(span_o);
					tr_o.append(td_o);
				}
				{
					// 종료
					let td_o = $("<td>");
					let span_o = $("<span>");
					let txt = "";
					
					if(item.pay_type == "CPM") {
						txt = (item.exposure_count / item.exposure_target * 100).toFixed(0) + "%";
						if(item.exposure_target - item.exposure_count < 100) {
							span_o.addClass("deadLine");
						}
					} else if(item.pay_type == "CPP") {
						let diff = moment(item.end_ymd).diff(moment().format("YYYY-MM-DD 00:00:00"), "days");
						if(diff < 0) {
							txt = "종료";
						} else {
							txt = "D-" + Math.abs(diff);
						}
						
						if(diff <= 7 && diff >= 0) {
							span_o.addClass("deadLine");
						}
					}
					span_o.text(txt);
					td_o.append(span_o);
					tr_o.append(td_o);
				}
				body_o.append(tr_o);
			}
		},
		
		// 승인 대기중인 광고 탭
		drawRequestList: function(data) {
			let body_o = $("#requestBody").empty();
			let list = data.list;
			if(list.length == 0) {
				let div_o = $("<div>").addClass("notnotnot").text("승인 대기중인 광고가 없습니다.");
				$("#advTabs4 .tableWrap").append(div_o);
				return;
			} 
			for (let item of list) {
				let tr_o = $("<tr>");
				{
					// NO
					let td_o = $("<td>").text(item.seq);
					tr_o.append(td_o);
				}
				{
					// 과금방식
					let td_o = $("<td>").text(item.pay_type);
					tr_o.append(td_o);
				}
				{
					// 캠페인명
					let td_o = $("<td>");
					let a_o = $("<a>").attr({
						"href" : "/demand/campaign/sg/list?id=" + item.campaign_id,
					}).text(item.campaign_name);
					td_o.append(a_o);
					tr_o.append(td_o);
				}
				{
					// 광고명
					let td_o = $("<td>");
					let a_o = $("<a>").attr({
						"href" : "/demand/campaign/sg/detail?id=" + item.sg_id,
					}).text(item.sg_name);
					td_o.append(a_o);
					tr_o.append(td_o);
				}
				{
					// 승인요청 시점
					let td_o = $("<td>").text(item.request_date);
					tr_o.append(td_o);
				}
				body_o.append(tr_o);
			}
		}
	}
	
	// 이벤트 담당
	const _event = {
		clickTab: function(evo) {
			let tabId = evo.find("span").attr("id");
			if(tabId == "exposure") {
				_campaign.getList(1, "EA");
			} else if(tabId == "price") {
				_campaign.getList(1, "PA");
			} else if(tabId == "proceed") {
				_campaign.getList(1, "PT");
			} else if(tabId == "request") {
				_campaign.getList(0);
			}
		},
		
		
		clickSort: function(evo){
			evo.toggleClass("turningA");
			
			let tabId = $("#sgTab .active span").attr("id");
			let status = "";
			if(tabId == "request") {
				status = 0;
			} else {
				status = 1;
			}
			
			let type = evo.attr("data-type");
			let sortType = "";
			if(type == "campaignName") {
				if(evo.hasClass("turningA")) {
					sortType = "CD";
				} else {
					sortType = "CA";
				}
			} else if(type == "sgName") {
				if(evo.hasClass("turningA")) {
					sortType = "SD";
				} else {
					sortType = "SA";
				}
			} else if(type == "exposure") {
				if(evo.hasClass("turningA")) {
					sortType = "ED";
				} else {
					sortType = "EA";
				}
			} else if(type == "price") {
				if(evo.hasClass("turningA")) {
					sortType = "PD";
				} else {
					sortType = "PA";
				}
			} else if(type == "date") {
				if(evo.hasClass("turningA")) {
					sortType = "DD";
				} else {
					sortType = "DA";
				}
			}
			_campaign.getList(status, sortType);
		},
		
		moveReport: function(evo) {
			let campaignId = evo.attr("data-cid");
			let sgId = evo.attr("data-sid");
			
			if(campaignId && sgId) {
				location.href = "/demand/report/period?campaignId=" + campaignId + "&sgId=" + sgId;
			} else {
				customModal.alert({
					content: "아직 광고가 노출되지 않아 리포트를 생성할 수 없습니다.",
				});
			}
		}
	}
	
	return {
		init,
	}
	
})();