(function($) {
    $.fn.customPaging = function(option, callback) {

        /**
         * 옵션
         */
        var _option = {
            // 보여질 게시글 수
            limit: 20,
            // 보여질 페이지 수
            showPageNum: 5,
            // 데이터가 없을 경우 페이징 표시여부
            isShowNoData: true
        }
        
        _option = $.extend(_option, option);

        /**
         * 내부 변수
         */
        var _paging_o = this;
        // 현재 페이지
        var _curPage = 1;
        // 첫행 게시글 번호
        var _offset = 0;
        // 전체 게시글 수
        var _totalCount = 0;
        // 전체 페이지 수
        var _totalPage = 0;
        // 시작 페이지
        var _startPage = 0;
        // 끝 페이지
        var _endPage = 0;

        var _callback = callback;

        function _setLimit(limit) {
      	  _option.limit = limit;
        };
        
        // 파라미터 구하기
        function _getParam(curPage) {
            if (curPage) {
                _curPage = curPage;
            }
            var limit = _option.limit;
            _offset = (_curPage - 1) * limit;

            return {
                offset: _offset,
                limit: limit
            };
        };

        // 페이지 그리기 
        function _drawPage(totalCount) {
            var limit = _option.limit;
            if(limit == null) {
            	limit = totalCount;
            }
            var showPageNum = _option.showPageNum;
            _totalCount = totalCount;
            _totalPage = _totalCount == 0 ? 1 : Math.ceil(totalCount / limit);
            _startPage = Math.floor((_curPage - 1) / showPageNum) * showPageNum + 1;
            _endPage = _startPage + showPageNum - 1;

            if (_endPage > _totalPage) {
                _endPage = _totalPage;
            }
            //0401 방어코드 추가(현재페이지가 토탈 페이지보다 크면 현재페이지를 1로 바꿔버림)
            if(_curPage > _totalPage) {
				_curPage = 1;
			}

            $(_paging_o).html("");
            
            if(_option.isShowNoData == false && totalCount == 0) {
                return;
            }

            
            var fristDisabled = '';
            var lastDisabled = '';
            //_curPage 현재페이지
            if(_curPage == 1){
				fristDisabled = ' disabled';
            }
                    
//			//첫페이지이동 버튼 
//			var firstBtn = $("<li>").addClass("page-item" + fristDisabled);
//        	var firstPage = $("<a>").addClass("page-link").attr({ "href": "javascript:;"}).html("<span aria-hidden='true'>«</span>");
//        	_paging_o.append(firstBtn.append(firstPage));
//        	//첫페이지이동 버튼 이벤트 바인딩
//			$(firstPage).off();
//			$(firstPage).on("click", function(){
//				_fnPageClick(1);
//			});
			
			//이전버튼
			var li_o = $("<li>").addClass("page-item" + fristDisabled);
			var prev_o = $("<a>").addClass("page-link").attr({ "href": "javascript:;", "aria-label": "Previous" }).html("&lt;");
        	_paging_o.append(li_o.append(prev_o));
        	//이전버튼 이벤트 바인딩
        	$(prev_o).off();
            $(prev_o).on("click", function(ev) {
                _fnPageClick( Number(_curPage) - 1);
            });

            for (var i = _startPage; i <= _endPage; i++) {
                var li_o = $("<li>").addClass("page-item");
                if (i == _curPage) {
                    li_o.addClass("active");
                }
                var a_o = $("<a>").addClass("page-link");
                a_o.attr("href", "javascript:;");
                a_o.attr("data-page", i);
                a_o.text(i);

                li_o.append(a_o);

                $(a_o).off();
                $(a_o).on("click", function(ev) {
                    var target = ev.currentTarget;
                    var pageNum = $(target).attr("data-page");
                    _fnPageClick(pageNum);
                });

                $(_paging_o).append(li_o);
            }
            
			if(_curPage == _totalPage){
				lastDisabled = ' disabled';
			}
			//다음버튼
			var li_o = $("<li>").addClass("page-item" + lastDisabled);
            var next_o = $("<a>").addClass("page-link").attr({ "href": "javascript:;", "aria-label": "Next" }).html("&gt;");
            _paging_o.append(li_o.append(next_o));
            
            //다음버튼 이벤트 바인딩
            $(next_o).off();
            $(next_o).on("click", function() {
                _fnPageClick( Number(_curPage) + 1);
            });
            
//            //마지막페이지 버튼
//			var lastBtn = $("<li>").addClass("page-item" + lastDisabled);
//			var lastPage = $("<a>").addClass("page-link").attr({ "href": "javascript:;"}).html("<span aria-hidden='true'>»</span>");
//			_paging_o.append(lastBtn.append(lastPage));
//			
//			//마지막페이지 버튼 이벤트 바인딩
//			$(lastPage).off();
//			$(lastPage).on("click", function(){
//				_fnPageClick(_totalPage);
//			});
			
        };

        // 페이지 클릭 이벤트
        function _fnPageClick(pageNum) {
            if (typeof(_callback) == "function") {
                _curPage = pageNum;
                _callback(pageNum);
            }
        }

        return {

        	setLimit: function(limit) {
        		_setLimit(parseInt(limit));
        	},
        	
            getParam: function(curPage) {
                return _getParam(curPage);
            },

            drawPage: function(totalCount) {
                _drawPage(totalCount);
            },

            getCurPage: function() {
                return _curPage;
            }
        };
    };


}(jQuery));