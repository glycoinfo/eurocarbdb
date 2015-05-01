<#assign title>Taxonomy search</#assign>
<#include "/template/common/header.ftl" />

<h1>${title}</h1>

<#-------------------------- if there are results ----------------------------->
<#assign count_results = matchingTaxonomies.size() />
<#if ( count_results > 0 ) >
<p>
<table>
<tr>
    <th>Query</th>
    <td>${ action.searchCriteria }.</td>
</tr>
<tr>
    <th>Number of results</th>
    <td>${ count_results }</td>
</tr>
</table>
<ul>
  <#list matchingTaxonomies?sort as t >
  <li>
    <@ecdb.taxonomy t=t />
    <#assign parents=t.getParentTaxonomySubset() />
    <#if (parents?exists && parents.size() > 0) >
    <span class="breadcrumbs">
      <#list parents?reverse as p > &lt; <@ecdb.taxonomy t=p /></#list>
    </span>
    </#if>
  </li>
  </#list>	
</ul>
</p>

<#else/><#----------------- if there are no results --------------------------->
<p>
    There are no taxonomies that match the given criteria.
</p>
</#if>

<#include "/template/common/footer.ftl" />


