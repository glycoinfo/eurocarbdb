<#if show_detail >
<perturbation id="${x.perturbationId?c}" mesh-id="${x.meshId}" version="${x.version}">
    <name>${x.perturbationName}</name>
    <description>${x.description!''}</description>
    <relations>
        <#if x.parentPerturbation?exists >
        <parents>
            ${xml.serialise( x.parentPerturbation, false )}
        </parents>
        </#if>
        <#assign kids = x.childPerturbations /><#t>
        <#if (kids?exists && kids?size > 0)>
        <children>
            <#list kids as k><#if ! k.isRoot() >${xml.serialise( k, false )}</#if></#list>
        </children>
        </#if>
    </relations>
</perturbation>
<#else>
<perturbation id="${x.perturbationId?c}" mesh-id="${x.meshId}" version="${x.version}" />
</#if>

