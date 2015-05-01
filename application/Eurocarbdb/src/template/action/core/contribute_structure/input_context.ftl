<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />

<#assign title>Contribute structure: input biological context</#assign>

<@ecdb.use_js_lib name="${'AutoCompleter'}" />
<@ecdb.use_js_lib name="${'AlphabetisedTabbedList'}" />

<#include "/template/common/header.ftl" />

<h1>${title}</h1>

<p>Specify the biological context in which the structure has been found.</p>

<#if Session.contribute_structureglycanSequence?exists>
  <#assign glycanSequence=Session.contribute_structureglycanSequence />
  <#include "/template/action/core/show_glycan/short_summary.ftl"/>
</#if>

<@ww.form>
<#if parameters.get('glycanSequenceId')?exists>
<@ww.hidden name="glycanSequenceId" value="${parameters.get('glycanSequenceId')[0]}"/>
</#if>
<#include "/template/action/core/create_biological_context/input.ftl"/>
</@ww.form>

<#include "/template/common/footer.ftl" />
