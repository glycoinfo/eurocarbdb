<#import "/template/lib/Eurocarb.lib.ftl" as eurocarb />

<#assign title>Create NMR Project</#assign>

<#include "/template/common/header.ftl" />

<h1>${title}</h1>

<p>step 1 - experiment</p>


<style>
	.comment
	{
		color: darkgrey;
		font-size: 10px;
		font-family: sans-serif;
	}
</style>

<@ww.form method="post">
	<@ww.textfield value="" name="ProjectName" label="Project Name" />
	<tr><td colspan="2" class="comment">memops.api.Implementation.Project.name</td></tr>
	
	<@ww.textfield value="" name="ExperimentName" label="Experiment Name" />
	<tr><td colspan="2" class="comment">ccp.api.nmr.Nmr.Experiment.name</td></tr>

	<@ww.textfield value="" name="ExperimentType" label="Experiment Type" />
	<tr><td colspan="2" class="comment">ccp.api.nmr.Nmr.Experiment.experimentType (COSY, TOCSY, ...). maybe make this a &lt;select&gt;</td></tr>

	<@ww.select name="Dimensions" label="Dimensions" list=r"#{'1':'1', '2':'2', '3':'3'}" />
	<tr><td colspan="2" class="comment">ccp.api.nmr.Nmr.Experiment.numDim - xxx</td></tr>
	
	<@ww.file name="SpectrumPicture" label="Spectrum Picture" />
	<tr><td colspan="2" class="comment">ccp.api.nmr.Nmr.DataSourceImage (not in api atm)</td></tr>

	<@ww.textfield value="" name="NoiseLevel" label="Noise Level" />
	<tr><td colspan="2" class="comment">ccp.api.nmr.Nmr.DataSource.noiseLevel</td></tr>

	<@ww.textfield value="" name="SignalLevel" label="Signal Level" />	
	<tr><td colspan="2" class="comment">ccp.api.nmr.Nmr.DataSource.signalLevel (not in api atm)</td></tr>

	<@ww.textfield value="" name="SignalMethod" label="Signal Method" />	
	<tr><td colspan="2" class="comment">ccp.api.nmr.Nmr.DataSource.snMethod (not in api atm)</td></tr>

	<@ww.textfield value="" name="NumberOfScans" label="Number of Scans" />
	<tr><td colspan="2" class="comment">ccp.api.nmr.Nmr.Experiment.numScans</td></tr>


	<@ww.file name="RawData" label="Raw Data" />
	<tr><td colspan="2" class="comment">ccp.api.nmr.Nmr.DataSource.dataLocation (memops.api.Implementation.DataLocation)</td></tr>

	<@ww.textfield name="RawDataFileType" label="File Type" />
	<tr><td colspan="2" class="comment">ccp.api.nmr.Nmr.DataSource.fileType (ccp.api.nmr.Nmr.DataSourceFileType)</td></tr>

	<@ww.select name="RawDataDataType" label="Data Type" list=r"#{'UNPROCESSED':'Unprocessed', 'PROCESSED':'Processed'}" /> 
	<tr><td colspan="2" class="comment">ccp.api.nmr.Nmr.DataSource.dataType (ccp.api.nmr.Nmr.DataSourceDataType)</td></tr>

	
	<@ww.submit value="%{'Next >'}" action="create_nmr_project_forward_to_step2" />
</@ww.form>

<#include "/template/common/footer.ftl" />

