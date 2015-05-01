<#assign title>Create new experiment step</#assign>
<#include "/template/ui/user/header.ftl" />

<h1>${title}</h1>

<@ww.form >

<#assign methods=["HPLC", "Mass spectrometry", "NMR"] />

<@ww.select label="Select technique" 
            list=methods >

<@ww.submit label="Continue ->" />
            
</@ww.form>

<#include "/template/ui/user/footer.ftl" />
