// 별도 페이지 layout 처리
const externalLayout = (function(){
	
	let _callback = null;
	
	function init() {
		// 헤더, 사이드바 초기화 후 각 페이지의 콜백 실행을 위한 promise
		comm.sendPromise(() => {
			return externalHeader.init()
				.then(() => {
					if(_callback != null && typeof(_callback) == "function") {
						return _callback();
					}
				}).catch(); 
		});
	}

	return {
		init,
		setCallback: function(callback){
			_callback = callback;
		},
	}
	
})();