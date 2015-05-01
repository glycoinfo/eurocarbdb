<#include "./header.ftl">


<table border="0" cellpadding="0" cellspacing="0" width="702">
<tr><TD class="gpf_heading" colspan="3">Glyco-Peakfinder</TD></tr>
<tr>
    <TD colspan="3" height="15">
    </TD></tr>
<TR>
    <TD colspan="3" class="gpf_table_top"/>
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
    <td class="peakfinder_table_headline">${errorInformation.title}</td>
    <TD width="20"/>
</tr>
<tr>
    <TD colspan="3" height="20"/>
</tr>
<tr>
    <TD width="20"/>
    <td class="gpf_contens_b_left">${errorInformation.text}</td>
    <TD width="20"/>
</tr>
<tr>
    <TD colspan="3" height="20"/>
</tr>
<tr>
    <TD width="20"/>
    <TD class="peakfinder_table_norma_text">
        <ul>
        <#list errorInformation.errors as x>
        	<li>${x}</li>
        </#list>
        </ul>
    </TD>
    <TD width="20"/>
</tr>
<tr>
    <TD colspan="3" height="20"/>
</tr>
<tr>
    <TD colspan="3" align="center">
    	<input class="peakfinder_button" type="button" name="anno" value="Back" onclick="window.location.href='${errorInformation.backUrl}'">
    </TD>
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
</table>


<#include "./footer.ftl">