<#assign title>Prelimary</#assign>
<#include "/template/common/header.ftl" />

<h1>Preliminary Data</h1>
<table class="table_top_header full_width">

<tr><th>Name</th><th>Image</th><th>GU</th><th>DB GU</th>
      <#list displayPrelimList as p>
<tr><td>${p[4]}</td><td><img src="get_sugar_image.action?download=true&scale=0.4&outputType=png&glycanSequenceId=${p[5]?c}"/></td><td>${p[2]}</td><td>${p[3]}</td><#if p_has_next></#if>
      </#list>
</table>


<#include "/template/common/footer.ftl" />
