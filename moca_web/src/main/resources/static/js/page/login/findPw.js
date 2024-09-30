const findPw = (function() {

	// 해당 페이지 초기화 함수
	function init() {
		_evInit();
	}
	
	// 이벤트 초기화 
	function _evInit() {
		let evo = $("[data-src='join'][data-act]").off();
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
			if(act_v == "clickCreatePw") {
				event.clickCreatePw();
			} else if(act_v == "clickLogo") {
				location.href = "/login";
			}
		}
	}
	
	// 이벤트 담당
	let _event = {
		// 임시 비밀번호 생성
		clickCreatePw: function() {
			let uid_v = $("#uid").val();
			let email_v = $("#email").val();
			
			if(util.valNullChk(uid_v) || util.valNullChk(email_v)) {
				loginCustomModal.alert({
					content: "입력되지 않은 항목이 있습니다.<br/>모든 정보를 입력해주세요.",
				});
				return;
			} 
			
			let url_v = "/member/signUp/find/pw";
			
			let data_v = {
				"uid": uid_v,
				"email": email_v,
			};
			
			comm.send(url_v, data_v, "POST", function(resp) {
				if(resp.result == true) {
					loginCustomModal.alert({
						content: "입력하신 이메일로<br/>임시 비밀번호를 발송했습니다.",
					});
					return;
				} else {
					loginCustomModal.alert({
						content: "입력하신 정보가 일치하지 않습니다.<br/>다시 입력해 주세요.",
					});
					return;
				}
			});
		},
	}
	
	return {
		init,
	}
	
})();