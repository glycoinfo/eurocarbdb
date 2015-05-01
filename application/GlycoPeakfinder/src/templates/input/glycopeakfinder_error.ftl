<#include "../header.ftl">

<form method="POST" action="${baseUrl}/Input.action" name="values">

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
    	<tr>
            <TD class="gpf_table_menu_left"/>
            <TD class="gpf_table_menu_button" width="95"><img border="0" src="./images/mass_unselect.jpg" id='menu_mass' width="100%"></TD>
            <TD class="gpf_table_menu_space" width="20"/>
            <TD class="gpf_table_menu_button" width="95"><img border="0" src="./images/residue_unselect.jpg" id='menu_resi' width="100%"></TD>
            <TD class="gpf_table_menu_space" width="20"/>
            <TD class="gpf_table_menu_button" width="95"><img border="0" src="./images/ion_unselect.jpg" id='menu_ions' width="100%"></TD>
            <TD class="gpf_table_menu_space" width="20"/>
            <TD class="gpf_table_menu_button" width="95"><img border="0" src="./images/modifications_unselect.jpg" id='menu_modi' width="100%"></TD>
            <TD class="gpf_table_menu_space" width="230"/>
            <TD class="gpf_table_menu_right"/>
        </tr>
        </table>
    </td>
    <TD class="gpf_table_top_right"/>
</TR>
<TR>
    <TD class="gpf_table_left"/>
    <td bgcolor="#cddcec" width="700">
        <table cellpadding="0" cellpadding="0" border="0" width="700">
<!-- Start of data areal -->

<tr>
    <TD colspan="3" height="20"/>
</tr>
<tr>
    <TD width="20"/>
    <td class="peakfinder_table_headline">Some of the data that you have entered are unfortunately still not valid :</td>
    <TD width="20"/>
</tr>
<tr>
    <TD colspan="3" height="20"/>
</tr>
<tr>
    <TD width="20"/>
    <TD class="peakfinder_table_norma_text">
        <ul>
        <#list settings.errorList as x>
        	<li>${x}</li>
        </#list>
        </ul>
    </TD>
    <TD width="20"/>
</tr>
<tr>
    <TD colspan="3" height="20"/>
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
            <TD width="55"></TD>
            <TD width="160">
            </TD>
            <TD width="55"></TD>
            <TD width="160">
                <input class="peakfinder_button" type="button" name="anno" value="Back" onclick="js:switchToPage('${pageFrom}');">
            </TD>
            <TD width="55"></TD>
            <TD width="160">
            </TD>
            <TD width="55"></TD>
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