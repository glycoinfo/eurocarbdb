<peakAnnotation>
  
  <#if (x.peakLabeled?exists)>
  ${xml.serialise( x.peakLabeled, false )}
  </#if>   

  <#if (x.peakAnnotateds?exists) >
  <peakAnnotateds>
    <#list x.peakAnnotateds as pa> 
    ${xml.serialise(pa , true )} 
    </#list>
  </peakAnnotateds>
  </#if>  

</peakAnnotation>