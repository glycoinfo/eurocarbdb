<#assign title>Glycan Entry</#assign>
<#include "/template/common/header.ftl" />

<h1>Detector Attributes</h1>
<h2>Glycan classification details for glycan ${glycanId}</h2>


<p class="hplc">A description of the core attributes</p>

<table class="hplc"><th class="hplc">Name</th><th class="hplc">GU</th><th class="hplc">Standard Dev.</th><th class="hplc">MS</th><th class="hplc">MS/MS</th>

<tr><td class="hplc">${glycan.name}<td class="hplc">${glycan.gu}<td class="hplc">${glycan.std}<td class="hplc">${glycan.ms}<td class="hplc">${glycan.msMs}

</table>

<p class="hplc">Monosaccharide Composition</p>



<table class="hplc"><th class="hplc">Hex</th><th class="hplc">HexNAc</th><th class="hplc">NeuNAc</th><th class="hplc">Fucose</th><th class="hplc">Xylose</th><th class="hplc">NeuGc</th>

<tr><td class="hplc">${glycan.hex}<td class="hplc">${glycan.hexnac}<td class="hplc">${glycan.neunac}<td class="hplc">${glycan.fucose}<td class="hplc">${glycan.xylose}<td class="hplc">${glycan.neugc}

</table>

<p class="hplc">Structural Classification</p>

<table class="hplc"><th class="hplc">A1</th><th class="hplc">A2</th><th class="hplc">A3</th><th class="hplc">A4</th><th class="hplc">Core Fucose</th><th class="hplc">Bisect</th><class="hplc"><th class="hplc">Beta Gal</th><th class="hplc">Alpha Gal</th><th class="hplc">GalNAc</th><th class="hplc">Poly Lac</th><th class="hplc">Fucose Outer</th><th class="hplc">Mannose</th>

<tr><td class="hplc">${glycan.a1}<td class="hplc">${glycan.a2}<td class="hplc">${glycan.a3}<td class="hplc">${glycan.a4}<td class="hplc">${glycan.f6}<td class="hplc">${glycan.b}<td class="hplc">${glycan.bgal}<td class="hplc">${glycan.agal}<td class="hplc">${glycan.galnac}<td class="hplc">${glycan.polylac}<td class="hplc">${glycan.fouterarm}<td class="hplc">${glycan.mannose}

</table>

<p>Publication details</p>

<table class="hplc"><tr><th class="hplc">Author</th><th class="hplc">Year</th><th class="hplc">Reported GU</th><th class="hplc">MS</th><th class="hplc">MS/MS</t
h>
      <#list reference as p>
<tr><td class="hplc"><a href="http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=pubmed&dopt=Abstract&list_uids=${p[4]?c}">${p[3]}</a><td class="hplc">${p[5]?c}<td class="hplc">${p[0]}<td class="hplc">${p[2]}<td class="hplc">${p[1]}<#if p_has_next></#if>
      </#list>
</table>



  </body>
</html>
