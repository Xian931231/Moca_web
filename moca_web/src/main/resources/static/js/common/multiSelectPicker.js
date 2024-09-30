// mure96 
// multiselectpicker 생성 및 전체 옵션 선택 로직 처리
const multiSelectPicker = (function() {
	
	let _option = {
		element: null,		// 요소
		dataList: null,		// data list  
		isAllOption: false, // true, false 전체 옵션 여부
		textAllOption: "전체", // 전체 옵션 텍스트 default: 전체
		valueKey: null,		// option 의 value 속성 key 값
		textKey: null,		// option 의 text key 값
		callback: {
			changed: null,
		}
	}
	
	function init(option) {
		
		_option = $.extend(true, _option, option);
		
		let element = _option.element;
		
		if(element) {
			let element_o = $(element).html("");
			
			let dataList = _option.dataList;
			
			if(dataList) {
				for(let i=0; i<dataList.length; i++) {
					let item = dataList[i];
					if(i == 0 && _option.isAllOption) {
						let all_o = $("<option>").attr({
							"value": "0",
						}).html(_option.textAllOption);
						element_o.append(all_o);
					}
					
					let option_o = $("<option>").attr({
						"value": item[_option.valueKey],
					}).html(item[_option.textKey]);
					element_o.append(option_o);
				}
				
				$(element_o).selectpicker();
				
				if(_option.isAllOption) {
					$(element_o).off("changed.bs.select");
					$(element_o).on("changed.bs.select", function(e, index, isSelected, prevValue) {
						let currentValue = $(element_o).find("option:eq("+index+")").attr("value");
						
						let values = [];
						if(isSelected) {
							$(element_o).find("option").each(function(index, option_o) {
								values.push(option_o.value);
							});
						}
						
						if(currentValue === "0") {
							$(element_o).val(values);
							$(element_o).selectpicker("refresh");
						} else {
							values = $(element_o).val();
							values = values.filter(val => {
								return val !== "0";
							});
							
							if(dataList.length === values.length) {
								values.push("0");
							}
							$(element_o).val(values);
							$(element_o).selectpicker("refresh");
						}
					});
					
					if(_option.callback.changed && typeof(_option.callback.changed) === "function") {
						_option.callback.changed(e, index, isSelected, prevValue);
					}
				}
			}
		}
		return element;
	}
	
	return {
		init,
	}
})();