<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<#assign title>Search GlycoBase</#assign>
<#include "/template/common/header.ftl" />



<h1>Search GlycoBase</h1>
<p>GlycoBase entries can searched either by GU value or by glycan name type:</p>
<form>

<form>

<table>
  <tr>
   <td>
     Glycan GU:<input type="text" name="searchGlycanGu" />
   </td>
   <input type="hidden" name="imageStyle" value="uoxf" />
   <input type="hidden" name="searchGlycanGu" value="-1" />
   <input type="hidden" name="searchGlycanName" value="none" />
  </tr>
  <tr><td><input type="submit" value="Search GU value ->"></td></tr>
</table>

 </form>

 <form>

 <table>
   <tr>
    <td>
     Glycan Name:<input type="text" name="searchGlycanName" />
     </td>
  </tr>
   <input type="hidden" name="imageStyle" value="uoxf" />
   <input type="hidden" name="searchGlycanGu" value="-1"/>
   <tr><td><input type="submit" value="Search Name ->"></td></tr>
  </table>

</form>





<#include "/template/common/footer.ftl" />

