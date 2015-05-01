<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<#import "/template/lib/TextUtils.lib.ftl" as text />

<#assign title>${perturbation.perturbationName}</#assign>

<@ecdb.use_js_lib name="${'TagVisualisation'}"/>
<@ecdb.use_js_lib name="${'AlphabetisedTabbedList'}" />

<@ww.url action="show_perturbation" id="url_show_perturbation" includeParams="none"/>

<#include "/template/common/header.ftl" />

<!-- breadcrumbs for parent perturbs -->
<#if perturbation.isRoot() >
<!-- dont show breadcrumbs for root perturbation -->
<#else>
<div class="breadcrumbs breadcrumbs_multi_row">
  <#assign parents = perturbation.getAllParentPerturbations() >
  <div><a href="show_perturbation.action">Perturbation</a></div>
  <#if parents?exists && (parents.size() > 0) >
    <#list parents as p >
      <#if ! p.isRoot() >
        <div>&gt;</div>
        <div><@ecdb.perturbation p=p /><#--<a href="?perturbationId=${ p.perturbationId?c }">${ p.perturbationName }</a>--></div>
      </#if>
    </#list>
    <div>&gt;</div>
    <div><em>${perturbation.perturbationName}</em></div>
  </#if>
</div>
</#if>

<@ecdb.context_box title="External links">
<a class=" mesh " href="http://www.nlm.nih.gov/cgi/mesh/2006/MB_cgi?term=${ perturbation.perturbationName?url("UTF-8") }">MeSH</a>
<a class=" pubmed " href="http://www.ncbi.nlm.nih.gov/sites/entrez?db=pubmed&term=%22${perturbation.perturbationName?url("UTF-8")}%22[mesh]" >Pubmed</a>
<#if (! perturbation.isRoot()) >
<@text.wikipedia>${ perturbation.perturbationName }</@text.wikipedia>
</#if>
</@ecdb.context_box>


<h1>${title}</h1>
<#assign count_seqs=perturbation.subPerturbationsGlycanSequenceCount />
<table>	
  <tr>
    <th>EUROCarbDB&nbsp;perturbation&nbsp;ID</th>
    <td>${ perturbationId?c }</td>	
  </tr>  
  <tr>
    <th>MeSH ID</th>
    <td><a href="http://www.nlm.nih.gov/cgi/mesh/2006/MB_cgi?term=${ perturbation.perturbationName?url("UTF-8") }">${ perturbation.meshId }</a></td>	
  </tr>    
  <tr>
    <th>Description</th>
    <td>${ action.getMeshDescriptionHTML() }</td>	
  </tr>
  <tr>
    <th>Number of glycan structures encompassed by this perturbation</th>
    <td>
        <#if (count_seqs > 0)>
            ${count_seqs} (<@ecdb.actionlink name="search_glycan_sequence" params="perturbationName=${perturbation.perturbationName}">show</@>)
        <#else/>
            (none)
        </#if>
    </td>
  </tr>
</table> 

<h2>Direct child perturbation entries</h2>

<#assign kids = action.getSortedChildPerturbations() >
<#if kids?exists && (kids.size() > 0) >
<ul>
  <#list kids as t >
  <#-- skip the (self-referential) root of the tree -->
  <#if t.perturbationName != "Chemicals and Drugs" >
  <li>
    <a href="?perturbationId=${ t.perturbationId?c }"
       title="${ t.briefDescription }" >${ t.perturbationName }</a><br/> 
  </li>
  </#if>
  </#list>
</ul>
<#else>
(none)
</#if>       

<#assign perturbation_info = perturbation.getListOfSubPerturbationsGlycanSequenceCount() />
<#if (perturbation_info?exists && perturbation_info?size > 0) >
<h2>Glycan sequence distribution within sub-classifications</h2>

<!-- sub-taxa tag cloud -->
<div>
<table id="perturbation_list">
    <thead>
    <tr>
      <th>ECDB id</th>
      <th>Perturbation name</th>
      <th>Structure count</th>
    </tr>
    </thead>
    <tbody>
    <#list perturbation_info as per>
      <tr>
        <td>${per[0]?c}</td>
        <td>${per[1]}</td>
        <td>${per[2]?c}</td>
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
	var tagvis = new ECDB.TagVisualisation("perturbation_list",[ECDB.TagVisualisation.TagCloud]);
	
	ECDB.TagVisualisation.TagCloud.ELEMENT_CSS_CLASS = "ecdb_richtagcloud";
	ECDB.TagVisualisation.TagCloud.TAG_ELEMENT_CSS_CLASS = "ecdb_richtagcloud_tag";
	
	tagvis.tagColumn = 1;
	tagvis.visualisations[0].tagFactory = function(tagId,tag,row) {
		var md = MochiKit.DOM;
		var ecdbId = row[0].childNodes[0].data;
		var tagEl = md.A({ "id" : ECDB.TagVisualisation.TagCloud.TAG_ELEMENT_ID_PREFIX + tagId,
						  "href" : "${url_show_perturbation}?perturbationId=" + ecdbId,
						  "title" : tag + " has " + row[2].childNodes[0].data + " structure(s)"
					}, tag);
		return tagEl;
	};
	tagvis.visualisations[0].update(1);
});

//
</script>

<#include "/template/common/footer.ftl" />


