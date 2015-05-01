<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<@ecdb.header title="Reference detail"/>

<h1>Reference detail</h1>


<#if reference?exists >
  <#if reference.referenceType=="journal">
    <p><i>${reference.authorListAsCitationString}</i>;</p>
    <#if reference.title?exists>
      <p><b>${reference.title}</b></p>
    </#if>      
   <p>${reference.journalAsCitationString}</p>
  <#elseif reference.referenceType=="database">
    <p>Database entry, id=${reference.externalReferenceId}.</p>
  <#elseif reference.referenceType=="website">  
    <p>Web page entry.</p>
  </#if>

  <#if reference.url?exists>
    <p><a href="${reference.url}">Visit the website</a></p>
  <#else>
    <p>No URL defined for this entry.</p>
  </#if>

  <#if reference.referenceComments?exists>
    <p><i>${reference.referenceComments}</i></p>
  </#if>

<#assign sequenceRefs = reference.getGlycanSequenceReferences() />  
<#if (sequenceRefs?exists && sequenceRefs?size > 0)>
<h2>Sequences associated with this Reference</h2>
<ul>
<#list sequenceRefs as sr>
    <li class="no_bullets" style="float: left; display: inline; padding: 1em;">
    <@ecdb.linked_sugar_image id=sr.glycanSequence.glycanSequenceId scale="0.35" />
    <br/><span style="font-size: smaller">Sequence ID ${sr.glycanSequence.glycanSequenceId?c}</span>
    <br/><span style="font-size: x-small"><@ecdb.detail_link object=sr text="link" /> added by <@ecdb.contributor c=sr.contributor /> ${sr.dateEntered?date}</span>
    </li>
</#list>    
</ul>    
</#if>
  
  
<#assign evidenceRefs = reference.getReferencedEvidence() />  
<#if (evidenceRefs?exists && evidenceRefs?size > 0)>
<h2>Evidence associated to this Reference</h2>
<#list evidenceRefs as er>
<div>
    <@ecdb.evidence ev=er.evidence />
    link added by <@ecdb.contributor c=er.contributor /> ${er.dateEntered?date}
</div>    
</#list>    
</#if>
  


<#else>
    <p>Nothing to show.</p>
</#if>

<@ecdb.footer/>
