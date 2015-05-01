<#assign title>Tissue Taxonomy search</#assign>
<#include "/template/common/header.ftl" />

<h1>${title}</h1>

<#-------------------------- if there are results ----------------------------->
<#assign count_results = matchingTissueTaxonomies.size() />
<#if ( count_results > 0 ) >
<p>
<table>
<tr>
    <th>Query</th>
    <td>List all tissue taxonomies where ${ searchCriteria }.</td>
</tr>
<tr>
    <th>Number of results</th>
    <td>${ count_results }</td>
</tr>
</table>
<ul>
  <#list matchingTissueTaxonomies?sort as t >
  <li>
    <@ecdb.tissue t=t />
    <#assign parents=t.allParentTissueTaxonomies>
    <#if (parents?exists && parents.size() > 0) >      
    <span class="breadcrumbs">
      <#list parents?reverse as p> &lt; <@ecdb.tissue t=p show_unknown=false /></#list>
    </span>
    </#if>
  </li>
</#list>	
</ul>
</p>

<#else/><#----------------- if there are no results --------------------------->
<p>
    There are no tissue taxonomies that match the given criteria.
</p>
</#if>

<#include "/template/common/footer.ftl" />


