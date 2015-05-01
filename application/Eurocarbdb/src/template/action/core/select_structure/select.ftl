<#import "/template/lib/FormInput.lib.ftl" as input />
<#assign title>Select structure</#assign>

<#include "/template/common/header.ftl" />

<h1>${title}</h1>

<p>Specify the glycan structure using the builder applet.</p>
<@ww.form method="post" id="frmInput" name="frmInput" enctype="multipart/form-data">
<#include "input.ftl"/>
<div class="section_toggle">
<a href="#" class="hd">GlycoCT upload</a>
<div class="bd">
  <p>If you have a GlycoCT XML file, you can upload it here</p>

  <label>Glyco CT XML</label>
  <input type="file" name="sequenceFile" />
  <input type="submit" name="submitAction" value="Load" />
</div>
</div>
</@ww.form>


<#include "/template/common/footer.ftl" />