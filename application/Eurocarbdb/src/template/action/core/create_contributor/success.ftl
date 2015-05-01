<#assign title>Contributor created</#assign>
<#include "/template/common/header.ftl" />

<#import "/template/lib/TextUtils.lib.ftl" as text />

<h1>${title}</h1>

<p>New user '${ contributor.contributorName }' created</p>

<@ww.form action="contribute"><@ww.submit value="Continue ->" /></@ww.form> 

<#include "/template/common/footer.ftl" />
