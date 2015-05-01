<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<#assign title>GlycoBase</#assign>
<#include "/template/common/header.ftl" />

<script type="text/javascript">
  function toggle_it(itemID){
  // Toggle visibility between none and inline
  if ((document.getElementById(itemID).style.display == 'none'))
  {
   document.getElementById(itemID).style.display = 'inline';
  } else {
   document.getElementById(itemID).style.display = 'none';
         }
  }
</script>

<script type="text/javascript">
var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script>
<script type="text/javascript">
try {
var pageTracker = _gat._getTracker("UA-1428316-3");
pageTracker._trackPageview();
} catch(err) {}</script>

<@ecdb.context_box title="Classification">
<form action="show_glycobaseRefine.action">
<table>
<tr class="style7">
  <td width="100%" align="left"><a href="#" onClick="toggle_it('a1')">A1:</a></td>
</tr>
<tr >
  <td colspan="2">
        <table width="100%" id="a1" name="a1" style="display:none;">
          <tr>
             <td><span class="style7">
                <INPUT TYPE=CHECKBOX NAME="a1s" value="1"> S
                <INPUT TYPE=CHECKBOX NAME="a1f" value="1"> F(6)
                <INPUT TYPE=CHECKBOX NAME="a1b" value="1"> Bisect
                <INPUT TYPE=CHECKBOX NAME="a1bgal" value="1"> B Gal
                <INPUT TYPE=CHECKBOX NAME="a1agal" value="1"> A Gal
                <INPUT TYPE=CHECKBOX NAME="a1galnac" value="1"> GalNAc
                <INPUT TYPE=CHECKBOX NAME="a1polylac" value="1"> Polylac
                <INPUT TYPE=CHECKBOX NAME="a1fouterarm" value="1"> Fuc. Outer
                <INPUT TYPE=CHECKBOX NAME="a1hybrid" value="1"> Hybrid
                <INPUT TYPE=CHECKBOX NAME="a1mannose" value="1"> Mannose
                </span> </td>
           </tr>
         </table>
    </td>
</tr>

<tr class="style7">
  <td width="100%" align="left" height="23" valign="middle"><a href="#" onClick="toggle_it('pr2')">A2:</a></td>
</tr>
<tr >
<td colspan="2">
       <table width="100%" id="pr2" name="a2" style="display:none;">
          <tr>
            <td><span class="style7">
                <INPUT TYPE=CHECKBOX NAME="a2s" value="1"> S
                <INPUT TYPE=CHECKBOX NAME="a2f" value="1"> F(6)
                <INPUT TYPE=CHECKBOX NAME="a2b" value="1"> Bisect
                <INPUT TYPE=CHECKBOX NAME="a2bgal" value="1"> B Gal
                <INPUT TYPE=CHECKBOX NAME="a2agal" value="1"> A Gal
                <INPUT TYPE=CHECKBOX NAME="a2galnac" value="1"> GalNAc
                <INPUT TYPE=CHECKBOX NAME="a2polylac" value="1"> Polylac
                <INPUT TYPE=CHECKBOX NAME="a2fouterarm" value="1"> Fuc. Outer
                <INPUT TYPE=CHECKBOX NAME="a2hybrid" value="1"> Hybrid
                <INPUT TYPE=CHECKBOX NAME="a2mannose" value="1"> Mannose
                </span> </td>
	   </tr>

        </table>
</td>
</tr>
<tr class="style7">
  <td width="100%" align="left" height="23" valign="middle"><a href="#" onClick="toggle_it('pr3')">A3:</a></td>
 </tr>
<tr >
<td colspan="2">
       <table width="100%" id="pr3" name="a3" style="display:none;">
          <tr>
            <td><span class="style7">
                <INPUT TYPE=CHECKBOX NAME="a3s" value="1"> S
                <INPUT TYPE=CHECKBOX NAME="a3f" value="1"> F(6)
                <INPUT TYPE=CHECKBOX NAME="a3b" value="1"> Bisect
                <INPUT TYPE=CHECKBOX NAME="a3bgal" value="1"> B Gal
                <INPUT TYPE=CHECKBOX NAME="a3agal" value="1"> A Gal
                <INPUT TYPE=CHECKBOX NAME="a3galnac" value="1"> GalNAc
                <INPUT TYPE=CHECKBOX NAME="a3polylac" value="1"> Polylac
                <INPUT TYPE=CHECKBOX NAME="a3fouterarm" value="1"> Fuc. Outer
                <INPUT TYPE=CHECKBOX NAME="a3hybrid" value="1"> Hybrid
                <INPUT TYPE=CHECKBOX NAME="a3mannose" value="1"> Mannose
                </span> </td>
           </tr>

        </table>
