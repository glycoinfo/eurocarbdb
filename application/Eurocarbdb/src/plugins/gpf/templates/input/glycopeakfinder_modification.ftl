<#include "../header.ftl">

<h2>Glyco-Peakfinder</h2>

<form method="POST" action="${baseUrl}/Input.action" name="values">
  <input type="text" class="gpf_information" height="1" name="pageTo" value=""/>
  <input type="text" class="gpf_information" height="1" name="pageFrom" value="modi"/>

<div id="gpf_tabs">
  <ul>   
    <li><a href="#" onclick="switchToPage('mass');">Mass</a></li>
    <li><a href="#" onclick="switchToPage('resi');">Residue</a></li>
    <li><a href="#" onclick="switchToPage('ions');">Ion/charge</a></li>
    <li class="current"><a href="#">Modifications</a></li>
  </ul>
  <div></div>
</div>  
<table border="0" cellpadding="0" cellspacing="0" width="702">
<TR>
    <TD class="gpf_table_left"/>
    <td width="700">
        <table cellpadding="0" cellpadding="0" border="0" width="700">

        <tr><TD colspan="3" height="5"></TD></tr>
<TR>
    <td colspan="7" align="center">
        <tablE cellpadding="0" cellspacing="0" border="0" >
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

				<input class="peakfinder_button" type="button" value="Help" name="help" onClick="window.open('./help/GlycoPeakfinder_mod_help.html')">
					    </TD>
        </TR>
        </tablE>
    </td>
    <TD >
    </TD>
