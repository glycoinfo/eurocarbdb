<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<#import "/template/lib/TextUtils.lib.ftl" as text />
<#setting url_escaping_charset='ISO-8859-1'>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">

<#if ! title?exists><#assign title="EuroCarbDB" /></#if>
<head><#--====================================================================== begin html head -->
<title>${title}</title>
<meta content="text/html; charset=ISO-8859-1" http-equiv="content-type" />
<meta name="description" content="EuroCarbDB is a database of known carbohydrate structures and their experimental data, initially mass spectrometry, HPLC and NMR data" />
<meta name="keywords" content="glycobase, nibrt, glycan, glycoinformatics, carbohydrate, polysaccharide, oligosaccharide, glycosylation, monosaccharide, sugar, glycobiology, glycoprotein, glycolipid, bioinformatics, eurocarb, eurocarbdb, EBI, glycosciences" />
<meta name="author" content="matt harrison, alessio ceroni, matthew campbell, hiren joshi, magnus lundborg, kai maass" />

<link rel="stylesheet" type="text/css" href="${base}/css/eurocarb_new.css"/>
<link rel="stylesheet" type="text/css" href="${base}/css/jquery-ui-1.7.2.custom.css"  />	
<!--[if IE 7]>
<link href="${base}/css/eurocarb.ie7.css" rel="stylesheet" type="text/css" />
<![endif]-->

<!--[if IE 8]>
<link href="${base}/css/eurocarb.ie8.css" rel="stylesheet" type="text/css" />
<![endif]-->
<@ww.url value="/js/lib/EurocarbCommon.js" includeParams="none" id="url_ecdb_common_js"/>
<@ww.url value="/js/lib/MochiKit-1.4.2/MochiKit.js" includeParams="none" id="url_mochikit_js"/>
<@ww.url value="/js/lib/JQuery/jquery-1.3.2.min.js" includeParams="none" id="url_jquery_core"/>
<@ww.url value="/js/lib/JQuery/jquery-ui-1.7.2.custom.min.js" includeParams="none" id="url_jquery_ui"/>


<#--
This next section is for templates who use this header tmpl that need to add 
additional stuff into the HTML header -- just set/append to the freemarker 
variable 'additional_head_content'.
--><#if additional_head_content?exists >
<!-- start of additional head section content -->
${additional_head_content}
<!-- end of additional head section content -->
</#if>

<#if (ecdb.css_includes)?exists >
<!-- ecdb CSS includes -->
<#list ecdb.css_includes?reverse as a_css>
<link rel="stylesheet" type="text/css" href="${a_css}"/>
</#list>
<!-- end ecdb CSS includes -->
</#if>


<#if (ecdb.js_includes)?exists >
<!-- ecdb js includes -->
<script src="${url_mochikit_js}" type="text/javascript"></script>
<script src="${url_ecdb_common_js}" type="text/javascript"></script>
<script src="${url_jquery_core}" type="text/javascript"></script>
<script src="${url_jquery_ui}" type="text/javascript"></script>
<script type="text/javascript">jQuery.noConflict();</script>
<#list ecdb.js_includes as a_js>
<script src="${a_js}" type="text/javascript"></script>
</#list>
<!-- end ecdb js includes -->
</#if>



<!--
  Setup array to store functions which should be called "onload".
  Setup function to call each "onload" function.
-->
<script>
var onLoadFunctions;
    
function runOnLoad()
{
    if ( onLoadFunctions == undefined )
        onLoadFunctions = [];
    
    for(var func in onLoadFunctions){
        console.log("running func: " + onLoadFunctions[func] );  
        onLoadFunctions[func]();
    }
}
</script>

<script type="text/javascript">
//<![CDATA[
    connect(window,'onload',ECDB.windowLoadScript);

    function show(id,display) 
    {
        for (var i = 1; i<=10; i++) 
        {
          var m = $('menu'+i);
          var d = $('smenu'+i);
          if( m && d ) 
          {
            if( i==id ) 
            {
		          if( display=='block' ) {
		            m.style.background='#44c';
		            addElementClass(m,'active_menu');
              } else {
		            m.style.background='#000099';
		            removeElementClass(m,'active_menu');
              }
              d.style.display=display;
            }
            else 
            {
        	    m.style.background='#000099';
              d.style.display='none';
            }
          }
        }
    }
    connect(ECDB,'onload',show);

//]]>
 </script>


</head><#--======================================================================= end html head -->
<body class="yui-skin-sam"
      onload="<#if onload_function?exists>${onload_function};"</#if>runOnLoad()"
      <#if onunload_function?exists>onunload="${onunload_function}"</#if>
><#--===================================================================== begin html body -->
      
<div id="oldbrowser">
<p>
    If the formatting of this page doesn't look right then chances are
    that you're using an old browser. Perhaps it's time to 
    <a href="http://mozilla.com">upgrade</a>? 
</p>
</div>    
   
