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

<p>step 2 - sample</p>

<@ww.form method="post">
	<@ww.textfield value="" name="Solvent" label="Solvent" />
	<tr><td colspan="2" class="comment">ccp.api.nmr.Nmr.SampleCondition (ccp.api.nmr.Nmr.SampleConditionType - free text field)</td></tr>

	<@ww.textfield value="" name="Salts" label="Salts" />
	<tr><td colspan="2" class="comment">ccp.api.nmr.Nmr.SampleCondition (ccp.api.nmr.Nmr.SampleConditionType)</td></tr>

	<@ww.textfield value="" name="pHpD" label="pH / pD" />
	<tr><td colspan="2" class="comment">ccp.api.nmr.Nmr.SampleCondition (ccp.api.nmr.Nmr.SampleConditionType)</td></tr>
	
	<tr><td colspan="2" class="comment">SampleCondition is a free text field, but only allows numerical values. ccp.api.lims.Sample provides additional sample related fields.</td></tr>

	<@ww.submit value="%{'< Back'}" action="create_nmr_project_back_to_step1" />
	<@ww.submit value="%{'Next >'}" action="create_nmr_project_forward_to_step3" />
</@ww.form>

<#include "/template/common/footer.ftl" />

