<#assign bc = context />
<div class="short_biological_context biologicalContext${context.id?c} " id="biological_context_${context.id?c}">
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
  <span>${bc.dateEntered?datetime}</span>
</div>
</div>