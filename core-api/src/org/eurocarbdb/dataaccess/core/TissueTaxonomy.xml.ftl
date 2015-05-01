<#if show_detail >
<tissue id="${x.tissueTaxonomyId?c}" mesh-id="${x.meshId}" version="${x.version}">
    <name>${x.tissueTaxon}</name>
    <description>${x.description}</description>
    <synonyms>
        <#list x.tissueTaxonomySynonyms as s >
        <synonym id="${s.tissueTaxonomySynonymId?c}" name="${s.synonym}" />
        </#list>
    </synonyms>
    <relations>
        <#if (x.parentTaxonomy?exists && ! x.isRoot())>
        <parents>
            ${xml.serialise( x.parentTissueTaxonomy, false )}
        </parents>
        </#if>
        <#assign kids = x.childTissueTaxonomies />
        <#if (kids?exists && kids?size > 0)>
        <children>
            <#list kids as k><#if ! k.isRoot()>${xml.serialise( k, false )}</#if></#list>
        </children>
        </#if>
    </relations>
</tissue>
<#else/>
<tissue id="${x.tissueTaxonomyId?c}" mesh-id="${x.meshId}" version="${x.version}"/>
</#if>

