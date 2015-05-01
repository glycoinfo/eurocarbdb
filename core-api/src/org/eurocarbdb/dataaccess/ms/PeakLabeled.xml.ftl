<#if show_detail>
<peakLabeled mzValue="${x.mzValue?c}"
	     intensityValue="${x.intensityValue?c}"
	     monoisotopic="${x.monoisotopic?string('true','false')}"
	     chargeCount="${x.chargeCount?c}"
	     <#if (x.fwhm?exists)>fwhm="${x.fwhm?c}"</#if>
	     <#if (x.signalToNoise?exists)>signalToNoise="${x.signalToNoise?c}"</#if>
	     >     
<#else>
<peakLabeled mzValue="${x.mzValue?c}"
	     intensityValue="${x.intensityValue?c}">
</#if>
</peakLabeled>
	     