<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />

<#include "/template/common/header.ftl" />

<h1>${title}</h1>
<#if ( message?length > 0 )>
<div class="error_message" style="text-align:center">
  ${message}
</div>
</#if>
<#assign redirected_from>${base}/showRequests.action</#assign>
<#if title="Activation Status" || title="Deactivation Status" >
<#assign redirected_from>${base}/showRequests.action</#assign>
<#elseif title="Promotion Status">
<#assign redirected_from>${base}/showPromotableContributors.action</#assign>
<#elseif title="Demotion Status">
<#assign redirected_from>${base}/showDemotableContributors.action</#assign>
<#elseif title="Block Status">
<#assign redirected_from>${base}/showUnblockedContributors.action</#assign>
<#elseif title="Unblock Status">
<#assign redirected_from>${base}/showBlockedContributors.action</#assign>
</#if>
<@ww.form theme="simple" method="post" action="${redirected_from}">
    <table class="table_form">
      <tr><td colspan="2" align="center"><@ww.submit value="Back"/> </td></tr>
    </table>
</@ww.form>
<#include "/template/common/footer.ftl" />
