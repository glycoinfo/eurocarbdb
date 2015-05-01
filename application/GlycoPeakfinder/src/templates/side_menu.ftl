<div class="left-box">
     
	<div class="gp-navigation-box">
    <p class="navigation-head">Glyco-Peakfinder</p>
    	<ul class="gp-navigation-body">
<#if pageType = "calculation" && result.initialized = false>

<li <#if pageType = "introduction">class="gp-navigation-here"<#else>class="gp-navigation-body"</#if>> 	&nbsp;<img src="${baseUrl}/images/icon_intro.gif" alt="icon"/>	&nbsp;<img src="${baseUrl}/images/side_normal_intro.png" onmouseover="getElementById('side_intro').src='${baseUrl}/images/side_link_intro.png'" onmouseout="getElementById('side_intro').src='${baseUrl}/images/side_normal_intro.png'" id="side_intro" border="0" title="Introduction"  alt="Introduction" onclick="js:switchToPage('side_intro');"/></li> 
<li style="text-align:center;"><img src="${baseUrl}/images/separator.png" height="6" width="145"></li>
<li <#if pageType = "load_settings">class="gp-navigation-here"<#else>class="gp-navigation-body"</#if>> 	&nbsp;<img src="${baseUrl}/images/icon_l_set.gif" alt="icon"/>	&nbsp;<img src="${baseUrl}/images/side_normal_lsett.png" onmouseover="getElementById('side_lsett').src='${baseUrl}/images/side_link_lsett.png'" onmouseout="getElementById('side_lsett').src='${baseUrl}/images/side_normal_lsett.png'" id="side_lsett" border="0" title="Load Settings" alt="Load Settings" onclick="js:switchToPage('side_lsett');"/></li>
<li <#if pageType = "save_settings">class="gp-navigation-here"<#else>class="gp-navigation-body"</#if>> 	&nbsp;<img src="${baseUrl}/images/icon_s_set.gif" alt="icon"/>	&nbsp;<img src="${baseUrl}/images/side_normal_ssett.png" onmouseover="getElementById('side_ssett').src='${baseUrl}/images/side_link_ssett.png'" onmouseout="getElementById('side_ssett').src='${baseUrl}/images/side_normal_ssett.png'" id="side_ssett" border="0" title="Save Settings" alt="Save Settings" onclick="js:switchToPage('side_ssett');"/></li> 
<li style="text-align:center;"><img src="${baseUrl}/images/separator.png" height="6" width="145"></li>
<li <#if pageType = "calculation">class="gp-navigation-here"<#else>class="gp-navigation-body"</#if>> 		&nbsp;<img src="${baseUrl}/images/icon_calc.gif" alt="icon"/>	&nbsp;<img src="${baseUrl}/images/side_normal_calc.png" onmouseover="getElementById('side_calc').src='${baseUrl}/images/side_link_calc.png'" onmouseout="getElementById('side_calc').src='${baseUrl}/images/side_normal_calc.png'" id="side_calc" border="0" title="Calculation" alt="Calculation" onclick="js:switchToPage('side_calc');"/></li> 
<li style="text-align:center;"><img src="${baseUrl}/images/separator.png" height="6" width="145"></li>
<li <#if pageType = "load_result">class="gp-navigation-here"<#else>class="gp-navigation-body"</#if>> 		&nbsp;<img src="${baseUrl}/images/icon_l_res.gif" alt="icon"/>	&nbsp;<img src="${baseUrl}/images/side_normal_lresult.png" onmouseover="getElementById('side_lres').src='${baseUrl}/images/side_link_lresult.png'" onmouseout="getElementById('side_lres').src='${baseUrl}/images/side_normal_lresult.png'" id="side_lres" border="0" title="Load Result" alt="Load Result" onclick="js:switchToPage('side_lres');"/></li> 
<li <#if pageType = "save_result">class="gp-navigation-here"<#else>class="gp-navigation-body"</#if>> 		&nbsp;<img src="${baseUrl}/images/icon_s_res.gif" alt="icon"/>	&nbsp;<#if result.initialized = true><img src="${baseUrl}/images/side_normal_sresult.png" onmouseover="getElementById('side_sres').src='${baseUrl}/images/side_link_sresult.png'" onmouseout="getElementById('side_sres').src='${baseUrl}/images/side_normal_sresult.png'" id="side_sres" border="0" title="Save Result" alt="Save Result" onclick="js:switchToPage('side_sres');"/><#else><img src="${baseUrl}/images/side_deakt_sresult.png"/></#if></li> 
<li style="text-align:center;"><img src="${baseUrl}/images/separator.png" height="6" width="145"></li>
<li <#if pageType = "download">class="gp-navigation-here"<#else>class="gp-navigation-body"</#if>> 		&nbsp;<img src="${baseUrl}/images/icon_down.gif" alt="icon"/>	&nbsp;<img src="${baseUrl}/images/side_normal_down.png" onmouseover="getElementById('side_down').src='${baseUrl}/images/side_link_down.png'" onmouseout="getElementById('side_down').src='${baseUrl}/images/side_normal_down.png'" id="side_down" border="0" title="Download" alt="Download" onclick="js:switchToPage('side_down');"/></li> 
<li <#if pageType = "contact">class="gp-navigation-here"<#else>class="gp-navigation-body"</#if>> 			&nbsp;<img src="${baseUrl}/images/icon_contact.gif" alt="icon"/> &nbsp;<img src="${baseUrl}/images/side_normal_contact.png" onmouseover="getElementById('side_cont').src='${baseUrl}/images/side_link_contact.png'" onmouseout="getElementById('side_cont').src='${baseUrl}/images/side_normal_contact.png'" id="side_cont" border="0" title="Contact" alt="Contact" onclick="js:switchToPage('side_cont');"/></li> 

