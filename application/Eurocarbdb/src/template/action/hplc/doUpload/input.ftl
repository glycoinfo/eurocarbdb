<#assign title>Profile Submission</#assign>
<#include "/template/common/header.ftl" />

<h1>Undigest Profile Submission</h1>

<p>Upload your undigested HPLC profile data here</p>
<p>Your exported data must match a specific format (see guide to be written!)</p>

<form method="post" enctype="multipart/form-data">
  Your new set of profiles will be assigned an ID value: ${profileId} <br/> 
  File: <input type="file" name="upload"/>
  <input type="hidden" name="imageStyle" value="uoxf"/>
  <input type="submit" name="submitAction" value="Upload"/>
</form>


<#include "/template/common/footer.ftl" />
