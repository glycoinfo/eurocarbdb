<#include "/template/common/html_doctype_declaration.ftl" />

<#assign title>NCBI Taxonomy update</#assign>

<head>
<#include "/template/common/default_head_section.ftl" />
    <title>${title}</title>
    <meta http-equiv="refresh" content="1"/>
</head>

<body>
<#include "/template/common/default_body_sections.ftl" />

<h1>${title}</h1>

<p>
<#if (percentComplete > 0) >
    Download in progress, ${percentComplete}% complete at 
    ${downloadSpeed?string("###.#")}Kb/sec, approximately 
    ${estimateOfTimeRemaining?string("###.#")} secs remaining.
<#else>
    Preparing to download...
</#if>
</p>

<#include "/template/common/footer.ftl" />


