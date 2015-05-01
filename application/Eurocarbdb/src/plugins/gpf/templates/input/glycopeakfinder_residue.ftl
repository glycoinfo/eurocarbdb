<#include "../header.ftl">

<h2>Glyco-Peakfinder</h2>

<form method="POST" action="${baseUrl}/Input.action" name="values">
  <input type="text" class="gpf_information" height="1" name="pageTo" value=""/>
  <input type="text" class="gpf_information" height="1" name="pageFrom" value="resi"/>

<div id="gpf_tabs">
  <ul>   
    <li><a href="#" onclick="switchToPage('mass');">Mass</a></li>
    <li class="current"><a href="#">Residue</a></li>
    <li><a href="#" onclick="switchToPage('ions');">Ion/charge</a></li>
    <li><a href="#" onclick="switchToPage('modi');">Modifications</a></li>
  </ul>
  <div></div>
</div>
<table border="0" cellpadding="0" cellspacing="0" width="702">
<TR>
    <TD class="gpf_table_left"/>

    <td width="700">
        <table cellpadding="0" cellpadding="0" border="0" width="700">

                <tr><TD colspan="12" height="10"></TD></tr>
				<TR>
    				<td colspan="12" align="center">
        				<table cellpadding="0" cellspacing="0" border="0" >
 						<TR>
            				<TD width="80">
								<input class="peakfinder_button" type="button" name="annotate" value="Annotate peaks" onclick="js:switchToPage('calc');">
            				</TD>
            				<TD width="40"></TD>
            				<TD width="80">
	            				<input class="peakfinder_button" type="button" onClick="window.location.href='Reset.action'" value="Reset all settings">
            				</TD>
            				<TD width="40"></TD>
            				<TD width="80">
								<input class="peakfinder_button" type="button" name="resetform" value="Reset form" onclick="js:switchToPage('reset');">
            				</TD>
            				<TD width="40"></TD>
            				<TD width="80">
								<input class="peakfinder_button" type="button" value="Help" name="help" onClick="window.open('./help/GlycoPeakfinder_res_help.html')">
	    					</TD>
        				</TR>
        				</tablE>
    				</td>
				</TR>
				<TR>
    				<td colspan="12" align="center" height="20"/>
				</TR>
				<TR>
    				<td colspan="12" align="center">
        				<table cellpadding="0" cellspacing="0" border="0" >
 						<TR>
            				<TD width="150" class="peakfinder_table_bold_text">
								Select by motif : 
            				</TD>
            				<TD width="310">
							<select name="motif" size="1" width="300" style="width:300px;">
								<option value="choose" selected>Choose a motif ...</option>
								<#list settings.motifs as x>
									<#if settings.spectraType = "profile">
										<#if x.used = true><option value="${x.id}">${x.name}</option></#if>
									<#else>
										<#if x.used = false><option value="${x.id}">${x.name}</option></#if>
									</#if>									
			        			</#list>
			    			</select>	            				
            				</TD>
            				<TD width="100" align="center">
								<input class="peakfinder_stecial_button" style="width:100px;" type="button" name="select motif" value="Select" onclick="js:switchToPage('motif');">
            				</TD>            				
        				</TR>
        				</tablE>
    				</td>
				</TR>
				<TR>
    				<td colspan="12" align="center" height="10"/>
				</TR>
<#list settings.categorie as x>
        <tr>
        	<td colspan="12" height="10"></td>
        </tr>
        <tr>
        	<td width="10"></td>
        	<td width="200" class="peakfinder_table_contens_left_under">${x.name}</td>
        	<td width="110" class="peakfinder_table_bold_text">Increment mass</td>
			<td width="100" class="peakfinder_table_bold_text">Abbreviation</td>
        	<td class="peakfinder_table_bold_text" colspan="2">Occurrence</td>
        	<td colspan="6" class="peakfinder_table_bold_text">
<#if settings.spectraType = "profile">
<#else>
        		Calculation of fragments
</#if>        	
        	</td> 
        </tr>
        <tr>
        	<td width="10"></td>
        	<td width="200" class="peakfinder_table_contens_left_under"></td>
        	<td width="110" class="peakfinder_table_bold_text"></td>
			<td width="100" class="peakfinder_table_bold_text"></td>
        	<td width="50" class="peakfinder_table_bold_text">min</td>
        	<td width="50" class="peakfinder_table_bold_text">max</td>
