<#include "../header.ftl">

<h2>Glyco-Peakfinder</h2>

<form method="POST" action="${baseUrl}/Result.action" name="values">
  <input type="text" class="gpf_information" height="1" name="pageTo" value=""/>
  <input type="text" class="gpf_information" height="1" name="pageFrom" value="info"/>
  
<div id="gpf_tabs">
  <ul>
    <li><a href="#" onclick="switchToPage('resu');">Result</a></li>
    <li><a href="#" onclick="switchToPage('stru');">Structures</a></li>
    <li class="current"><a href="#">Settings</a>  
  </ul>
  <div></div>
</div>
<table border="0" cellpadding="0" cellspacing="0" width="702">
<TR>
    <TD class="gpf_table_left"/>
    <td width="700">
        <table cellpadding="0" cellpadding="0" border="0" width="700">
<!-- Start of data areal -->
<tr><TD colspan="6" height="10"/></tr>
<tr>
    <TD width="250" class="gpf_contens_b_right" valign="top">spectra type :</TD>
    <TD width="10"/>
    <TD width="230" class="gpf_contens_left" valign="top">
		<#if settings.spectraType = "profile">profile</#if>     
		<#if settings.spectraType = "fragmented">fragmented</#if>
		<#if settings.spectraType = "ms2">MS<sup>2</sup></#if>
    </TD>
    <TD width="100" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="100" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="10"/>
</tr>
<tr><TD colspan="6" height="10"/></tr>
<tr>
    <TD width="250" class="gpf_contens_b_right" valign="top">masstype :</TD>
    <TD width="10"/>
    <TD width="230" class="gpf_contens_left" valign="top">
		<#if settings.massType = "mono">monoisotopic<#else>average</#if>    
    </TD>
    <TD width="100" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="100" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="10"/>
</tr>
<tr><TD colspan="6" height="10"/></tr>
<tr>
    <TD width="250" class="gpf_contens_b_right" valign="top">accuracy :</TD>
    <TD width="10"/>
    <TD width="230" class="gpf_contens_left" valign="top">
		${settings.accuracy} ${settings.accuracyType}     
    </TD>
    <TD width="100" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="100" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="10"/>
</tr>
<tr><TD colspan="6" height="10"/></tr>
<tr>
    <TD width="250" class="gpf_contens_b_right" valign="top">mass shift</TD>
    <TD width="10"/>
    <TD width="230" class="gpf_contens_left" valign="top">
		${settings.massShift} u     
    </TD>
    <TD width="100" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="100" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="10"/>
</tr>
<tr><TD colspan="6" height="10"/></tr>
<tr>
    <TD width="250" class="gpf_contens_b_right" valign="top">modification of complete structure :</TD>
    <TD width="10"/>
    <TD width="230" class="gpf_contens_left" valign="top">
		<#if settings.perSubstitution = "none">none</#if>
		<#if settings.perSubstitution = "pme">per-Methylation</#if>
		<#if settings.perSubstitution = "pdme">per-Deutero-Methylation</#if>
		<#if settings.perSubstitution = "pac">per-Acetylation</#if>
		<#if settings.perSubstitution = "pdac">per-Deutero-Acetylation</#if>
    </TD>
    <TD width="100" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="100" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="10"/>
</tr>
<tr><TD colspan="6" height="10"/></tr>
<tr>
    <TD width="250" class="gpf_contens_b_right" valign="top">reducing end:</TD>
    <TD width="10"/>
    <TD width="230" class="gpf_contens_left" valign="top">
		${settings.derivatisation}
    </TD>
    <TD width="100" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="100" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="10"/>
</tr>
<tr><TD colspan="6" height="10"/></tr>
<tr>
    <TD width="250" class="gpf_contens_b_right" valign="top">residues :</TD>
    <TD width="10"/>
    <TD width="230" class="gpf_contens_left" valign="top">
<#list usedResidue as residue>
    ${residue.abbr}
    </TD>
    <TD width="100" class="gpf_contens_left" valign="top">${residue.min} - ${residue.max}</TD>
    <TD width="100" class="gpf_contens_right" valign="top">${residue.mass}</TD>
    <TD width="10"/>
</tr>
<tr>
    <TD width="250" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="10"/>
    <TD width="230" class="gpf_contens_left" valign="top">
</#list>
    </TD>
    <TD width="100" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="100" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="10"/>
</tr>
<tr><TD colspan="6" height="10"/></tr>
<tr>
    <TD width="250" class="gpf_contens_b_right" valign="top">charged ions :</TD>
    <TD width="10"/>
    <TD width="230" class="gpf_contens_left" valign="top">
<#list settings.ionList as ions>
<#if ions.used = true>
     ${ions.abbr}
    </TD>
    <TD width="100" class="gpf_contens_right" valign="top"></TD>
    <TD width="100" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="10"/>
</tr>
<tr>
    <TD width="250" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="10"/>
    <TD width="230" class="gpf_contens_left" valign="top">
</#if>
</#list>
    </TD>
    <TD width="100" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="100" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="10"/>
</tr>
<#if settings.otherIonBool = "true">
<tr>
    <TD width="250" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="10"/>
    <TD width="230" class="gpf_contens_left" valign="top">
    ${settings.otherIonName}
    </TD>
    <TD width="100" class="gpf_contens_left" valign="top"></TD>
    <TD width="100" class="gpf_contens_right" valign="top">${settings.otherIonMass}</TD>
    <TD width="10"/>
