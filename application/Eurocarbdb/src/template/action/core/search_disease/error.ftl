<html>
<head>
    <title>Taxonomy detail</title>
</head>

<body>

<h1>Taxonomy search</h1>

<p>Errors:</p>

<#assign errs = actionErrors />
<#if ( errs?exists && errs.size() > 0) >
    <#list errs as e >
    <p>${e}</p>
    </#list>
</#if>

</body>
</html>


