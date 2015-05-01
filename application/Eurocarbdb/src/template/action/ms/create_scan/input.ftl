<#assign title>Create Scan</#assign>
<#include "/template/common/header.ftl" />

<h1>Create Scan</h1>

<h3> Set experimental parameters: </h3>

<@ww.form id="scan_form">
<@ww.hidden name="acquisitionId" value="${acquisitionId?c}"/>
<@ww.hidden name="parentId" value="${parentId?c}"/>

<#include "scan_form.ftl"/>

<@ww.submit value="Create" name="submitAction" />
</@ww.form>

<#include "/template/common/footer.ftl" />

