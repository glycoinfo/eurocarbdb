<#assign title>Create Biological Content</#assign>
<#include "/template/common/header.ftl" />

<h1>Create a biological content</h1>

<p>In order to ensure a full description of your experimental data is stored you are required to add the following data.</p>

<h2>Create Tissue</h2>

<form action="search_tissue_taxonomy.action">
  <table class="table_form">
    <tr>
      <td>
	<label for="search">
	  Enter a tissue taxonomy search term
	</label>
      </td>
      <td>
	<input type="hidden" name="profileId" value="${profileId}" />
	<input id="search" type="text" name="tissueTaxonomyName" />
	<input type="submit" value="Search for name -&gt;" />
      </td>
    </tr>
  </table>

</form>


<#include "/template/common/footer.ftl" />

