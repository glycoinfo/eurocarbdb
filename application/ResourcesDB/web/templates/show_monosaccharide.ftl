<#include "./header.ftl">
<table class="outer">
<tr>
	<td class="tablehead">Monosaccharide<#if id != 0> Id ${id?c}</#if>: ${ms.name}</td>
</tr>
<#-- tabs: -->
<tr class="tabsline">
	<td class="tabsline">
		<div id="tabs">
				<#if !tab??>
					<#assign tab = "residue">
				</#if>
				<#if tab = "residue">
					<span class="tabcell_selected">Residue</span>
				<#else>
					<span class="tabcell"><a href="display_monosaccharide.action<#if id != 0>?id=${id?c}<#else>?name=${ms.name}&amp;amp;scheme=msdb</#if>&amp;tab=residue" title="Show general information on this residue">Residue</a></span>
				</#if>
				<#if tab = "notation">
					<span class="tabcell_selected">Notation</span>
				<#else>
					<span class="tabcell"><a href="display_monosaccharide.action<#if id != 0>?id=${id?c}<#else>?name=${ms.name}&amp;scheme=msdb</#if>&amp;tab=notation" title="Show the names for this residue in various notations">Notation</a></span>
				</#if>
				<#if tab = "atoms">
					<span class="tabcell_selected">Atoms</span>
				<#else>
					<#if ms.atoms?? && ms.atoms?size gt 0>
					<span class="tabcell"><a href="display_monosaccharide.action<#if id != 0>?id=${id?c}<#else>?name=${ms.name}&amp;scheme=msdb</#if>&amp;tab=atoms" title="List the atoms this monosaccharide consists of">Atoms</a></span>
					<#else>
					<span class="tabcell" title="No atom information available">Atoms</span>
					</#if>
				</#if>
				<#if tab = "fragments">
					<span class="tabcell_selected">Fragments</span>
				<#else>
					<#if ms.hasFragmentData()>
					<span class="tabcell"><a href="display_monosaccharide.action<#if id != 0>?id=${id?c}<#else>?name=${ms.name}&amp;scheme=msdb</#if>&amp;tab=fragments" title="Show Cross Ring Fragments">Fragments</a></span>
					<#else>
					<span class="tabcell" title="No Cross Ring Fragments available">Fragments</span>
					</#if>
				</#if>
				<#if tab = "all">
					<span class="tabcell_selected">All</span>
				<#else>
					<span class="tabcell"><a href="display_monosaccharide.action<#if id != 0>?id=${id?c}<#else>?name=${ms.name}&amp;scheme=msdb</#if>&amp;tab=all" title="Show all data in one tab">All</a></span>
				</#if>
		</div>
	</td>
</tr>
<#-- /tabs -->

