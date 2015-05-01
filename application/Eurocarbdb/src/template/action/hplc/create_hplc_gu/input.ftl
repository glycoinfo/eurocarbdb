<#assign title>Glycan sequence detail</#assign>
<#include "/template/common/header.ftl" />
<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />

<h1>${title}</h1>

<#if glycan?exists >

<p>
<img style="display: block;" 
     src="get_sugar_image.action?download=true&outputType=png&glycanSequenceId=${glycan.glycanSequenceId?c}" 
     />
</p>
<p>
<table class="table_left_header">

    <tr>
        <th>Glycan Sequence ID</th> 
        <td>${glycan.glycanSequenceId?c}</td>
    </tr>
  
  

     <!-- references -->
    <#assign hplc = displayHplcGlycan />
    <#assign references = displayRef />
    <tr>
        <th>References</th>
        <td>
        <#if ( references?exists && references?size>0) >
	    <p>To add a new GU value to any given Pubmed reference click the corresponding article.</p>
            <ul>
                <#list references as r>
		<#if (r.pubmedId?exists &&  r.referenceType=="journal") || (r.referenceType=="database") || (r.referenceType=="website") >
                <li><@ecdb.addGu ref=r hplc=hplc /></li>
		</#if>
                </#list>
		
            </ul>
        <#else/>
            <p> There are no references associated to this structure. </p>
        </#if>
        </td>
    </tr>

    
    <!-- references 
    <#assign references = displayRef />
    <tr>
        <th>References</th>
        <td>
        <#if ( references?exists && references?size>0 && hplcGlycan?exists ) >
            <ul>
                <#list references as r>
                <li><a href="create_gu.action?glycanSequenceId=${glycanSequenceId?c}&amp;glycanReferenceId=${r[1]}">Pubmed Id: test</a> <#if r.title?exists> <b>${r[14]}</b> </#if></li>
                </#list>
            </ul>
        <#else/>
            <p> There are no entries for this structure in the HPLC database</p>
			<ul>
                <#list references as r>
                <li><a href="create_gu.action?glycanSequenceId=${glycan.glycanSequenceId?c}&amp;glycanReferenceId=${r.referenceId?c}&amp;newStructure=yes">Pubmed Id: ${r.pubmedId?c}</a> <#if r.title?exists> <b>${r.title}</b> </#if></li>
                </#list>
            </ul>
        </#if>
        </td>
    </tr>
    --> 
</table>

<#else/>
    <p>
		No sequence!
    </p>
</#if>

<#include "/template/common/footer.ftl" />
