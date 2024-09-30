const externalHeader = (function() {

	let _userInfo = null;
	// 해당 페이지 초기화 함수
	function init() {
		return _data.getUserInfo()
			.then(() => _data.getLoginUserMenu())
			.then(() => {
				_evInit();
			}).catch();
			
	}
	
	// 이벤트 초기화 
	function _evInit() {
		let evo = $("[data-src='externalHeader'][data-act]").off();
		evo.on("click", function(ev){
			_action(ev);
		});
	}
	
	// 이벤트 분기 함수
	function _action(ev) {
		let evo = $(ev.currentTarget);
		
		let act_v = evo.attr("data-act");
		
		let type_v = ev.type;
		
		let event = _event;
		
		if(type_v == "click") {
			if(act_v == "clickMenu") {
				event.clickMenu(evo);
			} else if(act_v == "clickLogo") {
				event.clickLogo();
			} else if(act_v == "clickLogout") {
				event.clickLogout();
			}
		}
	}
	
	// 데이터 
	let _data = {
		// 로그인 유저 정보 조회
		getUserInfo: function() {
			return new Promise((resolve) => {
				let url_v = "/member/external/myData/get";
				
				comm.send(url_v, null, "POST", function(resp) {
					if (resp.result) {
						let data = resp.data;
						_userInfo = data;
						_data.drawUserInfo(data);
						resolve();
					}
				});
			})
		},
		
		// 로그인 유저 정보 그리기
		drawUserInfo: function(data) {
			// 로그인 아이디
			$("#loginId").text(data.uid + " 님");
		},
		
		// 로그인 유저 메뉴 정보 조회
		getLoginUserMenu: function() {
			return new Promise((resolve) => {
				let url_v = "/member/external/menu/list";
				
				comm.send(url_v, {}, "POST", function(resp) {
					if(resp.result) {
						let menu = resp.list[0];
						if(menu && menu.sub_list) {
							_data.drawLoginUserMenu(menu.sub_list);
						}
						resolve();
					}
				});
			});
		},
		
		// 로그인 유저 메뉴 그리기
		drawLoginUserMenu: function(list) {
			let menu_o = $("#menuList");
			
			// 로그인정보 메뉴 제외 삭제
			menu_o.find("li:gt(0)").remove();
		
			for(let menu of list) {
				let li_o = $("<li>");
				menu_o.append(li_o);
				{
					let p_o = $("<p>");
					li_o.append(p_o);
					{
						let a_o = $("<a>").attr("href", menu.url).text(menu.menu_name);
						p_o.append(a_o);
					}
				}
			}
		}
	}
			
	// 이벤트 담당
	let _event = {
		// 메뉴 클릭
		clickMenu: function(evo) {
			if (evo.is(":checked")) {
	            $(".menu").slideDown();
	            $(".hamburger-button-state").prop("checked", true);
	            $('body').css({"overflow":"hidden"});
	        } else {
	            $(".menu").slideUp();
	            $(".hamburger-button-state").prop("checked", false);
	            $('body').css({"overflow":"auto"});
	        };
		},
		
		// 로고 클릭
		clickLogo: function() {
			location.href = "/external/login";
		},
		
		// 로그아웃
		clickLogout: function() {
			let url_v = "/external/logout";
			let data_v = "";
			
			comm.send(url_v, data_v, "POST", function() {
				location.replace("/external/login");
			});
		} 
	}
	
	return {
		init,
		event: _event,
		userInfo: function(){
			return _userInfo;
		},
	}
	
})();