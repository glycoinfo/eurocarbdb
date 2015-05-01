<#assign title>Tissue Taxonomy search</#assign>
<#include "/template/common/header.ftl" />

<#import "/template/lib/FormInput.lib.ftl" as input />

<h1>${title}</h1>

<!-- search for name or synonym -->
<form>

  <table class="table_form">
    <tr>
      <td>
	<label for="search">
	  Enter a tissue taxonomy search term
	</label>
      </td>
      <td>
	<input id="search" type="text" name="tissueTaxonomyName" />
	<input type="submit" value="Search for name -&gt;" />
      </td>
    </tr>
  </table>

</form>

<#include "/template/common/footer.ftl" />