<#else>

<li <#if pageType = "introduction">class="gp-navigation-here"<#else>class="gp-navigation-body"</#if>> 	&nbsp;<img src="${baseUrl}/images/icon_intro.gif" alt="icon"/>	&nbsp;<a  class="navigation-body" href="${baseUrl}/Introduction.action" title="Introduction">Introduction</a></li> 
<li style="text-align:center;"><img src="${baseUrl}/images/separator.png" height="6" width="145"></li>
<li <#if pageType = "load_settings">class="gp-navigation-here"<#else>class="gp-navigation-body"</#if>> 	&nbsp;<img src="${baseUrl}/images/icon_l_set.gif" alt="icon"/>	&nbsp;<a  class="navigation-body" href="${baseUrl}/LoadSettings.action" title="Load Settings">Load Settings</a></li>
<li <#if pageType = "save_settings">class="gp-navigation-here"<#else>class="gp-navigation-body"</#if>> 	&nbsp;<img src="${baseUrl}/images/icon_s_set.gif" alt="icon"/>	&nbsp;<a  class="navigation-body" href="${baseUrl}/SaveSettings.action" title="Save Settings">Save Settings</a></li> 
<li style="text-align:center;"><img src="${baseUrl}/images/separator.png" height="6" width="145"></li>
<li <#if pageType = "calculation">class="gp-navigation-here"<#else>class="gp-navigation-body"</#if>> 		&nbsp;<img src="${baseUrl}/images/icon_calc.gif" alt="icon"/>	&nbsp;<a  class="navigation-body" href="${baseUrl}/<#if result.initialized = true>Result<#else>Input</#if>.action" title="Calculation">Calculation</a></li> 
<li style="text-align:center;"><img src="${baseUrl}/images/separator.png" height="6" width="145"></li>
<li <#if pageType = "load_result">class="gp-navigation-here"<#else>class="gp-navigation-body"</#if>> 		&nbsp;<img src="${baseUrl}/images/icon_l_res.gif" alt="icon"/>	&nbsp;<a  class="navigation-body" href="${baseUrl}/LoadResult.action" title="Load Result">Load Result</a></li> 
<li <#if pageType = "save_result">class="gp-navigation-here"<#else>class="gp-navigation-body"</#if>> 		&nbsp;<img src="${baseUrl}/images/icon_s_res.gif" alt="icon"/>	&nbsp;<#if result.initialized = true><a  class="navigation-body" href="${baseUrl}/SaveResult.action" title="Save Result">Save Result</a><#else><span class="navigation-disable">Save Result</span></#if></li> 
<li style="text-align:center;"><img src="${baseUrl}/images/separator.png" height="6" width="145"></li>
<li <#if pageType = "download">class="gp-navigation-here"<#else>class="gp-navigation-body"</#if>> 		&nbsp;<img src="${baseUrl}/images/icon_down.gif" alt="icon"/>	&nbsp;<a  class="navigation-body" href="${baseUrl}/Download.action" title="Download">Download</a></li> 
<li <#if pageType = "contact">class="gp-navigation-here"<#else>class="gp-navigation-body"</#if>> 			&nbsp;<img src="${baseUrl}/images/icon_contact.gif" alt="icon"/> &nbsp;<a  class="navigation-body" href="${baseUrl}/Contact.action" title="Contact">Contact</a></li> 

</#if>
        </ul>
	</div>
    <br />
</div>
