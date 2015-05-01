<#assign title>Login error</#assign>
<#include "/template/common/header.ftl" />

<h1>${title}</h1>

<@ww.form action="login" method="post">
<p>
    User '${username}' doesn't exist. 
</p>

<@ww.submit value="Continue ->" />
</@ww.form>

<#include "/template/common/footer.ftl" />
