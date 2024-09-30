/**
 * 공통 파일 유틸
 * 기능
 * 
 * 1. 파일 업로드 후 그리기 처리  
 * 2. 업로드된 파일을 처리 
 * 3. 업로드 파일 size 체크 
 * 4. 업로드 파일 확장자 체크 
 * 5. 업로드 파일 재생시간 확인 ( 비디오인 경우 )
 * 6. 업로드한 파일 삭제시 처리
 */
const fileUtil = (function() {

	// 파일 제한 300MB 
	let _limitSize = 1024 * 1024 * 300;

	// 파일 제한 확장자
	let _limitExt = [
		"jpg", "png", "mp4", "mov",
	];
	
	/**
	 * 설정된 콜백함수 실행 함수 
	 * callback: 콜백 함수
	 * data: 콜백함수에 넘겨줄 데이터
	 */
	function _processCallback(callback, data) {
		if(typeof(callback) == "function") {
			callback(data);
		}
	}
	
	/*
	 * 업로드 파일 사이즈 체크
	 * file: file 객체
	 */
	function isUploadableSize(file) {
		if(file && file instanceof File ) {
			if(file.size > _limitSize) {
				return false;
			}
			return true;
		}
		return false;
	}
	
	/**
	 * 업로드 파일 확장자 체크 
	 * file: file 객체
	 * allowList: 허용 파일 확장자 리스트
	 * - default: ["jpg", "png", "mp4", "mov"]
	 * 
	 */
	function isUploadableExt(file, allowList = _limitExt) {
		 if(allowList == null || allowList.length == 0) return false; 
		 
		 allowList = allowList.map(item => {
			 return item.toLowerCase();
		 });
		 
		 let fileName = file.name;
		 let fileExt = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length);
		 
		 if(allowList.includes(fileExt)) {
			 return true;
		 }
		 
		 return false;
	}
	
	/**
	 * 비디오 파일 재생시간 체크
	 *  file: file 객체 
	 *  callback: 콜백함수 
	 */
	function getFileDuration(file, callback) {
		
		if(file instanceof File) {
			let video_o = document.createElement("video");
			// file 인 경우 (input)
			video_o.src = URL.createObjectURL(file);
			$(video_o).off();
			$(video_o).on("loadedmetadata", function(e) {
				_processCallback(callback, {duration: this.duration});
			});
		} else {
			_processCallback(callback, null);
		} 
	}
	
	/**
	 * 파일 정보 리턴
	 * file: file 객체
	 * callback: 콜백함수
	 */
	function getFileInfo(file, callback) {
		if(file instanceof File) {
			let type = file.type;
			if(type.startsWith("image")) {
				let reader = new FileReader();
				reader.readAsDataURL(file);
				reader.onload = function(e) {
					let img = new Image();
					img.src = e.target.result;
					img.onload = function() {
						
						let width = this.width;
						let height = this.height;
						
						_processCallback(callback, {
							width, 
							height
						});
					}
				}
			} else if(type.startsWith("video")) {
				let video = document.createElement("video");
				video.src = URL.createObjectURL(file);
				$(video).on("loadedmetadata", function(e) {
					let width = this.videoWidth;
					let height = this.videoHeight;
					let duration = this.duration;
					
					_processCallback(callback, {
						width, 
						height, 
						duration
					});
				});
			}
		}
	}
	
	/**
	 * 업로드할 파일 정보 그리기 
	 * 파일이 선택되지 않았을땐 label 의 text 지워줌
	 * file: file 객체
	 * inputId: input 태그 id 
	 * emptyText: 파일을 첨부하지않았을때 보여줄 텍스트 default: ""
	 */
	function setUploadFile(file, inputId, emptyText = "") {
		if(file instanceof File) {
			if(inputId != null && $("#" + inputId).get(0) != null) {
				let labelElement = $("label[for='" + inputId + "']");
				if(labelElement.get(0)) {
					if(file) {
						let fileName = file.name;
						labelElement.html(fileName);
					} else {
						labelElement.html(emptyText);
					}
				}
			}
		}
	}
	
	/**
	 * 비디오 영상 시간 	
	 * 영상시간 오차범위 2번째 소수점 내림해서 15.0, 30.0 가능
	 */
	function floorDuration(duration) {
		if(!duration) {
			return 0;
		}
		return parseFloat(duration.toFixed(1));
	}
	
	/**
	 * 비디오 load 함수
	 * 영상이 준비되지 않으면 가져올 때 까지 반복적으로 load
	 */
	function loadVideo(video, callback) {
		if(video[0].tagName == "VIDEO") {
			let _interval = setInterval(function() {
				if(video.get(0).readyState == 4) {
					_processCallback(callback, null);
					clearInterval(_interval);
				} else {
					video.get(0).load();
				}
			}, 1000);
		}
	}
	
	return {
		isUploadableSize,
		isUploadableExt,
		getFileDuration,
		getFileInfo,
		setUploadFile,
		floorDuration,
		loadVideo,
	}
})();