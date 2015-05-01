<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />

<#assign title>Activation Requests</#assign>

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
        </tr>

     <#list contributors as contributor>
        <tr>
            <td><p>${(contributor.contributorName)?html}</td>
            <td><p>${(contributor.fullName!" ")?html}</td>
            <td><p>${(contributor.email!" ")?html}</td>
            <td><p>${(contributor.institution!" ")?html}</td>
            <td>
                <@ww.form theme="simple" method="post" action="${base}/activate.action">
                    <@ww.hidden name="loginName" value="${contributor.contributorName}"/>
                    <@ww.submit value="activate" />
                </@ww.form>
            </td>
            <td>
                <@ww.form theme="simple" method="post" action="${base}/deactivate.action">
                    <@ww.hidden name="loginName" value="${contributor.contributorName}"/>
                    <@ww.submit title="deactivates and removes the user." value="remove" />
                </@ww.form>
            </td>
        </tr>
    </#list>
</table>
</#if>
</#if>
<#include "/template/common/footer.ftl" />
