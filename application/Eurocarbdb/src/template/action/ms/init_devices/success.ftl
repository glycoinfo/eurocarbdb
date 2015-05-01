<#assign title>Device Creation</#assign>
<#include "/template/common/header.ftl" />

<h1>Devices Successfully Stored</h1>
    
<p>Added ${addedManufacturers} new manufacturers.</p>
<p>Added ${addedDevices} new devices.</p>
    
<h2>Manufacturers</h2>
<table class="table_top_header">
  <thead><tr><th>Name</th></tr></thead>
  <tbody>
    <#list manufacturers as m>
      <tr><td>${m.name}</td></tr>
    </#list>
  </tbody>
</table>

<h2>Devices</h2>
<table class="table_top_header">
  <thead>
    <tr><th>Model</th><th>Manufacturer</th></tr>
  </thead>
  <tbody>
    <#list devices as d>
      <tr><td>${d.model}</td><td>${d.manufacturer.name}</td></tr>
    </#list>
  </tbody>
</table>

<#include "/template/common/footer.ftl" />