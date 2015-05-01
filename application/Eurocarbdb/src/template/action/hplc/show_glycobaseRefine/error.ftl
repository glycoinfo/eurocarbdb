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
                <INPUT TYPE=CHECKBOX NAME="a1s" value="1">S<br>
                <INPUT TYPE=CHECKBOX NAME="a1f" value="1">F(6)<br>
                <INPUT TYPE=CHECKBOX NAME="a1b" value="1">Bisect<br>
                <INPUT TYPE=CHECKBOX NAME="a1bgal" value="1">B Gal<br>
                <INPUT TYPE=CHECKBOX NAME="a1agal" value="1">A Gal<br>
                <INPUT TYPE=CHECKBOX NAME="a1galnac" value="1">GalNAc<br>
                <INPUT TYPE=CHECKBOX NAME="a1polylac" value="1">Polylac<br>
                <INPUT TYPE=CHECKBOX NAME="a1fouterarm" value="1">Fuc. Outer<br>
                <INPUT TYPE=CHECKBOX NAME="a1hybrid" value="1">Hybrid<br>
                <INPUT TYPE=CHECKBOX NAME="a1mannose" value="1">Mannose<br>
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
                <INPUT TYPE=CHECKBOX NAME="a2s" value="1">S<br>
                <INPUT TYPE=CHECKBOX NAME="a2f" value="1">F(6)<br>
                <INPUT TYPE=CHECKBOX NAME="a2b" value="1">Bisect<br>
                <INPUT TYPE=CHECKBOX NAME="a2bgal" value="1">B Gal<br>
                <INPUT TYPE=CHECKBOX NAME="a2agal" value="1">A Gal<br>
                <INPUT TYPE=CHECKBOX NAME="a2galnac" value="1">GalNAc<br>
                <INPUT TYPE=CHECKBOX NAME="a2polylac" value="1">Polylac<br>
                <INPUT TYPE=CHECKBOX NAME="a2fouterarm" value="1">Fuc. Outer<br>
                <INPUT TYPE=CHECKBOX NAME="a2hybrid" value="1">Hybrid<br>
                <INPUT TYPE=CHECKBOX NAME="a2mannose" value="1">Mannose<br>
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
                <INPUT TYPE=CHECKBOX NAME="a3s" value="1">S<br>
                <INPUT TYPE=CHECKBOX NAME="a3f" value="1">F(6)<br>
                <INPUT TYPE=CHECKBOX NAME="a3b" value="1">Bisect<br>
                <INPUT TYPE=CHECKBOX NAME="a3bgal" value="1">B Gal<br>
                <INPUT TYPE=CHECKBOX NAME="a3agal" value="1">A Gal<br>
                <INPUT TYPE=CHECKBOX NAME="a3galnac" value="1">GalNAc<br>
                <INPUT TYPE=CHECKBOX NAME="a3polylac" value="1">Polylac<br>
                <INPUT TYPE=CHECKBOX NAME="a3fouterarm" value="1">Fuc. Outer<br>
                <INPUT TYPE=CHECKBOX NAME="a3hybrid" value="1">Hybrid<br>
                <INPUT TYPE=CHECKBOX NAME="a3mannose" value="1">Mannose<br>
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
                <INPUT TYPE=CHECKBOX NAME="a4s" value="1">S<br>
                <INPUT TYPE=CHECKBOX NAME="a4f" value="1">F(6)<br>
                <INPUT TYPE=CHECKBOX NAME="a4b" value="1">Bisect<br>
                <INPUT TYPE=CHECKBOX NAME="a4bgal" value="1">B Gal<br>
                <INPUT TYPE=CHECKBOX NAME="a4agal" value="1">A Gal<br>
                <INPUT TYPE=CHECKBOX NAME="a4galnac" value="1">GalNAc<br>
                <INPUT TYPE=CHECKBOX NAME="a4polylac" value="1">Polylac<br>
                <INPUT TYPE=CHECKBOX NAME="a4fouterarm" value="1">Fuc. Outer<br>
                <INPUT TYPE=CHECKBOX NAME="a4hybrid" value="1">Hybrid<br>
                <INPUT TYPE=CHECKBOX NAME="a4mannose" value="1">Mannose<br>
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

<h1>GlycoBase - Error</h1>
<p>Your search requirements generated 0 results, please try again</p>

<#include "/template/common/footer.ftl" />