<#if settings.spectraType = "profile">
        	<td colspan="6" class="peakfinder_table_bold_text"/>
<#else>
        	<td class="peakfinder_table_bold_text" width="25">A/X</td>
        	<td class="peakfinder_table_bold_text" width="25">E</td>
        	<td class="peakfinder_table_bold_text" width="25">F</td>
        	<td class="peakfinder_table_bold_text" width="25">G</td>
        	<td class="peakfinder_table_bold_text">H</td>
        	<td ></td>
</#if>   
        	 
        </tr>
        <tr>
        	<td colspan="12" height="5"></td>
        </tr>
	<#list x.residues as y>        
        <tr>
        	<td width="10"></td>
        	<td width="200" class="peakfinder_table_norma_text">${y.name}</td> 
        	<td width="110">
        		<table cellpadding="0" cellpadding="0" border="0" width="110">
        			<tr>
        				<td width="70" class="gpf_contens_right">${y.mass?string(",##0.000")}&nbsp;u</td>
        				<td width="40"/>
        			</tr>
        		</table>
        	</td>        	
        	<td width="100" class="peakfinder_table_norma_text">${y.abbr}</td>
        	<td width="50" class="peakfinder_table_norma_text">
        		<input type="text" name="myResidues.${x.id}_${y.id}_min" size="2" value="${y.min}">
        	</td>
        	<td width="50" class="peakfinder_table_norma_text">
        		<input type="text" name="myResidues.${x.id}_${y.id}_max" size="2" value="${y.max}">
        	</td>
        	
<#if settings.spectraType = "profile">
			<td class="peakfinder_table_norma_text" colspan="6"/>
<#else>
        		<td class="peakfinder_table_norma_text">
        		<#if y.hasAx = true>
	        		<input type="checkbox" name="myResidues.${x.id}_${y.id}_ax" value="true" <#if y.useAx = true>checked</#if>>
	        		<!--<input class="peakfinder_button_ax" type="button" name="configure" value="select" >-->
				</#if></td>	   
				
				<td class="peakfinder_table_norma_text">
        		<#if y.hasE = true>
	        		<input type="checkbox" name="myResidues.${x.id}_${y.id}_e" value="true" <#if y.useE = true>checked</#if>>
				</#if></td>	   
				
				<td class="peakfinder_table_norma_text">
        		<#if y.hasF = true>
	        		<input type="checkbox" name="myResidues.${x.id}_${y.id}_f" value="true" <#if y.useF = true>checked</#if>>
				</#if></td>	   
				
				<td class="peakfinder_table_norma_text">
        		<#if y.hasG = true>
	        		<input type="checkbox" name="myResidues.${x.id}_${y.id}_g" value="true" <#if y.useG = true>checked</#if>>
				</#if></td>	   
				
				<td class="peakfinder_table_norma_text">
        		<#if y.hasH = true>
	        		<input type="checkbox" name="myResidues.${x.id}_${y.id}_h" value="true" <#if y.useH = true>checked</#if>>
				</#if></td>	 
				<td width="10"></td>  				     		
</#if>        	
        	 
        </tr>
   	</#list>     
