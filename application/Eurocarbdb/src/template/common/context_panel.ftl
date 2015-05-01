
<#if ( action?exists && action.allActions?exists ) >

<h2>All actions</h2>
<ul>
  <#list action.getAllActions() as action_name >
    <#if ( action.currentActionName != action_name ) >
    <li><a href="${action_name}.action">${action_name}</a></li>
    <#else />
    <li><strong><em>${action_name}</em></strong></li>
    </#if>
  </#list>
</ul>
<hr/>
<#else/>
    <em>(Template does not have an action associated with it)</em>
    <hr/>
</#if><#-- end if action?exists -->

<h2>Session data</h2>

<#if ( session?size > 0 )>
<ul>
<#list session.getAttributeNames() as key >
    <li>${ key?replace("webwork.ScopeInterceptor:class org.eurocarbdb.action.", "" ) }<#-- = ${ session[key]?string } --></li>
</#list>
</ul>
<#--
<ul>
<#list session.getValueNames() as key >
    <li>${ key }</li>
</#list>
</ul>
-->

<#else/>
    <p style="font-size: x-small;">
        <em>No data in session</em>
    </p>
</#if><#-- end if session?size -->


