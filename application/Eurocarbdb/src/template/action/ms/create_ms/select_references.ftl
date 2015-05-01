<#assign title>Select references</#assign>
<#import "/template/lib/FormInput.lib.ftl" as input />
<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />

<@ecdb.use_js_lib name="${'YUI::Tabs'}"/>

<#include "/template/common/header.ftl" />

<h1>${title}</h1>
<a href="create_acquisition.action">Next</a><br/>

<#include "/template/action/core/create_references/input.ftl"/>

<#include "/template/common/footer.ftl" />