</#list>   
        <tr>
        	<td colspan="12" height="10"></td>
        </tr>
        <tr>
        	<td width="10"></td>
        	<td width="200" class="peakfinder_table_contens_left_under">Other residues</td>
        	<td width="110" class="peakfinder_table_bold_text">Increment mass</td>
			<td width="100" class="peakfinder_table_bold_text">Abbreviation</td>
        	<td class="peakfinder_table_bold_text" colspan="2">Occurence</td>
        	<td colspan="6" class="peakfinder_table_bold_text">
        	</td> 
        </tr>
        <tr>
        	<td width="10"></td>
        	<td width="200" class="peakfinder_table_contens_left_under"></td>
        	<td width="110" class="peakfinder_table_bold_text"></td>
			<td width="100" class="peakfinder_table_bold_text"></td>
        	<td width="50" class="peakfinder_table_bold_text">min</td>
        	<td width="50" class="peakfinder_table_bold_text">max</td>
        	<td colspan="6" class="peakfinder_table_bold_text">
        	</td>  
        </tr>
        <tr>
        	<td colspan="12" height="5"></td>
        </tr>
        <tr>
        	<td width="10"></td>
        	<td width="200" class="peakfinder_table_norma_text">
        		Other residue 1
        	</td>        	
        	<td width="110" class="peakfinder_table_norma_text">
        		<input type="text" name="myOtherResidues.1_mass" size="10" value="${settings.otherResidueOne.mass}">&nbsp;&nbsp;u
        	</td>        	
        	<td width="100" class="peakfinder_table_norma_text">
        		<input type="text" name="myOtherResidues.1_name" size="5" value="${settings.otherResidueOne.name}">
        	</td>
        	<td width="50" class="peakfinder_table_norma_text">
        		<input type="text" name="myOtherResidues.1_min" size="2" value="${settings.otherResidueOne.min}">
        	</td>
        	<td width="50" class="peakfinder_table_norma_text">
        		<input type="text" name="myOtherResidues.1_max" size="2" value="${settings.otherResidueOne.max}">
        	</td>
        	<td colspan="6"></td>       	
        </tr>
        <tr>
        	<td colspan="12" height="12"></td>
        </tr>
        <tr>
        	<td width="10"></td>
        	<td width="200" class="peakfinder_table_norma_text">
        		Other residue 2
        	</td>        	
        	<td width="110" class="peakfinder_table_norma_text">
        		<input type="text" name="myOtherResidues.2_mass" size="10" value="${settings.otherResidueTwo.mass}">&nbsp;&nbsp;u
        	</td>        	
        	<td width="100" class="peakfinder_table_norma_text">
        		<input type="text" name="myOtherResidues.2_name" size="5" value="${settings.otherResidueTwo.name}">
        	</td>
        	<td width="50" class="peakfinder_table_norma_text">
        		<input type="text" name="myOtherResidues.2_min" size="2" value="${settings.otherResidueTwo.min}">
        	</td>
        	<td width="50" class="peakfinder_table_norma_text">
        		<input type="text" name="myOtherResidues.2_max" size="2" value="${settings.otherResidueTwo.max}">
        	</td>
        	<td colspan="6"></td>       	
        </tr>
        <tr>
        	<td colspan="12" height="12"></td>
        </tr>
        <tr>
        	<td width="10"></td>
        	<td width="200" class="peakfinder_table_norma_text">
        		Other residue 3
        	</td>        	
        	<td width="110" class="peakfinder_table_norma_text">
        		<input type="text" name="myOtherResidues.3_mass" size="10" value="${settings.otherResidueThree.mass}">&nbsp;&nbsp;u
        	</td>        	
        	<td width="100" class="peakfinder_table_norma_text">
        		<input type="text" name="myOtherResidues.3_name" size="5" value="${settings.otherResidueThree.name}">
        	</td>
        	<td width="50" class="peakfinder_table_norma_text">
        		<input type="text" name="myOtherResidues.3_min" size="2" value="${settings.otherResidueThree.min}">
        	</td>
        	<td width="50" class="peakfinder_table_norma_text">
        		<input type="text" name="myOtherResidues.3_max" size="2" value="${settings.otherResidueThree.max}">
        	</td>
        	<td colspan="6"></td>       	
        </tr>
        
        <tr>
        	<td colspan="12" height="10"></td>
        </tr>
        <tr><TD colspan="12" height="10"></TD></tr>
				<TR>
    				<td colspan="12" align="center">
        				<table cellpadding="0" cellspacing="0" border="0" >
 						<TR>
            				<TD width="80">
								<input class="peakfinder_button" type="button" name="annotate" value="Annotate peaks" onclick="js:switchToPage('calc');">
            				</TD>
            				<TD width="40"></TD>
            				<TD width="80">
	            				<input class="peakfinder_button" type="button" onClick="window.location.href='Reset.action'" value="Reset all settings">
            				</TD>
            				<TD width="40"></TD>
            				<TD width="80">
								<input class="peakfinder_button" type="button" name="resetform" value="Reset form" onclick="js:switchToPage('reset');">
            				</TD>
            				<TD width="40"></TD>
            				<TD width="80">
								<input class="peakfinder_button" type="button" value="Help" name="help" onClick="window.open('./help/GlycoPeakfinder_res_help.html')">
	    					</TD>
        				</TR>
        				</tablE>
    				</td>
				</TR>
        <tr><TD colspan="12" height="10"></TD></tr>
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
</table>
</form>

<#include "../footer.ftl">