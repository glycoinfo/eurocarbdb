<#if show_detail>
<annotation annotationId="${x.annotationId?c}"
	    dateEntered="${x.dateEntered?datetime}">
  
  <#if (x.contributor?exists)>
  ${xml.serialise( x.contributor, false )}
  </#if>
	    
  <#if (x.parentStructure?exists)>
  ${xml.serialise( x.parentStructure, false )}
  </#if> 
  
  <#if (x.persubstitution?exists)>
  <persubstitution abbreviation="${x.persubstitution.abbreviation}"
		   name="${x.persubstitution.name}"/>
  </#if>

  <#if (x.reducingEnd?exists)>
  <reducingEnd abbreviation="${x.reducingEnd.abbreviation}"
		   name="${x.reducingEnd.name}"/>
  </#if>   

  <#if (x.peakAnnotations?exists) >
  <peakAnnotations>
    <#list x.peakAnnotations as pa> 
    ${xml.serialise(pa , true )} 
    </#list>
  </peakAnnotations>
  </#if>  

<#else>
<annotation annotationId="${x.annotationId?c}"/>
</#if>
</annotation>