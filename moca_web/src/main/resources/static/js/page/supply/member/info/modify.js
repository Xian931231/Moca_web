const supplyMember = (function(){
	
	function init(){
		_data.getData();
		$('#selectEmail').selectpicker('refresh');
		_evInit()
	};
	
	function _evInit(){
		let evo = $("[data-src='supplyMember'][data-act]").off();
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
			//사업자 정보 수정
			}else if(act_v == "modifyCompanyRegnumImage"){
				modifyMember.modifyCompanyRegnumImage();
			//기본 정보 수정
			}else if(act_v == "modifyMemberInfo"){
				event.modifyMember();
			//이메일 정보 동일 체크
			}else if(act_v == "clickSameEmail"){
				modifyMember.clickSameEmail();
			//메인 페이지 이동
			}else if(act_v == "moveMainPage"){
				location.href="/";
			//회원 탈퇴
			}else if(act_v =="removeMember"){
				event.removeMember();
			//회원탈퇴 클릭
			}else if(act_v == "processingProductChk"){
				event.processingProductChk();
			}
		}else if(type_v == "change"){
			//도메인 변경시
			if(act_v == "changeDomain"){
				modifyMember.changeDomain();
				$("#emailConfirm").html("");
			//사업자 등록중 변경시
			}else if(act_v == "changeImg"){
				modifyMember.changeImg(evo);
			}
		}else if(type_v == "keyup"){
			//핸드폰 번호 수정시 자동 '-' 처리
			if(act_v == "autoHypenPhone"){
				let mobileNum = $("#mobile").val();
				$("#mobile").val(util.autoHypenPhone(mobileNum));
			}
		}
	}
	let _event = {
		modifyMember: function(){
			modifyMember.modifyMembrer(null, function(resp){
				if(resp.result){
					customModal.alert({
						content: "수정되었습니다.",
						confirmCallback: function() {
							location.href = "/";
						}
					});
				}else{
					let msg = "수정하실 정보를 다시 확인해주세요.";
					if(resp.code == 2204){
						msg = "이미 존재하는 회원의 핸드폰번호입니다.";
					}else if(resp.code == 2212){
						msg = "현재 비밀번호가 일치하지 않습니다.";
					}else if(resp.code == 2213){
						msg = "새 비밀번호와 새 비밀번호 확인이 일치하지 않습니다.";
					}else if(resp.code == 2202){
						msg = "이미 존재하는 회원의 이메일입니다.";
					}else if(resp.code == 2208){
						msg = "인증번호가 올바르지 않습니다.";
					}else if(resp.code == 2204){
						msg = "이미 존재하는 회원의 핸드폰입니다.";
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
		//종료되지 않은 상품이 있는지 확인 
		processingProductChk: function(){
			$("#modalSearch").val("");
			
			url_v = "/member/processing/product";
			comm.send(url_v, null, "POST", function(resp){
				if(resp.result){
					$("#allCategory").modal("show");
				}else {
					msg = "종료되지 않은 상품이 있어 </br>계속 진행할 수 없습니다.</br>문제가 지속될 경우 <a href='mailto:moca@innocean.com'>moca@innocean.com</a>로</br>연락 주시기 바랍니다.";
					
					customModal.alert({
						content: msg
						, confirmCallback: function(){
							$("#withdrawal").modal("hide");
						}
					});
				}
			});
		},
		removeMember: function(){
			modifyMember.removeMember($("#modalSearch").val(), function(resp){
				if(resp.result){
					customModal.alert({
						content: "회원 탈퇴 요청이 완료됐습니다.</br>플랫폼 이용이 제한됩니다."
						, confirmCallback: function(){
							header.event.clickLogout();
						}
					});
				}else{
					let msg = "탈퇴에 실패 했습니다.";
					
					if(resp.code == 2212){
						msg = "비밀번호를 다시 확인하고 입력해주세요.";
					}
					
					customModal.alert({
						content: msg
					});
				}
			});
		}
	}
	
	let _data = {
		getData: function(){
			let url_v = "/member/myData/get/masking";	
			comm.send(url_v, null, "POST", function(resp){
				_data.drawData(resp.data);
			});
		},
		drawData: function(data){
			//기본정보
			{
				//id
				$("#uid").html(data.uid);
				//이메일
				{
					let companyEmail = data.company_email;
					let companyEmailUserName = companyEmail.split("@")[0];
					let companyEmailDomain = companyEmail.split("@")[1];
					
					$("#companyEmail1").val(util.unescapeData(companyEmailUserName));
					$("#companyEmail2").val(util.unescapeData(companyEmailDomain));
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
			
			// 업무 담당자 정보
			{
				//담당자 이름
				$("#uname").attr({
					"placeholder":data.masked_uname
				});
				//담당자 연락처
				$("#mobile").attr({
					"placeholder":data.masked_mobile
				});
				//담당자 이메일
				$("#userEmail").val(util.unescapeData(data.email));
			}
			
			//사업자 정보
			{
				//상호(법인명)
				$("#companyName").val(util.unescapeData(data.company_name));
				//사업자등록번호
				$("#companyRegnum").val(util.unescapeData(data.company_regnum));
				//대표자 성명
				$("#ceoName").val(util.unescapeData(data.ceo_name));
				//업태
				$("#bizKind").val(util.unescapeData(data.biz_kind));
				//업종
				$("#bizType").val(util.unescapeData(data.biz_type));
				//사업장 주소
				$("#companyAddress1").val(util.unescapeData(data.address1));
				//상세주소
				$("#companyAddress2").val(util.unescapeData(data.address2));
			}
			_evInit();
		}
	}
	
	return{
		init,
	}
})();