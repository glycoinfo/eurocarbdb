
<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />

<#assign title>Registration</#assign>

<#include "/template/common/header.ftl" />



                <h1>${title}</h1>
                <div >
                    <p>Your details have been succcessfully saved.</p>
                    <p>Please wait for the admin to respond. </p>
                    <p>Please check you email to see if your account has been activated or not.</p>
                    <p>Please also check your spam filter. </p>
                    <p>Sometime Our email falls into Spam. </p>
                </div>
                <#--
                <#if ( message?length > 0 )>
                <div class="error_message">
                  ${message}
                </div>
                </#if>     
                -->

<#include "/template/common/footer.ftl" />
