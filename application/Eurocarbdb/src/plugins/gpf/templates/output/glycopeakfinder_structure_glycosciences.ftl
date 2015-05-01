<#include "../header.ftl">

<h2>Glyco-Peakfinder</h2>

<form method="POST" action="${baseUrl}/Result.action" name="values">
  <input type="text" class="gpf_information" height="1" name="pageTo" value=""/>
  <input type="text" class="gpf_information" height="1" name="pageFrom" value="stru"/>

<div id="gpf_tabs">
  <ul>
    <li><a href="#" onclick="switchToPage('resu');">Result</a></li>
    <li class="current"><a href="#">Structures</a></li>
    <li><a href="#" onclick="switchToPage('sett');">Settings</a>  
  </ul>
  <div></div>
</div>
<table border="0" cellpadding="0" cellspacing="0" width="702">
<TR>
    <TD class="gpf_table_left"/>
    <td width="700">

<!-- Start of data areal -->
        <table cellpadding="4" cellpadding="2" border="0" width="700">
			<tr bgcolor="#ffffcc">
				<td class="peakfinder_table_contens_left" width="90">Searched<br>composition:</td>
				<td class="peakfinder_table_contens_left">${dbResult.composition}</td>
				<td class="peakfinder_table_contens_left" width="45">Mass:</td>
				<td class="peakfinder_table_contens_left" width="60">${dbResult.mass}</td>
                <td class="peakfinder_table_contens_left" width="75">Found<br>structures:</td>
                <td class="peakfinder_table_contens_left" width="60">${dbResult.entryCount}</td>
			</tr>
        </table>
        <table cellpadding="0" cellpadding="0" border="0" width="700">		
			<tr>
				<td width="100"/>
				<td width="100" align="center"><#if dbResult.currentPage != 1><a href="./Result.action?pageTo=stru&searchPage=${dbResult.previewPage}" border="0"><img border="0" src="./images/pfeil_left.png"/></a></#if></td>
				<td width="100" class="peakfinder_table_contens_left" style="vertical-align:middle;">Current page : ${dbResult.currentPage}</td>
				<td width="200" align="center">
				<select name="searchPage" size="1">
				<#list dbResult.pages as entry>
					<option value="${entry}" <#if dbResult.currentPage = entry>selected</#if>>${entry}</option>
				</#list>
			    </select>
				<input class="peakfinder_stecial_button" type="submit" value="Go to Page">	
				</td>
				<td width="100" align="center"><#if dbResult.currentPage != dbResult.lastPage><a href="./Result.action?pageTo=stru&searchPage=${dbResult.nextPage}" border="0"><img border="0" src="./images/pfeil_right.png"/></a></#if></td>
				<td width="100"/>
			</tr>
        </table>			
        <table cellpadding="4" cellpadding="2" border="0" width="700">		
			<tr>
				<td class="peakfinder_table_contens_left" width="53">Linux ID</td>
				<td class="peakfinder_table_contens_left" >2D-Plot of structure</td>
			</tr>
        </table>			
        <table cellpadding="4" cellpadding="0" border="0" width="700">		
	<#list dbResult.entry as structures>				
			<tr>
				<td class="gpf_text_center" width="55" bgcolor="#ffffff" align="center" rowspan="2"><a href="http://www.glycosciences.de/sweetdb/start.php?action=explore_linucsid&linucsid=${structures.linucs}" target="_blank">${structures.linucs}</a></td>
				<td bgcolor="#ffffff" align="center">
					<iframe src="./GetGlycosciencesStructure.action?id=${structures.linucs}" width="590" height="${structures.height}">
					  <textarea rows="10" name="${structures.linucs}" cols="75" class="gpf_iupac_structure">${structures.iupac}</textarea>	
					  </iframe>	
					<!-- img src="get_sugar_image.action?download=true&scale=0.5&outputType=png&inputType=gwlinucs&tolerateUnknown=1&sequences=${structures.linucsCode?url}"/ -->
				</td>
			</tr>
			<tr>
				<td bgcolor="#ffffff">
					<a href="./GetSugarCode.action?type=linucs&id=${structures.linucs}"> [Linucs File] </a> &nbsp;&nbsp;
					<a href="./GetSugarCode.action?type=iupac&id=${structures.linucs}"> [IUPAC 2D Graph File] </a>
				</td>
			</tr>
	</#list>
        </table>
              <table cellpadding="0" cellpadding="0" border="0" width="700">		
			<tr>
				<td width="100"/>
				<td width="100" align="center"><#if dbResult.currentPage != 1><a href="./Result.action?pageTo=stru&searchPage=${dbResult.previewPage}" border="0"><img border="0" src="./images/pfeil_left.png"/></a></#if></td>
				<td width="100" class="peakfinder_table_contens_left" style="vertical-align:middle;">Current page : ${dbResult.currentPage}</td>
				<td width="200" align="center">
				</td>
				<td width="100" align="center"><#if dbResult.currentPage != dbResult.lastPage><a href="./Result.action?pageTo=stru&searchPage=${dbResult.nextPage}" border="0"><img border="0" src="./images/pfeil_right.png"/></a></#if></td>
				<td width="100"/>
			</tr>
        </table>			
      <p/>
<!-- End of data areal -->

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
            <TD width="130"></TD>
            <TD width="160">
	            <input class="peakfinder_button" type="button" onClick="window.location.href='Input.action?pageTo=mass'" value="Change settings">
            </TD>
            <TD width="120"></TD>
            <TD width="160">
	            <input class="peakfinder_button" type="button" onClick="window.location.href='Reset.action'" value="New calculation">
            </TD>
            <TD width="130"></TD>
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