<#assign title>Show or Download Uploadded Glycoworkbench file</#assign>
<#setting url_escaping_charset='ISO-8859-1'>

<#include "/template/common/header.ftl" />


<h1>Browse Annotations of Scan. ${scanId}</h1>
<table class="ms_table">
  <thead>
    <tr class="ms_table_grey">
      <th><h3>Number of Annotaions</h3></th>
      <th><h3>Date Entered</h3></th>
      <th><h3>Contributor Name</h3></th>     
       <th><h3>Show</h3></th>
       <th><h3>Download</h3></th>
    </tr>
  </thead>
  <tbody>
    <#assign col=1>
    <#assign i=0>
    <#list scanAnnotations as sa>
      <tr <#if col==1>class="ms_table_white"<#else>class="ms_table_lightgrey"</#if>>
      <td>${items[i][0]}</td>
      <td>${items[i][1]}</td>
      <td>${items[i][2]}</td>
      <td><a href="show_scan.action?scanId=${scanId} & dateEntered=${items[i][1]} & contributorName=${items[i][2]}">Show</a></td>
      <td><a href="show_scan.action?scanId=${scanId} & dateEntered=${items[i][1]} & contributorName=${items[i][2]}">Download</a></td>
      </tr> 
      <#assign col= -1*col>
      <#assign i=i+1>
    </#list>
  </tbody>
</table>

<#include "/template/common/footer.ftl" />