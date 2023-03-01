$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");
	//csrf
	// var token = $("meta[name = '_csrf']").attr("content");
	// var header = $("meta[name = '_csrf_header']").attr("content");
	// $(document).ajaxSend(function (e, xhr, options){
	// 	xhr.setRequestHeader(header, token);
	// })
	var title = $("#recipient-name").val();
	var content = $("#message-text").val();
	//发送异步请求
	$.post(
		CONTEXT_PATH + "/discuss/add",
		{"title":title, "content":content},
		function (data){
			data = $.parseJSON(data);
			$("#hintBody").text(data.msg);
			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				//
				if(data.code==0){
					window.location.reload();
				}
			}, 2000);
		}
	);
}