<div id="header"><#--========================================================== begin header div -->

<div id="title"><#-- begin title div -->
<a href="${base}/home.action"><img src="${base}/images/eurocarb_logo_transparent.png"/>EurocarbDB</a>
</div><#--============ end title div -->

<div id="login_link">
<#if currentContributor?exists && currentContributor.isLoggedIn()>
<a class="home" href="${base}/show_contributor.action?contributorId=${currentContributor.contributorId}">Home</a>
<a class="loginout" href="${base}/logout.action">Logout</a>
<#else>
<a class="loginout" href="${base}/login.action">Login</a>
</#if>
</div>


<div id="perspectives" onmouseout="show()"><#--============================================== begin perspectives div ===
this div wraps the top-row menu items.
-->

<!-- Browse -->
<div class="menu_item" onmouseover="show('1','block')">
  <div class="menu_title"><a id="menu1" href="${base}/browse.action">Browse</a></div>
  <div class="sub_menu" onmouseover="show('1','block')" id="smenu1">
   
      <a href="${base}/browse_structures.action">Browse structures</a>
      <a href="${base}/browse_evidence.action">Browse evidence</a>
      <hr/>
      <a href="${base}/show_taxonomy.action">Browse taxonomies</a>
      <a href="${base}/show_tissue_taxonomy.action">Browse tissues</a>
      <a href="${base}/show_disease.action">Browse diseases</a>
      <a href="${base}/show_perturbation.action">Browse perturbations</a>
      <hr/>
      <a href="${base}/show_glycobase.action?imageStyle=uoxf">Browse GlycoBase</a>	    
  </div>
</div>

<!-- Search -->
<div class="menu_item" onmouseover="show('2','block')">
  <div class="menu_title"><a id="menu2" href="${base}/search.action">Search</a></div>
  <div class="sub_menu" onmouseover="show('2','block')" id="smenu2">   
      <a href="${base}/search_glycan_sequence.action">Search structures</a>
      <a href="${base}/search_biological_context.action">Search biological contexts</a>
      <hr/>
      <a href="${base}/search_glycobase.action">Search GlycoBase HPLC data</a> 
  </div>
</div>

<!-- Contribute -->
<div class="menu_item" onmouseover="show('3','block')">
    <div class="menu_title"><a id="menu3" href="${base}/contribute.action">Contribute</a></div>
    <div class="sub_menu" onmouseover="show('3','block')" id="smenu3">
        <a href="${base}/contribute_structure.action">Contribute structure</a>
        <a href="${base}/create_ms.action">Contribute MS data</a>
        <a href="${base}/select_settings.action">Contribute HPLC data</a>
        <a href="javascript:alert('Sorry, the workflow for the contribution of NMR data is not yet finished!');">Contribute NMR data</a>
    </div>	
</div>

<!-- Tools -->
<div class="menu_item" onmouseover="show('4','block')">
  <div class="menu_title"><a id="menu4" href="${base}/tools.action">Tools</a></div>
  <div class="sub_menu" onmouseover="show('4','block')" id="smenu4">	   
    <a href="${base}/gpf/Introduction.action">Glyco-Peakfinder</a>
    <a href="${base}/gwb/home.action">GlycoWorkbench</a>
    <a href="${base}/select_instrument.action">AutoGU</a>
    <a href="http://www.casper.organ.su.se/eurocarbdb/casper.action">CASPER</a>
  </div>
</div>	

<!-- Documentation
<div class="menu_item" onmouseover="show('5','block')">
  <div class="menu_title"><a id="menu5" href="${base}/help.action">Documentation</a></div>
  <div class="sub_menu" onmouseover="show('5','block')" id="smenu5">
    <a href="${base}/help.action">Help contents</a>
    <a href="${base}/wiki.action">Wiki</a>
    <a href="${base}/about.action">About</a>
  </div>
</div>
-->
<#if currentContributor?exists && currentContributor.isLoggedIn()>
<#if currentContributor.getIsAdmin()==true>
    <#-- user admin -->
    <div class="menu_item" onmouseover="show('6','block')">
        <div class="menu_title"> <a id="menu6" href="#">Adminstration</a></div>
        <div class="sub_menu" onmouseover="show('6','block')" id="smenu6">

            <a href="${base}/showRequests.action"/>Requests(${currentContributor.getNumberOfInactiveContributors()})</a>
            <hr/>
            <a href="${base}/showPromotableContributors.action"/>Promote User</a>
            <a href="${base}/showDemotableContributors.action"/>Demote User</a>
            <hr/>
            <a href="${base}/showUnblockedContributors.action"/>Block User</a>
            <a href="${base}/showBlockedContributors.action"/>Unblock User</a>
        </div>
    </div>
</#if>
</#if>
<!-- About -->
<div class="menu_item  menu_title single_link" >
    <a href="${base}/about.action">About</a>
</div>

</div><#--================================================================= end perspectives div -->
</div><#--======================================================================= end header div -->

