<#include "./header.ftl">

<table class="outer">
<tr>
	<td class="tablehead">Substituent ${name}</td>
</tr>
<tr class="tabsline">
	<td class="tabsline">
		<div id="tabs">
				<#if tab = "residue">
					<span class="tabcell_selected">Residue</span>
				<#else>
					<span class="tabcell"><a href="show_substituent.action?name=${name}&tab=residue">Residue</a></span>
				</#if>
				<#if tab = "notation">
					<span class="tabcell_selected">Notation</span>
				<#else>
					<span class="tabcell"><a href="show_substituent.action?name=${name}&tab=notation">Notation</a></span>
				</#if>
				<#if tab = "atoms">
					<span class="tabcell_selected">Atoms</span>
				<#else>
					<span class="tabcell"><a href="show_substituent.action?name=${name}&tab=atoms">Atoms</a></span>
				</#if>
				<#if tab = "all">
					<span class="tabcell_selected">All</span>
				<#else>
					<span class="tabcell"><a href="show_substituent.action?name=${name}&tab=all">All</a></span>
				</#if>
		</div>
	</td>
</tr>
<#-- residue tab: -->
<#if tab = "residue" || tab="all">
<tr class="outer">
	<td style="vertical-align:top;">
		<table class="contenttable">
			<tr>
				<td colspan="2" class="tablehead">Properties:</td>
			</tr>
			<tr>
				<td class="tablecontent">Name:</td>
				<td class="tablecontent">${name}</td>
			</tr>
			<tr>
				<td class="tablecontent">Valence (min/max):</td>
				<td class="tablecontent">${substTmpl.minValence!'?'}/${substTmpl.maxValence!'?'}</td>
			</tr>
			<tr>
				<td class="tablecontent">Monoisotopic Mass:</td>
				<td class="tablecontent">${substTmpl.monoMass?string(",##0.0")} g/mol</td>
			</tr>
			<tr>
				<td class="tablecontent">Average Mass:</td>
				<td class="tablecontent">${substTmpl.avgMass?string(",##0.0")} g/mol</td>
			</tr>
			<tr>
				<td class="tablecontent">Formula:</td>
				<td class="tablecontent">${substTmpl.formula!'-'}</td>
	
			</tr>
			<tr>
				<td class="tablecontent">Composition:</td>
				<td class="tablecontent">${substTmpl.composition.toWebFormula()!'-'}</td>
	
			</tr>
			<tr>
				<td class="tablecontent">InChi:</td>
				<td class="tablecontent">${substTmpl.inchi!'-'}</td>
	
			</tr>
			<tr>
				<td class="tablecontent">Smiles:</td>
				<td class="tablecontent">${substTmpl.smiles!'-'}</td>
			</tr>
		</table>
		<br>
	</td>
</tr>
<tr class="outer">
	<td style="vertical_align:top;">
		<table class="contenttable">
			<tr>
				<td colspan="5" class="tablehead">Valid linking positions:</td>
			</tr>
			<tr>
				<td class="tablehead">Position</td>
				<td class="tablehead">Linked Atom</td>
				<td class="tablehead">Replaced Atom</td>
				<td class="tablehead">Bond Order</td>
				<td class="tablehead">Default Linkage Type</td>
			</tr>
			<#list substTmpl.validLinkingPositions as vlp>
			<tr>
				<td class="tablecontent">${vlp.position}</td>
				<td class="tablecontent">${vlp.linkedAtom.name}</td>
				<td class="tablecontent"><#if vlp.replacedAtom??>${vlp.replacedAtom.name!'-'}<#else>-</#if></td>
				<td class="tablecontent">${vlp.bondOrder?string("0.0")}</td>
				<td class="tablecontent">${vlp.defaultLinktype}</td>
			</tr>
			</#list>
		</table>
		<br>
	</td>
</tr>
<tr class="outer">
	<td style="vertical_align:top;">
		<form method="get" action="query_monosaccharide_by_name.action">
			<input type="hidden" name="substName" value="${name}">
			<input type="hidden" name="scheme" value="msdb">
			&bull; find monosaccharides featuring this substituent, linkage type 
			<select name="substMsLinktype" size="1">
			<#if substituentLinktypeList??>
			<#list substituentLinktypeList as linktype>
				<option value="${linktype}">${linktype}</option>
			</#list>
			</#if>
				<option value="">any</option>
			</select>
			<input type="submit">
		</form>
	</td>
</tr>
</#if><#-- /residue tab -->
<#-- notation tab: -->
<#if tab="notation" || tab="all">
<tr class="outer">
	<td style="vertical-align:top;">
		<table width="100%" class="contenttable">
		<tr>
			<td colspan="5" class="tablehead">Notation:</td>
		</tr>
		<tr>
			<td class="tablehead">Scheme:</td>
			<td class="tablehead">Linkage Type:</td>
			<td class="tablehead">Name (monosacch. included):</td>
			<td class="tablehead">Name (separate residue):</td>
			<td class="tablehead">Comment:</td>
		</tr>

		<#-- <tr class="bold">
			<td class="tablecontent">MonoSaccharideDB</td>
			<td class="tablecontent">any</td>
			<td class="tablecontent">${name}</td>
			<td class="tablecontent"></td>
			<td class="tablecontent">primary alias</td>
		</tr>-->
		<#list substTmpl.aliasList as alias>
		<tr<#if alias.isPrimary()> class="bold"</#if>>
			<td class="tablecontent">${alias.namescheme.nameStr}</td>
			<td class="tablecontent">${alias.linktype1}</td>
			<td class="tablecontent">${alias.residueIncludedName!}</td>
			<td class="tablecontent">${alias.separateDisplayName!}</td>
			<td class="tablecontent"><#if alias.isPrimary()>primary&nbsp;alias<#else>secondary&nbsp;alias</#if></td>
		</tr>
		</#list>
		</table>
	</td>
</tr>
</#if><#-- /notation tab -->
<#-- atoms tab: -->
<#if tab="atoms" || tab="all">
<tr class="outer">
	<td style="vertical-align:top;">
		<table width="100%" class="contenttable">
		<tr>
			<td colspan="4" class="tablehead">Atoms:</td>
		</tr>
		<tr>
			<td class="tablehead">Name:</td>
			<td class="tablehead">Element:</td>
			<td class="tablehead">Connections [Bond order]:</td>
			<td class="tablehead">Comments:</td>
		</tr>

		<#list substTmpl.atoms as atom>
		<tr>
			<td class="tablecontent">${atom.getName()}</td>
			<td class="tablecontent"><a href="show_element.action?id=${atom.element.periodicNumber}">${atom.element.getName()}</a> (${atom.element.symbol})</td>
			<td class="tablecontent">
				<#list atom.connections as conatom>
					${conatom.toAtom.name}&nbsp[${conatom.bondOrder}]
				</#list>
			</td>
			<td class="tablecontent">
				<#list substTmpl.validLinkingPositions as vlp>
					<#if vlp.replacedAtom??>
						<#if vlp.replacedAtom.id == atom.id>
							Deleted when linked at substituent position ${vlp.position?c}.
						</#if>
					</#if>
				</#list>
			</td>
		</tr>
		</#list>
		</table>
	</td>
</tr>
</#if><#-- /atoms tab -->
</table>
<!-- footer: -->
<#include  "./footer.ftl">