<#include "./header.ftl">

<form method="POST" action="./GlycoPeakfinder.action" name="values">

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
<#if result.initialized = true>
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
<#else>
		<table border="0" cellpadding="0" cellspacing="0" width="700">
		<tr>
            <TD class="gpf_table_menu_left"/>
            <TD class="gpf_table_menu_button" width="95"><a href="${baseUrl}/Input.action?pageTo=mass"><img border="0" src="${baseUrl}/images/mass_unselect.jpg" onmouseover="getElementById('menu_mass').src='${baseUrl}/images/mass_over.jpg'" onmouseout="getElementById('menu_mass').src='${baseUrl}/images/mass_unselect.jpg'" id='menu_mass' width="100%"></a></TD>
            <TD class="gpf_table_menu_space" width="20"/>
            <TD class="gpf_table_menu_button" width="95"><a href="${baseUrl}/Input.action?pageTo=resi"><img border="0" src="${baseUrl}/images/residue_unselect.jpg" onmouseover="getElementById('menu_resi').src='${baseUrl}/images/residue_over.jpg'" onmouseout="getElementById('menu_resi').src='${baseUrl}/images/residue_unselect.jpg'" id='menu_resi' width="100%"></a></TD>
            <TD class="gpf_table_menu_space" width="20"/>
            <TD class="gpf_table_menu_button" width="95"><a href="${baseUrl}/Input.action?pageTo=ions"><img border="0" src="${baseUrl}/images/ion_unselect.jpg" onmouseover="getElementById('menu_ions').src='${baseUrl}/images/ion_over.jpg'" onmouseout="getElementById('menu_ions').src='${baseUrl}/images/ion_unselect.jpg'" id='menu_ions' width="100%"></a></TD>
            <TD class="gpf_table_menu_space" width="20"/>
            <TD class="gpf_table_menu_button" width="95"><a href="${baseUrl}/Input.action?pageTo=modi"><img border="0" src="${baseUrl}/images/modifications_unselect.jpg" onmouseover="getElementById('menu_modi').src='${baseUrl}/images/modifications_over.jpg'" onmouseout="getElementById('menu_modi').src='${baseUrl}/images/modifications_unselect.jpg'" id='menu_modi' width="100%"></a></TD>
            <TD class="gpf_table_menu_space" width="230"/>
            <TD class="gpf_table_menu_right"/>
        </tr>   
        </table>
</#if>        
    </td>
    <TD class="gpf_table_top_right"/>
</TR>
<TR>
    <TD class="gpf_table_left"/>
    <td bgcolor="#cddcec" width="700" align="center">
        <table cellpadding="0" cellpadding="0" border="0" width="680">
<!-- Start of data areal -->
    <tr>
        <td width=20%"></td><td width=30%"></td><td width=20%"></td><td width=30%"></td>
    </tr>
        <TR>
        <TD width="20%" align="center" valign="middle">
            <img src="./images/logo_tn.jpg">
        </td>   
        <TD colspan=3  class="kai_contens_justify" >

            <b><u>Introduction</u></b><br>
                        The "Glyco-Peakfinder" is a tool for fast annotation of glycan MS spectra.
            MS-profiles, MS<sup>n</sup> spectra with different types of ions (glycosidic cleavages and/or cross-ring cleavages) can be calculated in parallel. 
            The option of detecting differently- and/or multiply-charged ions in one calculation cycle provides a fast and complete annotation of the whole spectrum.
            All the additional options of "Glyco-Peakfinder" (e.g. calculation of modifications either at the reducing end or within the sequence ) 
            increase the field of application from native glycans to a variable set of glycoconjugates. 
            The results from "Glyco-Peakfinder" can be used for advanced database searches in GLYCOSCIENCES.de.
                </TD>
    </tr>
	<tr>
        <TD colspan="4" height="30"/>
	</tr>
    <tr>
        <td valign=top  class="gpf_contens_left"><b><u>How to cite:</u></b></td>
        <td colspan=3 valign=top class="kai_contens_justify">
            	Kai Maass, Ren&eacute; Ranzinger, Hildegard Geyer, Claus-Wilhelm von der Lieth, Rudolf Geyer,<br>
		"Glyco-Peakfinder" - <i>de novo</i> composition analysis of glycoconjugates<br>
		Proteomics, 2007, 7, 4435-4444.
        </td>
    </tr>
    <tr>
        <TD colspan="4" height="30"/>
    </tr>
    <tr>
        <td valign=top  class="gpf_contens_left"><b><u>Examples:</u></b></td>
        <td colspan=3 valign=top>
<#list settings.examples as x>
            <a href="${baseUrl}/Example.action?example=${x.id}" target="_self">${x.name}</a><br>
</#list>            
        </td>
    </TR>
	<tr>
        <TD colspan="4" height="30"/>
	</tr>
    <tr>
        <td colspan=2 valign=top  class="gpf_contens_left">
            <b><u>Implemented features:</u></b><br>
	    <ol>
            <li type="square">Annotation of profiles and fragment spectra<br></li>
            <li type="square">Annotation of multiply-charged ions<br></li>
            <li type="square">Modifications at the reducing end <br></li>
            <li type="square">Annotation of A- and X- fragments<br></li>
            <li type="square">Neutral hydrogen exchange<br></li>
            <li type="square">Glycopeptides and glycolipids<br></li>
            <li type="square">Second generation fragmentation<br></li>
            <li type="square">Annotation of permethylated and peracetylated structures<br></li>
	    </ol>
        </td>
        <td colspan=2 valign=top  class="gpf_contens_left">
            <b><u>coming soon:</u></b><br>
	    <ol>
            <li type="square">Enzymatic digestion of entered peptides<br></li>
	    <li type="square">File upload of vendor specific peaklists<br></li>
	    <li type="square">Request lipid and peptide masses from remote databases<br></li>
	    <li type="square">Calculation of scores for different annotations for the same peak<br></li>
	    <li type="square">Concomitant annotation of MS and MS<sup>n</sup> data<br></li>
	    <li type="square">Provision of a SOAP interface for "Glyco-Peakfinder"<br></li>
	    </ol>
	    </td>
        </TR>
    <tr>
        <td colspan="4" height="30"></td>
    </tr>
    <tr>

        <td  class="gpf_contens_left">
            <b><u>latest news:</u></b><br>
        </td>
    </tr>
    <tr>
        <td  class="gpf_contens_left">
            2008/05/20
        </td>
        <td colspan = 3  class="gpf_contens_left">
            Major updates of "Glyco-Peakfinder" (<a href="http://www.dkfz.de/spec/EuroCarbDB/applications/ms-tools/OldGlycoPeakfinder/GlycoPeakfinder.action">older version</a>)<br><br>
        </td>
    </tr>
    <tr>
        <td  class="gpf_contens_left">
            2006/11/13
        </td>
        <td colspan = 3  class="gpf_contens_left">
            2<sup>nd</sup> version of "Glyco-Peakfinder" online <br><br>
        </td>
    </tr>
    <tr>
        <td  class="gpf_contens_left">
            2006/06/01
        </td>
        <td colspan = 3  class="gpf_contens_left">
            1<sup>st</sup> version of "Glyco-Peakfinder" online<br><br>
        </td>
    </tr>
    </table>
    <table cellpadding="0" cellpadding="0" border="0" width="700">
    <tr>    
        <td colspan=4 bgcolor="#ffffee"  class="gpf_contens_left">
            programmed by Ren&eacute; Ranzinger and Kai Maa&szlig; 
        </td>

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

</form>

<#include "./footer.ftl">