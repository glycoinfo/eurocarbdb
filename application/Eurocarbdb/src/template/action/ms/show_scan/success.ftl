<#assign title>Show Scan Page</#assign>
<#include "/template/common/header.ftl" />
<#setting url_escaping_charset='ISO-8859-1'>

<#macro show_annotation ann >
<br/><br/>

<table class="table_top_header full_width">
  <thead>
    <tr>
      <th>Peak</th>
      <th>Intensity</th>
      <th>Persubstitution</th>
      <th>Reducing end</th>
      <th colspan="5"/>
      <th>Annotation</th>
      <th>Calc. mass</th>
      <th>Type</th>
      <th>Charge</th>
      <th>Charged ions</th>
    </tr>
  </thead>
	 <tbody>
	 <tr>
	  <td>${ann.peakLabeled.mzValue}</td> 
	  <td>${ann.peakLabeled.intensityValue}</td>
	  <td>${ann.persubstitution.name}</td>
	  <td>${ann.reducingEnd.name}</td>
	  <td colspan="5"/>
	  <td>
	      <img style="display: block;" src="get_sugar_image.action?download=false&amp;scale=0.5&amp;opaque=false&amp;outputType=png&amp;sequences=${ann.sequenceGws?url}" />
	  </td>
	  <td><#if ann.calculatedMass!=0>${ann.calculatedMass}</#if></td>
	  <td>
	      <#list ann.fragmentations as f>
	        <#if (f.fragmentType = "A" || f.fragmentType = "X")>
		  <sup>${f.cleavageOne},${f.cleavageTwo}</sup>${f.fragmentType} 
		<#else>
		  ${f.fragmentType} 
		</#if>
	      </#list>
	   </td>
	    <td>
	      <#assign charge = 0>
	      <#list ann.peakAnnotatedToIons as i>
	        <#assign charge = charge + i.ion.charge>
	      </#list>
	      <#if charge!=0>${charge}</#if>
	    </td>
	    <td>
	      <#list ann.peakAnnotatedToIons as i>
	        ${i.ion.charge} ${i.ion.ionType}<sup>+</sup> 
	      </#list>
	    </td>
	  </tr>
	</tbody>
</table>

</#macro>

<#macro show_peaklist scan>
<div id="peaklist_table" class="peaklist_table">
<table class="table_top_header full_width">
  <thead>
    <tr>
      <th>Date Entered</th>
      <th>Base Peak/mz</th>
      <th>Base Peak Intensity</th>
      <th>Low mz</th>
      <th>High mz</th>
    </tr>
  </thead>
  <tbody>
    <#list scan.peakLists as pl>
	<tr>
	  <td>${pl.dateEntered}</td> 
	  <td><#if pl.basePeakMz?exists>${pl.basePeakMz}</#if></td>
	  <td><#if pl.basePeakIntensity?exists>${pl.basePeakIntensity}</#if></td>
	  <td>${pl.lowMz}</td>
	  <td>${pl.highMz}</td>
	  <td colspan="5"/>
	</tr>  
    </#list>
  </tbody>
</table>
</div>
</#macro>

<#if scan?exists >

<!-- Display Data: -->

<h1>Overview of Scan no. ${scanId} </h1>

<a href="show_acquisition.action?acquisitionId=${scan.acquisition.acquisitionId}&contributorName=${contributorName}">Show parent acquisition</a>

<@ecdb.scan_detail_brief scan=scan />

<hr class="separator"/>

<!-- show labeled peaks -->
<h2>Peaklist</h2>
<@show_peaklist scan=scan />

<!-- show annotations -->
<h2>Annotation</h2>
<#list peakAnnotateds as ann >
<div>
  <@show_annotation ann=ann />
</div>
</#list>

<#else>

<#if annotation?exists >
<div id="annotation_list" class="annotation_list">
  <@show_annotation ann=annotation/>
</div>
</#if>

</#if>

<#include "/template/common/footer.ftl" />

