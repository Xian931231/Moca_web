const externalLogin = (function () {
	
	// 해당 페이지 초기화 함수
	function init(){
		_evInit();
		util.removeSessionAllParam();
	}
	
	// 이벤트 초기화 
	function _evInit() {
		let evo = $("[data-src='externalLogin'][data-act]").off();
		evo.on("click keyup", function(ev){
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
			if(act_v == "clickLogin") {
				event.clickLogin();
			}
		} else if(type_v == "keyup") {
			if(act_v == "keyupLogin") {
				if(ev.keyCode == 13) {
					event.clickLogin();
				}
			}
		}
	}
	
	const _data = {
		// 로그인 데이터
		getSubmitData: function() {
			let data_v = {};
			
			// 아이디
			data_v.uid = $("#loginId").val();
			
			// 비밀번호
			data_v.passwd = $("#loginPw").val();
			
			return data_v;
		},
	}
	
	// 이벤트 담당
	const _event = {
		// 로그인
		clickLogin: function() {
			let data_v = _data.getSubmitData();
			
			if(!_validateSubmitData(data_v)) {
				return;
			}
			let url_v = "/external/login";
			
			comm.send(url_v, data_v, "POST", function(resp) {
				if(resp.result) {
					if(resp.default_url) {
						location.href = resp.default_url;
					} else {
						location.href = "/external/product/list";
					}
				} else {
					customModal.alert({
						"content" : "로그인에 실패했습니다.",
					});
				}
			});
		}
	}
	
	// 유효성 검사
	function _validateSubmitData(data) {
		let isValid = true;
		
		if(!data.uid || !data.passwd) {
			isValid = false;
		}
		
		if(!isValid) {
			customModal.alert({
				"content" : "로그인에 실패했습니다.",
			});
		}
		return isValid;
	}
	
	return {
		init
	}
	
})();