<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
  <head>
    <meta content="text/html; charset=ISO-8859-1" http-equiv="content-type"><title>${title}</title>
     <link rel="icon" href="/ecdb/images/favicon.ico" />
     <link rel="shortcut icon" href="/ecdb/images/favicon.ico" /> 
     <link rel="stylesheet" type="text/css" href="/ecdb/css/eurocarb_new.css"/>

     <#if (ecdb.css_includes)?exists >
     <#list ecdb.css_includes as a_css>
     <link rel="stylesheet" type="text/css" href="${a_css}"/>
     </#list>
     </#if>
	
     <#if (ecdb.js_includes)?exists >
     <#list ecdb.js_includes as a_js>
     <script src="${a_js}" type="text/javascript"></script>
     </#list>
     </#if>

     <script type="text/javascript">

       window.onload=show;

       function show(id,display) {
         for (var i = 1; i<=10; i++) {
           d = document.getElementById('smenu'+i);
	   if( d ) {
	     if( i==id ) {
                d.style.display=display;
             }
	     else {
	       d.style.display='none';
	     }
           }
	 }
       }

       function GetXmlHttpObject() {
         var xmlHttp=null;
	 try {
	   // Firefox, Opera 8.0+, Safari
	   xmlHttp=new XMLHttpRequest();
	 }
	 catch (e) {
	   // Internet Explorer
	   try {
	     xmlHttp=new ActiveXObject("Msxml2.XMLHTTP");
	   }
	   catch (e) {
	     xmlHttp=new ActiveXObject("Microsoft.XMLHTTP");
	   }
	 }
	 return xmlHttp;
       }

       function setCookie( name, value ) {
         // set time, it's in milliseconds
	 var today = new Date();
	 today.setTime( today.getTime() );

	 // expire in one month
	 var expires = 30 * 1000 * 60 * 60 * 24; 
	 var expires_date = new Date( today.getTime() + (expires) );

	 // set cookie
	 var cookie = name + "=" +escape( value ) +
	   ";expires=" + expires_date.toGMTString();
	  
	 document.cookie = cookie
       }

     </script>
  </head>
  <#if onload_function?exists>
  <body onload="${onload_function}">
  <#else>
  <body>
  </#if>
    
