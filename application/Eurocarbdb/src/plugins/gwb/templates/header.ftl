<#assign additional_head_content >
<#if (refresh?exists) >
<meta http-equiv="refresh" content="${refresh}" />
</#if>
<link rel="stylesheet" type="text/css" href="css/glycoworkbench.css" />
</#assign>

<#include "/template/common/header_leftopen.ftl" />
</div>

<div id="gwb_tabs">
  <ul>
    <li <#if (current=="home")>class="current"</#if>><a href="home.action">home</a></li>
    <li <#if (current=="builder")>class="current"</#if>><a href="builder.action">builder</a></li>
    <li <#if (current=="examples")>class="current"</#if>><a href="examples.action">examples</a></li>
    <li <#if (current=="manual")>class="current"</#if>><a href="manual.action">manual</a></li>
    <li <#if (current=="contact")>class="current"</#if>><a href="contact.action">contact</a>
  </ul>
</div>
<div id="main">        