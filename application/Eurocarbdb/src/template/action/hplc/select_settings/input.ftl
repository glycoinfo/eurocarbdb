<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<#assign title>Select Devices</#assign>

<@ww.url value="/css/action/select_settings_input.css" includeParams="none" id="url_page_css"/>


<@ww.url value="/js/lib/JQuery/jquery-ui-1.7.1.custom.min.js" includeParams="none" id="url_personal"/>
<@ww.url value="/js/lib/JQuery/jquery-1.3.2.min.js" includeParams="none" id="url_jquery"/>
<@ecdb.include_js url="${url_jquery}" />
<@ecdb.include_js url="${url_personal}" />
<@ecdb.include_css url="${url_page_css}" />


<#include "/template/common/header.ftl" />

<script type="text/javascript">
	$(function(){

	// Tabs
	$('#tabs').tabs();

	// Dialog			
	$('#dialog').dialog({
	autoOpen: false,
	width: 600,
	buttons: {
	"Ok": function() { 
		$(this).dialog("close"); 
	}, 
	"Cancel": function() { 
		$(this).dialog("close"); 
	} 
	}
	});
				
	// Dialog Link
	$('#dialog_link').click(function(){
		$('#dialog').dialog('open');
		return false;
	});

				
				
	//hover states on the static widgets
	$('#dialog_link, ul#icons li').hover(
		function() { $(this).addClass('ui-state-hover'); }, 
		function() { $(this).removeClass('ui-state-hover'); }
	);
				
	});
</script>

<script type="text/javascript">  
	$(document).ready(function(){  
	    $("#developer").change(onSelectChange);       
	});  

	function onSelectChange(){  
	var selected = $("#developer option:selected");       
	var output = "";  
	if(selected.val() != 0){  
	    output = "You Selected " + selected.text();  
	}  
	$("#output").html(output);  
	}  
</script>  

<style type="text/css">

	.headers { margin-top: 2em; }
	#dialog_link {padding: .4em 1em .4em 20px;text-decoration: none;position: relative; font: 90% "Helvetica Neue", "Trebuchet MS", sans-serif; margin: 50px;}
	#dialog_link span.ui-icon {margin: 0 5px 0 0;position: absolute;left: .2em;top: 50%;margin-top: -8px;}
	ul#icons {margin: 0; padding: 0;}
	ul#icons li {margin: 2px; position: relative; padding: 4px 0; cursor: pointer; float: left;  list-style: none;}
	ul#icons span.ui-icon {float: left; margin: 0 4px;}
</style>	
	<h1>Stored Settings</h1>
<#if ( displayColumn?size>0 && displayDetector?size>0 && displayInstrument?size>0 && displaySoftware?size>0 )>

	<p>You have previously submitted data to EUROCarbDB using the below devices and settings.</p>
	<p>Please select your settings before uploading new data. If your selections are not stored click <a href="select_instrument.action">here</a>.</p>

<@ww.form>
	<!-- Tabs -->
	
	<div id="tabs">
		<ul>
		<li><a href="#tabs-1">Column</a></li>
		<li><a href="#tabs-2">HPLC Instrument</a></li>
		<li><a href="#tabs-3">Detector</a></li>
		</ul>
		<div id="tabs-1">
			
		<p>Select your column type from the drop down list below. A description of the selected column is listed in the table for your reference.</p>
		<!--
		<p>If your column is not listed create a <a href="create_column.action">new column</a>.</p>
		-->
		<p>Once selected continue to the HPLC Instrument tab above.</p>
		<select id="columnType" name="columnId" size="1">
		    <#list displayColumn as c>
		    <option value="${c.id}">${c.manufacturer}_${c.model}</option>
		    </#list>
		</select>
		
		<br /><br />
			
		<table class="table_top_header half_width">
		<tr>
		<th class="hplc">Manufacturer</th>
		<th class="hplc">Model</th>
		<th class="hplc">Material</th>
		<th class="hplc">Length (mm)</th>
		<th class="hplc">Width (mm)</th>
		<th class="hplc">Particle Size (&#181)</th>
		</tr>  
     	
		<#list showTypes as list>
		<tr>
		<td align="center">${list.manufacturer}</td>
		<td align="center">${list.model}</td>
		<td align="center">${list.packingMaterial}</td>
		<td align="center">${list.columnSizeLength}</td>
		<td align="center">${list.columnSizeWidth}</td>
		<td align="right">${list.particleSize}
		<#if list_has_next>
		</#if>
		</tr>
		</#list>
		</table>
			
		</div>		
		<div id="tabs-2">
		<p>Select your HPLC Instrument type from the drop down list below.</p>
		<!--
		<p>If your instrument is not listed create a <a href="create_column.action">new instrument</a>.</p>
		-->
		<p>Once selected continue to the detector tab above.</p>	
		<select name="instrumentId" size="1">
		<#list displayInstrument as i>
		<option value="${i.id}">${i.manufacturer}_${i.model}</option>
		</#list>
		</select>
		
			
		</div>
			
			
		<div id="tabs-3">
		<p>Select your detector type from the drop down list below.</p>
		<!--
		<p>If your detector is not listed create a <a href="create_column.action">new column</a>.</p>
		-->
		<select name="detectorId" size="1">
		<#list displayDetector as d>
		<option value="${d.id}">${d.manufacturer}_${d.model}</option>
		</#list>
		</select>
		
	      
		<@ww.submit value="Next" name="submitAction" />
			
		</div>
	</div>
</@ww.form>	
		
		<p><a href="#" id="dialog_link" class="ui-state-default ui-corner-all"><span class="ui-icon ui-icon-newwin"></span>Instructions</a></p>
		
		
		<!-- ui-dialog -->
		<div id="dialog" title="Selecting Devices">
			<p>Before submitting any experimental data to the database a description of your instrument settings is required.</p>
			<p>The selections available are dependent on previous data entries. If you find your device settings are not listed click <a href="select_instrument.action">here</a>.</p>
		</div>
</#if>
<#include "/template/common/footer.ftl" />

