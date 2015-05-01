<#include "../header.ftl">
<div class="borderbox" width="100%">
	<h1>Simple queries:</h1><hr>
	<h2>Query MonoSaccharideDB by Id</h2>
	<form method="get" action="display_monosaccharide.action">
	<span class="bold">Enter Monosaccharide ID:</span> <input type="text" name="id" size="4">
	<input type="submit">
	</form><hr>
	<h2>Query MonosaccharideDB by Name</h2>
	<form method="get" action="query_monosaccharide_by_name.action">
	<span class="bold">Select Notation Scheme:</span>
	<select size="1" name="scheme">
		<option value="carbbank">CarbBank</option>
		<option value="glycosciences">GlycoSciences.de</option>
		<option value="msdb">MonoSaccharideDB</option>
		<option value="glycoct">GlycoCT</option>
		<option value="cfg">CFG</option>
		<option value="bcsdb">BCSDB</option>
		<option value="pdb">Protein Data Bank (PDB)</option>
	</select><br><br>
	<span class="bold">Enter Monosaccharide Name:</span>
	<input type="text" name="name" size="40">
	<br><br>
	<span class="bold">Separate Substitutions:</span><br>
	<#list 1..4 as counter>
	Position <input type="text" name="substMsPos" size="2"> Type <input type="text" name="substName" size="30">
	LinkageType <select name="substMsLinktype" size="1">
		<option value="">default</option>
		<option value="o">o (H_AT_OH)</option>
		<option value="d">d (DEOXY)</option>
		<option value="h">h (H_LOSE)</option>
	</select><br>
	</#list><br>
	<input type="submit">
	</form>
</div>

<div class="borderbox" width="100%">
	<h1>Advanced Queries:</h1>
	<ul>
	<li><a href="query_monosaccharide_by_properties.action">by Monosaccharide Properties</a></li>
	<!--</ul>
</div>

<div class="borderbox" width="100%">
	<h1>Monosaccharide Builder:</h1>
	<ul>-->
	<li><a href="build_monosaccharide_by_groups.action">Monosaccharide Builder</a></li>
	<!--<li>by notation</li>-->
	</ul>
</div>
<#include "../footer.ftl">