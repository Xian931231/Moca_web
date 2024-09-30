const customDatePicker = (function(){
	let _options = {
        format: "yyyy-mm-dd",
        //startDate: date,
        maxViewMode: 2,
        language: "ko",
        todayHighlight: false,
        startView: 0,
        disableTouchKeyboard:true,
        autoclose: true,
        orientation: "auto bottom", // popup 위치 
        //multidateSeparator: false,
        //defaultViewDate: { year: new Date().getFullYear(), month: new Date().getMonth(), day: new Date().getDate() }
        //defaultViewDate:	today,
        //daysOfWeekHighlighted: [0,6],
	}
	
	function init(elementId, option){
		let element = $("#" + elementId);
		_options = $.extend(true, _options, option);
		
		element.attr("readonly", true);
		
		return element.parent().datepicker(_options);
		
	}
	
	return {
		init,
	}
})();