<#assign title>EXperiment Details</#assign>
<#include "/template/common/header.ftl" />

<h1>Profile Submission</h1>
<p>All information regarding sample type and instrument selections have been stored. </p>
<p>The next few steps allow you to store information regarding the HPLC method runs and the upload of integrated HPLC data (undigested and digested sample sets).</p>
<p>You may also include any comments that may be useful for future reference.</p>

<form>

<!--<@ww.textfield label="Instrument Id" name="instrumentId" value="${instrumentId}"/>
<@ww.textfield label="Column Id" name="columnId"/></p>
<@ww.textfield label="Detector Id" name="detectorId" value="${detectorId}"/></p>
-->
<table>
<input type="hidden" label="Instrument Id" name="instrumentId" value="${instrumentId}"/>
<input type="hidden" label="Column Id" name="columnId" value="${columnId}"/>
<input type="hidden"  label="Detector Id" name="detectorId" value="${detectorId}"/>
<tr><td>
Acquisition Software:</td><td><select name="profile.acqSwVersion" size="1"><option value="Waters Empower">Waters Empower</option><option values="Agilent Chemstation">Agilent Chemstation</option></select></td></tr>
<!-- <tr><td>
Date Acquired: </td><td><@ww.textfield  name="profile.dateAcquired"/></td></tr>
-->
<tr><td>
Dextran Standard: </td><td><@ww.textfield name="profile.dextranStandard"/></td></tr>
<tr><td>
Comments: </td><td><@ww.textfield name="profile.userComments"/></td></tr>
<tr><td>
<input type="hidden" label="Operator" name="profile.operator"/></p>
<tr><td></td>
<td><@ww.submit name="submit"/></td></tr>
</form>
</table>
<!--
<@ww.textfield label="Acquisition Software" name="profile.acqSwVersion"/></p>
<@ww.textfield label="Date" name="profile.dateAcquired"/></p>
<@ww.textfield label="Dextran Standard" name="profile.dextranStandard"/></p>
<@ww.textfield label="Operator" name="profile.operator"/></p>
<@ww.textfield label="Comments" name="profile.userComments"/></p>
<@ww.submit name="submit"/>
-->





</form>

<#include "/template/common/footer.ftl" />