<#-- ### residue tab: #################################################################### -->
<#if tab = "residue" || tab="all">
<tr class="outer">
	<td style="vertical-align:top;">
		<table class="noborder"><#-- <div class="display_table"> -->
			<tr> <#-- <div class="display_row"> -->
				<td> <#-- <div class="display_cell"> -->
					<#-- monosaccharide: -->
					<table width="100%" class="contenttable">
						<tr>
							<td colspan="2" class="tablehead">Monosaccharide:</td>
						</tr>
						<tr>
							<td class="tablecontent">Id:</td>
							<td class="tablecontent"><#if id != 0>${id?c}<#else>not in database (data generated on the fly)</#if></td>
						</tr>
						<tr>
							<td class="tablecontent">Name:</td>
							<td class="tablecontent">${ms.name}</td>
						</tr>
						<#if ms.carbbankAlias??>
						<tr>
							<td class="tablecontent">CarbBank Name:</td>
							<td class="tablecontent">${ms.carbbankAlias.name}
							<#list ms.carbbankAlias.externalSubstList as subst>
							<br> + (${subst.getPosition1Str("/","?")}-${subst.getSubstituentPosition1Str("/","?")}) ${subst.name}
							</#list>
							</td>
						</tr>
						</#if>
						<tr>
							<td class="tablecontent">Composition:</td>
							<td class="tablecontent">${ms.composition.toWebFormula()}</td>
						</tr>
						<tr>
							<td class="tablecontent">Monoisotopic Mass:</td>
							<td class="tablecontent">${ms.monoMass?string(",##0.0")} g/mol</td>
						</tr>
						<tr>
							<td class="tablecontent">Avg. Mass:</td>
							<td class="tablecontent">${ms.avgMass?string(",##0.0")} g/mol</td>
						</tr>
						<#if ms.smiles??>
						<tr>
							<td class="tablecontent">Smiles:</td>
							<td class="tablecontent">${ms.smiles}</td>
						</tr>
						</#if>
						<#if ms.inchi??>
						<tr>
							<td class="tablecontent">InChi:</td>
							<td class="tablecontent">${ms.inchi}</td>
						</tr>
						</#if>
						<tr>
							<td class="tablecontent">Possible Linkage Positions:</td>
							<td class="tablecontent">
							<#list ms.possibleLinkingPositions as pos>
								${pos.position}
							</#list>
							(<a href="#linking_positions">details</a>)
							</td>
						</tr>
					</table>
					
					<#-- basetype: -->
					<table width="100%" class="contenttable">
						<tr>
							<td colspan="2" class="tablehead">Basetype:</td>
						</tr>
						<tr>
							<td class="tablecontent">Name:</td>
							<td class="tablecontent"><#if basetypeMsId gt 0 && basetypeMsId != id><a href="display_monosaccharide.action?id=${basetypeMsId}"></#if>${ms.basetype.name}<#if basetypeMsId gt 0></a></#if></td>
						</tr>
				
						<tr>
							<td class="tablecontent">Size:</td>
							<td class="tablecontent">${ms.size}</td>
						</tr>
						<tr>
							<td class="tablecontent">Anomeric:</td>
							<td class="tablecontent"><#if ms.anomer??>${ms.anomer.fullname!'?'}<#else>error</#if></td>
				
						</tr>
						<tr>
							<td class="tablecontent">Abs. Config:</td>
				
							<td class="tablecontent">${ms.configuration!' '}</td>
						</tr>
						<tr>
							<td class="tablecontent">Ring Type:</td>
							<td class="tablecontent">${ms.ringtype} <#if ms.ringStart gt 0 && ms.ringEnd gt 0>(${ms.ringStart} : ${ms.ringEnd})</#if></td>
						</tr>
						<tr>
							<td class="tablecontent">Stereocode: <a href="notation.action?topic=stereocode#stereocode" class="hint" title="A concise description of the basetype stereochemistry. Click for more info.">?</a></td>
							<td class="tablecontent">${ms.stereoStr}</td>
						</tr>
						<tr>
							<td class="tablecontent">Core Modifications: <a href="notation.action?topic=coremod#coremod" class="hint" title="Click to get a description of core modification types.">?</a></td>
							<td class="tablecontent">
								<#if ms.countCoreModifications() != 0>
									<#list ms.coreModifications as coreMod>
									${coreMod.getPosition1Str("/","?")} ${coreMod.name}<br>
									</#list>
								<#else>
									none
								</#if>
							</td>
						</tr>
						<tr>
							<td class="tablecontent">Composition:</td>
							<td class="tablecontent">${ms.basetype.composition.toWebFormula()}</td>
						</tr>
						<tr>
							<td class="tablecontent">Monoisotopic Mass:</td>
							<td class="tablecontent">${ms.basetype.monoMass?string(",##0.0")} g/mol</td>
						</tr>
						<tr>
							<td class="tablecontent">Avg. Mass:</td>
							<td class="tablecontent">${ms.basetype.avgMass?string(",##0.0")} g/mol</td>
						</tr>
						<#if ms.basetype.dbId gt 0>
						<tr>
							<td class="tablecontent" colspan="2">
								<a href="query_monosaccharide_by_properties.action?basetypeId=${ms.basetype.dbId?c}">find all monosaccharides with this basetype</a>
							</td>
						</tr>
						</#if>
					</table>
					
					<#-- substitutions: -->
					<table width="100%" class="contenttable">
						<tr>
							<td colspan="3" class="tablehead">Substituents:</td>
						</tr>
						<#if ms.countSubstitutions() = 0>
							<td class="tablecontent" colspan="3">none</td>
						<#else>
						<tr>
							<td class="tablehead">Position:</td>
							<td class="tablehead">Name:</td>
							<td class="tablehead">Linkage Type: <a href="notation.action?topic=linktype#linktype" class="hint_inverse" title="Click to get a description of linkage types.">?</a></td>
						</tr>
				
						<#list ms.substitutions as subst>
						<tr>
							<td class="tablecontent">${subst.getPosition1Str("/","?")}<#if subst.hasPosition2()>,${subst.getPosition2Str("/","?")}</#if></td>
							<td class="tablecontent"><a href="show_substituent.action?name=${subst.name}" title="click for more information on this substituent">${subst.name}</a></td>
							<td class="tablecontent">${subst.linkagetype1}<#if subst.hasPosition2()>, ${subst.linkagetype2}</#if></td>
						</tr>
						</#list>
						</#if>
					</table>
					
					<#-- linking positions: -->
					<table width="100%" class="contenttable" id="linking_positions">
						<tr>
							<td class="tablehead" colspan="2">Possible Linking Positions:</td>
						</tr>
						<tr>
							<td class="tablehead">Pos.</td>
							<td class="tablehead">Comment</td>
						</tr>
						<#list ms.possibleLinkingPositions as pos>
						<tr>
							<td class="tablecontent">
								${pos.position?c}
							</td>
							<td class="tablecontent">
								<#if pos.isAnomeric()>&bull; anomeric center<br></#if>
								<#if pos.linkingSubstitution??>&bull; via substituent ${pos.linkingSubstitution.name}</#if>
								<#if pos.comment??>&bull; ${pos.comment}</#if>
							</td>
						</tr>
						</#list>
					</table>
				</td> <#-- </div> -->
				
				<#-- representations: -->
				<td style="vertical-align:top;"> <#-- <div class="display_cell"> -->
					<#if ms.hasHaworth(defaultGraphicsFormat) || ms.hasFischer(defaultGraphicsFormat) ||  ms.getCfgImageId(defaultGraphicsFormat) gt 0 ||  ms.getOxfordImageId(defaultGraphicsFormat) gt 0>
					<#-- graphical: -->
					<div class="bold">Graphical representations:</div>
					</#if>
					<#assign hasStructureImage = false>
					<#if ms.getHaworthImageId(defaultGraphicsFormat) gt 0>
						<img src="get_ms_representation.action?representationId=${ms.getHaworthImageId(defaultGraphicsFormat)?c}" class="structure_image" title="Haworth representation" alt="">
						<#assign hasStructureImage = true>
					<#else>
						<#if id == 0 && ms.hasHaworth(defaultGraphicsFormat)>
							<img src="get_ms_representation.action?monosaccName=${ms.name}&amp;repType=haworth&amp;repFormat=PNG&amp;namescheme=msdb" class="structure_image" title="Haworth representation" alt="">
							<#assign hasStructureImage = true>
						</#if>
					</#if>
					<#if ms.getFischerImageId(defaultGraphicsFormat) gt 0>
						<img src="get_ms_representation.action?representationId=${ms.getFischerImageId(defaultGraphicsFormat)?c}" class="structure_image" alt="">
						<#assign hasStructureImage = true>
					<#else>
						<#if ms.hasFischer(defaultGraphicsFormat)>
							<img src="get_ms_representation.action?monosaccName=${ms.name}&amp;repType=fischer&amp;repFormat=PNG&amp;namescheme=msdb" class="structure_image" alt="">
							<#assign hasStructureImage = true>
						</#if>
					</#if>
					<#if hasStructureImage>
						<br><br>
					</#if>
					<#assign hasSymbolImage = false>
					<#if ms.getCfgImageId(defaultGraphicsFormat) gt 0>
						<#if hasSymbolImage == false>
						<span class="display_row">
						</#if>
						<span class="display_cell_topright">
						<span class="smalltext">CFG Symbol:<br></span>
						<img src="get_ms_representation.action?representationId=${ms.getCfgImageId(defaultGraphicsFormat)?c}" class="structure_image" title="CFG symbol" alt="">
						</span>
						<#assign hasSymbolImage = true>
					</#if>
					<#if ms.getCfgBwImageId(defaultGraphicsFormat) gt 0>
						<#if hasSymbolImage == false>
						<span class="display_row">
						</#if>
						<span class="display_cell_topright">
						<span class="smalltext">CFG Symbol (gray):<br></span>
						<img src="get_ms_representation.action?representationId=${ms.getCfgBwImageId(defaultGraphicsFormat)?c}" class="structure_image" title="CFG symbol (grayscale)" alt="">
						</span>
						<#assign hasSymbolImage = true>
					</#if>
					<#if ms.getOxfordImageId(defaultGraphicsFormat) gt 0>
						<#if hasSymbolImage == false>
						<span class="display_row">
						</#if>
						<span class="display_cell_topright">
						<span class="smalltext">Oxford Symbol:<br></span>
						<img src="get_ms_representation.action?representationId=${ms.getOxfordImageId(defaultGraphicsFormat)?c}" class="structure_image" title="Oxford symbol" alt="">
						</span>
						<#assign hasSymbolImage = true>
					</#if>
					<#if hasSymbolImage>
						</span>
						<br><br>
					</#if>
					<#-- coordinates: -->
					<#if ms.getPdbCoordinatesId() gt 0 || ms.getMol2CoordinatesId() gt 0 || ms.getChemCompId() gt 0>
						<div class="bold">3D structure:</div>
						<#if ms.getPdbCoordinatesId() gt 0>
						<script type="text/javascript">
							jmolInitialize("plugin/jmol");
							<!--jmolCheckBrowser("popup", "plugin/browsercheck", "onClick");-->
						</script>
						<script type="text/javascript">
							jmolApplet(300, "load get_ms_representation.action?representationId=${ms.getPdbCoordinatesId()?c}");
						</script>
						<br>	
						</#if>
						<span class="bold">Download 3D structure file:</span><br>
						<#if ms.getPdbCoordinatesId() gt 0>
						&bull; <a href="get_ms_representation.action?representationId=${ms.getPdbCoordinatesId()?c}">PDB format</a>
						</#if>
						<#if ms.getMol2CoordinatesId() gt 0>
						&bull; <a href="get_ms_representation.action?representationId=${ms.getMol2CoordinatesId()?c}">Mol2 format</a>
						</#if>
						<#if ms.getChemCompId() gt 0>
						&bull; <a href="get_ms_representation.action?representationId=${ms.getChemCompId()?c}">CCPN ChemComp</a>
						</#if>
					</#if>
				</td> <#-- </div> -->
			</tr> <#-- </div> -->
		</table> <#-- </div> -->
	</td>
