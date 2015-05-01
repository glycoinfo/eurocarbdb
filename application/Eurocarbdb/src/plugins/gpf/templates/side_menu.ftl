<#--<div id="navigations">-->
     
  <!-- div class="gp-navigation-box" -->
    	<#--<ul class="gp-navigation-body">-->
<#if pageType = "calculation" && result.initialized = false>

<#--<li <#if pageType = "introduction">class="gp-navigation-here"<#else>class="gp-navigation-body"</#if>> 	&nbsp;<#--<img src="images/icon_intro.gif" alt="icon"/>	&nbsp;--><a href="#" id="side_intro" border="0" title="Introduction" onclick="js:switchToPage('side_intro');">Introduction</a><#--<#--</li>--> 
<#--<li style="text-align:center;"><#--<img src="images/separator.png" height="6" width="145">--><#--<#--</li>-->
<#--<li <#if pageType = "load_settings">class="gp-navigation-here"<#else>class="gp-navigation-body"</#if>> 	&nbsp;<#--<img src="images/icon_l_set.gif" alt="icon"/>	&nbsp;--><a href="#" id="side_lsett" border="0" title="Load Settings" onclick="js:switchToPage('side_lsett');">Load Settings</a><#--<#--</li>-->
<#--<li <#if pageType = "save_settings">class="gp-navigation-here"<#else>class="gp-navigation-body"</#if>> 	&nbsp;<#--<img src="images/icon_s_set.gif" alt="icon"/>	&nbsp;--><a href="#" id="side_ssett" border="0" title="Save Settings" onclick="js:switchToPage('side_ssett');">Save Settings</a><#--<#--</li>--> 
<#--<li style="text-align:center;"><#--<img src="images/separator.png" height="6" width="145">--><#--<#--</li>-->
<#--<li <#if pageType = "calculation">class="gp-navigation-here"<#else>class="gp-navigation-body"</#if>> 		&nbsp;<#--<img src="images/icon_calc.gif" alt="icon"/>	&nbsp;--><a href="#" id="side_calc" border="0" title="Calculation" onclick="js:switchToPage('side_calc');">Calculation</a><#--</li>--> 
<#--<li style="text-align:center;"><#--<img src="images/separator.png" height="6" width="145">--><#--</li>-->
<#--<li <#if pageType = "load_result">class="gp-navigation-here"<#else>class="gp-navigation-body"</#if>> 		&nbsp;<#--<img src="images/icon_l_res.gif" alt="icon"/>	&nbsp;--><a href="#" id="side_lres" border="0" title="Load Result" onclick="js:switchToPage('side_lres');">Load Result</a><#--</li>--> 
<#--<li <#if pageType = "save_result">class="gp-navigation-here"<#else>class="gp-navigation-body"</#if>> 		&nbsp;<#--<img src="images/icon_s_res.gif" alt="icon"/>	&nbsp;--><#if result.initialized = true><a href="#" id="side_sres" border="0" title="Save Result" onclick="js:switchToPage('side_sres');">alt="Save Result"</a><#else><a border="0">Save Result</a></#if><#--</li>--> 
<#--<li style="text-align:center;"><#--<img src="images/separator.png" height="6" width="145">--><#--</li>-->
<#--<li <#if pageType = "download">class="gp-navigation-here"<#else>class="gp-navigation-body"</#if>> 		&nbsp;<#--<img src="images/icon_down.gif" alt="icon"/>	&nbsp;--><a href="#" id="side_down" border="0" title="Download" onclick="js:switchToPage('side_down');">Download</a><#--</li>--> 
<#--<li <#if pageType = "contact">class="gp-navigation-here"<#else>class="gp-navigation-body"</#if>> 			&nbsp;<#--<img src="images/icon_contact.gif" alt="icon"/> &nbsp;--><a href="#" id="side_cont" border="0" title="Contact" onclick="js:switchToPage('side_cont');">Contact</a><#--</li>--> 

<#else>

<#--<li <#if pageType = "introduction">class="gp-navigation-here"<#else>class="gp-navigation-body"</#if>> 	&nbsp;<#--<img src="images/icon_intro.gif" alt="icon"/>	&nbsp;--><a  class="navigation-body" href="Introduction.action" title="Introduction">Introduction</a><#--</li>--> 
<#--<li style="text-align:center;"><#--<img src="images/separator.png" height="6" width="145">--><#--</li>-->
<#--<li <#if pageType = "load_settings">class="gp-navigation-here"<#else>class="gp-navigation-body"</#if>> 	&nbsp;<#--<img src="images/icon_l_set.gif" alt="icon"/>	&nbsp;--><a  class="navigation-body" href="LoadSettings.action" title="Load Settings">Load Settings</a><#--</li>-->
<#--<li <#if pageType = "save_settings">class="gp-navigation-here"<#else>class="gp-navigation-body"</#if>> 	&nbsp;<#--<img src="images/icon_s_set.gif" alt="icon"/>	&nbsp;--><a  class="navigation-body" href="SaveSettings.action" title="Save Settings">Save Settings</a><#--</li>--> 
<#--<li style="text-align:center;"><#--<img src="images/separator.png" height="6" width="145">--><#--</li>-->
<#--<li <#if pageType = "calculation">class="gp-navigation-here"<#else>class="gp-navigation-body"</#if>> 		&nbsp;<#--<img src="images/icon_calc.gif" alt="icon"/>	&nbsp;--><a  class="navigation-body" href="<#if result.initialized = true>Result<#else>Input</#if>.action" title="Calculation">Calculation</a><#--</li>--> 
<#--<li style="text-align:center;"><#--<img src="images/separator.png" height="6" width="145">--><#--</li>-->
<#--<li <#if pageType = "load_result">class="gp-navigation-here"<#else>class="gp-navigation-body"</#if>> 		&nbsp;<#--<img src="images/icon_l_res.gif" alt="icon"/>	&nbsp;--><a  class="navigation-body" href="LoadResult.action" title="Load Result">Load Result</a><#--</li>--> 
<#--<li <#if pageType = "save_result">class="gp-navigation-here"<#else>class="gp-navigation-body"</#if>> 		&nbsp;<#--<img src="images/icon_s_res.gif" alt="icon"/>	&nbsp;--><#if result.initialized = true><a  class="navigation-body" href="SaveResult.action" title="Save Result">Save Result</a><#else><a class="navigation-disable">Save Result</a></#if><#--</li>--> 
<#--<li style="text-align:center;"><#--<img src="images/separator.png" height="6" width="145">--><#--</li>-->
<#--<li <#if pageType = "download">class="gp-navigation-here"<#else>class="gp-navigation-body"</#if>> 		&nbsp;<#--<img src="images/icon_down.gif" alt="icon"/>	&nbsp;--><a  class="navigation-body" href="Download.action" title="Download">Download</a><#--</li>--> 
<#--<li <#if pageType = "contact">class="gp-navigation-here"<#else>class="gp-navigation-body"</#if>> 			&nbsp;<#--<img src="images/icon_contact.gif" alt="icon"/> &nbsp;--><a  class="navigation-body" href="Contact.action" title="Contact">Contact</a><#--</li>--> 

</#if>
        <#--</ul>-->
	<!-- /div -->
<#--</div>-->
