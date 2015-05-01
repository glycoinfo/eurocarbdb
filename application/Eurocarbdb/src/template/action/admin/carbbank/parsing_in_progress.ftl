<#assign title>Parsing and loading Carbbank</#assign>
<#include "/template/common/header.ftl" />

<!-- meta http-equiv="refresh" content="1" / -->

<h1>${title}</h1>

<p>
<#if (carbbankParser.percentComplete > 0) >
    Parse/Load in progress, ${carbbankParser.percentComplete}% complete after 
    ${carbbankParser.millisecsElapsed / 1000} seconds.
<#else>
    Preparing to begin parsing...
</#if>
</p>

<#include "/template/common/footer.ftl" />


