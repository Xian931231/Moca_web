const util = (function(){
	
	let _timer = null;
	
	function getUUID(){ 
		// UUID용 변수
		var lut = []; 
		for (var i=0; i<256; i++) { 
			lut[i] = (i<16?'0':'')+(i).toString(16); 
		};
		
		var d0 = Math.random()*0xffffffff|0;
	    var d1 = Math.random()*0xffffffff|0;
	    var d2 = Math.random()*0xffffffff|0;
	    var d3 = Math.random()*0xffffffff|0;
	    return lut[d0&0xff]+lut[d0>>8&0xff]+lut[d0>>16&0xff]+lut[d0>>24&0xff]+'-'+
	      lut[d1&0xff]+lut[d1>>8&0xff]+'-'+lut[d1>>16&0x0f|0x40]+lut[d1>>24&0xff]+'-'+
	      lut[d2&0x3f|0x80]+lut[d2>>8&0xff]+'-'+lut[d2>>16&0xff]+lut[d2>>24&0xff]+
	      lut[d3&0xff]+lut[d3>>8&0xff]+lut[d3>>16&0xff]+lut[d3>>24&0xff];
	}
	
	function aesEncode(plain_text){
		GibberishAES.size(256);
		var tmp = GibberishAES.aesEncrypt(plain_text, getSecKey());
		tmp = tmp.replace(/[\r|\n]/g, '');
	    return tmp;
	}

	function aesDecode(base64_text){
		try {
			GibberishAES.size(256);
			var tmp = GibberishAES.aesDecrypt(base64_text, getSecKey());
		    return tmp;
		} catch(e) {
			console.error(e);
			setCookie("new_secret_key", "", -1);
			location.href = "/";
		}
		return null;
	}
	
	function getCookie(key){
		let result;
		const cookie = document.cookie.split(";");
		
		cookie.some((v) => {
			v = v.replaceAll(" ", "");
			
			let item = v.split("=");
			
			if(item[0] === key) {
				result = item[1];
				return true;
			}
		});
		
		return result;
	}
	
	function setCookie(key, value, day){
		var cookie = key + "=" + encodeURI(value) + ";path=/ ;";
		
		if(day != null) {
			var expire = new Date();
			
			expire.setDate(expire.getDate() + day);
			
			cookie += "expires=" + expire.toGMTString() + ";";
		}
		
		document.cookie = cookie;
	}
	
	function getUrlParam() {
		let data = null;
		let search = location.search.substring(1);
		
		if(search) {
			data = {};
			
			let split = search.split("&");
			
			for(let str of split) {
				let split2 = str.split("=");
				
				let key = split2[0];
				let value = split2[1];
				
				if(!isNaN(value)) {
					value = parseInt(value);
				}
				
				data[key] = value;
			}
		}
		return data;
	}
	
	// 숫자 3자리 마다 콤마 찍어서 반환
    function numberWithComma(num) {
    	if(num == null || num == undefined) return "";
    	// IOS 이슈 lockbehind
    	// num.toString().replace(/\B(?<!\.\d*)(?=(\d{3})+(?!\d))/g, ",");
    	return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
    }
    
    //체크박스 전체 체크, 해제 
    function setCheckBox(element) {
  		let allCheckbox_o = $(element);
        let table_o = allCheckbox_o.closest("table");
        
        let checkbox = $(table_o).find("tbody input[type='checkbox']");
        if(allCheckbox_o.prop("checked")) {
			checkbox.prop("checked", true);
		} else {
			checkbox.prop("checked", false);
		}
		
		checkbox.change(function() {
	        let checkedbox = $(table_o).find("tbody input[type='checkbox']:checked");
			if(checkbox.length == checkedbox.length) {
				allCheckbox_o.prop("checked", true);
			} else {
				allCheckbox_o.prop("checked", false);
			}
		});
    }
    
    // 이메일 인증 타이머
	function setAuthTimer(target = "authTimer", time = 300 ) {
		let startTime = new Date().getTime() + (time * 1000);
		let target_o = $("#"+ target);
		let min = "";
		let sec = "";
		
		target_o.show();

		_timer = setInterval(function () {
			time =  Math.ceil((startTime - new Date().getTime()) / 1000);
			
			min = parseInt(time / 60);
			sec = time % 60;
			
			if(String(sec).length == 1) {
				sec = String(sec).padStart(2, '0');
			}
			
			target_o.html(min + ":" + sec);
			time--;
			
			if(time < 0) {
				clearInterval(_timer);
				target_o.html("인증시간 만료");
			}
		}, 1000);
	}
	/**
     * param data 를 FormData로 변경
     */
	function getFormData(data) {
		if (data == null) return null;

        let formData = new FormData();

        let keys = Object.keys(data);

        for (let key of keys) {
            let value = data[key];
            
			if (value instanceof FileList) {
				for (let file of value) {
					formData.append(key, file);
				}
			} else {
				formData.append(key, value);
			} 
        }

        return formData;
    }
	
	//이메일 인증 타이머 초기화
	function clearAuthTimer(timer = _timer){
		clearInterval(_timer);
	}
	
	//null체크
	function valNullChk(val) {
		if(val == "" || val == null){
			return true;
		}else{
			return false;
		}
	}
	
	//전화번호 '-' 자동 변환 
	function autoHypenPhone(num){
		num = num.replace(/[^0-9]/g, '')
	   .replace(/^(\d{2,3})(\d{3,4})(\d{4})$/, `$1-$2-$3`);
	   
	    return num;
	}
	
	// 바이트 변환
	function convertByte(bytes, decimals = 2){
		if (bytes === 0) return '0 b';

	    const k = 1024;
	    const dm = decimals < 0 ? 0 : decimals;
	    const sizes = ['b', 'kb', 'mb', 'gb', 'tb', 'pb', 'eb', 'zb', 'yb'];

	    const i = Math.floor(Math.log(bytes) / Math.log(k));

	    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
	}
	
	/**
	 * startDate와 endDate간 unit으로 차이 계산
	 * unit: days, months, years ...  
	 */
	function getDiffDate(startDate, endDate, unit = "days") {
		if(moment) {
			let mSdate = moment(startDate);
			let mEndDate = moment(endDate);
			if(mEndDate.isAfter(mSdate)) {
				return mEndDate.diff(mSdate, unit);
			}
		}
	}
	
	// blob 형식 파일 다운로드
	function blobFileDownload(url, data, callback = null) {
		let prefixUrl = "/api/v1";

		if (!url.startsWith(prefixUrl)) {
		   url = prefixUrl + url;
		}

		let formData = new FormData();
		formData.append("e", util.aesEncode(JSON.stringify(data)));

		let xhr = new XMLHttpRequest();
		xhr.onreadystatechange = function () {
		   if (this.readyState == 4 && this.status == 200) {

		      let filename = "";
		      let disposition = xhr.getResponseHeader('Content-Disposition');
		      if (disposition && disposition.indexOf('attachment') !== -1) {
		         let filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
		         let matches = filenameRegex.exec(disposition);
		         if (matches != null && matches[1]) filename = matches[1].replace(/['"]/g, '');
		      }
		      filename = decodeURIComponent(filename);
		      if (filename) {
		         let blob = xhr.response;

		         if (window.navigator && window.navigator.msSaveOrOpenBlob) {
		            // IE에서 동작
		            window.navigator.msSaveBlob(blob, filename);
		         } else {
		            let a = document.createElement("a");
		            let url = URL.createObjectURL(blob);
		            a.href = url;
		            a.download = filename;
		            document.body.appendChild(a);
		            a.click();
		            window.URL.revokeObjectURL(url);
		         }
		      }
		      if (typeof callback == "function") {
		         callback();
		      }
		   }
		}
		xhr.responseType = 'blob';
		xhr.open('POST', url);
		xhr.send(formData);
		xhr.onprogress = function () {
		   comm.showLoading(true);
		}
		xhr.onload = function () {
		   comm.showLoading(false);
		}
	}

	// 세션 파리미터 모두 삭제 
	function removeSessionAllParam() {
		sessionStorage.removeItem("sessionParam");
	}
	
	// 키값에 해당하는 세션 파라미터 삭제
	function removeSessionParam(groupKey, subKey) {
		if(!groupKey) {
			return false;
		}
		param = getSessionAllParam();
		
		if(subKey) {
			delete param[groupKey][subKey];
		} else {
			delete param[groupKey];
		}
		sessionStorage.setItem("sessionParam", JSON.stringify(param));
	}
	
	// 세션 파라미터 설정
	function setSessionParam(groupKey, subKey, param) {
		if(!groupKey || !param) {
			return;
		}
		let newParam = {};
		
		if(subKey) {
			newParam[groupKey] = {};
			newParam[groupKey][subKey] = param;
		} else {
			newParam[groupKey] = param;
		}
		
		// 기존에 저장된 파라미터
		let oldParam = getSessionAllParam();
		
		if(oldParam) {
			// 기존에 저장된 파라미터와 새롭게 생성되는 파라미터 병합
			newParam = _mergeObject(oldParam, newParam);
		}
		sessionStorage.setItem("sessionParam", JSON.stringify(newParam));
	}
	
	// 모든 세션 파라미터 조회 
	function getSessionAllParam() {
		let param = sessionStorage.getItem("sessionParam");
		if(param) {
			param = JSON.parse(param);
		}
		return param;
	}
	
	// 키값에 해당하는 세션 파라미터 조회
	function getSessionParam(groupKey, subKey) {
		if(!groupKey || !subKey) {
			return {};
		}
		let param = getSessionAllParam();
		
		if(!param) {
			return {};
		}
		return param[groupKey][subKey];
	}
	
	// target객체에 source객체 병합
	function _mergeObject(target, source) { 	
		if(!source) {
			return target;
		}
		
		for(let key in source) {
			if(target[key]){
	            if(typeof(target[key]) == "object") {
	            	_mergeObject(target[key], source[key]);
	            } else {
	            	target[key] = source[key];
	            }
	        } else {
	        	target[key] = source[key];
	        }
	    }
	    return target;
	}
	
	//escape 처리된 html 태그를 unescape 처리
	function unescapeData(text) {
		if(text == null || text == ""){
			return text;
		}
	    var doc = new DOMParser().parseFromString(text, "text/html");
	    return doc.documentElement.textContent;
	}
	
	/**
	 * 타 계정 로그인 (관리자, 대행사 계정 사용) 
	 * 관리자 > 광고주, 대행사, 매체사
	 * 대행사 > 광고주
	 */
	function staffLogin({
		memberId, 
		memberUid,
		memberUtype,
		callback
	}) {
		if(!memberId || !memberUid || !memberUtype) return;
		if(memberUtype != "B" && memberUtype != "D" && memberUtype != "S") return;
		
		let userInfo = header.userInfo();
		if(userInfo == null) return; 
		
		let staffUtype = userInfo.utype;
		if(staffUtype != "B") return;
		
		let url_v = "/member";
		let data_v = {};
		
		if(staffUtype == globalConfig.memberType.AGENCY.utype) {
			url_v += "/agency/demand/login";
			data_v.demand_member_id = memberId;
		}
		
		let memberType = null;
		let memberTypeKeys = Object.keys(globalConfig.memberType);
		for(let key of memberTypeKeys) {
			let target = globalConfig.memberType[key];
			if(memberUtype == target.utype) {
				memberType = target;
			}
		}
		
		let confirmMsg = memberType.name + " 계정으로 페이지 이동하시겠습니까?</br>" + memberUid + " 아이디로 로그인됩니다.";
		
		customModal.confirm({
			content: confirmMsg,
			confirmCallback: () => {
				comm.send(url_v, data_v, "POST", function(resp){
					if(resp.result) {
						if(typeof(callback) == "function") {
							callback(resp);
						} else {
							location.href = "/";
						}
					} else {
						customModal.alert({
							content: "접속이 제한된 계정입니다.",
						});
					}
				});
			}
		});
	}
	
	return {
		getUUID,
		aesEncode,
		aesDecode,
		getCookie,
		setCookie,
		getUrlParam,
		numberWithComma,
		setCheckBox,
		setAuthTimer,
		clearAuthTimer,
		getFormData,
		valNullChk,
		autoHypenPhone,
		convertByte,
		getDiffDate,
		blobFileDownload,
		removeSessionAllParam,
		removeSessionParam,
		setSessionParam,
		getSessionAllParam,
		getSessionParam,
		staffLogin,
		unescapeData
	}
	
})();