<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />

<#assign title>Unblock Contributors</#assign>

<#include "/template/common/header.ftl" />

<h1>${title}</h1>
<#if ( message?length > 0 )>
<div class="error_message">
  ${message}
</div>
</#if>
<#if contributors??>
<#if contributors?size!=0 >
<table>
        <tr>
            <th>Login Name</th>
            <th>Full Name</th>
            <th>Email</th>
            <th>Institution</th>
            <th>Last Login</th>
        </tr>

     <#list contributors as contributor>
        <tr>
            <td><p>${(contributor.contributorName)?html}</td>
            <td><p>${(contributor.fullName!" ")?html}</td>
            <td><p>${(contributor.email!" ")?html}</td>
            <td><p>${(contributor.institution!" ")?html}</td>
            <td><p>${(contributor.lastLogin!" ")?html}</td>
            <td>
                <@ww.form theme="simple" method="post" action="${base}/unblock.action">
                    <@ww.hidden name="loginName" value="${contributor.contributorName}"/>
                    <@ww.submit value="unblock" />
                </@ww.form>
            </td>
        </tr>
    </#list>
</table>
</#if>
</#if>
<#include "/template/common/footer.ftl" />
