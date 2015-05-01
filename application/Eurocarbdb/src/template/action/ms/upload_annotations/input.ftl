<#assign title>Upload Annotations Page</#assign>
<#include "/template/common/header.ftl" />

<h1>Upload annotated peaklist</h1>

<@ww.form method="POST" id="scan_form" enctype="multipart/form-data">
  <@ww.hidden name="scan.scanId"/>
  <@ww.file label="Annotated peaklist file" name="annotationsFile"/>
  <@ww.submit value="Upload"/>
</@ww.form>

<#include "/template/common/footer.ftl" />