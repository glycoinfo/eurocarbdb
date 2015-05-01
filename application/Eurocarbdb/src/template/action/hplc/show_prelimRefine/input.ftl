<#assign title>autoGu</#assign>
<#include "/template/ui/user/header.ftl" />

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
<style type="text/css">
<!--
.style5 {font-family: Arial, Helvetica, sans-serif; font-size: 10; }
.style7 {font-family: Arial, Helvetica, sans-serif; font-size: 10px; }
-->
</style>


<div class="hplc_create_form">
<h1>Preliminary Assignments</h1>
<p></p>
</div>

<div class="hplc_form">
<p>Preliminary undigested assignments with reference to GU matching</p>
<p>Data for profile Id: ${profileId}</p>


<!--<form method="post" enctype="multipart/form-data">
  <@ww.textfield label="Id" name="profileId"/>      
  <input type="submit" name="submitAction" value="Upload"/>
</form>
-->
<p>Proceed onto <a href="show_digestionAnalysis.action?digestId=1&profileId=${profileId}"> digest analysis</a></p>
</div>


<div class="hplc_table_data">
<table width="500"><tr><td><table><th>Glycan Id</th><th>Name</th><th>Structure</th><th>GU</th><th>DB GU</th><th>Area</th>
      <#list showRefinedPrelim as p>
<tr><td>${p.glycanId?c}</td><td>${p.nameAbbreviation}<td align="center"><img src="/ecdb/images/hplc/${p.glycanId?c}.png"></td><td>${p.peakArea}</td><td>${p.dbGu}</td><td>${p.peakArea}</td><#if p_has_next></#if>	
      </#list></td></tr>
</table>

</td>
<td>
<table><tr><td>
<form name="test" action="testrefine.action" method="get">
<table width="200">
<tr class="style7">
  <td width="100%" align="left"><a href="#" onClick="toggle_it('a1')">A1</a> :</td>
  <td height="23" valign="middle" >
    </td>
</tr>
<tr >
  <td colspan="2">
        <!--
            This is the table we will be showing, or hiding. Note it's id.
            If you are producing a series of table rows from a database,
            you can give each row a dynamic id, perhaps the key for the row from the database,
            and use the show hide property for extended information, streamlining your page's presentation,
            but still giving the availability of all the information without additional page loads.
        -->
        <table width="100%" id="a1" name="a1" style="display:none;">
          <tr>
             <td><span class="style5">
                <INPUT TYPE=CHECKBOX NAME="a1s" value="1">S
                <INPUT TYPE=CHECKBOX NAME="a1f" value="1">Core Fuc.
                <INPUT TYPE=CHECKBOX NAME="a1b" value="1">Bisect
                <INPUT TYPE=CHECKBOX NAME="a1bgal" value="1">B Gal
                <INPUT TYPE=CHECKBOX NAME="a1agal" value="1">A Gal
                <INPUT TYPE=CHECKBOX NAME="a1galnac" value="1">GalNAc
                <INPUT TYPE=CHECKBOX NAME="a1polylac" value="1">Polylac
                <INPUT TYPE=CHECKBOX NAME="a1fouterarm" value="1">Fuc. Outer
                <INPUT TYPE=CHECKBOX NAME="a1hybrid" value="1">Hybrid
                <INPUT TYPE=CHECKBOX NAME="a1mannose" value="1">Mannose
              </span> </td>
              </tr>
          </table>
        </td>
    </tr>



<tr class="style7">
  <td width="100%" align="right" height="23" valign="middle"><a href="#" onClick="toggle_it('pr2')">A2</a> :</td>
  <td height="23" valign="middle" >
    </td>
