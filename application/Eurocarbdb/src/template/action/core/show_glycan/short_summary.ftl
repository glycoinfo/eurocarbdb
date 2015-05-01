<@ecdb.context_box title="Selected sequence"> 
  <img      src="get_sugar_image.action?download=true&inputType=gws&outputType=png&sequences=${glycanSequence.sequenceGWS?url}" class="sugar_image"
       />
  <#if (glycanSequence.glycanSequenceId > 0) >
    <a href="show_glycan.action?glycanSequenceId=${glycanSequence.glycanSequenceId?c}" title="Sequence ID ${glycanSequence.glycanSequenceId?c} - click for full detail">Show entry</a>
  <#else>
    <span>New entry</span>
  </#if>
</@>
<@ecdb.context_box title="Previously reported in">
  <div class="context_table">
    <h4>Species</h4>
    <#assign tax_list = glycanSequence.uniqueTaxonomies />
    <#if ( tax_list?exists && tax_list?size > 0 ) >
    <ul id="linked_taxonomies" class="bc_context_list">
      <#list tax_list as tax >
      <li><@ecdb.humanised_taxonomy tax /></li>
      </#list>
    </ul>
    </#if>
    <h4>Disease</h4>
    <#assign disease_list = glycanSequence.uniqueDiseases />
    <#if ( disease_list?exists && disease_list?size > 0 ) >
    <ul id="linked_diseases" class="bc_context_list">
      <#list disease_list as disease >
      <li>${disease.diseaseName}</li>
      </#list>
    </ul>
    </#if>
    <h4>Tissue</h4>
    <#assign tissue_list = glycanSequence.uniqueTissues />
    <#if ( tissue_list?exists && tissue_list?size > 0 ) >
    <ul id="linked_tissues" class="bc_context_list">
      <#list tissue_list as tissue >
      <li>${tissue.name}</li>
      </#list>
    </ul>
    </#if>
    
    
  </div>
</@>
<script type="text/javascript">
connect(ECDB,'onload',function() {
  var names = ['linked_taxonomies','linked_diseases','linked_tissues'];
  for (var i in names) {
	  new ECDB.AlphabetisedTabbedList(names[i]).buildTabbedList();
  }
});
</script>