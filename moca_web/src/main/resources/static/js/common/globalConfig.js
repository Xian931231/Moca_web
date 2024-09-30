const globalConfig = (function(){
	
	function getS3Url() {
		let hostname = location.hostname;
		
		if(hostname == "www.mocafelab.com") {
			return "https://media.mocafelab.com/";
		} else if(hostname.indexOf("local") > -1 || hostname == "dev.mocafelab.com") {
			return "https://mocafe-dev.s3.ap-northeast-2.amazonaws.com/";
		} 
	}
	
	function isLogAvailable() {
		let hostname = location.hostname;
		
		if(hostname == "localhost" || hostname == "dev.mocafelab.com") {
			return true;
		}
		
		return false;
	}
	
	function getMapOption() {
		if(typeof(naver) === "object") {
			return {
				center: new naver.maps.LatLng(36.33, 127.77),
		        zoom: 8,
		        minZoom: 8,
		        maxZoom: 14,
			}
		}
	}
	
	function getMapZoomBySiCode(siCode){
		switch(siCode) {
			case "all":
				return 8;
			case "S38": 
				return 9;
			case "S31" : //경기도
			case "S32" : //강원도
			case "S33" : //충청북도
			case "S34" : //충청남도
			case "S35" : //전라북도
			case "S36" : //전라남도
			case "S37" : //경상북도
			case "S39" : //제주도
				return 10;
			case "S24" : // 광주
			case "S25" : // 대전
			case "S26" : // 울산
			case "S29" : // 세종
				return 12;
			default: 
				return 11;
		}
	}
	
	let memberType = {
		ADMIN: {
			name: "관리자",
			utype: "A",
		},
		AGENCY: {
			name: "대행사",
			utype: "B",
		},
		DEMAND: {
			name: "광고주",
			utype: "D",
		},
		SUPPLY: {
			name: "매체사",
			utype: "S",
		},
		EXTERNAL: {
			name: "별도페이지 관리자",
			utype: "E",
		},
	}
	
	return {
		getS3Url,
		isLogAvailable,
		// 서비스 년도. TODO 2024로 변경 예정
		serviceYear: 2023,
		getMapOption,
		memberType,
		getMapZoomBySiCode,
	}
	
})();