<#if show_detail>   
<scan scanId="${x.scanId?c}"
      msExponent="${x.msExponent?c}"
      polarity="${x.polarity?string('positive','negative')}"
      deisotoped="${x.deisotoped?string('true','false')}"
      chargeDeconvoluted="${x.chargeDeconvoluted?string('true','false')}"
      basePeakMz="${x.basePeakMz?c}"
      basePeakIntensity="${x.basePeakIntensity?c}"
      startMz="${x.startMz?c}"
      endMz="${x.endMz?c}"
      lowMz="${x.lowMz?c}"
      highMz="${x.highMz?c}">
  
  <!-- scanImage -->
 
  <#if (x.msMsRelationshipsWithChildren?exists)>
  <children>
    <#list x.msMsRelationshipsWithChildren as r>
    <msms precursorMz="${r.precursorMz?c}"
	  precursorIntensity="${r.precursorIntensity?c}"
	  precursorMassWindowLow="${r.precursorMassWindowLow?c}"
	  precursorMassWindowHigh="${r.precursorMassWindowHigh?c}"
	  precursorCharge="${r.precursorCharge?c}"
	  msMsMethod="${r.msMsMethod}"> 
      ${xml.serialise( r.childScan, false )}
    </msms>
    </#list>
  </children>
  </#if>
   
  <#if (x.annotations?exists) >
  <annotations>
    <#list x.annotations as a> 
    ${xml.serialise(a , true )} 
    </#list>
  </annotations> 
  </#if>

  <#if (x.peakLabeleds?exists) >
  <peakLabeleds>
    <#list x.peakLabeleds as pl> 
    ${xml.serialise(pl , true )} 
    </#list>
  </peakLabeleds>
  </#if>  

<#else>
<scan scanId="${x.scanId?c}">
</#if>
</scan>
    
    