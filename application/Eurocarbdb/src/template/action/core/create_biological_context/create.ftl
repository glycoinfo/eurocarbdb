<#assign title>Input biological context</#assign>
<#include "/template/common/header.ftl" />

<#include "scripts.ftl" />
<#include "style.ftl" />

<h1>${title}</h1>

<@ww.form theme="simple">

  <#include "input.ftl" />


  <input type="submit" name="submitAction" value="Cancel"/>
  <input type="submit" name="submitAction" value="Store"/>

</@ww.form>

<#include "/template/common/footer.ftl" />