<#assign title>Detector Entry</#assign>
<#include "/template/common/header.ftl" />

<h1>Detector Attributes</h1>
<h2></h2>   
    <div>
      <ul>
	<li>id = ${detectorId}</li>
	<li>Manufacturer = ${detector.manufacturer}</li>
        <li>Model = ${detector.model}</li>
	<li>Excitation = ${detector.excitation}</li>
	<li>Emission = ${detector.emission}</li>
	<li>Bandwidth = ${detector.bandwidth}</li>
	<li>Sampling Rate = ${detector.samplingRate}</li>
      </ul>
    </div>
    
  </body>
</html>
