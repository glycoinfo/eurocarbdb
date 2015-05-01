<#include "../header.ftl">
<h1>Query Elements by Periodic Number</h1>
<form method="get" action="show_element.action">
Periodic Number: <input type="text" name="id" size="4">
<input type="submit">
</form>
<h1>Query Elements by Symbol</h1>
<form method="get" action="show_element.action">
Element Symbol:
<input type="text" name="symbol" size="40">
<input type="submit">
</form>

<#include "../footer.ftl">