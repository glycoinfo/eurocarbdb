<#assign title>Scan image creation</#assign>
<#include "/template/common/header.ftl" />

<h1>Add image to scan</h1>

<@ww.form theme="simple" method="post" enctype="multipart/form-data">
<@ww.hidden value="${scan.scanId}" name="scanId" />

<p>The scan has already an image associated to it do you want to delete it first?</p>

<@ww.submit value="Back" name="submitAction"/>
<@ww.submit value="Delete" name="submitAction"/>

</@ww.form>

<#include "/template/common/footer.ftl" />