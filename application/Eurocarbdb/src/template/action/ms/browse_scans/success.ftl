<#assign title>Scan Browsing Page</#assign>
<#setting url_escaping_charset='ISO-8859-1'>

<#include "/template/ui/user/header.ftl" />


<h1>Browse Scans</h1>

<@ww.form>
<@ww.submit value ="Add new scan" name="submitAction"/>
</@ww.form>


<table class="ms_table">
  <thead>
    <tr class="ms_table_grey">
      <th><h3>Scan ID</h3></th>
      <th><h3>Device</h3></th>
      <th><h3>MS Exp.</h3></th>
      <th><h3>Polarity</h3></th>
      <th><h3>Deisotoped</h3></th>
      <th><h3>Deconvoluted</h3></th>      
      <th><h3>Base Peak m/z</h3></th>
      <th><h3>Base Peak Int.</h3></th>
      <th><h3>Start m/z</h3></th>
      <th><h3>End m/z</h3></th>
      <th><h3>Low m/z</h3></th>
      <th><h3>High m/z</h3></th>
      <th><h3>Quality</h3></th>
    </tr>
  </thead>
  <tbody>
    <#if scans?exists >
      <#assign col=1>
      <#list scans as s>
        <tr <#if col==1>class="ms_table_white"<#else>class="ms_table_lightgrey"</#if>>
	  <td><a href="workflow_ms_show.action?scanId=${s.scanId}">${s.scanId}</a></td>
	  <td>${s.device.manufacturer.name} ${s.device.model}</td>
	  <td>${s.msExponent}</td>
	  <td>${s.polarity?string('positive','negative')}</td>
	  <td>${s.deisotoped?string('yes','no')}</td>
	  <td>${s.chargeDeconvoluted?string('yes','no')}</td>
	  <td>${s.basePeakMz}</td>
	  <td>${s.basePeakIntensity}</td>
	  <td>${s.startMz}</td>
	  <td>${s.endMz}</td>
	  <td>${s.lowMz}</td>
	  <td>${s.highMz}</td>
	  <td>${s.contributorQuality}</td>
	</tr> 
	<#assign col= -1*col>
      </#list>
    </#if>
  </tbody>
</table>

<#include "/template/common/footer.ftl" />