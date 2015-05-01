<#assign title>Create Column</#assign>
<#include "/template/common/header.ftl" />

<h3>Error occurred while creating the detector</h3> 

<#assign errs = actionErrors />
<#if ( errs?exists && errs.size() > 0) >
    <#list errs as e >
    <p>${e}</p>
    </#list>
</#if>

<#if action.hasFieldErrors() >
<h2>Parameter errors</h2>
<#list action.fieldErrors.entrySet() as e >
<p>${ e.getKey() } - already stored</p>
</#list>
</#if>

<@ww.form>
  <@ww.submit value="Back" name="submitAction"/>
</@ww.form>

<#include "/template/common/footer.ftl" />
