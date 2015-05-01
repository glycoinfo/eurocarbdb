<#assign title>Device Creation</#assign>
<#include "/template/common/header.ftl" />

<h1>Device Creation</h1>

<p>Error occurred while storing the Devices:</p>

<#assign errs = actionErrors />
<#if ( errs?exists && errs.size() > 0) >
    <#list errs as e >
    <p>${e}</p>
    </#list>
</#if>

<#include "/template/common/footer.ftl" />