</tr>
</#if>
<tr><TD colspan="6" height="10"/></tr>
<tr>
    <TD width="250" class="gpf_contens_b_right" valign="top">charge state :</TD>
    <TD width="10"/>
    <TD width="230" class="gpf_contens_left" valign="top">
<#list settings.chargeList as charge>
<#if charge.used = true>
     ${charge.abbr}
    </TD>
    <TD width="100" class="gpf_contens_right" valign="top"></TD>
    <TD width="100" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="10"/>
</tr>
<tr>
    <TD width="250" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="10"/>
    <TD width="230" class="gpf_contens_left" valign="top">
</#if>
</#list>
    </TD>
    <TD width="100" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="100" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="10"/>
</tr>
<tr><TD colspan="6" height="10"/></tr>
<tr>
<tr>
    <TD width="250" class="gpf_contens_b_right" valign="top">exchange ions :</TD>
    <TD width="10"/>
    <TD width="230" class="gpf_contens_left" valign="top">
<#list settings.ionExchangeIonList as ions>
<#if ions.used = true>
     ${ions.abbr}
    </TD>
    <TD width="100" class="gpf_contens_right" valign="top"></TD>
    <TD width="100" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="10"/>
</tr>
<tr>
    <TD width="250" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="10"/>
    <TD width="230" class="gpf_contens_left" valign="top">
</#if>
</#list>
    </TD>
    <TD width="100" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="100" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="10"/>
</tr>
<#if settings.otherIonExchangeBool = "true">
<tr>
    <TD width="250" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="10"/>
    <TD width="230" class="gpf_contens_left" valign="top">
    ${settings.otherIonExchangeIonName}
    </TD>
    <TD width="100" class="gpf_contens_left" valign="top"></TD>
    <TD width="100" class="gpf_contens_right" valign="top">${settings.otherIonExchangeIonMass}</TD>
    <TD width="10"/>
</tr>
</#if>
<tr><TD colspan="6" height="10"/></tr>
<tr>
    <TD width="250" class="gpf_contens_b_right" valign="top">ion exchange count :</TD>
    <TD width="10"/>
    <TD width="230" class="gpf_contens_left" valign="top">
<#list settings.ionExchangeCountList as charge>
<#if charge.used = true>
     ${charge.abbr}
    </TD>
    <TD width="100" class="gpf_contens_right" valign="top"></TD>
    <TD width="100" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="10"/>
</tr>
<tr>
    <TD width="250" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="10"/>
    <TD width="230" class="gpf_contens_left" valign="top">
</#if>
</#list>
    </TD>
    <TD width="100" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="100" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="10"/>
</tr>
<tr><TD colspan="6" height="10"/></tr>
<#if settings.spectraType != "profile">
<tr>
    <TD width="250" class="gpf_contens_b_right" valign="top">fragments :</TD>
    <TD width="10"/>
    <TD width="230" class="gpf_contens_left" valign="top">
<#list settings.fragmentTypeList as frag>
<#if frag.used = true>
     ${frag.abbr}
    </TD>
    <TD width="100" class="gpf_contens_right" valign="top"></TD>
    <TD width="100" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="10"/>
</tr>
<tr>
    <TD width="250" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="10"/>
    <TD width="230" class="gpf_contens_left" valign="top">
</#if>
</#list>
    </TD>
    <TD width="100" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="100" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="10"/>
</tr>
<tr><TD colspan="6" height="10"/></tr>
</#if>
<tr>
    <TD width="250" class="gpf_contens_b_right" valign="top">lose/gain of molecules:</TD>
    <TD width="10"/>
    <TD width="230" class="gpf_contens_left" valign="top">
<#list settings.lossGainMolecules as mol>
<#if (mol.gain > 0) || (mol.loss > 0) >
     ${mol.name} ( ${mol.abbr} ) 
    </TD>
    <TD width="100" class="gpf_contens_left" valign="top"><#if (mol.loss > 0) >lose 0-${mol.loss}<br></#if><#if (mol.gain > 0) >gain 0-${mol.gain}</#if></TD>
    <TD width="100" class="gpf_contens_right" valign="top"><#if settings.massType = "mono">${mol.massMono}<#else>${mol.massAvg}</#if></TD>
    <TD width="10"/>
</tr>
<tr>
    <TD width="250" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="10"/>
    <TD width="230" class="gpf_contens_left" valign="top">
</#if>
</#list>    
    </TD>
    <TD width="100" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="100" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="10"/>
</tr>
<#if (settings.otherLossGainMolecule.gain > 0 ) || (settings.otherLossGainMolecule.loss > 0)>
<tr>
    <TD width="250" class="gpf_contens_b_right" valign="top"></TD>
    <TD width="10"/>
    <TD width="230" class="gpf_contens_left" valign="top">
    ${settings.otherLossGainMolecule.name}
    </TD>
    <TD width="100" class="gpf_contens_left" valign="top"><#if (settings.otherLossGainMolecule.loss > 0) >lose 0-${settings.otherLossGainMolecule.loss}<br></#if><#if (settings.otherLossGainMolecule.gain > 0) >gain 0-${settings.otherLossGainMolecule.gain}</#if></TD>
    <TD width="100" class="gpf_contens_right" valign="top">${settings.otherLossGainMolecule.mass}</TD>
    <TD width="10"/>
</tr>
</#if>


<tr><TD colspan="6" height="10"/></tr>

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