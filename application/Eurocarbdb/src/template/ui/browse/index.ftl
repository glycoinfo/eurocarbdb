<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<#import "/template/lib/TextUtils.lib.ftl" as text />

<@ww.url value="/css/action/browse.css" id="url_page_css"/>
<@ww.url action="show_taxonomy" id="url_taxonomy" includeParams="false"/>
<@ww.url action="show_disease" id="url_disease" includeParams="false"/>
<@ww.url action="show_tissue_taxonomy" id="url_tissue" includeParams="false"/>
<@ecdb.include_css url="${url_page_css}"/>

<@ecdb.header title="Browse"/>

<h1>Browse EUROCarbDB</h1>
<p>EUROCarbDB is a glycan database, indexing results by biological source, structure, or 
experimental method used to obtain the data.</p>

<h2>Structures</h2>
<ul>
<li><@ww.a href="/ecdb/ww/browse_structures.action">Show all structures</@ww.a></li>
</ul>

<h2>Biological Sources</h2>
<ul>
<li><@ww.a href="${url_taxonomy}?taxonomy.ncbiId=40674">Mammalian</@ww.a></li>
<li><@ww.a href="${url_taxonomy}?taxonomy.ncbiId=9606">Human</@ww.a></li>
<li><@ww.a href="${url_taxonomy}?taxonomy.ncbiId=2">Bacterial</@ww.a></li>
</ul>

<h2>Disease states</h2>
<ul>
<li><@ww.a href="${url_disease}?disease.meshId=C04">Cancer</@ww.a></li>
</ul>

<h2>Tissue</h2>
<ul>
<li><@ww.a href="${url_tissue}?tissueTaxonomy.meshId=A03.556.124.526.356">Colon</@ww.a></li>
<li><@ww.a href="${url_tissue}?tissueTaxonomy.meshId=A12.459">Feces</@ww.a></li>
</ul>

<@ecdb.footer />