<#include "../header.ftl">
<h1>Help on Monosaccharide Entry Pages</h1>
<table class="outer">
<tr>
	<td class="longtext">
		The information about the monosaccharides is organized in tabs. Data that are found on the separate tabs are listed below. The tab labelled "all" contains all data at a glance.<br><br>
	</td>
</tr>
<tr>
	<td style="vertical-align:top;">
		<table width="100%" class="contenttable">
			<tr>
				<td class="tablehead"><a name="residue" class="anchor"></a>Residue tab:</td>
			</tr>
			<tr>
				<td class="tablecontent">
					<div class="longtext">
					The residue tab is shows the general information about the monosaccharide. This includes the name, the composition, the average and monoisotopic masses, and graphical representations of the monosaccharide.
					Furthermore, the monosaccharide basetype (i.e. the (CH<sub>2</sub>O)<sub>n</sub> backbone and the core modifications such as deoxygenations or terminal carboxyl groups) is described in detail and, if present, the substituents are listed.
					</div>
				</td>
			</tr>
		</table>
	</td>
</tr>
<tr>
	<td style="vertical-align:top;">
		<table width="100%" class="contenttable">
			<tr>
				<td class="tablehead"><a name="notation"></a>Notation tab:</td>
			</tr>
			<tr>
				<td class="tablecontent">
					<div class="longtext">
					The notation tab contains information about the nomenclature of the monosaccharides in various notation schemes.
					The table consist of three columns: The first one gives the name of the notation scheme, such as CarbBank or GlycoCT, for instance. The second column states the name of the monosaccharide in that notation.
					For some residues, there might be more than one valid name in some notation schemes, e.g. a trivial name and a systematical name. Therefore, MonoSaccharideDB distinguishes primary and secondary alias names.
					For translation into a notation only primary alias names shall be used.
					The information if a residue name is a primary or a secondary alias is given in the third column ("comments"). This column also indicates if a residue name is a trivial name. Primary aliases are printed in bold.
					</div>
				</td>
			</tr>
		</table>
	</td>
</tr>
<tr>
	<td style="vertical-align:top;">
		<table width="100%" class="contenttable">
			<tr>
				<td class="tablehead"><a name="atoms"></a>Atoms tab:</td>
			</tr>
			<tr>
				<td class="tablecontent">
					<div class="longtext">
					In the atoms tab the atoms are listed. For each atom of the monosaccharide the name, the periodic element and the connections to other atoms is given. The bond order of the connections is given in square brackets. 
					</div>
				</td>
			</tr>
		</table>
	</td>
</tr>
<tr>
	<td style="vertical-align:top;">
		<table width="100%" class="contenttable">
			<tr>
				<td class="tablehead"><a name="fragments"></a>Cross Ring Fragments:</td>
			</tr>
			<tr>
				<td class="tablecontent">
					<div class="longtext">
					This tab will list the cross ring fragments that can be observed for the monosaccharide in mass spectrometrie. The information is not yet implemented.
					</div>
				</td>
			</tr>
		</table>
	</td>
</tr>
</table>
<#include "../footer.ftl">