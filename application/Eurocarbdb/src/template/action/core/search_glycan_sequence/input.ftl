<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />

<#assign title>Search Structures</#assign>

<@ecdb.use_js_lib name="${'YUI::Tabs'}"/>
<@ecdb.use_js_lib name="${'AutoCompleter'}"/>

<#include "/template/common/header.ftl" />

<style type="text/css">
  #main {
    overflow: visible;
  }
</style>

<#assign action_name="search_glycan_sequence.action?">

<h1>${title}</h1>

<#if (additionalQueries?size>0) >
<#assign title="Refine existing search"/>
<p>From your existing search:</p>
<ul>
  <#list additionalQueries as query>
  <li><#if query.searchSequence?exists >
    <#assign img>substructure <@ecdb.sugar_image_for_seq_from_search seq=query.searchSequence style="vertical-align: middle;" /></#assign>
  ${query.description?replace("substructure", img, 'f' )}
  <#else>
  ${query.description}
  </#if>      
   (${query.resultCount} results)</li>
  </#list>
</ul>
<p>refine your query by adding new search terms</p>
</#if>

<#if (queryHistory?size>0) >
<@ecdb.context_box title="Previous searches">
<div id="glycan_sequence_query_history"
  <ul>
  <#list queryHistory?reverse as query>
    <#if query.queryTime?exists >
    <li><a href="${action_name}historicalQueriesToRun=${queryHistory.indexOf(query)}" class="query_result_count">${query.resultCount} results</a> <span class="query_description"><#if query.searchSequence?exists >
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
</#if>
<!-- intro -->
<div id="search_intro">
<p>
  This page allows you to search for glycan sequences by biological context 
  and physical mass/composition and sequence.
</p>
</div>

<@ww.form action="search_glycan_sequence" method="post">
<div id="search_boxes" class="yui-navset yui-skin-sam">
  <ul class="yui-nav">
    <li<#if ! (sequenceGWS?exists)> class="selected"</#if>><a href="#glycan_id">Glycan structure ID</a></li>
    <li><a href="#bc_search">Biological context</a></li>
    <li<#if (sequenceGWS?exists)>  class="selected"</#if>><a href="#sequence_search">Sub-structure</a></li>
    <li><a href="#mass_search">Mass</a></li>
    <li><a href="#composition_search">Composition</a></li>
    </ul>

    <div class="yui-content">

    <!-- ID search -->
    <div id="glycan_id">
        <@ww.textfield name="glycanId" value="" label="Search by a EuroCarbDB Glycan ID" />
        <p>
        Note that specifying a glycan ID will cause any other search parameters
        specified to be ignored.
        </p>
    </div>

    <!-- biological context search -->
    <div id="bc_search">
        <@ww.textfield name="taxonomyName" label="Enter a taxonomy/species name" />
        <@ww.textfield name="tissueName" label="Enter a tissue name" />
        <@ww.textfield name="diseaseName" label="Enter a disease name" />
        <@ww.textfield name="perturbationName" label="Enter a perturbation name"/>
    </div>

    <div id="sequence_search">
        <@ww.select name="sequencePosition" label="Select position" list="{'Anywhere','Core','Terminii','Core + Terminii'}" value="'Anywhere'"/>
        <applet     
          id="GlycanBuilder" 
          name="GlycanBuilder" 
          code="org.eurocarbdb.application.glycanbuilder.GlycanBuilderApplet.class" 
          archive="GlycanBuilderApplet.jar" 
          width="700" 
          height="420" 
          mayscript="true">
        <param name="document" value="${sequenceGWS!""}" />
        </applet>
          <@ww.hidden name="sequenceGWS" value="${sequenceGWS!''}" id="sequenceGWS" />
    </div>
    
    <div id="mass_search">
        <br/>
        <em>This feature temporarily disabled. It will return soon.</em>
        <br/>
        <br/>
        <br/>
        
<#-- disabled until working...
    <@ww.textfield name="discreteMass" label="Enter a specific mass (Da)" />
        <@ww.textfield name="discreteMassTolerance" label="and a tolerance (Da)" />
        <br/>
        - or -
        <br/>
        <@ww.textfield name="lowMass" label="Enter a low mass (Da) in a mass range" />
        <@ww.textfield name="highMass" label="and a high mass (Da)" />
        <br/>
-->        
        <#--
        <@ww.radio name="useAvgMass" value="false" label="monoisotopic mass" />
        <@ww.radio name="useAvgMass" value="true"  label="average mass" />
        -->
<#-- disabled until working...
        <input type="radio" name="avgMass" value="false" selected="selected" /> monoisotopic mass
        <input type="radio" name="avgMass" value="true"  /> average mass
        <br/>
-->
    </div>
        
    <div id="composition_search">
        <br/>
        <em>This feature temporarily disabled. It will return soon.</em>
        <br/>
        <br/>
        <br/>
<#-- disabled until working...
        <@ww.textfield name="exactComp" label="Enter an exact composition" />
        <br/>
        - or -
        <br/>
        <@ww.textfield name="minComp" label="Enter a minimum composition" />
        <@ww.textfield name="maxComp" label="and a maximum composition" />      
-->
    </div>

</div>

  <!--Don't id a submit button as submit, you won't be able to run document.forms[name].submit()-->
  <@ww.submit id="submit_button" value="Search ->" />


</div>

<#list historicalQueriesToRefine as historyId>
<input type="hidden" name="historicalQueriesToRun" value="${historyId}"/>
</#list>

</@ww.form>


<script type="text/javascript">
connect(ECDB,'onload',function() {
  tabview = new YAHOO.widget.TabView('search_boxes'); 
  var builder_tab = null;
  for (var i = 0; i < tabview.get('tabs').length; i++) {
    if (tabview.get('tabs')[i].get('contentEl').id == 'sequence_search') {
      builder_tab = tabview.get('tabs')[i];
      break;
    }
  }
  if (builder_tab == null) {
    return;
  }

  builder_tab.addListener("activeChange",function(e) {

    var applet = $('GlycanBuilder');

    if (e.newValue) {
      if ( ECDB.InitAppletIfNotLoaded(applet) ) {
        return;
      }
    }

    if (applet.getDocument() != null) {      
      $('sequenceGWS').value = applet.getDocument();    
    }

  });

  connect($('submit_button'),'onclick', function() {
    var applet = $('GlycanBuilder');

    if ( ECDB.InitAppletIfNotLoaded(applet) ) {
      return;
    }
    
    if (applet.getDocument() != null) {    
      $('sequenceGWS').value = applet.getDocument();    
    }    
  });

  new ECDB.Autocompleter('autocompleter.action','taxonomy_name').apply($('search_glycan_sequence_taxonomyName'));

  new ECDB.Autocompleter('autocompleter.action','tissue_name').apply($('search_glycan_sequence_tissueName'));

  new ECDB.Autocompleter('autocompleter.action','disease_name').apply($('search_glycan_sequence_diseaseName'));

  new ECDB.Autocompleter('autocompleter.action','perturbation_name').apply($('search_glycan_sequence_perturbationName'));


  connect(ECDB,'appletload', function(applet) {
    if ($('sequenceGWS').value != null) {
      applet.setDocument($('sequenceGWS').value);
    }
  });

  connect(ECDB,'appletload',function(applet) {
    applet.setNotation(ECDB.GetRenderingType());
  });

  connect(ECDB,"notationchange",function() {
    if ($('GlycanBuilder').setNotation) {
      $('GlycanBuilder').setNotation(ECDB.GetRenderingType());
    }
  });


});


</script>

<#include "/template/common/footer.ftl" />

