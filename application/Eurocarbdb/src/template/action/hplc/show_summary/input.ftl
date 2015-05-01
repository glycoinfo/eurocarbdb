<#assign title>Summary of HPLC experiments</#assign>
<#include "/template/common/header.ftl" />

<div class="hplc_create_form">
<h1>Stored Profiles</h1>
<p></p>
</div>

<div class="hplc_form">
<p>A summary of data already stored</p>
<p>Or start uploading new data <a href="select_instrument.action">here</a></p>
</div>


<!--<div class="hplc_table_data">-->
<table class="table_top_header full_width">

<th class="hplc">Id</th><th class="hplc">Operator</th><th class="hplc">Date</th><th class="hplc">Comments</th><th>Prelim. Assign</th><th>Digestions</th>
<#list showSummary as sum>
<tr><td>${sum.profileId}<td>${sum.operator}<td>${sum.dateAcquired}<td>${sum.userComments}<td><a href="show_prelimAssign.action?profileId=${sum.profileId}">view</a><td><a href="show_summaryDigests.action?profileId=${sum.profileId}">view</a><#if sum_has_next></#if>
</#list>
</table>
<!--</div>-->
<#include "/template/common/footer.ftl" />
