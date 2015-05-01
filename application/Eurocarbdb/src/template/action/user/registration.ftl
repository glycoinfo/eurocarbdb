
<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />

<#assign title>Registration</#assign>

<#include "/template/common/header.ftl" />



                <h1>${title}</h1>
                <#if ( message?length > 0 )>
                <div class="error_message">
                  ${message}
                </div>
                </#if>            
            <@ww.form theme="simple" method="post">
                <table class="table_form">
                    <tr><th>Full Name*:</th><td><@ww.textfield name="fullName" /></td></tr>
                    <tr><th>Institution/Lab*:</th><td><@ww.textfield name="institution" /></td></tr>
                    <tr><th>E-mail*:</th><td><@ww.textfield name="email" /></td></tr>
                    <tr><th>Login Name*:</th><td><@ww.textfield name="loginName" /></td></tr>                    
                    <tr><td colspan="2" align="center"><@ww.submit value="Register"/>  <@ww.reset value="Clear"/></td></tr>
                </table>
            </@ww.form>
        
<#include "/template/common/footer.ftl" />
