<#assign title>Select biological context</#assign>
<#include "/template/common/header.ftl" />

<#include "scripts.ftl" />
<#include "style.ftl" />

<h1>${title}</h1>

<p>Currently, you  may either submit a corresponding reference to the your data submission or leave this blank to avail of the EUROCarDb resources.</p>

<p>To start click on the '...' and search a term. To select your term s simple the corresponding name.</p>

<@ww.form theme="simple">

  <#include "input.ftl" />

  <input type="hidden" name="profileId" value="${profileId?c}" />
  <input type="hidden" name="bc" value="bcId" />
  <input type="submit" name="submitAction" value="Cancel"/>
  <input type="submit" name="submitAction" value="Next"/>
  <input type="submit" name="submitAction" value="Skip" />


</@ww.form>

<#include "/template/common/footer.ftl" />
