<#assign title>Annotation Display Page</#assign>
<#setting url_escaping_charset='ISO-8859-1'>

<#include "/template/ui/user/header.ftl" />


<h1>Annotation Succesfully stored</h1>

<@ww.form theme="simple">

<!-- 	Display Structure: -->


<table class="table_top_header">
  <thead>
    <tr><th>Structure</th></tr>
  </thead>
  <tbody>
    <tr>
      <td>
	<img style="display: block;" src="get_sugar_image.action?download=false&scale=1&opaque=false&outputType=png&glycanSequenceId=${scan.parentStructure.glycanSequenceId?c}" /
      </td>
    </tr>
  </tbody>
</table>

<!-- 	Display Annotation: -->

<table class="table_top_header full_width">
  <thead>
    <tr>
      <th>peak id</th>
      <th>peak</th>
      <th>Intensity</th>
      <th>annotation id</th>
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
    <#list orderedPeaks as p>
      <#if p.peaksAnnotateds.size()==0 >
        <!-- tr<#if col==1>class="ms_table_white"<#else>class="ms_table_lightgrey"</#if> -->
	<tr>
	  <td>${p.mzValue}</td> 
	  <td>${p.intensityValue}</td>
	</tr>
      <#else>
	<#assign ann_id=0>
	<#list p.peaksAnnotateds as q>
	  <!-- tr <#if col==1>class="ms_table_white"<#else>class="ms_table_lightgrey"</#if> -->
	  <tr>
	    <#if ann_id==0>
	      <td rowspan="${p.peaksAnnotateds.size()?c}">${p.peaksLabeledId}</td> 
	      <td rowspan="${p.peaksAnnotateds.size()?c}">${p.mzValue}</td> 
	      <td rowspan="${p.peaksAnnotateds.size()?c}">${p.intensityValue}</td>
	    </#if>
	    <td>${q.peaksAnnotatedId}</td> 
	    <td>
	      <img style="display: block;" src="get_sugar_image.action?download=false&scale=0.5&opaque=false&outputType=png&sequences=${q.formula?url}" />
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
	      <#list q.peaksAnnotatedToIons as i>
	        <#assign charge = charge + i.charge>
	      </#list>
	      <#if charge!=0>${charge}</#if>
	    </td>
	    <td>
	      <#list q.peaksAnnotatedToIons as i>
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

<@ww.submit value="Finish" name="submitAction" />

</@ww.form>


<#include "/template/common/footer.ftl" />