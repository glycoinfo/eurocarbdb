<#include "../header.ftl">
<h1>Basetype Notation</h1>

<p class="longtext">
The basetype of a monosaccharide describes residue size, the <a href="#stereocode">stereochemistry</a> (incl. <a href="#configuration">absolute configuration</a> and <a href="#anomer">anomeric</a>) and the ring closure.
In addition, it may contain a number of <a href="#coremod">core modifications</a>.
</p>

<h3 id="configuration">Absolute Configuration</h3>

<p class="longtext">
For the use of the configurational symbols and prefixes, see the <a href="http://www.chem.qmul.ac.uk/iupac/2carb/03n04.html#04" class="external_link" target="_blank">IUPAC definition 2-Carb-4</a>.
</p>

<h3 id="anomer">Anomeric</h3>

<p class="longtext">
For the definition of the anomeric, see the <a href="http://www.chem.qmul.ac.uk/iupac/2carb/06n07.html#06" class="external_link" target="_blank">IUPAC definition 2-Carb-6</a>.
</p>

<h3 id="stereocode">Stereochemistry</h3>

<p class="longtext">
Many monosaccharides only differ in the stereochemistry of the basetype backbone carbons.
In most notations, this stereochemistry is denoted using the <a href="http://www.chem.qmul.ac.uk/iupac/2carb/02.html#0222" class="external_link" target="_blank">IUPAC stem type</a> ("parent") names.
</p>
<p class="longtext">
In addition to this indirect description of the stereochemistry based on parent names, MonoSaccharideDB features a <span class="italic">Stereocode</span> field, which contains a direct description of the stereochemistry.
The stereocode is a String that contains one character for each carbon of the basetype backbone.
"1" indicates that the corresponding carbon is in <span class="configuration_label">L</span>-Configuration (OH-group pointing left in Fischer projection), "2" marks a <span class="configuration_label">D</span>-Configuration (OH-group pointing right in Fischer projection), and "0" is used to describe achiral positions.
<span class="configuration_label">D</span>-Glucose in open chain form, for example, has the stereocode "021220":<br>
<img src="images/D-Glc-Stereocode.png" alt="D-Glucose Stereocode">
</p>
<p class="longtext">
When a ring is formed from this, the anomeric center (position 1 in this example) becomes a chiral atom and thus the stereocode of that position is adjusted depending on the anomer.
For example, the stereocode of &beta;-<span class="configuration_label">D</span>-Glc<span class="italic">p</span> is "121220", that of &alpha;-<span class="configuration_label">D</span>-Glc<span class="italic">p</span> is "221220".
</p>
<p class="longtext">
In case MonoSaccharideDB is queried with a residue name, in which the absolute configuration is not given (e.g. "a-Fucp" in <a href="notation.action?topic=schemes#carbbank">CarbBank</a> notation), the stereocode is given based on the <span class="configuration_label">D</span>-Configuration, and "1" and "2" are replaced by "3" and "4", respectively.
Thus, the stereocode "443340" is assigned to the CarbBank residue "a-Fucp".
</p>
<br>
<h3 id="coremod">Core Modifications</h3>
<p class="longtext">
The monosaccharide basetype can feature a number of core modifications.
Several of them result in achiral positions and thus influence stereochemistry.
</p>
<p class="longtext">
The subsequent table summarizes the core modifications that are used in MonoSaccharideDB.
</p>
<table class="contenttable">
<tr>
	<td class="tablehead">Name</td>
	<td class="tablehead">Description</td>
	<td class="tablehead">Valence</td>
	<td class="tablehead">Comment</td>
</tr>
<tr>
	<td class="tablecontent">DEOXY</td>
	<td class="tablecontent">Deoxygenation of a position: The OH group is removed and replaced by a hydrogen atom.</td>
	<td class="tablecontent">1</td>
	<td class="tablecontent">&bull; results in achiral position</td>
</tr>
<tr>
	<td class="tablecontent">KETO</td>
	<td class="tablecontent">A carbonyl group in the open chain version of a monosaccharide. This modification is omitted if it is only present at position 1 (standard aldose).</td>
	<td class="tablecontent">1</td>
	<td class="tablecontent">&bull; results in achiral position</td>
</tr>
<tr>
	<td class="tablecontent">ALDI</td>
	<td class="tablecontent">Alditol: Reduction of the aldehyde group to CH<sub>2</sub>OH.</td>
	<td class="tablecontent">1</td>
	<td class="tablecontent">&bull; always at position 1</td>
</tr>
<tr>
	<td class="tablecontent">ACID</td>
	<td class="tablecontent">Carboxyl (COOH) group.</td>
	<td class="tablecontent">1</td>
	<td class="tablecontent">&bull; always at terminal position</td>
</tr>
<tr>
	<td class="tablecontent">EN</td>
	<td class="tablecontent">Double bond in the basetype backbone. This modification implies that - unless explicitly stated with a deoxy modification - hydroxyl groups are preserved.</td>
	<td class="tablecontent">2</td>
	<td class="tablecontent">&bull; results in achiral positions</td>
</tr>
<tr>
	<td class="tablecontent">ENX</td>
	<td class="tablecontent">Double bond in the basetype backbone with unknown deoxygenation pattern.</td>
	<td class="tablecontent">2</td>
	<td class="tablecontent">&bull; results in achiral positions</td>
</tr>
<tr>
	<td class="tablecontent">YN</td>
	<td class="tablecontent">Triple bond in the basetype backbone.</td>
	<td class="tablecontent">2</td>
	<td class="tablecontent">&bull; results in achiral positions</td>
</tr>
<tr>
	<td class="tablecontent">ANHYDRO</td>
	<td class="tablecontent">Intramolecular anhydride.</td>
	<td class="tablecontent">2</td>
	<td class="tablecontent"></td>
</tr>
<tr>
	<td class="tablecontent">EPOXY</td>
	<td class="tablecontent">Intramolecular anhydride at neighboring positions.</td>
	<td class="tablecontent">2</td>
	<td class="tablecontent"></td>
</tr><#-->
<tr>
	<td class="tablecontent">LACTONE</td>
	<td class="tablecontent">Acid function that is involved in ring closure.</td>
	<td class="tablecontent">2</td>
	<td class="tablecontent">&bull; results in achiral anomeric center</td>
</tr><-->
<tr>
	<td class="tablecontent">SP</td>
	<td class="tablecontent">Triple bond to a substituent.</td>
	<td class="tablecontent">1</td>
	<td class="tablecontent">&bull; only possible at terminal positions<br>&bull; always in combination with substituent</td>
</tr>
<tr>
	<td class="tablecontent">SP2</td>
	<td class="tablecontent">Double bond to a substituent.</td>
	<td class="tablecontent">1</td>
	<td class="tablecontent">&bull; results in achiral position<br>&bull; always in combination with substituent</td>
</tr>
<tr>
	<td class="tablecontent">GEMINAL</td>
	<td class="tablecontent">Loss of stereochemistry due to identical substituents with DEOXY and H_LOSE linkage types at a single position.</td>
	<td class="tablecontent">1</td>
	<td class="tablecontent">&bull; results in achiral position<br>&bull; always in combination with substituents</td>
</tr>
</table>
<br>
<#include "../footer.ftl">