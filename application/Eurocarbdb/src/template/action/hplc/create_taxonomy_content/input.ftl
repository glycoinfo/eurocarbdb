<#assign title>Create Biological Content</#assign>
<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />


<#include "/template/common/header.ftl" />

<h1>Create a biological content</h1>

<p>To ensure a full and accurate description of your experimental data is stored you are required to add the following data:</p>
<ul><li>Taxonomy Information</li><li>Tissue Type</li><li>Disease State</li></ul>

<p></p>
<p>To skip this step click <a href="doUpload.action?profileId=${profileId}">here.</a></p>

<h2>Create Taxonomy</h2>

<form action="search_taxonomy.action">
<table class="table_form">

<tr><!-- a specific ecdb taxonomy id -->

<tr><!-- taxonomy name/term -->
    <td>
        <label for="search_name">
        Enter a taxonomy search term 
        </label>
    </td>
    <td>
        <input type="hidden" name="profileId" value="${profileId}" />
        <input id="search_name" type="text" name="taxonomyName" />
        <input type="submit" value="Search for term -&gt;" />
    </td>
</tr>
</table>

</form>

<#include "/template/common/footer.ftl" />

