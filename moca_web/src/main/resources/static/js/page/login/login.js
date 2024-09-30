const login = (function() {
	
	// 해당 페이지 초기화 함수
	function init() {
		_evInit();
		_sessionClear();
	}
	
	// 이벤트 초기화 
	function _evInit() {
		let evo = $("[data-src='login'][data-act]").off();
		evo.on("click keyup", function(ev) {
			_action(ev);
		});
	}
	
	// 세션 초기화
	function _sessionClear() {
		localStorage.clear();
	}
	
	// 이벤트 분기 함수
	function _action(ev) {
		let evo = $(ev.currentTarget);
		
		let act_v = evo.attr("data-act");
		
		let type_v = ev.type;
		
		let event = _event;
		
		const container = $("#container");
		
		if(type_v == "click") {
			if(act_v == "clickJoin") {
				event.clickJoin();
			} else if(act_v == "signUp") {
				event.clickSignUp(container);
			} else if(act_v == "signIn") {
				event.clickSignIn(container);
			} else if(act_v == "clickLogin") {
				event.clickLogin();
			} else if(act_v == "clickFindId") {
				location.href = "/login/findId";
			} else if(act_v == "clickFindPw") {
				location.href = "/login/findPw";
			} else if(act_v == "clickLogo") {
				location.href = "/login";
			}
		} else if(type_v == "keyup") {
			if(act_v == "loginId" || act_v == "loginPw") {
				if(ev.keyCode == 13) {
					event.clickLogin();
				}
			}
		}
	}
	
	// 이벤트 담당
	let _event = {
		// 가입하기
		clickJoin: function() {
			if ($("#age_above").is(":checked") == true){
				let type = $("input[name=select-type]:checked").val();
				if(util.valNullChk(type)) {
					customModal.alert({
						content: "가입 유형을 선택해주세요."
					});
					return;
				}
				location.href = "/signup/join?type=" + type;
		    } else {
		    	loginCustomModal.alert({
					content: "14세 미만은 가입하실 수 없습니다.",
				});
		    }
		},
		
		// 회원가입(패널변경)
		clickSignUp: function(container) {
			container.addClass("right-panel-active");
		},
		
		// 로그인(패널변경)
		clickSignIn: function(container) {
			container.removeClass("right-panel-active");
		},
		
		// 로그인 클릭
		clickLogin: function() {
			// 공백 체크
	   		if($("#loginId").val() == "") {
	    		loginCustomModal.alert({
					content: "아이디가 입력되지 않았습니다.",
				});
	   			$("#loginId").focus();
	    		return;
	    	} else if($("#loginPw").val() == "") {
	    		loginCustomModal.alert({
					content: "비밀번호가 입력되지 않았습니다.",
				});
	    		$("#loginPw").focus();
	    		return;
	    	} else {
				if($("#loginBtn").attr("is-run") == 0) {
					$("#loginBtn").attr({"is-run": 1});
					_event.loginProcess();
				}
			}
		},
		
		// 로그인 실행
		loginProcess: function (duplicateYn) {
			let url_v = "/login";
			if(duplicateYn == "Y") {
				url_v = "/login/duplicate";
			}
			
			let data_v = {
				uid : $("#loginId").val(),
				passwd : $("#loginPw").val()
			}
			
			comm.send(url_v, data_v, "POST", function(resp) {
				let code = resp.code;
				let content = "";
				$("#loginBtn").attr({"is-run": 0});
				
				if(resp.result == false) {
					if(code == "1001") {
						content = "탈퇴한 회원입니다.";
					} else if(code == "1002") {
						content = "인증 대기 중인 회원입니다.";
					} else if(code == "1003") {
						content = "중단된 회원입니다.";
					} else if(code == "1004") {
						content = "휴면회원입니다.";
					} else if(code == "1005") {
						loginCustomModal.confirm({
							content: "접속이 제한된 계정입니다.<br/>비밀번호를 다시 설정하고 접속해주세요.",
							confirmText: "비밀번호 재설정",
							confirmCallback: function() {
								location.href = "/login/findPw";
							},
							cancelText: "확인",
						});
					} else if(code == "1009") {
						loginCustomModal.confirm({
							content: "다른 기기/브라우저에서 로그인 되어있습니다.<br>로그아웃 시키시겠습니까?",
							confirmCallback: function() {
								_event.loginProcess("Y");
							},
						});
					} else if(code == "1010") {
						content = "회원가입 승인 대기중인 계정입니다.<br>승인 완료 후 로그인 해주세요.";
					} else if(code == "1011") {
						content = "승인 거부된 계정입니다.";
					} else {
						content = "로그인에 실패했습니다.<br/>연속적인 오류 발생 시 접속이 제한됩니다.";
					}
					
					if(content != "") {
						loginCustomModal.alert({
							content: content,
						});
					}
				} else {
					location.reload();
				}
			});
		},
	}
	
	return {
		init,
	}
	
})();