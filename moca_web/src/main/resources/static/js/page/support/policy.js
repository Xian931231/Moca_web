let policy = (function() {
	
	// 해당 페이지 초기화 함수
	function init() {
		_setTerms();
		_evInit();
	}
	
	// 이벤트 초기화 
	function _evInit() {
		let evo = $("[data-src='join'][data-act]").off();
		evo.on("click", function(ev) {
			_action(ev);
		});
	}
	
	// 이벤트 분기 함수
	function _action(ev) {
		let evo = $(ev.currentTarget);
		
		let act_v = evo.attr("data-act");
		
		let type_v = ev.type;

		if(type_v == "click") { // 클릭 이벤트
			if(act_v == "clickLogo") {
				location.href = "/";
			}
		}
	}
	
	// 광고주, 대행사, 매체사에 따른 약관 셋팅
	function _setTerms() {
		let termsHtml = ["/assets/text/serviceTerms.txt", "/assets/text/demandTerms.txt", "/assets/text/agencyTerms.txt"];
		let terms_o = ["#serviceArea", "#demandArea", "#agencyArea"];
		let terms = "";
		
		for(let i = 0; i < 3; i++) {
			let xmlHttp = new XMLHttpRequest();			

			xmlHttp.open("GET", termsHtml[i], true);
			xmlHttp.send();
	
			xmlHttp.onreadystatechange = function() { 
			    if(this.status == 200 && this.readyState == this.DONE) {
			        terms = xmlHttp.responseText;
			        $(terms_o[i]).html(terms);            
			    }
			};
		}
	}

	return {
		init,
	}
	
})();