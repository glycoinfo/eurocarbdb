<#include "../header.ftl">

<h1>Monosaccharide Query Results</h1>
<#if uncertainMs??>
	<h3>Database was queried using a monosaccharide name that contains uncertainties.</h3>
	<a href="display_monosaccharide.action?name=${uncertainMs.name}&scheme=msdb">Display data for this uncertain / fuzzy residue.</a><br><br>
	<#if msResultList?size gt 0><h3>Database entries matching this name template:</h3></#if>
</#if>
<#if msResultList?size==0>
	<#if !(uncertainMs??)>
	<span class="bold">Sorry, no monosaccharides found matching your query.<br>
	<br>
	<a href="javascript:history.back()">(go back)</a></span>
	</#if>
<#else>
	<div class="resultscounts">Results 1-${msResultList?size} of ${msResultList?size}</div>
	<table class="contenttable">
		<tr>
			<td class="tablehead">Id</td>
			<td class="tablehead">Name (MsDB)</td>
			<td class="tablehead">Name (CarbBank)</td>
			<td class="tablehead">Image</td>
		</tr>
		
		<#list msResultList as ms>
		<tr>
			<td class="tablecontent">${ms.dbId?c}</td>
			<td class="tablecontent"><a href="display_monosaccharide.action?id=${ms.dbId?c}">${ms.name}</a></td>
			<td class="tablecontent">
			<#if ms.carbbankAlias??>
				<a href="display_monosaccharide.action?id=${ms.dbId?c}">${ms.carbbankAlias.name}
				<#list ms.carbbankAlias.externalSubstList as subst>
				<br> + (${subst.getPosition1Str("/","?")}-${subst.getSubstituentPosition1Str("/","?")}) ${subst.name}
				</#list></a>
			</#if>
			</td>
			<td class="tablecontent"><#if ms.getImageId(defaultGraphicsFormat) gt 0><a href="display_monosaccharide.action?id=${ms.dbId?c}"><img src="get_ms_representation.action?representationId=${ms.getImageId(defaultGraphicsFormat)?c}" class="structure_image"></a></#if></td>
		</tr>
		</#list>
	</table>
</#if>
<#include "../footer.ftl">