<#include "../header.ftl">

<h2>Glyco-Peakfinder</h2>

<form method="POST" action="${baseUrl}/Input.action" name="values">
  <input type="text" class="gpf_information" height="1" name="pageTo" value=""/>
  <input type="text" class="gpf_information" height="1" name="pageFrom" value="ions"/>

<div id="gpf_tabs">
  <ul>   
    <li><a href="#" onclick="switchToPage('mass');">Mass</a></li>
    <li><a href="#" onclick="switchToPage('resi');">Residue</a></li>
    <li class="current"><a href="#">Ion/charge</a></li>
    <li><a href="#" onclick="switchToPage('modi');">Modifications</a></li>
  </ul>
  <div></div>
</div>
<table border="0" cellpadding="0" cellspacing="0" width="702">
<TR>
    <TD class="gpf_table_left"/>
    <td width="700">
        <table cellpadding="0" cellpadding="0" border="0" width="700">
<!-- Start of data areal -->
		<tr><TD colspan="7" height="10"></TD></tr>
				<TR>
    				<td colspan="7" align="center">
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
				<input class="peakfinder_button" type="button" value="Help" name="help" onClick="window.open('./help/GlycoPeakfinder_ion_help.html')">
	    					</TD>
        				</TR>
        				</tablE>
    				</td>
				</TR>



		<tr><TD colspan="7" height="10"></TD></tr>
                <tr>
		    <TD colspan="4">
			<div class="peakfinder_table_headline">
                        	Specify charge states and select ions<br>
			</div>
			<div class="kai_contens_center_gray">
				For multiple selection press "ctrl" and <br>click left mouse button simultaneously. 
			</div>
                    </TD>
<#if settings.spectraType = "profile"> 						
		    <TD colspan="3">
			<br>
		    </td>
		</tr>
<#else>
		    <TD colspan="3">
			<div class="peakfinder_table_headline">
                        Specify fragmentation options<br>
			</div>
			<div class="kai_contens_center_gray">
				For multiple selection press "ctrl" and <br>click left mouse button simultaneously. 
			</div>

                    </TD>
		</tr>
</#if>
		<tr><TD colspan="7" height="10"></TD></tr>
		<tr>
			<td width="10" class="peakfinder_table_norma_text"></td>
			<td width="250" valign="top">
				<div class="peakfinder_table_contens_left">
                        		Charge-states to be calculated:<br>
				</div>
				<div class="kai_contens_justify_gray">
					All selected charge-states can be calculated simultaneously. 
				</div>				
			</td>
			<td width="30" class="peakfinder_table_norma_text"></td>			
			<td width="100" class="peakfinder_table_norma_text">
				<select name="settings.charge" size="4" multiple>
	      		<#list settings.chargeList as x>
		    		<option value="${x.name}" <#if x.used = true>selected</#if>>${x.name}</option>
		      	</#list>
			    </select>    
			</td>
<#if settings.spectraType = "profile"> 						
			<td width="250" class="peakfinder_table_contens_left"></td>
			<td width="30" class="peakfinder_table_norma_text"></td>
			<td width="100" class="peakfinder_table_norma_text"></td>
<#else>
			<td width="250" valign="top">
				<div class="peakfinder_table_contens_left">
                        		No of fragmentations to be calculated:<br>
				</div>
				<div class="kai_contens_justify_gray">
					The selected numbers for cleavage level is combined with all selected fragmentation types. 
				</div>				
			</td>
			<td width="30" class="peakfinder_table_norma_text"></td>
			<td width="50" class="peakfinder_table_norma_text">
				<select name="settings.multiFragmentation" size="4" multiple>
					 <#list settings.multiFragmentationList as x>
                          <option value="${x.name}" <#if x.used = true>selected</#if>>${x.name}</option>
                     </#list>			    
                </select>    
			</td>

</#if>
		</tr>
	    <tr><TD colspan="3" height="10"></TD></tr>
		<tr>
			<td width="10" class="peakfinder_table_norma_text"></td>
			<td width="250" valign="top">
				<div class="peakfinder_table_contens_left">
                        		Select charged Ions:<br>
				</div>
				<div class="kai_contens_justify_gray">
					All combinations of selected ions can be calculated simultaneously.
				</div>				
			</td>
			<td width="30" class="peakfinder_table_norma_text">
				<input class="gp_radio_check" type="checkbox" name="settings.ionBool" value="true" <#if settings.ionBool = "true">checked</#if>>
			</td>
			<td width="100" class="peakfinder_table_norma_text">
				<select name="settings.ion" size="5" multiple>
	      		<#list settings.ionList as x>
		    		<option value="${x.name}" <#if x.used = true>selected</#if>>${x.name}</option>
		      	</#list>
			    </select>    
			</td>
