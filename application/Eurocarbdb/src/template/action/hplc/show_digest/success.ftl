<#assign title>Detector Page</#assign>
<#include "/template/ui/user/header.ftl" />

<h1>HPLC Detector</h1>

<p>Results presented here</p>

<table class=".ms_table_grey"><tr><th>Peak<th>Area<th>GU<th>DB GU<th>Name<th>Image Id
      <#list peaks as p>
<tr><td>${p.assignedPeak}<td>${p.peakArea}<td>${p.gu}<td>${p.dbGu}<td>${p.nameAbbreviation}<td>${p.glycanId}<#if p_has_next></#if>	
      </#list>
</table>

<#include "/template/common/footer.ftl" />

