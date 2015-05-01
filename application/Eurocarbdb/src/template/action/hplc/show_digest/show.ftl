<#assign title>Show Scan Page</#assign>
<#include "/template/common/header.ftl" />

<!-- Display Data: -->

<p>Overview of the experimental parameters used:</p>

<table>
  <tr>
    <td>Device</td>
    <td>${scan.device}</td>
  </tr>
  <tr>
    <td>MS Exponent</td>
    <td>${scan.msExponent}</td>
  </tr>
  <tr>
    <td>Scan polarity</td>
    <td>${scan.polarity?string('positive','negative')}</td>
  </tr>
  <tr>
    <td>Deisotoped</td>
    <td>${scan.deisotoped?string('yes','no')}</td>
  </tr>
  <tr>
    <td>Charge deconvoluted</td>
    <td>${scan.chargeDeconvoluted?string('yes','no')}</td>
  </tr>
  <tr>
    <td>Datafile</td>
    <td>${scan.dataFile}</td>
  </tr>
  <tr>
    <td>Base Peak m/z</td>
    <td>${scan.basePeakMz}</td>
  </tr>
  <tr>
    <td>Base Peak Intensity</td>
    <td>${scan.basePeakIntensity}</td>
  </tr>
  <tr>
    <td>Start m/z</td>
    <td>${scan.startMz}</td>
  </tr>
  <tr>
    <td>End m/z</td>
    <td>${scan.endMz}</td>
  </tr>
  <tr>
    <td>Low m/z</td>
    <td>${scan.lowMz}</td>
  </tr>
  <tr>
    <td>High m/z</td>
    <td>${scan.highMz}</td>
  </tr>
  <tr>
    <td>User specified quality level of the scan</td>
    <td>${scan.contributorQuality}</td>
  </tr>
</table>

<p>Annotation:</p>

<table>
  <tr>
    <td>User specified quality level of the annotationn</td>
    <td>${contributorQuality}</td>
  </tr>
</table>

<table class="ms_table">
  <thead>
    <tr>
      <th>peak</th>
      <th>Intensity</th>
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
    <#list scan.peaksLabeleds as p>
      <#if p.peaksAnnotateds.size()==0 >
        <tr<#if col==1>class="ms_table_white"<#else>class="ms_table_blue"</#if>>
	  <td>${p.mzValue}</td> 
	  <td>${p.intensityValue}</td>
	</tr>
      <#else>
	<#assign ann_id=0>
	<#list p.peaksAnnotateds as q>
	  <tr <#if col==1>class="ms_table_white"<#else>class="ms_table_blue"</#if>>
	    <#if ann_id==0>
	      <td rowspan="${p.peaksAnnotateds.size()?c}">${p.mzValue}</td> 
	      <td rowspan="${p.peaksAnnotateds.size()?c}">${p.intensityValue}</td>
	    </#if>
	    <td>
	      <img style="display: block;" src="get_sugar_image.action?download=false&scale=0.5&outputType=png&sequences=${q.formula?url}" />
	    </td>
	    <td>${q.calculatedMass}</td>
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
	      ${charge}
	    </td>
	    <td>
	      <#list q.peaksAnnotatedToIons as i>
	        ${i.charge} ${i.ion.ionType} 
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

<!-- neutral exchanges not supported in the first vesion-->

<@ww.form>

  <@ww.submit value="Close" name="close" />


</@ww.form>

<#include "/template/common/footer.ftl" />

