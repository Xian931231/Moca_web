/**
 * customModal
 */
const customModal = (function(){
	/**
	 * alert 
	 * option : {
	 *  title: 제목,
	 *  content: 내용,
	 *  confirmCallback: 닫힘 콜백
	 *  confirmText: 확인 버튼 텍스트 
	 * }
	 */
	function alert(option){
		
		let modal_o = $("<div>").addClass("modal fade").attr({
			"data-backdrop": "static",
			"data-keyboard": false,
			"tabindex": -1,
			"aria-labelledby": "staticBackdropLabel",
			"aria-hidden": true,
		});
		{
			let dialog_o = $("<div>").addClass("modal-dialog");
			modal_o.append(dialog_o);
			
			let content_o = $("<div>").addClass("modal-content");
			dialog_o.append(content_o);
			
			{
				let header_o = $("<div>").addClass("modal-header");
				content_o.append(header_o);
				
				if(option.title != null) {
					let title_o = $("<h4>").addClass("modal-title");
					header_o.append(title_o);
					title_o.html(option.title);
				}
				
				let close_o = $("<button>").addClass("close").attr({
					"type": "button",
					"data-dismiss": "modal",
					"aria-label": "Close",
				}).html("<span aria-hidden='true'>&times;</span>");
				header_o.append(close_o);
			}
			{
				let body_o = $("<div>").addClass("modal-body");
				content_o.append(body_o);
				
				if(option.content != null) {
					body_o.append("<p>" + option.content + "</p>");
				}
			}
			{
				let footer_o = $("<div>").addClass("modal-footer");
				content_o.append(footer_o);
				
				let confirm_o = $("<button>").addClass("modal-close-btn").html("확인");
				footer_o.append(confirm_o);
				
				
				if(option.confirmText != null) {
					confirm_o.html(option.confirmText);
				}			
				
				confirm_o.off("click");
				confirm_o.on("click", function(){
					$(modal_o).modal("hide");
					if(typeof(option.confirmCallback) == "function") {
						option.confirmCallback();
					}
				});
			}
		}
		
		$(document.body).append(modal_o);
		
		$(modal_o).modal("show");
		
		$(modal_o).off("hidden.bs.modal");
		$(modal_o).on("hidden.bs.modal", function(){
			$(modal_o).remove();
		});
	}
	
	/**
	 * confirm 
	 * 
	 * option : {
	 *  title: 제목,
	 *  content: 내용,
	 *  confirmCallback: 확인 버튼 콜백
	 *  confirmText: 확인 버튼 텍스트 
	 *  cancelCallback: 취소 버튼 콜백 
	 *  cancelText: 취소 버튼 텍스트
	 * }
	 */
	function confirm(option){ 
		let modal_o = $("<div>").addClass("modal fade").attr({
			"data-backdrop": "static",
			"data-keyboard": false,
			"tabindex": -1,
			"aria-labelledby": "staticBackdropLabel",
			"aria-hidden": true,
			"id" : "id-modal4"
		});
		{
			let dialog_o = $("<div>").addClass("modal-dialog");
			modal_o.append(dialog_o);
			
			let content_o = $("<div>").addClass("modal-content");
			dialog_o.append(content_o);
			
			{
				let header_o = $("<div>").addClass("modal-header");
				content_o.append(header_o);
				
				if(option.title != null) {
					let title_o = $("<h4>").addClass("modal-title");
					header_o.append(title_o);
					title_o.html(option.title);
				}
				
				let close_o = $("<button>").addClass("close").attr({
					"type": "button",
					"data-dismiss": "modal",
					"aria-label": "Close",
				}).html("<span aria-hidden='true'>&times;</span>");
				header_o.append(close_o);
			}
			{
				let body_o = $("<div>").addClass("modal-body");
				content_o.append(body_o);
				
				if(option.content != null) {
					body_o.append("<p>" + option.content + "</p>");
				}
			}
			{
				
				let footer_o = $("<div>").addClass("modal-footer");
				content_o.append(footer_o);

				let cancel_o = $("<button>").addClass("btn btn-sm btn-clear").attr({
					"type": "button",
					"data-dismiss": "modal"
				}).html("취소");
				footer_o.append(cancel_o);
				
				if(option.cancelText != null) {
					cancel_o.html(option.cancelText);
				}
				
				cancel_o.off("click");
				cancel_o.on("click", function(){
					$(modal_o).modal("hide");
					if(typeof(option.cancelCallback) == "function") {
						option.cancelCallback();
					}
				});
				
				let confirm_o = $("<button>").addClass("btn btn-sm btn-dark").attr({
					"type": "button",
					"data-dissmiss": "modal",
				}).html("확인");
				footer_o.append(confirm_o);
				
				if(option.confirmText != null) {
					confirm_o.html(option.confirmText);
				}	
				
				confirm_o.off("click");
				confirm_o.on("click", function(){
					$(modal_o).modal("hide");
					if(typeof(option.confirmCallback) == "function") {
						option.confirmCallback();
					}
				});
				
				
			}
		}
		
		$(document.body).append(modal_o);
		
		$(modal_o).modal("show");
		
		$(modal_o).off("hidden.bs.modal");
		$(modal_o).on("hidden.bs.modal", function(){
			$(modal_o).remove();
		});
	}
	
	return {
		alert,
		confirm,
	}
})();