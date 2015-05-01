<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<#assign title>Browse structures</#assign>
<#include "/template/common/header.ftl" />

<h1>${title}</h1>

<form method="post">
<textarea name="sequences" rows="20" cols="60" ></textarea>
<select name="inputType">
    <option value="glycoct_condensed">GlycoCT</option>
</select>

</form>

<#include "/template/common/footer.ftl" />
