const menu = (function() {
	
	function init() {
		return _list.getList();
	}
	
	function _evInit() {
		let evo = $("[data-src='sidebar'][data-act]").off();
		evo.on("click", function(e) {
			_action(e);
		});
	}
	
	function _action(e) {
		let type_v = e.type;
		
		let evo = e.currentTarget;
		
		let act_v = $(evo).attr("data-act");
		
		let event = _event;
		
		if(type_v == "click") {
			if(act_v == "clickMenu") {
				event.clickMenu(evo);
			}
		}
	}
	
	let _event = {
		clickMenu: (evo) => {
			let url = $(evo).attr("data-href");
			if(url) {
				location.href = url;
			}
		}
	}

	// 목록 관련 
	let _list = {
		getList: function() {
			return new Promise((resolve, reject) => {
				let url_v = "/member/menu/list";
				comm.send(url_v, null, "POST", function(resp) {
					if(resp.result) {
						if(_hasUrlRole(resp.list)) {
							_list.drawList(resp.list);
							_evInit();
							resolve();
						} else {
							location.href = "/error/role";
							reject();
						}
					}
				});
			})
		},
		
		drawList: function(list) {
			dev.log(list);
			
			let sidebar_o = $("#sidebarList").html("");
			
			let parent_o = sidebar_o.parent();
			
			let userInfo = header.userInfo();
			
			if(userInfo && userInfo.utype) {
				let parentClass = "";
				
				switch(userInfo.utype) {
				case "A": 
					// 관리자
					parentClass = "moCafe";
					break;
				case "B": 
					// 대행사
					parentClass = "ag";
					break;
				case "D":
					// 광고주
					parentClass = "cus";
					break;
				case "S":
					// 매체사
					parentClass = "med";
					break;
				}
				parent_o.addClass(parentClass);
			}
			
			let menuList = list[0].sub_list;
			
			for(let i=0; i<menuList.length; i++) {
				let menuItem = menuList[i];
				dev.log(menuItem);
				
				if(menuItem.sub_yn == "N"){
					let targetHeadId = "menu_head_" + menuItem.menu_id;
					let targetCollapseId = "menu_collapse_" + menuItem.menu_id;
					
					{
						let card_o = $("<div>").addClass("card");
						sidebar_o.append(card_o);
						{
							let header_o = $("<div>").addClass("card-header").attr({
								"id": targetHeadId
							});
							card_o.append(header_o);
							{
								let h2_o = $("<h2>").addClass("mb-0");
								header_o.append(h2_o);
								{
									let btn_o = $("<button>").addClass("d-flex align-items-center btn btn-link collapsed").attr({
										"data-toggle": "collapse",
										"data-target": "#"+targetCollapseId,
										"aria-expanded": "false",
										"aria-controls": targetCollapseId,
									});
									h2_o.append(btn_o);
									{
										btn_o.append("<span class='sideIcon "+ menuItem.menu_class +"'></span>");
										btn_o.append("<span class='headMenu'>"+ menuItem.menu_name + "</span>");
										if(menuItem.link_yn === "N") {
											btn_o.append("<b class='caret'><span></span></b>");
											if(i == 0) {
												btn_o.attr("aria-expanded", "true");
											}
										} else {
											btn_o.attr({
												"data-href": menuItem.url,
												"data-src": "sidebar",
												"data-act": "clickMenu",
											});
										}
									}
								}
							}
							if(menuItem.sub_list && menuItem.sub_list.length > 0 && menuItem.sub_yn == "N") {
								let collapse_o = $("<div>").addClass("collapse").attr({
									"id": targetCollapseId,
									"aria-labelledby": targetHeadId,
									"data-parent": "#sidebarList",
								});
								card_o.append(collapse_o);
								{
									let body_o = $("<div>").addClass("card-body");
									collapse_o.append(body_o);
								
									let ul_o = $("<ul>");
									body_o.append(ul_o);
									
									for(let subMenu of menuItem.sub_list) {
										if(subMenu.sub_yn === "N") {
											let li_o = $("<li>");
											ul_o.append(li_o);
											
											let a_o = $("<a>").attr({
												"href": subMenu.url,
											}).html(subMenu.menu_name);
											li_o.append(a_o);
											
											if(location.pathname === subMenu.url) {
												card_o.find("button").attr("aria-expanded", true).removeClass("collapsed");
												collapse_o.addClass("show");
												
												/*
												let pagePath_o = $("<a>").attr({
													"href": subMenu.url,
												}).html(menuItem.menu_name + " &gt; " + subMenu.menu_name);
												
												$(".pagePath").html(pagePath_o);
												
												dev.log(menuItem.menu_name + " > " + subMenu.menu_name);
												*/
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	// 페이지 URL 접근 권한 확인
	function _hasUrlRole(list) {
		if(!list) {
			return false;
		}
		let hasUrlRole = false;
		let pathName = location.pathname;
		
		for(let menu of list) {
			if(pathName == menu.url){
				hasUrlRole = true;
				break;
			}
			
			if(menu.sub_list && menu.sub_list.length > 0) {
				hasUrlRole = _hasUrlRole(menu.sub_list);
				if(hasUrlRole) {
					break;
				}
			}
		}
		return hasUrlRole;
	}
	
	return {
		init,
	}
})();