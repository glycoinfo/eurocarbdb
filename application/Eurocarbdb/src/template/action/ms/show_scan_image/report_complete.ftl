<#assign title>Show annotation report</#assign>
<#assign onload_function="loadReport()"/>
<#include "/template/common/header.ftl" />

<#import "/template/lib/FormInput.lib.ftl" as input />

<script language="javascript">  
 	
  function loadReport()
  {
    if (!document.AnnotationReportApplet.isActive()) 
    {
      setTimeout("loadReport()", 1000);
      return ;
    }
    alert('applet active');
	
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
      alert('received ' + xmlHttp.responseText);
      document.AnnotationReportApplet.setDocument(xmlHttp.responseText);
      alert('set ' + document.AnnotationReportApplet.getDocument());
    }
  }
	
</script>

<h1>${title}</h1>

<applet id="AnnotationReportApplet" name="AnnotationReportApplet" CODE="org.eurocarbdb.application.glycoworkbench.plugin.AnnotationReportApplet.class" ARCHIVE="AnnotationReportApplet.jar" WIDTH="900" HEIGHT="700" mayscript="true">
</applet>

<#include "/template/common/footer.ftl" />