<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<#assign title>Browse structures</#assign>
<#include "/template/common/header.ftl" />

<h1>${title}</h1>
<p>
    ${totalResults} distinct sequences. Indefinite sequences are highlighted.
</p>
<#if ( message?length > 0 )>
	<div style="padding: 1em 0 1em 0; color: darkred;">
		${message}	
	</div>
</#if>

<#if (results?size>0)>

<!-- Indexes -->
<@ecdb.context_box title="Order structures by">
<#list action.indexes as i >
    <#if ( i.name == index.name )>
    <em>${i.title}</em>
    <#else>
    <a href="${base}/browse_structures.action?indexedBy=${i.name}" title="${i.description}">${i.title}</a>
    </#if>
</#list>
</@ecdb.context_box>

<@ecdb.page_navigator action_name="browse_structures.action?"/>
<br/>

<table class="table_top_header full_width">
<thead>
    <tr>
        <th>Structure</th>
        <th>Evidence</th>
        <th>Biological&nbsp;contexts</th>
        <th>References</th>
    </tr>
</thead>
<tbody>      
<#list results as seq>
    <tr>
        <td>
            <@ecdb.linked_sugar_image_seq_only id=seq.glycanSequenceId seq=seq.sequenceGWS?url scale="0.5" />
            <br/>
            <span style="font-size: x-small">
            ID <a href="show_glycan.action?glycanSequenceId=${seq.glycanSequenceId?c}">${seq.glycanSequenceId?c}</a><#if ! seq.isDefinite()> (<abbr title="structure contains one or more unknown structural elements"><em>indefinite</em></abbr>)</#if>, entered ${seq.dateEntered?date} by <@ecdb.contributor c=seq.contributor />
            </span>
        </td>
        <td>
        <#if seq.hasEvidence()>
            <#list seq.evidence as ev >
                <#t><#if (ev_index > 0)>, </#if><@ecdb.evidence ev=ev />
            </#list>
        <#else>
            -
        </#if>
        </td>
        <td>	      
            <ul class="no_bullets"><#assign BCs=seq.uniqueBiologicalContexts />
            <#if ( BCs?exists && BCs?size > 0 )>
            <#list BCs as bc>
                <li><@ecdb.detail_link object=bc text="${ bc_index + 1 }." title="View biological context detail" /> 
                    <@ecdb.biological_context_brief bc=bc /></li>
                <#if (bc_index > 4 && BCs.size() > 5)>
                    <em>(and ${BCs.size() - bc_index} more...)</em>
                    <#break>
                </#if>
            </#list>
            <#else>
            -
            </#if>
            </ul>
        </td>
        <td>	      
            <ul class="no_bullets"><#assign refs=seq.references />
            <#if ( refs?exists && refs?size > 0 )>
            <#list refs as ref >
                <li><@ecdb.detail_link object=ref text="${ ref_index + 1 }." title="View reference detail" />
                    <@ecdb.reference_brief object=ref /></li>
                <#if (ref_index > 4 && refs.size() > 5)>
                    <em>(and ${refs.size() - ref_index} more...)</em>
                    <#break>
                </#if>
            </#list>
            <#else>
            -
            </#if>
            </ul>
        </td>
    </tr>
</#list>
</tbody>
</table>

<@ecdb.page_navigator action_name="browse_structures.action?"/>

<#else/>
    <p>No sequences available for browsing.</p>
</#if>

<#include "/template/common/footer.ftl" />
