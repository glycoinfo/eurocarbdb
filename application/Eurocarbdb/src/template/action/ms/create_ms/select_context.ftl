<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />

<#assign title>Biological context search</#assign>

<@ecdb.use_js_lib name="${'YUI::Tabs'}"/>
<@ecdb.use_js_lib name="${'AutoCompleter'}" />

<#include "/template/common/header.ftl" />
<style type="text/css">
  .short_glycan_sequence_context {
    width: 300px;
    height: 75px;
    font-size: 0.75em;
    margin: 5px;
    float: left;
  }
  
  .short_glycan_sequence_context img {
    max-height: 50px !important;
  }
  
  .short_glycan_sequence_context:hover {
    cursor: pointer;
    background: #9999CC;
    border: solid #888888 1px;
  }
  
  .short_glycan_sequence_context:hover a {
    color: #ffffff !important;  
  }
</style>

<h1>Select sequence context to add evidence to</h1>
<div>
<#list glycanSequenceContexts as context >
  <@ecdb.short_glycan_sequence_context context=context />
</#list>
<div style="clear: both; width: 100%; height: 0px;"></div>
</div>
<h2>Alternatively</h2>
<@ecdb.actionlink name="create_acquisition!input">Upload mzXML file</@><br/>
<@ecdb.actionlink name="gwupload_phaseI">Upload GlycoWorkbench annotation file</@><br/>
<@ecdb.actionlink name="GenerateGwbFile">View/Download Annotations</@>

<script type="text/javascript">
connect(ECDB,'onload',function() {
  var contexts = getElementsByTagAndClassName('*','short_glycan_sequence_context');
  for (var i = 0; i < contexts.length; i++) {
    var a_context = contexts[i];
    var re = /glycan_sequence_context_(\d+)/
    if (a_context.id.match(re)) {
      var matched_id = RegExp.$1;
      connect(a_context,"onclick", partial(function(someid) {
        window.location=("?glycanSequenceContextId="+someid);
      },matched_id));
    }
  }
});
</script>

<#include "/template/common/footer.ftl" />
