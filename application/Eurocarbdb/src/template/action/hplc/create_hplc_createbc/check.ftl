<#assign title>Select biological context</#assign>
<#include "/template/common/header.ftl" />

<h1>Reference information</h1>

<p>Currently, who may either submit a corresponding reference to the your data submission or leave this blank to avail of the EUROCarDb resources.</p>


<@ww.form theme="simple">

   
  <input type="hidden" name="profileId" value="${displayProfileId?c}" />
  <input type="hidden" name="bc" value="${displayBcId?c}" />
  <input type="submit" name="submitAction" value="Cancel"/>
  <input type="submit" name="submitAction" value="Next"/>
  <input type="submit" name="submitAction" value="Skip" />

</@ww.form>

<#include "/template/common/footer.ftl" />