</tr>
<tr >
<td colspan="2">
        <!--
            This is the table we will be showing, or hiding. Note it's id.
            If you are producing a series of table rows from a database,
            you can give each row a dynamic id, perhaps the key for the row from the database,
            and use the show hide property for extended information, streamlining your page's presentation,
            but still giving the availability of all the information without additional page loads.
        -->
       <table width="100%" id="pr2" name="police_response1" style="display:none;">
          <tr>
            <td><span class="style5">
                <INPUT TYPE=CHECKBOX NAME="a2s" value="1">S
                <INPUT TYPE=CHECKBOX NAME="a2f" value="1">Core Fuc.
                <INPUT TYPE=CHECKBOX NAME="a2b" value="1">Bisect
                <INPUT TYPE=CHECKBOX NAME="a2bgal" value="1">B Gal
                <INPUT TYPE=CHECKBOX NAME="a2agal" value="1">A Gal
                <INPUT TYPE=CHECKBOX NAME="a2galnac" value="1">GalNAc
                <INPUT TYPE=CHECKBOX NAME="a2polylac" value="1">Polylac
                <INPUT TYPE=CHECKBOX NAME="a2fouterarm" value="1">Fuc. Outer
                <INPUT TYPE=CHECKBOX NAME="a2hybrid" value="1">Hybrid
                <INPUT TYPE=CHECKBOX NAME="a2mannose" value="1">Mannose 
		</span> </td>
              </tr>

        </table>
        </td>

</tr>



<tr class="style7">
  <td width="100%" align="right" height="23" valign="middle"><a href="#" onClick="toggle_it('pr3')">A3</a> :</td>
  <td height="23" valign="middle" >
    </td>
</tr>
<tr >
<td colspan="2">
        <!--
            This is the table we will be showing, or hiding. Note it's id.
            If you are producing a series of table rows from a database,
            you can give each row a dynamic id, perhaps the key for the row from the database,
            and use the show hide property for extended information, streamlining your page's presentation,
            but still giving the availability of all the information without additional page loads.
        -->
       <table width="100%" id="pr3" name="police_response1" style="display:none;">
          <tr>
            <td><span class="style5">
                <INPUT TYPE=CHECKBOX NAME="a3s" value="1">S
                <INPUT TYPE=CHECKBOX NAME="a3f" value="1">Core Fuc.
                <INPUT TYPE=CHECKBOX NAME="a3b" value="1">Bisect
                <INPUT TYPE=CHECKBOX NAME="a3bgal" value="1">B Gal
                <INPUT TYPE=CHECKBOX NAME="a3agal" value="1">A Gal
                <INPUT TYPE=CHECKBOX NAME="a3galnac" value="1">GalNAc
                <INPUT TYPE=CHECKBOX NAME="a3polylac" value="1">Polylac
                <INPUT TYPE=CHECKBOX NAME="a3fouterarm" value="1">Fuc. Outer
                <INPUT TYPE=CHECKBOX NAME="a3hybrid" value="1">Hybrid
                <INPUT TYPE=CHECKBOX NAME="a3mannose" value="1">Mannose
                </span> </td>
              </tr>

        </table>
        </td>

</tr>



<tr class="style7">
  <td width="100%" align="right" height="23" valign="middle"><a href="#" onClick="toggle_it('pr4')">A4</a> :</td>
  <td height="23" valign="middle" >
    </td>
</tr>
<tr >
<td colspan="2">
        <!--
            This is the table we will be showing, or hiding. Note it's id.
            If you are producing a series of table rows from a database,
            you can give each row a dynamic id, perhaps the key for the row from the database,
            and use the show hide property for extended information, streamlining your page's presentation,
            but still giving the availability of all the information without additional page loads.
        -->
       <table width="100%" id="pr4" name="police_response1" style="display:none;">
          <tr>
            <td><span class="style5">
                <INPUT TYPE=CHECKBOX NAME="a4s" value="1">S
                <INPUT TYPE=CHECKBOX NAME="a4f" value="1">Core Fuc.
                <INPUT TYPE=CHECKBOX NAME="a4b" value="1">Bisect
                <INPUT TYPE=CHECKBOX NAME="a4bgal" value="1">B Gal
                <INPUT TYPE=CHECKBOX NAME="a4agal" value="1">A Gal
                <INPUT TYPE=CHECKBOX NAME="a4galnac" value="1">GalNAc
                <INPUT TYPE=CHECKBOX NAME="a4polylac" value="1">Polylac
                <INPUT TYPE=CHECKBOX NAME="a4fouterarm" value="1">Fuc. Outer
                <INPUT TYPE=CHECKBOX NAME="a4hybrid" value="1">Hybrid
                <INPUT TYPE=CHECKBOX NAME="a4mannose" value="1">Mannose
                </span> </td>
              </tr>

        </table>
        </td>

 </tr>

<INPUT TYPE=SUBMIT VALUE="submit">
    </tr>
</table>
</form>

</table>
</div>

<#include "/template/common/footer.ftl" />

