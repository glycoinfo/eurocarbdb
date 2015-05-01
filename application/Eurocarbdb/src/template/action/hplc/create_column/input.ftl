<#assign title>Create Column</#assign>
<#include "/template/common/header.ftl" />

<h1>New Column Entry</h1>

<@ww.form>
<table>
<tr><td>Maufacturer: </td>
<td><@ww.textfield   name="manufacturer"/><span class="hint">Manufacturer name</span></td>
</tr>
<tr><td>Model: </td>
<td><@ww.textfield   name="model"/></td>
</tr>
<tr><td>Packing Material: </td>
<td><@ww.textfield  name="packingMaterial"/></td>
</tr>
<tr><td>Column Width: </td>
<td><@ww.textfield  name="columnSizeWidth"/></td>
</tr>
<tr><td>Column Length: </td>
<td><@ww.textfield  name="columnSizeLength"/></td>
</tr>
<tr><td>Particle Size: </td>
<td><@ww.textfield  name="particleSize"/></td>
</tr>

<tr><td></td><td><@ww.submit/></td></tr>
</table>
</@ww.form>

<#include "/template/common/footer.ftl" />

