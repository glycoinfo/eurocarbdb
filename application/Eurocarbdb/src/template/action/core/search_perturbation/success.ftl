<#assign title>Perturbation fulltext search</#assign>
<#include "/template/common/header.ftl" />

<h1>${title}</h1>

<#-------------------------- if there are results ----------------------------->
<#assign count_results = matchingPerturbations.size() />
<#if ( count_results > 0 ) >
<p>
<table>
<tr>
    <th>Query</th>
    <td>List all taxonomies where ${ searchCriteria }.</td>
</tr>
<tr>
    <th>Number of results</th>
    <td>${ count_results }</td>
</tr>
</table>
<ul>
  <#list matchingPerturbations?sort as p >
  <li>
    <@ecdb.perturbation p=p />
    <#assign parents=p.allParentPerturbations>
    <#if (parents?exists && parents.size() > 0) >      
    <span class="breadcrumbs">
      <#list parents?reverse as pa> &lt; <@ecdb.perturbation p=pa /></#list>
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


