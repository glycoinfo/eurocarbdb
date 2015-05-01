<#include "../header.ftl">
<span class="bold">Upload residue representation</span><br><br>
<#if errorMsg??><span class="error">${errorMsg}</span><br><br></#if>
Residue type: ${moleculeClass}<br>
<#if moleculeClass=="monosaccharide">
Residue: Monosaccharide ${ms.dbId}: ${ms.name} / ${ms.carbbankName!'-'}<br><br>
<#--><#if format??>format: ${format} / </#if><#if type??>type ${type} / </#if><#if data??>data set<#else> data is null</#if><br></#-->
</#if>
Select representation to upload:<br>
<form method="post" action="upload_representation.action" enctype="multipart/form-data">
<input type="hidden" name="moleculeClass" value="${moleculeClass}">
<input type="hidden" name="moleculeId" value="${moleculeId}">
<#if moleculeName??><input type="hidden" name="moleculeName" value="${moleculeName}"></#if>
<select name="type" size="1">
	<option value="haworth"<#if type?? && type="haworth"> selected</#if>>Haworth</option>
	<option value="fischer"<#if type?? && type="fischer"> selected</#if>>Fischer</option>
	<option value="coordinates"<#if type?? && type="coordinates"> selected</#if>>3D Coord.</option>
	<option value="cfg_symbol"<#if type?? && type="cfg_symbol"> selected</#if>>CFG</option>
	<option value="cfg_symbol_bw"<#if type?? && type="cfg_symbol_bw"> selected</#if>>CFG (gray)</option>
	<option value="oxford_symbol"<#if type?? && type="oxford_symbol"> selected</#if>>Oxford</option>
</select>
<select name="format" size="1">
	<option value="png"<#if format?? && format="png"> selected</#if>>PNG</option>
	<option value="jpg"<#if format?? && format="jpg"> selected</#if>>JPG</option>
	<option value="svg"<#if format?? && format="svg"> selected</#if>>SVG</option>
	<option value="gif"<#if format?? && format="gif"> selected</#if>>GIF</option>
	<option value="pdb"<#if format?? && format="pdb"> selected</#if>>PDB</option>
	<option value="mol2"<#if format?? && format="mol2"> selected</#if>>Mol2</option>
	<option value="chem_comp"<#if format?? && format="chem_comp"> selected</#if>>ChemComp</option>
</select>
<input type="file" name="dataFile"><br>
<input type="submit">
</form>
<#include "../footer.ftl">