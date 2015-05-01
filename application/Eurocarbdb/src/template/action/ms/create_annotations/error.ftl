<#assign title>Create Annotations: error</#assign>
<#setting url_escaping_charset='ISO-8859-1'>

<#include "/template/common/header.ftl" />

<h1>Error while creating annotations</h1>

<#assign errs = actionErrors />
<#if ( errs?exists && errs.size() > 0) >
    <#list errs as e >
    <p>${e}</p>
    </#list>
</#if>

<#if action.hasFieldErrors() >
<h2>Parameter errors</h2>
<#list action.fieldErrors.entrySet() as e >
<p>${ e.getKey() } - ${ e.getValue() }</p>
</#list>
</#if>

<@ww.form>
  <@ww.submit value="Back" name="submitAction"/>
</@ww.form>

<#include "/template/common/footer.ftl" />