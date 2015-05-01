<html>
<head>
    <title>Detector details</title>
</head>

<body>

<h1>Detector details</h1>

<p>Error occurred while retrieving the detector:</p>

<#assign errs = actionErrors />
<#if ( errs?exists && errs.size() > 0) >
    <#list errs as e >
    <p>${e}</p>
    </#list>
</#if>

</body>
</html>


