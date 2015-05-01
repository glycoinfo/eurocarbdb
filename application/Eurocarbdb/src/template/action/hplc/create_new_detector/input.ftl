<#assign title>User home</#assign>
<#include "/template/ui/user/header.ftl" />

<h1>Detector Entry</h1>

<h2>Detector</h2>

<p><a href="create_detector.action">Use stored settings</a>

<@ww.form>
<p>

<form validate="true">
<@ww.textfield label="Manufacturer" name="detector.manufacturer"/></p>
<@ww.textfield label="Model" name="detector.model"/></p>
<@ww.textfield label="Excitation" name="detector.excitation"/></p>
<@ww.textfield label="Emission" name="detector.emission"/></p>
<@ww.textfield label="Bandwidth" name="detector.bandwidth"/></p>
<@ww.textfield label="Sampling Rate" name="detector.samplingRate"/></p>
	<@ww.submit name="submit"/>
</@ww.form>

<#include "/template/common/footer.ftl" />