</TR>                
<tr><TD colspan="7" height="10"></TD></tr>
                <TR>

		    <TD colspan="7">
			<div class="peakfinder_table_headline">
                        	Modification of the complete structure<br><br>
			</div>
			<div class="kai_contens_center_gray">
				By chosing a modification, you have to alter the mass for other residues you have already entered on the last page.<br>
				Persubstitutions won't work, if you have specified a lipid, a glycopeptide or another modification at the reducing end.
			</div>

                    </TD>
                </TR>
        <tr><TD colspan="3" height="10"></TD></tr>
		<tr>
			<td width="10" class="peakfinder_table_contens_left"></td>
			<td width="30"><input class="gp_radio_check" type="radio" name="settings.perSubstitution" value="none" <#if settings.perSubstitution = "none">checked</#if>></td>
			<td width="80" class="peakfinder_table_norma_text">none</td>
			<td width="30"><input class="gp_radio_check" type="radio" name="settings.perSubstitution" value="pme" <#if settings.perSubstitution = "pme">checked</#if>></td>
			<td width="220" class="peakfinder_table_norma_text">per-Methylation</td>
			<td width="30"><input class="gp_radio_check" type="radio" name="settings.perSubstitution" value="pdme" <#if settings.perSubstitution = "pdme">checked</#if>></td>
			<td width="220" class="peakfinder_table_norma_text">per-Deutero-Methylation</td>
		</tr>
        <tr><TD colspan="3" height="5"></TD></tr>
		<tr>
			<td width="10" class="peakfinder_table_contens_left"></td>
			<td width="30"></td>

			<td width="80" class="peakfinder_table_norma_text"></td>
			<td width="30"><input class="gp_radio_check" type="radio" name="settings.perSubstitution" value="pac" <#if settings.perSubstitution = "pac">checked</#if>></td>
			<td width="220" class="peakfinder_table_norma_text">per-Acetylation</td>
			<td width="30"><input class="gp_radio_check" type="radio" name="settings.perSubstitution" value="pdac" <#if settings.perSubstitution = "pdac">checked</#if>></td>
			<td width="220" class="peakfinder_table_norma_text">per-Deutero-Acetylation</td>
		</tr>
        <tr><TD colspan="3" height="20"></TD></tr>		
                <TR>

		    <TD colspan="7">
			<div class="peakfinder_table_headline">
                        	Modification at the reducing end<br><br>
			</div>
			<div class="kai_contens_center_gray">
				For glycopeptides and glycolipids no further persubsitution can be calculated.
			</div>
                    </TD>
                </TR>

        <tr><TD colspan="3" height="5"></TD></tr>
		<tr>
			<td width="10" class="peakfinder_table_contens_left"></td>
			<td width="30"><input class="gp_radio_check" type="radio" name="settings.derivatisation" value="none" <#if settings.derivatisation = "none">checked</#if>></td>
			<td width="100" class="peakfinder_table_norma_text">none</td>
			<td width="30"><input class="gp_radio_check" type="radio" name="settings.derivatisation" value="red" <#if settings.derivatisation = "red">checked</#if>></td>
			<td width="220" class="peakfinder_table_norma_text">reduced</td>
			
			<td width="30"><input class="gp_radio_check" type="radio" name="settings.derivatisation" value="d" <#if settings.derivatisation = "d">checked</#if>></td>
			<td width="220" class="peakfinder_table_norma_text">deoxy</td>
		</tr>
        <tr><TD colspan="3" height="5"></TD></tr>		
		<tr>
			<td width="10" class="peakfinder_table_contens_left"></td>
			<td width="30"></td>
			<td width="100" class="peakfinder_table_norma_text"></td>
			<td width="30"><input class="gp_radio_check" type="radio" name="settings.derivatisation" value="PA" <#if settings.derivatisation = "PA">checked</#if>></td>
			<td width="220" class="peakfinder_table_norma_text">2-Amino pyridine</td>

			<td width="30"><input class="gp_radio_check" type="radio" name="settings.derivatisation" value="2AB" <#if settings.derivatisation = "2AB">checked</#if>></td>
			<td width="220" class="peakfinder_table_norma_text">2-Amino benzamide</td>
		</tr>
        <tr><TD colspan="3" height="5"></TD></tr>		
		<tr>
			<td width="10" class="peakfinder_table_contens_left"></td>
			<td width="30"></td>
			<td width="100" class="peakfinder_table_norma_text"></td>
			<td width="30" valign="top"><input class="gp_radio_check" type="radio" name="settings.derivatisation" value="DAP" <#if settings.derivatisation = "DAP">checked</#if>></td>
			<td width="220" class="peakfinder_table_norma_text" valign="top">2,6-Diamino pyridine</td>

			<td width="30" valign="top"><input class="gp_radio_check" type="radio" name="settings.derivatisation" value="DAPMAB" <#if settings.derivatisation = "DAPMAB">checked</#if>></td>
			<td width="220" class="peakfinder_table_norma_text">4-(N-[2,4-Diamino-6-pteridinylmethyl]-<br>amino)benzoic acid</td>
		</tr>
        <tr><TD colspan="3" height="5"></TD></tr>		
		<tr>
			<td width="10" class="peakfinder_table_contens_left"></td>
			<td width="30"></td>
			<td width="100" class="peakfinder_table_norma_text"></td>
			<td width="30"><input class="gp_radio_check" type="radio" name="settings.derivatisation" value="AMC" <#if settings.derivatisation = "AMC">checked</#if>></td>
			<td width="220" class="peakfinder_table_norma_text">7-Amino-4-methylcoumarin</td>

			<td width="30"><input class="gp_radio_check" type="radio" name="settings.derivatisation" value="6AQ" <#if settings.derivatisation = "6AQ">checked</#if>></td>
			<td width="220" class="peakfinder_table_norma_text">6-Amino quinoline</td>
		</tr>
        <tr><TD colspan="3" height="5"></TD></tr>		
		<tr>
			<td width="10" class="peakfinder_table_contens_left"></td>
			<td width="30"></td>
			<td width="100" class="peakfinder_table_norma_text"></td>
			<td width="30"><input class="gp_radio_check" type="radio" name="settings.derivatisation" value="2AA" <#if settings.derivatisation = "2AA">checked</#if>></td>
			<td width="220" class="peakfinder_table_norma_text">2-Aminoacridone</td>

			<td width="30"><input class="gp_radio_check" type="radio" name="settings.derivatisation" value="FMC" <#if settings.derivatisation = "FMC">checked</#if>></td>
			<td width="220" class="peakfinder_table_norma_text">9-Fluorenylmethyl carbazate</td>
		</tr>
        <tr><TD colspan="3" height="5"></TD></tr>		
		<tr>
			<td width="10" class="peakfinder_table_contens_left"></td>
			<td width="30"></td>
			<td width="100" class="peakfinder_table_norma_text"></td>
			<td width="30"><input class="gp_radio_check" type="radio" name="settings.derivatisation" value="DH" <#if settings.derivatisation = "DH">checked</#if>></td>
			<td width="220" class="peakfinder_table_norma_text">Dansylhydrazine</td>

			<td width="30"><input class="gp_radio_check" type="radio" name="settings.derivatisation" value="AA" <#if settings.derivatisation = "AA">checked</#if>></td>
			<td width="220" class="peakfinder_table_norma_text">Antranilic acid</td>
		</tr>
        <tr><TD colspan="3" height="25"></TD></tr>		
		<tr>
			<td width="10" class="peakfinder_table_contens_left"></td>
			<td width="30"></td>
			<td width="100" class="peakfinder_table_norma_text"></td>
			<td width="30"><input class="gp_radio_check" type="radio" name="settings.derivatisation" value="other" <#if settings.derivatisation = "other">checked</#if>></td>

			<td width="220" class="peakfinder_table_norma_text">other modification:&nbsp;&nbsp;
	        	<input type="text" name="settings.otherModificationName" size="6" value="${settings.otherModificationName}"></td>
			<td colspan="2" class="peakfinder_table_norma_text">enter mass:&nbsp;&nbsp;
				<input type="text" name="settings.otherModMass" size="15" value="${settings.otherModMass}">&nbsp;&nbsp;u
			</td>
		</tr>
        <tr><TD colspan="3" height="25"></TD></tr>		
		<tr>
			<td width="10" class="peakfinder_table_contens_left"></td>

			<td width="30"></td>
			<td width="100" class="peakfinder_table_norma_text"></td>
			<td width="30"><input class="gp_radio_check" type="radio" name="settings.derivatisation" value="lipid" <#if settings.derivatisation = "lipid">checked</#if>></td>
			<td width="220" class="peakfinder_table_norma_text">Glycolipid</td>
			<td colspan="2" class="peakfinder_table_norma_text"></td>
		</tr>
        <tr><TD colspan="3" height="5"></TD></tr>		
		<tr>
			<td width="10" class="peakfinder_table_contens_left"></td>

			<td width="30"></td>
			<td width="100" class="peakfinder_table_norma_text"></td>
			<td width="30"></td>
			<td width="220" class="peakfinder_table_norma_text">Sphingosine:&nbsp;
				<select name="settings.sphingosin" size="1">
					<#list settings.sphingosinList as x>
						<option value="${x.id}" <#if x.used = true>selected</#if>>${x.name}</option>
			        </#list>
			    </select>
			<td colspan="2" class="peakfinder_table_norma_text">fatty acid:&nbsp;&nbsp;
				<select name="settings.fattyAcid" size="1">
					<#list settings.fattyAcidList as y>
						<option value="${y.id}" <#if y.used = true>selected</#if>>${y.name} (${y.abbr})</option>
			        </#list>
			    </select>
			</td>

		</tr>
        <tr><TD colspan="3" height="15"></TD></tr>		
		<tr>
			<td width="10" class="peakfinder_table_contens_left"></td>
			<td width="30"></td>
			<td width="100" class="peakfinder_table_norma_text"></td>
			<td width="30"><input class="gp_radio_check" type="radio" name="settings.derivatisation" value="peptid" <#if settings.derivatisation = "peptid">checked</#if>></td>
			<td width="220" class="peakfinder_table_norma_text">Glycopeptide</td>
			<td colspan="2" class="peakfinder_table_norma_text"><!--Enzyme :&nbsp;&nbsp;

				<select name="settings.enzyme" size="1">
						<option value="1" >none</option>
						<option value="2" >Trypsin</option>
						<option value="3" >Pronase</option>
						<option value="3" >nur ein weiteres Enzym, umd</option>
				</select>-->
			</td>

		</tr>
        <tr><TD colspan="3" height="5"></TD></tr>		
		<tr>
			<td width="10" class="peakfinder_table_contens_left"></td>
			<td width="30"></td>
			<td width="100" class="peakfinder_table_norma_text"></td>
			<td width="30"></td>
			<td colspan="3" class="peakfinder_table_norma_text">Peptide sequence :&nbsp;&nbsp;
                        <textarea rows="4" name="settings.asSequence" cols="73" class="gpf_peaklist">${settings.asSequence}</textarea>

			</td>
		</tr>
        <tr><TD colspan="3" height="10"></TD></tr>
<TR>
    <td colspan="7" align="center">
        <tablE cellpadding="0" cellspacing="0" border="0" >
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
				<input class="peakfinder_button" type="button" value="Help" name="help" onClick="window.open('./help/GlycoPeakfinder_mod_help.html')">
					    					</TD>
        				</TR>

        				</tablE>
    				</td>
				</TR>	    <tr><TD colspan="3" height="10"></TD></tr>
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