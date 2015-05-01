<#assign title>Contribute structure</#assign>
<#include "/template/common/header.ftl" />
<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />

<h1>${title}</h1>

<@ww.form theme="simple" method="post" >

<p>The structure you have entered is already existing. Do you want to
add a new biological context?<p>

<br/>

<input type="submit" name="submitAction" value="Cancel" />
<input type="submit" name="submitAction" value="Set context" />

</@ww.form>

<#include "/template/common/footer.ftl" />
