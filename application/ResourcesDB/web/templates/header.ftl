<#setting locale="en_US">
<#--<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<#--<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
        "http://www.w3.org/TR/html4/strict.dtd"> -->
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
       "http://www.w3.org/TR/html4/loose.dtd">
<html <#--xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en"-->>

<head>

  <title>${title}</title>
  <!-- Begin related links -->
  <link rel="shortcut icon"
        href="images/msdbweb_favicon.ico" >
  <link rel="copyright" title="Copyright"
        href="http://www.eurocarbdb.org/copyright" >
  <!-- End related links -->

  <!-- Begin stylesheets -->
  <link rel="stylesheet" type="text/css"
        href="./msdb_styles.css" >
  <!-- End stylesheets -->

  <!-- Begin script slot -->
  <script src="plugin/jmol/Jmol.js" type="text/javascript"></script>
  <!-- <script src="./glycopeakfinder.js" type="text/javascript"></script> -->
  <!-- End script slot -->

</head>

<body>
<div id="header">
<span class="header_title">MonoSaccharide<span class="red">DB</span></span>
<span class="header_right">by <a href="http://www.eurocarbdb.org" target="_blank">EUROCarb<span class="red">DB</span><img src="images/ecdb_logo.png" width="49" height="32" class="noborder" alt="ECDB Logo" ></a></span>
</div>
<#if mainMenuItems??>
	<div id="top_menu">
	<#list mainMenuItems as item>
		<#if currentMainMenuItem?? && item.name() == currentMainMenuItem.name()>
		&bull;&nbsp;${item.getLabel()}
		<#else>
		&bull;&nbsp;<a href="${item.actionName}">${item.getLabel()}</a>
		</#if>
	</#list>
	<span class="right"><a href="javascript:history.back()">back</a></span>
	</div>
	<hr class="redline">
</#if>
<#if subMenuItems??>
	<div id="submenu">
	<#list subMenuItems as subItem>
		&bull;&nbsp;<a href="${subItem.actionName}"<#if currentSubMenuItem?? && subItem.name() == currentSubMenuItem.name()> class="selected"</#if>>${subItem.getLabel()}</a>
	</#list>
	<#if helpAction??>
		<span class="right"><a href="${helpAction}${helpActionArguments}">Help</a></span>
	</#if>
	</div>
	<hr class="redline">
</#if>

<div class="content">