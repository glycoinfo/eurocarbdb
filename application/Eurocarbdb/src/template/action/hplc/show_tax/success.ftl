<#assign title>Glycan Entry</#assign>
<#include "/template/common/header.ftl" />

<div class="hplc_glycan_entry">
Glycan Id: ${glycanId?c}
</div>

<h1>Glycan classification details for ${glycanName}</h1>

<table>
<#if imageStyle == "uoxf" || imageStyle == "cfg">
<tr><td>
<img src="get_sugar_image.action?download=true&scale=0.5&inputType=Ogbi&outputType=png&notation=${imageStyle}&sequences=${glycanName}"/>
</td>
<td>
</#if>

<#if imageStyle == "original">
<tr><td>
<img src="/ecdb/images/hplc/${glycanId?c}.png"/>
</td>
<td>
</#if>
        <ul>
                <li><a href="show_tax.action?refId=${refId}&glycanId=${glycanId?c}&imageStyle=original&glycanName=${glycanName}">Original UOXF Nomenclature</a></li>
                <li><a href="show_tax.action?refId=${refId}&glycanId=${glycanId?c}&imageStyle=uoxf&glycanName=${glycanName}">UOXF Nomenclature (glycoCT suport)</a></li>
                <li><a href="show_tax.action?refId=${refId}&glycanId=${glycanId?c}&imageStyle=cfg&glycanName=${glycanName}">CFG Format</a></li>
        </ul>
</td>

</table>

<h3>Temp. core taxonomy relationships</h3>

<#list displayGlycobaseList as tax>
        <ul>
                <li><a href="show_taxonomy.action?ncbiId=${tax[0]?c}">Top Tax Order</a></li>
                <li><a href="show_taxonomy.action?ncbiId=${tax[1]?c}">Species Info</a></li>
                <li>Perturbation -> ${tax[2]}</li>
                <li>Disease -> ${tax[3]}</li>
                <li>Tissue Type -> ${tax[4]}</li>
         </ul>
<#if tax_has_next></#if>
</#list>


<h3>Corresponding Reference Details</h3>

<#list displayRefs as ref>
         <ul>
                <li><a href="http://www.show_taxonomy_parent.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=pubmed&dopt=Abstract&list_uids=${ref[4]?c}">{${ref[3]}</a></li>
                <li>Year: ${ref[5]}</li>
                <li>GU Value: ${ref[0]}</li>
         </ul>
<#if ref_has_next></#if>
</#list>

<#include "/template/common/footer.ftl" />
