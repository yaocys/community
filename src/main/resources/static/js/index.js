$(function () {
    // 给按钮绑定单击事件
    $("#publishBtn").click(publish);
});

function publish() {
    // 隐藏帖子内容的编辑框
    $("#publishModal").modal("hide");

    /*
    发送异步请求前将CSRF令牌设置到请求的请求头中
     */
    // const token = $("meta[name='_csrf']").attr("content");
    // const header = $("meta[name='_csrf_header']").attr("content");
/*    $(document).ajaxSend(function (e, xhr, options) {
        xhr.setRequestHeader(header, token);
    });*/

    // 获取标题和内容
    const title = $("#recipient-name").val();
    const content = $("#message-text").val();
    // 发送异步请求
    $.post(
        CONTEXT_PATH + "/discuss/add",
        {"title": title, "content": content},
        // 回调函数，处理返回结果
        function (data) {
            data = $.parseJSON(data);
            // 在提示框中显示返回信息
            $("#hintBody").text(data.msg);
            // 显示提示框
            $("#hintModal").modal("show");
            // 2s后自动隐藏提示框
            setTimeout(function () {
                $("#hintModal").modal("hide");
                // 发送成功，刷新页面
                if (data.code == 0) window.location.reload();
            }, 2000);
        }
    );
}