<#if settings.spectraType = "profile"> 			
			<td width="250" class="peakfinder_table_contens_left"></td>
			<td width="30" class="peakfinder_table_norma_text"></td>			
			<td width="100" class="peakfinder_table_norma_text"></td>
<#else>
			<td width="250" valign="top">
				<div class="peakfinder_table_contens_left">
                        		Fragmentation types to be calculated:<br>
				</div>
				<div class="kai_contens_justify_gray">
					All selected fragmentation types can be calculated simultaneously. 
				</div>				
			</td>
			<td width="30" class="peakfinder_table_norma_text"></td>			
			<td width="50" class="peakfinder_table_norma_text">
				<select name="settings.fragmentType" size="6" multiple>
	      		<#list settings.fragmentTypeList as x>
		    		<option value="${x.name}" <#if x.used = true>selected</#if>>${x.name}</option>
		      	</#list>
			    </select>    
			</td>
</#if>			
		</tr>
	    <tr><TD colspan="3" height="10"></TD></tr>
		<tr>
			<td width="10" class="peakfinder_table_norma_text"></td>
			<td width="250" valign="top">
				<div class="peakfinder_table_contens_left">
                        		Add charged Ion and enter mass:<br>
				</div>
			</td>
			<td width="30" class="peakfinder_table_norma_text">
				<input class="gp_radio_check" type="checkbox" name="settings.otherIonBool" value="true" <#if settings.otherIonBool = "true">checked</#if>>
			</td>
			<td width="100" class="peakfinder_table_norma_text">
        		<input type="text" name="settings.otherIonName" size="4" value="${settings.otherIonName}">
			</td>
			<td width="100" class="peakfinder_table_contens_left">&nbsp;</td>
			<td width="30" class="peakfinder_table_contens_left">&nbsp;</td>
			<td width="50" class="peakfinder_table_contens_left">&nbsp;</td>
		</tr>
	    <tr><TD colspan="3" height="10"></TD></tr>
		<tr><TD colspan="3" height="2"></TD></tr>
		<tr>
			<td width="10" class="peakfinder_table_norma_text"></td>
			<td width="250" >
			</td>
			<td class="peakfinder_table_norma_text" colspan="2">
				<input type="text" name="settings.otherIonMass" size="9" value="${settings.otherIonMass}">&nbsp;&nbsp;u 
			</td>
		</tr>
	    <tr><TD colspan="3" height="20"></TD></tr>

