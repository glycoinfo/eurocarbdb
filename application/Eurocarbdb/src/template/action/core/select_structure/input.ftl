

<applet id="GlycanBuilder" name="GlycanBuilder" code="org.eurocarbdb.application.glycanbuilder.GlycanBuilderApplet.class" archive="GlycanBuilderApplet.jar" width="700" height="400" >
</applet>
<p>
<input type="submit" id="sel_seq" name="submitAction" value="Select sequence" />
</p>
<@ww.hidden name="sequenceGWS" value="" id="sequenceGWS" />

<script type="text/javascript">

  connect($('sel_seq'),'onclick',function() {
    $('sequenceGWS').value = document.GlycanBuilder.getDocument();

    return true;
  });

  connect($('sel_seq'),'ajaxonclick',function() {
    $('sequenceGWS').value = document.GlycanBuilder.getDocument();
    return true;
  });
  
  connect(ECDB,'onload',function() {

    var applet = $('GlycanBuilder');

    applet.setNotation(ECDB.GetRenderingType());

    connect(ECDB,"notationchange",function() {
      applet.setNotation(ECDB.GetRenderingType());      
    });

    <#if sequenceGWS?exists > 
    connect(ECDB,"appletload",function() {
      applet.setDocument("${sequenceGWS}");
    });
    </#if>

    ECDB.InitAppletIfNotLoaded(applet);

  });

  ECDB.NEED_TO_LOAD_SCRIPTS=false;
  
</script>
 
