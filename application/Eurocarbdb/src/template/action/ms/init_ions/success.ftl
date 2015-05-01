<#assign title>Ions Creation</#assign>
<#include "/template/common/header.ftl" />

<h1>Ions Successfully Stored</h1>

<p>Added ${addedIons} new ions.</p>

<h2>Ions</h2>
<table class="table_top_header">
  <thead>
    <tr><th>Type</th></tr>
  </thead>
  <tbody>
    <#list ions as i>
      <tr><td>${i.ionType}</td></tr>
    </#list>
  </tbody>
</table>

<#include "/template/common/footer.ftl" />