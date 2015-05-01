<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />

<#assign title>Reset Status</#assign>

<#include "/template/common/header.ftl" />



                <h1>${title}</h1>

                <#if ( message?length > 0 )>
                <div class="error_message" style="text-align:center">
                  ${message}
                </div>
                </#if>
<center>
<p>
    Please go back to login page.
    <a href="login.action">Login</a>. 
</p>
</center>

<#include "/template/common/footer.ftl" />
