<#include "../header.ftl">

<h2>Glyco-Peakfinder</h2>

<form method="POST" action="${baseUrl}/Result.action" name="values">
  <input type="text" class="gpf_information" height="1" name="pageTo" value=""/>
  <input type="text" class="gpf_information" height="1" name="pageFrom" value="resu"/>
  
<div id="gpf_tabs">
  <ul>
    <li class="current"><a href="#">Result</a></li>
    <li><a href="#" onclick="switchToPage('stru');">Structures</a></li>
    <li><a href="#" onclick="switchToPage('sett');">Settings</a>  
  </ul>
  <div></div>
</div>
<table border="0" cellpadding="0" cellspacing="0" width="702">
<TR>
    <TD class="gpf_table_left"/>
    <td width="700">
        <table cellpadding="2" cellpadding="0" border="0" width="700">
<!-- Start of data areal -->
    <tr>
        <td class="peakfinder_table_contens_left" colspan="7" height="5"/>
    </tr>
    <tr>
        <td class="peakfinder_table_contens_left" colspan="7">Allowed residues :</td>
    </tr>
<#list usedResidue as residue>
    <tr>
        <td class="peakfinder_table_contens_left" colspan="2" valign="top"/>
        <td class="peakfinder_table_contens_left" valign="top">${residue.abbr}</td>
        <td class="peakfinder_table_contens_left" valign="top" colspan="3">${residue.min} - ${residue.max}</td>
        <td class="peakfinder_table_contens_left" valign="top"/>
    </tr>
</#list>
    <tr>
        <td class="peakfinder_table_contens_left" colspan="7" height="10"/>
    </tr>


<#if settings.spectraType = "ms2">
		
	<tr>
        <td class="peakfinder_table_contens_left" colspan="7">Precursor Peak :</td>
    </tr>
    <tr bgcolor="#ffffcc">
        <td class="peakfinder_table_contens_left" valign="top">Mass</td>
        <td class="peakfinder_table_contens_left" valign="top">Intensity</td>
        <td class="peakfinder_table_contens_left" valign="top">Composition<br>(check for fragment and structure search)</td>
        <td class="peakfinder_table_contens_left" valign="top">Charged <br>Ions</td>
        <td class="peakfinder_table_contens_left" valign="top">Ion <br>type</td>
        <td class="peakfinder_table_contens_left" valign="top">Mass <br>calculated</td>
        <td class="peakfinder_table_contens_left" valign="top">Deviation <br>[ppm]</td>
    </tr>
<#list result.precursor as pre>
	<#if pre.realCount = 0>        			
					<tr bgcolor="#ffffff">
		            	<td class="gpf_contens_right">${pre.mz?string(",##0.000")}
		<#if (settings.massShiftValue < 0)>        			
							<br>(-${settings.massShiftValue?string(",##0.000")})
   	    </#if>
		<#if (settings.massShiftValue > 0)>        			
							<br>(+${settings.massShiftValue?string(",##0.000")})
   	    </#if>
		             	</td>	     
	        			<td class="gpf_contens_right">
	    <#if pre.intensity = 0>        			
	        			n/a
       	<#else>
	        		${pre.intensity}
   	    </#if>
	           			</td>
	       		        <#if pre.complette = false>
						<td class="peakfinder_table_max_anno" colspan="5">This peak has ${pre.annotationCount} annotations. Only the first ${pre.realCount} are shown.</td>
						<#else>
						<td class="peakfinder_table_contens_left" colspan="5"/>
						</#if>
				    </tr>
   	<#else>
		<#list pre.annotation as anno>
				    <tr bgcolor="#ffffff">
		    <#if anno.number = pre.lowAnnoId>
			        	<td class="gpf_contens_right" rowspan="${pre.count?c}">${pre.mz?string(",##0.000")}
		<#if (settings.massShiftValue < 0)>        			
							<br>(${settings.massShiftValue?string(",##0.000")})
   	    </#if>
		<#if (settings.massShiftValue > 0)>        			
							<br>(+${settings.massShiftValue?string(",##0.000")})
   	    </#if>
			        	</td>
	        			<td class="gpf_contens_right" rowspan="${pre.count?c}">
			    <#if pre.intensity = 0>        			
	        			n/a
	        	<#else>
	        		${pre.intensity}
	    	    </#if>
	           			</td>
		    </#if>
				        <td class="peakfinder_table_contens_left"><input type="checkbox" name="searchNumber" value="pre-${pre.number}-${anno.number}" unchecked>
				        	&nbsp;${anno.composition}
	        			    <#if anno.gainLossString != "">
	        			        &nbsp;&nbsp;&nbsp;&nbsp;${anno.gainLossString}
				    	    </#if>	        			            				
				        </td>
	    	    		<td class="peakfinder_table_contens_left">${anno.ions}</td>
				        <td class="peakfinder_table_contens_left">${anno.fragments}</td>
	        			<td class="gpf_contens_right">${anno.mass?string(",##0.00000")}</td>
			    	    <td class="gpf_contens_right">${anno.deviation?string(",##0.0")}</td>
				    </tr>
		</#list>
		<#if pre.complette = false>
			<tr bgcolor="#ffffff">
				<td class="peakfinder_table_max_anno" colspan="5">This peak has ${pre.annotationCount} annotations. Only the first ${pre.realCount} are shown.</td>
			</tr>
		</#if>			    
					    
	</#if>	
</#list>	
    <tr>
        <td class="peakfinder_table_contens_left" colspan="7" height="10"/>
    </tr>
	<tr>
        <td class="peakfinder_table_contens_left" colspan="7">Fragmented Spectra:</td>
    </tr>	
