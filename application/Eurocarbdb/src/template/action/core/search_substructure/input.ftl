<#assign title>Search by substructure</#assign>
<#assign onload_function="init_applet()">
<#include "/template/common/header.ftl" />

<#import "/template/lib/FormInput.lib.ftl" as input />

<script language="javascript">    
  function init_applet() {
    if (!document.GlycanBuilder.isActive()) 
    {
      setTimeout("init_applet()", 1000);
      return ;
    }
    
    document.GlycanBuilder.setShowRedendCanvas(false);
    <#if (sugarImageNotation?exists) >
    document.GlycanBuilder.setNotation("${sugarImageNotation}");
    </#if>
    <#if sequenceGWS?exists > 
    document.GlycanBuilder.setDocument("${sequenceGWS}");
    </#if>
  }

  function on_form_submit()
  {
    document.frmInput.sequenceGWS.value = document.GlycanBuilder.getDocument();
  }	
</script>

<h1>${title}</h1>
<p>Specify the substructure to be searched using the builder applet.</p>

<#if ( message?length > 0 )>
<div class="error_message">
  ${message}	
</div>
</#if>

<@ww.form theme="simple" onsubmit="on_form_submit();"  id="frmInput" name="frmInput">
<@ww.hidden name="sequenceGWS" value="" id="sequenceGWS" />
    
<br/><br/>
<applet     
    id="GlycanBuilder" 
    name="GlycanBuilder" 
    code="org.eurocarbdb.application.glycanbuilder.GlycanBuilderApplet.class" 
    archive="GlycanBuilderApplet.jar" 
    width="700" 
    height="400" 
    mayscript="true">
</applet>

<br/>
<p>Search for this motif anywhere <input type="submit" name="submitAction" value="Search" /></p>
<p>Search for this motif anchored at the reducing terminus <input type="submit" name="submitAction" value="Search core" /><p>
<p>Search for this motif anchored to non-reducing terminii <input type="submit" name="submitAction" value="Search terminal" /><p>

</@ww.form>

<#macro example_substructure_search seq title>
    <div style="display: inline;">
    ${title}:
    <a href="search_substructure.action?sequenceGWS=${seq}&submitAction=Search+core" title="Search for ${title}">
    <@ecdb.sugar_image_for_seq seq="${seq}" scale="0.4" />
    </a>
    </div>
</#macro>

<@ecdb.context_box title="Common searches">
    <@example_substructure_search title="N-linked core" seq="freeEnd--%3Fb1D-GlcNAc%2Cp--4b1D-GlcNAc%2Cp--4b1D-Man%2Cp(--3a1D-Man%2Cp)--6a1D-Man%2Cp%24MONO%2CperMe%2CNa%2C0%2CfreeEnd" />
    <@example_substructure_search title="O-linked core 1" seq="redEnd--%3Fa1D-GalNAc%2Cp--3b1D-Gal%2Cp%24MONO%2CperMe%2CNa%2C0%2CredEnd" />
    <@example_substructure_search title="O-linked core 2" seq="redEnd--%3Fa1D-GalNAc%2Cp(--3b1D-Gal%2Cp)--6b1D-GlcNAc%2Cp%24MONO%2CperMe%2CNa%2C0%2CredEnd" />
    <@example_substructure_search title="O-linked core 3" seq="redEnd--%3Fa1D-GalNAc%2Cp--3b1D-GlcNAc%2Cp%24MONO%2CperMe%2CNa%2C0%2CredEnd" />
    <@example_substructure_search title="O-linked core 4" seq="redEnd--%3Fa1D-GalNAc%2Cp(--3b1D-GlcNAc%2Cp)--6b1D-GlcNAc%2Cp%24MONO%2CperMe%2CNa%2C0%2CredEnd" />
    <@example_substructure_search title="O-linked core 5" seq="redEnd--%3Fa1D-GalNAc%2Cp--3a1D-GalNAc%2Cp%24MONO%2CperMe%2CNa%2C0%2CredEnd" />
    <@example_substructure_search title="O-linked core 6" seq="redEnd--%3Fa1D-GalNAc%2Cp--6b1D-GlcNAc%2Cp%24MONO%2CperMe%2CNa%2C0%2CredEnd" />
    <@example_substructure_search title="O-linked core 7" seq="redEnd--%3Fa1D-GalNAc%2Cp--6a1D-GalNAc%2Cp%24MONO%2CperMe%2CNa%2C0%2CredEnd" />
    <@example_substructure_search title="O-linked core 8" seq="redEnd--%3Fa1D-GalNAc%2Cp--3a1D-Gal%2Cp%24MONO%2CperMe%2CNa%2C0%2CredEnd" />
</@>

<#include "/template/common/footer.ftl" />
