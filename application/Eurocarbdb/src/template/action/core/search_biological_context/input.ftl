<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />

<#assign title>Biological context search</#assign>

<@ecdb.use_js_lib name="${'YUI::Tabs'}"/>
<@ecdb.use_js_lib name="${'AutoCompleter'}" />

<#include "/template/common/header.ftl" />

<style type="text/css">
  #search_boxes .wwgrp {
    width: 220px;
  }
  #search_boxes .wwgrp input[type="text"] {
    width: 220px;
  }  
</style>

<h1>${title}</h1>
<div id="search_boxes" class="yui-navset yui-skin-sam">
  <ul class="yui-nav">
    <li class="selected"><a href="#taxonomy_search">Taxonomy</a></li>
    <li><a href="#tissue_search">Tissue</a></li>
    <li><a href="#disease_search">Disease</a></li>
    <li><a href="#perturbation_search">Perturbation</a></li>
  </ul>
  <div class="yui-content">
    <div id="taxonomy_search">
      <!-- taxonomy search -->
      <@ww.form action="search_taxonomy" method="post">
	<@ww.textfield name="taxonomyName" label="Enter a NCBI Taxonomy name or ID" />
        <@ww.textfield name="taxonomyID" label="Enter a Eurocarb taxonomy ID" />
        <@ww.submit value="Search" />
      </@ww.form>
    </div>    
    <div id="tissue_search">
      <!-- tissue search -->
      <@ww.form action="search_tissue_taxonomy" method="post">
        <@ww.textfield name="tissueTaxonomyName" label="Enter a tissue name" />
        <@ww.submit value="Search" />

      </@ww.form>
    </div>
    <div id="disease_search">
      <!-- disease search -->
      <@ww.form action="search_disease" method="post">
        <@ww.textfield name="diseaseName" label="Enter a disease name" />
        <@ww.submit value="Search" />

      </@ww.form>
    </div>
    <div id="perturbation_search">
      <!-- perturbation search -->
      <@ww.form action="search_perturbation" method="post">
        <@ww.textfield name="perturbationName" label="Enter a peturbation name" />
        <@ww.submit value="Search" />
      </@ww.form>
    </div>
  </div>
</div>  

<script type="text/javascript">

connect(ECDB,'onload',function() {
  new YAHOO.widget.TabView('search_boxes');
  new ECDB.Autocompleter('autocompleter.action','taxonomy_name').apply($('search_taxonomy_taxonomyName'));

  new ECDB.Autocompleter('autocompleter.action','tissue_name').apply($('search_tissue_taxonomy_tissueTaxonomyName'));

  new ECDB.Autocompleter('autocompleter.action','disease_name').apply($('search_disease_diseaseName'));

  new ECDB.Autocompleter('autocompleter.action','perturbation_name').apply($('search_perturbation_perturbationName'));
  
});

</script>

<#include "/template/common/footer.ftl" />