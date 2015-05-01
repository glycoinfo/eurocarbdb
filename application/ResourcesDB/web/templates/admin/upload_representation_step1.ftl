<#include "../header.ftl">
<span class="bold">Upload residue representation</span><br><br>
<#if errorMsg??>
<span class="error">${errorMsg}</span><br><br>
</#if>
<form method="get" action="upload_representation.action">
Step 1: Select database entry:<br><br>
Select molecule type:
<select name="moleculeClass" size="1">
<option value="monosaccharide">Monosaccharide</option>
<option value="substituent">Substituent</option>
<option value="aglycon">Aglycon</option>
</select><br>
Enter molecule id: </span><input type="text" name="moleculeId" size="3"><span class="bold"> OR</span> molecule name: <input type="text" name="moleculeName" size="20"><br>
<input type="submit">
</form>
<#include "../footer.ftl">