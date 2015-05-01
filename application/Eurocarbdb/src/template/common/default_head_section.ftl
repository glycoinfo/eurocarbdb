<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<!-- standard presentation stuff -->
    <link rel="icon" href="/images/favicon.ico" />
    <link rel="shortcut icon" href="/images/favicon.ico" /> 
    <link rel="stylesheet" type="text/css" href="/ecdb/css/eurocarb_default.css" />

    <title>${title}</title>
    <#-- Let's disable this for the moment
	<@ww.head calendarcss="" />
	-->
<!-- CSS -->
<#if (ecdb.css_includes)?exists >
<#list ecdb.css_includes as a_css>
<link rel="stylesheet" type="text/css" href="${a_css}"/>
</#list>
<#else/>
<!-- no stylesheets loaded from css_includes -->
</#if>

<!-- javascripts -->
<#if (ecdb.js_includes)?exists >
<#list ecdb.js_includes as a_js>
<script src="${a_js}" type="text/javascript"></script>
</#list>
<#else/>
<!-- no javascripts loaded from js_includes -->
</#if>