$(function () {
    //点击发布
    $("#publishBtn").click(publish);


});

$(function () {
    //点击发布
    $("#publishNoticeBtn").click(publishNotice);


});

$(function () {
    //点击发布
    $("#load_more").click(load_more);


});

function publishNotice() {
    $("#NoticeModal").modal("hide");

    // 发送AJAX请求之前,将CSRF令牌设置到请求的消息头中.
    //var token = $("meta[name='_csrf']").attr("content");
    // var header = $("meta[name='_csrf_header']").attr("content");
    // $(document).ajaxSend(function(e, xhr, options){
    //     xhr.setRequestHeader(header, token);
    // });

    //获取标题和内容

    var content = $("#notice-text").val();

    //发送异步的请求(post)
    $.post(
        CONTEXT_PATH + "/Notice/add",
        {"content": content},
        //返回值
        function (data) {
            data = $.parseJSON(data);
            //在提示框中显示返回信息
            $("#hintBody").text(data.msg);
            //显示提示框
            $("#hintModal").modal("show");
            //两秒后，自动隐藏
            setTimeout(function () {
                $("#publishModal").modal("hide");
                $("#hintModal").modal("show");
                setTimeout(function () {
                    $("#hintModal").modal("hide");
                    //刷新页面
                    if(data.code==0){
                        window.location.reload();
                    }
                }, 2000);
            })
        }
    );
}


function publish() {
   // $("#publishModal").modal("hide");

    // 发送AJAX请求之前,将CSRF令牌设置到请求的消息头中.
    //var token = $("meta[name='_csrf']").attr("content");
    // var header = $("meta[name='_csrf_header']").attr("content");
    // $(document).ajaxSend(function(e, xhr, options){
    //     xhr.setRequestHeader(header, token);
    // });

    //获取标题和内容
    var title = $("#recipient-name").val();
    var content = $("#message-text").val();
    var tag=$("#content-tag").val();
    //发送异步的请求(post)
    $.post(
        CONTEXT_PATH + "/discuss/add",
        {"title": title, "content": content,"tag":tag},
        //返回值
        function (data) {
            data = $.parseJSON(data);
            //在提示框中显示返回信息
            if(data.code==0){
                window.location.reload();
            }
        }
    );
}

var numb=2;
var selectedTag=null;
function load_more() {

    $.post(
        CONTEXT_PATH + "/discuss/load_more?pageNumb="+numb+"&tag="+selectedTag,

        //返回值
        function (data) {
            numb++;
            //data = $.parseJSON(data);

            //$(".postList").html(data);
            //$("#hintBody").text(data);
            //$("#hintModal").modal("show");


            $('.list-unstyled').append(data);
            //$('.load_more').hidden=true;


        }
    );

}

function changeFilter(type) {
    window.href=CONTEXT_PATH+"/index2";


}
