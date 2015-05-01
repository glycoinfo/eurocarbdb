<#assign title>Taxonomy search</#assign>
<#include "/template/common/header.ftl" />

<h1>${title}</h1>

<form>
  
<table class="table_form">

<tr><!-- a specific ecdb taxonomy id -->
    <td>
        <label for="search_id">
        Enter a Eurocarb Taxonomy ID 
        </label>  
    </td>
    <td>
        <input id="search_id" type="text" name="taxonomyID" />
        <input type="submit" value="Search for taxonomy ID -&gt;" />   
    </td>
</tr>

<tr><!-- a specific NCBI id -->
    <td>
        <label for="search_ncbi">
        Enter a NCBI Taxonomy ID 
        </label>  
    </td>
    <td>
        <input id="search_ncbi" type="text" name="ncbiID" />
        <input type="submit" value="Search for NCBI ID -&gt;" />   
    </td>
</tr>

<tr><!-- taxonomy name/term -->
    <td>
        <label for="search_name">
        Enter a taxonomy search term 
        </label>
    </td>
    <td>
        <input id="search_name" type="text" name="taxonomyName" />
        <input type="submit" value="Search for term -&gt;" />
    </td>
</tr>
</table>

</form>

<#include "/template/common/footer.ftl" />