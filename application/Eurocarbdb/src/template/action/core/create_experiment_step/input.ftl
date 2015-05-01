<#assign title>Create new experiment step</#assign>
<#include "/template/ui/user/header.ftl" />

<h1>${title}</h1>

<p>
    Adding a new experiment step to experiment ${experimentStep.experiment.experimentName}.
</p>
<@ww.form >

<#assign methods=["HPLC", "Mass spectrometry", "NMR"] />

<@ww.hidden name="experimentId" value="${experimentId}" />

<@ww.select label="Select technique" 
            name="techniqueId"
            list=allTechniques 
            listKey="techniqueId" 
            listValue="techniqueName" 
            />

<@ww.submit label="Continue ->" />
            
</@ww.form>

<#include "/template/ui/user/footer.ftl" />
