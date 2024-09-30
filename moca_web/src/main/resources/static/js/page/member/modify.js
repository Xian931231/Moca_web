const modifyMember = (function(){
	
	let _companyEmail = null;
	let _oldCompanyEmail = null;
	let _authKey = "";
	
	//인증번호 발송 api
	function sendAuthCode(option, callback){
		let _option = {
            inputEmailId: $("#companyEmail1").val(),
            inputEmailDomain: $("#companyEmail2").val(),
        }
        
        _option = $.extend(_option, option);
		
		let url_v = "/member/signUp/send/email";
		
		if(!util.valNullChk(_option.inputEmailId) && !util.valNullChk(_option.inputEmailDomain)){
			let data_v = {"company_email" : _option.inputEmailId + "@" + _option.inputEmailDomain}
		
			comm.send(url_v, data_v, "POST", function(resp){
				if(typeof(callback) == "function"){
					callback(resp)
				}
			});
		}else{
			customModal.alert({
				content:"이메일 형식을 확인해주세요."
			});
		}
	}
	
	//인증번호 체크 api
	function authKeyCheck(option,callback){
		
		let _option = {
			authKey_v: $("#authKey").val(),
			emailId_v: $("#companyEmail1").val(),
            emailDomain_v: $("#companyEmail2").val(),
		}
		
		_option = $.extend(_option, option);
		
		let url_v = "/member/signUp/auth/check";
		
		let company_email = _option.emailId_v + "@" + _option.emailDomain_v;
		
		if(!util.valNullChk(_option.authKey_v)){
			//인증된 이메일, 인증코드 초기화
			_companyEmail = null;
			_authKey = null;
			
			let data_v = {
				"auth_value": _option.authKey_v,
				"company_email": company_email
			};
			
			comm.send(url_v, data_v, "POST", function(resp){
				if(resp.result){
					_companyEmail = company_email;
					_authKey = _option.authKey_v;
				}
				
				if(typeof(callback) == "function"){
					callback(resp);
				}
			});
			
		}
	}
	
	// 기본 정보 수정 api
	function modifyMembrer(values, callback){
		let url_v = "/member/modify/info";
		
		let data_v = getSubmitData();
		
		data_v = $.extend(data_v, values);
		
		if(!validate(data_v)){
			return false;
		}
		comm.send(url_v, data_v, "POST", function(resp){
			if(typeof(callback) == "function"){
				callback(resp)
			}
		});
	}
	
	//유효성 검사
	function validate(data){
		
		//비밀번호 체크
		if(!util.valNullChk(data.passwd) || !util.valNullChk(data.confirm_passwd)|| !util.valNullChk(data.new_passwd)){
			
			if(util.valNullChk(data.passwd)){
				customModal.alert({
					content: "기존 비밀 번호를 확인해주세요."
				});
				return false;	
			}
			
			if(util.valNullChk(data.new_passwd)|| util.valNullChk(data.confirm_passwd)){
				customModal.alert({
					content: "변경할 비밀 번호를 확인해주세요."
				});
				return false;
			}
			if(data.new_passwd != data.confirm_passwd){
				customModal.alert({
					content: "변경할 비밀 번호를 확인해주세요."
				});
				return false;
			}
		}
		
		//새비밀번호와 새비밀번호 확인 값이 서로같은지 확인
		if(data.new_passwd != data.confirm_passwd){
			customModal.alert({
				content: "변경할 비밀 번호를 확인해주세요."
			});
			return false;
		}
		
		//담당자 이메일 빈값 체크
		if(util.valNullChk(data.email)){
			customModal.alert({
				content: "담당자 이메일을 입력해 주세요."
			});
			return false;
		}
		
		return true;
	}
	
	// 기본정보, 업무 담당자 정보 데이터
	function getSubmitData(){
		let data = {};
		
		//비밀번호			
		data.passwd = $("#oldPassWord").val();
		data.new_passwd = $("#newPassWord").val();
		data.confirm_passwd = $("#reNewPassWord").val();
		
		//이메일
		if($("#companyEmail1").val() == null || $("#companyEmail1").val() == "" || $("#companyEmail2").val() == null || $("#companyEmail2").val() == ""){
			data.company_email = null;
		}else{
			data.company_email = $("#companyEmail1").val() + "@" + $("#companyEmail2").val();	
		}
		
		data.auth_value = _authKey;
		
		//대표 url
		data.url = $("#url").val();
		
		//수신 동의 email
		let acceptEmail = "N";
		if($("#acceptEmail").is(":checked")){
			acceptEmail = "Y";
		}
		data.accept_email = acceptEmail;
		
		//수신동의 sns
		let acceptSns = "N";
		if($("#acceptSms").is(":checked")) {
			acceptSns = "Y";
		}
		data.accept_sms = acceptSns;
		
		//담당자 이름
		data.uname = $("#uname").val();
		
		//담당자 연락처
		data.mobile = $("#mobile").val();
		
		//담당자 이메일
		data.email = $("#userEmail").val();
		
		return data;
	}
	
	//사업자 정보 수정 요청
	function modifyCompanyRegnumImage(){
		let file = $("#companyRegnumImage")[0].files[0];
		
		if(file == null){
			customModal.alert({
				content: "사업자 등록증을 업로드해주세요."
			});
			return false;
		}
		
		let fileSize = file.size;
		
		if(fileSize > 1024 * 200){
			customModal.alert({
				content: "사이즈가 200KB를 초과합니다."
			});
			return false;
		}
		
		let url_v = "/member/modify/request";
		let data_v = {
			"company_regnum_image" : file
		}
		
		let formData = util.getFormData(data_v);
		
		comm.sendFile(url_v, formData, "POST", function(resp){
			customModal.alert({
				content: "사업자 정보 수정을 요청했습니다."
				,confirmCallback: function(){
					location.href = "/"; 
				}
			});
		},function(){
			customModal.alert({
				content: "사업자 등록증<br/>업로드되지 않았습니다.",
			});
		});
	}
	
	// 도메인 변경시 발생하는 이벤트
	function changeDomain(option){
		let _option = {
            selectDomain: "selectDomain" ,
            inputEmailDomain: $("#companyEmail2"),
        }
        
        _option = $.extend(_option, option);
		
		
		let seletedEmail = $("#"+_option.selectDomain+" option:selected").val();
		$("#"+_option.selectDomain).selectpicker("val",seletedEmail);
		if(seletedEmail != "directInput") {
			_option.inputEmailDomain.val(seletedEmail);
			_option.inputEmailDomain.attr("readonly", true);
		} else {
			_option.inputEmailDomain.val("");
			_option.inputEmailDomain.attr("readonly", false);
		}
	}
	
	//사업자 등록증 변경시 이름 변경 이벤트
	function changeImg(option){
		let _option = {
			inputImage : $("#companyRegnumImage")
		}
		
		 _option = $.extend(_option, option);
		
		if(_option.inputImage.val()){
			let fileName = _option.inputImage[0].files[0].name;
			_option.inputImage.siblings(".custom-file-label").html(fileName);	
		}else{
			_option.inputImage.siblings(".custom-file-label").html(null);
		}
	}
	
	
	// 기본 정보와 동일 클릭
	function clickSameEmail(option){
		let _option ={
			emailId : $("#companyEmail1").val(),
			emailDomain : $("#companyEmail2").val()
		}
		
		 _option = $.extend(_option, option);
		
		if(_option.emailId == null || _option.emailId == "" || _option.emailDomain == null || _option.emailDomain == ""){
			customModal.alert({
				content: "이메일 정보를 정확히 입력해 주세요."
			});
			return false;
		}
		if($("#correctBase").is(":checked") == true) {
			$("#userEmail").val(_option.emailId + "@" + _option.emailDomain);
		}else{
			$("#userEmail").val("");
		}
	}
	//회원 탈퇴 요청
	function removeMember(passwd=$("#modalSearch").val(), callback){
		let url_v = "/member/leave/request";
		let data_v = {
			"passwd": passwd
		}
		
		comm.send(url_v, data_v, "POST", function(resp){
			if(typeof(callback) == "function"){
				callback(resp);
			}
		});
	}
	
	//기본 정보 데이터
	function getSimpleSubmitData(){
		let data = {};
		
		//비밀번호			
		data.passwd = $("#oldPassWord").val();
		data.new_passwd = $("#newPassWord").val();
		data.confirm_passwd = $("#reNewPassWord").val();
		
		//이메일
		if($("#companyEmail1").val() == null || $("#companyEmail1").val() == "" || $("#companyEmail2").val() == null || $("#companyEmail2").val() == ""){
			data.company_email = null;
		}else{
			data.company_email = $("#companyEmail1").val() + "@" + $("#companyEmail2").val();	
		}
		
		data.auth_value = _authKey;
		
		//대표 url
		data.url = $("#url").val();
		
		//수신 동의 email
		let acceptEmail = "N";
		if($("#acceptEmail").is(":checked")){
			acceptEmail = "Y";
		}
		data.accept_email = acceptEmail;
		
		//수신동의 sns
		let acceptSns = "N";
		if($("#acceptSms").is(":checked")) {
			acceptSns = "Y";
		}
		data.accept_sms = acceptSns;
		
		return data;
	}
	
	//유효성 검사
	function simpleValidate(data){
		
		//비밀번호 체크
		if(!util.valNullChk(data.passwd) || !util.valNullChk(data.confirm_passwd)|| !util.valNullChk(data.new_passwd)){
			
			if(util.valNullChk(data.passwd)){
				customModal.alert({
					content: "기존 비밀 번호를 확인해주세요."
				});
				return false;	
			}
			
			if(util.valNullChk(data.new_passwd)|| util.valNullChk(data.confirm_passwd)){
				customModal.alert({
					content: "변경할 비밀 번호를 확인해주세요."
				});
				return false;
			}
			if(data.new_passwd != data.confirm_passwd){
				customModal.alert({
					content: "변경할 비밀 번호를 확인해주세요."
				});
				return false;
			}
		}
		
		
		return true;
	}
	
	return{
		sendAuthCode,
		authKeyCheck,
		modifyMembrer,
		modifyCompanyRegnumImage,
		changeDomain,
		changeImg,
		clickSameEmail,
		removeMember,
		getSimpleSubmitData,
		simpleValidate
	}
})();