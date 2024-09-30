const areaList=(function() {
	
	//조회한 data 보관용
	let _old_data_v = {};
	
	function init(){
		_setDatepicker();
		_list.getDemandList();
		_map.init();
		_evInit();
	}
	
	function _evInit(){
		let evo = $("[data-src='areaList'][data-act]").off();
		evo.on("click", function(ev) {
			_action(ev);
		});
	}
	
	function _action(ev){
		let evo = $(ev.currentTarget);
		
		let type_v = ev.type;
		
		let act_v = evo.attr("data-act");
		
		let event = _event;
		
		if(type_v == "click") {
			if(act_v == "clickSearchBtn") {
				event.clickSearchBtn();
			}else if(act_v == "clickExcelDownloadBtn"){
				event.clickExcelDownloadBtn();
			}
		} 
	}
	
	
	let _event = {
		//검색버튼 클릭
		clickSearchBtn: function(){
			_list.getTableList();
		},
		//엑셀 버튼 클릭
		clickExcelDownloadBtn: function(){
			let url_v = "/report/agency/excel/byArea";
			util.blobFileDownload(url_v, _old_data_v, function(){});
			
		}
	};
	
	let _list = {
		// 리포트 
		getTableList: function(){
			let url_v = "/report/agency/list/byArea";
			
			let data_v = _list.getSearchData();
			
			//검색 조건 제한 사항 체크
			if(!_list.getValidate(data_v)){
				return;
			}
			
			_old_data_v = data_v;
			comm.send(url_v, data_v, "POST", function(resp){
				//data가 있으면 엑셀 버튼 노출
				if(resp.list.length > 0){
					$("#excelBtn").show();
				}else{
					$("#excelBtn").hide();
				}
				
				let siCode_o = $("#selectSiList option:selected");
				let siCode_v = siCode_o.val();
				let mapZoom = null;
				if(siCode_v && siCode_v != "all") {
					mapZoom = globalConfig.getMapZoomBySiCode(siCode_v);
				}else{
					mapZoom = globalConfig.getMapZoomBySiCode("all");
				}
				
				_list.getMapList(mapZoom);
				_draw.drawTableList(resp.list);
			});
		},
		//지도 관련 정보 조회
		getMapList: function(mapZoom){
			let url_v = "/report/agency/list/byAreaMap";
			let data_v = _list.getSearchData();
			
			if(_list.getValidate(data_v)){
				comm.send(url_v, data_v, "POST", function(resp){
					_map.clearMarker();
					_map.setMap(resp.list, mapZoom);
				});
			}
		},
		//검색조건 
		getSearchData: function(){
			let startDate_v = $("#sDate").val();
			let endDate_v = $("#eDate").val();
			let memberId_v = $("#selectDemandList").val();
			
			
			
			let data = {
				str_dt: startDate_v
				, end_dt: endDate_v
				, member_list: memberId_v
				, map_level: _map.instance.getZoom(),
			}
			
			let siCode_v = $("#selectSiList").val();
			if(siCode_v && siCode_v != "all") {
				data.si_code = siCode_v;
			}
			
			let guCode_v = $("#selectGuList").val();
			if(guCode_v && guCode_v != "all") {
				data.gu_code = guCode_v;
			}
			
			
			return data;
			
		},
		//검색 조건 체크
		getValidate: function(data){
			let result = true;
			
			if(util.getDiffDate(data.str_dt, data.end_dt, "months") >= 3) {
				customModal.alert({
					content: "최대 조회 기간은 3개월입니다.",
				});
				result = false;
			}
			
			if(moment(data.str_dt).isAfter(data.end_dt)) {
				customModal.alert({
					content: "시작일이 종료일 이후일 수 없습니다.",
				});
				result = false;
			}	
			
			
			return result;
		},
		
		// 지역 목록 조회
		getAreaList: function(siCode = null,){
			let url_v = "/common/areacode/list";
			
			let data_v = {
				si_code: siCode,
			}
			
			comm.send(url_v, data_v, "POST", function(resp){
				_draw.drawAreaList(resp.list, siCode);
			});
		},
		
		getDemandList: function(){
			let url_v = "/report/agency/access/demand/list";
			
			comm.send(url_v, null, "POST", function(resp){
				_draw.drawDemandList(resp.list);
				_list.getAreaList();
				_list.getTableList();
				_list.getMapList();
			});
		}
		
	};
	
	let _draw = {
		// 지역 목록 그리기 시,구
		drawAreaList: function(list, siCode){
			let key = "si";
			
			let list_o = $("#selectSiList").off("changed.bs.select");
			list_o.on("changed.bs.select", function(e, clickedIndex){
				let value = $(e.currentTarget).val();
				_list.getAreaList(value);
				
			});
			if(siCode != null) {
				list_o = $("#selectGuList");
				key = "gu";
			}
			list_o.html("");
			
			for(let i=0; i<list.length; i++) {
				let item = list[i];
				if(i == 0) {
					let option_o = $("<option>").attr({
						"value":"all"
					}).html("전체 지역");
					list_o.append(option_o);
				}
				let option_o = $("<option>").html(item[key + "_name"]).attr({
					"data-id": item[key + "_code"],
					"value": item[key + "_code"],
					"data-lat": item[key + "_latitude"],
					"data-lng": item[key + "_longitude"],
				});
				list_o.append(option_o);
			}
			
			list_o.selectpicker("refresh");
			
		},
		
		//담당 광고주 옵션 그리기
		drawDemandList: function(list){
			let option = {
					element: $("#selectDemandList")
					, dataList: list
					, isAllOption: true
					, textAllOption: "전체 매체"
					, valueKey: "member_id"
					, textKey: "company_name"
				}
				
				
				multiSelectPicker.init(option);
				
				let memberArr = [0];
				for(let item of list){
					memberArr.push(item.member_id);
				}
				$("#selectDemandList").val(memberArr);
				$("#selectDemandList").selectpicker("refresh");
			_evInit();
			
		},
		
		//리포트 테이블 그리기
		drawTableList: function(list){
			let body_o = $("#listBody").html("");
			let total_v = 0;
			//광고주
			for(let i = 0; i < list.length; i++){
				let demandItem = list[i];
				
				let tr_o = $("<tr>");
				body_o.append(tr_o);
				
				let th_o = $("<th>").html(demandItem.company_name).attr("rowspan", demandItem.rowspan);
				tr_o.append(th_o);
				
				let siList = demandItem.si_list;
				for(let j = 0; j < siList.length; j++){
					let siItem = siList[j];
					
					if(j > 0){
						tr_o = $("<tr>");
						body_o.append(tr_o);
					}
					
					//지역
					let td_o = $("<td>").html(siItem.si_name).attr("rowspan", siItem.rowspan);
					tr_o.append(td_o);
					
					let guList = siItem.gu_list;
					if(guList){
						for(let z = 0; z < guList.length; z++){
							let guItem = guList[z];
							
							if(z > 0){
								tr_o = $("<tr>");
								body_o.append(tr_o);
							}
							
							//지역 구분
							{
								let td_o = $("<td>").html(guItem.gu_name);
								tr_o.append(td_o);
							}
							//노출량
							{
								let td_o = $("<td>").html(util.numberWithComma(guItem.cnt));
								tr_o.append(td_o);
								total_v += guItem.cnt;
							}
							
						}	
					}else{
						{
							// 지역 구분
							let td_o = $("<td>").html("-");
							tr_o.append(td_o);
						}
						{
							// 노출량
							let td_o = $("<td>").html("-");
							tr_o.append(td_o);
						}
					}
					
				}
				
			}
			
			//합계
			$("#listBodyTotal").html(util.numberWithComma(total_v));
			
		}
	}
	
	let _map = {
		markers: null,
			
		instance: null,
		
		init: function(){
			let mapInstance = new naver.maps.Map('mapWrap', globalConfig.getMapOption());
			_map.instance = mapInstance;
			// 지도 레벨 변경
			mapInstance.zoom_changed = (zoom) => {
				//변경되면 이벤트 발생 
				_list.getMapList();
			}
		},
		getMap: () => {
			return _map.instance;
		},
		
		setMap: (list, mapZoom) => {
			if(mapZoom != null && mapZoom != ""){
				let mapLat = 127.77;
				let mapLng = 36.33;
				
				let siCode_o = $("#selectSiList option:selected");
				let siCode_v = siCode_o.val();
				if(siCode_v && siCode_v != "all") {
					mapLat = siCode_o.attr("data-lat");
					mapLng = siCode_o.attr("data-lng");
				}
				
				let guCode_o = $("#selectGuList option:selected");
				let guCode_v = guCode_o.val();
				if(guCode_v && guCode_v != "all") {
					mapZoom = 14;
					mapLat = guCode_o.attr("data-lat");
					mapLng = guCode_o.attr("data-lng");
				}
				
				_map.instance.updateBy(new naver.maps.LatLng(mapLng, mapLat), mapZoom);
			}
			

			let list_o = $("#mapMarkerDiv").html("");
			for(let item of list) {
				
				let marker_o = "<a class='marker_city' href='javascript:;' role='button' 'aria-hidden': false, 'aria-pressed': false>";
					marker_o += "<div class='marker_city-inner'>";
					marker_o += "<div class='city_feature'>";
					marker_o += item.display_name;
					marker_o += "</div>";
					marker_o += "<div class='city_infos'>";
					marker_o += util.numberWithComma(item.cnt);
					marker_o += "</div>";
					marker_o += "<div class='marker_transparent'>";
					marker_o += "</div>";
					marker_o += "</div>";
				
				let position = new naver.maps.LatLng(item.latitude, item.longitude);
				
				let marker = new naver.maps.Marker({
					position: new naver.maps.LatLng(item.latitude, item.longitude),
					map: _map.getMap(),
					icon: {
						content: marker_o,
					}
				});
				
				marker.addListener("mouseover", (e) => {
					e.overlay.setZIndex(1000);
				});
				
				marker.addListener("mouseout", (e) => {
					e.overlay.setZIndex(100);
				});
				
				if(_map.markers === null) {
					_map.markers = [];
				}
				
				_map.markers.push(marker);
			}
		},
		
		clearMarker: function() {
			if(_map.markers && _map.markers.length > 0) {
				_map.markers.forEach(function(marker) {
					marker.setMap(null);
				});
				_map.markers = [];
			}
		}
		
	}
	
	function _setDatepicker() {
		customDatePicker.init("sDate").datepicker("setDate",moment().subtract(7, "days").format("YYYY-MM-DD"));
		customDatePicker.init("eDate").datepicker("setDate",moment().format("YYYY-MM-DD"));
	}
	
	return {
		init,
	}
	
})();