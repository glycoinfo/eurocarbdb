<#assign taxes = taxonomy.getAllChildTaxonomiesWithContext()/>
<#if (taxes.size() > 0) >
<h3>Glycan sequence distribution within sub-taxa</h3>
</#if>
<!-- sub-taxa tag cloud -->
<div>
<table id="taxonomy_list">
	<thead>
	<tr>
	  <th>ECDB id</th> 
	  <th>NCBI id</th>
      <th>Taxonomy name</th>
      <th>Structure count</th>
    </tr>
	</thead>
	<tbody>
	<#list taxes as tax>
	  <tr>
	    <td>${tax.getTaxonomyId()?c}</td>
        <td>${tax.getNcbiId()?c}</td>
        <td>${tax.getTaxon()}</td>
        <td>${tax.getAllStructures().size()?c}</td>
      </tr>
    </#list>
	</tbody>
</table>
</div>
<!-- end sub-taxa tag cloud -->
