<#assign title>Scan image creation</#assign>
<#include "/template/common/header.ftl" />

<h1>Add image to scan</h1>

<@ww.form theme="simple" method="post" enctype="multipart/form-data">
<@ww.hidden value="${scan.scanId}" name="scanId" />

<#if action.hasFieldErrors() >
<#list action.fieldErrors.entrySet() as e >
<font color="red"><p>${ e.getKey() } - ${ e.getValue() }</p></font>
</#list>
</#if>

<h3>Upload annotation report</h3>

<p>Choose an annotation report file generated with GlycoWorkbench. The
images would be produced from the report</p>

<@ww.file label="report file" name="annotationReportFile"/><@ww.submit value="Upload" name="submitAction"/>

<h3>Upload image</h3>

<p>Alternatively choose an image that would be associated with the scan</p>

<@ww.file label="image file" name="scanImageFile"/><@ww.submit value="Upload" name="submitAction"/>

<br>

<@ww.submit value="Back" name="submitAction"/>


</@ww.form>

<#include "/template/common/footer.ftl" />