<#assign title>Association of Glycan Sequence and Context</#assign>
<#include "/template/ui/user/header.ftl" />

<h1>Associate a glycan sequence with a biological context</h1>

<h3>Specify identifiers</h3>


<@ww.form>
  <@ww.textfield label="Sequence id" name="linker.glycanSequence.glycanSequenceId"/><br>
  <@ww.textfield label="Context id" name="linker.biologicalContext.biologicalContextId"/><br>
  <@ww.submit value="Submit" name="submitAction"/>
</@ww.form>

<#include "/template/common/footer.ftl" />