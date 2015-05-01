<#include "../header.ftl">
  
<h2>Glyco-Peakfinder</h2>

<form method="POST" action="Input.action" name="values" enctype="multipart/form-data">
  <input type="text" class="gpf_information" height="1" name="pageTo" value=""/>
  <input type="text" class="gpf_information" height="1" name="pageFrom" value="mass"/>

<div id="gpf_tabs">
  <ul>   
    <li class="current"><a href="#">Mass</a></li>
    <li><a href="#" onclick="js:clearFileSettings('true');switchToPage('resi');">Residue</img></li>
    <li><a href="#" onclick="js:clearFileSettings('true');switchToPage('ions');">Ion/charge</a></li>
    <li><a href="#" onclick="js:clearFileSettings('true');switchToPage('modi');">Modifications</a></li>
  </ul>
  <div></div>
</div>
<table border="0" cellpadding="0" cellspacing="0" width="702">    
<TR>
    <TD class="gpf_table_left"/>
    <td width="700">
        <table cellpadding="0" cellpadding="0" border="0" width="700">
<!-- Start of data areal -->
                <tr><TD colspan="5" height="10"></TD></tr>
				<TR>
    				<td colspan="5" align="center">
        				<table cellpadding="0" cellspacing="0" border="0" >
 						<TR>
            				<TD width="80">
								<input class="peakfinder_button" type="button" name="annotate" value="Annotate peaks" onclick="js:clearFileSettings('true');switchToPage('calc');">
            				</TD>
            				<TD width="40"></TD>
            				<TD width="80">
	            				<input class="peakfinder_button" type="button" onClick="window.location.href='Reset.action'" value="Reset all settings">
            				</TD>
            				<TD width="40"></TD>
            				<TD width="80">
								<input class="peakfinder_button" type="button" name="resetform" value="Reset form" onclick="js:clearFileSettings('true');switchToPage('reset');">
            				</TD>
            				<TD width="40"></TD>
            				<TD width="80">
								<input class="peakfinder_button" type="button" value="Help" name="help" onClick="window.open('./help/GlycoPeakfinder_mass_help.html')">
	    					</TD>
        				</TR>
        				</tablE>
    				</td>
    				<TD >
    				</TD>
				</TR>
                <tr><TD colspan="5" height="10"></TD></tr>
                <TR>
                    <TD class="peakfinder_table_headline" colspan="5">
                        Enter spectrum parameter
                    </TD>
                </TR>
                <tr><TD colspan="5" height="20"></TD></tr>
                <tr>
                    <TD width="60"></TD>
				    <TD class="gpf_contens_b_left">Specify spectrum type:</td>
                    <TD class="gpf_contens_left"><input class="gp_radio_check" type="radio" name="settings.spectraType" value="profile" <#if settings.spectraType = "profile">checked</#if>> Glycan Profile</TD>
                    <TD width="30"></TD>
                    <TD width="30"></TD>
                </tr>
                <tr>
                    <TD width="60"></TD>
				    <TD class="gpf_contens_left"></td>
                    <TD class="gpf_contens_left"><input class="gp_radio_check" type="radio" name="settings.spectraType" value="ms2" <#if settings.spectraType = "ms2">checked</#if>> MS<sup>2</sup></TD>                    
                    <TD width="30"></TD>
                    <TD width="30"></TD>
                </tr>
                <tr>
                    <TD width="60"></TD>
				    <TD class="gpf_contens_left"></td>
                    <TD class="gpf_contens_left"><input class="gp_radio_check" type="radio" name="settings.spectraType" value="fragmented" <#if settings.spectraType = "fragmented">checked</#if>> Fragment Spectrum</TD>                    
                    <TD width="30"></TD>
                    <TD width="30"></TD>
                </tr>
                <tr><TD colspan="5" height="20"></TD></tr>
                <tr>
                    <TD width="60"></TD>
		            <TD class="gpf_contens_b_left">Accuracy of entered masses: </td>
		    		<TD class="gpf_contens_left"><input type="text" name="settings.accuracy" size="15" value="${settings.accuracy}">&nbsp;&nbsp;
		            <select name="settings.accuracyType" size="1">
<#if settings.accuracyType = "ppm">		            
						<option value="ppm" selected>ppm</option>
						<option value="u">u</option>						
<#else>
						<option value="ppm">ppm</option>
						<option value="u" selected>u</option>						
</#if>						
			        </select>
		            </TD>
				    <TD width="30"></TD>
				    <TD width="30"></TD>
                </tr>
                <tr><TD colspan="5" height="20"></TD></tr>
                <tr>
                    <TD width="60"></TD>
				    <TD class="gpf_contens_b_left">Select mass values for calculation: </td>
<#if settings.massType = "mono">                    
                    <TD class="gpf_contens_left"><input class="gp_radio_check" type="radio" name="settings.massType" value="mono" checked> monoisotopic</TD>
                    <TD width="30"></TD>
                    <TD width="30"></TD>
                </tr>
                <tr>
                    <TD width="60"></TD>
				    <TD class="gpf_contens_left"></td>
				    <TD class="gpf_contens_left"><input class="gp_radio_check" type="radio" name="settings.massType" value="avg"> average</TD>
