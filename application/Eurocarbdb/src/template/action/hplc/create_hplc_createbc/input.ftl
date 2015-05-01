<#--
<#if currentContributor?exists >
<#assign bcs=currentContributor.myUniqueBiologicalContexts />
    <#if ( bcs?size > 5 ) >
        <@ww.select na />
    <#else/>
    Previous contexts you have defined:
    <ol>
    <#list bcs as bc >
        <li><@ecdb.biological_context bc=bc separator=" " /><@ww.submit value="copy ->" /></li>
    </#list>
    </ol>
    </#if>
</#if>
-->

<table class="table_form">

  <#if ( message?length > 0 )>
  <div class="error_message">
    ${message}	
  </div>
  </#if>
  

  <tr id="taxonomy">
    <th>Taxonomy:</th>
    <td>
      <div>
	<span id="taxonomy_value" class="search_value">
	  <input type="hidden" id="taxonomy_value_input" name="taxonomySearch" />
	  <span id="taxonomy_value_text">unspecified </span>	 	  
	  <span>
	    [
	    <ul class="hmenu">
	      <li><span class="clickable" onclick="select('taxonomy','unspecified')">unspecified</span>, </li>
	      <#list currentContributor.myTaxonomies as t>
	      <li><span class="clickable" onclick="select('taxonomy','${t.taxon}')">${t.taxon}</span>, </li>
	      </#list>
	    </ul>	
	    <span class="clickable" onclick="showSearch('taxonomy')">...</span>  
	    ]
	  </span>
	</span>		  
	<span id="taxonomy_search" class="search_search">
	  <input id="taxonomy_search_input" type="text"/>
	  <span id="taxonomy_search_button" class="clickable" onclick="search('taxonomy')">Search</span>
	  <span id="taxonomy_search   join fetch gs.glycanContexts
         _clear" class="clickable" onclick="reset('taxonomy')">Clear</span>
	</span>
      </div>
      <div>
	<div id="taxonomy_results" class="search_results">
	</div>
	<div id="taxonomy_nav" class="search_nav">
	  <span class="clickable" onclick="showPreviousResults('taxonomy')">back </span>
	  <span class="clickable" onclick="showNextResults('taxonomy')">more</span>
	</div>
      </div>
    </td>
  </tr>

  <tr id="tissue">
    <th>Tissue:</th>
    <td>
      <div>
	<span id="tissue_value" class="search_value">
	  <input type="hidden" id="tissue_value_input"  name="tissueTaxonomySearch" />
	  <span id="tissue_value_text">unspecified </span>
	  <span>
	    [
	    <ul class="hmenu">
	      <li><span class="clickable" onclick="select('tissue','unspecified')">unspecified</span>, </li>
	      <#list currentContributor.myTissueTaxonomies as t>
	      <li><span class="clickable" onclick="select('tissue','${t.tissueTaxon}')">${t.tissueTaxon}</span>, </li>
	      </#list>
	    </ul>	
	    <span class="clickable" onclick="showSearch('tissue')">...</span>  
	    ]
	  </span>
	</span>
	<span id="tissue_search" class="search_search">
	  <input id="tissue_search_input" type="text"/>
	  <span id="tissue_search_button" class="clickable" onclick="search('tissue')">Search</span>
	  <span id="tissue_search_clear" class="clickable" onclick="reset('tissue')">Clear</span>
	</span>
      </div>
      <div>
	<div id="tissue_results" class="search_results">
	</div>
	<div id="tissue_nav" class="search_nav">
	  <span class="clickable" onclick="showPreviousResults('tissue')">back </span>
	  <span class="clickable" onclick="showNextResults('tissue')">more</span>
	</div>
      </div>
    </td>
  </tr>
  <tr id="disease">
    <th>Disease:</th>
    <td>
      <div>
	<span id="disease_list" class="search_list">
	  <ul id="disease_list_list" class="search_list_list">
	  </ul>
	  <div id="disease_list_add">
	    <span id="disease_list_empty">none </span>
	    <span>
	      [	     
	      <ul class="hmenu">		
		<#list currentContributor.myDiseases as d>
		<li><span class="clickable" onclick="add('disease','${d.diseaseName}')">${d.diseaseName}</span>, </li>
		</#list>
	      </ul>	
	      <span class="clickable" onclick="showSearch('disease')">...</span>  
	      ]	     
	    </span>
	  </div>
	</span>
	<span id="disease_search" class="search_search">
	  <input id="disease_search_input" type="text"/>
	  <span id="disease_search_button" class="clickable" onclick="search('disease')">Search</span>
	  <span id="disease_search_clear" class="clickable" onclick="reset('disease')">Clear</span>
	</span>
      </div>
      <div>
	<div id="disease_results" class="search_results">
	</div>
	<div id="disease_nav" class="search_nav">
	  <span class="clickable" onclick="showPreviousResults('disease')">back </span>
	  <span class="clickable" onclick="showNextResults('disease')">more</span>
	</div>
      </div>
    </td>
  </tr>

  <tr id="perturbation">
    <th>Perturbation:</th>
    <td>
      <div>
	<span id="perturbation_list" class="search_list">
	  <ul id="perturbation_list_list" class="search_list_list">
	  </ul>
	  <div id="perturbation_list_add">
	    <span id="perturbation_list_empty">none </span>
	    <span>
	      [
	      <ul class="hmenu">
		<#list currentContributor.myPerturbations as p>
		<li><span class="clickable" onclick="add('perturbation','${p.perturbationName}')">${p.perturbationName}</span>, </li>
		</#list>
	      </ul>	
	      <span class="clickable" onclick="showSearch('perturbation')">...</span>  
	      ]
	    </span>
	  </div>
	</span>
	<span id="perturbation_search" class="search_search">
	  <input id="perturbation_search_input" type="text"/>
	  <span id="perturbation_search_button" class="clickable" onclick="search('perturbation')">Search</span>
	  <span id="perturbation_search_clear" class="clickable" onclick="reset('perturbation')">Clear</span>
	</span>
      </div>
      <div>
	<div id="perturbation_results" class="search_results">
	</div>
	<div id="perturbation_nav" class="search_nav">
	  <span class="clickable" onclick="showPreviousResults('perturbation')">back </span>
	  <span class="clickable" onclick="showNextResults('perturbation')">more</span>
	</div>
      </div>
    </td>
  </tr>
</table>