<!-- Begin ion exchange and small molecules -->
                <tr>
		    <TD colspan="4">
			<div class="peakfinder_table_headline">
                        	Calculate ion exchanges<br>
			</div>
			<div class="kai_contens_center_gray">
				For multiple selection press "ctrl" and <br>click left mouse button simultaneously. 
			</div>
                    </TD>
		    <TD colspan="3">
			<div class="peakfinder_table_headline">
                        Loss and gain of small molecules<br>
			</div>
			<div class="kai_contens_center_gray">
				Specify max. number of losses/<br>gains for small molecules. 
			</div>

                    </TD>
		</tr>
	    <tr><TD colspan="3" height="10"></TD></tr>
		<tr>
		    	<TD colspan="4" valign="top">
				<table>
					<tr>
						<td width="10" class="peakfinder_table_norma_text"></td>
						<td width="250" valign="top">
							<div class="peakfinder_table_contens_left">
                        					No of ion-exchanges to be calculated:<br>
							</div>
							<div class="kai_contens_justify_gray">
								Calculation of neutral ion-exchanges (e. g. H<sup>+</sup> --> Na<sup>+</sup>). Exchange is allowed for all selected ions.
							</div>				
						</td>
						<td width="30" class="peakfinder_table_norma_text">
							<br>
						</td>
						<td width="100" class="peakfinder_table_norma_text">
							<select name="settings.ionExchangeCount" size="3" multiple>
	      							<#list settings.ionExchangeCountList as x>
		    							<option value="${x.name}" <#if x.used = true>selected</#if>>${x.name}</option>
		      						</#list>
			    				</select>    
						</td>
					</tr>
					<tr><TD colspan="3" height="10"></TD></tr>
					<tr>
						<td width="10" class="peakfinder_table_norma_text"></td>
						<td width="250" valign="top">
							<div class="peakfinder_table_contens_left">
                        					Select ions for neutral exchange:<br>
							</div>
						</td>
						<td width="30" class="peakfinder_table_norma_text">
							<input class="gp_radio_check" type="checkbox" name="settings.ionExchangeBool" value="true" <#if settings.ionExchangeBool = "true">checked</#if>>
						</td>
						<td width="100" class="peakfinder_table_norma_text">
							<select name="settings.ionExchangeIon" size="3" multiple>
	      							<#list settings.ionExchangeIonList as x>
		    							<option value="${x.name}" <#if x.used = true>selected</#if>>${x.name}</option>
		      						</#list>
			    			</select>    
						</td>
					</tr>
	    				<tr><TD colspan="3" height="10"></TD></tr>
					<tr>
						<td width="10" class="peakfinder_table_norma_text"></td>
						<td width="250" valign="top">
							<div class="peakfinder_table_contens_left">
                        					Add ion for exchange and enter mass:<br>
							</div>
						</td>
						<td width="30" class="peakfinder_table_norma_text">
							<input class="gp_radio_check" type="checkbox" name="settings.otherIonExchangeBool" value="true" <#if settings.otherIonExchangeBool = "true">checked</#if>>
						</td>
						<td width="100" class="peakfinder_table_norma_text">
        						<input type="text" name="settings.otherIonExchangeIonName" size="4" value="${settings.otherIonExchangeIonName}">
						</td>
					</tr>
					<tr><TD colspan="3" height="2"></TD></tr>
					<tr>
						<td width="10" class="peakfinder_table_norma_text"></td>
						<td width="250" >
						</td>
						<td class="peakfinder_table_norma_text" colspan="2">
				 			<input type="text" name="settings.otherIonExchangeIonMass" size="9" value="${settings.otherIonExchangeIonMass}">&nbsp;&nbsp;u 
						</td>
					</tr>
				</table>
			</td>		
		    	<TD colspan="3" valign="top">
				<table>
					<tr>
						<td width="170">
							<div class="peakfinder_table_contens_left">
                        					molecule
							</div>
						</td>
						<td width="70">
							<div class="peakfinder_table_contens_left">
                        					mass
							</div>
						</td>
						<td width="50">
							<div class="peakfinder_table_contens_left">
                        					loss
							</div>
						</td>
						<td width="50">
							<div class="peakfinder_table_contens_left">
                        					gain<br>
							</div>
						</td>
					</tr>
					<tr><TD colspan="3" height="10"></TD></tr>
<#list settings.lossGainMolecules as x>
					<tr>
				        	<td width="170" class="peakfinder_table_norma_text">${x.name} (${x.abbr})</td>
				        	<td width="70" class="peakfinder_table_norma_text">
				        		<#if settings.massType = "avg">
				        			${x.massAvg?string(",##0.000")} 
				        		<#else>
					        		${x.massMono?string(",##0.000")} 
				        		</#if>
				        		u</td>
				        	<td width="50" class="peakfinder_table_norma_text">
        						<input type="text" name="myMolecules.${x.id}_loss" size="1" value="${x.loss}">
        					</td>
        					<td width="50" class="peakfinder_table_norma_text">
        						<input type="text" name="myMolecules.${x.id}_gain" size="1" value="${x.gain}">
        					</td>
					</tr>
					<tr><TD colspan="3" height="10"></TD></tr>
</#list>						
					<tr>
				        	<td width="170" class="peakfinder_table_norma_text">Other molecule <input type="text" name="settings.otherLossGainMolecule.name" size="2" value="${settings.otherLossGainMolecule.name}"></td>
				        	<td width="70" class="peakfinder_table_norma_text"><input type="text" name="settings.otherLossGainMoleculeMass" size="4" value="${settings.otherLossGainMoleculeMass}"> u</td>
				        	<td width="50" class="peakfinder_table_norma_text">
        						<input type="text" name="settings.otherLossGainMolecule.loss" size="1" value="${settings.otherLossGainMolecule.loss}">
        					</td>
        					<td width="50" class="peakfinder_table_norma_text">
        						<input type="text" name="settings.otherLossGainMolecule.gain" size="1" value="${settings.otherLossGainMolecule.gain}">
        					</td>
					</tr>
				</table>
			</TD>
	    <tr><TD colspan="3" height="10"></TD></tr>
				<TR>
    				<td colspan="7" align="center">
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
				<input class="peakfinder_button" type="button" value="Help" name="help" onClick="window.open('./help/GlycoPeakfinder_ion_help.html')">
	    					</TD>
        				</TR>
        				</table>
    				</td>
				</TR>	    
<tr><TD colspan="3" height="10"></TD></tr>
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
    <TD colspan="3" height="20"/>
</TR>
</table>

</form>

<#include "../footer.ftl">
