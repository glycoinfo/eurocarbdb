<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<#assign title>GlycoBase Search Results</#assign>
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
.style7 {font-family: Arial, Helvetica, sans-serif; font-size: 10px; }
-->
</style>

<#assign resultsSize = displayGlycobaseList?size>

<h1>GlycoBase</h1>
<!-- p>The structure format supports: <a href="search_glycobase.action?imageStyle=uoxf&searchGlycanGu=${searchGlycanGu}&searchGlycanName=${searchGlycanName}">Oxford Format</a> and <a href="search_glycobase.action?imageStyle=cfg&searchGlycanGu=${searchGlycanGu}&searchGlycanName=${searchGlycanName}">CFG</a></p-->

<p>Your search retrieved ${resultsSize} results</p> 


<table class="table_top_header full_width">
  <th>Glycan Name</th><th>Structure</th>

      <#list displayGlycobaseList as glyco>
<tr><td class="hplc"><a href="show_glycanEntry.action?glycanId=${glyco.glycanId?c}&trueCT=yes&imageStyle=uoxf">${glyco.name}</a></td><td class="hplc"><img src="get_sugar_image.action?download=true&scale=0.4&outputType=png&glycanSequenceId=${glyco.ogbitranslation}"/></td><#if glyco_has_next></#if>
      </#list>
     

</table>



<#include "/template/common/footer.ftl" />

