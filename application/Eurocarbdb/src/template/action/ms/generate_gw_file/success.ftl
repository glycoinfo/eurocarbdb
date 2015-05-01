<#assign title>Show or Download Uploadded Glycoworkbench file</#assign>
<#setting url_escaping_charset='ISO-8859-1'>

<#include "/template/common/header.ftl" />


<h1>Browse Acquisitions and their peaklists</h1>
<table class="ms_table">
  <thead>
    <tr class="ms_table_grey">
      <th><h3>Number of Scans</h3></th>
      <th><h3>Acquisition Id</h3></th>
      <th><h3>Acquisition File Name</h3></th>
      <th><h3>Date Entered</h3></th>
      <th><h3>Contributor Name</h3></th>     
       <th><h3>Show Annotation</h3></th>
    </tr>
  </thead>
  <tbody>
    <#assign col=1>
    <#assign i=0>
    <#list peakListsAndAquisitions as pa>
      <tr <#if col==1>class="ms_table_white"<#else>class="ms_table_lightgrey"</#if>>
      <td>${items[i][0]}</td>
      <td>${items[i][1]}</td>
      <td>${items[i][2]}</td>
      <td>${items[i][3]}</td>
      <td>${items[i][4]}</td>
      <td> <@ecdb.actionlink name="show_acquisition" params="acquisitionId = ${items[i][1]}&amp;dateEntered=${items[i][3]}&amp;contributorName= ${items[i][4]}">Show Scans</@></td>
      <!--
      <a href="show_acquisition.action?acquisitionId=${items[i][1]}&contributorName=${items[i][4]}&dateEntered=${items[i][3]?date}">Show Scans</a></td>
      !-->
      </tr> 
      <#assign col= -1*col>
      <#assign i=i+1>
    </#list>
  </tbody>
</table>

<#include "/template/common/footer.ftl" />