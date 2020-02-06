


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

//注册协议确认
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


	$("#phone").blur(function () {
		var phone = $.trim($("#phone").val());
		if (""==phone){
			showError("phone","手机号码不能为空");
		}else if (!/^1[1-9]\d{9}$/.test(phone)){
			showError("phone","请输入正确格式的手机号");
		}else {
			//执行到这里说明以上两种情况都满足了，验证该手机号是否已经被注册
			$.ajax({
				url:"loan/checkPhone",
				type:"post",
				data:{
					phone:phone
				},
				success:function (data) {
					if ("10000"==data.code){
						showSuccess("phone");
					}else {
						showError("phone",data.message);
					}
				},
				error:function () {
					showError("phone","系统繁忙，请重试");
				}
			})
		}
	})

	//当获得焦点时隐藏error信息
	$("#phone").focus(function () {
		hideError("phone");
		return;
	})

	$("#loginPassword").on("blur",function () {
		var loginPassword  =$.trim( $("#loginPassword").val());
		if (""==loginPassword){
			showError("loginPassword","密码不能为空");
		}else if (!/^[0-9a-zA-Z]+$/.test(loginPassword)){
			showError("loginPassword","密码字符只可使用数字和大小写英文字母");
		}else if (!/^(([a-zA-Z]+[0-9]+)|([0-9]+[a-zA-Z]+))[a-zA-Z0-9]*/.test(loginPassword)){
			showError("loginPassword","密码应同时包含英文或数字");
		}else if (loginPassword.length<6||loginPassword.length>20){
			showError("loginPassword","密码的长度应在6-20之间");
		}else {//执行到这里，说明密码符合要求
			showSuccess("loginPassword");
			/*//将密码转成MD5的密文
			$("#loginPassword").val($.md5(loginPassword));*/
		}
	})

	$("#loginPassword").focus(function () {
		hideError("loginPassword");
		return;
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
		return;
	})


	//为注册按钮绑定单击事件
	$("#btnRegist").click(function () {
		//在发送register的请求之前，需要保证上面三个条件必须时满足的：只要触发其blur事件，error中没有提示信息说明提示成功
		$("#phone").blur();
		$("#loginPassword").blur();
		$("#captcha").blur();
		/*$("div[id$='Err']").each(function () {
            alert($(this).html());
            return;   没有起到作用  使用flag标识
        })
            alert("发送请求");*/

		//alert($("div[id$='Err']").text());  test返回的是jQuery对象所有文本的拼接字符串的值

        if (($("div[id$='Err']").text())==""){//发送ajax请求到后端
            var loginPassword  =$.trim( $("#loginPassword").val());
            //将密码进行md5的加密，赋值回密码文本框里面
            $("#loginPassword").val($.md5(loginPassword));
            $.ajax({
                url:"loan/register",
                data:{
                    "phone":$.trim($("#phone").val()),
                    "loginPassword":$.md5(loginPassword)
                },
                type:"post",
                success:function (data) {
                    if (data.code=="10000"){
                        //注册成功，跳转到实名验证
                            window.location.href="realName.jsp";
                    }

                },
                error:function () {
                    showError("captcha","系统繁忙，请重试");
                }
            })

        }
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