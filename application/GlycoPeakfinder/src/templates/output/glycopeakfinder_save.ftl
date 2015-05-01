<#include "../header.ftl">

<table border="0" cellpadding="0" cellspacing="0" width="702">
<tr><TD class="gpf_heading" colspan="3">Glyco-Peakfinder</TD></tr>
<tr>
    <TD colspan="3" height="15">
        <input type="text" class="gpf_information" height="1" name="pageTo" value="">
        <input type="text" class="gpf_information" height="1" name="pageFrom" value="info">
    </TD></tr>
<TR>
    <TD class="gpf_table_top_left"/>
    <td>
        <table border="0" cellpadding="0" cellspacing="0" width="700">
            <TD class="gpf_table_menu_left"/>
            <TD class="gpf_table_menu_button" width="95"><a href="${baseUrl}/Result.action?pageTo=resu"><img border="0" src="${baseUrl}/images/result_unselect.jpg" onmouseover="getElementById('menu_result').src='${baseUrl}/images/result_over.jpg'" onmouseout="getElementById('menu_result').src='${baseUrl}/images/result_unselect.jpg'" id='menu_result' width="100%"></a></TD>
            <TD class="gpf_table_menu_space" width="20"/>
            <TD class="gpf_table_menu_button" width="95"><a href="${baseUrl}/Result.action?pageTo=stru"><img border="0" src="${baseUrl}/images/structures_unselect.jpg" onmouseover="getElementById('menu_stru').src='${baseUrl}/images/structures_over.jpg'" onmouseout="getElementById('menu_stru').src='${baseUrl}/images/structures_unselect.jpg'" id='menu_stru' width="100%"></a></TD>
            <TD class="gpf_table_menu_space" width="20"/>
            <TD class="gpf_table_menu_button" width="95"><a href="${baseUrl}/Result.action?pageTo=modi"><img border="0" src="${baseUrl}/images/settings_unselect.jpg" onmouseover="getElementById('menu_sett').src='${baseUrl}/images/settings_over.jpg'" onmouseout="getElementById('menu_sett').src='${baseUrl}/images/settings_unselect.jpg'" id='menu_sett' width="100%"></a></TD>
            <TD class="gpf_table_menu_space" width="345"/>
            <TD class="gpf_table_menu_right"/>
        </table>
    </td>
    <TD class="gpf_table_top_right"/>
</TR>
<TR>
    <TD class="gpf_table_left"/>
    <td bgcolor="#cddcec" width="700" align="center">
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