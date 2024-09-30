const agencySimpleModify = (function(){
	
	function init(){
		_data.drawData();
		$('#selectEmail').selectpicker('refresh');
		_evInit()
	};
	
	function _evInit(){
		let evo = $("[data-src='agencySimpleModify'][data-act]").off();
		evo.on("click change keyup", function(ev){
			_action(ev);
		});
	}
	
	function _action(ev){
		let evo = $(ev.currentTarget);
		let act_v = evo.attr("data-act");
		let type_v = ev.type;
		let event = _event;
		
		if(type_v == "click") {
			//인증번호 발송
			if(act_v == "sendAuthCode"){
				event.sendAuthCode();
			//인증번호 체크
			}else if(act_v == "authKeyCheck"){
				event.authKeyCheck();
			//기본정보 수정
			}else if(act_v == "modifyMemberInfo"){
				event.modifyMembrer();
			}
		}else if(type_v == "change"){
			//도메인 변경시
			if(act_v == "changeDomain"){
				modifyMember.changeDomain();
			}
		}
	}
	
	let _event = {
		modifyMembrer: function(){
			let url_v = "/member/myInfo/modify"
			
			let data_v = modifyMember.getSimpleSubmitData();
			
			if(!modifyMember.simpleValidate(data_v)){
				return false;
			}
			
			comm.send(url_v, data_v, "POST", function(resp){
				if(resp.result){
					customModal.alert({
						content: "수정되었습니다.",
						confirmCallback: function() {
							location.href = "/agency/main";
						}
					});
				}else{
					let msg = "수정하실 정보를 다시 확인해주세요.";
					if(resp.code == 2212){
						msg = "현재 비밀번호가 일치하지 않습니다.";
					}else if(resp.code == 2213){
						msg = "새 비밀번호와 새 비밀번호 확인이 일치하지 않습니다.";
					}else if(resp.code == 2202){
						msg = "이미 존재하는 회원의 이메일입니다.";
					}else if(resp.code == 2208){
						msg = "인증번호가 올바르지 않습니다.";
					}else if(resp.code == 2210){
						msg = "영문, 숫자, 특수문자를 포함해 10자 이상으로 입력해 주세요."
					}
					
					customModal.alert({
						content: msg
					});
				}
			});
		},
		
		sendAuthCode: function(){
			modifyMember.sendAuthCode(null, function(resp){
				if(resp.result){
					//이메일 인증 타이머 초기화
					util.clearAuthTimer();
					//이메일 인증 타이머 설정
					util.setAuthTimer();
					customModal.alert({
						content: "입력해주신 메일주소로<br/>인증번호가 발송되었습니다.",
						confirmCallback: function() {
							$("#sendAuthCode").html("인증번호 다시 받기");
						}
					});
				}else{
					let msg = "인증번호를 발송할수없습니다</br>이메일 주소를 다시 확인해 주세요.";
					if(resp.code == 2202){
						msg = "이미 존재하는 회원의 이메일입니다.";
					}else if(resp.code == 2216){
						msg = "이전과 동일한 이메일입니다.";
					}
					
					customModal.alert({
						content: msg
					});
				}
			});
		},
		authKeyCheck: function(){
			modifyMember.authKeyCheck(null, function(resp){
				$("#emailConfirm").attr({
					"style" : ""
				});
				if(resp.result){
					$("#emailConfirm").html("이메일 인증이 완료되었습니다.");
					$("#authTimer").html("").hide();
					util.clearAuthTimer();
					
				}else{
					if(resp.code == 2207){
						$("#emailConfirm").html("인증시간이 만료되었습니다.");
					}else{
						$("#emailConfirm").html("인증번호가 올바르지 않습니다. 다시 입력해주세요.");
					}
				}
			});
		},
	}
	
	let _data = {
		drawData: function(){
			let data = header.userInfo();
			
			//기본정보
			{
				//이름
				$("#uName").html(data.uname);
				//id
				$("#uId").html(data.uid);
				//이메일
				{
					let companyEmail = data.company_email;
					
					if(companyEmail != null && companyEmail != ""){
						let companyEmailUserName = companyEmail.split("@")[0];
						let companyEmailDomain = companyEmail.split("@")[1];
						
						$("#companyEmail1").val(util.unescapeData(companyEmailUserName));
						$("#companyEmail2").val(util.unescapeData(companyEmailDomain));	
					}
				}
				//대표 url
				if(data.url != null && data.url != ""){
					$("#url").val(util.unescapeData(data.url));
				}
				//수신동의
				{
					if(data.accept_email == "Y"){
						$("#acceptEmail").attr({"checked":true});
					}
					if(data.accept_sms == "Y"){
						$("#acceptSms").attr({"checked":true});
					}
				}
			}
			_evInit();
		}
	}
	
	return{
		init,
	}
	
})();