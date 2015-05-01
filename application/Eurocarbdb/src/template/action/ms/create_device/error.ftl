<html>
<head>
    <title>Device Creation</title>
</head>

<body>

<h1>Device Creation</h1>

<p>Error occurred while storing the Device:</p>

<#assign errs = actionErrors />
<#if ( errs?exists && errs.size() > 0) >
    <#list errs as e >
    <p>${e}</p>
    </#list>
</#if>

</body>
</html>


