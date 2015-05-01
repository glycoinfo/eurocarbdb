<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<#assign title>Select scan for annotation</#assign>
<#setting url_escaping_charset='ISO-8859-1'>

<#include "/template/common/header.ftl" />

<h1>${title}</h1>

<@ww.form name="scanform" action="edit_scan">
<@ww.hidden id="scanId" name="scanId" value="" />
<#if (acquisition.scans?size>0)>
  
<table class="table_top_header">
  <thead>
    <tr>
      <th></th>
      <th>Id</th>
      <th>MS Exp.</th>
      <th>Polarity</th>
      <th>Deisotoped</th>
      <th>Deconvoluted</th>      
      <th>Annotated</th>      
      <th>Base Peak m/z</th>
      <th>Base Peak Int.</th>
      <th>Start m/z</th>
      <th>End m/z</th>
      <th>Low m/z</th>
      <th>High m/z</th>
      <th>Quality</th>
    </tr>
  </thead>
  <tbody>
    <#assign first=1/>
    <#list acquisition.scans as s>
      <tr>
	  <td><input type="button" value="Annotate scan" onclick="$('scanId').value=${s.scanId}; log($('scanId').value); document.forms['scanform'].submit();"/></td>
	  <#assign first=0>	  
	<td>${s.scanId}</td>
	<td>${s.msExponent}</td>
	<td>${s.polarity?string('positive','negative')}</td>
	<td>${s.deisotoped?string('yes','no')}</td>
	<td>${s.chargeDeconvoluted?string('yes','no')}</td>
	<td><#if (s.annotations?exists & s.annotations?size>0)>yes<#else>no</#if></td>
	<td>${s.basePeakMz}</td>
	<td>${s.basePeakIntensity}</td>
	<td>${s.startMz}</td>
	<td>${s.endMz}</td>
	<td>${s.lowMz}</td>
	<td>${s.highMz}</td>
	<td>${s.contributorQuality}</td>
      </tr> 
    </#list>
  </tbody>
</table>
<#else>
No scans.
</#if>

<@ww.submit value="Back to acquisition" name="submitAction"/>
<@ww.submit value="Add annotations to scan" name="submitAction"/>

</@ww.form>



<#include "/template/common/footer.ftl" />