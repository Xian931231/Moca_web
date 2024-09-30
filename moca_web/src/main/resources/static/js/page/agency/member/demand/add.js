const demandAdd = (function(){
	
	let _idCheck = 0;
	let _fileCheck = 0;
	
	// 페이지 초기화 함수
	function init(){
		_evInit();
	}
	
	// 이벤트 초기화 
	function _evInit(){
		let evo = $("[data-src='demandAdd'][data-act]").off();
		evo.on("click change blur keyup", function(ev) {
			_action(ev);
		});
	}
	
	//이벤트 분기
	function _action(ev){
		let evo = $(ev.currentTarget);
		let act_v = evo.attr("data-act");
		let type_v = ev.type;
		let event = _event;
		if(type_v == "click"){
			if(act_v == "searchPostcode"){
				postcodeInit();
			}else if(act_v == "clickIdCheck"){
				event.clickIdCheck();
			}else if(act_v == "addDemand"){
				event.addDemand();
			}else if(act_v =="prevPage"){
				location.href="/agency/member/demand/list";
			}
		}else if(type_v == "change"){
			if(act_v == "changeDomain"){
				changeDomain();
			}else if(act_v == "changeFile"){
				event.changeFile(evo);
			}
		}else if(type_v == "keyup"){
			//핸드폰 번호 수정시 자동 '-' 처리
			if(act_v == "autoHypenPhone"){
				let mobileNum = $("#mobile").val();
				$("#mobile").val(util.autoHypenPhone(mobileNum));
			}
		}else if(type_v == "blur"){
			if(act_v == "changeBiznum"){
				event.changeBiznum();
			}
		}
	}
	
	let _event = {
		//광고주 등록
		addDemand: function(){
			let url_v = "/member/agency/demand/add";
			let data_v = getSubmitData();
			
			if(!validateChk(data_v)){
				return false;
			}
			
			
			let formData = new FormData();
			
			let keys = Object.keys(data_v);
			for(let key of keys) {
				let value = data_v[key];
				formData.append(key, value);
			}
			
			comm.sendFile(url_v, formData, "POST", function(resp){
				if(resp.result){
					customModal.alert({
						content: "이메일로 임시 비밀번호를<br>전달했습니다."
						, confirmCallback: function(){
							location.href = "/agency/member/demand/list";
						}
					});
				}else{
					let code = resp.code;
					
					if(code == 2200) {
						loginCustomModal.alert({
							content: "이미 등록된 아이디입니다. 다른 아이디를 사용해주세요.",
						});
					} else if(code == 2201) {
						loginCustomModal.alert({
							content: "아이디는 영문과 숫자를 포함해 4자 이상 15자 이하로 입력해 주세요.",
						});
					} else if(code == 2202) {
						loginCustomModal.alert({
							content: "이미 등록된 이메일입니다.",
						});
					} else if(code == 2203) {
						loginCustomModal.alert({
							content: "유효하지 않은 이메일입니다.",
						});
					} else if(code == 2204) {
						loginCustomModal.alert({
							content: "이미 등록된 핸드폰 번호입니다.",
						});
					} else if(code == 2205) {
						loginCustomModal.alert({
							content: "유효하지 않은 핸드폰 번호입니다.",
						});
					}  else {
						loginCustomModal.alert({
							content: "회원가입에 실패했습니다. 다시 시도해 주세요.",
						});
					}
					
				}
			});
			
		},
		
		//아이디 유효성 검사 및 중복 체크
		clickIdCheck: function(){
			let id_v = $("#uid").val();
		
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
						
						_idCheck = 1;
					} else {
						let code = resp.code;
						if(code == 2200) {
							loginCustomModal.alert({
								content: "이미 등록된 아이디입니다.<br/>다른 아이디를 사용해주세요."
							});
						} else if(code == 2201) {
							loginCustomModal.alert({
								content: "영문과 숫자를 포함해<br/>4자 이상 15자 이하로 입력해주세요."
							});
						}
					}
				});
			}
		},
		changeFile: function(){
			let file = $("#companyRegnumImage")[0].files[0]
			
			if(file) {
				let fileName = file.name;
				let fileExt = fileName.substring(fileName.lastIndexOf(".") + 1).toUpperCase();
				
				let fileSize = file.size;
				if(fileExt != "JPEG" && fileExt != "JPG") {
					$("#fileAlert").html("확장자가 올바르지 않습니다.");
				} else if(fileSize > 1024 * 1024 * 200) {
					$("#fileAlert").html("사이즈가 200MB를 초과합니다.");
				} else {
					$("#fileAlert").html("파일이 첨부되었습니다.");
					_fileCheck = 1;
				}
				$("#companyRegnumImage").siblings(".custom-file-label").html(fileName);
			} else {
				$("#companyRegnumImage").siblings(".custom-file-label").html(null);
				$("#fileAlert").html("");
				_fileCheck = 0;
			}
		},
		
		// 사업자등록번호 입력
		changeBiznum: function() {
			let regnum = $("#companyRegnum");
			
			// 숫자만
			regnum.val(regnum.val().replace(/[^0-9]/g, ''));
			
			// 법인 체크
			let corpo = regnum.val().substring(3,5);
			
			const corpoList = ["81", "82", "83", "84", "85", "86", "87", "88"];
			for(let num of corpoList) {
				if(num == corpo && regnum.val().length == 10) {
					$("#regnumNotice").html("");
					$("#companyRegnum").attr({"data-check": 1});
					
					return;
				} else {
					$("#regnumNotice").html("사업자 번호가 올바르지 않습니다.");
					$("#companyRegnum").attr({"data-check": 0});
				}
			}
		},
		
	}
	
	//광고주 정보
	function getSubmitData(){
		let data ={};
		//회원 타입
		data.utype = "D"
		//상호명
		data.company_name = $("#companyName").val();
		//사업자 등록 번호
		data.company_regnum = $("#companyRegnum").val();
		//대표자 성명
		data.ceo_name = $("#ceoName").val();
		//업태
		data.biz_type = $("#bizType").val();
		//업종
		data.biz_kind = $("#bizKind").val();
		//사업장 주소
		data.address1 = $("#address1").val();
		//상세주소
		data.adddress2 = $("#address2").val();
		//주소번호
		data.zipcode = $("#zipcode").val();
		//사업자 등록증
		data.company_regnum_image = $("#companyRegnumImage")[0].files[0];
		//아이디
		data.uid = $("#uid").val();
		//담당자 이메일
		data.company_email1 = $("#companyEmail1").val();
		data.company_email2 = $("#companyEmail2").val();
		data.company_email = data.company_email1 + "@" + data.company_email2
		//담당자 이름
		data.uname = $("#uname").val();
		//담당자 연락처
		data.mobile = $("#mobile").val();
		
		
		return	data;
	}
	//유효성 검사
	function validateChk(data_v){
		
		let regNumCheck = $("data-check").attr("data-check");
		if(regNumCheck == 0){
			loginCustomModal.alert({
				content: "사업자 번호를 다시 확인해주세요."
			});
			return false;
		}
		
		for(let data in data_v) {
			if(data_v[data] == null || data_v[data] == "") {
				loginCustomModal.alert({
					content: "입력되지 않은 항목이 있습니다.<br/>모든 정보를 입력해주세요.",
				});
				return false;
			}
		}
		
		if(_idCheck == 0) {
			loginCustomModal.alert({
				content: "아이디 사용가능 여부 확인을 해주세요."
			});
			return false;
		}
		
		if(_fileCheck == 0) {
			loginCustomModal.alert({
				content: "올바르지 않은 사업자 등록증입니다."
			});
			return false;
		}
		
		return true;
	}
	// 다음 주소찾기
	function postcodeInit(){
		new daum.Postcode({
        	oncomplete: function(data) {
                let address = ""; // 주소
		        let zipcode_o = $("#zipcode"); // 우편번호
		        let address1_o = $("#address1"); // 주소
		        let address2_o = $("#address2"); // 상세주소
		
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
	
	return {
		init,
	}
	
})();