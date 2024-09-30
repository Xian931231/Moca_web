const agencyMain = (function () {
	
	// 해당 페이지 초기화 함수
	function init(){
		/**
		 * 목록 조회 타입
		 * list type : withCpSg (광고주-대행사 [캠페인 [광고]])
		 			 , sg (광고주-대행사 [광고])
		 			 , request (대기중인 광고)
		   sort_type : _evnet.clickSort() 참고
		 */
		_member.getSgList(1, "withCpSg", "AED");
	}
	
	// 이벤트 초기화 
	function _evInit() {
		let evo = $("[data-src='agencyMain'][data-act]").off();
		evo.on("click", function(ev){
			_action(ev);
		});
	}
	
	// 이벤트 분기 함수
	function _action (ev) {
		let evo = $(ev.currentTarget);
		
		let act_v = evo.attr("data-act");
		
		let type_v = ev.type;
		
		let event = _event;
		
		if(type_v == "click") {
			if(act_v == "clickTab") {
				event.clickTab(evo);
			} else if(act_v == "moveSgDetail") {
				event.moveSgDetail(evo);
			} else if(act_v == "moveSgList") {
				event.moveSgList(evo);
			} else if(act_v == "moveDemandMain") {
				event.moveDemandMain(evo);
			} else if(act_v == "clickSort") {
				event.clickSort(evo);
			}
		}
	}
	
	const _member = {
		getSgList: function(status, listType, sortType) {
			let url_v = "/member/agency/sg/list"
			
			let data_v = {
				status : status,
				list_type : listType,
				sort_type : sortType
			};
			
			comm.send(url_v, data_v, "POST", function(resp) {
				if(resp.result) {
					let tabId = $("#sgTab .active span").attr("id");
					$(".notnotnot").remove();
					
					_campaign.drawCount(resp.total);
					if(tabId == "exposure") {
						_sg.drawExposureList(resp);
					} else if(tabId == "price") {
						_sg.drawPriceList(resp);
					} else if(tabId == "proceed") {
						_sg.drawProceedList(resp);
					} else if(tabId == "request") {
						_sg.drawRequestList(resp);
					}
					_evInit();
				}
			});
		}
	}
	
	const _campaign = {
		drawCount: function(data) {
			$("#standardDate").text(moment().subtract(1, "days").format("YYYY-MM-DD") + "일 기준");
			$("#exposure").text(util.numberWithComma(data.exposure_total));
			$("#price").text(util.numberWithComma(data.price_total));
			$("#proceed").html("<i>" + data.soon_end_count + "</i> / " + data.proceed);
			$("#request").text(util.numberWithComma(data.request));
		}
	}
	
	
	const _sg = {
		// 총 노출 수
		drawExposureList: function(data) {
			let body_o = $("#exposureBody").empty();
			let list = data.list;
			let rowCount = 0;
			
			if(list.length == 0) {
				let div_o = $("<div>").addClass("notnotnot").text("진행중인 광고가 없습니다.");
				$("#agTabs1 .tableWrap").append(div_o);
				return;
			} 
			
			for(let i=0; i<list.length; i++) {
				let item = list[i];
				
				let campaignList = item.campaign_list;
				
				let length = 0;
				for(cItem of campaignList) {
					let sgList = cItem.sg_list;
					length += sgList.length
				}
				
				for(let j=0; j<campaignList.length; j++) {
					let cItem = campaignList[j];
					
					let sgList = cItem.sg_list;
					
					for(let k=0; k<sgList.length; k++) {
						rowCount++;
						let sgItem = sgList[k];						
						let tr_o = $("<tr>").attr({
							"data-dsp" : item.dsp_id,
							"data-dsp-uid": item.dsp_uid,
						});
						body_o.append(tr_o);
						if(k == 0 && j == 0) {
							{
								// NO
								let td_o = $("<td>").attr("rowspan", length).text(item.seq);
								tr_o.append(td_o);
							}
							{
								// 광고주
								let td_o = $("<td>").attr("rowspan", length);
								let span1_o = $("<span>").html(item.dsp_company_name + "<br>");
								let span2_o = $("<span>");
								let a_o = $("<a>").attr({
									"href" : "javascript:;",
									"data-src" : "agencyMain",
									"data-act" : "moveDemandMain",
								}).text(item.dsp_uid);
								span2_o.append(a_o);
								td_o.append(span1_o);
								td_o.append(span2_o);
								tr_o.append(td_o);
							}
							{
								// 담당자
								let td_o = $("<td>").attr({
									"rowspan" : length
								}).text(item.uname);
								tr_o.append(td_o);
							}
						}
						if(k == 0) {
							{
								// 과금방식
								let td_o = $("<td>").attr("rowspan", cItem.sg_list.length).text(cItem.pay_type);
								tr_o.append(td_o);
							}
							{
								// 캠페인명
								let td_o = $("<td>").attr("rowspan", cItem.sg_list.length);
								let a_o = $("<a>").attr({
									"href" : "javascript:;",
									"data-src" : "agencyMain",
									"data-act" : "moveSgList",
									"data-id"  : cItem.campaign_id,
								}).text(cItem.name);
								td_o.append(a_o);
								tr_o.append(td_o);
							}
						}
						{
							// 광고명
							let td_o = $("<td>");
							let a_o = $("<a>").attr({
								"href" : "javascript:;",
								"data-src" : "agencyMain",
								"data-act" : "moveSgDetail",
								"data-id"  : sgItem.sg_id,
							}).text(sgItem.sg_name);
							td_o.append(a_o);
							tr_o.append(td_o);
						}
						{
							// 노출량
							let td_o = $("<td>").text(util.numberWithComma(sgItem.exposure_count));
							tr_o.append(td_o);
						}
						// 합계
						if(j == campaignList.length - 1 && k == sgList.length - 1) {
							let tr_o = $("<tr>").addClass("middleTfoot");
							let th_o = $("<th>").attr("colspan", 6).text("합계");
							let td_o = $("<td>").text(util.numberWithComma(item.total_exposure));
							tr_o.append(th_o);
							tr_o.append(td_o);
							body_o.append(tr_o);
						} 
					}
				}
			}
		},
		
		// 총 집행금액
		drawPriceList: function(data) {
			let body_o = $("#priceBody").empty();
			let list = data.list;
			let rowCount = 0;
			if(list.length == 0) {
				let div_o = $("<div>").addClass("notnotnot").text("진행중인 광고가 없습니다.");
				$("#agTabs2 .tableWrap").append(div_o);
				return;
			} 
			for (let i=0; i<list.length; i++) {
				let item = list[i];
				let sgList = item.sg_list;
				for(let j=0; j<sgList.length; j++) {
					rowCount++;
					let sgItem = sgList[j];
					
					let tr_o = $("<tr>").attr({
						"data-dsp" : item.dsp_id,
						"data-dsp-uid": item.dsp_uid,
					});
					body_o.append(tr_o);
					
					if(j == 0) {
						{
							// NO
							let td_o = $("<td>").attr("rowspan", item.sg_list.length).text(i + 1);
							tr_o.append(td_o);
						}
						{
							// 광고주
							let td_o = $("<td>").attr("rowspan", item.sg_list.length).html(item.dsp_company_name + "<br>");
							let a_o = $("<a>").attr({
								"href" : "javascript:;",
								"data-src" : "agencyMain",
								"data-act" : "moveDemandMain",
							}).text(item.dsp_uid);
							td_o.append(a_o);
							tr_o.append(td_o);
						}
						{
							// 담당자
							let td_o = $("<td>").attr("rowspan", item.sg_list.length).text(item.uname);
							tr_o.append(td_o);
						}
					}
					{
						// 과금방식
						let td_o = $("<td>").text(sgItem.pay_type);
						tr_o.append(td_o);
					}
					{
						// 광고명
						let td_o = $("<td>");
						let a_o = $("<a>").attr({
							"href" : "javascript:;",
							"data-src" : "agencyMain",
							"data-act" : "moveSgDetail",
							"data-id" : sgItem.sg_id,
						}).text(sgItem.sg_name);
						td_o.append(a_o);
						tr_o.append(td_o);
					}
					{
						// 집행금액
						let td_o = $("<td>").text(util.numberWithComma("\\" + sgItem.price));
						tr_o.append(td_o);
					}
					if(j == (sgList.length - 1)) {
						{
							// 합계
							let tr_o = $("<tr>").addClass("middleTfoot");
							let th_o = $("<th>").attr("colspan", 5).text("합계");
							let td_o = $("<td>").text(util.numberWithComma("\\" + item.total_price));
							tr_o.append(th_o);
							tr_o.append(td_o);
							body_o.append(tr_o);
						}
					}
				}
			}
		},
		// 진행중인 광고
		drawProceedList: function(data) {
			let body_o = $("#proceedBody").empty();
			let list = data.list;
			let rowCount = 0;
			
			if(list.length == 0) {
				let div_o = $("<div>").addClass("notnotnot").text("진행중인 광고가 없습니다.");
				$("#agTabs3 .tableWrap").append(div_o);
				return;
			}
			for(let i=0; i<list.length; i++) {
				let item = list[i];
				
				let campaignList = item.campaign_list;
				
				let length = 0;
				for(cItem of campaignList) {
					let sgList = cItem.sg_list;
					length += sgList.length
				}
				
				for(let j=0; j<campaignList.length; j++) {
					let cItem = campaignList[j];
					
					let sgList = cItem.sg_list;
					
					for(let k=0; k<sgList.length; k++) {
						rowCount++;
						let sgItem = sgList[k];
						let tr_o = $("<tr>").attr({
							"data-dsp" : item.dsp_id,
							"data-dsp-uid": item.dsp_uid,
						});
						body_o.append(tr_o);
						if(k == 0 && j == 0) {
							{
								// NO
								let td_o = $("<td>").attr("rowspan", length).text(item.seq);
								tr_o.append(td_o);
							}
							{
								// 광고주
								let td_o = $("<td>").attr("rowspan", length);
								let span1_o = $("<span>").html(item.dsp_company_name + "<br>");
								let span2_o = $("<span>");
								let a_o = $("<a>").attr({
									"href" : "javascript:;",
									"data-src" : "agencyMain",
									"data-act" : "moveDemandMain",
								}).text(item.dsp_uid);
								span2_o.append(a_o);
								td_o.append(span1_o);
								td_o.append(span2_o);
								tr_o.append(td_o);
							}
							{
								// 담당자
								let td_o = $("<td>").attr({
									"rowspan" : length
								}).text(item.uname);
								tr_o.append(td_o);
							}
						}
						if(k == 0) {
							{
								// 과금방식
								let td_o = $("<td>").attr("rowspan", cItem.sg_list.length).text(cItem.pay_type);
								tr_o.append(td_o);
							}
							{
								// 캠페인명
								let td_o = $("<td>").attr("rowspan", cItem.sg_list.length);
								let a_o = $("<a>").attr({
									"href" : "javascript:;",
									"data-src" : "agencyMain",
									"data-act" : "moveSgList",
									"data-id"  : cItem.campaign_id,
								}).text(cItem.name);
								td_o.append(a_o);
								tr_o.append(td_o);
							}
						}
						{
							// 광고명
							let td_o = $("<td>");
							let a_o = $("<a>").attr({
								"href" : "javascript:;",
								"data-src" : "agencyMain",
								"data-act" : "moveSgDetail",
								"data-id"  : sgItem.sg_id
							}).text(sgItem.sg_name);
							td_o.append(a_o);
							tr_o.append(td_o);
						}
						{
							// 진행상황
							let td_o = $("<td>");
							let span_o = $("<span>");
							let txt = "";
							if(cItem.pay_type == "CPM") {
								txt = sgItem.exposure_count + "/" + sgItem.exposure_target + "회";
								if(sgItem.exposure_target - sgItem.exposure_count < 100) {
									span_o.addClass("deadLine");
								}
								
							} else if(cItem.pay_type == "CPP") {
								txt = sgItem.start_ymd + " ~<br>" + sgItem.end_ymd;
								let diff = moment(sgItem.end_ymd).diff(moment().format("YYYY-MM-DD 00:00:00"), "days");

								if(diff <= 7 && diff >= 0) {
									span_o.addClass("deadLine");
								}
							}
							span_o.html(txt);
							td_o.append(span_o);
							tr_o.append(td_o);
						}
						{
							// 종료
							let td_o = $("<td>");
							let span_o = $("<span>");
							
							let txt = "";
							
							if(cItem.pay_type == "CPM") {
								txt = (sgItem.exposure_count / sgItem.exposure_target * 100).toFixed(0) + "%";
								if(sgItem.exposure_target - sgItem.exposure_count < 100) {
									span_o.addClass("deadLine");
								}
							} else if(cItem.pay_type == "CPP") {
								let diff = moment(sgItem.end_ymd).diff(moment().format("YYYY-MM-DD 00:00:00"), "days");
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
					}
				}
			}
		},
		
		// 승인 대기중인 광고
		drawRequestList: function(data) {
			let body_o = $("#requestBody").empty();
			let list = data.list;
			if(list.length == 0) {
				let div_o = $("<div>").addClass("notnotnot").text("승인 대기중인 광고가 없습니다.");
				$("#agTabs4 .tableWrap").append(div_o);
				return;
			} 
			for(let item of list) {
				let tr_o = $("<tr>").attr({
					"data-dsp" : item.dsp_id,
					"data-dsp-uid": item.dsp_uid,
				});
				{
					// NO
					let td_o = $("<td>").text(item.seq);
					tr_o.append(td_o);
				}
				{
					// 광고주
					let td_o = $("<td>").html(item.dsp_company_name + "<br>");
					let a_o = $("<a>").attr({
						"href" : "javascript:;",
						"data-src" : "agencyMain",
						"data-act" : "moveDemandMain",
					}).text(item.dsp_uid);
					td_o.append(a_o);
					tr_o.append(td_o);
				}
				{
					// 담당자
					let td_o = $("<td>").text(item.uname);
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
						"href" : "javascript:;",
						"data-src" : "agencyMain",
						"data-act" : "moveSgList",
						"data-id"  : item.campaign_id,
					}).text(item.campaign_name);
					td_o.append(a_o);
					tr_o.append(td_o);
				}
				{
					// 광고명
					let td_o = $("<td>");
					let a_o = $("<a>").attr({
						"href" : "javascript:;",
						"data-src" : "agencyMain",
						"data-act" : "moveSgDetail",
						"data-id"  : item.sg_id
					}).text(item.sg_name);
					td_o.append(a_o);
					tr_o.append(td_o);
				}
				{
					// 집행요청금액
					let td_o = $("<td>").text(util.numberWithComma("\\" + item.price));
					tr_o.append(td_o);
				}
				{
					// 승인요청시점
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
				_member.getSgList(1, "withCpSg", "AED");
			} else if(tabId == "price") {
				_member.getSgList(1, "sg", "APD");
			} else if(tabId == "proceed") {
				_member.getSgList(1, "withCpSg", "TD")
			} else if(tabId == "request") {
				_member.getSgList(0, "request", "RD");
			}
		},
		
		moveSgDetail: function(evo) {
			let tr_o = evo.closest("tr");
			let memberId = tr_o.attr("data-dsp");
			let memberUid = tr_o.attr("data-dsp-uid");
			let memberUtype = globalConfig.memberType.DEMAND.utype;
			let sgId = evo.attr("data-id");
			let callback = function() {
				location.href = "/demand/campaign/sg/detail?id=" + sgId;
			};
			
			util.staffLogin({
				memberId,
				memberUid,
				memberUtype,
				callback
			});
		},
		
		moveSgList: function(evo) {
			let tr_o = evo.closest("tr");
			let memberId = tr_o.attr("data-dsp");
			let memberUid = tr_o.attr("data-dsp-uid");
			let memberUtype = globalConfig.memberType.DEMAND.utype;
			let campaignId = evo.attr("data-id");
			let callback = function() {
				location.href = "/demand/campaign/sg/list?id=" + campaignId;
			};
			
			util.staffLogin({
				memberId,
				memberUid,
				memberUtype,
				callback
			});
		},
		
		moveDemandMain: function(evo) {
			let tr_o = evo.closest("tr");
			let memberId = tr_o.attr("data-dsp");
			let memberUid = tr_o.attr("data-dsp-uid");
			let memberUtype = globalConfig.memberType.DEMAND.utype;
			
			util.staffLogin({
				memberId,
				memberUid,
				memberUtype,
			});
		},
		
		clickSort: function(evo) {
			evo.toggleClass("turningA");
			
			let tabId = $("#sgTab .active span").attr("id");
			let status = "";
			let listType = "";
			if(tabId == "exposure") {
				status = 1;
				listType = "withCpSg"
			} else if(tabId == "price") {
				status = 1;
				listType = "sg"
			} else if(tabId == "proceed") {
				status = 1;
				listType = "withCpSg"
			} else if(tabId == "request") {
				status = 0;
				listType = "request"
			}
			
			let type = evo.attr("data-type");
			let sortType = "";
			if(type == "exposure") {
				if(evo.hasClass("turningA")) {
					sortType = "AED";
				} else {
					sortType = "AEA";
				}
			} else if(type == "price") {
				if(evo.hasClass("turningA")) {
					sortType = "APD";
				} else {
					sortType = "APA";
				}
			} else if(type == "request") {
				if(evo.hasClass("turningA")) {
					sortType = "RD";
				} else {
					sortType = "RA";
				}
			}
			_member.getSgList(status, listType, sortType);
		}
		
	}
	
	return {
		init,
	}
	
})();