</tr>
</#if><#-- /residue tab -->

<#-- ### notation tab: #################################################################### -->
<#if tab="notation" || tab="all">
<tr class="outer">
	<td style="vertical-align:top;">
		<table width="100%" class="contenttable">
		<tr>
			<td colspan="<#if ms.hasAliasWithResidueExcludedSubst()>4<#else>3</#if>" class="tablehead">Notation:</td>
		</tr>
		<tr>
			<td class="tablehead">Scheme: <a href="notation.action?topic=schemes" class="hint_inverse" title="The notation scheme, in which the alias name is valid. Click for more info.">?</a></td>
			<td class="tablehead">Name:</td>
			<#if ms.hasAliasWithResidueExcludedSubst()><td class="tablehead">Separate Substituents:</td></#if>
			<td class="tablehead">Comment:</td>
		</tr>

		<tr class="bold">
			<td class="tablecontent">MonoSaccharideDB</td>
			<td class="tablecontent">${ms.name}</td>
			<#if ms.hasAliasWithResidueExcludedSubst()><td class="tablecontent"></td></#if>
			<td class="tablecontent">primary alias</td>
		</tr>
		<#list ms.synonyms as alias>
		<tr<#if alias.isPrimary()> class="bold"</#if>>
			<td class="tablecontent">${alias.namescheme.nameStr}</td>
			<td class="tablecontent">${alias.name}</td>
			<#if ms.hasAliasWithResidueExcludedSubst()>
			<td class="tablecontent">
			<ul>
			<#list alias.externalSubstList as subst>
				<#if alias.namescheme.nameStr == "GlycoCT">
				<li>(${subst.getPosition1Str("/","?")}${subst.linkagetype1.getType()}-${subst.getSubstituentPosition1Str("/","?")}) ${subst.name}</li>
				<#else>
				<li>(${subst.getPosition1Str("/","?")}-${subst.getSubstituentPosition1Str("/","?")}) ${subst.name}</li>
				</#if>
			</#list>
			</ul>
			</td></#if>
			<td class="tablecontent"><#if alias.isPrimary()>primary&nbsp;alias<#else>secondary&nbsp;alias</#if><#if alias.isTrivialName()>, trivial&nbsp;name</#if></td>
		</tr>
		</#list>
		</table>
	</td>
