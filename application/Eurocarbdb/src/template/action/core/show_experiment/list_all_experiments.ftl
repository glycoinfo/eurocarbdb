<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<#assign title>Experiment list</#assign>
<#include "/template/common/header.ftl" />

<h1>${title}</h1>

<#assign experiments=action.getAllExperiments() />
<p>
<#if ( experiments?exists && experiments?size > 0 ) >
<table>
    <tr>
        <th>Experiment name</th>
        <th>Date entered</th>
    </tr>
    <#list experiments as e >
    <tr>
        <td><@ecdb.experiment e=e /></td>
        <td>${e.dateEntered?string.long}</td>
    </tr>
    </#list>
</table>
<#else/>
    (You have no experiments - <a href="create_experiment.action">create one</a>).
</#if>
</p>

<#include "/template/common/footer.ftl" />
