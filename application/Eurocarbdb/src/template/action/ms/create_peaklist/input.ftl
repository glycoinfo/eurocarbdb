<#assign title>Peaklist Page</#assign>
<#include "/template/common/header.ftl" />

<h1> Upload a peaklist file </h1>

<@ww.form method="POST" id="scan_form" enctype="multipart/form-data">
  <@ww.hidden name="scan.scanId"/>
  <@ww.file label="Peaklist file" name="peaklistFile"/>
  <@ww.submit value="Upload"/>
</@ww.form>

<#include "/template/common/footer.ftl" />


