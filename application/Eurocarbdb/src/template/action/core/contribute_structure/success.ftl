<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />

<#assign title>Contribute structure: sequence details</#assign>

<@ecdb.use_js_lib name="${'AjaxFormSubmitter'}"/>
<@ecdb.use_js_lib name="${'YUI::Tabs'}"/>

<#include "/template/common/header.ftl" />

<h1>${title}</h1>

<#if glycanSequence?exists >

<p>The association between structure and biological context has been
successfully created.</p>

<p>
  <a href="show_glycan.action?glycanSequenceId=${glycanSequence.glycanSequenceId?c}">
  <img style="display: block;" 
       src="get_sugar_image.action?download=true&amp;inputType=gws&amp;outputType=png&amp;sequences=${glycanSequence.sequenceGWS?url}" 
       />
  </a>
</p>

<table class="table_left_header">
  <tr>
    <th>EurocarbDB Glycan Sequence ID</th> 
    <td>
      <a href="show_glycan.action?glycanSequenceId=${glycanSequence.glycanSequenceId?c}">${glycanSequence.glycanSequenceId}</a>
    </td>
  </tr>
  <tr>
    <th>Originally contributed</th> 
    <td>${glycanSequence.dateEntered?date}, by <@ecdb.detail_link 
    object=glycanSequence.contributor 
    text=glycanSequence.contributor.contributorName /></td>
  </tr>        
  
  <!-- biological contexts for this sequence -->
  <tr>
    <th>New biological context</th>
    <td>
      <@ecdb.biological_context bc=biologicalContext /><br/><br/>
    </td>
  </tr>  

  <!-- references -->
  <tr>
    <th>References</th>
    <td>
      <@ecdb.actionlink class="ecdb_button" name="edit_structure_select_reference" id="add_reference" params="glycanSequence.glycanSequenceId=${glycanSequence.glycanSequenceId?c}">Add reference</@>
      <#if ( references?exists && references?size>0) >
      <ul>
	<#list references as r>
	<li>
	  <p><@ecdb.actionlink class="ecdb_button reference_delete_button" name="edit_structure_delete_reference" params="glycanSequence.glycanSequenceId=${glycanSequence.glycanSequenceId?c}&amp;reference.referenceId=${r.referenceId?c}">Remove</@></p><p><@ecdb.reference ref=r /></p>
	</li>
	</#list>
      </ul>
      <#else/>
      <p>
	There are no references associated to this structure.
      </p>
      </#if>
    </td>
  </tr>    
</table>
<p>
<@ecdb.actionlink class="ecdb_button" name="create_ms" params="glycanSequenceContextId=${glycanSequenceContext.glycanSequenceContextId?c}">Add MS data</@>
</p>
<#else/>
<p>
  No sequence!
</p>
</#if>

<script type="text/javascript">
ECDB.AjaxFormSubmitter.PanelBasedSubPageFactory($('add_reference'),'references_forms');
ECDB.AjaxFormSubmitter.PanelBasedFactory('reference_delete_button');
</script>

<#include "/template/common/footer.ftl" />
