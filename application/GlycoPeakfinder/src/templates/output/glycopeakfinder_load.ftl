<#include "../header.ftl">

<table border="0" cellpadding="0" cellspacing="0" width="702">
<tr><TD class="gpf_heading" colspan="3">Glyco-Peakfinder</TD></tr>
<tr>
    <TD colspan="3" height="15">
        <input type="text" class="gpf_information" height="1" name="pageTo" value="">
        <input type="text" class="gpf_information" height="1" name="pageFrom" value="info">
    </TD></tr>
<TR>
    <TD colspan="3" class="gpf_table_top"/>
</TR>
<TR>
    <TD class="gpf_table_left"/>
    <td bgcolor="#cddcec" width="700" align="center">
		<form method="POST" action="${baseUrl}/LoadResult.action" name="values" enctype="multipart/form-data">
			<table>
				<tr><TD colspan="5" height="10"/></tr>
				<tr>
					<TD width="60"></TD>
				    <TD class="gpf_contens_b_left">Specify file type :</td>
                    <TD class="peakfinder_table_norma_text" colspan="2">
						<input align="middle" size="21" type="file" name="loadResult" class="peakfinder_eingabe">
			        </TD>
                    <TD width="30"></TD>
                </tr>
				<tr><TD colspan="5" height="10"/></tr>
                <tr>
					<TD width="60"></TD>
				    <TD class="gpf_contens_b_left">Specify file type :</td>
                    <TD class="peakfinder_table_norma_text" colspan="2">
						    <select name="fileExtension" size="1">
						    	<option value="gpxml">GlycoPeakfinder file (*.xml)</option>
						    </select>
                    </TD>
                    <TD width="30"></TD>
                </tr>
				<tr><TD colspan="5" height="10"/></tr>
				<tr><TD colspan="5" align="center">
					<input class="peakfinder_stecial_button" value="Load" type="submit">
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