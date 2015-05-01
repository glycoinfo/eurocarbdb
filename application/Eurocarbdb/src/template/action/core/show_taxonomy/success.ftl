<#assign title>EurocarbDb &gt; Taxonomy detail &gt; ${taxonomy.taxon}</#assign>
<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<#import "/template/lib/TextUtils.lib.ftl" as text />

<@ww.url value="/css/action/show_taxonomy_success.css" includeParams="none" id="url_page_css"/>

<@ecdb.use_js_lib name="${'TagVisualisation'}"/>
<@ecdb.use_js_lib name="${'ContextImageSearcher'}" />
<@ecdb.use_js_lib name="${'AlphabetisedTabbedList'}" />

<@ecdb.include_css url="${url_page_css}"/>

<@ww.url action="show_taxonomy" id="url_show_taxonomy" includeParams="none"/>


<#include "/template/common/header.ftl" />

<!-- breadcrumbs for parent taxa -->
<#if taxonomy.isRoot() >
<!-- dont show breadcrumbs for root taxon -->
<#else>
<div class=" show_taxonomy_parent_tax breadcrumbs breadcrumbs_multi_row ">
  <#assign parents = taxonomy.getParentTaxonomySubset() >
  <div><a href="show_taxonomy.action" title="Root of the taxonomy hierachy">Taxonomy</a></div>
  <#if parents?exists && (parents.size() > 0) >
    <#list parents as t >
      <#if ! t.isRoot() >
        <div>&gt;</div>
        <#if t.rank != "no rank">
        <div><@ecdb.taxonomy t=t /><br/><span>${t.rank}</span></div>
        <#else/>
        <div><@ecdb.taxonomy t=t /></div>
        </#if>
      </#if>
    </#list>
  </#if>
    <div>&gt;</div>
    <div><em>${taxonomy.taxon}</em></div>
</div>
</#if>

<!-- External links context_box -->
<@ecdb.context_box title="External links">
<@text.ncbitaxonomy id=taxonomy.ncbiId >NCBI</@>
<#if ! taxonomy.isRoot() >
<@text.wikipedia>${ taxonomy.taxon }</@text.wikipedia>
</#if>
</@ecdb.context_box>

<h1><#if taxonomy.isRoot() >Taxonomy root<#else/>${taxonomy.taxon}</#if></h1>

<!-- image thumbnail -->
<div id="show_taxonomy_image_thumbnail">
	<!--<img id="taxonomy_thumbnail" width="170px" height="170px"/>-->
</div>

<!-- taxon info -->
<#assign count_seqs=taxonomy.subTaxonomiesGlycanSequenceCount />
<table>
  <tr>
    <th>Rank</th>
    <td>${taxonomy.rank}</td>
  </tr>
  <tr>
    <th>NCBI Taxonomy id</th>
    <td><@text.ncbitaxonomy id=taxonomy.ncbiId >${taxonomy.ncbiId?c}</@></td>
  </tr>
  <tr>
    <th>EuroCarbDB Taxonomy id</th>
    <td>${taxonomy.taxonomyId?c}</td>
  </tr>
<#if (taxonomy.synonyms?exists && taxonomy.synonyms?size > 0)>
  <tr>
    <th>Synonyms</th>
    <td><@text.join list=taxonomy.synonyms /></td>
  </tr>
</#if>
  <tr>
    <th>Number of glycan structures encompassed by this taxon</th>
    <td>
        <#if (count_seqs > 0)>
            ${count_seqs} (<@ecdb.actionlink name="search_glycan_sequence" params="taxonomyName=${taxonomy.taxon}">show</@>)
        <#else/>
            (none)
        </#if>
    </td>
  </tr>
</table>

<!-- parent taxon -->
<#if (!taxonomy.isRoot()) >
<h2>Direct parent taxon of this taxon</h2>
<ul>
<li><@ecdb.taxonomy t=taxonomy.parentTaxonomy /></li>
</ul>
</#if>

<!-- child taxa -->
<#assign kids = taxonomy.childTaxonomies >
<#if ( kids?exists && (kids.size() > 0)) >
<div id="subtaxa">
<h2>Direct sub-taxa of this taxon</h2>
<div class=" show_taxonomy_child_tax ">
    <ul id="child_taxonomies">
    <#list kids as t >
        <#if (!t.isRoot()) >
        <li><@ecdb.taxonomy t=t /></li><#t/>
        </#if>
    </#list>
    </ul>
