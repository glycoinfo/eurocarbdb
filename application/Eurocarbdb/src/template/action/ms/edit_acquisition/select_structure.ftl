<#import "/template/lib/FormInput.lib.ftl" as input />
<#assign title>Select structure</#assign>

<#include "/template/common/header.ftl" />

<h1>${title}</h1>

<p>Specify the glycan structure using the builder applet.</p>
<@ww.form method="post" id="select_structure_frm" name="frmInput" enctype="multipart/form-data">
<#if parameters.get('acquisitionId')?exists>
<@ww.hidden name="acquisitionId" value="${parameters.get('acquisitionId')[0]}"/>
<@ww.hidden name="evidenceId" value="${parameters.get('evidenceId')[0]}"/>
</#if>
<#if parameters.get('biologicalContextId')?exists>
<@ww.hidden name="biologicalContextId" value="${parameters.get('biologicalContextId')[0]}"/>
</#if>
<#include "/template/action/core/select_structure/input.ftl"/>
</@ww.form>


<#include "/template/common/footer.ftl" />