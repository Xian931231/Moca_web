/**
 * 공통 공지사항 상세
 */
const noticeDetail = (function() {

	let _urlParam = util.getUrlParam();
	
	function init() {
		if(_urlParam.id) {
			_getData();
		} else {
			location.href = "/board/notice";
		}
	}
	
	function _evInit() { 
		let evo = $("[data-src='detail'][data-act]").off();
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
			if(act_v == "clickNotice") {
				event.clickNotice(evo);
			} else if(act_v == "clickList") {
				location.href = "/board/notice";
			} else if(act_v == "clickViewKind") {
				event.clickViewKind(evo);
			}
		} 
	}
	
	// 데이터 조회
	function _getData() {
		let url_v = "/board/notice/detail";
		
		let data_v = {
			board_notice_id: _urlParam.id,
		}
		
		comm.send(url_v, data_v, "POST", function(resp) {
			if(resp.data) {
				_drawData(resp.data);
				_evInit();
			} else {
				location.href = "/board/notice";
			}
		});
	}
	
	// 데이터 그리기 
	function _drawData(data) {
		$("#viewKindList").find("button[data-kind='"+data.view_kind+"']").addClass("active");
		$("#noticeTitle").html(data.title);
		$("#noticeDate").html(data.insert_date_str);
		let content = data.content;
		if(content) {
			content = content.replaceAll("\n", "</br>");
		}
		$("#noticeContent").html(content);
		
		if(data.file_path && data.file_name) {
			$("#noticeImg").show().append(
				$("<img>").attr("src", globalConfig.getS3Url() + data.file_path + "/" + data.file_name)
			);
		}
		
		// 이전글
		if(data.prev_board_notice_id) {
			let a_o = $("<a>").attr({
				"href": "javascript:;",
				"data-src": "detail",
				"data-act": "clickNotice",
				"data-id": data.prev_board_notice_id,
			}).html(data.prev_board_notice_title);
			
			let title_o = $("<th>").append("[이전글] ").append(a_o);
            let date_o = $("<th>").html(data.prev_board_notice_insert_date);
            
            $("#prevTitleDiv").append(title_o);
            $("#prevTitleDiv").append(date_o);
            $($("#prevTitleDiv").parents("li")[0]).show();
		} 
		
		// 다음글 
		if(data.next_board_notice_id) {
			
			let a_o = $("<a>").attr({
				"href": "javascript:;",
				"data-src": "detail",
				"data-act": "clickNotice",
				"data-id": data.next_board_notice_id,
			}).html(data.next_board_notice_title);
			
			let title_o = $("<th>").append("[다음글] ").append(a_o);
            let date_o = $("<th>").html(data.next_board_notice_insert_date);
            
            $("#nextTitleDiv").append(title_o);
            $("#nextTitleDiv").append(date_o);
            $($("#nextTitleDiv").parents("li")[0]).show();
		} 
	}
	
	let _event = {
		// 이전/다음 공지 클릭 
		clickNotice: (evo) => {
			let id = evo.attr("data-id");
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