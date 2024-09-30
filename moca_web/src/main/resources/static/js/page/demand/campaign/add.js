const campaignAdd = (function () {
	
	// 해당 페이지 초기화 함수
	function init(){
		_evInit();
	}
	
	// 이벤트 초기화 
	function _evInit() {
		let evo = $("[data-src='campaignAdd'][data-act]").off();
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
			if(act_v == "addCampaign") {
				event.addCampaign();
			} else if(act_v == "addCampaignMoveSgAdd") {
				event.addCampaignMoveSgAdd();
			}
		}
	}
	
	// 이벤트 담당
	const _event = {
		addCampaign: function() {
			_addCampaign(function() {
				location.href = "/demand/campaign/list";
			});
		},
		
		addCampaignMoveSgAdd: function() {
			_addCampaign(function(resp) {
				let data = resp.data;
				if(data.pay_type == "CPM") {
					location.href = "/demand/campaign/sg/addcpm?id=" + data.id;
				} else if(data.pay_type == "CPP") {
					location.href = "/demand/campaign/sg/addcpp?id=" + data.id;
				}
			});
		}
	}
	
	// 캠페인 생성 함수
	function _addCampaign(callback) {
		if(_validateData()) {
			let url_v = "/campaign/demand/add";
			
			let data_v = {
				campaign_name: $("#name").val(),
				pay_type: $("#payType").val()
			}
		
			comm.send(url_v, data_v, "POST", function(resp) {
				if(resp.result) {
					if(typeof(callback) == "function") {
						callback(resp);
					}
				}
			});
		}
	}
	
	
	function _validateData() {
		let result = true;
		
		let name_v = $("#name").val();
		if(!name_v || name_v.length > 30) {
			result = false;
		}
		
		let payType_v = $("#payType").val();
		if(!payType_v) {
			result = false;
		}
		
		return result;
	}
	
	return {
		init
	}
	
})();