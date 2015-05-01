<#assign title>Detector Entry</#assign>
<#include "/template/common/header.ftl" />

<h1>Success</h1>
<h2>A reminder:</h2>
<div>
 <ul>
   <li>id = ${detector.detectorId}</li> 
   <li>Manufacturer = ${detector.manufacturer}</li>
   <li>Model = ${detector.model}</li>
   <li>Excitation = ${detector.excitation}</li>
   <li>Emission = ${detector.emission}</li>
   <li>Bandwidth = ${detector.bandwidth}</li>
   <li>Sampling Rate = ${detector.samplingRate}</li>
 </ul>
</div>

<#include "/template/common/footer.ftl" />

