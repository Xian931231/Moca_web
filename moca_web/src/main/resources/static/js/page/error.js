const error = (function () {
	
	// 해당 페이지 초기화 함수
	function init(){
		_evInit();
	}
	
	// 이벤트 초기화 
	function _evInit() {
		let evo = $("[data-src='error'][data-act]").off();
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
			if(act_v == "clickMoveLoginPage") {
				event.clickMoveLoginPage();
			}
		} 
	}
	
	// 이벤트 담당
	let _event = {
		// 로그인 페이지 이동
		// 에러나기 전 페이지가 별도페이지면 별도페이지 로그인으로 이동
		clickMoveLoginPage: function() {
			let referrer = document.referrer;
			let host = location.host;
			let index = referrer.indexOf(host) + host.length;
			
			if(referrer.substring(index).startsWith("/external")) {
				location.href = "/external/login";
			} else {
				location.href = "/login";
			}
		}
	}
	
	return {
		init
	}
	
})();