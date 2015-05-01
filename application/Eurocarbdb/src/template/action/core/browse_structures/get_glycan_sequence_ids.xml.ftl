<#include "/template/common/xml_header.ftl" />
<#assign ids=action.getAllGlycanStructureIds() />

<#if ( ids?exists && ids?size > 0 )>
<list from="0" count="${ids?size?c}" order="date" />
<#list ids as id >
    <glycan id="${id?c}" />
</#list>
<#else>
    <list from="0" count="0" order="date" />
    <!-- No sequences -->
</#if>

<#include "/template/common/xml_footer.ftl" />
