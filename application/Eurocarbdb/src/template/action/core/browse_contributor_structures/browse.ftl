<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<#assign title>Browse structures for contributor ${contributor.contributorName}</#assign>
<#include "/template/common/header.ftl" />

<h1>${title}</h1>

<#if ( message?length > 0 )>
	<div style="padding: 1em 0 1em 0; color: darkred;">
		${message}	
	</div>
</#if>

<#if (results?size>0)>
  
  <@ecdb.page_navigator action_name="browse_contributor_structures.action?contributorId=${contributor.contributorId}&"/>
  <br>

  <table class="table_top_header full_width">
    <thead>
      <tr>
        <th>Structure</th>
        <!--th>Composition</th>
        <th>Mass (mono/avg)</th-->
        <th>Entered</th>
        <th>Contributor</th>
	<th>Data</th>
        <th>Taxonomies</th>
      </tr>
    </thead>
    <tbody>      
      <#list results as seq>
        <tr>
	  <td>
	     <a href="show_glycan.action?glycanSequenceId=${seq.glycanSequenceId?c}">
	       <img src="get_sugar_image.action?download=true&scale=0.5&outputType=png&inputType=gws&tolerateUnknown=1&sequences=${seq.sequenceGWS?url}"/>
	     </a>
	  </td>
	  <!-- td><#if seq.composition?exists>${seq.composition}</#if></td>
	  <td><#if seq.massMonoisotopic?exists && seq.massAverage?exists>${seq.massMonoisotopic} (${seq.massAverage})</#if></td -->
	  <td>${seq.dateEntered?date}</td>
	  <td><a href="show_contributor.action?contributorId=${seq.contributor.contributorId?c}">${seq.contributor.contributorName}</a></td>
	  <td>
	    <#if seq.hasEvidence()>
	      <#if seq.hasHPLCEvidence()>HPLC</#if>
	      <#if seq.hasMSEvidence()>MS</#if>
	      <#if seq.hasNMREvidence()>NMR</#if>
	    <#else>
	      -
	    </#if>
	  </td>
	  <td>	      
	    <ul class="no_bullets">
	      <#list seq.taxonomies as t>
		<li><a href="show_taxonomy.action?taxonomyId=${t.taxonomyId?c}">${t.taxon}</a></li>
	      </#list>
	     </ul>
	  </td>
        </tr>
     </#list>
   </tbody>
  </table>

  <@ecdb.page_navigator action_name="browse_contributor_structures.action?contributorId=${contributor.contributorId}&"/>

<#else/>
    <p>No sequences available for browsing.</p>
</#if>

<#include "/template/common/footer.ftl" />