<#else>
                    <TD class="gpf_contens_left"><input class="gp_radio_check" type="radio" name="settings.massType" value="mono"> monoisotopic</TD>
                    <TD width="30"></TD>
                    <TD width="30"></TD>
                </tr>
                <tr>
                    <TD width="60"></TD>
				    <TD class="gpf_contens_left"></td>
				    <TD class="gpf_contens_left"><input class="gp_radio_check" type="radio" name="settings.massType" value="avg" checked> average</TD>
</#if>                      
                    <TD width="30"></TD>
                    <TD width="30"></TD>
                </tr>
                <tr><TD colspan="5" height="20"></TD></tr>
                <TR>
                    <TD class="peakfinder_table_headline" colspan="5">
                        Enter mass list
                    </TD>
                </TR>
                <tr><TD colspan="5" height="20"></TD></tr>
                <tr>
					<TD width="60"></TD>
				    <TD class="gpf_contens_b_left">Select file from file system:</td>
                    <TD class="peakfinder_table_norma_text" colspan="2">
                            <input align="middle" size="21" type="file" name="myFile" class="peakfinder_eingabe">
                    </TD>
                    <TD width="30"></TD>
                </tr>
                <tr><TD colspan="5" height="5"></TD></tr>
                <tr>
					<TD width="60"></TD>
				    <TD class="gpf_contens_b_left">Specify file type and upload file:</td>
                    <TD class="peakfinder_table_norma_text" colspan="2">
						    <select name="fileExtension" size="1">
						      <option value="txt" selected>Two column ASCII File (*.txt)</option>
						      <option value="flexAnalysis">flexAnalysis 2.4.11.0 (*.xml)</option>
						    </select>
                            <input type="button" name="hochladen" value="Upload" onclick="js:setFromPage('file');clearFileSettings('false');switchToPage('mass');">
                    </TD>
                    <TD width="30"></TD>
                </tr>
                <tr><TD colspan="5" height="10"></TD></tr>                
                <tr class="peakfinder_table_norma_text">
                    <TD width="60"></TD>
		    <TD class="gpf_contens_b_left" valign="top">
			<div class="gpf_contens_b_left">Or enter peak list manually:</div><br>
			<div class="kai_contens_justify_gray">
				The peak list format looks like:<div><br>
			<div class="kai_contens_center_gray">
				2398.6453 99.765<div><br>
			<div class="kai_contens_justify_gray">
				First value: m/z<br>
				Separator: space<br>
				Second value: intensity<br>
				(can be omitted)</div>
			</div>
		    </td>
                    <TD class="peakfinder_table_norma_text" colspan="2">
                        <textarea rows="10" name="settings.peakList" cols="50" class="gpf_peaklist">${settings.peakList}</textarea>
                    </TD>
                    <TD width="30"></TD>
                </tr>
                <tr><TD colspan="5" height="10"></TD></tr>
                <tr>
                    <TD width="60"></TD>
		            <TD class="gpf_contens_b_left">Mass shift of the complete spectrum: </td>
		    		<TD class="gpf_contens_left"><input type="text" name="settings.massShift" size="15" value="${settings.massShift}">&nbsp;u&nbsp;
		            </TD>
				    <TD width="30"></TD>
				    <TD width="30"></TD>
                </tr>
                <tr><TD colspan="5" height="10"></TD></tr>
                <tr>
                    <TD width="60"></TD>
		            <TD class="gpf_contens_b_left">Precursor of the sprectrum (only for MS<sup>2</sup>): </td>
		    		<TD class="gpf_contens_left"><input type="text" name="settings.precursor" size="15" value="${settings.precursor}">&nbsp;u&nbsp;
		            </TD>
				    <TD width="30"></TD>
				    <TD width="30"></TD>
                </tr>
                <tr><TD colspan="5" height="10"></TD></tr>
                <tr>
                    <TD width="60"></TD>
		            <TD class="gpf_contens_b_left">Max. Number of Annotations per Peak: </td>
		    		<TD class="gpf_contens_left"><input type="text" name="settings.annotationsPerPeak" size="15" value="${settings.annotationsPerPeak}">
		            </TD>
				    <TD width="30"></TD>
				    <TD width="30"></TD>
                </tr>
                <tr><TD colspan="5" height="20"></TD></tr>
				<TR>
    				<td colspan="5" align="center">
        				<table cellpadding="0" cellspacing="0" border="0" >
 						<TR>
            				<TD width="80">
								<input class="peakfinder_button" type="button" name="annotate" value="Annotate peaks" onclick="js:clearFileSettings('true');switchToPage('calc');">
            				</TD>
            				<TD width="40"></TD>
            				<TD width="80">
	            				<input class="peakfinder_button" type="button" onClick="window.location.href='Reset.action'" value="Reset all settings">
            				</TD>
            				<TD width="40"></TD>
            				<TD width="80">
								<input class="peakfinder_button" type="button" name="resetform" value="Reset form" onclick="js:clearFileSettings('true');switchToPage('reset');">
            				</TD>
            				<TD width="40"></TD>
            				<TD width="80">
				<input class="peakfinder_button" type="button" value="Help" name="help" onClick="window.open('./help/GlycoPeakfinder_mass_help.html')">
	    					</TD>
        				</TR>
        				</tablE>
    				</td>
    				<TD >
    				</TD>
				</TR>
                <tr><TD colspan="5 " height="10"></TD></tr>

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
</table>

</form>

<#include "../footer.ftl">