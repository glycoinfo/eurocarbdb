<#assign title>autoGu</#assign>
<#include "/template/ui/user/header.ftl" />

<form method="post" enctype="multipart/form-data">
  <@ww.textfield label="Id" name="profileId"/>      
  <input type="submit" name="submitAction" value="Upload"/>
</form>

<p>Proceed onto <a href="digestion_analysis.action?digestId=1&profileId=${profileId}"> digest analysis</a></p>

<table><tr><th>Glycan Id</th><th>Name</th><th>GU</th><th>DB GU</th>
      <#list prelimarytwo as p>
<tr><td>${p.glycanId?c}</td><td>${p.nameAbbreviation}<td>${p.gu}</td><td>${p.dbGu}</td><#if p_has_next></#if>	
      </#list>
</table>

<#include "/template/common/footer.ftl" />

