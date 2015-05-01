
<html>
<head>
<#assign title>Login successful</#assign>
<#include "/template/common/header.ftl" />
<script language="text/javascript">
$(function(){
    top.location="${redirectedUrl!"contribute.action"}";
});
</script>
<meta http-equiv="REFRESH" content="0;url=contribute.action"></HEAD>
</head>
<body>
<h1>${title}</h1>

<#include "/template/common/footer.ftl" />
</body>
</html>

