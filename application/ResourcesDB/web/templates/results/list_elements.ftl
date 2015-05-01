<#include "../header.ftl">

<h1>Elements</h1>
				<table class="contenttable">
					<tr>
						<td class="tablehead">Periodic Number</td>
						<td class="tablehead">Symbol</td>
						<td class="tablehead">Name</td>
						<td class="tablehead">Avg. Mass</td>
						<td class="tablehead">Stable</td>
						<td class="tablehead">Known Isotopes</td>
						<td class="tablehead">Most Abundant Isotope</td>
					</tr>
			
					<#list elementsList as periodic>
					<tr>
						<td class="tablecontent">${periodic.periodicNumber}</td>
						<td class="tablecontent">${periodic.symbol}</td>
						<td class="tablecontent"><a href="show_element.action?id=${periodic.periodicNumber}">${periodic.getName()}</a></td>
						<td class="tablecontent">${periodic.avgMass?c}</td>
						<td class="tablecontent"><#if periodic.isStable()>yes<#else>no</#if></td>
						<td class="tablecontent"><a href="show_element.action?id=${periodic.periodicNumber}#isotopes">${periodic.getIsotopesCount()}</a></td>
						<td class="tablecontent"><#if periodic.getMostAbundantIsotope().abundance!=0><sup>#{periodic.getMostAbundantIsotope().mass;m0}</sup>${periodic.symbol}<#else>-</#if></td>
					</tr>
					</#list>
				</table>

<#include "../footer.ftl">