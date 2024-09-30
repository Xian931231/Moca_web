const join = (function() {
	
	const _urlParam = util.getUrlParam();
	
	// 해당 페이지 초기화 함수
	function init() {
		// url param 체크
		if(!util.valNullChk(_urlParam) && (_urlParam.type == "demand" || _urlParam.type == "agency" || _urlParam.type == "supply")) {
			_setTerms();
			_evInit();
		} else {
			location.href = "/";
		}
	}
	
	// 이벤트 초기화 
	function _evInit() {
		let evo = $("[data-src='join'][data-act]").off();
		evo.on("click change blur focus", function(ev) {
			_action(ev);
		});
	}
	
	// 이벤트 분기 함수
	function _action(ev) {
		let evo = $(ev.currentTarget);
		
		let act_v = evo.attr("data-act");
		
		let type_v = ev.type;

		let event = _event;
		
		if(type_v == "click") { // 클릭 이벤트
			if(act_v == "agree") {
				event.clickAgree(evo);
			} else if(act_v == "allAgree") {
				event.clickAllAgree(evo);
			} else if(act_v == "clickPrev") {
				location.href = "/login";
			} else if(act_v == "clickComplete") {
				event.clickAgreeComplete();
			} else if(act_v == "clickPrevJoin") {
				event.clickPrevJoin();
			} else if(act_v == "clickIdCheck") {
				event.clickIdCheck();
			} else if(act_v == "clickAuthSend") {
				event.clickAuthSend();
			} else if(act_v == "clickAuthCheck") {
				event.clickAuthCheck();
			} else if(act_v == "clickPostcode") {
				_postcodeInit();
			} else if(act_v == "clickCorrectBase") {
				event.clickCorrectBase();
			} else if(act_v == "clickSignUp") {
				event.clickSignUp();
			} else if(act_v == "clickLogo") {
				location.href = "/login";
			}
		} else if(type_v == "change") { // 체인지 이벤트
			if(act_v == "selectEmail") {
				event.changeSelectedEmail();
			} else if(act_v == "changeFile") {
				event.changeFile(evo);
			}
		} else if(type_v == "blur") { // 포커스 이벤트
			if(act_v == "comfirmPasswd") {
				event.checkPasswd();
			} else if(act_v == "changeMobile") {
				event.changeMobile();
			} else if(act_v == "changeBiznum") {
				event.changeBiznum();
			}
		} else if(type_v == "focus") {
			if(act_v == "clickAddressInput") {
				event.clickAddressInput();
			}
		}
	}
	
	// 광고주, 대행사, 매체사에 따른 약관 셋팅
	function _setTerms() {
		$(".jogin-section").css("display", "none");
		let type = _urlParam.type;
		{
			let h_o = $("h2[name=joinType]");
		
			if(type == "demand") {
				h_o.html("광고주 회원가입");
			} else if(type == "agency") {
				h_o.html("대행사 회원가입");
			} else if(type == "supply") {
				h_o.html("매체 회원가입");
				$("#serviceTerms").attr({"rows": 30});
			}
		}
		{
			// 공통 서비스 이용약관
			let text_o = $("#serviceTerms");
			let xmlHttp = new XMLHttpRequest();		
			
			xmlHttp.open("GET", "/assets/text/serviceTerms.txt", true);
			xmlHttp.send();
	
			xmlHttp.onreadystatechange = function() { 
			    if(this.status == 200 && this.readyState == this.DONE) {
			        let terms = xmlHttp.responseText;
			        text_o.html(terms);            
			    }
			};
		}
		{
			if(type != "supply") {
				let ul_o = $("#termsList");
				let xmlHttp = new XMLHttpRequest();
				let html = "";
				
				{
					let li_o = $("<li>").addClass("join-consent");
					ul_o.append(li_o);
					
					let p_o = $("<p>");
					li_o.append(p_o);
					
					let text_o = $("<textarea>").attr({
						"name": "join-textarea",
						"cols": "100",
						"rows": "15",
						"readonly": true
					});
					li_o.append(text_o);
					
					if(type == "demand") {
						html = "/assets/text/demandTerms.txt";
						p_o.html("광고주 이용약관");
					} else if(type == "agency") {
						html = "/assets/text/agencyTerms.txt";
						p_o.html("대행사 이용약관");
					}
					
					xmlHttp.open("GET", html, true);
					xmlHttp.send();
			
					xmlHttp.onreadystatechange = function() { 
					    if(this.status == 200 && this.readyState == this.DONE) {
					        let terms = xmlHttp.responseText;
					        text_o.html(terms);            
					    }
					};
				}
				{
					let li_o = $("<li>").addClass("terms-join-checkbox");
					ul_o.append(li_o);
					
					let input_o = $("<input>").attr({
						"type": "checkbox",
						"class": "chk",
						"name": "agree",
						"id": "consent-ck2",
						"checked": true,
						"data-src": "join",
						"data-act": "agree"
					});
					li_o.append(input_o);
					
					let label_o = $("<label>").attr({
						"for": "consent-ck2"						
					})
					li_o.append(label_o);
					
					let span_o = $("<span>").html("동의합니다.");
					li_o.append(span_o);
				}
			}
		}
	}
	
	// 이벤트 담당
	let _event = {
		// 동의합니다 클릭
		clickAgree: function(evo) {
			if ($(evo).is(":checked") == false) {
				$("#consent").prop("checked", false);
			}
			
			// 동의합니다 모두 체크 시 전체동의 선택
			let agree = $("input[name=agree]").length;
			let agreeCnt = $("input[name=agree]:checked").length;
			
			if(agree == agreeCnt) {
				$("#consent").prop("checked", true);
			}
		},
		
		// 전체동의 클릭
		clickAllAgree: function(evo) {
			let checked = $(evo).is(":checked");

	    	if(checked) {
	    		$(evo).parents(".terms-wrap").find("input[name=agree]").prop("checked", true);
	    	} else {
				$(evo).parents(".terms-wrap").find("input[name=agree]").prop("checked", false);
			}
		},
		
		// 동의 및 정보입력 완료 클릭
		clickAgreeComplete: function() {
			let agree = $("input[name=agree]").length;
			let agreeCnt = $("input[name=agree]:checked").length;
			
			if(agree == agreeCnt) {
				$(".terms-section").css("display", "none");
				$(".jogin-section").css("display", "");
				$("html").scrollTop(0);
			} else {
				loginCustomModal.alert({
					content: "모든 약관에 동의 하셔야<br>다음 단계로 진행 가능합니다.",
				});
			}
		},
		
		// 이전 단계 클릭
		clickPrevJoin: function() {
			$(".jogin-section").css("display", "none");
			$(".terms-section").css("display", "");
			$("html").scrollTop(0);
		},
		
		// 아이디 중복 확인
		clickIdCheck: function() {
			let id_o = $("#joinUid");
			let id_v = id_o.val();
			
			if(!util.valNullChk(id_v)) {
				let url_v = "/member/signUp/duplicate/id";
				
				let data_v = {
					"uid": id_v
				}
				
				comm.send(url_v, data_v, "POST", function(resp) {
					if(resp.result == true) {
						loginCustomModal.alert({
							content: "사용 가능한 아이디 입니다."
						});
						
						id_o.attr({
							"id-check": 1,
							"id-value": id_v
						});
					} else {
						let code = resp.code;
						if(code == 2200) {
							loginCustomModal.alert({
								content: "이미 등록된 아이디입니다.<br/>다른 아이디를 사용해주세요."
							});
						} else if(code == 2201) {
							loginCustomModal.alert({
								content: "영문과 숫자를 포함해<br/>6자 이상 15자 이하로 입력해주세요."
							});
						}
						
						id_o.attr({
							"id-check": 0,
							"id-value": ""
						});
					}
				});
			}
		},
		
		// 비밀번호 확인
		checkPasswd: function() {
			let passwd_o = $("#joinPasswd");
			let passwd_v = passwd_o.val();
			let confirm_passwd_o = $("#joinConfirmPasswd");
			let confirm_passwd_v = confirm_passwd_o.val();
			
			if(util.valNullChk(passwd_v) || util.valNullChk(confirm_passwd_v)) {
				$("#passwdConfirmText").html("");
				passwd_o.attr({"pw-check": 0});
				confirm_passwd_o.attr({"pw-check": 0});
			} else {
				if(passwd_v != confirm_passwd_v) {
					$("#passwdConfirmText").html("비밀번호가 일치하지 않습니다.");	
					passwd_o.attr({"pw-check": 0});
					confirm_passwd_o.attr({"pw-check": 0});
				} else {
					$("#passwdConfirmText").html("비밀번호가 일치합니다.");
					passwd_o.attr({"pw-check": 1});
					confirm_passwd_o.attr({"pw-check": 1});
				}
			}
		},
		
		// 연락처 입력
		changeMobile: function() {
			$("#joinMobile").val($("#joinMobile").val().replace(/[^0-9]/g, ''));
		},
		
		// 사업자등록번호 입력
		changeBiznum: function() {
			let regnum = $("#joinCompanyRegnum");
			
			// 숫자만
			regnum.val(regnum.val().replace(/[^0-9]/g, ''));
			
			// 법인 체크
			let corpo = regnum.val().substring(3,5);
			
			const corpoList = ["81", "82", "83", "84", "85", "86", "87", "88"];
			for(let num of corpoList) {
				if(num == corpo && regnum.val().length == 10) {
					$("#regnumNotice").html("");
					$("#joinCompanyRegnum").attr({"regnum-check": 1});
					
					return;
				} else {
					$("#regnumNotice").html("사업자 번호가 올바르지 않습니다.");
					$("#joinCompanyRegnum").attr({"regnum-check": 0});
				}
			}
		},
		
		// 주소입력 창 클릭
		clickAddressInput: function() {
			$("#postcodeBtn").trigger("click");
			$("#joinAddress2").focus();
		},
		
		// 이메일 주소 선택
		changeSelectedEmail: function() {
			let seletedEmail = $("#selectEmailAddress option:selected").val();
			if(seletedEmail != "directInput") {
				$("#joinCompanyEmailAddress").val(seletedEmail);
				$("#joinCompanyEmailAddress").attr("readonly", true);
			} else {
				$("#joinCompanyEmailAddress").attr("readonly", false);
			}
		},
		
		// 인증번호 전송
		clickAuthSend: function() {
			let email1_v = $("#joinCompanyEmail").val();
			let email2_v = $("#joinCompanyEmailAddress").val();
		
			if(!util.valNullChk(email1_v) && !util.valNullChk(email2_v)) {
				let url_v = "/member/signUp/send/email";
				
				let data_v = {
					"company_email": email1_v + "@" + email2_v,
				}
				
				comm.send(url_v, data_v, "POST", function(resp) {
					if(resp.result == true) {
						//이메일 인증 타이머 초기화
						util.clearAuthTimer();
						//이메일 인증 타이머 설정
						util.setAuthTimer();
						loginCustomModal.alert({
							content: "입력해주신 메일주소로<br/>인증번호가 발송되었습니다.",
							confirmCallback: function() {
								$("#authBtn").html("인증번호 다시 받기");
							}
						});
						
						$("#joinCompanyEmail").attr({
							"email-value": email1_v,
							"readonly": true
						});
						$("#joinCompanyEmailAddress").attr({
							"email-address-value": email2_v,
							"readonly": true
						})
						$("#selectEmailAddress").attr({
							"disabled": true
						})
					} else {
						if(resp.code == 2202) {
							loginCustomModal.alert({
								content: "이미 사용 중인 이메일입니다.",
							});
						} else {
							loginCustomModal.alert({
								content: "인증번호를 발송할 수 없습니다.<br/>이메일 주소를 다시 확인해 주세요.",
							});
						}
						
						$("#joinCompanyEmail").attr({
							"email-value": ""
						});
						$("#joinCompanyEmailAddress").attr({
							"email-address-value": ""
						})
					}
				});
			}
		},
		
		// 인증번호 확인
		clickAuthCheck: function() {
			let auth_v = $("#authNumber").val();
			let email_v = $("#joinCompanyEmail").val();
			let emailAddress_v = $("#joinCompanyEmailAddress").val();
			
			if(!util.valNullChk(auth_v)) {
				let url_v = "/member/signUp/auth/check";
				
				let data_v = {
					"company_email": email_v + "@" + emailAddress_v,
					"auth_value": auth_v
				}
				
				comm.send(url_v, data_v, "POST", function(resp) {
					if(resp.result == true) {
						$("#emailConfirm").html("이메일 인증이 완료되었습니다.");
						$("#authTimer").html("").hide();
						//이메일 인증 타이머 초기화
						util.clearAuthTimer();

						$("#authNumber").attr({
							"auth-check": 1,
							"auth-value": auth_v,
						});
					} else {
						if(resp.code == 2207) {
							$("#emailConfirm").html("인증시간이 만료되었습니다.");
						} else {
							$("#emailConfirm").html("인증번호가 올바르지 않습니다. 다시 입력해주세요.");
						}
						
						$("#authNumber").attr({
							"auth-check": 0,
							"auth-value": "",
						});
					}
				});
			}
		},
		
		// 기본 정보와 동일 클릭
		clickCorrectBase: function() {
			let email = $("#joinCompanyEmail").val() + "@" + $("#joinCompanyEmailAddress").val();
			
			if($("#correctBase").is(":checked") == true) {
				$("#joinUemail").val(email);	
			} else {
				$("#joinUemail").val("");	
			}
		},
		
		// 회원가입 완료 클릭
		clickSignUp: function() {
			let url_v = "/member/signUp/add";
			
			// 가입유형
			let type = _urlParam.type;
			let utype = "";
			if(type == "demand") {
				utype = "D"
			} else if(type == "agency") {
				utype = "B"
			} else if(type == "supply") {
				utype = "S"
			}
			
			// 수신동의
			let acceptEmail = "N"; 
			if($("#email").is(":checked")) {
				acceptEmail = "Y";
			}
			let acceptSms = "N";
			if($("#sns").is(":checked")) {
				acceptSms = "Y";
			}
			
			// 필수값
			let data_v = {
				"company_name": $("#joinCompanyName").val(),
				"company_regnum": ($("#joinCompanyRegnum").val()).toString(),
				"ceo_name": $("#joinCeoName").val(),
				"biz_type": $("#joinBizType").val(),
				"biz_kind": $("#joinBizKind").val(),
				"address1": $("#joinAddress1").val(),
				"address2": $("#joinAddress2").val(),
				"zipcode": ($("#joinZipcode").val()).toString(),
				"company_regnum_image": $("#joinCompanyRegnumImage")[0].files[0],
				"uid": $("#joinUid").val(),
				"passwd": $("#joinPasswd").val(),
				"confirm_passwd": $("#joinConfirmPasswd").val(),
				"company_email1": $("#joinCompanyEmail").attr("email-value"),
				"company_email2": $("#joinCompanyEmailAddress").attr("email-address-value"),
				"company_email": $("#joinCompanyEmail").attr("email-value") + "@" + $("#joinCompanyEmailAddress").attr("email-address-value"),
				"accept_email": acceptEmail,
				"accept_sms": acceptSms,
				"uname": $("#joinUname").val(),
				"mobile": ($("#joinMobile").val()).toString(),
				"email": $("#joinUemail").val(),
				"utype": utype
			}

			// 필수값 null 체크
			for(let data in data_v) {
				if(data_v[data] == null || data_v[data] == "") {
					loginCustomModal.alert({
						content: "입력되지 않은 항목이 있습니다.<br/>모든 정보를 입력해주세요.",
					});
					return;
				}
			}
			
			// 사업자등록번호 확인
			if($("#joinCompanyRegnum").attr("regnum-check") != 1) {
				loginCustomModal.alert({
					content: "사업자 번호가 올바르지 않습니다."
				});
				return;
			}
			
			// 사업자등록증 파일 유효성 확인
			if($("#joinCompanyRegnumImage").attr("file-check") != 1) {
				loginCustomModal.alert({
					content: "올바르지 않은 사업자 등록증입니다."
				});
				return;
			}
			
			// id 중복체크 유무 확인
			if($("#joinUid").attr("id-check") != 1 || $("#joinUid").val() != $("#joinUid").attr("id-value")) {
				loginCustomModal.alert({
					content: "아이디 사용가능 여부 확인을 해주세요."
				});
				return;
			}
			
			// 비밀번호 일치 확인
			if($("#joinPasswd").attr("pw-check") != 1 || $("#joinConfirmPasswd").attr("pw-check") != 1) {
				loginCustomModal.alert({
					content: "비밀번호가 일치하지 않습니다."
				});
				return;
			}
			
			// 인증번호 null 체크
			let auth_o = $("#authNumber");
			if(util.valNullChk(auth_o.val())) {
				loginCustomModal.alert({
					content: "인증번호를 입력해주세요."
				});
				return;
			} else {
				data_v.auth_value = auth_o.val();
			}
			
			// 이메일 인증 유무 확인
			if(auth_o.attr("auth-check") != 1 || auth_o.attr("auth-value") != auth_o.val() || $("#joinCompanyEmail").attr("email-value") != $("#joinCompanyEmail").val() || $("#joinCompanyEmailAddress").attr("email-address-value") != $("#joinCompanyEmailAddress").val()) {
				loginCustomModal.alert({
					content: "이메일 인증을 해주세요."
				});
				return;
			}

			// 가입자 URL (선택)
			data_v.url = $("#joinUrl").val();
			
			// 데이터를 FormData 변경
			let formData = util.getFormData(data_v);
			
			comm.sendFile(url_v, formData, "POST", function(resp) {
				if(resp.result == true) {
					let content = "";
					if(utype == "S") {
						content = "회원가입 신청을 완료했습니다.<br>승인이 완료되면 입력하신 이메일로 안내드립니다.";
					} else {
						content = "회원 가입이 완료되었습니다.<br/>가입하신 정보로 로그인해주세요.";
					}
					
					loginCustomModal.alert({
						content: content,
						confirmCallback: function() {
							location.href = "/login";
						}
					});
				} else {
					let code = resp.code;
					
					if(code == 2200) {
						loginCustomModal.alert({
							content: "이미 등록된 아이디입니다. 다른 아이디를 사용해주세요.",
						});
					} else if(code == 2201) {
						loginCustomModal.alert({
							content: "아이디는 영문과 숫자를 포함해 6자 이상 15자 이하로 입력해 주세요.",
						});
					} else if(code == 2202) {
						loginCustomModal.alert({
							content: "이미 등록된 이메일입니다.",
						});
					} else if(code == 2203) {
						loginCustomModal.alert({
							content: "유효하지 않은 이메일입니다.",
						});
					} else if(code == 2210) {
						loginCustomModal.alert({
							content: "비밀번호가 올바르지 않습니다.<br>다시 확인해주세요.",
						});
					} else {
						loginCustomModal.alert({
							content: "회원가입에 실패했습니다. 다시 시도해 주세요.",
						});
					}
				}
			});
		},
		
		// 파일 업로드
		changeFile: function(evo) {
			let fileName = $(evo).val().split("\\").pop();
			let file_o = $("#joinCompanyRegnumImage");
			file_o.siblings(".custom-file-label").html(fileName);

			let fileExt = fileName.substring(fileName.lastIndexOf(".") + 1).toUpperCase();
			let file = file_o.get(0).files[0];
			if(file) {
				let fileSize = file.size;
				if(fileExt != "PNG" && fileExt != "JPG" && fileExt != "PDF") {
					$("#fileAlert").html("확장자가 올바르지 않습니다.");
					file_o.attr({"file-check": 0});
				} else if(fileSize > 1024 * 200) {
					$("#fileAlert").html("사이즈가 200KB를 초과합니다.");
					file_o.attr({"file-check": 0});
				} else {
					$("#fileAlert").html("");
					file_o.attr({"file-check": 1});
				}
			} else {
				file_o.attr({"file-check": 0});
			}
		}
	}
	
	// 다음 주소찾기
	function _postcodeInit() {
		new daum.Postcode({
        	oncomplete: function(data) {
                let address = ""; // 주소
		        let zipcode_o = $("#joinZipcode"); // 우편번호
		        let address1_o = $("#joinAddress1"); // 주소
		        let address2_o = $("#joinAddress2"); // 상세주소
		
		        if (data.userSelectedType == "R") { // 도로명 주소
		            address = data.roadAddress;
		        } else { // 지번 주소
		            address = data.jibunAddress;
		        };
		        
				// 각 필드에 주소값을 넣는다.
		        zipcode_o.val(data.zonecode);
		        address1_o.val(address);
		        
		        // 상세주소 칸으로 커서 이동
		        address2_o.focus();
            }
        }).open();
	}
	
	return {
		init,
	}
	
})();