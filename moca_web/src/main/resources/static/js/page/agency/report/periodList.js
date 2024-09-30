const periodReportList = (() => {
	
	let _chartData = {};
	
	let _chart_o = null;
	
	//조회한 data 보관용
	let _old_data_v = {};
	
	let CHART_COLORS = {
	  red: 'rgba(255, 99, 132, 0.5)',
	  orange: 'rgba(255, 159, 64, 0.5)',
	  yellow: 'rgba(255, 205, 86, 0.5)',
	  green: 'rgba(75, 192, 192, 0.5)',
	  blue: 'rgba(54, 162, 235, 0.5)',
	  purple: 'rgba(153, 102, 255, 0.5)',
	  grey: 'rgba(201, 203, 207, 0.5)'
	};
	
	const NAMED_COLORS = [
	  CHART_COLORS.red,
	  CHART_COLORS.orange,
	  CHART_COLORS.yellow,
	  CHART_COLORS.green,
	  CHART_COLORS.blue,
	  CHART_COLORS.purple,
	  CHART_COLORS.grey,
	];
	
	// 해당 페이지 초기화 함수
	function init() {
		_evInit();
		_setSearchDatePicker();
		_setChart($("#chartTime"));
		_list.getDemandList(function(){
			_list.getReportList();	
		});
	}
	
	// 이벤트 초기화 
	function _evInit() {
		let evo = $("[data-src='periodReportList'][data-act]").off();
		evo.on("click", function(ev){
			_action(ev);
		});
	}
	
	// 이벤트 분기 함수
	function _action(ev) {
		let evo = $(ev.currentTarget);
		
		let act_v = evo.attr("data-act");
		
		let type_v = ev.type;
		
		let event = _event;
		
		if(type_v == "click") {
			if(act_v == "getSearch"){
				_list.getReportList();
			}else if(act_v == "changePeriodType"){
				event.changePeriodType(evo)
			}else if(act_v =="excelDownload"){
				event.excelDownload();
			}
		} 
	}
	// datepicker 설정
	function _setSearchDatePicker() {
		let reportType = $("button[name=reportType].active").attr("data-type");
		
		if(reportType == "hour"){
			
			//기간 selectBox 세팅
			$("#inputDate").attr({"style":"display:none"});
			$("#inputMonth").attr({"style":"display:none"});
			$("#inputHour").attr({"style":"display:"});
			
			//set datepicker
			customDatePicker.init("searchDate").datepicker("setDate",moment().format("YYYY-MM-DD"));
			
		}else if(reportType == "date"){
			
			//기간 selectBox 세팅
			$("#inputHour").attr({"style":"display:none"});
			$("#inputMonth").attr({"style":"display:none"});
			$("#inputDate").attr({"style":"display:"});
			
			//set datepicker
			customDatePicker.init("sDate").datepicker("setDate",moment().subtract(7, "days").format("YYYY-MM-DD"));
			customDatePicker.init("eDate").datepicker("setDate",moment().format("YYYY-MM-DD"));
			
			
			
		}else if(reportType == "month"){
			
			//기간 selectBox 세팅
			$("#inputDate").attr({"style":"display:none"});
			$("#inputHour").attr({"style":"display:none"});
			$("#inputMonth").attr({"style":"display:"});
			
			//set datepicker
			
			let startYear = globalConfig.serviceYear;
			let endYear = moment().format("YYYY");
			
			for(let i=startYear; i<=endYear; i++) {

				let option_o = $("<option>").attr({
					"value": i,
				}).html(i + "년");
				
				if(i==endYear) {
					option_o.attr("selected", "selected");
				}
				
				$("#searchStartYear").append(option_o);
				
				option_o = $("<option>").attr({
					"value": i,
				}).html(i + "년");
				
				if(i==endYear) {
					option_o.attr("selected", "selected");
				}
				
				$("#searchEndYear").append(option_o);
			}
			
			$("#searchStartYear").selectpicker();
			$("#searchEndYear").selectpicker();
			
			let month = String(new Date().getMonth() + 1);
			
			$("#searchStartMonth").selectpicker("val", month.padStart(2, "0"));
			$("#searchEndMonth").selectpicker("val", month.padStart(2, "0"));
			
		}
	}
	
	let _event = {
		//리포트 기간 변경 (ex. 시간별, 일별, 월별)
		changePeriodType: function(evo){
			let periodType = $(evo).attr("data-type");
			
			$("button[name=reportType].active").removeClass("active");
			$(evo).addClass("active");
			
			_chartData = {};
			$("#listBody").html("");
			$("#excelBtn").hide();
			
			if(periodType == "hour"){
				//chart 세팅
				_chart_o.destroy();
				
				_setChart($("#chartTime"));
				$("#reportType").html("시간");
			}else if(periodType == "date"){
				//chart 세팅
				_chart_o.destroy();
				
				let chartOption = {
					options: {
			    		scales: {
			      			yAxes: [{
			        			ticks: {
			          				beginAtZero: true,
						            steps: 7,
						            stepValue: 1000,
						            max: 8000,
			        			}
			      			}],
						},
			  		}
				};
				_setChart($("#chartTime"), chartOption);
				$("#reportType").html("날짜");
			}else if(periodType == "month"){
				//chart 세팅
				_chart_o.destroy();
				
				let chartOption = {
					options: {
			    		scales: {
			      			yAxes: [{
			        			ticks: {
			          				beginAtZero: true,
              						steps: 7,
              						stepValue: 2000,
              						max: 14000,
			        			}
			      			}],
						},
			  		}
				};
				_setChart($("#chartTime"), chartOption);
				$("#reportType").html("날짜");
			}
			
			_setSearchDatePicker();
			
			//_list.getReportList();
			_evInit();
		},
		
		//엑셀 다운로드
		excelDownload: function(){
			let reportType = $("button[name=reportType].active").attr("data-type");
			
			let url_v = "";
			
			//시간별
			if(reportType == "hour"){
				url_v = "/report/agency/excel/byTime";
			//일별	
			}else if(reportType == "date"){
				url_v = "/report/agency/excel/byDate";
			
			//월별	
			}else if(reportType == "month"){
				url_v = "/report/agency/excel/byMonth";
				
			}
			
			util.blobFileDownload(url_v, _old_data_v, function(){
				console.log("done")				
			});
			
		}
	}
	
	let _list = {
		getDemandList: function(callback){
			let url_v = "/report/agency/access/demand/list";
			
			comm.send(url_v, null, "POST", function(resp){
				
				let select_o = $("#searchDemandSelect").html("");
				select_o.selectpicker("destroy");
				
				select_o.append(new Option("전체 광고", "0"));
				
				for(let item of resp.list){
					select_o.append(new Option(item.company_name, item.member_id));
				}
				
				let memberArr = [0];
				for(let item of resp.list){
					memberArr.push(item.member_id);
				}
				
				$("#searchDemandSelect").val(memberArr);
				
				$("#searchDemandSelect").selectpicker("refresh");	
				
				if(typeof(callback) == "function"){
					callback();
				}
					
			})
		},
		//기간별 리포트 조회
		getReportList: function(){
			let reportType = $("button[name=reportType].active").attr("data-type");
			let url_v = "";
			
			let data_v ={
				member_list: $("#searchDemandSelect").val()
			};
			
			if(reportType == "hour"){
				url_v = "/report/agency/list/byTime";
				
				data_v.search_dt = $("#searchDate").val();
				
			}else if(reportType == "date"){
				url_v = "/report/agency/list/byDate";
				
				let startDate_v = $("#sDate").val();
				let endDate_v = $("#eDate").val();
				
				if(util.getDiffDate(startDate_v, endDate_v, "months") >= 3) {
					customModal.alert({
						content: "최대 조회 기간은 3개월입니다.",
					});
					return;
				}
				
				if(moment(startDate_v).isAfter(endDate_v)) {
					customModal.alert({
						content: "시작일이 종료일 이후일 수 없습니다.",
					});
					return;
				}
				
				data_v.str_dt = startDate_v;
				data_v.end_dt = endDate_v;
				
			}else if(reportType =="month"){
				url_v = "/report/agency/list/byMonth";
				
				let startDate = $("#searchStartYear").val() + "-" + $("#searchStartMonth").val();
				let endDate = $("#searchEndYear").val() + "-" + $("#searchEndMonth").val();
				
				if(util.getDiffDate(startDate, endDate, "months") >= 3) {
					customModal.alert({
						content: "최대 조회 기간은 3개월입니다.",
					});
					return;
				}
				
				let mSdate = moment(startDate);
				let mEdate = moment(endDate);
				if(mSdate.isAfter(mEdate)) {
					customModal.alert({
						content: "시작일이 종료일 이후일 수 없습니다.",
					});
					return;
				}
				
				data_v.str_year = $("#searchStartYear").val();
				data_v.str_month = $("#searchStartMonth").val();
				data_v.end_year = $("#searchEndYear").val();
				data_v.end_month = $("#searchEndMonth").val();
			}
			
			_old_data_v = data_v;
			comm.send(url_v, data_v, "POST", function(resp){
				//data가 있으면 엑셀 버튼 노출
				if(resp.list.length > 0){
					$("#excelBtn").show();
				}else{
					$("#excelBtn").hide();
				}
				
				
				if(reportType == "hour"){
					_list.drawHourReportList(resp.list);
				}else if(reportType == "date"){
					_list.drawDateReportList(resp.list);
				}else if(reportType == "month"){
					_list.drawMonthReportList(resp.list);
				}
				
				_list.setChartData(resp.list);
				
			});
		},
		
		//기간별 리포트 테이블 그리기
		drawHourReportList: function(list){
			let body_o = $("#listBody").html("");
			let total_o = $("#listBodyTotal").html("");
			let total_v = 0;
			for(let item of list){
				
				let tr_o = $("<tr>");
				body_o.append(tr_o);
				{
					let th_o=$("<th>").attr({
						"rowspan": 24
					});
					th_o.html(item.company_name);
					
					tr_o.append(th_o);	
				}
				
				if(item.hour_list){
					for(let i=0; i<24; i++){
						if(i > 0){
							tr_o = $("<tr>");
							body_o.append(tr_o);
						}
						//시간
						let str_o = i;
						let end_o = (i+1);
						{
							
							if(i<10){
								str_o = "0" + i;
							}
							
							if((i+1) < 10){
								end_o = "0" + (i+1); 
							}else if((i+1) == 24){
								end_o = "00";
							}
							
							let td_o = $("<td>").html(str_o + ":00 ~ " + end_o + ":00");
							tr_o.append(td_o);
						}
						let key = "hour_"+str_o;
						let hourItem = item.hour_list[key];
						
						//노출량
						{
							let td_o = $("<td>").html(util.numberWithComma(hourItem));
							tr_o.append(td_o);
							total_v += hourItem;
						}
					}
				}else{
					tr_o.append($("<td>").html("-"));
					tr_o.append($("<td>").html("-"));
				}
			}
			total_o.html(util.numberWithComma(total_v));	
		},
		//일별 리포트 테이블 그리기
		drawDateReportList: function(list){
			let body_o = $("#listBody").html("");
			let total_o = $("#listBodyTotal").html("");
			let total_v = 0;
			for(let item of list){
				let periodList = Object.keys(item.period_list);
				let tr_o = $("<tr>");
				body_o.append(tr_o);
				{
					let th_o=$("<th>").attr({
						"rowspan": periodList.length
					});
					th_o.html(item.company_name);
					
					tr_o.append(th_o);	
				}
				
				if(periodList.length > 0){
					for(let i=0; i<periodList.length; i++){
						if(i > 0){
							tr_o = $("<tr>");
							body_o.append(tr_o);
						}
						//날짜
						{
							let words = periodList[i].split('-');
							
							let changeWords = words[0]+"년"+words[1]+"월"+words[2]+"일"
							
							let td_o = $("<td>").html(changeWords);
							tr_o.append(td_o);
						}
						let periodItem = periodList[i];
						
						//노출량
						let periodCnt = item.period_list[periodItem];
						
						let td_o = $("<td>").html(util.numberWithComma(periodCnt));
						tr_o.append(td_o);
						total_v += periodCnt;
						
					}
				}else{
					tr_o.append($("<td>").html("-"));
					tr_o.append($("<td>").html("-"));
				}
			}
			total_o.html(util.numberWithComma(total_v));	
		},
		//월별 리포트 그리기
		drawMonthReportList: function(list){
			let body_o = $("#listBody").html("");
			let total_o = $("#listBodyTotal").html("");
			let total_v = 0;
			for(let item of list){
				let periodList = Object.keys(item.period_list);
				let tr_o = $("<tr>");
				body_o.append(tr_o);
				{
					let th_o=$("<th>").attr({
						"rowspan": periodList.length
					});
					th_o.html(item.company_name);
					
					tr_o.append(th_o);	
				}
				
				if(periodList.length > 0){
					for(let i=0; i<periodList.length; i++){
						if(i > 0){
							tr_o = $("<tr>");
							body_o.append(tr_o);
						}
						//날짜
						{
							let words = periodList[i].split('-');
							
							let changeWords = words[0]+"년"+words[1]+"월";
							
							let td_o = $("<td>").html(changeWords);
							tr_o.append(td_o);
						}
						let periodItem = periodList[i];
						
						//노출량
						let periodCnt = item.period_list[periodItem];
						
						let td_o = $("<td>").html(util.numberWithComma(periodCnt));
						tr_o.append(td_o);
						total_v += periodCnt;
						
					}
				}else{
					tr_o.append($("<td>").html("-"));
					tr_o.append($("<td>").html("-"));
				}
			}
			total_o.html(util.numberWithComma(total_v));	
			
		},
		//Chart 그리기
		setChartData: function(list){
			let reportType = $("button[name=reportType].active").attr("data-type");
			let chartData = {};
			
			let periodList = []
			//set labels
			if(reportType == "hour"){
				if(list !=null && list != ""){
					periodList =["0","1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23(시)"];
				}
				_chartData.labels = periodList;
			}else{
				if(list !=null && list != ""){
					periodList = Object.keys(list[0].period_list);
					
					let periodStrList = [];
					//날짜 형식 변경
					if(reportType =="date"){
						for(let key of periodList){
							let words = key.split('-');
							
							let chg = words[0]+"년"+words[1]+"월"+words[2]+"일"
							
							periodStrList.push(chg);
						}
					}else if(reportType == "month"){
						for(let key of periodList){
							let words = key.split('-');
							
							let chg = words[0]+"년"+words[1]+"월"
							
							periodStrList.push(chg);
						}
					}
					
					_chartData.labels = periodStrList;
				}	
			}
			
			
			let itemArr = [];
			for(let i=0; i<list.length; i++){
				let item = list[i];
				let dsColor = namedColor(i);
				
				let itemList = {
					label: item.company_name
					, fill: false
			      	, borderColor: dsColor
			      	, borderWidth: 1.5
			      	, pointBackgroundColor: dsColor
			      	, lineTension: 0
				};
				
				
				let cnt = [];
				//시간별 dataSetting
				if(reportType == "hour"){
					for(let i=0; i<24; i++){
						let str_o = "";
						if(i<10){
							str_o = "0" + i;
						}else{
							str_o = i;
						}
						
						let key = "hour_"+str_o;
						let hourItem = item.hour_list[key];
						cnt.push(hourItem);
					}
				//일별, 월별 dataSetting
				}else{
					for(let i=0; i<periodList.length; i++){
						let periodItem = periodList[i];
						//노출량
						cnt.push(item.period_list[periodItem]);
					}
				}
				
				itemList.data = cnt;
				 
				itemArr.push(itemList);
			}
			_chartData.datasets = itemArr;
			
			_chart_o.destroy();
			_setChart($("#chartTime"));
		}
		
	}
	
	//차트 세팅
	function _setChart(chartId, chartOption){
		let _chartOption = {
			type: 'line',
			data: _chartData,
			options: {
	    		scales: {
	      			xAxes: [{
	        			ticks: {
	          				beginAtZero: false,
	        			}
	      			}],
	      			yAxes: [{
	        			ticks: {
	          				beginAtZero: true,
	          				steps: 6,
	          				stepValue: 50,
	          				callbacks: {
								title: function(tooltipItem, data) {
		                          let title = data.datasets[tooltipItem[0].datasetIndex].label;
		                            return title;
		                        },
		                         label: function(tooltipItem, data) {
		                            let label = data.datasets[tooltipItem.datasetIndex].data[tooltipItem.index];
										
									return util.numberWithComma(label);
								}
							}
	        			}
	      			}],
	      			y: {}
				},
	    		legend: {
	      			display: true, //라벨숨김
	      			labels: {
				        fontColor: '#404040',
				        fontFamily: 'INNODAOOM-LIGHT',
				        padding: 40,
	      			},
			      	position: 'bottom',
			      	onClick: (e) => e.stopPropagation(),
	      			// label 필터링 방지
	    		},
	    		labels: {},
	    		responsive: true,
	    		maintainAspectRatio: false,
	    		tooltips: {
	      			axis: 'y',
	      			callbacks: {
						title: function(tooltipItem, data) {
                          let title = data.datasets[tooltipItem[0].datasetIndex].label;
                            return title;
                        },
                         label: function(tooltipItem, data) {
                            let label = data.datasets[tooltipItem.datasetIndex].data[tooltipItem.index];
								
							return util.numberWithComma(label);
						}
					}
	    		} //기간별 기준 일괄 데이터 팝업됨
	  		}
		};
		
		_chartOption = $.extend(true, _chartOption, chartOption);
		
		_chart_o= new Chart(chartId, _chartOption);
	}
	
	
	
	function namedColor(index) {
	  return NAMED_COLORS[index % NAMED_COLORS.length];
	}
	
	
	return {
		init,
	}
	
	
})();