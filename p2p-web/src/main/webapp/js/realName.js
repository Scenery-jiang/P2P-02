
//同意实名认证协议
$(function() {
	$("#agree").click(function(){
		var ischeck = document.getElementById("agree").checked;
		if (ischeck) {
			$("#btnRegist").attr("disabled", false);
			$("#btnRegist").removeClass("fail");
		} else {
			$("#btnRegist").attr("disabled","disabled");
			$("#btnRegist").addClass("fail");
		}
	});


	//进行实名认证的
	$("#realName").blur(function () {
		var realName = $("#realName").val();
		if (""==realName){
			showError("realName","真实姓名不能为空")
		}else if (!/^[\u4e00-\u9fa5]{0,}$/.test(realName)){
			showError("realName","真实姓名只能是汉字");
		}else {
			showSuccess("realName");
		}
	})
	//获得焦点时，隐藏错误信息
	$("#realName").focus(function () {
		hideError("realName");
	})

	$("#idCard").blur(function () {
		var idCard = $("#idCard").val();
		if (""==idCard){
			showError("idCard","身份证号不能为空")
		}else if (!/(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/.test(idCard)){
			showError("idCard","请填写正确格式的身份证号码");
		}else {
			showSuccess("idCard");
		}
	})


	$("#idCard").focus(function () {
		hideError("idCard");
	})


	$("#captcha").blur(function () {
		var captcha  = $("#captcha").val();
		if (""==captcha){
			showError("captcha","图形验证码不能为空不能为空");
		}else {//发送ajax请求，进行后台验证
			$.ajax({
				url:"loan/checkCaptcha",
				data:{
					captcha:captcha
				},
				type:"get",
				success:function (data) {
					if ("10000"==data.code){
						showSuccess("captcha");
					} else {
						showError("captcha",data.message);
					}
				},
				error:function () {
					showError("captcha","系统繁忙，请重试");
				}
			})
		}
	})

	$("#captcha").focus(function () {
		hideError("captcha");
	})

	$("#replayIdCard").blur(function () {
		var replayIdCard = $("#replayIdCard").val();
		var idCard = $("#idCard").val();
		if (idCard!=replayIdCard){
			showError("replayIdCard","身份证号码不一致");
		}else {
			showSuccess("replayIdCard");
		}
	})

	$("#replayIdCard").focus(function () {
		hideError("replayIdCard");
	})

	//为认证绑定事件
	$("#btnRegist").click(function () {
		//依次触发三个文本框的blur事件，一是否有错误提示作为是否通过的依据
		$("#realName").blur();
		var realNameError=$("#realNameErr").text();
		if (realNameError!=""){
			return false;
		}

		$("#idCard").blur();
		var idCardErr=$("#idCardErr").text();
		if (idCardErr!=""){
			return false;
		}

		$("#replayIdCard").blur();
		var replayIdCardErr=$("#replayIdCardErr").text();
		if (replayIdCardErr!=""){
			return false;
		}

		$("#captcha").blur();
		var captchaErr=$("#captchaErr").text();
		if (captchaErr!=""){
			return false;
		}
		//发送后台请求，进行实名验证
		$.ajax({
			url:"loan/verifyIdentify",
			type: "post",
			data:{
				realName:$.trim($("#realName").val()),
				idCard:$.trim($("#idCard").val())
			},
			success:function (data) {
				if (data.code=="10000"){
					window.location.href="index";
				}else {
					showError("captcha",data.message);
				}
			},
			error:function () {
				showError("captcha","系统繁忙，请重试");
			}
		})

	})
});
//打开注册协议弹层
function alertBox(maskid,bosid){
	$("#"+maskid).show();
	$("#"+bosid).show();
}
//关闭注册协议弹层
function closeBox(maskid,bosid){
	$("#"+maskid).hide();
	$("#"+bosid).hide();
}

//错误提示
function showError(id,msg) {
	$("#"+id+"Ok").hide();
	$("#"+id+"Err").html("<i></i><p>"+msg+"</p>");
	$("#"+id+"Err").show();
	$("#"+id).addClass("input-red");
}
//错误隐藏
function hideError(id) {
	$("#"+id+"Err").hide();
	$("#"+id+"Err").html("");
	$("#"+id).removeClass("input-red");
}
//显示成功
function showSuccess(id) {
	$("#"+id+"Err").hide();
	$("#"+id+"Err").html("");
	$("#"+id+"Ok").show();
	$("#"+id).removeClass("input-red");
}