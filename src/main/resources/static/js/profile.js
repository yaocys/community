$(function(){
	$(".follow-btn").click(follow);
});

function follow() {
	const btn = this;

	$.post(
		CONTEXT_PATH + "follow",
		{"entityType":3,"entityId":$(btn).prev().val()},
		function (data){
			data = $.parseJSON(data);
			if(data.code==0){
				// 这里理应还要改动关注数量什么的，图省事儿就直接刷新了页面
				// 那这样异步请求意义何在？！
				window.location.reload();
			}else alert(data.msg);
		}
	)

	if($(btn).hasClass("btn-info")) {
		// 关注TA
		$(btn).text("已关注").removeClass("btn-info").addClass("btn-secondary");
	} else {
		// 取消关注
		$(btn).text("关注TA").removeClass("btn-secondary").addClass("btn-info");
	}
}