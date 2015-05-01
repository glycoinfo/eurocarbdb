<#include "header.ftl">

<h2>Glyco-Peakfinder</h2>

<form method="POST" action="GlycoPeakfinder.action" name="values">
  <input type="text" class="gpf_information" height="1" name="pageTo" value=""/>
  <input type="text" class="gpf_information" height="1" name="pageFrom" value="info"/>

<div id="gpf_tabs">
  <ul>
    <#if result.initialized = true>
    <li><a href="Result.action?pageTo=resu">Result</a></li>
    <li><a href="Result.action?pageTo=stru">Structures</a></li>
    <li><a href="Result.action?pageTo=modi">Settings</a>
    <#else>
    <li><a href="Input.action?pageTo=mass">Mass</a></li>
    <li><a href="Input.action?pageTo=resi">Residue</a></li>
    <li><a href="Input.action?pageTo=ions">Ion/charge</a></li>
    <li><a href="Input.action?pageTo=modi">Modifications</a></li>
    </#if>
  </ul>
  <div></div>
</div>   
<table border="0" cellpadding="0" cellspacing="0" width="702">
    <td class="gpf_table_left"/>
    <td width="700" align="center">
        <table cellpadding="0" cellpadding="0" border="0" width="680">
<!-- Start of data areal -->
    <tr>
        <td width=20%"></td><td width=30%"></td><td width=20%"></td><td width=30%"></td>
    </tr>
        <tr>
        <td width="20%" align="center" valign="middle">
            <img src="./images/logo_tn.jpg">
        </td>   
        <td colspan=3  class="kai_contens_justify" >

            <b><u>Introduction</u></b><br>
                        The "Glyco-Peakfinder" is a tool for fast annotation of glycan MS spectra.
            MS-profiles, MS<sup>n</sup> spectra with different types of ions (glycosidic cleavages and/or cross-ring cleavages) can be calculated in parallel. 
            The option of detecting differently- and/or multiply-charged ions in one calculation cycle provides a fast and complete annotation of the whole spectrum.
            All the additional options of "Glyco-Peakfinder" (e.g. calculation of modifications either at the reducing end or within the sequence ) 
            increase the field of application from native glycans to a variable set of glycoconjugates. 
            The results from "Glyco-Peakfinder" can be used for advanced database searches in GLYCOSCIENCES.de.
                </td>
    </tr>
	<tr>
        <td colspan="4" height="30"/>
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
        <td colspan="4" height="30"/>
    </tr>
    <tr>
        <td valign=top  class="gpf_contens_left"><b><u>Examples:</u></b></td>
        <td colspan=3 valign=top>
<#list settings.examples as x>
            <a href="Example.action?example=${x.id}" target="_self">${x.name}</a><br>
</#list>            
        </td>
    </tr>
	<tr>
        <td colspan="4" height="30"/>
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
        </tr>
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
        <td colspan=4  class="gpf_contens_left">
            programmed by Ren&eacute; Ranzinger and Kai Maa&szlig; 
        </td>

    </tr>
<!-- End of data areal -->
        </table>
    </td>
    <td class="gpf_table_right"/>
</tr>
<tr>
    <td class="gpf_table_bottom_left"/>
    <td class="gpf_table_bottom"/>
    <td class="gpf_table_bottom_right"/>
</tr>
<tr>
    <td colspan="3" height="20"/>
</tr>
</table>

</form>

<#include "./footer.ftl">