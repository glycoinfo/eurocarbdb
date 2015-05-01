<#assign title>Glycoworkbench Annotation File Stored</#assign>
<#include "/template/common/header.ftl" />

<h1>Peaklists Successfully Stored</h1>

<@ww.form>
  <div>
  <h1> Done.. </h1>
     
  </div>
  <a href="show_acquisition.action?acquisitionId=${acquisitionId}">Show Your Annotation</a><br/>
  <@ecdb.actionlink name="gwupload_phaseI">Upload Another GlycoWorkbench annotation file</@>
  
 
</@ww.form>

<#include "/template/common/footer.ftl" />