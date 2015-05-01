<#assign title>New Detector</#assign>
<#include "/template/common/header.ftl" />

<h1>New Detector Entry</h1>


<@ww.form>
<table >
<tr><td>Manufacturer: </td>
<td><@ww.textfield  name="manufacturer"/></td></tr>
<tr><td>Model: </td>
<td><@ww.textfield name="model"/></td></tr>
<!--
<tr><td>Excitation: </td>
<td><@ww.textfield  name="excitation"/></td>
<tr><td>Emission: </td>
<td><@ww.textfield  name="emission"/></td>
<tr><td>Bandwidth: </td>
<td><@ww.textfield  name="bandwidth"/></td>
<tr><td>Sampling Rate: </td>
<td><@ww.textfield  name="samplingRate"/></td>
-->
<tr><td></td><td><@ww.submit name="submit"/></td></tr>
</table>
</@ww.form>

<#include "/template/common/footer.ftl" />