</div>
</div> 
<#else>
<!-- No sub-taxa! -->
</#if>

<#-- 
    show either:
        - actual sequences by sub-taxon, if not too many 
        - tag cloud of sub-taxa, no sequences
-->
<#if ((count_seqs > 0) && (count_seqs <= 50))>

    <!-- show actual structures -->
<h2>Glycan sequences</h2> 
    <table>
    
    <#-- this taxon -->
    <#assign tax_seqs = taxonomy.getAssociatedGlycanSequences() />
    <#if (tax_seqs?size > 0)> 
    <tr>
        <td>${tax_seqs?size} sequence(s) associated directly to this taxon:</td>
        <td><#list tax_seqs as gs >
        <@ecdb.linked_sugar_image id=gs.glycanSequenceId scale="0.4" />
        </#list>
        </td>
    </tr>
    </#if>
    
    <#-- sub-taxa -->
    <#assign subtax_seqs = taxonomy.getAllChildTaxonomiesWithContext() />
    <#if (subtax_seqs?size > 0)>
    <tr>
        <td colspan="2">Associated to sub-taxa of this taxon:</td>
    </tr>    
    <#list subtax_seqs?sort as tax >
    <tr>
        <td>
            <@ecdb.taxonomy t=tax /><#--<br/>
            <#list tax.taxonomySynonyms as s >
            <em>${s.synonym}</em><br/>
            </#list>-->
        </td>
        <td class="overlined">
        <#list tax.biologicalContexts as bc >
            <#list bc.glycanSequenceContexts as gsc >
            <@ecdb.linked_sugar_image id=gsc.glycanSequence.glycanSequenceId scale="0.4" />
            </#list>
        </#list>
        </td>
    </tr>
    </#list>
    </#if>
    
    </table>
    
<#else><!-- show table/tag cloud of taxonomy to glycan sequence -->

    <#assign tax_info = taxonomy.getListOfSubTaxonomiesGlycanSequenceCount() />
    <#if (tax_info?exists && tax_info?size > 0) >
    <h2>Glycan sequence distribution within sub-taxa</h2>
    
    <!-- sub-taxa tag cloud -->
    <div>
    <table id="taxonomy_list">
        <thead>
        <tr>
          <th>ECDB id</th> 
          <th>NCBI id</th>
          <th>Taxonomy name</th>
          <th>Structure count</th>
        </tr>
        </thead>
        <tbody>
        <#list tax_info as tax>
          <tr>
            <td>${tax[0]?c}</td>
            <td>${tax[1]?c}</td>
            <td>${tax[2]?cap_first}</td>
            <td>${tax[3]?c}</td>
          </tr>
        </#list>
        </tbody>
    </table>
    </div>
    <!-- end sub-taxa tag cloud -->
    </#if>

</#if><#-- end if count_seqs <= 20 -->

<script type="text/javascript">//<!--

connect(ECDB,'onload',function() {
	var tagvis = new ECDB.TagVisualisation("taxonomy_list",[ECDB.TagVisualisation.TagCloud]);
	
	ECDB.TagVisualisation.TagCloud.ELEMENT_CSS_CLASS = "ecdb_richtagcloud";
	ECDB.TagVisualisation.TagCloud.TAG_ELEMENT_CSS_CLASS = "ecdb_richtagcloud_tag";
	
	tagvis.tagColumn = 2;
	tagvis.visualisations[0].tagFactory = function(tagId,tag,row) {
		var md = MochiKit.DOM;
		var ecdbId = row[0].childNodes[0].data;
		var tagEl = md.A({ "id" : ECDB.TagVisualisation.TagCloud.TAG_ELEMENT_ID_PREFIX + tagId,
						  "href" : "${url_show_taxonomy}?taxonomyId=" + ecdbId,
						  "title" : tag + " has " + row[3].childNodes[0].data + " structure(s)"
					}, tag);
		return tagEl;
	};
	tagvis.visualisations[0].update(1);
});

// connect(ECDB,'onload',function() {
//       new ContextImageSearcher('taxonomy_thumbnail','site:tolweb.org ${ taxonomy.taxon }');
// });

connect(ECDB,'onload',function() {
	var tabber = new ECDB.AlphabetisedTabbedList('child_taxonomies');
	tabber.buildTabbedList();	
});

//-->
</script>

<#include "/template/common/footer.ftl" />
