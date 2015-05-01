<#assign title>Disease fulltext search</#assign>
<#include "/template/common/header.ftl" />

<h1>${title}</h1>


<!-- search for name or synonym -->
<form>

  <table class="table_form">
    <tr>
      <td>
	<label for="search">
	  Enter a disease search term
	</label>
      </td>
      <td>
	<input id="search" type="text" name="diseaseName" />
	<input type="submit" value="Search for name -&gt;" />
      </td>
    </tr>
  </table>
</form>

<#include "/template/common/footer.ftl" />