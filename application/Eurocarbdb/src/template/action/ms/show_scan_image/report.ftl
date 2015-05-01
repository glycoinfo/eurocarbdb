<#assign title>Show annotation report</#assign>
<#assign onload_function="initReport()"/>
<#include "/template/common/header_minimal.ftl" />

<#import "/template/lib/FormInput.lib.ftl" as input />

<script language="javascript">  
  function initReport() {
    // determine window size
    var width = 0, height = 0;
    if( typeof( window.innerWidth ) == 'number' ) {
      //Non-IE
      width = window.innerWidth;
      height = window.innerHeight;
    } 
    else if( document.documentElement && ( document.documentElement.clientWidth || document.documentElement.clientHeight ) ) 
    {
      //IE 6+ in 'standards compliant mode'
      width = document.documentElement.clientWidth;
      height = document.documentElement.clientHeight;
    } 
    else if( document.body && ( document.body.clientWidth || document.body.clientHeight ) ) {
      //IE 4 compatible
      width = document.body.clientWidth;
      height = document.body.clientHeight;
    }

    width = 0.95*width;
    height = 0.95*height;
    marginleft = 0.025*width;
    margintop = 0.025*height
    
    // add applet
    document.body.innerHTML = '<applet id="AnnotationReportApplet" name="AnnotationReportApplet" code="org.eurocarbdb.application.glycoworkbench.plugin.AnnotationReportApplet.class" ARCHIVE="AnnotationReportApplet.jar" width=' + width + ' height=' + height + ' mayscript="true" style="margin-left:' + marginleft + 'px;margin-top:' + margintop +'px;"/>';

    if (!document.AnnotationReportApplet.isActive()) 
    {
      setTimeout("loadReport()", 1000);
      return ;
    }
	
    xmlHttp=GetXmlHttpObject();
    if (xmlHttp==null) {
      alert ("Your browser does not support AJAX!");
      return;
    } 

    var url= "get_scan_image.action?scanImageId=${scanImageId}&which=report";
    document.body.style.cursor = 'wait';
    xmlHttp.onreadystatechange=setReport;
    xmlHttp.open("GET",url,true);
    xmlHttp.send(null);
  }

  function setReport() {
    if( xmlHttp.readyState==4 ) {     
      document.body.style.cursor = 'default';
      document.AnnotationReportApplet.setDocument(xmlHttp.responseText);
    }
  }
	
</script>

<#include "/template/common/footer_minimal.ftl" />