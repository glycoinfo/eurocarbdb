<#import "/template/lib/Eurocarb.lib.ftl" as eurocarb />

<#assign title>Create NMR Project</#assign>

<#include "/template/common/header.ftl" />

<h1>${title}</h1>

<style>
	.comment
	{
		color: darkgrey;
		font-size: 10px;
		font-family: sans-serif;
	}
</style>

<p>step 3 - data</p>


<div style="padding: 2em;">
	imagine fancy method of selecting atoms and entering shifts here
</div>

<ul>
	<li>mixing time (two possibilities - ccp.api.nmr.Nmr.ExpTransfer or ccp.api.nmr.Nmr.SampleCondition with SampleConditionType 'mixing-time')</li
	<li>acquisition time (location in data model ???)</li>
	<li>phasing parameters (location in data model ???)</li>
	<li>window function (location in data model ???)</li>
	<li>baseline corrections (location in data model ???)</li>
</ul>

<@ww.form method="post">
	<tr><td colspan="2">next set of fields is displayed for each dimension</td></tr>
	
	<@ww.textfield value="" name="SpectralWidth" label="Spectral Width" />
	<tr><td colspan="2" class="comment">ccp.api.nmr.Nmr.FreqDataDim (linked via ccp.api.nmr.Nmr.DataSource.dataDims)</td></tr>

	<@ww.submit value="%{'< Back'}" action="create_nmr_project_back_to_step2" />
	<@ww.submit value="%{'Finish >'}" action="create_nmr_project_finish" />
</@ww.form>

<#include "/template/common/footer.ftl" />

