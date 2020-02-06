<%--
  Created by IntelliJ IDEA.
  User: 江景
  Date: 2020/1/28
  Time: 12:19
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/";
%>
<html>
<head>
    <base href="<%=basePath%>">
    <title>Title</title>
</head>
<body>
<h3>平均年化利率：${historyAverageRate}</h3>
<br>
<h3>用户注册的总人数：${allUserCount}</h3>
<br>
<h3>平台累计投资金额：${allBidMoney}</h3>

</body>
</html>
