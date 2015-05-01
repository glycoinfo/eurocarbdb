<#assign title>Create references</#assign>
<#assign onload_function="select_reftype()"/>
<#include "/template/common/header.ftl" />
<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />

<h1>${title}</h1>

<#include "header.ftl" />

<@ww.form theme="simple" id="frmInput">

  <#include "input.ftl" />

  <br/>
  <hr/>

  <input type="submit" name="submitAction" value="Cancel"/>
  <input type="submit" name="submitAction" value="Finish"/>

</@ww.form>

<#include "/template/common/footer.ftl" />