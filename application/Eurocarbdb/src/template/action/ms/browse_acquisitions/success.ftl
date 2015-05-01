<#assign title>Acquisition Browsing Page</#assign>
<#setting url_escaping_charset='ISO-8859-1'>

<#include "/template/ui/user/header.ftl" />


<h1>Browse Acquisitions</h1>

<table class="ms_table">
  <thead>
    <tr class="ms_table_grey">
      <th><h3>Acquisition id</h3></th>
      <th><h3>Experiment id</h3></th>
      <th><h3>Contributor</h3></th>
      <th><h3>Device</h3></th>
      <th><h3>File</h3></th>
      <th><h3>File type</h3></th>
      <th><h3>Time start</h3></th>
      <th><h3>Time end</h3></th>
    </tr>
  </thead>
  <tbody>
    <#assign col=1>
    <#list acquisitions as a>
      <tr <#if col==1>class="ms_table_white"<#else>class="ms_table_lightgrey"</#if>>
        <td><a href="show_acquisition.action?acquisitionId=${a.acquisitionId}">${a.acquisitionId}</a></td>
	<td>
	  <#if a.evidence?exists & a.evidence.experiment?exists >
	    <a href="show_experiment.action?experimentId=${a.evidence.experiment.experimentId}">${a.evidence.experiment.experimentId}
	  </#if>
	</td>
	<td>${a.contributor.contributorName}</td>
	<td>${a.device.manufacturer.name} ${a.device.model}</td>
	<td>${a.filename}</td>
	<td>${a.filetype}</td>
	<td>${a.timeStart?datetime}</td>
	<td>${a.timeEnd?datetime}</td>
      </tr> 
      <#assign col= -1*col>
    </#list>
  </tbody>
</table>


<#include "/template/common/footer.ftl" />