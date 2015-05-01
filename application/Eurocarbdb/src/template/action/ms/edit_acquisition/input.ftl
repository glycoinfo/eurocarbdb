<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<#assign title>Acquisition Editing Page</#assign>
<#setting url_escaping_charset='ISO-8859-1'>

<@ecdb.use_js_lib name="${'AjaxFormSubmitter'}"/>
<@ecdb.use_js_lib name="${'DatePicker'}"/>
<@ecdb.use_js_lib name="${'ScanNavigator'}"/>
<@ecdb.use_js_lib name="${'YUI::Tabs'}"/>

<#include "/template/common/header.ftl" />

<style type="text/css">
div.acquisition_details
{
  position: relative;
}
div.acquisition_details .acquisition_parameter_details
{
  position: absolute;
  top: 0px;
  right: 0px;
  width: 45%;
  float: left;
  margin-left: 10px;
}
div.acquisition_details .short_glycan_sequence_context {
  font-size: 11px;
}
div.acquisition_details .metadata {
  display: none;
}
div.acquisition_parameter_details .wwgrp
{
  width: 45%;
  margin-left: 10px;
  float: left;  
}

div.acquisition_parameter_details .wwgrp select
{
  margin-top: 3px;
  right: 0px;
  width: 100%;
}

.acquisition_sequence_details {
  float: left;
  position: relative;
  width: 100% !important;
  padding-buttom: 10px;
}

.acquisition_sequence_details > img {
  display: block;
  float: left;
  height: 100px;
  border: solid 1px #ccc;
  -webkit-border-bottom-right-radius: 10px;
  -webkit-border-bottom-left-radius: 10px;
  -webkit-border-top-right-radius: 6px;
  -webkit-border-top-left-radius: 6px;
  -moz-border-radius: 6px 6px 10px 10px;
  padding: 10px 10px 10px 10px;
  cursor: pointer;
  -webkit-box-shadow: 2px 2px 2px rgba(0, 0, 0, 0.3);
  -moz-box-shadow: rgba(0, 0, 0, 0.3) 2px 2px 2px;
  margin: 5px;
}

.acquisition_sequence_details > img:hover {
  border: solid #dddddd 1px;
  background: #dddddd;
}



.acquisition_sequence_details img.selected_sequence {
  border-bottom: solid #9999CC 1px;
  background: #f2f2fb;
} 


.scan_detail .annotation_summary {
  float: left;
}

.scan_detail .annotation_summary img {
  height: 75px;
}

.no_sequence_selected a.auto_annotate_link
{
  color: #aaaaaa;
}

.no_sequence_selected a.auto_annotate_link:hover {
  text-decoration: none;
  color: #aaaaaa;
}

/* For the create annotations form that we're inlining,
   set the max-height for it to be 600px so that we 
   don't have a huge panel element
 */
form[name="create_annotations"], form[name="edit_annotations"] {
  height: 500px;
  padding-right: 10px;
  overflow-x: hidden;
  overflow-y: auto;
}

.peaklist_table {
  height: 500px;
}

</style>
<h1>Acquisition (${acquisition.acquisitionId}) details</h1>
<div class="acquisition_details">
  <!--  This link is functional, but we're commenting this out, since the
        acquisition still assumes you're associated with a biological context,
        and so it doesn't display the sequence you just added
  -->
  <!--
  <p><@ecdb.actionlink name="edit_acquisition_select_seq!input" params="acquisitionId=${acquisition.acquisitionId?c}&evidenceId=${acquisition.evidenceId?c}" class="ecdb_button bc_add_seq_link">Add sequence</@>
  </p>
  -->
<#if (acquisition.biologicalContexts?exists && acquisition.biologicalContexts?size>0) >
<div class="acquisition_context_details">
<p>Select glycan sequence to auto-annotate peaks</p>
<h2>Associated contexts</h2>
  <#list acquisition.biologicalContexts as context >
    <@ecdb.biological_context bc=context />
    <#if (context.glycanSequences?exists && context.glycanSequences?size>0) >
    <div class="acquisition_sequence_details">
      <#list acquisition.glycanSequences as seq >
	<@ecdb.guided_sugar_image id=seq.glycanSequenceId seq=seq.sequenceGWS?url />
      </#list>
      <div style="clear: both; height: 0px; width: 100%"></div>
    </div>
    </#if>
    <@ecdb.actionlink name="edit_acquisition_select_seq!input" params="acquisitionId=${acquisition.acquisitionId?c}&amp;biologicalContextId=${context.biologicalContextId?c}&amp;evidenceId=${acquisition.evidenceId?c}" class="ecdb_button bc_add_seq_link">Add new sequence</@>
  </#list>
