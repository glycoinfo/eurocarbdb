<#include "./header.ftl">
<table class="outer">
<tr>
	<td class="tablehead">Element ${element.getName()}</td>
</tr>
<tr>
	<td style="vertical-align:top;">
		<div class="display_table">
			<div class="display_row">
				<div class="display_cell">
					<table width="100%" class="contenttable">
						<tr>
							<td colspan="2" class="tablehead">Properties:</td>
						</tr>
						<tr>
							<td class="tablecontent">Periodic Number:</td>
							<td class="tablecontent">${element.periodicNumber?string("##0")}</td>
						</tr>
						<tr>
							<td class="tablecontent">Symbol:</td>
							<td class="tablecontent">${element.symbol}</td>
						</tr>
						<tr>
							<td class="tablecontent">Name:</td>
							<td class="tablecontent">${element.getName()}</td>
						</tr>
						<tr>
							<td class="tablecontent">Average Mass:</td>
							<td class="tablecontent">${element.avgMass?string(",##0.########")} g/mol</td>
						</tr>
						<tr>
							<td class="tablecontent">Stable:</td>
							<td class="tablecontent"><#if element.isStable()>yes<#else>no</#if></td>
						</tr>
						<tr>
							<td class="tablecontent">Density:</td>
							<td class="tablecontent"><#if element.density??>${element.density?string(",##0.0")}<#else>?</#if></td>
						</tr>
						<tr>
							<td class="tablecontent">Boiling Point:</td>
							<td class="tablecontent"><#if element.boilingPoint??>${element.boilingPoint?string(",##0.0")}°C<#else>?</#if></td>
						</tr>
						<tr>
							<td class="tablecontent">Melting Point:</td>
							<td class="tablecontent"><#if element.meltingPoint??>${element.meltingPoint?string(",##0.0")}°C<#else>?</#if></td>
						</tr>
						<tr>
							<td class="tablecontent">Specific Heat:</td>
							<td class="tablecontent"><#if element.specificHeat??>${element.specificHeat?string(",##0.0")} J/(g*K)<#else>?</#if></td>
						</tr>
						<tr>
							<td class="tablecontent">Ionisation Potential:</td>
							<td class="tablecontent"><#if element.ionisationPotential??>${element.ionisationPotential?string(",##0.0")} eV<#else>?</#if></td>
						</tr>
					</table>
				</div>
				<div class="display_cell">
					<div style="border: thin solid red;margin:5px;">
						<div style="margin:5px;">
							${element.periodicNumber?string("##0")}
						</div>
						<div style="margin:5px;margin-left:15px;font-size:2.5em;">
							${element.symbol}
						</div>
						<div style="margin:5px;">
							${element.avgMass?string(",##0.########")}
						</div>
					</div>
				</div>
				<div class="display_cell">
					<a name="isotopes"></a>
					<table width="100%" class="contenttable">
						<tr>
							<td colspan="7" class="tablehead">Isotopes:</td>
						</tr>
						<tr>
							<td class="tablehead">Name</td>
							<td class="tablehead">Neutrons</td>
							<td class="tablehead">Mass</td>
							<td class="tablehead">Abundance</td>
							<td class="tablehead">Spin</td>
							<td class="tablehead">Stable</td>
							<td class="tablehead">Half Life</td>
						</tr>
						<#if element.getIsotopesCount() = 0>
						<tr>
							<td colspan="7">no isotopes in database</td>
						</tr>
						<#else>
						<#list element.getIsotopes() as isotope>
						<tr<#if isotope.stable> class="bold"</#if>>
							<td class="tablecontent"><#if isotope.periodicSymbol??>${isotope.periodicSymbol}<#else><sup>#{isotope.mass;m0}</sup>${element.symbol}</#if><#if isotope.commonName??> (${isotope.commonName})</#if></td>
							<td class="tablecontent">${isotope.neutrons}</td>
							<td class="tablecontent">${isotope.mass?string(",##0.###########")}</td>
							<td class="tablecontent">${isotope.abundance?string(",##0.########")}</td>
							<td class="tablecontent"><#if isotope.spin??>${isotope.spin}<#else>-</#if></td>
							<td class="tablecontent"><#if isotope.stable>yes<#else>no</#if></td>
							<td class="tablecontent"><#if isotope.halfLife??>${isotope.halfLife}<#else>-</#if></td>
						</tr>
						</#list>
						</#if>
					</table>
				</div>
			</div>
		</div>
	</td>
</tr>
</table>
<#include "./footer.ftl">