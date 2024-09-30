// 각 페이지 layout 처리
const layout = (function(){
	
	let _callback = null;
	
	function init() {
		// 헤더, 사이드바 초기화 후 각 페이지의 콜백 실행을 위한 promise
		header.init()
			.then(() => menu.init())
			.then(() => {
				if(_callback != null && typeof(_callback) == "function") {
					_callback();
				}
			});
		// bootstrap selectpicker dropup 미사용
		$.fn.selectpicker.Constructor.DEFAULTS.dropupAuto = false;
		$.fn.dropdown.Constructor.Default.display = "static";
	}
	
	return {
		init,
		setCallback: function(callback){
			_callback = callback;
		},
	}
	
})();