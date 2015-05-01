<#assign title>Select biological context</#assign>

<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />

<@ecdb.use_js_lib name="${'AutoCompleter'}" />

<#include "/template/common/header.ftl" />

<h1>${title}</h1>

<#include "input.ftl" />

<#include "/template/common/footer.ftl" />