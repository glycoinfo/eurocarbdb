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
                <INPUT TYPE=CHECKBOX NAME="a1s" value="1">S
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
                <INPUT TYPE=CHECKBOX NAME="a2s" value="1">S
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
                <INPUT TYPE=CHECKBOX NAME="a3s" value="1">S
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
                <INPUT TYPE=CHECKBOX NAME="a4s" value="1">S
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
<form action="show_glycobase_contract.action">
<INPUT TYPE="SUBMIT" VALUE="Show All">
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



<h1>GlycoBase</h1>
<p>A database containing published details on 2-aminobenzamide (2AB) labelled released glycans. Each glycan entry is comprehenisvely annotated with a glucose unit (GU) value, monosaccharide composition, exoglycosidase digestion pathways, MS and MS/MS evidence and linked to the appropriate PubMed entry. The database was develop
ed in collaboration with the Oxford Glycobiology Institute and NIBRT.
</p>

<p>GlycoBase: <a href="http://glycobase.ucd.ie">Dubin-Oxford Glycobiology Lab. 2
AB database</a> developed at <a href="http://www.nibrt.ie">NIBRT</a></p>


<@ecdb.page_navigator action_name="show_glycobase_contractRefine.action?a1s=${a1s}&a1f=${a1f}&a1b=${a1b}&a1bgal=${a1bgal}&a1agal=${a1agal}&a1galnac=${a1galnac}&a1polylac=${a1polylac}&a1fouterarm=${a1fouterarm}&a1hybrid=${a1hybrid}&a1mannose=${a1mannose}&a2s=${a2s}&a2f=${a2f}&a2b=${a2b}&a2bgal=${a2bgal}&a2agal=${a2agal}&a2galnac=${a2galnac}&a2polylac=${a2polylac}&a2fouterarm=${a2fouterarm}&a2hybrid=${a2hybrid}&a2mannose=${a2mannose}&a3s=${a3s}&a3f=${a3f}&a3b=${a3b}&a3bgal=${a3bgal}&a3agal=${a3agal}&a3galnac=${a3galnac}&a3polylac=${a3polylac}&a3fouterarm=${a3fouterarm}&a3hybrid=${a3hybrid}&a3mannose=${a3mannose}&a4s=${a4s}&a4f=${a4f}&a4b=${a4b}&a4bgal=${a4bgal}&a4agal=${a4agal}&a4galnac=${a4galnac}&a4polylac=${a4polylac}&a4fouterarm=${a4fouterarm}&a4hybrid=${a4hybrid}&a4mannose=${a4mannose}&pharm=${pharm}&igG=${igG}&fsh=${fsh}&interferon=${interferon}&epo=${epo}&plant=${plant}&"/>

<br/>
<br/>
<table class="table_top_header full_width">

<th>Glycan Name</th><th>Structure</th><th>Mean GU Value</th>
</tr>
      <#list results as glyco>
      <tr><td><a href="show_glycanEntry.action?glycanId=${glyco[3]?c}">${glyco[0]}</a><td><img src="get_sugar_image.action?download=true&scale=0.4&outputType=png&glycanSequenceId=${glyco[1]?c}"/><td>${glyco[2]}</tr>
      </#list>
</table>


<@ecdb.page_navigator action_name="show_glycobase_contractRefine.action?a1s=${a1s}&a1f=${a1f}&a1b=${a1b}&a1bgal=${a1bgal}&a1agal=${a1agal}&a1galnac=${a1galnac}&a1polylac=${a1polylac}&a1fouterarm=${a1fouterarm}&a1hybrid=${a1hybrid}&a1mannose=${a1mannose}&a2s=${a2s}&a2f=${a2f}&a2b=${a2b}&a2bgal=${a2bgal}&a2agal=${a2agal}&a2galnac=${a2galnac}&a2polylac=${a2polylac}&a2fouterarm=${a2fouterarm}&a2hybrid=${a2hybrid}&a2mannose=${a2mannose}&a3s=${a3s}&a3f=${a3f}&a3b=${a3b}&a3bgal=${a3bgal}&a3agal=${a3agal}&a3galnac=${a3galnac}&a3polylac=${a3polylac}&a3fouterarm=${a3fouterarm}&a3hybrid=${a3hybrid}&a3mannose=${a3mannose}&a4s=${a4s}&a4f=${a4f}&a4b=${a4b}&a4bgal=${a4bgal}&a4agal=${a4agal}&a4galnac=${a4galnac}&a4polylac=${a4polylac}&a4fouterarm=${a4fouterarm}&a4hybrid=${a4hybrid}&a4mannose=${a4mannose}&pharm=${pharm}&igG=${igG}&fsh=${fsh}&interferon=${interferon}&epo=${epo}&plant=${plant}&"/>

<#include "/template/common/footer.ftl" />

