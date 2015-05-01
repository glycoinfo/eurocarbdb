<#include "../header.ftl">


<h2>Glyco-Peakfinder</h2>

<input type="text" class="gpf_information" height="1" name="pageTo" value=""/>
<input type="text" class="gpf_information" height="1" name="pageFrom" value="info"/>

<div id="gpf_tabs">
  <ul>
    <li><a href="Result.action?pageTo=resu">Result</a></li>
    <li><a href="Result.action?pageTo=stru">Structures</a></li>
    <li><a href="Result.action?pageTo=sett">Settings</a>  
  </ul>
  <div></div>
</div>
<table border="0" cellpadding="0" cellspacing="0" width="702">
<TR>
    <TD class="gpf_table_left"/>
    <td width="700" align="center">
		<form method="POST" action="${baseUrl}/SaveResult.action" name="values">
			<table>
				<tr><TD colspan="5" height="10"/></tr>
                <tr>
					<TD width="60"></TD>
				    <TD class="gpf_contens_b_left">Specify file type :</td>
                    <TD class="peakfinder_table_norma_text" colspan="2">
						    <select name="fileExtension" size="1">
						    	<option value="gpxml">GlycoPeakfinder file (*.xml)</option>
						    	<option value="gpxls">Excel report (*.xls)</option>
						    </select>
                    </TD>
                    <TD width="30"></TD>
                </tr>
				<tr><TD colspan="5" height="10"/></tr>
				<tr><TD colspan="5" align="center">
					<input class="peakfinder_stecial_button" value="Save & download" type="submit">
				</TD></tr>
                <tr><TD colspan="5" height="10"/></tr>                
            </table>
       </form>    
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


<#include "../footer.ftl">