<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<#if currentContributor?exists && currentContributor.isLoggedIn() && !currentContributor.getLastLogin()??>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<#import "/template/lib/TextUtils.lib.ftl" as text />
<#setting url_escaping_charset='ISO-8859-1'>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">


<head><#--====================================================================== begin html head -->

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
-->
<#if additional_head_content?exists >
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
<#--
<script>
  var onLoadFunctions=[];
  function runOnLoad(){
    for(var func in onLoadFunctions){
      onLoadFunctions[func]();
    }
  }
</script>
-->

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
     <#-- onload="<#if onload_function?exists>${onload_function};"</#if>runOnLoad()"
      <#if onunload_function?exists>onunload="${onunload_function}"</#if>
     -->
><#--===================================================================== begin html body -->

<div id="oldbrowser">
<p>
    If the formatting of this page doesn't look right then chances are
    that you're using an old browser. Perhaps it's time to
    <a href="http://mozilla.com">upgrade</a>?
</p>
</div>
<#else>
<#include "/template/common/header.ftl" />
</#if>
<#assign title>Change Password</#assign>


                <h1>${title}</h1>
                <#if ( message?length > 0 )>
                <div class="error_message">
                  ${message}
                </div>
                </#if>
            <@ww.form theme="simple" method="post">
                <table class="table_form">
                    <tr><th>Current Password*:</th><td><@ww.password name="currentPassword" value="" /></td></tr>
                    <tr><th>New Password*:</th><td><@ww.password name="newPassword" value="" /></td></tr>
                    <tr><th>Confirm Password*:</th><td><@ww.password name="confirmPassword" value="" /></td></tr>                    
                    <tr><td colspan="2" align="center"><@ww.submit value="Done"/>  <@ww.reset value="Clear"/></td></tr>
                </table>
            </@ww.form>

<#if currentContributor?exists && currentContributor.isLoggedIn() && currentContributor.getLastLogin()??>
    <#include "/template/common/footer.ftl" />
</#if>