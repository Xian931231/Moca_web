"use strict";
$(document).ready(function() {
  
  /*공통 스크립트  - 헤더, 사이드바, 푸터 인클루드*/
  /*
  $("header").load("header.html");
  $(".sidebar.cus").load("sidebarCus.html"); //광고주
  $(".sidebar.med").load("sidebarMed.html"); //매체사
  $(".sidebar.ag").load("sidebarAg.html"); //대행사
  $(".sidebar#moCafe").load("sidebarMoca.html"); //모카페관리자
  $("footer").load("footer.html");
	*/

  /*공통 스크립트 - 사이드바 설정*/
  $(".sidebar-wrapper>ul>li>a[data-toggle=collapse]").on("click", function(){
    var $_this = $(this);
    //$(".sidebar-wrapper>ul>li>div").removeClass("show");
    //$_this.siblings("div").addClass("show");
    $(".sidebar-wrapper>ul>li").removeClass("active");
    $($_this).parent("li").toggleClass("active");
        if ( $($_this).parent("li").hasClass("active") == true ) {
          //$(this).parent("div").addClass("show");
          $(this).parent("div").show();
        } else if ($($_this).parent("li").hasClass("active") == false) {
          //$(this).parent("div").removeClass("show");
          $(this).parent("div").hide();
        }
  });
  $(".sidebar-wrapper>ul>li").removeClass("active");
  // .sidebar:hover .navbar-nav li.active
  
  /*툴팁 설정*/
  $('[data-toggle="tooltip"]').tooltip();
  
  /*sorting 설정*/
  $(".sortingArrow i").click(function(){
    $(this).toggleClass("turningA");
    return false;
  });
  
  
  
});


