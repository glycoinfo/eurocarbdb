<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<#import "/template/lib/TextUtils.lib.ftl" as text />

<#assign title>${ disease.diseaseName }</#assign>

<@ecdb.use_js_lib name="${'TagVisualisation'}"/>
<@ecdb.use_js_lib name="${'AlphabetisedTabbedList'}" />

<@ww.url action="show_disease" id="url_show_disease" includeParams="none"/>

<#include "/template/common/header.ftl" />

<#if disease.isRootDisease() >
    <!-- no breadcrumbs, disease shown is the root disease -->
<#else>
<div class=" show_disease_parent_diseases breadcrumbs breadcrumbs_multi_row">
  <#assign parents = disease.getAllParentDiseases() />
  <div><a href="show_disease.action" title="Root of the disease hierachy">Diseases</a></div>
  <#if parents?exists && (parents.size() > 0) >
    <#list parents as d >
       <#if ! d.isRootDisease() >
         <div>&gt;</div>
         <div><a href="?diseaseId=${ d.diseaseId?c }">${ d.diseaseName }</a></div>
       </#if>
     </#list>
     <div>&gt;</div>
     <div><em>${disease.diseaseName}</em></div>
  </#if>
</div>
</#if>

<@ecdb.context_box title="External links">
	<a  class=" mesh " 
        href="http://www.nlm.nih.gov/cgi/mesh/2006/MB_cgi?term=${ disease.diseaseName?url("UTF-8") }" 
        title="View MeSH entry for '${disease.diseaseName}'"
        >MeSH</a>
	<a  class=" pubmed " 
        href="http://www.ncbi.nlm.nih.gov/sites/entrez?db=pubmed&term=%22${disease.diseaseName?url("UTF-8")}%22[mesh]" 
        title="Search pubmed for references linking to term '${disease.diseaseName}'"
        >Pubmed</a>
	<#if ! disease.isRootDisease() >
	<@text.wikipedia>${ disease.diseaseName }</@text.wikipedia>	
	</#if>
</@ecdb.context_box>


<h1>${title}</h1>
<#assign count_seqs=disease.subDiseasesGlycanSequenceCount />
<table>	
  <tr>
    <th>EUROCarbDB&nbsp;disease&nbsp;ID</th>
    <td>${ diseaseId?c }</td>	
  </tr>  
  <tr>
    <th>MeSH ID</th>
    <td><a href="http://www.nlm.nih.gov/cgi/mesh/2006/MB_cgi?term=${ disease.diseaseName?url("UTF-8") }">${ disease.meshId }</a></td>	
  </tr>    
  <tr>
    <th>Description</th>
    <td>${ action.getMeshDescriptionHTML() }</td>	
  </tr>
  <tr>
    <th>Number of glycan structures encompassed by this disease</th>
    <td>
        <#if (count_seqs > 0)>
            ${count_seqs} (<@ecdb.actionlink name="search_glycan_sequence" params="diseaseName=${disease.diseaseName}">show</@>)
        <#else/>
            (none)
        </#if>
    </td>
  </tr>
</table> 

<#assign kids = action.getSortedChildDiseases() >
<#if kids?exists && (kids.size() > 0) >
	<div class=" show_disease_child_diseases ">
	<h2>Disease sub-classifications</h2>
	<ul id="child_disease_list" >
    <#list kids as t >
        <#-- skip the (self-referential) root of the tree -->
        <#if t.diseaseName != "Diseases" >
	    <li><a href="?diseaseId=${ t.diseaseId?c }"
	       title="${ t.briefDescription }" >${ t.diseaseName }</a></li> 
        </#if>
    </#list>
    </ul>
    </div>
<#else>
</#if>

<#assign disease_info = disease.getListOfSubDiseasesGlycanSequenceCount() />
<#if (disease_info?exists && disease_info?size > 0) >
<h2>Glycan sequence distribution within sub-diseases</h2>

<!-- sub-taxa tag cloud -->
<div>
<table id="disease_list">
    <thead>
    <tr>
      <th>ECDB id</th>
      <th>Disease name</th>
      <th>Structure count</th>
    </tr>
    </thead>
    <tbody>
    <#list disease_info as dis>
      <tr>
        <td>${dis[0]?c}</td>
        <td>${dis[1]}</td>
        <td>${dis[2]?c}</td>
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
	tabber = new ECDB.AlphabetisedTabbedList('child_disease_list');
	tabber.buildTabbedList();	
});

connect(ECDB,'onload',function() {
	var tagvis = new ECDB.TagVisualisation("disease_list",[ECDB.TagVisualisation.TagCloud]);
	
	ECDB.TagVisualisation.TagCloud.ELEMENT_CSS_CLASS = "ecdb_richtagcloud";
	ECDB.TagVisualisation.TagCloud.TAG_ELEMENT_CSS_CLASS = "ecdb_richtagcloud_tag";
	
	tagvis.tagColumn = 1;
	tagvis.visualisations[0].tagFactory = function(tagId,tag,row) {
		var md = MochiKit.DOM;
		var ecdbId = row[0].childNodes[0].data;
		var tagEl = md.A({ "id" : ECDB.TagVisualisation.TagCloud.TAG_ELEMENT_ID_PREFIX + tagId,
						  "href" : "${url_show_disease}?diseaseId=" + ecdbId,
						  "title" : tag + " has " + row[2].childNodes[0].data + " structure(s)"
					}, tag);
		return tagEl;
	};
	tagvis.visualisations[0].update(1);
});

//
</script>


<#include "/template/common/footer.ftl" />


