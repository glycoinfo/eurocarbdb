<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<#assign title>Experiment detail</#assign>
<#include "/template/common/header.ftl" />

<h1>${title}</h1>


<table>
    <tr>
        <th>Experiment name</th>
        <td>${experiment.experimentName}</td>
    </tr>
    <tr>
        <th>Experiment comments</th>
        <td>
        <#if experiment.experimentComments?exists >
            ${experiment.experimentComments}
        <#else/>
            (no comments)
        </#if>
        </td>
    <tr>
        <th>Date entered</th>
        <td>${experiment.dateEntered?string.long}</td>
    </tr>
    <tr>
        <th>Samples used</th>
        <td><#assign samples=experiment.biologicalContexts />
            <#if ( samples?exists && samples?size > 0 ) >
            <#list samples as bc >
            <@ecdb.biological_context bc=bc /><br/>
            </#list>
            <#else/>
            <em>none</em><br/>    
            </#if>
            <a href="create_biological_context.action?experimentId=${experiment.id}">add biological context</a>
        </td>
    </tr>
    <tr>
        <th>Experiment steps</th>
        <td>
        <#assign steps = action.getExperimentSteps() />
        <#if ( steps?exists && steps?size > 0 ) >
            <#list steps?sort_by("dateEntered") as s >
            ${s.dateEntered?date} - <a href="show_experiment_step.action?experimentStepId=${s.experimentStepId}">${s.technique.techniqueAbbrev}</a><br/>    
            </#list>
        <#else/>
            <em>none</em> 
        </#if>
            <br/>
            (<a href="create_experiment_step.action?experimentId=${experiment.experimentId?c}"
                >add experiment step</a>)
        </td>
    </tr>
</table>

<#include "/template/common/footer.ftl" />
