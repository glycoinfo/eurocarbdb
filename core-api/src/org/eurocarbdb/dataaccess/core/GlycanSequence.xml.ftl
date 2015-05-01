<glycan id="${x.glycanSequenceId?c}" version="${x.version}">
    <sequence format="${x.sugarSequence.sequenceFormat.name}">
        ${x.sugarSequence.toString()?replace("\n"," ")}
    </sequence>

<#if show_detail>
<#--------------------------- contexts --------------------------->
<#assign contexts = x.getGlycanSequenceContexts() />
<#if (contexts?exists && contexts?size > 0)>
    <contexts>
    <#list contexts as c >
        ${xml.serialise( c.biologicalContext, false )}
    </#list>
    </contexts>
</#if>    
<#--------------------------- evidence --------------------------->
<#assign evidence = x.evidence />
<#if (evidence?exists && evidence?size > 0)>
    <evidence-list>
    <#list evidence as ev >
        ${xml.serialise( ev, false )}
    </#list>
    </evidence-list>
</#if>    
<#--------------------------- references --------------------------->
<#assign refs = x.references />
<#if (refs?exists && refs?size > 0)>
    <references> 
    <#list refs as r >
        ${xml.serialise( r )}
   </#list>
   </references>
</#if>   
</#if>   
</glycan>



