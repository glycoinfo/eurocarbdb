<#include "../header.ftl">


<h2>Glyco-Peakfinder</h2>

<form method="POST" action="${baseUrl}/Input.action" name="values">
  <input type="text" class="gpf_information" height="1" name="pageTo" value=""/>
  <input type="text" class="gpf_information" height="1" name="pageFrom" value="info"/>

<div id="gpf_tabs">
  <ul>   
    <li><a href="#">Mass</a></li>
    <li><a href="#">Residue</a></li>
    <li><a href="#">Ion/charge</a></li>
    <li><a href="#">Modifications</a></li>
  </ul>
  <div></div>
</div>   
<table border="0" cellpadding="0" cellspacing="0" width="702">
<TR>
    <TD class="gpf_table_left"/>
    <td width="700">
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
                <input class="peakfinder_button" type="button" name="anno" value="Back" onclick="js:setFromPage('');js:switchToPage('${pageFrom}',1);">
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