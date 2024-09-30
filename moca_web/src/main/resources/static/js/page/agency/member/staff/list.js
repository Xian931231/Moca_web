const staffList = (function(){
	
	function init(){
		_list.getStaffList();
		_list.getRoleList();
		_evInit();
	}
	
	function _evInit(){
		let evo = $("[data-src='staffList'][data-act]").off();
		evo.on("click change keyup", function(ev){
			_action(ev);
		});
	}
	function _action(ev){
		let evo = $(ev.currentTarget);
		let act_v = evo.attr("data-act");
		let type_v = ev.type;
		let event = _event;
		
		if(type_v == "click"){
			//검색버튼
			if(act_v == "getSearch"){
				_list.getStaffList();
			//직원 등록
			}else if(act_v == "addStaff"){
				event.addStaff();
			//구분 추가
			}else if(act_v == "addRole"){
				event.addRole();
			//구분 삭제
			}else if(act_v == "removeRole"){
				event.removeRole();
			//sort 기능	
			}else if(act_v =="sortMove"){
				event.sortMove(evo);
			//변경된 sort 리스트 저장
			}else if(act_v == "modifySort"){
				event.modifySort();
			//구분 상세 (구분별 메뉴 리스트);
			}else if(act_v == "getRoleDetail"){
				event.getRoleDetail(evo);
			//구분명 입력받기 위한 이벤트
			}else if(act_v == "clickRoleNameBtn"){
				event.clickRoleNameBtn();
			//구분명 수정
			}else if(act_v == "modifyRoleName"){
				event.modifyRoleName();
			//구분 메뉴 수정
			}else if(act_v == "modifyRoleMenu"){
				event.modifyRoleMenu(evo);
			}else if(act_v =="chkChild"){
				event.chkChild(evo);
			}else if(act_v == "chkParent"){
				event.chkParent(evo);
			}else if(act_v == "removeStaff"){
				event.removeStaff();
			//직원 전체 선택 이벤트
			}else if(act_v =="allChk"){
				event.allChk();
			//직원 선택 이벤트
			}else if(act_v =="staffChk"){
				event.staffChk();
			//수정이력 더보기 
			}else if(act_v == "modifyHistoryList"){
				_list.getModifyHistoryList(evo);
			//직원 담당 광고주 모달 오픈
			}else if(act_v == "openStaffModiyModal"){
				event.setStaffModiyModal(evo);
			//직원 개별 권한 수정
			}else if(act_v == "modifyDemand"){
				event.modifyDemand(evo);
			//직원 삭제 이벤트
			}else if(act_v == "clickRemoveStaffBtn"){
				event.clickRemoveStaffBtn();
			}
		}else if(type_v == "change"){
			//검색 셀렉트 박스 변경시
			if(act_v == "chgRoleId"){
				$("#searchRoleSelect").selectpicker("val",$("#searchRoleSelect").val());
				_list.getStaffList();
			//email 도메인 셀렉스 박스 변경시 
			}else if(act_v == "changeDomain"){
				event.changeDomain();
			//구분SelectBox 변경시
			}else if(act_v == "changeRoleSelectBox"){
				event.changeRoleSelectBox();
			}
		}else if(type_v == "keyup"){
			if(act_v == "inputSearch") {
				if(ev.keyCode === 13) {
					_list.getStaffList();
				}
			}
		}
	}
	
	let _event = {
		//이메일 도메인 이벤트
		changeDomain: function(){
			let seletedEmail = $("#emailConfirm option:selected").val();
			$("#emailConfirm").selectpicker("val",seletedEmail);
			if(seletedEmail != "directInput") {
				$("#companyEmailDomain").val(seletedEmail);
				$("#companyEmailDomain").attr("readonly", true);
			} else {
				$("#companyEmailDomain").val("");
				$("#companyEmailDomain").attr("readonly", false);
			}
		},
		allChk: function(){
			if($("#allChk").is(":checked")){
				$("input[name=staffChk]").prop("checked", true);	
			}else{ 
				$("input[name=staffChk]").prop("checked", false);
			}
		},
		staffChk: function(){
			var total = $("input[name=staffChk]").length;
			var checked = $("input[name=staffChk]:checked").length;
			
			if(total != checked){
				$("#allChk").prop("checked", false);
			} else{
				$("#allChk").prop("checked", true);	
			}  
		},
		//직원 등록
		addStaff: function(){
			let url_v = "/member/agency/staff/add";
			let data_v = _event.getSubmitData();
			
			if(!_event.staffValidate(data_v)){
				return false;
			}
			
			comm.send(url_v, data_v, "POST", function(resp){
				let msg = "등록에 실패했습니다.";
				if(resp.result){
					customModal.alert({
						content: "이메일로 임시 비밀번호를 전달했습니다. 접속 후 비밀번호를 변경해 주세요."
						, confirmCallback: function(){
							location.reload();
						}
					});
				}else{
					if(resp.code == 2200){
						msg = "이미 등록된 아이디입니다. 다른 아이디를 사용해주세요.";
					}else if(resp.code == 2202){
						msg = "이미 존재하는 회원의 이메일입니다.";
					}else if(resp.code == 2203){
						msg = "인증번호를 발송할 수 없습니다. 이메일 주소를 다시 확인해 주세요.";
					}else if(resp.code == 2201){
						msg = "아이디는 영문과 숫자를 포함해 4자 이상 15자 이하로 입력해 주세요.";
					}
					customModal.alert({
						content: msg
					});
				}
			});
		},
		//구분 등록
		addRole: function(){
			let url_v = "/role/agency/staff/add";
			let roleName = $("#roleName").val();
			
			if(util.valNullChk(roleName)){
				customModal.alert({
					content:"구분명을 입력해주세요."
				});
				return false;
			}
			
			let data_v = {
				role_name :roleName
			}
			comm.send(url_v, data_v, "POST", function(resp){
				if(resp.result){
					$("#roleName").val("");
					_list.getRoleList();
					
				}else{
					let msg = "구분 추가에 실패했습니다.";
					
					if(resp.code == 8100){
						msg = "같은 이름의 구분이<br>존재합니다."
					}
					
					customModal.alert({
						content: msg
					});
				}
				_evInit();
			});
			
		},
		//구분 삭제
		removeRole: function(){
			let checkList = $("input[type=checkbox][name=modalChk]:checked");
			let roleIdList = [];
			checkList.each(function(){
				let roleItem = {
					role_id: $(this).attr("data-id")
					,role_name: $(this).attr("data-name")
				};
				
				roleIdList.push(roleItem);
			});
			if(roleIdList.length > 0){
				customModal.confirm({
					content:"삭제하시겠습니까?",
					confirmCallback: function(){
						let url_v = "/role/agency/staff/remove"
						
						let data_v = {
							role_id_list: roleIdList
						}
						comm.send(url_v, data_v, "POST", function(resp){
							if(resp.result){
								customModal.alert({
									content:"삭제 되었습니다."
									, confirmCallback: function(){
										_list.getRoleList();
									}
								});
							}
						});		
					}
				});
			}else{
				customModal.alert({
					content: "삭제할 구분을 선택하세요."
				});
			}
		},
		//직원 등록 데이터
		getSubmitData: function(){
			let data = {};
			
			data.uid = $("#addStaffUid").val();
			data.uname = $("#addStaffName").val();
			data.role_id = $("#addModalRoleSelect").val();
			data.company_email = $("#companyEmailId").val() + "@" + $("#companyEmailDomain").val();
			
			return data;
		},
		//직원 등록 유효성 체크
		staffValidate: function(data){
			
			if(util.valNullChk(data.uid)){
				customModal.alert({
					content: "아이디를 입력해주세요."
				});
				return false;	
			}
			if(util.valNullChk(data.uname)){
				customModal.alert({
					content: "이름을 입력해주세요."
				});
				return false;	
			}
			if(util.valNullChk($("#companyEmailId").val()) || util.valNullChk($("#companyEmailDomain").val())){
				customModal.alert({
					content: "이메일을 입력해주세요."
				});
				return false;	
			}
			
			return true;
			
		},
		//직원 삭제
		removeStaff: function(){
			let url_v = "/member/agency/staff/remove";
			
			let password = $("#modalPassword").val();
			let removeReason = $("#modalReason").val();
			
			if(util.valNullChk(password) || util.valNullChk(removeReason)){
				customModal.alert({
					content: "비밀번호, 사유를 확인해 주세요."
				});
				$("#id-modal2").modal("hide");
				return false;
			}
			
			let checkList = $("input[type=checkbox][name=staffChk]:checked");
			let staffIdList = [];
			checkList.each(function(){
				if($(this).attr("data-status") != "L" && $(this).attr("data-status") != "R"){
					staffIdList.push($(this).attr("data-id"));	
				}
			});
			
			let data_v ={
				passwd: password
				, leave_reason: removeReason
				, staff_id_list: staffIdList
			};
			
			comm.send(url_v, data_v, "POST", function(resp){
				if(resp.result){
					customModal.alert({
						content: "삭제되었습니다."
						,confirmCallback: function(){
							
							$("#depo2").modal("hide");
							_list.getStaffList();
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
					$("#id-modal2").modal("hide");
				}
			});
			
		},
		//sorting 해주는 event
		sortMove: function(evo){
			let type_v = $(evo).attr("data-type"); 
			
			let checkList = $("input[type=checkbox][name=modalChk]:checked");
			let roleIdList = [];
			checkList.each(function(){
				roleIdList.push($(this).attr("data-id"));
			});
			
			if(type_v == "up"){
				for(let roleId of roleIdList){
					let li_o = $("#roleSort"+roleId).parent().parent().parent();
					li_o.prev().before(li_o);
				}
			}else if(type_v == "down"){
				//down일때는 리스트 역순으로 변경
				let reverseList = [...roleIdList].reverse();
				
				for(let roleId of reverseList){
					let li_o = $("#roleSort"+roleId).parent().parent().parent();
					li_o.next().after(li_o);
				}
			}
		},
		//sorting된 리스트 저장
		modifySort: function(){
			let checkList = $("input[type=checkbox][name=modalChk]");
			let roleIdList = [];
			checkList.each(function(){
				roleIdList.push($(this).attr("data-id"));
			});
			
			let url_v = "/role/agency/staff/modify/sort";
			
			let data_v = {
				role_id_list : roleIdList
			}
			
			comm.send(url_v, data_v, "POST", function(resp){
				let msg = "저장에 실패했습니다.";
				if(resp.result){
					msg = "저장되었습니다."		
				}
				customModal.alert({
					content: msg
				});
				_list.getRoleList();
			});
		},
		getRoleDetail: function(evo){
			let roleId = $(evo).attr("data-id");
			let roleName = $(evo).text();
			
			$('#roleNameSubmit, #inputRoleName').hide();  
			$('#detailRoleName, #setRoleName').show();
			
			$("#detailRoleName").html(roleName);
			$("#modifyRoleMenuBnt").attr({
				"data-idx":roleId
			});
			_list.getRoleMenuList(roleId, "roleMenuBody");
		},
		//구분명 수정 (inputText바꿔주는 이벤트)
		clickRoleNameBtn: function(){
			$("#inputRoleName").remove();
			let input_o = $("<input>").attr({
				"type": "text"
				, "name": "txt"
				, "id": "inputRoleName"
			});
			
			$('#detailRoleNameArea').append (input_o); 
			$('#detailRoleName, #setRoleName').hide();
	        $('#roleNameSubmit').show();  
	        _evInit();
		},
		//구분명 수정 api
		modifyRoleName: function(){
			let url_v = "/role/agency/staff/modify/name";
			let roleName = $("#inputRoleName").val();
			
			if(roleName.trim() == null || roleName.trim() == ""){
				customModal.alert({
					content: "구분명을 입력해주세요."
				})
				return ;
			}
			
			let data_v = {
				role_name: roleName
				, role_id: $("#modifyRoleMenuBnt").attr("data-idx")
			};
			
			comm.send(url_v, data_v, "POST", function(resp){
				if(resp.result){
					$("#detailRoleName").html(roleName);
					$("#roleNameSubmit").hide (); // remove the button
					$("#inputRoleName").remove();
		          	$('#detailRoleName, #setRoleName').show();
		          	
		          	_list.getStaffList();
					_list.getRoleList();
					_evInit();
				}else if(resp.code == 8100){
					customModal.alert({
						content: "같은 이름의 구분이 존재합니다."
					})
				}
			});
		},
		//구분 메뉴 수정 api 
		modifyRoleMenu: function(evo){
			let roleId = $(evo).attr("data-idx");
			
			let checkList = $("input[type=checkbox][name=menuChk]");
			
			let roleMenuList = [];
			checkList.each(function(){
				
				let accessYn = "N";
				
				if($(this).is(':checked')){
					accessYn ="Y";
				}
				
				let menuInfo = {
					menu_id: Number($(this).attr("data-idx"))
					, access_yn: accessYn
					, url: $(this).attr("data-url")
				}
				roleMenuList.push(menuInfo);
			});
			
			let url_v = "/role/agency/staff/modify";
			let data_v = {
				role_id: roleId
				, role_json: roleMenuList
			};
			
			comm.send(url_v, data_v, "POST", function(resp){
				let msg = "저장에 실패했습니다.";
				
				if(resp.result){
					msg = "저장되었습니다.";
				}
				
				customModal.alert({
					content: msg 
				});
			});
		},
		//구분메뉴 check 2step용 이벤트
		// 클릭시 하위 메뉴들 모두 체크 or 해제
		chkChild: function(evo){
			let menuId = $(evo).attr("data-idx");
			
			if($(evo).is(":checked")){
				$("input[data-parent-id="+menuId+"]").prop("checked", true);	
			}else{
				$("input[data-parent-id="+menuId+"]").prop("checked", false);
			}
			
		},
		//구분메뉴 check 3step용 이벤트
		// 클릭된 하위 메뉴가 있을때 상위메뉴 체크
		chkParent: function(evo){
			let parentId = $(evo).attr("data-parent-id");
			
			//3step의 체크된 개수가 0이면 2step 체크 해제
			if($("input[data-parent-id="+parentId+"]:checked").length ==0){
				$("input[data-idx="+parentId+"]").prop("checked", false);	
			}else{
				$("input[data-idx="+parentId+"]").prop("checked", true);
			}
		},
		
		//직원 담당 광고주 수정 모달 셋팅
		setStaffModiyModal: function(evo){
			let roleName = $(evo).attr("data-role_name");
			
			if(roleName != "최고 관리자"){
				$("#create-btn").modal("show");
				let url_v = "/role/agency/staff/permission/list";
			
			
				$("#modifyModalRoleSelect").selectpicker("val", $(evo).attr("data-role-id"));
				
				$("#modfiyDemandBnt").attr({
					"data-id": $(evo).attr("data-id")
				})
				
				let uid = $(evo).attr("data-uid");
				let uname = $(evo).attr("data-uname");
				
				$("#staffDetailName").html(uname + "(" + uid + ")");
				
				let data_v = {
					member_id: $(evo).attr("data-id")
				}
				
				comm.send(url_v, data_v, "POST", function(resp){
					//구분 그리기
					_list.drawRoleMenuList(resp.menu_list[0], "staffDetailModalMenuBody");
					//담당 광고주 그리기
					_list.drawDemandList(resp.demand_list);
				});
			}else{
				customModal.alert({
					content: "최고 관리자 계정은 변경할 수 없습니다."
				});
				return false;
			}
			
			
		},
		//개별권한관리 > 구분 변경 시
		changeRoleSelectBox: function(){
			let roleId = $("#modifyModalRoleSelect").val();
			$("#modifyModalRoleSelect").selectpicker("val", roleId);
			_list.getRoleMenuList(roleId, "staffDetailModalMenuBody");
		},
		//담당 광고주 수정
		modifyDemand: function(evo){
			let url_v = "/role/agency/staff/modify/permission";
			let data_v = {};
			
			let checkList = $("input[type=checkbox][name=demandChk]:checked");
			
			let demandList = [];
			checkList.each(function(){
				let demandId = {
					demand_id: Number($(this).attr("data-idx"))
				};
				//demandList.push($(this).attr("data-idx"));
				demandList.push(demandId);
			});
			data_v.demand_id_list = demandList;
			data_v.member_id = $(evo).attr("data-id");
			data_v.role_id = $("#modifyModalRoleSelect option:selected").val();
			
			comm.send(url_v, data_v, "POST", function(resp){
				msg = "저장에 실패했습니다.";
				if(resp.result){
					msg = "저장 되었습니다.";
				}
				customModal.alert({
					content:msg
					, confirmCallback: function(){
						location.reload();
					}
				});
			});
		},
		clickRemoveStaffBtn: function(){
	        if($(".chk").is(":checked") == true){
	          $('#depo2').modal('show')
	          $("#modalReason").val(null);
	          $("#modalPassword").val(null);
	        } 
		},
	}
	
	let _list = {
		//담당자 리스트 api
		getStaffList: function(curPage = 1){
			let url_v = "/member/agency/role/staff/list";
			
			let data_v = _list.getSearchData();
			
			let page_o = $("#listPage").customPaging(null, function(_curPage){
				_list.getStaffList(_curPage);
			});
			
			let pageParam = page_o.getParam(curPage);
			
			if(pageParam){
				data_v.offset = pageParam.offset;
				data_v.limit = pageParam.limit;
			}
			
			comm.send(url_v, data_v, "POST", function(resp){
				_list.drawStaffList(resp.list);
				page_o.drawPage(resp.tot_cnt);
				_evInit();
			});
		},
		
		//담당자 리스트 검색 조건
		getSearchData: function(){
			let data = {};
			
			let searchRole_v = $("#searchRoleSelect").val();
			if(searchRole_v) {
				data.role_id = searchRole_v;
			}
			
			let searchText_v = $("#searchText").val();
			if(searchText_v) {
				data.search_text = searchText_v;
			}
			
			return data;	
		},
		//담당자 리스트 그리기
		drawStaffList: function(list){
			let body_o = $("#staffListBody").html("");
			
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
						, "name": "staffChk"
						, "id": "staff"+item.member_id
						, "data-id": item.member_id
						, "data-status": item.status
						, "data-src": "staffList"
						, "data-act": "staffChk"
						
					});
					let label_o = $("<label>");
					label_o.attr({"for":"staff"+item.member_id});
					span_o.append(input_o);
					span_o.append(label_o);
					td_o.append(span_o);
					tr_o.append(td_o);
				}
				//구분
				{
					let td_o = $("<td>").html(item.role_name);
					if(item.status == "R" || item.status == "L"){
						td_o.addClass("deleteLine");
					}
					tr_o.append(td_o);
				}
				//이름
				{
					let td_o = $("<td>").html(item.uname);
					if(item.status == "R" || item.status == "L"){
						td_o.addClass("deleteLine");
					}
					tr_o.append(td_o);
				}
				//아이디
				{
					let td_o = $("<td>");
					let a_o = $("<a>");
					
					if(item.status == "R" || item.status == "L"){
						td_o.addClass("deleteLine");
					}else{
						a_o.attr({
							"href": "javascript:;"
							, "data-uid": item.uid
							, "data-uname": item.uname
							, "data-id": item.member_id
							, "data-role-id": item.role_id
							, "data-role_name": item.role_name
							, "data-src": "staffList"
							, "data-act": "openStaffModiyModal"
						});	
					}
					a_o.html(item.uid);
					td_o.append(a_o);
					tr_o.append(td_o);
				}
				//등록일자
				{
					let td_o = $("<td>").html(item.insert_ymd);
					if(item.status == "R" || item.status == "L"){
						td_o.addClass("deleteLine");
					}
					tr_o.append(td_o);
				}
				//수정이력 관련
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
								, "data-src": "staffList"
								, "data-act": "modifyHistoryList"
								, "data-name": item.uname
								, "data-uid": item.uid
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
		
		//구분 리스트 api
		getRoleList: function(){
			let url_v = "/role/agency/staff/list"
			comm.send(url_v, null, "POST", function(resp){
				let selectIdList = ["searchRoleSelect", "addModalRoleSelect", "modifyModalRoleSelect"]
				//selectBox용 구분 그리기
				_list.drawRoleListOption(resp.list, selectIdList);
				//구분리스트용 그리기
				_list.drawRoleList(resp.list);
				_evInit();
			});
		},
		//구분 셀렉트 박스 그리기
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
				select_o.selectpicker("refresh");	
				_evInit();
			}
			
		},
		//구분 리스트 (구분별 권한 관리 모달) 그리기
		drawRoleList: function(list){
			let body_o = $("#roleListBody").html("");
			let idx = 5;
			for(let item of list){
				if(item.modify_yn == "Y"){
					let tr_o = $("<tr>");
					body_o.append(tr_o);
					
					//체크박스
					{
						let td_o = $("<td>");
						let span_o = $("<span>").addClass("chk");
						let input_o = $("<input>").attr({
							"type":"checkbox"
							, "name":"modalChk"
							, "id": "roleSort"+item.id
							, "data-id": item.id
							, "data-name": item.name
						});
						let label_o = $("<label>").attr({
							"for": "roleSort"+item.id
						});
						
						span_o.append(input_o);
						span_o.append(label_o);
						td_o.append(span_o);
						tr_o.append(td_o);
					}
						
					//구분 명
					{
						let td_o = $("<td>");
						let a_o = $("<a>").attr({
							"href": "javascript:;"
							, "data-toggle": "modal"
							, "data-target": "#roleModal"
							, "data-id": item.id
							, "data-src": "staffList"
							, "data-act": "getRoleDetail"
						});
						a_o.html(item.name);
						td_o.append(a_o);
						tr_o.append(td_o);
					}
					
					idx++;
				}
			}
			_evInit();
		},
		//구분의 메뉴 리스트 api
		getRoleMenuList: function(roleId, bodyTarget){
			let url_v = "/role/agency/staff/menu/list";
			let data_v = {
				role_id : roleId
			}
			comm.send(url_v, data_v, "POST", function(resp){
				if(resp.result){
					_list.drawRoleMenuList(resp.list[0], bodyTarget);
				}
			});
			
		},
		//구분의 메뉴 리스트 그리기
		drawRoleMenuList: function(menuList, body_v){
			
			let body_o = $("#"+body_v).html("");
			let name = "menuChk";
			
			if(body_v == "staffDetailModalMenuBody"){
				 name = "staffDetailModalMenuChk";
			}
			
			if(menuList != null && menuList != ""){
				let list = menuList.sub_list;
				
				for(let secondStep of list){
					let tr_o = $("<tr>");
					body_o.append(tr_o).trigger("create");
					
					let secondInput_o = {
						"type": "checkbox"
						, "name": name
						, "id": "roleMenu"+secondStep.menu_id
						, "data-idx": secondStep.menu_id
						, "data-src": "staffList"
						, "data-act": "chkChild"
						, "data-url": secondStep.url
					}
					
					if(body_v == "staffDetailModalMenuBody"){
						secondInput_o ={
							"type": "checkbox"
							, "name": name
							, "id": "roleMenu"+secondStep.menu_id
							, "data-idx": secondStep.menu_id
							, "data-url": secondStep.url
							, disabled: true
						}
					}
					
					
					let second_td_o = $("<td>");
					let spanChk_o = $("<span>").addClass("chk");
					let input_o = $("<input>")
					input_o.attr(secondInput_o);
					
					if(secondStep.access_yn == 'Y'){
						input_o.prop("checked", true);
					}
					spanChk_o.append(input_o);
					
					let label_o = $("<label>");
					label_o.attr({"for": "roleMenu"+secondStep.menu_id});
					spanChk_o.append(label_o);
					
					let span_o = $("<span>").addClass("chk_text");
					span_o.html(secondStep.menu_name);
					spanChk_o.append(span_o);
					second_td_o.append(spanChk_o);
					tr_o.append(second_td_o);
					
					
					let third_td_o = $("<td>");
					
					if(secondStep.sub_list){
						let thirdStepList = secondStep.sub_list; 
						for(let thirdStep of thirdStepList){
							let third_spanChk_o = $("<span>").addClass("chk");
							let third_input_o = $("<input>")
							
							let thirdInput_o = {
								"type": "checkbox"
								, "name": name
								, "id": "roleMenu"+thirdStep.menu_id
								, "data-idx": thirdStep.menu_id
								, "data-parent-id": thirdStep.parent_id
								, "data-url": secondStep.url
							};
							
							if(body_v == "staffDetailModalMenuBody"){
								thirdInput_o ={
									"type": "checkbox"
									, "name": name
									, "id": "roleMenu"+thirdStep.menu_id
									, "data-idx": thirdStep.menu_id
									, "data-parent-id": thirdStep.parent_id
									, "data-url": secondStep.url
									, "disabled": true
								}
							}
							
							
							third_input_o.attr(thirdInput_o);
							
							if(thirdStep.access_yn == 'Y'){
								third_input_o.attr({
									"checked":true
								});
							}
							third_spanChk_o.append(third_input_o);
							
							let third_label_o = $("<label>");
							third_label_o.attr({"for": "roleMenu"+thirdStep.menu_id});
							third_spanChk_o.append(third_label_o);
							
							let third_span_o = $("<span>").addClass("chk_text");
							third_span_o.html(thirdStep.menu_name);
							third_spanChk_o.append(third_span_o);
							third_td_o.append(third_spanChk_o);
						}
					}
					tr_o.append(third_td_o);
				}
			}
			
			_evInit();
		},
		//수정 이력 더보기 이벤트
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
		// 담당 광고주 리스트 그리기
		drawDemandList: function(list){
			let body_o = $("#staffDetailModalDemandBody").html("");
			
			for(let item of list){
				let tr_o = $("<tr>");
				body_o.append(tr_o).trigger("create");
				
				let second_td_o = $("<td>");
				let spanChk_o = $("<span>").addClass("chk");
				let input_o = $("<input>")
				input_o.attr({
					"type": "checkbox"
					, "name": "demandChk"
					, "id": "demandChk"+item.member_id
					, "data-idx": item.member_id
				});
				
				if(item.choose == 'Y'){
					input_o.prop("checked", true);
				}
				spanChk_o.append(input_o);
				
				let label_o = $("<label>");
				label_o.attr({"for": "demandChk"+item.member_id});
				spanChk_o.append(label_o);
				
				let span_o = $("<span>").addClass("chk_text");
				span_o.html(item.company_name + "(" + item.uid + ")");
				spanChk_o.append(span_o);
				second_td_o.append(spanChk_o);
				tr_o.append(second_td_o);
			}
			_evInit();
		},
		
	}
	
	
	return {
		init,
	}
})();