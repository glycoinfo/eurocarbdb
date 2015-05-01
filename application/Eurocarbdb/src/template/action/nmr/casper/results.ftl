<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />

<#assign title>CASPER Results</#assign>

<#include "/template/common/header.ftl" />

<style type="text/css">
<!-- 

.hide{
	display:none;
}

-->
</style>

<script language="javascript">
<!--
function toggleShow()
{
  var cl;
  var theRules = new Array();

  if (document.styleSheets[1].cssRules)
    theRules = document.styleSheets[1].cssRules
  else if (document.styleSheets[1].rules)
    theRules = document.styleSheets[1].rules

  for (var i=0; i<theRules.length; i++)
  {
    if(theRules[i].selectorText == '.' + 'hide')
    {
      cl=theRules[i];
    }
  }
  if (cl.style.display != 'none' ) 
    {
      cl.style.display = 'none';
    }
    else
    {
      cl.style.display = 'table-row';
    }
}
// -->
</script>

<#include "citing.ftl" />

<h1>${title}</h1>

${results}

<@ww.form action="casper_${mode}_ccpn_save" method="post">
<#if savedProject!="">
  <@ww.submit value="Save as CCPN project" />
</#if>
</@ww.form>

<#include "/template/common/footer.ftl" />

