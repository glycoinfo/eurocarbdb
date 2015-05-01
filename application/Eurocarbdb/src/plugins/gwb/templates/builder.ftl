<#import "/template/lib/FormInput.lib.ftl" as input />
<#assign title>GlycanBuilder: a Rapid and Intuitive Glycan Structure Editor</#assign>
<#assign onload_function="init_applet()"/>
<#assign current="builder">
<#include "header.ftl" />

<div id="contents">

<h1>GlycanBuilder: a Rapid and Intuitive Glycan Structure Editor</h1>
 
<br/>

<applet     
    id="GlycanBuilder" 
    name="GlycanBuilder" 
    code="org.eurocarbdb.application.glycanbuilder.GlycanBuilderApplet.class" 
    archive="${base}/GlycanBuilderApplet.jar" 
    width="700" 
    height="400" 
    mayscript="true">
</applet>

<script> 
 
  function init_applet()
  {
    if (!document.GlycanBuilder.isActive()) 
    {
      setTimeout("init_applet()", 1000);
      return ;
    }
	
    <#if (sugarImageNotation?exists) >
    document.GlycanBuilder.setNotation("${sugarImageNotation}");
    </#if>
  }	

  function on_form_submit() {
 
    document.frmInput.download.value = "true";
   
    document.frmInput.sequences.value = document.GlycanBuilder.getDocument(); 

    document.frmInput.notation.value = document.GlycanBuilder.getNotation();
    document.frmInput.display.value = document.GlycanBuilder.getDisplay();
    document.frmInput.orientation.value = document.GlycanBuilder.getOrientation();

    document.frmInput.showInfo.value = document.GlycanBuilder.getShowInfo();  
    document.frmInput.showMasses.value = document.GlycanBuilder.getShowMasses();  
    document.frmInput.showRedend.value = document.GlycanBuilder.getShowRedend();      
 }
   
 </script>

 <@ww.form theme="simple" action="get_sugar_image.action" onsubmit="on_form_submit();" method="post" id="frmInput" name="frmInput" enctype="multipart/form-data">
 
  <input id="down" name="download" type="hidden" value="true"/>

  <input id="seq" name="sequences" type="hidden" value=""/>  

  <input id="not" name="notation" type="hidden" value=""/>  
  <input id="disp" name="display" type="hidden" value=""/>  
  <input id="orient" name="orientation" type="hidden" value=""/>  

  <input id="info" name="showinfo" type="hidden" value=""/>  
  <input id="masses" name="showmasses" type="hidden" value=""/>  
  <input id="redend" name="showredend" type="hidden" value=""/>  

  <input id="intype" name="inputtype" type="hidden" value="gws"/>  

  <label for="outtype">Output type:</label>
  <select id="outtype"  name="outputType">
    <optgroup label="Structures">
      <option>glycoct_xml</option>
      <option>glycoct_condensed</option>
      <option>glyde</option>
      <option>linucs</option>
      <option>gws</option>
    </optgroup>
    <optgroup label="Images">
      <option>svg</option>
      <option>eps</option>
      <option>pdf</option>
      <option>jpg</option>
      <option>png</option>
      <option>gif</option>
      <option>bmp</option>
    </optgroup>
  </select>

  <input type="submit" value="Get file"> 

</@ww.form>

  <h2 id="introduction">Introduction</h2>

  <p>The GlycanBuilder is a visual editor of glycan
  structures. Multiple structures can be assembled using a symbolic
  representation of monosaccharides, while the graphical placement of
  residues is carried out automatically. Various cartoon notations can
  be employed. All the structural features such as residue
  conformation, linkage position, substitutions and markers can be
  specified and displayed.</p>

  <p>The GlycanBuilder has been used to create the user interface of
  <a href="gwb.action">GlycoWorkbench</a>, a tool for the assisted
  annotation and interpretation of mass spectra of glycans. The
  GlycanBuilder is also used in the interface of the EUROCarbDB <a
  href="http://www.ebi.ac.uk/eurocarb">database</a>, to input and
  search for structures.

  <p>In this page you can test the structure builder on-line, draw
  your structures, compute their masses and export them. The list of
  export types comprises file formats for the encoding of structures
  and various graphical formats that can then be opened from the most
  popular document editors (Word, PowerPoint...). Just select from the
  list the format in which you prefer to export the structures and
  click on the "Get file" button to download the file.</p>

  <h2 id="installation">Download and Installation</h2>

  <p>
  The latest version of the GlycanBuilder tool is the 1.2.3480.  To
  install the tool, download the ZIP archive and extract its content
  in a folder of your choice. To run the tool double click on the file
  <strong>GlycanBuilder.jar</strong>.
  </p>
    
  <p>
  The tool has been tested under Windows, Linux and Mac OS X. In order
  to run the GlycanBuilder tool, the Java Runtime Environment (JRE)
  version 5.0 must be installed on the computer. The latest release of
  JRE 5 can be found on Sun <a
  href="http://java.sun.com/j2se/1.5.0/">homepage</a> together with
  the installation guide and system requirements.
  </p>

  <p>
  The applet necessitate the Java plugin for your current web browser
  to run. The plugin is installed together with the Java Runtime
  Environment.
  </p> 

<@ecdb.context_box title="GlycanBuilder">     

    <a href="download_builder.action" title="Download GlycanBuilder standalone version">Download standalone version<!--<img src="${base}/images/download_button.png"  vspace="12" hspace="12" alt="Download" align="center"/>--></a>
    <#--<a href="download_builder_applet.action"><img src="${base}/images/download_applet_button.png"  vspace="12" hspace="12" alt="Download applet" align="center"/></a>-->
    <#--a href="files/GlycanBuilderSrc.zip"><img src="${base}/images/download_source_button.png"  vspace="12" hspace="12" alt="Download source" align="center"/></a-->
    <a href="http://www.java.com/en/download/" title="Get Java Runtime Environment (may be required, depending on your system)">Get JRE<!--<img src="${base}/images/getjre_button.png" vspace="12" hspace="12" alt="Get JRE" align="center"/>--></a>
</@ecdb.context_box>

<@ecdb.context_box title="Reference">     
<p>
    A.Ceroni, A.Dell and S.M.Haslam, <strong>GlycanBuilder: a fast,
    intuitive and flexible software tool for building and displaying
    glycan structures</strong>, <a href="http://www.scfbm.org/content/2/1/3"><i>Source Code for Biology and Medicine, 2007,2:3</i></a>
</p>
</@ecdb.context_box>

</div>
<#include "footer.ftl" />
