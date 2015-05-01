<#assign title>Disease fulltext search</#assign>
<#include "/template/common/header.ftl" />
<#import "/template/lib/Eurocarb.lib.ftl" as eurocarb />

<h1>${title}</h1>

<#-------------------------- if there are results ----------------------------->
<#assign count_results = matchingDiseases.size() />
<#if ( count_results > 0 ) >
<p>
<table>
<tr>
    <th>Query</th>
    <td>List all diseases where ${ searchCriteria }.</td>
</tr>
<tr>
    <th>Number of results</th>
    <td>${ count_results }</td>
</tr>
</table>
<ul>
  <#list matchingDiseases?sort as d >
  <li>
    <@ecdb.disease d=d />
    <#assign parents=d.allParentDiseases>
    <#if (parents?exists && parents.size() > 0) >      
    <span class="breadcrumbs">
      <#list parents?reverse as p> &lt; <@ecdb.disease d=p /></#list>
    </span>
    </#if>   
  </li>
  </#list>	
</ul>
</p>

<#else/><#----------------- if there are no results --------------------------->
<p>
    There are no diseases that match the given criteria.
</p>
</#if>

<#include "/template/common/footer.ftl" />


