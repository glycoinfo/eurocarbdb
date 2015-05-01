<#include "../header.ftl">
<#--><h1>Simple queries:</h1><-->
<h2>Query Substituent by Name</h2>
<form method="get" action="show_substituent.action">
Enter Substituent Name:
<input type="text" name="name" size="40">
Select Notation Scheme:
<select size="1" name="scheme">
	<option value="carbbank">CarbBank</option>
	<option value="glycosciences">GlycoSciences.de</option>
	<option value="msdb">MonoSaccharideDB</option>
	<option value="glycoct">GlycoCT</option>
	<option value="cfg">CFG</option>
	<option value="bcsdb">BCSDB</option>
</select>
<input type="submit">
</form>
<#--><hr>
<h1>Advanced Queries:</h1>
<ul>
<li><a href="query_substituent.action">by Substituent Properties</a></li>
</ul>
<hr><-->
<#include "../footer.ftl">