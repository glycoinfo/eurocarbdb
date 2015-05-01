<#if show_detail>
<reference id="${x.referenceId?c}" type="${x.referenceType}" ref-name="${x.externalReferenceName}" ref-id="${x.externalReferenceId!''}">
    ${xml.serialise(x.contributor, false)}
</reference>
<#else>
<reference id="${x.referenceId?c}" type="${x.referenceType}" ref-name="${x.externalReferenceName}" ref-id="${x.externalReferenceId!''}" />
</#if>
