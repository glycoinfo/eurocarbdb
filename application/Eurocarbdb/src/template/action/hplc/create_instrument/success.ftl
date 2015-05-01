<#assign title>Instrument Entry</#assign>
<#include "/template/common/header.ftl" />

<h1>Success</h1>
<h2>A reminder:</h2>
<div>
 <ul>
  <li>Id = ${instrument.instrumentId}</li>	
  <li>Manufacturer = ${instrument.manufacturer}</li>
  <li>Model = ${instrument.model}</li>
  <li>Temperature = ${instrument.temperature}</li>
  <li>SolventA = ${instrument.solventA}</li>
  <li>SolventB = ${instrument.solventB}</li>
  <li>SolventC = ${instrument.solventC}</li>
  <li>SolventD = ${instrument.solventD}</li>
  <li>Flow Rate = ${instrument.flowRate}</li>
  <li>Flow Gradient = ${instrument.flowGradient}</li>
 </ul>
</div>

<#include "/template/common/footer.ftl" />

`

