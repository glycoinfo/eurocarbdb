<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<#assign title>Search structures</#assign>
<#include "/template/common/header.ftl" />

<#assign action_name="${base}/search_glycan_sequence.action?">
<#assign base_action_name="${base}/search_glycan_sequence.action?">


<h1>${title}</h1>

<@ecdb.context_box title="Actions"> 
<a href="${action_name}">New search</a>
<a href="${action_name}historicalQueriesToRefine=${queryHistory.indexOf(currentSearch)}"
    title="Search again within the current set of results"
    >Search within these results</a>
</@>


<#if ( message?length > 0 )>
	<!-- div style="padding: 1em 0 1em 0; color: darkred;">
		${message}	
	</div -->
</#if>

<#if ( currentSearch.description?exists && currentSearch.description != "" )>
<p>You searched for structures where
  <#if currentSearch.searchSequence?exists >
    <#assign img>substructure <@ecdb.sugar_image_for_seq_from_search seq=currentSearch.searchSequence style="vertical-align: middle;" /></#assign>
  ${currentSearch.description?replace("substructure", img, 'f' )}
  <#else>
  ${currentSearch.description}
  </#if>
  <#assign action_name="${action_name}historicalQueriesToRun=${queryHistory.indexOf(currentSearch)}&">
</p>
</#if>

<!-- Indexes -->
<@ecdb.context_box title="Order structures by">
<#list action.indexes as i >
    <#if ( i.name == index.name )>
    <em>${i.title}</em>
    <#else>
    <a href="${action_name}&amp;indexedBy=${i.name}" title="${i.description}">${i.title}</a>
    </#if>
</#list>
</@ecdb.context_box>

<@ecdb.context_box title="Previous searches"> 
<div id="glycan_sequence_query_history"
  <ul>
  <#list queryHistory?reverse as query>
    <#if query.queryTime?exists >
    <li><a href="${base_action_name}historicalQueriesToRun=${queryHistory.indexOf(query)}" class="query_result_count">${query.resultCount} results</a> <span class="query_description">
      <#if query.searchSequence?exists >
        <#assign img>substructure <@ecdb.sugar_image_for_seq_from_search seq=query.searchSequence style="vertical-align: middle;" /></#assign>
      ${query.description?replace("substructure", img, 'f' )}
      <#else>
      ${query.description}
      </#if>      
      </span><span class="query_time">${query.queryTime.time?datetime}</span></li>
    </#if>
  </#list>
  </ul>
</div>
</@>


<#if (results?size>0)>
  <p>Found ${totalResults}</p>

  <@ecdb.page_navigator action_name/>
  <br>

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
<#list results as seq>
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
  <@ecdb.page_navigator action_name/>
<#else/>
    <p>No sequences matching the criteria. <a href="search_glycan_sequence.action">Search again</a>.</p>
</#if>

<#include "/template/common/footer.ftl" />
