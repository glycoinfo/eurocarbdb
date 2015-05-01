<#assign title>GlycoWorkbench: Semi-Automatic Interpretation and Annotation of Mass Spectra of Glycans</#assign>
<#assign current="home">
<#include "header.ftl" />

<div id="contents">

<h1>GlycoWorkbench: Semi-Automatic Interpretation and Annotation of Mass Spectra of Glycans</h1>

<a href="${base}/images/gwb_screenshot.jpg"><img src="${base}/images/gwb_screenshot_small.jpg" width="250" hspace="20" alt="screenshot" style="float: right;"/></a>


<@ecdb.context_box title="GlycoWorkbench">     

    <a href="download.action" title="Download GlycoWorkbench standalone version">Download standalone version<!--<img src="${base}/images/download_button.png"  vspace="12" hspace="12" alt="Download" align="center"/>--></a>
    <#--<a href="download_builder_applet.action"><img src="${base}/images/download_applet_button.png"  vspace="12" hspace="12" alt="Download applet" align="center"/></a>-->
    <#--a href="files/GlycanBuilderSrc.zip"><img src="${base}/images/download_source_button.png"  vspace="12" hspace="12" alt="Download source" align="center"/></a-->
    <a href="http://www.java.com/en/download/" title="Get Java Runtime Environment (may be required, depending on your system)">Get JRE<!--<img src="${base}/images/getjre_button.png" vspace="12" hspace="12" alt="Get JRE" align="center"/>--></a>
</@ecdb.context_box>

<@ecdb.context_box title="Reference">     
<#--
<p>
    If you are using GlycoWorkbench or GlycanBuilder for preparing
    your articles, or if you employed the GlycanBuilder applet in your
    web interface, please cite:
</p>
-->
<p>
    A.Ceroni, K. Maass, H.Geyer, R.Geyer, A.Dell and S.M.Haslam,
  <strong>GlycoWorkbench: A Tool for the Computer-Assisted Annotation of
  Mass Spectra of Glycans</strong>,
  <a href="http://pubs.acs.org/cgi-bin/abstract.cgi/jprobs/2008/7/i04/abs/pr7008252.html">
    <em>Journal of Proteome Research, 7 (4), 1650--1659, 2008, DOI: 10.1021/pr7008252</em></a>
  </p>

  <p>A.Ceroni, A.Dell and S.M.Haslam, <strong>The GlycanBuilder: a fast,
  intuitive and flexible software tool for building and displaying
  glycan structures</strong>, <a
  href="http://www.scfbm.org/content/2/1/3"><em>Source Code for Biology
  and Medicine, 2007,2:3</em></a>
  </p>
</@ecdb.context_box>

<#--  
  <p class="footnote">
  <em><font size="small">created by Alessio Ceroni (<a
  href="http://www3.imperial.ac.uk/lifesciences/research/molecularbiosciences/massspec/people">ICL</a>),
  with the collaboration of Kai Maass (<a
  href="http://www.uniklinikum-giessen.de/bio/geyer_group.html">JLUG</a>)</font></em>
  </p>
-->
<p>
  <ul>
       <li><a href="#introduction">Introduction</a></li>
       <li><a href="#installation">Download and Installation</a></li>
       <li><a href="#citing">How to cite</a></li>
       <li><a href="#changes">Changes</a></li>
       <li><a href="#acknowledgements">Acknowledgements</a></li>
   </ul>  
</p>


<h2 id="introduction">Introduction</h2>

<p>
  GlycoWorkbench is a suite of software tools designed for rapid
  drawing of glycan structures and for assisting the process of
  structure determination from mass spectrometry data. The graphical
  interface of GlycoWorkbench provides an environment in which
  structure models can be rapidly assembled, their mass computed,
  their fragments automatically matched with MSn data and the results
  compared to assess the best candidate. GlycoWorkbench can greatly
  reduce the time needed for the interpretation and annotation of mass
  spectra of glycans.
</p>

<h2 id="installation">Download and Installation</h2>

<p>
  The latest version of the GlycoWorkbench tool is the 1.1.3480.  To
  install the tool, download the ZIP archive and extract its content
  in a folder of your choice. To run the tool in Windows use
  <strong>GlycoWorkbench.exe</strong>. In Mac OS or Linux double click on the
  file <strong>GlycoWorkbench.jar</strong>.
