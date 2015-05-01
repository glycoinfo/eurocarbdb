<#assign title>Create Instrument</#assign>
<#include "/template/common/header.ftl" />

<h2>Error occurred while creating the instrument</h2>

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
  <p>Try adding your new instrument again</p> 
  <@ww.submit value="Back" name="submitAction"/>
</@ww.form>

<#include "/template/common/footer.ftl" />
