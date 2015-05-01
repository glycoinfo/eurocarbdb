<#assign title>autoGU: Preliminary Assignments</#assign>
<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
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
<style type="text/css">
<!--
.style5 {font-family: Arial, Helvetica, sans-serif; font-size: 10; }
.style6 {font-family: Arial, Helvetica, sans-serif; font-size: 15px; }
.style7 {font-family: Arial, Helvetica, sans-serif; font-size: 10px; }
-->
</style>

<@ecdb.context_box title="Classification">
<form action="create_prelimAssign.action">
<table width="200">
<tr>Classification Selection</tr>

	<input type="hidden" label="Profile Id" name="profileId" value="${profileId}"/>
	<input type="hidden" label="Refine" name="refineAssignment" value="yes"/>
	<tr class="style6">
  	<td width="100%" align="left"><a href="#" onClick="toggle_it('a1')">A1</a></td>
  	<td height="23" valign="middle" ></td>
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


<tr class="style6">
  <td width="100%" align="left" height="23" valign="middle"><a href="#" onClick
="toggle_it('pr2')">A2</a></td>
  <td height="23" valign="middle" >
    </td>
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

<tr class="style6">
  <td width="100%" align="left" height="23" valign="middle"><a href="#" onClick="toggle_it('pr3')">A3</a></td>
  <td height="23" valign="middle" >
    </td>
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

<tr class="style6">
  <td width="100%" align="left" height="23" valign="middle"><a href="#" onClick="toggle_it('pr4')">A4</a></td>
  <td height="23" valign="middle" >
    </td>
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
 </tr>


</table>

<INPUT TYPE=SUBMIT VALUE="Refine Selection">
</form>
</@ecdb.context_box>

<@ecdb.context_box title="Digest Analaysis">
<a href="show_digestionAnalysis.action?profileId=${profileId}&digestId=1">Proceed to digestion analysis</a>
</@ecdb.context_box>


<h1>Preliminary Assignments</h1>
<p></p>

<p>Preliminary undigested assignments with reference to GU matching</p>
<p>Data for profile Id: ${profileId}</p>


<!--<form method="post" enctype="multipart/form-data">
  <@ww.textfield label="Id" name="profileId"/>      
  <input type="submit" name="submitAction" value="Upload"/>
</form>
-->
<p>Proceed onto <a href="show_digestionAnalysis.action?digestId=1&profileId=${profileId}"> digest analysis</a></p>

<#if showCriteria?exists>
<#assign resultsSize = showCriteria?size>
<p>Refinement criteria lists: ${resultsSize?c} entries</p>
<#else><#assign resultsSize = 0>
</#if>

<@ecdb.page_navigator action_name="create_prelimAssign.action?profileId=${profileId}&"/>

<table class="table_top_header full_width">
        <#if resultsSize == 0>
	 <tr><th>Name</th><th>Structure</th><th>GU</th><th>DB GU</th><th>Area</th><th>Remove</th></tr>
	<#list prelimarytwo as p>

	<tr><td><a href="show_glycanEntry.action?glycanId=${p[0]}&trueCT=yes&imageStyle=uoxf">${p[4]}</a></td><td align="center"><img src="get_sugar_image.action?download=true&scale=0.4&notation=uoxf&outputType=png&glycanSequenceId=${p[5]?c}" ></td><td>${p[2]}</td><td>${p[3]}</td><td>${p[1]}</td><td><a href="create_prelimAssign.action?profileId=${profileId}&deleteEntry=${p[6]?c}">delete</td>
      	<#if p_has_next></#if>	
      	</#list>
	</#if>
	<#if resultsSize != 0>
	 <tr><th>Name</th><th>Structure</th><th>GU</th><th>DB GU</th><th>Area</th><th>Remove</th></tr>
	<#list showCriteria as show>
	 <tr><td><a href="show_glycanEntry.action?glycanId=${show[4]}&trueCT=yes&imageStyle=uoxf">${show[0]}</a></td><td><img src="get_sugar_image.action?download=true&scale=0.4&notation=uoxf&outputType=png&glycanSequenceId=${show[4]?c}" ></td><td>${show[1]?c}</td><td>${show[2]?c}</td><td>${show[3]}</td><td><a href="create_prelimAssign.action?profileId=${profileId}&deleteEntry=${show[5]?c}">delete</tr>
	         <#if show_has_next></#if>
		         </#list>
			         </#if>

	</td></tr>
</table>


<@ecdb.page_navigator action_name="create_prelimAssign.action?profileId=${profileId}&"/>


<#include "/template/common/footer.ftl" />

