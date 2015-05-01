<#assign title>Display Biological Context</#assign>
<#include "/template/common/header.ftl" />

<h1>${title}</h1>

<p>Display a single Biological Context</p>

  <#if ( message?length > 0 )>
  <div class="error_message">
    ${message}	
  </div>
  </#if>

<@ww.form>

<@ww.textfield label="Enter the Biological Context ID" 
               name="biologicalContextId" />

<@ww.submit value="Continue ->" />

</@ww.form>

<#include "/template/common/footer.ftl" />