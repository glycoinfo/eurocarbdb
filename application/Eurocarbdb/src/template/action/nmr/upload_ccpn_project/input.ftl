<#import "/template/lib/Eurocarb.lib.ftl" as eurocarb />

<#assign title>Upload CCPN Project</#assign>

<#include "/template/common/header.ftl" />

<h1>${title}</h1>

<#-- show a query form -->
<@ww.form method="post" enctype="multipart/form-data">

<@ww.file label="CCPN Project" name="Project" />
<td colspan="2"><#-- the TD is a hack to get around WW rendering forms in a table -->
<input type="submit" value="Upload" />
</td>

</@ww.form>

<#include "/template/common/footer.ftl" />
