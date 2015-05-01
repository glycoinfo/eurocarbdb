<context id="${x.biologicalContextId?c}" version="${x.version}">
    <#list x.biologicalContextContributors as c>
      ${xml.serialise( c.contributor, show_detail)}
    </#list>
    ${xml.serialise( x.taxonomy, show_detail )}
    ${xml.serialise( x.tissueTaxonomy, show_detail )}
    <#------------------------------- diseases ----------------------------->
    <#if (x.diseaseContexts?exists && x.diseaseContexts?size > 0)>
    <disease-associations>
        <#list x.diseaseContexts as d >
        ${xml.serialise( d.disease, show_detail )}
        </#list>
    </disease-associations>
    </#if>
    <#----------------------------- perturbations --------------------------->
    <#if (x.diseaseContexts?exists && x.diseaseContexts?size > 0)>
    <perturbation-associations>
        <#list x.perturbationContexts as p >
        ${xml.serialise( p.perturbation, show_detail )}
        </#list>
    </perturbation-associations>
    </#if>
</context>

