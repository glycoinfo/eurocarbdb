<#assign title>Owned Acquisition Browsing Page</#assign>
<#setting url_escaping_charset='ISO-8859-1'>

<#include "/template/common/header.ftl" />


<h1>Browse Owned and Others Acquisitions</h1>
<h2> Owned Acquisitions </h2>
<table class="ms_table">
  <thead>
    <tr class="ms_table_grey">
      <th><h3>Acquisition id</h3></th>
       <th><h3>File</h3></th>
      <th><h3>Contributor</h3></th>     
    </tr>
  </thead>
  <tbody>
    <#assign col=1>
    <#list ownedAcquisitions as o>
      <tr <#if col==1>class="ms_table_white"<#else>class="ms_table_lightgrey"</#if>>
        <td><a href="uploadGWFile!input.action?acquisitionId=${o.acquisitionId}">${o.acquisitionId}</a></td>
	<td>${o.filename}</td>
	<td>${o.contributor.contributorName}</td>
      </tr> 
      <#assign col= -1*col>
    </#list>
  </tbody>
</table>
<h2> Others Acquisitions </h2>
<table class="ms_table">
  <thead>
    <tr class="ms_table_grey">
      <th><h3>Acquisition id</h3></th>
       <th><h3>File</h3></th>
      <th><h3>Contributor</h3></th>     
    </tr>
  </thead>
  <tbody>
    <#assign col=1>
    <#list othersAcquisitions as r>
      <tr <#if col==1>class="ms_table_white"<#else>class="ms_table_lightgrey"</#if>>
        <td><a href="uploadGWFile!input.action?acquisitionId=${r.acquisitionId}">${r.acquisitionId}</a></td>
	<td>${r.filename}</td>
	<td>${r.contributor.contributorName}</td>
      </tr> 
      <#assign col= -1*col>
    </#list>
  </tbody>
</table>



<#include "/template/common/footer.ftl" />