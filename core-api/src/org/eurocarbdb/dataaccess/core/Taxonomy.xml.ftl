<#if show_detail >
<taxonomy id="${x.taxonomyId?c}" ncbi-id="${x.ncbiId?c}" version="${x.version}">
    <name>${x.taxon}</name>
    <rank>${x.rank}</rank>
    <synonyms>
        <#list x.taxonomySynonyms as s >
        <synonym id="${s.taxonomySynonymId?c}" name="${s.synonym}" />
        </#list>
    </synonyms>
    <relations>
        <#if (x.parentTaxonomy?exists && ! x.isRoot())>
        <parents>
            ${xml.serialise( x.parentTaxonomy, false )}
        </parents>
        </#if>
        <#assign kids = x.childTaxonomies />
        <#if (kids?exists && kids?size > 0)>
        <children>
            <#list kids as k><#if ! k.isRoot()>${xml.serialise( k, false )}</#if></#list>
        </children>
        </#if>
    </relations>
</taxonomy>
<#else/>
<taxonomy id="${x.taxonomyId?c}" ncbi-id="${x.ncbiId?c}" version="${x.version}"/>
</#if>