</div>
</#if>
<div class="acquisition_reference_details">
  <@ecdb.actionlink class="ecdb_button" name="edit_acquisition_select_reference" id="add_reference" params="acquisition.acquisitionId=${acquisition.acquisitionId?c}&amp;acquisition.evidenceId=${acquisition.evidenceId?c}">Add reference</@>
<#if (acquisition.references?exists && acquisition.references?size>0)>
<h2>References</h2>
  <ul>
    <#list acquisition.references as r >    
      <li><p><@ecdb.actionlink class="ecdb_button reference_delete_button" name="edit_acquisition_delete_reference" params="acquisition.acquisitionId=${acquisition.acquisitionId?c}&amp;acquisition.evidenceId=${acquisition.evidenceId?c}&amp;reference.referenceId=${r.referenceId?c}">Remove</@></p><p><@ecdb.reference ref=r/></p></li>    
    </#list>
  </ul>
</#if>
</div>

<div class="acquisition_parameter_details">
      <#if (acquisition.filename?exists && acquisition.filename?size>0) >
      <a href="get_file.action?uri=${acquisition.filename?url}">download file</a>
      <#else>
      no file specified. ${acquisition.filename}
      </#if>
<@ww.form>
      <@ww.hidden name="acquisition.acquisitionId" />
      <@ww.hidden name="acquisition.evidenceId" />
      <@ww.select label="Device" list="devices" listValue="fullModelName" listKey="id" size="1" name="acquisition.device.deviceId" value="acquisition.device.deviceId" />
      <@ecdb.datepicker name="acquisition.dateObtained" value="acquisition.dateObtained" label="Date obtained"/> 
      <@ww.submit value="Update" name="submitAction"/>
</@ww.form>
</div>
</div>

<div style="width: 100%; height: 0px; clear: both;"></div>

<h2>Scan Objects</h2>
<p>
<@ecdb.actionlink class="ecdb_button" id="create_scan_button" name="create_scan!input" params="acquisitionId=${acquisition.acquisitionId?c}&amp;acquisition.evidenceId=${acquisition.evidenceId?c}">Add a scan</@>
</p>
<#macro show_scan_nodes scans parentScanId="">
<#if (scans?size>0)>
<#if (parentScanId != '') >
  <#assign ul_id="id='scan_list_"+parentScanId+"'"/>
<#else>
  <#assign ul_id=""/>
</#if>
<ul ${ul_id} class="scan_list">
  <#list scans as scan>
  <li id="list_${scan.scanId?c}">
    <div class="scan_link" id="scan_link_${scan.scanId?c}">Scan ${scan.scanId?c}, 
    <#if (scan.msExponent==1)>MS<#elseif (scan.msExponent==2)>MS/MS<#else>MS<sup>${scan.msExponent}</sup></#if>
    <#if (scan.msExponent>1)>, base peak m/z: ${scan.basePeakMz}</#if>
    </div>
    <@show_scan_nodes scans=scan.childScans parentScanId=scan.scanId?c />
    <#assign a_scan_detail >
    <div id="scan_detail_${scan.scanId?c}" class="scan_detail" onclick="ECDB.stop_event(event);">
      <a class="ecdb_button" onclick="return ECDB.edit_acquisition_scan_link_event(event);" href="edit_scan!input.action?scan.scanId=${scan.scanId?c}">Edit</a>
      <a class="ecdb_button" href="create_scan!input.action?acquisitionId=${acquisition.acquisitionId}&amp;acquisition.evidenceId=${acquisition.evidenceId?c}&amp;parentId=${scan.scanId?c}" onclick="return ECDB.edit_acquisition_scan_link_event(event);">Add</a>
      <a class="ecdb_button" onclick="return ECDB.edit_acquisition_scan_no_response_link_event(event);" href="delete_scan.action?scan.scanId=${scan.scanId?c}">Delete</a>
      <@ecdb.actionlink class="ecdb_button" name="create_peaklist!input" params="scan.scanId=${scan.scanId?c}" onclick="return ECDB.edit_acquisition_scan_link_event(event);">Upload peak list</@>
      <!--<@ecdb.actionlink name="create_scan_image" params="scan.scanId=${scan.scanId?c}">Append image</@>-->
      <#if (scan.peakLabeleds.size()>0) >
      <@ecdb.actionlink class="ecdb_button auto_annotate_link" name="create_annotations!input" params="scan.scanId=${scan.scanId?c}" onclick="return ECDB.create_annotation_link_event(event);">Auto annotate</@>
      </#if>
      <@ecdb.actionlink class="ecdb_button" name="upload_annotations!input" params="scan.scanId=${scan.scanId?c}" onclick="return ECDB.edit_acquisition_scan_link_event(event);" >Upload annotations</@><br/>
      <h2>Scan ${scan.scanId?c}
      <#if (scan.peakLabeleds.size()>0) >
      <@ecdb.actionlink class="ecdb_button" name="show_scan" params="scanId=${scan.scanId?c}" onclick="return ECDB.edit_acquisition_scan_peaklist_event(event);" >Show peaks</@>
      </#if></h2>
      <@ecdb.scan_detail_brief scan=scan />
    </div>
    </#assign>
    <#assign scan_details = scan_details + [a_scan_detail] />
  </li>
  </#list>  
