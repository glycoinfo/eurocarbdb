<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<#assign title>Search structures</#assign>
<#include "/template/common/header.ftl" />

<h1>${title}</h1>

<#if ( message?length > 0 )>
	<!-- div style="padding: 1em 0 1em 0; color: darkred;">
		${message}	
	</div -->
</#if>

<p>
  You searched for structures containing 
  <img style="vertical-align: middle" src="get_sugar_image.action?download=true&scale=1&outputType=png&inputType=gws&tolerateUnknown=1&sequences=${sequenceGWS?url}"/>
  <#if (action.searchCore && searchTerminal) >
  as core and terminal.
  <#elseif (searchCore) >
  as core.
  <#elseif (searchTerminal) >
  as terminal.
  <#else>
  </#if>
</p>

<#if (results?size>0)>
<p>
    Found ${totalResults}. <a href="search_substructure.action">Search again</a>
</p>

<@ecdb.page_navigator "search_substructure.action?sequenceGWS=${sequenceGWS?url}&submitAction=${submitAction}&"/>
<br/>

<table class="table_top_header full_width">
<thead>
    <tr>
        <th>Structure</th>
        <#--
        <th>Composition</th>
        <th>Mass (mono/avg)</th>
        <th>Entered</th>
        -->
        <th>Evidence</th>
        <th>Biological contexts</th>
        <#--<th>References</th>-->
    </tr>
</thead>
<tbody>      
<#list results as sqr>
<#assign seq = sqr.matchedGlycanSequence/>
    <tr>
        <td>
            <@ecdb.linked_sugar_image id=seq.glycanSequenceId seq=seq.sequenceGWS?url scale="1" />
            <br/>
            <span style="font-size: x-small">
            ID ${seq.glycanSequenceId?c}, entered ${seq.dateEntered?date} by <@ecdb.contributor c=seq.contributor />
            </span>
        </td>
<#--
        <td>
        <span style="font-size: x-small;">
        <@text.joinMap map=seq.composition joinEntriesBy="<br/>" joinPairsBy=":&nbsp;" />
        </span>
        </td>
-->
<#--
        <td><#if seq.massMonoisotopic?exists && seq.massAverage?exists>${seq.massMonoisotopic} (${seq.massAverage})</#if></td> 
        <td>
        ${seq.dateEntered?date}<br/>
        by <@ecdb.contributor c=seq.contributor />
        </td>
-->
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
            <ul class="no_bullets"><#assign BCs=seq.biologicalContexts />
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
        </td><#--
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
        </td>-->
    </tr>
</#list>
</tbody>
</table>

<@ecdb.page_navigator "search_substructure.action?sequenceGWS=${sequenceGWS?url}&submitAction=${submitAction}&"/>

<#else>
    <p><i>No sequences matching the criteria</i>.<br/>
    <a href="search_substructure.action">Search again</a></p>
</#if>

<#include "/template/common/footer.ftl" />

