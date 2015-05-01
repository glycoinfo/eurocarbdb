<#assign title>Association created</#assign>
<#include "/template/ui/user/header.ftl" />

<h1>Association created</h1>

<div>
  <ul>	
    <li>Sequence id: <a href="show_glycan.action?glycanSequenceId=${linker.glycanSequence.glycanSequenceId?c}">${linker.glycanSequence.glycanSequenceId?c}</a></li>
    <li>Context id: <a href="show_biological_context.action?biologicalContext.biologicalContextId=${linker.biologicalContext.biologicalContextId?c}">${linker.biologicalContext.biologicalContextId?c}</a></li>
  </ul>    
</div>

<#include "/template/common/footer.ftl" />