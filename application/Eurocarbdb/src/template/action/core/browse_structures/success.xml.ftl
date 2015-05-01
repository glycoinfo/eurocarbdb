<#include "/template/common/xml_header.ftl" />

<list from="${offset?c}" count="${resultsPerPage?c}" order="${index.name}" />

<#if ( results?exists && results?size > 0 )>
<#list results as gs >
${xmlio.serialise( gs )}
</#list>
<#else>
<!-- No sequences -->
</#if>

<#include "/template/common/xml_footer.ftl" />
