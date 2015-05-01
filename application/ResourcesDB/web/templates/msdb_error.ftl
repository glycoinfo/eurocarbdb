<#include "./header.ftl">
<h1>Error</h1>
<#if errorMsg??><span class="error">${errorMsg}</span><br></#if>
<#--><#if caughtException??>Exception: ${caughtException}<br><br>-->
<#if caughtException??><#if caughtException.getClass()="class org.eurocarbdb.resourcesdb.io.NameParsingException">
<pre>${caughtException.buildExplanationString()!' '}
</pre></#if></#if>
<br>
<a href="javascript:history.back()">back</a>
<#include "./footer.ftl">