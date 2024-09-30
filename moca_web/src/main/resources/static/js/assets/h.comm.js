/**===========================================================================================================  
 * @Project		: Common Lib Project
 * @Source		: h.comm.js
 * @Description	: 통신 관련 설정
 * @Version		: v0.8.8
 * 
 * Copyright(c) 2011 NewFrom All rights reserved
 * ===========================================================================================================  
 *  No        DATE      Author	                Description
 * ===========================================================================================================        
 *  0.8.0 	2014/05/26	hpiece@gmail.com		Initial Coding
 *  0.8.8 	2014/06/17	hpiece@gmail.com		Modify And Add Coding
 * ===========================================================================================================
 */
// [Console로그 및 화면 디버그용 사용]
const dev = (function() {
	function log(...s) {
		if (globalConfig.isLogAvailable()) {
			console.trace(...s);
		}
		return;
	}
	
	return {
		log
	}
})();

const comm = {
	timeout:60,					// timeout 시간 설정
	callbackQueue: new Array(),	// 콜백함수 큐
	isReceive: false,			// 큐 대기열 트리거
	prefixUrl: "/api/v1",		// url 앞에 붙을 고정 URL 
	errorMsg: "SERVER ERROR!",	// 기본 에러 메세지
	
	loading: false,				// ajax 통신 요청중이면 true.
	useLoading: true,			// 로딩 모달 사용 여부
	isTimeout: false,			// 타임아웃 여부 체크 
	
	// 프로미스를 사용하여 통신할 경우 로딩바 제어
	sendPromise: function(sendFnc) {
		if(sendFnc && typeof(sendFnc) == "function") {
			comm.useLoading = false;
			comm.showLoading(true);	
			sendFnc()
				.then(() => {
					comm.useLoading = true;
					comm.showLoading(false);	
				});
		}
	},
	
	// 통신 함수
	send: function(url, data, method, sucCallback, errCallback, option){
		if (url) {
			if (url != "/login" && url != "/external/login" && url != "/logout" && url != "/external/logout" && url != "/login/duplicate") {
				url = comm.prefixUrl + url;
			}
		} else {
			return;
		}
		
		// uuid 생성
		var _uuid = util.getUUID();
		
		// 콜백 정보 생성
		var callbackObj = {
				isComplete: false,
				uuid: _uuid,
				successCallback: sucCallback,
				errorCallback: errCallback
		};
		
		// 콜백 큐에 정보 입력
		comm.callbackQueue.push(callbackObj);
		
		// 데이터 변환
		if(data != null) {
			var json = JSON.stringify(data);
			data = "e=" + encodeURIComponent(util.aesEncode(json)); 
		}
		
		if(comm.useLoading && comm.loading == false) {
			comm.showLoading(true);
		}
		
		// ajax 통신객체 설정
		var jqxhr = $.ajax({
			url,
			type: method,
			data,
			contentType: "application/x-www-form-urlencoded; charset=UTF-8",
			/*
			contentType: "application/json; charset=UTF-8",
			contentType: "text; charset=UTF-8",
			 */
			// cors 설정
			/*
			xhrFields: {
			      withCredentials: true
			},
			*/
			success: function(response, status, xhr){
				let contentType = xhr.getResponseHeader("content-type");
				if(contentType && contentType.toLowerCase().indexOf("text/html") > -1) {
					location.href = "/";
					return;
				}
				
		    	if(response.e) {
					response = JSON.parse(util.aesDecode(response.e));
				}
				let code = response.header.code;
				
				if(code != 200) {
					let msg = response.header.msg;
					// error 
					comm.recv("error", _uuid, {xhr: xhr, status: status, msg: msg, code: code, callback: errCallback});
				} else {
					// success
					comm.recv("success", _uuid, {response: response.body});
				}
			},
			
			error: function(xhr, status, error){
				var json = xhr.responseJSON;
				var code = 500;
				var msg = comm.errorMsg;
				if(json) {
					if(json.header != null && json.header.msg) {
						code = json.header.code;
						msg = json.header.msg;
					}
				}
				
				comm.recv("error", _uuid, { xhr, status, error, msg, code, callback: errCallback });
			},
			
			complete: function(){
				// 로딩 관련 설정
				/*
				window.setTimeout(function() {
					if(comm.useLoading == true || instantLoading == true){
						comm.showLoading(false);
					}
					else{
						comm.loading = false;
					}
				}, 500);
				*/
			},
		});
	},
	
	//파일 업로드 ajax 
	sendFile: function(url, data, method, sucCallback, errCallback, option){
		
		if(url) {
			url = comm.prefixUrl + url;
		} else {
			return;
		}
		
		// uuid 생성
		var _uuid = util.getUUID();
		
		// 콜백 정보 생성
		var callbackObj = {
				isComplete: false,
				uuid: _uuid,
				successCallback: sucCallback,
				errorCallback: errCallback
		};
		
		// 콜백 큐에 정보 입력
		comm.callbackQueue.push(callbackObj);
		
		if(comm.useLoading && comm.loading == false) {
			comm.showLoading(true);
		}
		
		var fData = new FormData();
		if(data instanceof FormData) {
			var entries = data.entries();
			var obj = {};
			for(var pair of entries) {
				var key = pair[0];
				var value = pair[1];
				
				if(value instanceof File) {
					fData.append(key, value);
				} else {
					obj[key] = value;
				}
			}
			fData.append("e", util.aesEncode(JSON.stringify(obj)));
		}
		
		var jqxhr = $.ajax({
		    url,
		    type: method,
		    data: fData,
		    contentType : false,
		    processData: false,
		    enctype: "multipart/form-data",
		    xhrFields: {
		          withCredentials: true
		    },
		    cache: false,
		    
		    success: function(response, status, xhr){
		    	let contentType = xhr.getResponseHeader("content-type");
				if(contentType && contentType.toLowerCase().indexOf("text/html") > -1) {
					location.href = "/";
					return;
				}
		    	
		    	if(response.e) {
					response = JSON.parse(util.aesDecode(response.e));
				}
				let code = response.header.code;
				
				if(code != 200) {
					let msg = response.header.msg;
					// error 
					comm.recv("error", _uuid, {xhr: xhr, status: status, msg: msg, code: code, callback: errCallback});
				} else {
					// success
					comm.recv("success", _uuid, {response: response.body});
				}
			},
			
		    error: function(xhr, status, error){
				var json = xhr.responseJSON;
				var code = 500;
				var msg = comm.errorMsg;
				if(json) {
					if(json.header != null && json.header.msg) {
						code = json.header.code;
						msg = json.header.msg;
					}
				}
				comm.recv("error", _uuid, { xhr, status, error, msg, code});
			},
			
			complete: function(){
				window.setTimeout(function() {
					if(comm.useLoading == true){

						comm.showLoading(false);
					}
					else{
						comm.loading = false;
					}
				}, 500);
				
			},
		});

	},

	// 콜백 함수가 통신 시작한 순서대로 실행되기 위한 큐 로직
	recv: function(type, uuid, option){
		// 큐 대기 위한 설정
		if(comm.isReceive == true){
			setTimeout(function(){
				comm.recv(type, uuid);
			}, 500);
			return;
		}
		
		comm.isReceive = true;
		
		// 콜백 큐에 완료 처리
		for(var i = 0; i < comm.callbackQueue.length; i++){
			var cbInfo = comm.callbackQueue[i];
			if(cbInfo.uuid == uuid){
				cbInfo.isComplete = true;
				if(type == "success"){
					cbInfo.type = "success";
					cbInfo.response = option.response;
				}
				else if(type == "error"){
					cbInfo.type = "error";
					cbInfo.xhr = option.xhr;
					cbInfo.status = option.status;
					cbInfo.errorCallback = option.callback;
					cbInfo.code = option.code;
					cbInfo.msg = option.msg;
				}
				break;
			}
		}
		
		var cloneQueue = new Array().concat(comm.callbackQueue);
		
		// 콜백 큐에서 순서대로 완료된 통신 콜백 처리, 중간 순서에 완료되지 않으면 리턴
		for(var i = 0; i < cloneQueue.length; i++){
			var cbInfo = cloneQueue[i];
			if(cbInfo.isComplete == true){
				if(cbInfo.type == "success"){
					// 로그인 만료됐을때 처리
					if(cbInfo.response && cbInfo.response.code === 1008) {
						location.href = "/login";
						return;
					}
					
					if(cbInfo.response.code === 404) {
						
					}
					
					if(typeof cbInfo.successCallback == "function"){
						cbInfo.successCallback(cbInfo.response);
						
						// 통신 성공 시 헤더 타이머 초기화
						// 관리자 별도 페이지는 header.js를 쓰지 않아서 관리자 별도 페이지가 아닐 경우에만 실행
						if(!location.pathname.startsWith("/external")) {
							header.timerInit();
						}
					}
				}
				else if(cbInfo.type == "error"){
					let msg = "code: " + cbInfo.code + "\r\nmsg: " + cbInfo.msg;
					comm.useLoading = true;
					alert(msg);
				}
				comm.callbackQueue.shift();
			}
			else{
				break;
			}
			
			if(comm.useLoading && i == cloneQueue.length - 1) {
				comm.showLoading(false);
			}
		}
		
		comm.isReceive = false;
	},
	//로딩 함수 
	showLoading: function(flag){
		if(flag){
			$("#loader").show();
		}
		else{
			setTimeout(function(){
				$("#loader").hide();
			}, 500);
		}
	}
}