</td>
</tr>
<tr class="style7">
  <td width="100%" align="left" height="23" valign="middle"><a href="#" onClick="toggle_it('pr4')">A4:</a></td>
</tr>
<tr >
<td colspan="2">
       <table width="100%" id="pr4" name="4" style="display:none;">
          <tr>
            <td><span class="style7">
                <INPUT TYPE=CHECKBOX NAME="a4s" value="1"> S
                <INPUT TYPE=CHECKBOX NAME="a4f" value="1"> F(6)
                <INPUT TYPE=CHECKBOX NAME="a4b" value="1"> Bisect
                <INPUT TYPE=CHECKBOX NAME="a4bgal" value="1"> B Gal
                <INPUT TYPE=CHECKBOX NAME="a4agal" value="1"> A Gal
                <INPUT TYPE=CHECKBOX NAME="a4galnac" value="1"> GalNAc
                <INPUT TYPE=CHECKBOX NAME="a4polylac" value="1"> Polylac
                <INPUT TYPE=CHECKBOX NAME="a4fouterarm" value="1"> Fuc. Outer
                <INPUT TYPE=CHECKBOX NAME="a4hybrid" value="1"> Hybrid
                <INPUT TYPE=CHECKBOX NAME="a4mannose" value="1"> Mannose
                </span> </td>
              </tr>
        </table>
</td>
</tr>
</table>

		<INPUT TYPE="CHECKBOX" NAME="humanIgG" value="1">Human IgG<br>
		<INPUT TYPE="CHECKBOX" NAME="serum" value="1">Human Serum<br>
<INPUT TYPE=SUBMIT VALUE="Refine Selection">
</form>
</@ecdb.context_box>

<@ecdb.context_box title="NIBRT">
<p>National Institute for Bioprocessing Research and Training (NIBRT) is located in Dublin, Ireland 
with a mandate to support the development of the bioprocessing industry by: Training highly skilled personnel for the 
bioprocessing industry. Conducting world-class research in key areas of bioprocessing. Providing flexible, 
multi-purpose bioprocessing research and training facilities. For further information please 
refer to <a href="http://www.nibrt.ie">www.nibrt.ie</a> or <a href="mailto:info@nibrt.ie?Subject=GlycoBase">email</a></p>
</@ecdb.context_box>

<@ecdb.context_box title="Information">
<p>The goal of EUROCarbDB is to develop bioinformatic solutions which assist the interpretation and storage of experimental data. We, therefore, invite any groups interested in expanding the resources presented to contact the <a href="mailto:matthew.campbell@nibrt.ie">EUROCarbDB developers</a>.</p>
</@ecdb.context_box> 


<a href="http://www.nibrt.ie"><img src="${base}/images/nibrt_logo.png" align="right"/></a>


<h1>GlycoBase</h1>
<p>GlycoBase is a novel HPLC resource that contains elution positions (expressed as glucose unit values) for more than 
375 2AB-labeled N-linked glycan structures by a combination of NP-HPLC with exoglycosidase sequencing and mass spectrometry 
(MALDI-MS, ESI-MS, ESI-MS/MS, LC-MS, LC-ESI-MS/MS).</p>

<p>GlycoBase 2.0: <a href="http://glycobase.ucd.ie">Dubin-Oxford Glycobiology Lab. 2AB database</a> developed at <a href="http://www.nibrt.ie">NIBRT</a></p>
<p>GlycoBase 3.0 is a new product offering by NIBRT which contains new data collections and tools relevant to both academics and commercial bodies. For more information click <a href="http://glycobase.nibrt.ie/glycobase3">here</a></p>
<!-- p>The structure format supports: <a href="show_glycobase.action?imageStyle=uoxf&page=${page}">Oxford Format</a> and <a href="show_glycobase.action?imageStyle=cfg&page=${page}">CFG</a></p -->

<@ecdb.page_navigator action_name="show_glycobase.action?trueCT=yes&imageStyle=${imageStyle}&"/>

<br/>

<table class="table_top_header full_width">
<tr>
<th>Glycan Name</th><th>Structure</th><th>Mean GU Value</th>
</tr>

<#if (imageStyle == "uoxf" || imageStyle == "cfg")>
      <#list results as glyco>
     <tr><td><a href="show_glycanEntry.action?glycanId=${glyco[2]?c}">${glyco[0]}</a></td><td><a href="show_glycanEntry.action?glycanId=${glyco[2]?c}"><img src="get_sugar_image.action?download=true&scale=0.4&outputType=png&glycanSequenceId=${glyco[1]?c}"/></a></td><td>${glyco[3]?string("0.##")}</td></tr>

      </#list>
</#if>

</table>

  <@ecdb.page_navigator action_name="show_glycobase.action?trueCT=yes&imageStyle=${imageStyle}&"/>



<#include "/template/common/footer.ftl" />

