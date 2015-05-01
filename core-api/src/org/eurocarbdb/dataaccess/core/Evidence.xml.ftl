<evidence evidenceId="${x.evidenceId?c}"
	  evidenceType="${x.evidenceType?c}"
	  dateEntered="${x.dateObtained?datetime}">

  <#if (x.technique?exists)>
  ${xml.serialise( x.technique, false )}
  </#if>    

  <#if show_detail>

    <#if (x.contributor?exists)>
    ${xml.serialise( x.contributor, false )}
    </#if>

    <#if (x.experiment?exists)>
    ${xml.serialise( x.experiment, false )}
    </#if>

    <#if (x.evidenceContexts?exists)>
    <biologicalContexts>
      <#list x.evidenceContexts as ec >
      ${xml.serialise( ec.biologicalContext, false )}
      </#list>
    </biologicalContexts>
    </#if>        

    <#if (x.glycanSequenceEvidence?exists)>
    <glycanSequences>
      <#list x.glycanSequenceEvidence as gse >
      ${xml.serialise( gse.glycanSequence, false )}
      </#list>
    </glycanSequences>    
    </#if>   

    <#if (x.referencedEvidence?exists)>
    <references>
      <#list x.referencedEvidence as gse >
      ${xml.serialise( gse.reference, false )}
      </#list>
    </references>    
    </#if>
  </#if>
  
</evidence>