<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<!-- Silva 1.0 -->
<html xmlns="http://www.w3.org/1999/xhtml" lang="en"
      xml:lang="en">

<head>

		<meta name="description" content="${pageDescription}"/>
		<meta name="author" content="${pageAuthor}"/>
		<meta name="keywords" content="${pageKeywords}"/>

  <title>${pageTitle}</title>
  <!-- Start related links -->
  <link rel="shortcut icon"
        href="http://www.eurocarbdb.org/globals/favicon.ico" />
  <link rel="shortcut icon" />

  <!-- /End related links -->

  <!-- Begin stylesheets -->
  <link rel="stylesheet" type="text/css" media="all"
        href="./table-less.css" />
  <link rel="stylesheet" type="text/css" 
        href="./glycopeakfinder.css" />
  
  <!-- /End stylesheets -->

  <!-- Begin script slot -->
  <script src="./glycopeakfinder.js" type="text/javascript"></script>
  <!-- End script slot -->

</head>

<body class="page">


  <!-- Start page_header slot -->
  <div id="header">

    <div id="logo1pos">
      <a href="http://www.eurocarbdb.org">
        <img border="0"
             src="http://www.eurocarbdb.org/images/logo" /> 
      </a>

    </div>

<!--    <div id="logo2pos">
      <a href="http://www.eurocarbdb.org">
        <img border="0" src="images/logo-text" tal:attributes="src here/images/logo-text/absolute_url" /> 
      </a>
    </div>
-->
    <div id="logo3pos">
      <a href="http://www.infrae.com/products/silva">
        <img src="http://www.eurocarbdb.org/globals/powered_by.png"
             width="64" height="16" border="0" alt="Silva" /> </a>
      <a href="silva/silva_docs">
        <img src="http://www.eurocarbdb.org/images/book_16.png"
             width="18" border="0" alt="Silva Documentation" />
        <span style="color: #ffe;">Silva_docs</span>  </a>

    </div>
  </div>

  <!-- Begin Topmenu  -->
  <div id="topmenu">
    <ul>    
      <li><a class="topmenu-here" href="http://www.eurocarbdb.org/about">About</a>&nbsp;|&nbsp;</li>
      <li><a href="http://www.eurocarbdb.org/recommendations">Recommendations & protocols</a>&nbsp;|&nbsp;</li>

      <li><a href="http://www.eurocarbdb.org/databases">Databases</a>&nbsp;|&nbsp;</li>
      <li><a href="http://www.eurocarbdb.org/applications">Applications</a>&nbsp;|&nbsp;</li>
      <li><a href="http://www.eurocarbdb.org/links">Links</a>&nbsp;|&nbsp;</li>
      <li><a href="http://www.dkfz.de/spec/EuroCarbDB_forum/">Forum</a></li>
    </ul>

  </div>
  <!-- End Topmenu  -->

  <!-- Begin Submenu  -->
  <div id="submenu">
          <ul>
             
          </ul>

  </div> 
  <!-- End Submenu  -->


  <!-- Begin Breadcrumbs  -->
  <div id="location_left">
    <!-- Begin top_navigation slot -->
    
       
        <a href="http://www.eurocarbdb.org">EuroCarbDB</a>
      &middot;
       
        <a href="http://www.eurocarbdb.org/applications">Applications</a>
      &middot;

       
        <a href="http://www.eurocarbdb.org/applications/ms-tools">Tools for analysis of MS spectra</a>
      &middot;

       
        <a href="http://www.dkfz.de/spec/EuroCarbDB/applications/ms-tools/GlycoPeakfinder/">GlycoPeakfinder</a>
    <!-- End top_navigation slot -->
    
  </div>
  <!-- End Breadcrumbs  -->

  <!-- Begin Hosts  -->
  <div id="location_right">
    ::
    <a href="http://www.dkfz-heidelberg.de/spec/" target="_blank"> DKFZ</a> ::
    <a href="http://www.bijvoet-center.nl/" target="_blank"> Bijvoet Center</a> ::
    <a href="javascript:history.back();">back</a> ::
  </div>

  <!-- End Hosts  -->


<!-- End page_header slot -->
<div>

<#include "./side_menu.ftl">

<div class="public">