</ul>
</#if>
</#macro>
<#if (acquisition.scans?size>0)>
<#assign scan_details = []>
<div id="all_scans_container" class="no_sequence_selected">
<div id="all_scans" class="ygtv-highlight">
<@show_scan_nodes scans=acquisition.rootScans parentScanId='root'/>
</div>
<div id="scan_details_container">
  <#list scan_details as a_scan_detail>
  ${a_scan_detail}
  </#list>
</div>
<div style="clear: both; height: 0px; width: 100%;"></div>
</div>
</#if>
<script type="text/javascript">
connect(ECDB,"onload",function() {
//<![CDATA[
  /* We need to create an inlineable function for the events
     found within the TreeWidget, since the widget uses
     innerHTML to copy the data elements across, and 
     leaves the document tree in a mess.
  */
  
  var AJAX = ECDB.AjaxFormSubmitter;
  var PF = AJAX.PanelBasedFactory;
  
  ECDB.edit_acquisition_scan_link_event = AJAX.InlineEvent(PF,'scan_form');

  ECDB.edit_acquisition_scan_no_response_link_event = AJAX.InlineEvent(PF);

  ECDB.edit_acquisition_scan_peaklist_event = AJAX.InlineEvent(PF,'peaklist_table');

  ECDB.create_annotation_link_event = AJAX.InlineEvent(PF,"create_annotation_form",function() {
    if (! ECDB.selected_sequence ) {
     
      return false;
    }
    this.href.replace(/glycanSequence.glycanSequenceId=\d+/,'');
    this.href += "&glycanSequence.glycanSequenceId="+ECDB.selected_sequence;
    return true;
  });

  AJAX.PanelBasedFactory($('create_scan_button'), 'scan_form');
  AJAX.PanelBasedFactory('bc_add_seq_link','select_structure_frm');
  var submitters = AJAX.PanelBasedFactory('edit_annotation_link','edit_annotations_form');
  for (var i = 0; i < submitters.length; i++ ) {
    disconnectAll(submitters[i],'success');
  }
  AJAX.PanelBasedFactory('delete_annotation_link');
  
  AJAX.PanelBasedSubPageFactory($('add_reference'),'references_forms');
  AJAX.PanelBasedFactory('reference_delete_button');
  
  
  new ECDB.ScanNavigator($('all_scans'));

  var sequences = $$('.acquisition_sequence_details img');
  for(var i = 0; i < sequences.length; i++) {
    connect(sequences[i],'onclick',function (e) {
      if (ECDB.selected_image)
        removeElementClass(ECDB.selected_image,'selected_sequence');
      
        ECDB.selected_sequence = this.id.match(/\d+/g);
    
        ECDB.selected_image = this;
        addElementClass(this,'selected_sequence');
        removeElementClass($('all_scans_container'), 'no_sequence_selected');
    });
  }
});
//]]>
</script>

<#include "/template/common/footer.ftl" />