<#if show_detail>
<reference id="${x.referenceId?c}" type="${x.referenceType}" ref-name="${x.externalReferenceName}" ref-id="${x.externalReferenceId!''}" >    
    <#assign j = x.journal />
    <journal id="${j.journalId?c}">${j.journalTitle}</journal>
    <publication-year>${x.publicationYear?c}</publication-year>
    <title>${x.title}</title>
    <volume>${x.journalVolume?c}</volume>
    <first-page>${x.firstPage?c}</first-page>
    <last-page>${x.lastPage?c}</last-page>
    <authors>
        <#list x.authorList as a >
        <author lastname="${a.lastname}" initials="${a.firstnameInitialsString}" />
        </#list>
    </authors>
</reference>
<#else>
<reference id="${x.referenceId?c}" type="${x.referenceType}" ref-name="${x.externalReferenceName}" ref-id="${x.externalReferenceId!''}" />
</#if>
    
