<evidence id="${x.evidenceId?c}" type="${x.evidenceType}" version="${x.version}" dateEntered="${x.dateObtained?datetime}" >
	    
    <acquisition id="${x.acquisitionId?c}">
  
        <#if (x.device?exists)>
        ${xml.serialise( x.device, false )}
        </#if>

        <#if (x.contributor?exists)>
        ${xml.serialise( x.contributor, false )}
        </#if>
    
    <#-- brief XML above this line; detail XML includes the stuff below this line -->
    
    <#if show_detail>
        
        <#if (x.experiment?exists)>
        ${xml.serialise( x.experiment, false )}
        </#if>

        <#if (x.glycanSequenceEvidence?exists)>
        <glycanSequences>
            <#list x.glycanSequenceEvidence as gse >
            ${xml.serialise( gse.glycanSequence, false )}
            </#list>
        </glycanSequences>    
        </#if>   
        
        <#if (x.evidenceContexts?exists)>
        <biologicalContexts>
            <#list x.evidenceContexts as ec >
            ${xml.serialise( ec.biologicalContext, false )}
            </#list>
        </biologicalContexts>
        </#if>        
        
        
        <#if (x.referencedEvidence?exists)>
        <references>
            <#list x.referencedEvidence as re >
            ${xml.serialise( re.reference, false )}
            </#list>
        </references>    
        </#if>
        
        <#if (x.scans?exists)>
        <scans>
            <#list x.scans as s >
            ${xml.serialise( s, true )}
            </#list>
        </scans>
        </#if>    

        <!-- serialised mzXML data not shown yet -->
        
    </#if><#-- end if show_detail -->
    </acquisition>
      
</evidence>