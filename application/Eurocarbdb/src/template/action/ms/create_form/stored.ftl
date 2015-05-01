<#assign title>Acquisition stored</#assign>
<#include "/template/common/header.ftl" />

<h1>Acquisition Successfully Stored</h1>

<@ww.form>
  <div>
    <ul>
      <li>id = ${acquisition.acquisitionId}</li>
      <li>file = ${acquisition.filename}</li>
      <li>filetype = ${acquisition.filetype}</li>
      <li>dateObtained = ${acquisition.dateObtained?date}</li>
      <li>timeEnd = ${acquisition.dateEntered?date}</li>
    </ul>    
  </div>
  <@ww.submit name="submitAction" value="Next" />
</@ww.form>

<#include "/template/common/footer.ftl" />