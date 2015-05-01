<#assign glycanSequence = context.glycanSequence />
<#assign bc = context.biologicalContext />
<div class="short_glycan_sequence_context" id="glycan_sequence_context_${context.id?c}">
<div class="sequence">
  <@ecdb.linked_sugar_image_seq_only id=glycanSequence.glycanSequenceId seq=glycanSequence.sequenceGWS?url  scale="0.4"/>
</div>
<div class="context">
  <ul>
    <li><@ecdb.taxonomy t=bc.taxonomy /></li>
    <li><@ecdb.tissue t=bc.tissueTaxonomy /></li>
    <#if bc.diseases.size() gt 0 >
    <li><@ecdb.bc_disease_names bc=bc /></li>
    </#if>
    <#if bc.perturbations.size() gt 0 >
    <li><@ecdb.bc_perturbations bc=bc /></li>
    </#if>
  </ul>
</div>
<div class="metadata">
  <span>${context.dateEntered?datetime}</span>
</div>
</div>
