const demandList = (function(){
	
	// 페이지 초기화 함수
	function init(){
		_list.getDemandList();
		_list.getRoleList(["searchRoleSelect", "modalRoleSelect"]);
	}
	// 이벤트 초기화 
	function _evInit(){
		let evo = $("[data-src='demandList'][data-act]").off();
		evo.on("click change", function(ev){
			_action(ev);
		});
	}
	
	// 이벤트 분기 함수
	function _action(ev){
		let evo = $(ev.currentTarget);
		
		let act_v = evo.attr("data-act");
		
		let type_v = ev.type;
		
		let event = _event;
		
		if(type_v == "click"){
			//검색 버튼
			if(act_v == "getSearch"){
				_list.getDemandList();
			//수정이력 더보기 조회
			}else if(act_v == "modifyHistoryList"){
				_list.getModifyHistoryList(evo);
			}else if(act_v =="allChk"){
				event.allChk();
			//직원 선택 이벤트
			}else if(act_v =="demandChk"){
				event.demandChk();
			//광고주의 담당 직원 리스트 
			//the person in charge = 담당자
			}else if(act_v == "modifyContactStaffModal"){
				event.modifyContactStaffModal(evo);
			//담당 수정
			}else if(act_v == "modifyDemandAccessStaff"){
				event.modifyDemandAccessStaff(evo);
			//선택된 담당자 삭제 이벤트
			}else if(act_v == "clearSelectStaff"){
				event.clearSelectStaff(evo);
			//광고주 아이디로 로그인
			}else if(act_v == "demandLogin"){
				event.demandLogin(evo);
			//광고주 계정 삭제 버튼 클릭
			}else if(act_v == "removeRemoveBtn"){
		        if($(".chk").is(":checked") == true){
			   		$("#modalPassword").val("");
					$("#modalReason").val("");
		          	$('#depo2').modal('show');
		        } 
			}else if(act_v == "removeDemandChk"){
				event.removeDemandChk();
			}
		} else if(type_v == "change"){
			if(act_v =="selectRole"){
				event.changeRoleSelectBox(evo);
			}else if(act_v == "changeModalSelectRole"){
				event.changeModalSelectRole(evo);
			}else if(act_v =="clickRoleChk"){
				event.clickRoleChk(evo);
			}
		}
	}
	
	let _event = {
		changeRoleSelectBox: function(evo){
			$("#searchRoleSelect").selectpicker("val",$("#searchRoleSelect").val());
			let select_v = $("#searchRoleSelect").val();
			_list.getRoleStaffList(select_v, function(resp){
				_list.drawRoleStaffListSelectBox(resp.list);
				
			});
			
		},
		changeModalSelectRole: function(evo){
			$("#modalRoleSelect").selectpicker("val",$("#modalRoleSelect").val());
			let select_v = $("#modalRoleSelect").val();
			_list.getRoleStaffList(select_v, function(resp){
				_list.drawRoleStaffSelectList(resp.list);
			});
		},
		//전체 선택 이벤트
		allChk: function(){
			if($("#allChk").is(":checked")){
				$("input[name=demandChk]").prop("checked", true);	
			}else{ 
				$("input[name=demandChk]").prop("checked", false);
			}
		},
		//개별 선택 이벤트
		demandChk: function(){
			var total = $("input[name=demandChk]").length;
			var checked = $("input[name=demandChk]:checked").length;
			
			if(total != checked){
				$("#allChk").prop("checked", false);
			} else{
				$("#allChk").prop("checked", true);	
			}  
		},
		//담당 수정 모달 이벤트
		modifyContactStaffModal: function(evo){
			let checkList = $("input[type=checkbox][name=demandChk]:checked");
			
			let result = true;		
			let demandList = [];
			checkList.each(function(){
				let data_id = $(this).attr("data-id");
				let status = $(this).attr("data-status");
						
				if(status == "R" || status == "L"){
					result = false;
					return false;
				}
				
				demandList.push(data_id);
			});
			if(!result){
				return false;
			}
			if((demandList.length == 1) ){
				let demand_id = demandList[0];
				let select_v = $("#modalRoleSelect").val();
				$("#modifyDemandAccessStaffBtn").attr({
					"data-id": demand_id
				});
				
					
				_list.getDemandAccessStaffList(demand_id);
				
				_list.getRoleStaffList(select_v, function(resp){
					let checkList = $("div[name=checkedStaff]");
			
					let staffList = [];
					checkList.each(function(){
						staffList.push($(this).attr("data-id"));
					});
					
					_list.getRoleList(["modalRoleSelect"]);
					_list.drawRoleStaffSelectList(resp.list);
					
				});
				
				
				$('#depo1').modal('show');
			}
		},
		//담당 수정 모달 > 구분에 직원 체크시 이벤트
		clickRoleChk: function(evo){
			let chk_v = $(evo).is(":checked");
			if(chk_v){
				let uid = $(evo).attr("data-uid");
				let id = $(evo).attr("data-id");
				let uname = $(evo).attr("data-uname");
				let roleName = $(evo).attr("data-rolename");
				let body_o = $("#demandAccessStaffBody");
				
				
				let deopName_o = $("<div>").addClass("depoName-chk1");
				let divBtn_o = $("<div>").addClass("btn-sm btn-clear checkedStaff");
				divBtn_o.attr({
					"name": "checkedStaff"
					, "data-id": id
					, "data-src": "demandList"
					, "data-act": "clearSelectStaff"
				});
				divBtn_o.html(roleName+"_"+uname+"("+uid+")");
				deopName_o.append(divBtn_o);
				body_o.append(deopName_o);
				
			}else{
				_event.clearSelectStaff(evo);
			}
			_evInit();
		},
		// 선택된 담당자 삭제 시 이벤트
		clearSelectStaff: function(evo){
			let id = $(evo).attr("data-id");
			$("div[name=checkedStaff][data-id="+id+"]").parent().remove();
			$("#roleChk"+id).prop("checked", false);
		},
		//담당 수정
		modifyDemandAccessStaff: function(evo){
			let url_v = "/member/agency/demand/access/staff/modify";
			
			let checkList = $("div[name=checkedStaff]");
			
			let staffList = [];
			checkList.each(function(){
				let staffId={
					staff_id: Number($(this).attr("data-id"))
				}
				staffList.push(staffId);
			});
			
			let data_v = {
				staff_json: staffList
				, demand_id: $(evo).attr("data-id")
			}
			
			comm.send(url_v, data_v, "POST", function(resp){
				if(resp.result){
					customModal.alert({
						content: "수정 되었습니다."
						, confirmCallback: function(){
							$("#depo1").modal('hide');
							_list.getDemandList();
						}
					})		
				}
			});
		},
		
		removeDemandChk: function(){
			let url_v = "/member/agency/demand/remove/validate";
			
			let password = $("#modalPassword").val();
			let removeReason = $("#modalReason").val();
			
			if(util.valNullChk(password) || util.valNullChk(removeReason)){
				customModal.alert({
					content: "비밀번호, 사유를 확인해 주세요."
				});
				return false;
			}
			
			let checkList = $("input[type=checkbox][name=demandChk]:checked");
						
			let demandList = [];
			checkList.each(function(){
				if($(this).attr("data-status") != "L" && $(this).attr("data-status") != "R"){
					demandList.push($(this).attr("data-id"));
				}
			});
			
			let data_v = {
				demand_list: demandList
				, passwd: password
				, leave_reason: removeReason
			}
			
			comm.send(url_v, data_v, "POST", function(resp){
				if(resp.result){
					_event.removeDemand();
				}else{
					let msg = "";
					if(resp.code == 2212){
						msg = "비밀번호를 다시 확인하고 입력해주세요.";
					}else if(resp.code == 2301){
						msg = "광고가 진행중인 계정은 삭제할 수 없습니다.";
					}
					
					customModal.alert({
						content: msg
					});
				}
			});
		},
		
		//대행사 소속 광고주 탈퇴 요청
		removeDemand: function(){
			customModal.confirm({
				content: "계정을 삭제하셔도 집행, 정산 등의<br>데이터는 유지될 수 있습니다.<br>계속 진행 하시겠습니까?",
					confirmCallback: function(){
						let url_v = "/member/agency/demand/remove";
			
						let password = $("#modalPassword").val();
						let removeReason = $("#modalReason").val();
						
						if(util.valNullChk(password) || util.valNullChk(removeReason)){
							customModal.alert({
								content: "비밀번호, 사유를 확인해 주세요."
							});
							return false;
						}
						
						let checkList = $("input[type=checkbox][name=demandChk]:checked");
						
						let demandList = [];
						checkList.each(function(){
							if($(this).attr("data-status") != "L" && $(this).attr("data-status") != "R"){
								demandList.push($(this).attr("data-id"));
							}
						});
						
						let data_v = {
							demand_list: demandList
							, passwd: password
							, leave_reason: removeReason
						}
						
						comm.send(url_v, data_v, "POST", function(resp){
							if(resp.result){
								customModal.alert({
									content: "삭제되었습니다."
									,confirmCallback: function(){
										$("#id-modal2").modal("hide");
										$("#depo2").modal("hide");
										_list.getDemandList();
									}
								});
							}else{
								let msg = "삭제에 실패했습니다."
								if(resp.code == 2212){
									msg = "현재 비밀번호가 일치하지 않습니다.";
								}
								customModal.alert({
									content: msg
								});
							}
							
						});
					}
			});
		},
		
		//광고주 아이디로 로그인
		demandLogin: function(evo){
			let memberId = $(evo).attr("data-id");
			let memberUid = $(evo).attr("data-uid");
			let memberUtype = globalConfig.memberType.DEMAND.utype;
			
			util.staffLogin({
				memberId,
				memberUid,
				memberUtype,
			});
		}
	}
	
	let _list = {
		//구분 별 직원 리스트 api 
		getRoleStaffList: function(roleId, callback){
			let url_v= "/member/agency/role/staff/list";
			let data_v ={
				role_id: roleId
				, option: "option"
			};
			comm.send(url_v, data_v, "POST", function(resp){
				if(resp.result){
					if(typeof(callback) == "function"){
						callback(resp)
					}
				}
			});
		},
		//구분별 직원 리스트 셀렉트박스 그리기용
		drawRoleStaffListSelectBox: function(list){
			let select_o = $("#searchStaffSelect").html("");
			select_o.selectpicker("destroy");
			
			select_o.append(new Option("전체", ""));
			
			for(let item of list){
				select_o.append(new Option(item.uname, item.member_id));
			}
			select_o.selectpicker("refresh");	
			_evInit();
		},
		//대행사별 광고주 리스트 api
		getDemandList: function(curPage = 1){
			let url_v = "/member/agency/demand/list";
			
			let data_v = _list.getSearchData();
			
			let page_o = $("#listPage").customPaging(null, function(_curPage){
				_list.getList(_curPage);
			});
			
			let pageParam = page_o.getParam(curPage);
			
			if(pageParam){
				data_v.offset = pageParam.offset;
				data_v.limit = pageParam.limit;
			}
			
			comm.send(url_v, data_v, "POST", function(resp){
				_list.drawDemandList(resp.list);
				page_o.drawPage(resp.tot_cnt);
				_evInit();
			});
		},
		//검색 정보 
		getSearchData: function(){
			let data = {};
			
			let searchRole_v = $("#searchRoleSelect").val();
			if(searchRole_v) {
				data.staff_role_id = searchRole_v;
			} 	
			
			let searchStaff_v = $("#searchStaffSelect").val();
			if(searchStaff_v){
				data.staff_id = searchStaff_v;
			}
			
			let searchDemand_v = $("#searchDemandSelect").val();
			if(searchDemand_v){
				data.search_type = searchDemand_v;
			}
			
			let searchInput_v = $("#searchInput").val();
			if(searchInput_v){
				data.search_value = searchInput_v;
			}
			
			return data;
			
		},
		// 광고주 리스트 그리기
		drawDemandList: function(list){
			let body_o = $("#demandList").html("");
			
			for(let item of list){
				let tr_o = $("<tr>");
				body_o.append(tr_o);
				//체크박스
				{
					let td_o = $("<td>");
					let span_o = $("<span>").addClass("chk");
					let input_o = $("<input>").addClass("chk");
					input_o.attr({
						"type": "checkbox"
						, "name": "demandChk"
						, "id": "staff"+item.member_id
						, "data-id": item.member_id
						, "data-status": item.demand_status
						, "data-src": "demandList"
						, "data-act": "demandChk"
					});
					let label_o = $("<label>");
					label_o.attr({"for":"staff"+item.member_id});
					span_o.append(input_o);
					span_o.append(label_o);
					td_o.append(span_o);
					tr_o.append(td_o);
				}
				//광고주명
				{
					let td_o = $("<td>").html(item.demand_name);
					if(item.demand_status == "R" || item.demand_status == "L"){
						td_o.addClass("deleteLine");
					}
					tr_o.append(td_o);
				}
				//아이디
				{
					let td_o = $("<td>");
					let a_o = $("<a>");
					if(item.demand_status == "R" || item.demand_status == "L"){
						td_o.addClass("deleteLine");
					}else{
						a_o.attr({
							"href": "javascript:;"
							, "data-src": "demandList"
							, "data-act": "demandLogin"
							, "data-id": item.member_id
							, "data-uid": item.demand_uid
						});
					}
					a_o.html(item.demand_uid);
					td_o.append(a_o);
					tr_o.append(td_o);
				}
				//담당자
				{
					let td_o = $("<td>").html(item.staff_role_name);
					if(item.demand_status == "R" || item.demand_status == "L"){
						td_o.addClass("deleteLine");
					}
					tr_o.append(td_o);
					
				}
				//계정 등록 일자
				{
					let td_o = $("<td>").html(item.insert_date);
					if(item.demand_status == "R" || item.demand_status == "L"){
						td_o.addClass("deleteLine");
					}
					tr_o.append(td_o);
				}
				//수정 이력
				{
					let msg = null;
					let msgTitle = null;
					
					if(!util.valNullChk(item.message) ||!util.valNullChk(item.modify_history_date)){
						msgTitle = item.update_ymd + " / 변경자 : " + item.update_member_id;
						msg = item.message;
						msg = msg.replace(/\n/g, "<br>");
					}
					//수정 이력
					{
						let td_o = $("<td>");
						let span_o = $("<span>");
						span_o.html(msgTitle);
						td_o.append(span_o);
						td_o.append(msg);
						tr_o.append(td_o);
					}
					//더보기
					{
						let td_o = $("<td>");
						if(item.modify_history_count >= 2){
							let div_o = $("<div>").attr({
								"data-toggle": "modal"
								, "data-target": "#view-btn"
								, "data-idx": item.member_id
								, "data-src": "demandList"
								, "data-act": "modifyHistoryList"
								, "data-name": item.demand_name
								, "data-uid": item.demand_uid
							});
							let img_o = $("<img>").attr({
								"src": "/assets/imgs/icon_add2.png"
								, "alt": ""
							});
							div_o.append(img_o);
							td_o.append(div_o);
							
						}
						tr_o.append(td_o);
					}
				} 
				
			}
			_evInit();
		},
		//대행사 구분 리스트 api
		getRoleList: function(select_v){
			let url_v = "/role/agency/staff/list"
			comm.send(url_v, null, "POST", function(resp){
				//selectBox용 구분 그리기
				_list.drawRoleListOption(resp.list, select_v);
				_evInit();
			});
		},
		//구분 selectBox 그리기
		drawRoleListOption: function(list, selectIdList){
			for(let selectId of selectIdList){
				let select_o = $("#"+selectId).html("");
				select_o.selectpicker("destroy");
				
				if(selectId == "searchRoleSelect"){
					select_o.append(new Option("전체", ""));
				}				
				
				for(let item of list){
					select_o.append(new Option(item.name, item.id));
				}
				
				if(selectId == "modalRoleSelect"){
					_event.changeModalSelectRole();
				}
				
				select_o.selectpicker("refresh");	
				_evInit();
			}
			
		},
		//수정이력(더보기) 리스트 api
		getModifyHistoryList: function(evo){
			let url_v = "/member/agency/history/list";
			let data_v = {
				member_id: $(evo).attr("data-idx")
			}
			let uname = $(evo).attr("data-name");
			let uid = $(evo).attr("data-uid");
			
			comm.send(url_v, data_v, "POST", function(resp){
				_list.drawModifyHistoryList(resp.list, uid, uname);
			});
			
		},
		//수정이력(더보기) 그리기
		drawModifyHistoryList: function(list, id, name){
			let uid = name + "(" + id + ")";
			$("#modifyHistoryModalId").html(uid);
			
			let body_o = $("#modifyHistoryListBody").html("");
			for(let item of list){
				let div_o = $("<div>").addClass("view-text");
				body_o.append(div_o);
				let p_o = $("<p>");
				div_o.append(p_o);
				
				//title
				{
					let title_v = item.update_date + " / 변경자 : " +  item.update_member_uid;
					let span_o = $("<span>").html(title_v);
					let br_o = $("<br>");
					p_o.append(span_o);
					p_o.append(br_o);
				}
				
				//content
				{
					
					if(!util.valNullChk(item.message) ||!util.valNullChk(item.message)){
						msg = item.message;
						msg = msg.replace(/\n/g, "<br>");
					}
					
					let span_o = $("<span>");
					span_o.html(msg);
					p_o.append(span_o);
				}
			}
		},
		//광고주의 담당 직원 리스트 api
		getDemandAccessStaffList: function(demandId){
			let url_v = "/member/agency/demand/access/staff/list";
			let data_v = {
				demand_id: demandId
			}
			
			comm.send(url_v, data_v, "POST", function(resp){
				_list.drawDemandAccessStaffList(resp.list);
			});
			
		},
		//광고주의 담당 직원 리스트 그리기
		drawDemandAccessStaffList: function(list){
			let body_o = $("#demandAccessStaffBody").html("");
			
			for(let item of list){
				let div_o = $("<div>").addClass("depoName-chk1");
				body_o.append(div_o);
				let divBtn_o = $("<div>").addClass("btn-sm btn-clear");
				divBtn_o.attr({
					"name": "checkedStaff"
					,"data-id": item.member_id
					, "data-src": "demandList"
					, "data-act": "clearSelectStaff"
				});
				divBtn_o.html(item.name+"_"+item.uname+"("+item.uid+")");
				div_o.append(divBtn_o);
				
			}
			_evInit();
		},
		//담당 수정 모달 > 구분 > 구분별 소속 직원 체크 리스트 그리기
		drawRoleStaffSelectList: function(list){
			let checkList = $("div[name=checkedStaff]");
	
			let staffList = [];
			checkList.each(function(){
				staffList.push($(this).attr("data-id"));
			});
			
			let idx = 0;
			let drainage = 0;
			let body_o = $("#roleStaffListBody").html("");
			
			for(let item of list){
				let chkSpan_o = $("<span>").addClass("chk");
				body_o.append(chkSpan_o);
				let input_o = $("<input>").attr({
					"type": "checkbox"
					, "name": "chk_p"
					, "id": "roleChk"+item.member_id
					, "data-id": item.member_id
					, "data-uname": item.uname
					, "data-rolename": item.role_name
					, "data-uid": item.uid
					, "data-src": "demandList"
					, "data-act": "clickRoleChk"
				});
				for(let staffId of staffList){
					if(item.member_id == staffId){
						input_o.prop('checked',true);
					}
				}
				let label_o = $("<label>").attr({
					"for":"roleChk"+item.member_id
				});
				let span_o = $("<span>").addClass("chk_text");
				span_o.html(item.uname+"("+item.uid+")");
				chkSpan_o.append(input_o);
				chkSpan_o.append(label_o);
				chkSpan_o.append(span_o);
				
			}
			_evInit();
		}
	}
	return{
		init,
	};
})();