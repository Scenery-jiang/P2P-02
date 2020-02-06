var referrer = "";//登录后返回页面
referrer = document.referrer;
if (!referrer) {
    try {
        if (window.opener) {
            // IE下如果跨域则抛出权限异常，Safari和Chrome下window.opener.location没有任何属性
            referrer = window.opener.location.href;
        }
    } catch (e) {
    }
}

//按键盘Enter键即可登录
$(document).keyup(function (event) {
    if (event.keyCode == 13) {
        login();
    }
});

/*$(document).read(function () {
       也是页面加载完毕时执行，将document转化成jQuery对象掉用read方法
   })*/
$(function () {//页面加载完毕时
    //为文本框绑定单击事件
    $("#loginId").click(function () {
        login();
    })
})

function login() {
    $("#loginPassword").html("");
    //依次判断是否符条件
    var phon = $.trim($("#phone").val());
    if ("" == phon) {
        $("#showId").html("账号不能为空");
        return;
    }
    //密码不不能为空
    var loginPassword = $.trim($("#loginPassword").val());
    if ("" == loginPassword) {
        $("#showId").html("密码不能为空");
        return;
    }

    var captcha = $.trim($("#captcha").val());
    if ("" == captcha) {
        $("#showId").html("验证码不能为空");
        return;
    }
    $("#loginPassword").val($.md5(loginPassword));
    //发送ajax请求，登录
    $.ajax({
        url: "loan/login",
        type: "post",
        data: {
            loginPassword:$.md5(loginPassword),
            phone:phon,
            captcha:captcha
        },
        success:function (data) {
            if ("10000"==data.code){
                window.location.href = referrer;
            }else {
                $("#loginPassword").val("");
                $("#showId").html(data.message);
            }
        },
        error:function () {
            $("#loginPassword").val("");
            $("#showId").html("系统繁忙，请稍后重试");
        }
    })


}
