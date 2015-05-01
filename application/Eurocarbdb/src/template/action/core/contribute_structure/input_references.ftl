<#assign title>Add references</#assign>
<#import "/template/lib/FormInput.lib.ftl" as input />
<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />

<@ecdb.use_js_lib name="${'YUI::Tabs'}"/>

<#include "/template/common/header.ftl" />
<#assign context_params>
<#if parameters.get('biologicalContext.biologicalContextId')?exists>biologicalContext.biologicalContextId=${parameters.get('biologicalContext.biologicalContextId')[0]}</#if>&amp;<#if parameters.get('glycanSequence.glycanSequenceId')?exists>glycanSequence.glycanSequenceId=${parameters.get('glycanSequence.glycanSequenceId')[0]}</#if>
</#assign>

<@ecdb.actionlink class="ecdb_button" params=context_params name="contribute_structure_finish">Add to database</@>

<h1>${title}</h1>
<#assign additional_fields>
<#if parameters.get('glycanSequence.glycanSequenceId')?exists>
<@ww.hidden name="glycanSequence.glycanSequenceId" value="${parameters.get('glycanSequence.glycanSequenceId')[0]}"/>
</#if>
<#if parameters.get('acquisition.acquisitionId')?exists>
<@ww.hidden name="acquisition.acquisitionId" value="${parameters.get('acquisition.acquisitionId')[0]}"/>
</#if>
</#assign>

<#include "/template/action/core/create_references/input.ftl"/>

<#include "/template/common/footer.ftl" />
