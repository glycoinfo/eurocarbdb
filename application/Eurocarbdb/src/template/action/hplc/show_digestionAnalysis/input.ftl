<#assign title>Search</#assign>
<#include "/template/common/header.ftl" />

<div class="hplc_create_form">
<h1>Digest Upload</h1>
<p></p>
</div>

<div class="hplc_form">
<p>Upload your undigested profile here</p>
<p>Your exported data must match a specific format</p>
</div>



<form method="post" enctype="multipart/form-data">
  <@ww.textfield label="Id" name="digestId"/>

<select label="enz" name="enzymeName">
<option value="ABS">ABS</option>
<option value="BTG">BTG</option>
<option value="BKF">BKF</option>
<option value="GUH">GUH</option>
<option value="JBM">JBM</option>
<option value="SPH">SPH</option>
<option value="NANI">NANI</option>
</select>

   
  File: <input type="file" name="upload"/>
  <input type="submit" name="submitAction" value="Upload"/>
</form>


<#include "/template/common/footer.ftl" />
