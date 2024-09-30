const modify = (function () {
	
	let urlParam = util.getUrlParam();
	// 해당 페이지 초기화 함수
	function init(){
		_detail.getDetail();
		_evInit();
	}
	
	// 이벤트 초기화 
	function _evInit() {
		let evo = $("[data-src='modify'][data-act]").off();
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
			if(act_v == "moveSgList") {
				event.moveSgList();
			} else if(act_v == "modifyCampaign") {
				event.modifyCampaign();
			}
		}
	}
	
	const _detail = {
		getDetail: function() {
			let url_v = "/campaign/demand/detail";
			
			if(util.valNullChk(urlParam) || !urlParam.id || isNaN(urlParam.id)){ 
				location.href = "/demand/campaign/list";
				return;
			}
			
			let data_v = {
				campaign_id : urlParam.id,
			}
			
			comm.send(url_v, data_v, "POST", function(resp) {
				if(resp.result) {
					_detail.drawDetail(resp.data);
				} else {
					location.href = "/demand/campaign/list";
				}
			});
		},
		
		drawDetail: function(data) {
			let payType = data.pay_type;
			if(payType == "CPP") {
				$("#cppPrice").show();
				$("#price").text(util.numberWithComma(data.total_price) + "원");
			}
			
			$("#payType").text(payType);
			
			$("#name").val(util.unescapeData(data.name));
		}
	}
	
	// 이벤트 담당
	const _event = {
		
		// 캠페인 내 광고 리스트로 이동		
		moveSgList: function() {
			location.href = "/demand/campaign/sg/list?id=" + urlParam.id;;
		},
		
		// 캠페인 수정
		modifyCampaign: function() {
			if(_validateData()) {
				let url_v = "/campaign/demand/modify";
				
				let id = urlParam.id;
				
				let data_v = {
					campaign_id : id
				}
				data_v.campaign_name = $("#name").val();
				
				comm.send(url_v, data_v, "POST", function(resp) {
					if(resp.result) {
						location.href = "/demand/campaign/sg/list?id=" + id;
					}
				});
			}
		}
	}
	
	function _validateData() {
		let result = true;
		
		let name_v = $("#name").val();
		if(!name_v || name_v.length > 30) {
			result = false;
		}
		
		return result;
	}
	
	return {
		init
	}
	
})();