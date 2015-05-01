<peakAnnotated glycoCtId="${x.glycoCtId?c}"
	       calculatedMass="${x.calculatedMass?c}">
  
  <#if (x.peakAnnotatedToSmallMolecules?exists)>
  <smallMolecules>
    <#list x.peakAnnotatedToSmallMolecules as pas >
    <smallMolecule operation="${pas.operation}" 
		   name="${pas.smallMolecule.name}"/>
    </#list>
  </smallMolecules>
  </#if>   
	       
  <#if (x.peakAnnotatedToIons?exists)>
  <ions>
    <#list x.peakAnnotatedToIons as pai >
    <ion charge="${pai.charge?c}" 
	 gain="${pai.gain?string('true','false')}" 
	 ionType="${pai.ion.ionType}"/>
    </#list>
  </ions>
  </#if> 

  <#if (x.fragmentations?exists)>
  <fragmentations>
    <#list x.fragmentations as f >
    <fragmentation fragmentType="${f.fragmentType}"
		   fragmentDc="${f.fragmentDc}"
		   fragmentAlt="${f.fragmentAlt}"
		   fragmentPosition="${f.fragmentPosition?c}"
		   cleavageOne="${f.cleavageOne?c}"
		   cleavageTwo="${f.cleavageTwo?c}"/>
    </#list>
  </fragmentations>
  </#if> 

</peakAnnotated>