
<!-- begin main content -->
<div id="main">      
<#if progressSteps?exists>
<div class="breadcrumbs breadcrumbs_multi_row">
<#assign firstStep = 1/>
	
<#list progressSteps as ps>
  <#if firstStep==0 >	
  <div>&gt;</div>
  </#if>

  <#if (ps==currentStep) >
  <div><b>${ps}</b></div>
  <#else/>
  <div>${ps}</div>
  </#if>

  <#assign firstStep = 0/>
</#list>
  </div>
  <br />
</#if><#-- end if progressSteps exists -->

<#if (actionErrors?exists && actionErrors.size()>0) || ( fieldErrors?exists && fieldErrors.keySet().size()>0 ) ||  passErrorMessage?exists>


<@ecdb.dialog_notification elementName="dialog-error" modal="true"/>

<div id="dialog-error" title="Error processing request">
  <div id="errors">
  <#if actionErrors?exists>
  <div id="action_errors">
    <ul>
    <#list actionErrors as error>
      <li>${error}</li>
    </#list>
    </ul>
  </div>
  </#if>
  <#if fieldErrors?exists>
  <div id="field_errors">
    <ul>
    <#list fieldErrors.keySet() as error>
      <li>Error with ${error}</li>
    </#list>
    </ul>
  </div>
  </#if>

  <#if passErrorMessage?exists>
  <p>${passErrorMessage}</p>
  </#if>
  </div>
</div>

</#if>