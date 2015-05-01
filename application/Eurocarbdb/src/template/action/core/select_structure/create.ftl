<#assign title>Create structure</#assign>
<#assign onload_function="init_applet()">
<#include "/template/common/header.ftl" />
<#import "/template/lib/FormInput.lib.ftl" as input />

<#include "scripts.ftl"/>

<h1>${title}</h1>

<p>Specify the glycan structure using the builder applet.</p>

<@ww.form theme="simple" onsubmit="on_form_submit();" method="post" id="frmInput" name="frmInput" enctype="multipart/form-data">

  <#include "input.ftl"/>
  
  <hr/>
  <br/>
  <input type="submit" name="submitAction" value="Cancel" />
  <input type="submit" name="submitAction" value="Store" />
  
</@ww.form>

<#include "/template/common/footer.ftl" />