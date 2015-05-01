<#assign title></#assign>
<#include "/template/ui/user/header.ftl" />

<p>
Successfully created Experiment '${experiment.experimentName}' (experiment id = ${experiment.experimentId?c}) 
</p>

<p>
<#if experiment.experimentComments?exists >
    ${experiment.experimentComments}
<#else/>
    (no comments)
</#if>
</p>


<#include "/template/ui/user/footer.ftl" />
