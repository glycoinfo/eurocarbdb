<#assign title>Errors performing action</#assign>
<#include "/template/common/header.ftl" />

<h1>${title}</h1>


!!!take further look at webwork tags first

<#if action.hasActionErrors() >
<h2>Action errors</h2>
<#list action.actionErrors as the_error >
<p>${the_error}</p>
</#list>
</#if>


<#if action.hasFieldErrors() >
<h2>Parameter errors</h2>
<#list action.fieldErrors.entrySet() as e >
<p>${ e.getKey() } - ${ e.getValue() }</p>
</#list>
</#if>


<#include "/template/common/footer.ftl" />