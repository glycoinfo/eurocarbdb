<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<#assign title>Browse Glycan structure contexts for contributor ${contributor.contributorName}</#assign>
<#include "/template/common/header.ftl" />

<h1>${title}</h1>

<#if ( message?length > 0 )>
	<div style="padding: 1em 0 1em 0; color: darkred;">
		${message}	
	</div>
</#if>

<#if (results?size>0)>
  
  <@ecdb.page_navigator action_name="browse_contributor_contexts.action?contributorId=${contributor.contributorId}&"/>
  <br>


  <table class="table_top_header full_width">
    <thead>
      <tr>
        <th>Sequence</th>
	<th>Taxonomy</th>
	<th>Tissue</th>
	<th>Disease</th>
	<th>Perturbation</th>
      </tr>
    </thead>
    <tbody>      
      <#list results as gc>
        <#assign gs = gc.glycanSequence />
	<#assign bc = gc.biologicalContext />
        <tr>
	  <td>
	    <@ecdb.linked_sugar_image scale=0.5 id=gs.glycanSequenceId seq="${gs.sequenceGWS}"/>
	  </td>
	  <td><@ecdb.taxonomy t=bc.taxonomy /></td>
	  <td><@ecdb.tissue t=bc.tissueTaxonomy /></td>
	  <#if bc.diseases.size() gt 0 >
	  <td><@ecdb.bc_disease_names bc=bc /></td>
	  </#if>
	  <#if bc.perturbations.size() gt 0 >
	  <td><@ecdb.bc_perturbations bc=bc /></td>
	  </#if>
        </tr>
     </#list>
   </tbody>
  </table>

  <@ecdb.page_navigator action_name="browse_contributor_contexts.action?contributorId=${contributor.contributorId}&"/>

<#else/>
    <p>No sequences available for browsing.</p>
</#if>

<#include "/template/common/footer.ftl" />



    