</#if>


    <tr bgcolor="#ffffcc">
        <td class="peakfinder_table_contens_left" valign="top">Mass</td>
        <td class="peakfinder_table_contens_left" valign="top">Intensity</td>
        <td class="peakfinder_table_contens_left" valign="top">Composition<br>(check for fragment and structure search)</td>
        <td class="peakfinder_table_contens_left" valign="top">Charged <br>Ions</td>
        <td class="peakfinder_table_contens_left" valign="top">Ion <br>type</td>
        <td class="peakfinder_table_contens_left" valign="top">Mass <br>calculated</td>
        <td class="peakfinder_table_contens_left" valign="top">Deviation <br>[ppm]</td>
    </tr>
    
<#list result.annotatedPeaks as peak>
    <#if peak.realCount = 0>        			
					<tr bgcolor="#ffffff">
		            	<td class="gpf_contens_right">${peak.mz?string(",##0.000")}
		<#if (settings.massShiftValue < 0)>        			
							<br>(-${settings.massShiftValue?string(",##0.000")})
   	    </#if>
		<#if (settings.massShiftValue > 0)>        			
							<br>(+${settings.massShiftValue?string(",##0.000")})
   	    </#if>
		             	</td>	     
	        			<td class="gpf_contens_right">
	    <#if peak.intensity = 0>        			
	        			n/a
       	<#else>
	        		${peak.intensity}
   	    </#if>
	           			</td>
				        <#if peak.complette = false>
						<td class="peakfinder_table_max_anno" colspan="5">This peak has ${peak.annotationCount} annotations. Only the first ${peak.realCount} are shown.</td>
						<#else>
						<td class="peakfinder_table_contens_left" colspan="5"/>
						</#if>			    
				    </tr>
   	<#else>
		<#list peak.annotation as anno>
				    <tr bgcolor="#ffffff">
		    <#if anno.number = peak.lowAnnoId>
			        	<td class="gpf_contens_right" rowspan="${peak.count?c}">${peak.mz?string(",##0.000")}
		<#if (settings.massShiftValue < 0)>        			
							<br>(${settings.massShiftValue?string(",##0.000")})
   	    </#if>
		<#if (settings.massShiftValue > 0)>        			
							<br>(+${settings.massShiftValue?string(",##0.000")})
   	    </#if>
			        	</td>
	        			<td class="gpf_contens_right" rowspan="${peak.count?c}">
			    <#if peak.intensity = 0>        			
	        			n/a
	        	<#else>
	        		${peak.intensity}
	    	    </#if>
	           			</td>
		    </#if>
				        <td class="peakfinder_table_contens_left"><input type="checkbox" name="searchNumber" value="frag-${peak.number}-${anno.number}" unchecked>
				        	&nbsp;${anno.composition}
	        			    <#if anno.gainLossString != "">
	        			        &nbsp;&nbsp;&nbsp;&nbsp;${anno.gainLossString}
				    	    </#if>	        			            				
				        </td>
	    	    		<td class="peakfinder_table_contens_left">${anno.ions}</td>
				        <td class="peakfinder_table_contens_left">${anno.fragments}</td>
	        			<td class="gpf_contens_right">${anno.mass?string(",##0.00000")}</td>
			    	    <td class="gpf_contens_right">${anno.deviation?string(",##0.0")}</td>
				    </tr>
		</#list>
		<#if peak.complette = false>
			<tr bgcolor="#ffffff">
						<td class="peakfinder_table_max_anno" colspan="5">This peak has ${peak.annotationCount} annotations. Only the first ${peak.realCount} are shown.</td>
			</tr>
		</#if>			    
	</#if>		
</#list>
		<tr>
        	<td class="peakfinder_table_contens_left" colspan="7" height="10"/>
		</tr>
		<tr>
			<td/>
			<td/>
        	<td class="peakfinder_table_contens_left" colspan="5" >
        		<input style="width:250px" class="peakfinder_stecial_button" type="button" onClick="js:switchToPage('dela');" value="Delete selected annotations">
        	</td>
		</tr>
		<tr>
			<td/>
			<td/>
        	<td class="peakfinder_table_contens_left" >
        		<input style="width:250px" class="peakfinder_stecial_button" type="button" onClick="js:switchToPage('stru');" value="Search composition in DB">
        	</td>
        	<td class="peakfinder_table_contens_left" colspan="4">
        		<select name="database" size="1">
					<option value="glycosciences" selected>GLYCOSCIENCES.de</option>
			    </select>
        	</td>
		</tr>
		<tr>
        	<td class="peakfinder_table_contens_left" colspan="7" height="10"/>
		</tr>
<!-- End of data areal -->
        </table>
    </td>
    <TD class="gpf_table_right"/>
</TR>
<TR>
    <TD class="gpf_table_bottom_left"/>
    <TD class="gpf_table_bottom"/>
    <TD class="gpf_table_bottom_right"/>
</TR>
<TR>
    <TD colspan="3" height="20"/>
</TR>
<TR>
    <TD width="1">
    </TD>
    <td width="700">
        <tablE cellpadding="0" cellspacing="0" border="0" width="700">
        <TR>
            <TD width="130"></TD>
            <TD width="160">
	            <input class="peakfinder_button" type="button" onClick="window.location.href='Input.action?pageTo=mass'" value="Change settings">
            </TD>
            <TD width="120"></TD>
            <TD width="160">
	            <input class="peakfinder_button" type="button" onClick="window.location.href='Reset.action'" value="New calculation">
            </TD>
            <TD width="130"></TD>
        </TR>
        </tablE>
    </td>
    <TD width="1">
    </TD>
</TR>
<TR>
    <TD colspan="3" height="20"/>
</TR>
</table>

</form>
<#include "../footer.ftl">