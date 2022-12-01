/**
 * 页面加载事件，页面加载完毕自动调用
 */
$(function () {
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
})

/**
 * 点赞
 * @param btn
 * @param entityType
 * @param entityId
 * @param entityAuthorId
 * @param postId 帖子id，通过隐藏表单项获取
 */
function like(btn, entityType, entityId, entityAuthorId, postId) {
    $.post(
        CONTEXT_PATH + "/like",
        {"entityType": entityType, "entityId": entityId, "entityAuthorId": entityAuthorId, "postId": postId},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus == 1 ? "已赞" : "赞");
            } else alert(data.msg);
        }
    )
}

// 个人主页

/**
 * 置顶
 */
function setTop() {
    $.post(
        CONTEXT_PATH + "/discuss/top",
        {"id": $("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0)
                $("#topBtn").attr("disabled", "disabled")
            else alert(data.msg);
        }
    )
}

/**
 * 加精
 */
function setWonderful() {
    $.post(
        CONTEXT_PATH + "/discuss/wonderful",
        {"id": $("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0)
                $("#wonderfulBtn").attr("disabled", "disabled")
            else alert(data.msg);
        }
    )
}

/**
 * 删除
 */
function setDelete() {
    $.post(
        CONTEXT_PATH + "/discuss/delete",
        {"id": $("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0)
                location.href = CONTEXT_PATH + "/index";
            else alert(data.msg);
        }
    )
}
