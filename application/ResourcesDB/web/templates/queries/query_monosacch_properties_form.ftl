<#include "../header.ftl">
<h1>Query Monosaccharide by Properties</h1>
<form method="get" action="query_monosaccharide_by_properties.action">
<span class="searchfield_title" title="The number of backbone carbons">Size:</span>
<input type="text" name="size" size="2">
<br>

<span class="searchfield_title" title="The anomeric of the basetype">Anomeric:</span>
<select name="anomer" size="1">
	<option value="">any</option>
	<option value="a">alpha</option>
	<option value="b">beta</option>
	<option value="n">none</option>
</select>
<br>

<span class="searchfield_title" title="The abs. configuration of the basetype">Configuration:</span>
<select name="configuration" size="1">
	<option value="">any</option>
	<option value="d">D</option>
	<option value="l">L</option>
</select>
<br>

<span class="searchfield_title" title="The stereocode of the basetype">Stereocode:</span>
<input type="text" name="stereocode" size="8">
<br>

<span class="searchfield_title" title="The Iupac parent residue name of the basetype">IUPAC Parent Name</span><sup>*</sup>:
<select name="parentname" size="1">
	<option value="">any</option>
	<option value="glc">Glc (Glucose)</option>
	<option value="gal">Gal (Galactose)</option>
	<option value="man">Man (Mannose)</option>
	<option value="fuc">Fuc (Fucose)</option>
	<option value="rha">Rha (Rhamnose)</option>
	<option value="neu">Neu (Neuraminic Acid)</option>
	<option value="ido">Ido (Idose)</option>
</select>
<br>

<table class="hidden_table">
<tr>
<td>
	<span class="searchfield_title" title="Modifications of the basetype">Core Modifications:</span><br>
	<div class="searchfield_subline">
		Position <input type="text" name="corePos1" size="2"> Type
			<select name="coreMod1" size="1">
				<option value="">select</option>
				<option value="deoxy">deoxy</option>
				<option value="en">en</option>
				<option value="acid">carboxyl group (acid)</option>
				<option value="keto">carbonyl group (keto)</option>
			</select>
	</div>
	<div class="searchfield_subline">
		Position <input type="text" name="corePos2" size="2"> Type
			<select name="coreMod2" size="1">
				<option value="">select</option>
				<option value="deoxy">deoxy</option>
				<option value="en">en</option>
				<option value="acid">carboxyl group (acid)</option>
				<option value="keto">carbonyl group (keto)</option>
			</select>
	</div>
	<div class="searchfield_subline">
		Position <input type="text" name="corePos3" size="2"> Type
			<select name="coreMod3" size="1">
				<option value="">select</option>
				<option value="deoxy">deoxy</option>
				<option value="en">en</option>
				<option value="acid">carboxyl group (acid)</option>
				<option value="keto">carbonyl group (keto)</option>
			</select>
	</div>
</td>
<td>
	<span class="searchfield_title" title="Substituents added to the basetype">Substitutions:</span><br>
	<div class="searchfield_subline">
		Position <input type="text" name="substPos1" size="2">
		Type <select name="substName1" size="1">
				<option value="">select</option>
				<#list substituentTemplateNamesList as substName>
				<option value="${substName}">${substName}</option>
				</#list>
			</select>
	</div>
	<div class="searchfield_subline">
		Position <input type="text" name="substPos2" size="2">
		Type <select name="substName2" size="1">
				<option value="">select</option>
				<#list substituentTemplateNamesList as substName>
				<option value="${substName}">${substName}</option>
				</#list>
			</select>
	</div>
	<div class="searchfield_subline">
		Position <input type="text" name="substPos3" size="2">
		Type <select name="substName3" size="1">
				<option value="">select</option>
				<#list substituentTemplateNamesList as substName>
				<option value="${substName}">${substName}</option>
				</#list>
			</select>
	</div>
</td>
</tr>
</table>
<br>
<div class="searchfield_title">Find monosaccharides that contain <input type="radio" name="logicalOperator" value="and" checked> all or <input type="radio" name="logicalOperator" value="or"> any of the given parameters.<br>
<input type="submit">
</div>
</form>
<#include "../footer.ftl">