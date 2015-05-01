
<!-- main content ends here -->
</div>	


<div id="refine"><#--========================================================== begin refine div ===
this is the right-floated 'contextual menu' column. stuff can be added to it from other templates 
by appending/assigning to the freemarker variable 'context_menu', which will appear inside a 
div inside this (refine) div.
-->
           
<#if (navigations?exists && (navigations?size > 0)) >
<!-- start action-specific navigation items -->
<@ecdb.context_box >
<div id="navigations"><#-- begin navigations div -->
<#list navigations as n>
<a href="${n.url?url}">${n.label}</a>
</#list>
</div><#-- end navigations div -->
</@ecdb.context_box >
<!-- end action-specific navigation items -->
</#if><#-- end if navigations -->


<@ecdb.sugar_notation_widget />


<#-- Add user actions box if user is logged in -->
<#if currentContributor?exists && currentContributor.isLoggedIn() && ! currentContributor.isGuest() >
<@ecdb.context_box id="user_options_box" title="User options" prepend=true >
<a class="accountdetails" href="${base}/update_contributor_details.action">Account settings for  ${currentContributor.contributorName}</a> 
<#--<a href="javascript:alert('Future feature placeholder')">Add to bookmarks</a>-->
<#--<a href="javascript:alert('Future feature placeholder')">Submit corrections for this entry</a>-->
</@ecdb.context_box>
</#if>

<#-- add a export xml link if action supports it -->
<#attempt>
<#if action.canGenerateXml() >
<@ecdb.context_box title="Export">
    <a href="${request.getRequestURL()}<#if (request.getQueryString()?exists) >?${request.getQueryString()}&<#else>?</#if>output=xml" target="_blank">View as XML</a>
</@ecdb.context_box>
</#if>
<#recover>
<!-- this action does not support generation of results as XML  -->
</#attempt>

<#-- Report problems/suggestions box -->
<@ecdb.context_box title="Contact us">
<p>
    Have a suggestion? Want to help?
</p>
    <a href="http://groups.google.com/group/eurocarb-users/topics"
        title="mailing group for scientific users of the software (requires google account)"
        target="_blank"
        >eurocarb-users@googlegroups.com</a> 
    <a href="http://groups.google.com/group/eurocarb-devel/topics"
        title="mailing group for developers and technically inclined users (requires google account)"
        target="_blank"
        >eurocarb-devel@googlegroups.com</a> 
    <hr/>
<p>
    See a problem? 
</p>
    <@ecdb.create_issue_link 
        text="report a scientific issue" 
        issue_template="Scientific issue" 
        title="Report a scientific problem directly (requires google account)" 
    >Please describe your issue, including IDs where possible:</@>
    <@ecdb.create_issue_link 
        text="report a technical problem" 
        title="Report a technical problem directly (requires google account)"        
    >Please describe the problem you're having:</@>
</@ecdb.context_box>

<#--=========== context boxes ================
this call dumps all the context boxes accumulated so far. 
context_box'es added after this point will not be shown 
-->
<@ecdb.context_menu_inline />

</div><#--======================================================================= end refine div -->


<#--========================= session debugging =========================
<div id="session">
<h3>HTTP sessions data</h3>
<#if ( session?size > 0 )>
<ul>
  <#list session.getAttributeNames() as key >
    <#assign key_value = session.getAttribute(key) />
    <#assign key_name = key?replace("webwork.ScopeInterceptor:class org.eurocarbdb.action.", "" ) />
    <li>${key_name} = ${ key_value!"null"?string }</li>
  </#list>
</ul>
<#else/>
<p style="font-size: x-small;">
  <em>No data in session</em>
</p>
</#if>
</div 
-->

<div id="footer">
  <p>EuroCarbDB is a Research Infrastructure Design Study Funded
  by the 6th Research Framework Program of the European Union
  (Contract: RIDS Contract number 011952)
  </p>	
</div>

</body>
</html>


