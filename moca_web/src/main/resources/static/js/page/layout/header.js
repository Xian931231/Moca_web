const header = (function() {

	let _userInfo = null;
	let timer = null;
	
	// 해당 페이지 초기화 함수
	function init(){
		return new Promise((resolve) => {
			_drawInfo()
				.then(() => _evInit())
				.then(() => resolve());
		});
	}
	
	
	// 이벤트 초기화 
	function _evInit() {
		let evo = $("[data-src='header'][data-act]").off();
		evo.on("click mouseenter mouseleave", function(ev){
			_action(ev);
		});
	}
	
	// 이벤트 분기 함수
	function _action(ev) {
		let evo = $(ev.currentTarget);
		
		let act_v = evo.attr("data-act");
		
		let type_v = ev.type;
		
		let event = _event;
		
		if(type_v == "mouseenter") {
			if(act_v == "info") {
				event.mouseEnter();
			}
		} else if(type_v == "mouseleave") {
			if(act_v == "header") {
				event.mouseLeave();
			}
		} else if(type_v == "click") {
			if(act_v == "clickLogout") {
				event.clickLogout();
			} else if(act_v == "clickExtension") {
				event.clickExtension();
			} else if(act_v == "clickModifyPw") {
				_event.clickModifyPw();
			} else if(act_v == "clickModifyPwLater") {
				_event.clickModifyPwLater();
			} else if(act_v == "clickLogo") {
				location.href = "/";
			}
		}
	}
	
	// 로그인 정보
	function _drawInfo() {
		let url_v = "/member/myData/get";
		let data_v = null;
		let label_o = $("#label");
		let login_o = $("#loginId");
		
		return new Promise((resolve) => {
			comm.send(url_v, data_v, "POST", function(resp) {
				if (resp) {
					let data = resp.data;
					_userInfo = resp.data;
					let utype = data.utype;
					let agencyId = data.agency_id;
					let url = null;
					if(utype == "A") {
						label_o.html("&nbsp;관리자");
						url = "/";
					} else if(utype == "D") {
						label_o.html("&nbsp;광고주");
						url = "/demand/";
					} else if(utype == "B") {
						label_o.html("&nbsp;대행사");
						url = "/agency/";
					} else if(utype == "S") {
						label_o.html("&nbsp;매체사");
						url = "/supply/";
					}
					url +="member/info/modify";
					
					$("#modifyUrl").attr({
						href:url
					});
					
					label_o.attr({
						"data-utype":utype
					});
					login_o.html(data.uid);

					// 비밀번호 변경 안내 모달
					if(data.is_expire == "Y") {
						$("#timerModal").modal("show");
					}
					
					//_timerInit();
					
					resolve();
				}
			});
		})
	}
	
	/*
 	세션, DB 만료시간 포함 타이머
	function _timerInit(data) {
		let time_o = $("#timer");
		let time = 10;
		let timer = null;

		let expireDate = new Date(data.expire_date);
		let expireTime = expireDate.getTime();
		let nowDate = new Date();
		let nowTime = nowDate.getTime();
		let savedTime = Math.round((expireTime - nowTime) / 1000);
		
		if(localStorage.getItem("timer")) {
			time = localStorage.getItem("timer");
		}

		// 세션에 저장된 시간이 DB에 저장된 만료시간 - 현재시간보다 클 경우 DB 만료시간 기준으로 타이머 설정
		if(time != 1800 || time > savedTime || savedTime == 1800) {
			localStorage.setItem("timer", savedTime);
			time = savedTime;
		}
		
		time_o.html(_setTimer(time));

		let startTime = new Date().getTime() + (time * 1000);		

		timer = setTimeout(function recursive() {
			time = startTime - new Date().getTime();
			
			let timeText = _setTimer(time - 1);
			time_o.html(timeText);

			localStorage.setItem("timer", time);

			if(time == 0) {
				clearTimeout(timer);
			} else {
				setTimeout(recursive, 1000);
			}
		});
	}
	*/
	
	// 타이머
	function timerInit() {
		_clearTime();
		
		let time = 60 * 30;
		let time_o = $("#timer").html((time / 60) + ":00");
		
		let startTime = new Date().getTime() + (time * 1000);

		timer = setInterval(function() {
			time = startTime - new Date().getTime();
			
			let timeText = _setTimer(time - 1);

			time_o.html(timeText);

			if(time <= 0) {
				_clearTime();
				time_o.html("00:00");
			}
		}, 1000);
	}
	
	// 타이머 초기화
	function _clearTime() {
		clearInterval(timer);
		timer = null;
	}
	
	// 타이머 텍스트 설정
	function _setTimer(time) {
		time = Math.ceil(time / 1000);
		let min = parseInt(time / 60);
		let sec = time % 60;
		
		if(String(min).length == 1) {
			min = String(min).padStart(2, '0');
		}
		if(String(sec).length == 1) {
			sec = String(sec).padStart(2, '0');
		}
		
		let timeText = min + ":" + sec; 

		return timeText;
	}
	
	// 이벤트 담당
	let _event = {
		// 헤더 메뉴 보이기
		mouseEnter: function() {
			$("#popInfoBox").css("display", "block");
		},
		
		// 헤더 메뉴 숨기기
		mouseLeave: function() {
			$("#popInfoBox").css("display", "none");
		},
		
		// 로그아웃
		clickLogout: function() {
			let url_v = "/logout";
			let data_v = "";
			
			comm.send(url_v, data_v, "POST", function() {
				localStorage.clear();
				location.replace("/login");
			});
		},
		
		// 로그인 연장
		clickExtension: function() {
			let url_v = "/login/extension";
			let data_v = "";
			
			comm.send(url_v, data_v, "POST", function() {
				localStorage.setItem("timer", 1800);
			});
		},
		
		// 비밀번호 변경
		clickModifyPw: function() {
			let passwd = $("input[name=passwd]").val();
			let newPasswd = $("input[name=new_passwd]").val();
			let confirmPasswd = $("input[name=confirm_passwd]").val();
			
			if(util.valNullChk(passwd) || util.valNullChk(newPasswd) || util.valNullChk(confirmPasswd)) {
				customModal.alert({
					content: "입력되지 않은 값이 있습니다."
				});
				
				return;
			}
			
			let url_v = "/member/modify/pw";
			
			let data_v = {
				"passwd": passwd,
				"new_passwd": newPasswd,
				"confirm_passwd": confirmPasswd,
			};
			
			comm.send(url_v, data_v, "POST", function(resp) {
				let code = resp.code;

				if(code == "2210") {
					customModal.alert({
						content: "비밀번호가 올바르지 않습니다.<br>다시 확인해주세요."
					});
				} else if(code == "2211") {
					customModal.alert({
						content: "현재 비밀번호와 동일하게 변경할 수 없습니다."
					});
				} else if(code == "2212") {
					customModal.alert({
						content: "현재 비밀번호가 일치하지 않습니다."
					});
				} else if(code == "2213") {
					customModal.alert({
						content: "새 비밀번호와 새 비밀번호 확인이 일치하지 않습니다."
					});
				} else if(resp.result == true) {
					customModal.alert({
						content: "비밀번호가 변경 되었습니다.",
						confirmCallback: function() {
							$("#timerModal").modal("hide");
						}
					});
				}
			});
		},
		
		// 30일 후에 변경
		clickModifyPwLater: function() {
			let url_v = "/member/modify/pw/later";
			let data_v = "";
			
			comm.send(url_v, data_v, "POST", function() {
				$("#timerModal").modal("hide");
			});
		}
	}
	
	//정보수정 url 셋팅
	function setModifyUrl(){
		
	}
	
	return {
		init,
		event: _event,
		userInfo: function(){
			return _userInfo;
		},
		timerInit,
	}
	
})();