<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<#assign title>Browse evidence</#assign>
<#include "/template/common/header.ftl" />

<h1>${title}</h1>

<#if ( message?length > 0 )>
	<div style="padding: 1em 0 1em 0; color: darkred;">
		${message}	
	</div>
</#if>

<#if (results?size>0)>

<#assign evidence_type_map = action.getMapOfEvidenceCountByType() />
<#if ( evidence_type_map?exists && evidence_type_map?size > 0 )>
<@ecdb.context_box title="Evidence summary">
<p>
    ${evidence_type_map["MS"]!0} mass spectra
    <br/>
    ${evidence_type_map["HPLC"]!0} HPLC profiles
    <br/>
    ${evidence_type_map["NMR"]!0} spectra
</p>
</@ecdb.context_box>
<#else>
<!-- couldnt get evidence type map -->
</#if>


<!-- Indexes -->
<@ecdb.context_box title="Order evidence by">
<#list action.indexes as i >
    <#if ( i.name == index.name )>
    <em>${i.title}</em>
    <#else>
    <a href="${base}/browse_evidence.action?indexedBy=${i.name}" title="${i.description}">${i.title}</a>
    </#if>
</#list>
</@ecdb.context_box>

<@ecdb.page_navigator action_name="browse_evidence.action?"/>
<br/>

<table class="table_top_header full_width">
<thead>
    <tr>
        <th>Type</th>
        <th>Entered</th>
        <th>Contributor</th>
        <th>Structures</th>
        <th>Taxonomies</th>
        <th>Ref</th>
    </tr>
</thead>
<tbody>      
<#list results as ev>
    <tr>
        <td><@ecdb.evidence ev=ev /></td>
        <td>${ev.dateEntered}</td>
        <td><@ecdb.contributor c=ev.contributor /></td>
        <td>
            <ul class="hmenu">
            <#list ev.glycanSequences as gs>
            <li><@ecdb.linked_sugar_image id=gs.glycanSequenceId seq=gs.sequenceGWS?url scale="0.25" /></li>
            </#list>
            </ul>
        </td>
        <td>	      
            <ul class="hmenu">
            <#list ev.taxonomies as t>
            <li><@ecdb.taxonomy t=t /></li>
            </#list>
            </ul>
        </td>
        <td>	      
            <#list ev.references as r>	     
            <a href="show_reference.action?referenceId=${r.referenceId?c}" title="${r}">[${r.referenceId?c}]</a>
            </#list>
        </td>
    </tr>
</#list>
</tbody>
</table>

<@ecdb.page_navigator action_name="browse_evidence.action?"/>

<#else/>
    <p>No evidence available for browsing.</p>
</#if>

<#include "/template/common/footer.ftl" />
