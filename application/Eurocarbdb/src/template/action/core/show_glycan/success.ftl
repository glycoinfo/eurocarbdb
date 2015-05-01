<#assign title>Glycan sequence detail</#assign>
<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<@ecdb.include_js url="${base}/js/lib/jit.js" />
<@ecdb.include_js url="${base}/js/lib/viz.js" />
<#t><#include "/template/common/header.ftl" />
<h1>${title}</h1>

<#if glycan?exists >

<!-- the sugar image -->
<p><@ecdb.guided_sugar_image id=glycan.glycanSequenceId seq=glycan.sequenceGWS/></p>     

<p>
<table class="table_left_header">

    <tr>
        <th>EurocarbDB Glycan Sequence&nbsp;ID</th> 
        <td>${glycan.glycanSequenceId?c}</td>
    </tr>
    
    <tr>
        <th>Originally contributed</th> 
        <td>${glycan.dateEntered}, by <@ecdb.contributor c=glycan.contributor /></td>
    </tr>
    
    <!-- evidence for this sequence -->
    <#assign evidence_list = glycan.evidence />
    <tr>
	    <th>Evidence for this sequence</th> 
        <td>
    <#if ( evidence_list?exists && evidence_list?size > 0 ) >
        <#list evidence_list as ev >
            <@ecdb.evidence ev=ev  /> 
            entered on ${ev.dateEntered}
            by <@ecdb.contributor c=ev.contributor /><br/>
        </#list>
    <#else/>
        (No evidence for this sequence has been contributed yet)
    </#if>
        </td>
	</tr>

<!-- biological contexts for this sequence -->
<#assign biological_context_list = glycan.uniqueBiologicalContexts />
<tr>
    <th>Biological contexts in which this sequence has been observed</th>
    <td>
    <#if ( biological_context_list?exists && biological_context_list?size > 0 ) >
        <ul>
            <#list biological_context_list as bc >
            <li><@ecdb.biological_context bc=bc separator="; " /></li>
            </#list>
        </ul>
    <#else><#-- no biological contexts -->
        (No biological context information for this sequence has been contributed yet)
    </#if>
    </td>
</tr> 

<#attempt>    
<#assign biological_context_graph = glycan.getTaxonomyGraph() />
<#--assign biological_context_graph = glycan.getBiologicalContextGraph() /-->
<#if ( biological_context_graph?exists && biological_context_graph?size > 0 ) >
<tr>
  <th>Taxonomic distribution of this glycan</th>
    <td><div id="glycan_${glycan.glycanSequenceId?c}_taxa" class="taxonomy_tree"></div>
<script>
//<![CDATA[
    var json = <@ecdb.jit_json_tree graph=biological_context_graph />;
    var div = "glycan_${glycan.glycanSequenceId?c}_taxa";
    draw_taxonomy_tree( div, json );
//]]>
</script>
<#else><#-- no biological contexts -->
    (No taxa)
</#if>
<#recover>
    <!-- failed to draw tax tree -->
</#attempt>
  </td>
</tr>


<!-- references -->
<#assign references = listReference />
<tr>
    <th>References</th>
    <td>
    <#if ( references?exists && references?size>0) >
        <ul>
            <#list references as r>
            <li><@ecdb.reference ref=r/></li>
            </#list>
        </ul>
    <#else>
        <p> There are no references associated to this structure. </p>
    </#if>
    </td>
</tr>

<!-- composition -->
<#assign compositionMap = glycan.composition />
<#if compositionMap?exists >
<tr>
    <th>Composition</th>
    <td><@text.joinMap map=compositionMap joinEntriesBy="<br/>" joinPairsBy=":&nbsp;" /></td>
</tr>
</#if>

<!-- sequence -->
<#assign seq = glycan.sugarSequence />
<tr>
    <th>Sequence</th>
    <td><!--
        <#if (glycan.sequenceIupac?exists)><pre style="font-size: small">${glycan.sequenceIupac}</pre>
        <hr/>
        </#if>--><pre style="font-size: small">${seq}</pre></td>
</tr>

</table>

<#assign seq=glycan.sequenceGWS?url />

<#--============== context box for further actions ===============-->
<@ecdb.context_box title="Actions" >
<a href="${base}/search_glycan_sequence!input.action?sequenceGWS=${seq}" title="Use this structure as the input to a sub-structure search">Sub-structure search</a>
<#if ! currentContributor.isGuest() >
<hr/>
<!-- actions for adding new evidence -->
<a href="create_ms.action?glycanSequenceId=${glycanSequenceId}" title="Add or associate mass-spectrometry data to this entry">Add MS data</a>
<a href="create_hplc_gu.action?glycanSequenceId=${glycanSequenceId}" title="Add or associate HPLC data to this entry">Add HPLC data</a>
<a href="javascript:alert('Addition of NMR data not yet finished!')" title="Add or associate NMR data to this entry">Add NMR data</a>
<a href="contribute_structure_bc.action?glycanSequenceId=${glycanSequenceId?c}" title="Associate glycan with a biological context">Add Biological Context</a?
</#if>
</@ecdb.context_box>