</p>
    
  <p>
  The tool has been tested under Windows, Linux and Mac OS X. In order
  to run the GlycoWorkbench tool, the Java Runtime Environment (JRE)
  version 5.0 or later must be installed on the computer. The latest release of
  the JRE 5.0 can be found on Sun <a
  href="http://java.sun.com/j2se/1.5.0/">homepage</a> together with
  the installation guide and system requirements.
  </p> 


<h2 id="citing">How to cite</h2>
  
<p>
    If you are using GlycoWorkbench or GlycanBuilder for preparing
    your articles, or if you employed the GlycanBuilder applet in your
    web interface, please cite:
</p>

<p>
    A.Ceroni, K. Maass, H.Geyer, R.Geyer, A.Dell and S.M.Haslam,
  <strong>GlycoWorkbench: A Tool for the Computer-Assisted Annotation of
  Mass Spectra of Glycans</strong>,
  <a href="http://pubs.acs.org/cgi-bin/abstract.cgi/jprobs/2008/7/i04/abs/pr7008252.html">
    <em>Journal of Proteome Research, 7 (4), 1650--1659, 2008, DOI: 10.1021/pr7008252</em></a>
  </p>

  <p>A.Ceroni, A.Dell and S.M.Haslam, <strong>The GlycanBuilder: a fast,
  intuitive and flexible software tool for building and displaying
  glycan structures</strong>, <a
  href="http://www.scfbm.org/content/2/1/3"><em>Source Code for Biology
  and Medicine, 2007,2:3</em></a>
  </p>
  
  <h2 id="changes">Changes</h2>

  <h4>Version 1.1</h4>
  <dl>
    <dt>Build 3480  - 22/09/2008</dt>
    <dd>
      <ul>
	<li><strong>GAG profiling tool</strong></li>
	<li><strong>in-silico fragmentation of sulfated glycans</strong></li>
	<li>graphic display of the peak list, similar to the spectra panel</li>
	<li>residue properties toolbar</li>
	<li>copy tables in spreadsheets with structures as compositions</li> 
	<li>search databases by sub-structure</li>
	<li>close dialogs by pressing Escape</li>
	<li>select OK in dialogs by pressing Enter</li>
	<li>added executable, icons and splashscreen for Windows users</li>
	<li>fixed export to SVG</li>
      </ul>
    </dd>
  </dl>

  <h4>Version 1.0</h4>
  <dl>
    <dt>Build 3353  - 21/07/2008</dt>
    <dd>
      <ul>
	<li><strong>Store spectra in the workspace file</strong></li>
	<li><strong>Browse structures in the included databases</strong></li>
	<li>Fixed memory overflow with big spectra files</li>
	<li>Fixed graphical export of annotation reports</li>
      </ul>
    </dd>
  </dl>

  <dl>
    <dt>Build 3110  - 12/05/2008</dt>
    <dd>
      <ul>
	<li><strong>Create, edit and store graphical reports of the annotated mass spectrum</strong></li>
	<li><strong>Run composition analysis with <a href="http://www.dkfz.de/spec/EuroCarbDB/applications/ms-tools/GlycoPeakfinder">Glyco-Peakfinder</a></strong></li>
	<li><strong>Search for structures into databases (CFG, Carbbank, Glycosciences)</strong></li>
	<li><strong>Create personalized structure databases</strong></li>
	<li>Annotate MS peak lists with compositions or complete structures</li>
	<li>Create structures with repeating units</li>	
	<li>Support user specified reducing end types</li>
	<li>Manage multiple user defined structure databases</li>
	<li>Import from Carbbank, Glyco-CT; export to Glyde II</li>
	<li>Open ascii spectra files</li>
	<li>Add notes and comments to the workspace</li>
	<li>Copy/Paste/Delete annotations</li>
	<li>Support multiple bonds and multiple linkage positions</li>
	<li>Enlarged set of residues, substitutions and markers</li>
	<li>Compute internal ring fragments</li>
	<li>Support for MGF peak lists</li>
	<li>Bug fixes</li>
      </ul>
    </dd>
  </dl>

  <h4>Version 0.6</h4>
  <dl>
    <dt>Build 1622  - 19/06/2007</dt>
    <dd>
      <ul>
	<li>Workspace management: handle multiple candidate sets, peaklists and annotations
	<li>Workspace panel: handle multiple MS/MS levels and all relative documents
	<li>Save/open annotated peak list
	<li>Import/export to Glyco-CT	
	<li>Import from Glycomind
	<li>Activate undo/redo for all documents
	<li>Annotation options dialog manage maximum number of exchanges 
	<li>Annotate with charges and exchanges derived from parent ion
	<li>Specify multiple charges and neutral exchanges in mass options dialog
	<li>Specify mass options separately for each structure	
	<li>Compute charges and exchanges in fragment panel
	<li>Manage selection and copy fragments in fragment editor
	<li>Copy fragments from summary view
	<li>User settings for graphic options
	<li>Display of antennae with multiplicity
	<li>Progress monitor during annotation
	<li>Recent residue toolbar maintained after closing
	<li>Bug fixes
      </ul>
    </dd>
  </dl>
  
  <h4>Version 0.5</h4>
  <dl>
    <dt>Build 748  - 23/01/2007</dt>
    <dd>
        <ul>
	    <li>Major improvement in the visualization, gives more compact structures and fix display of UOXF notation
	    <li>Annotation of peaks with multiple ion types
	    <li>Negative ion mode with multiple ion types
	    <li>Computation of multiple cross-ring fragments
	    <li>Find a fragment from a given m/z value
	    <li>Copy rows of table panels in Excel
	    <li>Cut/delete in fragment list panel
	    <li>Creation of the Plugin API, trasformation of tools in plugins, search for plugins at run-time
	    <li>Added modification (deoxygenation, saturation) to the list of components
	    <li>Added templates for GAGs
	    <li>Rework of options dialogs
	</ul>
    </dd>

  <h4>Version 0.4</h4>
  <dl>
    <dt>Build 642  - 7/12/2006</dt>
    <dd>
        <ul>
	    <li>visual editor of fragments
	    <li>manual revision of annotated peak list
	    <li>computation of m/z values with multiple charges 
	    <li>cleavage of substituents
	    <li>enlarged list of ring fragments (all the residues are now supported)
	    <li>calibration panel in PPM
	    <li>reorganization of tool panels
	    <li>autosave options
	    <li>CFG notation with automatic placement regarding of linkage
	    <li>display of structure in compact version
	    <li>export image without reducing end marker
	    <li>fixed mass of derivatized labeled structures
	    <li>bug fixes
	</ul>
    </dd>
      
    <dt>Build 462  - 2/11/2006</dt>
    <dd>
        <ul>
	   <li>spectra viewer (support for ABI, Bruker, mzXML and mzData spectra files)
	   <li>calibration panel
	   <li>manual placement of residues in the editor
	   <li>import from LINUCS files
	   <li>command line invocation
	   <li>editing of annotated peak list
	   <li>annotation accuracy in PPM
	   <li>bug fixes
	</ul>
    </dd>
  </dl>

  <h4>Version 0.3</h4>
  <dl>
    <dt>Build 326  - 11/9/2006</dt>
    <dd>First public version</dd>
  </dl>

  <h2 id="acknowledgements">Acknowledgements</h2> 

  <p>First of all, I must give a big thanks to the Glycoconjugate
  biochemistry group at JLUG and especially to Tobias Lehr, which have
  provided continuous testing and feedback to GlycoWorkbench and made
  it possible to reach its current level of functionality.</p>

  <p>All my other thanks go to those projects which have provided free
  of charge some great Java libraries, sharing the result of their work
  with all the developers community. Many features of this software
  would not be possible without their effort. We should always
  remember to support open source and open access policies.</p>

  <p>The component for loading raw spectra files have been realized
  using the fantastic library developed by Jayson Falkner and his
  collegues at <a
  href="http://www.proteomecommons.org/current/531/">ProteomeCommons</a>. They
  are working really hard to extend the support to all available
  formats in close collaboration with the MS instrumentation
  vendors. These guys really need all the appraisal and the support
  from the mass spectrometry community.</p>

  <p>The components for displaying graphs have been realized using the
  <a href="http://www.jfree.org/jfreechart/">JFreeChart</a> library,
  from the JFree collection of Java free software projects.</p>

  <p>The export to SVG, EPS and PDF has been made possible by the <a
  href="http://xmlgraphics.apache.org/batik/">Batik</a> toolkit from
  the Apache XML Graphics Project, part of the Apache Software
  Foundation for open source software.</p>  
</div>

<#include "footer.ftl" />


