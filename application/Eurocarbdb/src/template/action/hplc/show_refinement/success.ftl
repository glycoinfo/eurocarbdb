<#assign title>autoGu</#assign>
<#include "/template/ui/user/header.ftl" />

<div class="hplc_create_form">
<h1>Refined Assignmentd</h1>
<p></p>
</div>

<div class="hplc_form">
<p>Refined list of potential structures for the undigested sample</p>
<p>Data for profile Id: ${profileId}</p>


<div class="hplc_table_data">
<table><tr><th>Glycan Id</th><th>Name</th><th>Structure</th><th>GU</th><th>DB GU</th><th>Area</th>
      <#list displayRefinement as p>
<tr><td>${p.glycanId?c}</td><td>${p.nameAbbreviation}</td><td align="center"><img src="/ecdb/images/hplc/${p.glycanId?c}.png"></td><td>${p.gu}</td><td>${p.dbGu}</td><td>${p.PeakArea}</td><#if p_has_next></#if>	
      </#list>
</table>
</div>

<#include "/template/common/footer.ftl" />