<#--============== context box for equivalents ===============-->
<#attempt>
<#assign max_equivalents=10 />
<#assign equivalents=glycan.relations.equivalents( -1 ) />
<@ecdb.context_box title="Equivalent structures" >
<#if (equivalents?size > 0)>
    <p>
    <#if glycan.isDefinite() ><!--
        Structure is <abbr title="(ie: contains no unknown elements)">definite</abbr>;
        the following structures are ring conformation isomers at the root terminus.
    -->    
    <#else>
        Structure with ID ${glycan.id?c} is <em>indefinite</em>; 
        it contains one or more unknown structural elements.
        Below are structures in the database that are more 
        definite equivalents of this structure.
    </#if>
    </p>
  <#if (equivalents?size > max_equivalents)>
    <p>${equivalents?size} total, showing most recent 10</p>
    <#else>
    <p>${equivalents?size} structure(s)</p>
  </#if>
  <#list equivalents as gs >
    <#if gs_index == max_equivalents><#break></#if>
    <@ecdb.linked_sugar_image id=gs.glycanSequenceId seq=gs.sequenceGWS?url  />
  </#list>
<#else>
    <!-- no equivalents -->  
    <#if glycan.isDefinite() >
    <p>none, structure is <abbr title="(ie: contains no unknown elements)">definite</abbr></p>
    <#else>
    <p>structure is <abbr title="(ie: structure contains one or more unknown elements)">indefinite</abbr>, 
    but does not currently match any other structure in the database</p>
    </#if>
</#if>
</@ecdb.context_box>
<#recover>
    <!-- equivalents query failed -->
</#attempt>

<#--============== context box for stereochem equivalents ===============-->
<#attempt>
<#assign max_stereochem_equivalents=10 />
<#assign stereochem_equivalents=glycan.relations.getStereochemicalEquivalents( -1 ) />
<@ecdb.context_box title="Stereochemical equivalents" >
<#if (stereochem_equivalents?size > 0)>
    <#if (stereochem_equivalents?size > max_stereochem_equivalents)>
    <p>${stereochem_equivalents?size} total, showing most recent 10<#--
        (<a href="${base}/search_glycan_sequence.action?sequenceGWS=${seq};sequencePosition=Core;sequencePosition=Terminii;count_residues=${glycan.countResidues?c};option=ignore_linkages">see list</a>)
    --></p>
    <#else>
    <p>${stereochem_equivalents?size} structure(s)</p>
    </#if>
    <#list stereochem_equivalents as gs >
    <#if gs_index == max_stereochem_equivalents><#break></#if>
    <@ecdb.linked_sugar_image id=gs.glycanSequenceId seq=gs.sequenceGWS?url />
    </#list>
<#else>
    <em>none</em>    
</#if>
</@ecdb.context_box>
<#recover>
<!-- superstructure query failed -->
</#attempt>

<#--============== context box for linkage isomers ===============-->
<#attempt>
<#assign max_isomers=10 />
<#assign linkageIsomers=glycan.relations.getLinkageIsomers( -1 ) />
<@ecdb.context_box title="Linkage isomers" >
<#if (linkageIsomers?size > 0)>
    <#if (linkageIsomers?size > max_isomers)>
    <p>${linkageIsomers?size} total, showing most recent 10<#--
        (<a href="${base}/search_glycan_sequence.action?sequenceGWS=${seq};sequencePosition=Core;sequencePosition=Terminii;count_residues=${glycan.countResidues?c};option=ignore_linkages">see list</a>)
    --></p>
    <#else>
    <p>${linkageIsomers?size} structure(s)</p>
    </#if>
    <#list linkageIsomers as gs >
    <#if gs_index == max_isomers><#break></#if>
    <@ecdb.linked_sugar_image id=gs.glycanSequenceId seq=gs.sequenceGWS?url  />
    </#list>
<#else>
    <em>none</em>    
</#if>
</@ecdb.context_box>
<#recover>
<!-- linkage isomer query failed -->
</#attempt>

<#--=============== context box for superstructures ==============-->
<#attempt>
<#assign max_superstructs=10 />
<#assign superstructures=glycan.relations.getSuperstructures( -1 ) />
<@ecdb.context_box title="Superstructures of this structure" >
<#if (superstructures?size > 0)>
<p>
    <a href="${base}/search_glycan_sequence.action?sequenceGWS=${seq}"
        title="View all structures that have this structure as a substructure (plus the current structure)"
        >${superstructures?size}</a><#if (superstructures?size > max_superstructs)>
    total, showing most recent 10
    <#else>
    superstructures(s)
    </#if>
    </p>
    <#list superstructures as gs >
    <#if gs_index == max_superstructs><#break></#if>
    <@ecdb.linked_sugar_image id=gs.glycanSequenceId seq=gs.sequenceGWS?url  />
    </#list>
<#else>
    <em>none</em>    
</#if>
</@ecdb.context_box>
<#recover>
<!-- superstructure query failed -->
</#attempt>

<#else/>
    <p>
		No sequence!
    </p>
</#if>

<#include "/template/common/footer.ftl" />
