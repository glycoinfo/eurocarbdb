<#assign title>Annotation Display Page</#assign>
<#setting url_escaping_charset='ISO-8859-1'>

<#include "/template/common/header.ftl" />

<h1>Annotation Page</h1>

<#assign annotation = scan.firstAnnotation/>

<@ww.form theme="simple">

<!-- 	Display Structure: -->

<h2>Parent structure:</h2>

<table class="table_top_header">
  <thead>
    <tr><th>Selected structure</th></tr>
  </thead>
  <tbody>
    <tr>
      <td>
	<img style="display: block;" src="get_sugar_image.action?download=false&scale=1&opaque=false&inputType=gws&outputType=png&sequences=${annotation.parentStructure.sequenceGWS?url}" />
      </td>
    </tr>
  </tbody>
</table>

<table class="table_form">
  <tr><th>Persubstitution:</th><td>${annotation.persubstitution.name}</td></tr>
  <tr><th>Reducing end:</th><td>${annotation.reducingEnd.name}</td></tr>
</table>

<!-- 	Display Annotation: -->

<h2>Annotations:</h2>

<@ww.submit value="Back" name="submitAction" />
<@ww.submit value="Delete selected" name="submitAction" />
<@ww.submit value="Store annotations" name="submitAction" />

<br>
<br>

<table class="table_top_header full_width">
  <thead>
    <tr>
      <th>peak</th>
      <th>Intensity</th>
      <th>select</th>
      <th>fragment</th>
      <th>calc. mass</th>
      <th>type</th>
      <th>charge</th>
      <th>charged ions</th>
    </tr>
  </thead>
  <tbody>
    <#assign peak_id=0>
    <#assign col=1>
    <#list annotation.peakAnnotationsOrdered as pan>
      <#if pan.peakAnnotateds?size==0 >
	<tr>
	  <td>${pan.peakLabeled.mzValue}</td> 
	  <td>${pan.peakLabeled.intensityValue}</td>
	  <td colspan="6"></td>
	</tr>
      <#else>
	<#assign ann_id=0>
	<#list pan.peakAnnotateds as q>
	  <tr>
	    <#if ann_id==0>
	      <td rowspan="${pan.peakAnnotateds.size()?c}">${pan.peakLabeled.mzValue}</td> 
	      <td rowspan="${pan.peakAnnotateds.size()?c}">${pan.peakLabeled.intensityValue}</td>
	    </#if>
	    <td>
	      <#if q.calculatedMass!=0><input type="checkbox" name="selectedAnnotations" value="${peak_id}_${ann_id}" /></#if>
	    </td>
	    <td>
	      <img style="display: block;" src="get_sugar_image.action?download=false&scale=0.5&opaque=false&outputType=png&sequences=${q.sequenceGWS?url}" />
	    </td>
	    <td><#if q.calculatedMass!=0>${q.calculatedMass}</#if></td>
	    <td>
	      <#list q.fragmentations as f>
	        <#if (f.fragmentType = "A" || f.fragmentType = "X")>
		  <sup>${f.cleavageOne},${f.cleavageTwo}</sup>${f.fragmentType} 
		<#else>
		  ${f.fragmentType} 
		</#if>
	      </#list>
	    </td>
	    <td>
	      <#assign charge = 0>
	      <#list q.peakAnnotatedToIons as i>
	        <#assign charge = charge + i.charge>
	      </#list>
	      <#if charge!=0>${charge}</#if>
	    </td>
	    <td>
	      <#list q.peakAnnotatedToIons as i>
	        ${i.charge} ${i.ion.ionType}<sup>+</sup> 
	      </#list>
	    </td>
	    <#assign ann_id=ann_id+1>
	  </tr>
	</#list>
      </#if>
      <#assign peak_id=peak_id+1>
    <#assign col= -1*col>
    </#list>
  </tbody>
</table>

</@ww.form>


<#include "/template/common/footer.ftl" />