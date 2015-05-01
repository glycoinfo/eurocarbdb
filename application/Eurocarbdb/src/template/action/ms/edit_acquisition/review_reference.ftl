<#assign title>Add references</#assign>
<#import "/template/lib/FormInput.lib.ftl" as input />
<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />

<#include "/template/common/header.ftl" />
<#if ! actionErrors?exists || actionErrors?size == 0 >
<div id="errors">
</div>
</#if>
<div id="references_forms">
<div>
<#if reference?exists>
  <@ecdb.reference ref=reference/>
</#if>
</div>
<@ww.form action="edit_acquisition_add_reference">
  <#if parameters.get('acquisition.acquisitionId')?exists>
    <@ww.hidden name="acquisition.acquisitionId" value="${parameters.get('acquisition.acquisitionId')[0]}"/>
  </#if>
  <@ww.hidden name="reference.referenceId"/>
  <@ww.submit value="Add"/>
</@ww.form>
</div>
<#include "/template/common/footer.ftl" />