</tr>
</#if><#-- /notation tab -->

<#-- ### atoms tab: #################################################################### -->
<#if tab="atoms" || tab="all">
<tr class="outer">
	<td style="vertical-align:top;">
	<#if ms.atoms?? && ms.atoms?size gt 0>
		<table width="100%" class="contenttable">
		<tr>
			<td colspan="3" class="tablehead">Atoms:</td>
		</tr>
		<tr>
			<td class="tablehead">Name:</td>
			<td class="tablehead">Element:</td>
			<td class="tablehead">Connections [Bond order]:</td>
		</tr>

		<#list ms.atoms as atom>
		<tr>
			<td class="tablecontent">${atom.name}</td>
			<td class="tablecontent"><a href="show_element.action?id=${atom.element.periodicNumber?c}">${atom.element.getName()}</a> (${atom.element.symbol})</td>
			<td class="tablecontent">
				<#list atom.connections as conatom>
					${conatom.toAtom.name}&nbsp[${conatom.bondOrder}]
				</#list>
			</td>
		</tr>
		</#list>
		</table>
	<#else>
	No atoms available for this monosaccharide.
	</#if>
	</td>
</tr>
</#if><#-- /atoms tab -->

<#-- ### fragments tab: #################################################################### -->
<#if tab="fragments" || (tab="all" && ms.hasFragmentData())>
<tr class="outer">
	<td style="vertical-align:top;">
		<table width="100%" class="contenttable">
		<tr>
			<td class="tablehead">Cross Ring Fragments:</td>
		</tr>
		<tr>
			<td class="tablecontent">Sorry, fragmentation data not yet available.</td>
		</tr>
		</table>
	</td>
</tr>
</#if><#-- /fragments tab -->
</table>
<#include "./footer.ftl">