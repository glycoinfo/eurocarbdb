<#if show_detail >
<disease id="${x.diseaseId?c}" mesh-id="${x.meshId}" version="${x.version}">
    <name>${x.diseaseName}</name>
    <description>${x.description!''}</description>
    <relations>
        <#if x.parentDisease?exists >
        <parents>
            ${xml.serialise( x.parentDisease, false )}
        </parents>
        </#if>
        <#assign kids = x.childDiseases /><#t>
        <#if (kids?exists && kids?size > 0)>
        <children>
            <#list kids as k><#if ! k.isRoot() >${xml.serialise( k, false )}</#if></#list>
        </children>
        </#if>
    </relations>
</disease>
<#else>
<disease id="${x.diseaseId?c}" mesh-id="${x.meshId}" version="${x.version}" />
</#if>

