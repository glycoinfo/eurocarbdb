<#assign glycanSequence = context.glycanSequence />
<#assign bc = context.biologicalContext />
<div class="sequence">
  <@ecdb.linked_sugar_image id=glycanSequence.glycanSequenceId seq=glycanSequence.sequenceGWS?url  scale="0.5"/>
</div>

<b>Species:</b> <@ecdb.taxonomy t=bc.taxonomy /><br/>
<b>Tissue:</b> <@ecdb.tissue t=bc.tissueTaxonomy />
<#if bc.diseases.size() gt 0 >
<b>Diseases:</b> <br/>
<@ecdb.bc_disease_names bc=bc />
</#if>
<#if bc.perturbations.size() gt 0 >
<b>Perturbations</b><br/>
<@ecdb.bc_perturbations bc=bc />
</#if>
