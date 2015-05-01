<#assign title>Details of Glycan sequence - Reference association</#assign>
<#include "/template/common/header.ftl" />

<h1>${title}</h1>

<#assign gs = detailedObject.glycanSequence />
<#assign r = detailedObject.reference />
<#assign c = detailedObject.contributor />
<p>
    Association added by <@ecdb.contributor c=c /> on ${detailedObject.dateEntered?datetime}
</p>

<table class="table_left_header"> 
<tr>
    <th>
        Glycan Sequence
        <br/><span style="font-size: x-small; font-weight: normal;">(<@ecdb.detail_link object=gs text="Sequence ID ${gs.glycanSequenceId?c}" />)</span>
    </th>
    <td>
        <@ecdb.linked_sugar_image id=gs.glycanSequenceId />
    </td>
</tr>
<tr>
    <th>
        Reference
        <br/><span style="font-size: x-small; font-weight: normal;">(<a href="show_reference.action?referenceId=${r.referenceId?c}">Reference ID ${r.referenceId?c}</a>)</span>
    </th>
    <td>
        <@ecdb.reference ref=r />
    </td>
</tr>
<#assign gsr_set=r.glycanSequenceReferences /> 
<#if ( gsr_set?size > 1 )>
<tr>
    <th>
        Other structures associated to this reference
    </th>
    <td>
    <#list gsr_set as gsr>
        <#if gsr != detailedObject>
        <@ecdb.linked_sugar_image id=gsr.glycanSequence.glycanSequenceId scale="0.25" />
        </#if>
    </#list>
    </td>
</tr>
</#if>
</table>
    
<#include "/template/common/footer.ftl" />
