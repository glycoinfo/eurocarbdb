<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<#import "/template/lib/TextUtils.lib.ftl" as text />

<#assign title>${tissueTaxonomy.tissueTaxon}</#assign>

<@ecdb.use_js_lib name="${'TagVisualisation'}"/>
<@ecdb.use_js_lib name="${'AlphabetisedTabbedList'}" />

<@ww.url action="show_tissue_taxonomy" id="url_show_tissue_taxonomy" includeParams="none"/>

<#include "/template/common/header.ftl" />

<!-- breadcrumbs for parent tissue taxa -->
<#if tissueTaxonomy.isRoot() >
<!-- dont show breadcrumbs for root tissue taxon -->
<#else>
<div class=" show_tissue_taxonomy_parent_tissues breadcrumbs breadcrumbs_multi_row">
  <#assign parents = tissueTaxonomy.getAllParentTissueTaxonomies() />
  <div><a href="show_tissue_taxonomy.action" title="Root of the tissue taxonomy hierachy">Tissue</a></div>
  <#if parents?exists && (parents.size() > 0) >
    <#list parents as p >
        <#if ! p.isRoot() >
        <div>&gt;</div>
        <div><@ecdb.tissue t=p /><#--<a href="?tissueTaxonomyId=${ d.tissueTaxonomyId?c }">${ d.tissueTaxon }</a>--></div>
        </#if>
    </#list>
    <div>&gt;</div>
    <div><em>${tissueTaxonomy.tissueTaxon}</em></div>
  </#if>
</div>
</#if>

<!-- External links context_box -->
<@ecdb.context_box title="External links">
<a class=" mesh " href="${tissueTaxonomy.meshUrl}">MeSH</a>
<a class=" pubmed " href="http://www.ncbi.nlm.nih.gov/sites/entrez?db=pubmed&term=%22${tissueTaxonomy.tissueTaxon?url("UTF-8")}%22[mesh]" >Pubmed</a>
<#if (! tissueTaxonomy.isRoot()) >
<@text.wikipedia>${ tissueTaxonomy.tissueTaxon }</@text.wikipedia>
</#if>
</@ecdb.context_box>

<h1>${title}</h1>
<#assign count_seqs=tissueTaxonomy.subTissueTaxonomiesGlycanSequenceCount />
<div class=" show_tissue_taxonomy_description ">
<table>
  <tr>
    <th>EUROCarbDB tissue ID</th>
    <td>${tissueTaxonomy.tissueTaxonomyId?c}</td>
  </tr>
  <tr>
    <th>MeSH ID</th>
    <td><a href="${tissueTaxonomy.meshUrl}">${tissueTaxonomy.meshId}</a></td>
  </tr>
  <tr>
    <th>Description</th>
    <td>${ action.getMeshDescriptionHTML() }</td>
  </tr>
  <tr>
    <th>Number of glycan structures encompassed by this Tissue taxon</th>
    <td>
        <#if (count_seqs > 0)>
            ${count_seqs} (<@ecdb.actionlink name="search_glycan_sequence" params="tissueName=${tissueTaxonomy.tissueTaxon}">show</@>)
        <#else/>
            (none)
        </#if>
    </td>
  </tr>
</table>
</div>

<#assign kids = action.getSortedChildTissueTaxonomies() >
<#if ( kids?exists && (kids.size() > 0)) >
<h2>Sub-taxa of this tissue</h2>
	<div class=" show_tissue_taxonomy_child_tissue ">
		<ul id="child_tissue_taxonomy_list">
        <#list kids as t >
    		<li><a href="?tissueTaxonomyId=${ t.tissueTaxonomyId?c }">${ t.tissueTaxon }</a></li>
    	</#list>
    	</ul>
    </div>
<#else>
	<h3>No sub classifications</h3>
</#if>

<#assign tissue_info = tissueTaxonomy.getListOfSubTissuesGlycanSequenceCount() />
<#if (tissue_info?exists && tissue_info?size > 0) >
<h2>Glycan sequence distribution within sub-taxa of this tissue</h2>

<!-- sub-taxa tag cloud -->
<div>
<table id="tissue_list">
    <thead>
    <tr>
      <th>ECDB id</th>
      <th>Tissue name</th>
      <th>Structure count</th>
    </tr>
    </thead>
    <tbody>
    <#list tissue_info as tis>
      <tr>
        <td>${tis[0]?c}</td>
        <td>${tis[1]}</td>
        <td>${tis[2]?c}</td>
      </tr>
    </#list>
    </tbody>
</table>
</div>
<!-- end sub-taxa tag cloud -->
</#if>



<script type="text/javascript">
//

connect(ECDB,'onload',function() {
	tabber = new ECDB.AlphabetisedTabbedList('child_tissue_taxonomy_list');
	tabber.buildTabbedList();	
});

connect(ECDB,'onload',function() {
	var tagvis = new ECDB.TagVisualisation("tissue_list",[ECDB.TagVisualisation.TagCloud]);
	
	ECDB.TagVisualisation.TagCloud.ELEMENT_CSS_CLASS = "ecdb_richtagcloud";
	ECDB.TagVisualisation.TagCloud.TAG_ELEMENT_CSS_CLASS = "ecdb_richtagcloud_tag";
	
	tagvis.tagColumn = 1;
	tagvis.visualisations[0].tagFactory = function(tagId,tag,row) {
		var md = MochiKit.DOM;
		var ecdbId = row[0].childNodes[0].data;
		var tagEl = md.A({ "id" : ECDB.TagVisualisation.TagCloud.TAG_ELEMENT_ID_PREFIX + tagId,
						  "href" : "${url_show_tissue_taxonomy}?tissueTaxonomyId=" + ecdbId,
						  "title" : tag + " has " + row[2].childNodes[0].data + " structure(s)"
					}, tag);
		return tagEl;
	};
	tagvis.visualisations[0].update(1);
});

//
</script>

<#include "/template/common/footer.ftl" />
