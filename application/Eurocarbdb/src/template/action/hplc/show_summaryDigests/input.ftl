<#assign title>Summary of HPLC experiments</#assign>
<#include "/template/common/header.ftl" />

<div class="hplc_create_form">
<h1>Summary of Digest Data Stored</h1>
<p></p>
</div>

<div class="hplc_form">
<p>Data for profile Id: ${profileId}</p>


<!--<div class="hplc_table_data">-->
<table class="table_top_header full_width">

<th class="hplc">Id</th><th class="hplc">Enzyme</th>
<#list showSummary as sum>
<tr><td><a href="show_digestsAssign.action?profileId=${profileId}&digestId=${sum.digestId}">${sum.digestId}</a><td>${sum.sequentialDigest}<#if sum_has_next></#if>
</#list>
</table>
<!--</div>-->

<#include "/template/common/footer.ftl" />
