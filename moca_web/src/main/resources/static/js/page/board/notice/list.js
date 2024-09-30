/**
 * 공통 공지사항 목록
 */
const noticeList = (function() {
	
	let _urlParam = util.getUrlParam();
	
	function init() {
		let all_o = $("#viewKindList").find("button[data-kind='A']");
		
		if(_urlParam && _urlParam.kind) {
			let target_o = $("#viewKindList").find("button[data-kind='"+_urlParam.kind+"']");
			if(target_o.get(0)) {
				$("#viewKindList").find(".active").removeClass("active");
				target_o.addClass("active");
			} else {
				all_o.addClass("active");
			}
		} else {
			all_o.addClass("active");
		}
		
		_list.getList();
	}
	
	function _evInit() { 
		let evo = $("[data-src='list'][data-act]").off();
		evo.on("click keyup", function(ev){
			_action(ev);
		});
	}
	
	function _action(ev) {
		let evo = $(ev.currentTarget);
		
		let type_v = ev.type;
		
		let act_v = evo.attr("data-act");
		
		let event = _event;
		
		if(type_v == "click") {
			if(act_v == "clickDetail") {
				event.clickDetail(evo);
			} else if(act_v == "clickViewKind") {
				event.clickViewKind(evo);
			} else if(act_v == "clickSearch") {
				_list.getList();
			}	
		} else if(type_v == "keyup") {
			if(act_v == "inputSearch") {
				if(ev.keyCode === 13) {
					_list.getList();
				}
			}
		}
	}
	
	let _list = {
		// 목록 조회
		getList: (curPage = 1) => {
			let viewKind = $("#viewKindList").find("button.active").attr("data-kind");
			if(viewKind == "A") {
				viewKind = null;
			}
			
			let url_v = "/board/notice/list";
			
			let data_v = {
				view_kind: viewKind,
			};
			
			let searchType_v = $("#searchType").val();
			// 전체가 아닐 경우
			if(searchType_v != "A") {
				data_v.search_type = searchType_v;
			}
			
			let searchValue_v = $("#searchValue").val();
			if(searchValue_v) {
				data_v.search_value = searchValue_v;
			}
			
			let page_o = $("#listPage").customPaging(null, (_curPage) => {
				_list.getList(_curPage);
			});
			
			let pageParam = page_o.getParam(curPage);
			
			data_v = $.extend(true, data_v, pageParam);
			comm.send(url_v, data_v, "POST", function(resp) {
				page_o.drawPage(resp.tot_cnt);
				_list.drawList(resp.list);
				_evInit();
			});
		},
		// 목록 그리기
		drawList: (list) => {
			let list_o = $("#listBody").html("");
			for(let item of list) {
				let tr_o = $("<tr>");
				list_o.append(tr_o);
				
				{
					let td_o = $("<td>").html(item.seq);
					tr_o.append(td_o);
				}
				{
					let a_o = $("<a>").attr({
						"href": "javascript:;",
						"data-src": "list",
						"data-act": "clickDetail",
						"data-id": item.board_notice_id
					}).html(item.title);
					let td_o = $("<td>").append(a_o);
					tr_o.append(td_o);
				}
				{
					let td_o = $("<td>").html(item.insert_date_str);
					tr_o.append(td_o);
				}
			}
		},
	}
	
	let _event = {
		// 상세 클릭
		clickDetail: (evo) => {
			let id = $(evo).attr("data-id");
			location.href = "/board/notice/detail?id=" + id;
		},
		
		// 뷰타입 변경
		clickViewKind: (evo) => {
			location.href = "/board/notice?kind=" + $(evo).attr("data-kind");
		},
	}
	
	return {
		init,
	}
})();