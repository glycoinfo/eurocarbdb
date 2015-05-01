<#assign title>Summary of HPLC experiments</#assign>
<#include "/template/common/header.ftl" />

<h1>Stored Profiles</h1>

<p>A summary of data stored for evidence Id : ${evidenceId?c}</p>
<p>Or start uploading new data <a href="select_settings.action">here</a></p>
<#list showSummary as show>
<h2>Sample Description</h2>
<p>Taxonomy: ${show[0]}</p>
<p>Tissue: ${show[1]}</p>

</#list>

<h2>Profile data description</h2>
<p>Profile Id: ${profile.profileId} </p>
<p>Comments: ${profile.userComments}</p>
<p>Dextran Standard: ${profile.dextranStandard}</p>
<p>Software: ${profile.acqSwVersion}</p>
<p>Column: ${profile.column.manufacturer}_${profile.column.model}</p>
<p>Instrument: ${profile.instrument.manufacturer}_${profile.instrument.model}</p>

<h2>Stored Data</h2>
<p>View preliminary assignments <a href="show_prelimAssign.action?profileId=${profile.profileId?c}"/>here</a></p>

<p>Digest data:</p>
<#list showDigestSummary as d>

<li><a href="show_digestsAssign.action?profileId=${profile.profileId?c}&digestId=${d[1]?c}">${d[2]?string}</a></li>

<#if d_has_next></#if>
</#list>



<#include "/template/common/footer.ftl" />
