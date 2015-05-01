<#--  
= !Eurocarb.lib.ftl =

<wiki:toc>

A library of generic Freemarker text macros for generating HTML interfaces
for the Eurocarb project.

To use this library in templates, use the following line:
    
    <#import "/template/lib/Eurocarb.lib.ftl" as ecdb />

assuming that this file is located at "$WEBAPPLICATION_ROOT/template/lib". 

*Note* that this macro lib is included by default when using the standard 
Eurocarbdb header include.

*Note* also that when included in Eurocarbdb templates, you need the 'text' 
prefix in the macro, ie: a macro used here as '<@repeat ...' will be used in 
Eurocarbdb templates as '<@text.repeat ...'.

Authors:

mjh, ac, hirenj, mc

This page auto-generated from source with Perl:

    cat application/Eurocarbdb/src/template/lib/TextUtils.lib.ftl | perl -e 'BEGIN { undef $/ } $text = <>; while ($text =~ /<\#-+(.+?)-+>/gs) { print $1; }' > EurocarbLibFtl.wiki

== Macros ==
-->

<#import "/template/lib/TextUtils.lib.ftl" as text />

<#import "/template/lib/Eurocarb.js.lib.ftl" as ecdbjs />


<#-------------------------------------------------------------------
=== macro: actionlink ===

Main macro for creating links to other action urls.

NOTE: this macro works, but you can always get away with using 
'${base}/action_name.action' and '${base}/my_namespace/action_name.action' 
and it will resolve the namespaces correctly.

Args:
    
    # name    - the name of the action, *without* the '.action' at the end.
    # ns      - optional - the namespace of the desired action, with or without a leading '/'; 
              defaults to current namespace.
    # params  - optional - any/all CGI params to append to action URL.
    
Any number of additional arguments may be supplied - these are added into the URL 
without modification.    
    
Examples:
{{{
given  : '<@ecdb.actionlink name="xxx">example1</@>' 
returns: '<a href="xxx.action">example1</a>'

given  : '<@ecdb.actionlink name="xxx" ns="/" params="" >example2</@>'
returns: '<a href="/[base-url]/xxx.action">example2</a>'

given  : '<@ecdb.actionlink name="xxx" ns="gwb" params="" >example3</@>'
returns: '<a href="/[base-url]/gwb/xxx.action">example3</a>'

given  : '<@ecdb.actionlink name="xxx" ns="/gpf" params="" >example4</@>'
returns: '<a href="/[base-url]/gpf/xxx.action">example4</a>'

given  : '<@ecdb.actionlink name="xxx" ns="/gpf" params="abc=1;cde=2" >example5</@>'
returns: '<a href="/[base-url]/gpf/xxx.action?abc=1;cde=2">example5</a>'

given  : '<@ecdb.actionlink name="xxx" ns="/gpf" params="abc=1;cde=2" onclick="some_js" >example6</@>'
returns: '<a href="/[base-url]/gpf/xxx.action?abc=1;cde=2" onclick="some_js">example6</a>'
}}}
-->
<#macro actionlink name ns="" params="" map... ><#--

//  handle cgi params  
--><#if ( params?length > 0 && ! params?starts_with("?") )><#local params="?${params}" /></#if><#--

//  if any extra attributes given, concat them into string
--><#local extra_params ><#if ( map?exists && map?size > 0)><#list map?keys as k > ${k}="${map[k]}"</#list><#else></#if></#local><#--

//  if no namespace given, return url in current namespace
--><#if ns == "" ><#--
    --><a href="${name}.action${params}"${extra_params}><#nested></a><#--
--><#else><#--
//  else url is returned in requested namespace 
    --><#if ! ns?starts_with("/") ><#local ns = "/${ns}" /></#if><#--
    --><#if ! ns?ends_with("/")   ><#local ns = "${ns}/" /></#if><#-- 
    --><a href="${base}${ns}${name}.action${params}"${extra_params}><#nested></a><#--
--></#if></#macro>


<#-------------------------------------------------------------------
=== macro: context_box ===

Creates a context box widget. The HTML generated looks like this:
{{{
    <div id="your_optional_id" class="context_box">
        <h3>Title, if given</h3>
        [your content here]
    </div>
}}}
Usage:
{{{
    <@ecdb.context_box title="Page Title"> 
        [your content here]
    </@>
}}}    
Args:

    # id          - optional string - DOM id of the HTML div
    # title       - optional string - title text for this box
    # prepend     - optional boolean - if true, content is prepended instead of appended.
    
Note that this macro only adds a widget to the current widget buffer variable
'context_menu' -- the actual HTML is placed in pages with the macro 'context_menu_inline',
found below.
    
Author: mjh
-->
<#macro context_box id="" title="" prepend=false >
<!-- (added context box item '${title}') -->
<#if ! context_menu?exists >
<#assign context_menu="" />
</#if>
<#assign context_menu >
<#if ! prepend >${context_menu}</#if>

<div <#if (id?length > 0)>id="${id}" </#if>class="context_box">
<#if (title?length > 0) ><h3>${title}</h3></#if>
<div class="context_box_frame">
<#nested>
</div>
</div>

<#if prepend >${context_menu}</#if>
</#assign>
</#macro>


<#-- 
=== macro: context_menu_inline ===
Just prints the contents of ${context_menu}. Don't use. 
-->
<#macro context_menu_inline >
<!-- start context_menu -->
<#if context_menu?exists >
${context_menu}
</#if>
<!-- end context_menu -->
</#macro>


<#-- 
=== macro: datepicker ===

Renders a date picker widget.

Args:
    # label
    # name
    # value

(more info here pls)
    
-->
<#macro datepicker label name value >
<#assign date_val = ""/>
<#assign human_val = "Today. Click to change"/>
<#if value?exists && value?is_date >
<#assign date_val = value?string('yyyy/MM/dd')/>
<#assign human_val = value?date />
</#if>
<@ww.textfield label=label name=name id="date_picker_${name}" value="${date_val}" title="${human_val}"/>
<script type="text/javascript">
connect(ECDB,'onload', function() {
  new ECDB.DatePicker($('date_picker_${name}'));
});
</script>
</#macro>




<#--
=== macro: sugar_notation_widget ===

Adds a sugar notation widget box thingy as a context box 
(see context_box macro).

Usage:
{{{
    <@sugar_notation_widget />
}}}

-->
<#macro sugar_notation_widget >
<@context_box id="notation_box" title="Notation" prepend=true>
<div id="sugar_notation_widget">
  <div id="notation_cfg" onclick="bind(ECDB.SetRenderingType,this)('cfg')">
    <a href="#" title="Consortium for Functional Glycomics format">CFG</a>
  </div>
  <div id="notation_cfglink" onclick="bind(ECDB.SetRenderingType,this)('cfglink')">
    <a href="#" title="Consortium for Functional Glycomics format">CFGl</a>
  </div>
  <div id="notation_text" onclick="bind(ECDB.SetRenderingType,this)('text')">
    <a href="#" title="IUPAC text format">Text</a>
  </div>
  <div id="notation_uoxf" onclick="bind(ECDB.SetRenderingType,this)('uoxf')">
    <a href="#" title="University of Oxford format">UOXF</a>
  </div>
  <div id="notation_uoxfcol" onclick="bind(ECDB.SetRenderingType,this)('uoxfcol')">
    <a href="#" title="University of Oxford colour format">UOXFCOL</a>
  </div>
</div>
<#if (sugarImageNotation?exists)>
<script type="text/javascript">
$('notation_${sugarImageNotation}').className = 'selected';
</script>
</#if>
</@context_box>
</#macro>


<#-- 
=== macro: create_issue_link ===

Renders a link to auto-create a new issue on eurocarb googlecode issue tracker.

Args:
    # summary 
        optional. the 1-line summary/title of issue to create. 
        defaults to the default for the template selected.
    # text  
        optional. link text (ie: between the <a href >...</a>). 
        defaults to "submit a new bug/issue".
    # exception 
        optional. an exception, if any. if present, a stack trace will be included
    # issue_template 
        optional. the googlecode issue template to use. 
        defaults to "Defect report from user"
    # status 
        optional. the initial issue status, defaults to 'New'.
    # target
        optional. href target. defaults to "_blank".
    # *    
        any number of additional attributes can be given and these will be inlined
        into the link, eg: title="href title" onclick="..."
        
Any text between <@create_issue_link ...></@> tags will be included
in the body of the issue page. Make sure this text is not too long, 
no more than 1000 or so characters.

Examples:
    
    # using mainly defaults
{{{
    <@create_issue_link 
        text="report a scientific issue"
        issue_template="Scientific issue" 
        target="_blank" />       
}}}
    
    # creating a link in response to an exception
{{{
    <@create_issue_link 
        summary=exception.message 
        text="report this exception"
        exception=exception />
}}}

-->
<#macro create_issue_link 
        summary=""
        text="submit a new bug/issue"
        exception=""
        issue_template="Defect report from user"
        status="New"
        target="_blank"
        map...
>
<#local comment>
<#nested>



---context---<#--// mjh: disable fetching of serverInfo cause throws exception everytime if macro used late in response generation -->
action:<#attempt>${action.class.name}
<#recover>none
</#attempt>
url:${request.requestURI}
params:${request.queryString!""}
server:${request.serverName}<#--// attempt>/${request.session.servletContext.serverInfo}<#recover></#attempt-->
</#local>
<#-- 

// include stack trace if an exception present -->
<#if (exception?exists && exception != "")>
<#local ex=exception />
<#-- 

//  generate stack trace -->
<#local stack_trace>
<#list [1,2,3,4] as i >
${ex.toString()}
<#list ex.stackTrace as s >
${s.toString()}
<#if (s_index == 2)><#--// only show 3 lines of stack trace per exception -->
...
<#break>
</#if>
</#list>
<#if ex.cause?exists >cause:<#local ex=ex.cause /><#else><#break></#if>
</#list>
</#local>
<#-- 

//  add stack trace -->
<#local comment>
${comment}
---
${stack_trace}
</#local>
</#if><#--// end if exception -->
<#-- 

//  ensure URL is not too long, otherwise url gets rejected. -->
<#if (summary?exists && summary != "")>
    <#local new_issue>status=New&amp;summary=${summary?url}&amp;prompt_name=${issue_template?url}&amp;comment=${comment?url}</#local>
<#else>
    <#local new_issue>status=New&amp;prompt_name=${issue_template?url}&amp;comment=${comment?url}</#local>
</#if>
<#if (new_issue?length > 1020)>
    <#assign new_issue="${new_issue?substring(0,1020)}..."/>
</#if>
<#-- 

//  return final URL -->
<a href="http://code.google.com/p/eurocarb/issues/entry?${new_issue}" 
    target="${target}"<#if (map?exists && map?size > 0 )><#list map?keys as key > ${key}="${map[key]}"</#list></#if>
    >${text}</a>
</#macro>


<#--
=== macro: dialog_notification ===

Usage: 
{{{
    <@dialog_notification elementName modal/> 
}}}

Creates a notification dialog containing the specified element by elementName.
You can create a modal dialog box by setting "modal" to true.

Note that for now, all applets are hidden so that they don't block the dialog
box.  I've not tested this on all browsers.

This is based on the JQuery-UI example code.

-->

<#macro dialog_notification elementName="" modal="true" width="600">
<script type="text/javascript">
jQuery(function(){			
  jQuery('#${elementName}').dialog({
    autoOpen: false,
    modal: ${modal},
    width: ${width},
    buttons: {
	    "Ok": function() { 
		    jQuery(this).dialog("close"); 
		    var applets=document.getElementsByTagName("applet");
		    for(var i=0;i<applets.length;i++){
		      applets[i].style.visibility="visible";
		    }
	    }
    }
  });
});


onLoadFunctions.push(function (){
  var applets=document.getElementsByTagName("applet");
  for(var i=0;i<applets.length;i++){
    applets[i].style.visibility="hidden";
  }
  jQuery('#${elementName}').dialog('open');
});
</script>

</#macro>

<#-------------------------------------------------------------------
=== macro: header ===

Usage:

    <@eurocarb.header title="Page Title" /> 

Args:

    # title       - HTML page title

Produces HTML header text. 

-->
<#macro header title="Untitled" >
<#include "/template/common/header.ftl" />
</#macro>


<#-------------------------------------------------------------------
=== macro: footer ===

Usage:
{{{
    <@eurocarb.footer/> 
}}}
Args:

    # title       - HTML page title

Produces HTML footer text. 

-->
<#macro footer>
<#include "/template/common/footer.ftl" />
</#macro>

<#-------------------------------------------------------------------
=== macro: detail_url ===

Usage:
{{{
    <@eurocarb.detail_url object=some_object /> 
}}}    
Args:
    # object      - An object that implements the EurocarbObject interface.

Creates a URL for the detail page of the passed object using heuristics 
and the EurocarbObject java interface.
Arbitrary additional attributes (target="..." onclick="...") are added to 
the rendered as given.    
    
-->
<#macro detail_url object >show_${ object.type }.action?${ object.identifierClass.simpleName?uncap_first }Id=${ object.id?c }</#macro>


<#-------------------------------------------------------------------
=== macro: detail_link ===

Usage:
{{{
    <@eurocarb.detail_link object=some_object /> 
    <@eurocarb.detail_link object=some_object>${text}</@eurocarb.detail_link>
}}}    
Args:
    # object      - An object that implements the EurocarbObject interface.
    # text        - The displayed text for the link. Default = object.getName().

Produces a link to a "show detail" page for the given object

Any other attributes (target="..." onclick="...") are rendered as given.    
    
-->
<#macro detail_link object text="" map... ><#if text == ""><#assign text><#nested/></#assign></#if><#if text == ""><#assign text="${ object.toString() }"/></#if><#if text == ""><#assign text="${ object.type }"/></#if>
<a href="<@detail_url object=object /><#--show_${ object.type }.action?${ object.identifierClass.simpleName?uncap_first }Id=${ object.id?c }-->"<#if (map?exists && map?size > 0 )><#list map?keys as key > ${key}="${map[key]}"</#list></#if>>${text}</a></#macro>



<#if ! css_includes?exists >
  <#assign css_includes=[] />
</#if>

<#macro include_css url>
  <#if ! css_includes?seq_contains(url) >
  <#assign css_includes = css_includes + [url] />
  </#if>
</#macro>

<#if ! js_includes?exists >
  <#assign js_includes=[] />
</#if>

<#macro include_js url >
  <#if ! js_includes?seq_contains(url) >
    <#assign js_includes = js_includes + [url] />
  </#if>
</#macro>

<#--
=== macro: use_js_lib ===

Usage: 
{{{
    <@use_js_lib name=[Library name] /> 
}}}

Macro to include ECDB libraries. All dependencies for the libraries
are globally controlled in the template that defines the ecdbjs macro 
(see: Eurocarb.js.lib.ftl)

Any libraries with names prefixed with XYZ:: or something similar, are 
wholly external libraries, and do not include any local libraries.

Any CSS and JS files associated with the libraries are retrieved.

-->

<#macro use_js_lib name="" >
  <@ww.url value="/js/lib/${name}.js" includeParams="none" id="url_to_add"/>
  <@ecdbjs.dependencies_for name="${name}"/>
  <#if ! name?contains(':') >
    <@ecdb.include_js url=url_to_add />
  </#if>
</#macro>


<#macro breadcrumb list current >
<div id="breadcrumbs">
<#list list as item >
<#if (item == current) > -&gt; <em>${item}</em>
<#else> -&gt; ${item} </#if>
</#list>
</div>
</#macro>


<#--
=== macro: contributor ===

Usage:
{{{
    <@contributor c=[Contributor object] /> 
    <@contributor c=[Contributor object] text="[some custom text here]" /> 
}}}

Renders contributor detail text with hyperlink.

-->
<#macro contributor c text=c.contributorName ><#local obftext><@obfuscate text=text /></#local><@detail_link object=c text=obftext /></#macro>

<#macro obfuscate text>
<#if ((text?index_of("@"))>0) >
${text?substring(0,text?index_of("@")+4)}..
<#else>
${text}
</#if>
</#macro>


<#--
=== macro: taxonomy ===

Usage: 
{{{    
    <@taxonomy t=[Taxonomy object] />
    <@taxonomy t=[Taxonomy object] text="[some custom text here]" />
}}}

Displays taxonomy detail text with hyperlinks.

-->
<#macro taxonomy t text=t.taxon><#if ( t.isRoot() ) >unknown/unspecified<#else><#local tooltip><#if t.taxonomySynonyms?size != 0><#list t.synonyms as s><#if s_index != 0>, </#if>${s}</#list><#else>${text}</#if></#local><@detail_link object=t text=text title="${tooltip}" /></#if></#macro>

<#macro humanised_taxonomy t><#if ( t.isRoot() ) >unknown/unspecified<#else>${t.taxon} <#if t.taxonomySynonyms?size != 0><i>(<@text.join list=t.synonyms />)</i></#if></#if></#macro>


<#--
=== macro: tissue ===

Usage: 
{{{
    <@tissue t=[TissueTaxonomy object] />
}}}

Display tissue detail text with hyperlinks

-->
<#macro tissue t text=t.tissueTaxon show_unknown=true><#if ( t.isRoot() && show_unknown ) >unknown/unspecified<#else><@detail_link object=t text=text /></#if></#macro>


<#--
=== macro: disease ===

Usage:
{{{
    <@disease d=[Disease object] />
}}}

Display disesase detail text with hyperlinks.

-->
<#macro disease d ><@detail_link object=d text="${d.diseaseName}" /></#macro>

<#--
=== macro: perturbation ===

Usage: 
{{{
    <@perturbation p=[Perturbation object] />
}}}

Display disease detail text with hyperlinks

-->
<#macro perturbation p ><@detail_link object=p text="${p.perturbationName}" /></#macro>


<#--
=== macro: perturbation ===

Usage: 
{{{
    <@perturbation p=[Perturbation object] />
}}}

Display disease detail text with hyperlinks

-->
<#macro short_biological_context context >
<#if context?exists>
  <#include "/template/action/core/show_biological_context/short_context.ftl"/>
</#if>
</#macro>


<#--
=== macro: short_glycan_sequence_context ===

Usage: 
{{{
    <@short_glycan_sequence_context context=[GlycanSequenceContext object] />
}}}

Renders a brief text description of a GlycanSequenceContext, with links.
-->
<#macro short_glycan_sequence_context context >
<#if context?exists>
  <#include "/template/action/core/show_glycan/short_context.ftl"/>
</#if>
</#macro>

<#--
=== macro: glycan_sequence_context ===

Usage: 
{{{
    <@glycan_sequence_context context=[GlycanSequenceContext object] />
}}}

Renders a brief text description of a GlycanSequenceContext, with links.
-->
<#macro glycan_sequence_context context >
<#if context?exists>
  <#include "/template/action/core/show_glycan/context_summary.ftl"/>
</#if>
</#macro>


<#--
=== macro: short_glycan_sequence_context_contribute ===

Usage: 
{{{
    <@ short_glycan_sequence_context_contribute context=[GlycanSequenceContext object] />
}}}

Renders a brief text description of a GlycanSequenceContext, with links.
-->
<#macro  short_glycan_sequence_context_contribute context >
<#if context?exists>
  <#include "/template/action/core/show_glycan/short_context_contribute.ftl"/>
</#if>
</#macro>


<#--
=== macro: scan_detail_brief ===

Usage: 
{{{
    <@scan_detail_brief scan=[Scan object] />
}}}

Renders a brief text description of a Scan, with links.

-->
<#macro scan_detail_brief scan >
<#if scan?exists>
  <#include "/template/action/ms/show_scan/short_scan.ftl"/>
</#if>
</#macro>


<#--
=== macro: biological_context ===

Usage: 
{{{
    <@biological_context bc=[BiologicalContext object] />
    <@biological_context bc=[BiologicalContext object] separator=[String] />
}}}
    
Display a detail link for a biological context with embedded links 
for its component Taxonomy and TissueTaxonomy, as well as any other
associated info. By default, this text spans multiple lines, unless
an alternate 'separator' argument is given.

-->
<#macro biological_context bc separator=";<br/>" >
    <#-- //  taxonomy -->
    <em>${bc.taxonomy.rank}</em>: 
    <@taxonomy t=bc.taxonomy />${separator}

    <#-- //  tissue  -->
    <em>tissue</em>: 
    <#if ( bc.tissueTaxonomy.isRoot() ) >
        unknown/unspecified${separator}
    <#else/>
        <@tissue t=bc.tissueTaxonomy />${separator}
    </#if>

    <#-- //  list any diseases: -->
    <#if (bc.diseases?exists && bc.diseases?size>0) >
    <em>diseases</em>: <#list bc.diseases as bcd><@disease d=bcd/> </#list>${separator}
    </#if>

    <#-- //  list any perturbations:  -->
    <#if (bc.perturbations?exists && bc.perturbations?size>0) >
    <em>perturbations</em>: <#list bc.perturbations as bcp><@perturbation p=bcp/> </#list>${separator}
   </#if>
  (<@detail_link object=bc text="detail" />)
</#macro>


<#--
=== macro: bc_disease_names ===

Usage: 
{{{
    <@bc_disease_names bc=[BiologicalContext object] />
}}}
    
Renders a list of disease detail links for the Diseases of 
a given BiologicalContext.

-->
<#macro bc_disease_names bc>
<#if (bc.diseases?exists && bc.diseases?size>0) >
<#list bc.diseases as bcd><@disease d=bcd/> </#list>
</#if>
</#macro>


<#--
=== macro: bc_perturbations ===

Usage: 
{{{
    <@bc_perturbations bc=[BiologicalContext object] />
}}}
    
Renders a list of disease detail links for the Perturbations of 
a given BiologicalContext.

-->
<#macro bc_perturbations bc>
<#if (bc.diseases?exists && bc.diseases?size>0) >
<#list bc.perturbations as bcp><@perturbation p=bcp/> </#list>
</#if>
</#macro>


<#--
=== macro: biological_context ===

Usage: 
{{{
    <@biological_context bc=[BiologicalContext object] />
    <@biological_context bc=[BiologicalContext object] separator=[String] />
}}}
    
Display a brief description of a biological context with embedded links 
for its component Taxonomy and TissueTaxonomy, as well as any other
associated info. Single line by default.

-->
<#macro biological_context_brief bc separator=" "><#t>
<#--

//  taxonomy 
 --><#if ( ! bc.taxonomy.isRoot() ) ><#if (bc.taxonomy.hasValidRank())><em>${bc.taxonomy.rank}</em>:&nbsp;</#if><@taxonomy t=bc.taxonomy />${separator}</#if><#--

//  tissue
 --><#if ( ! bc.tissueTaxonomy.isRoot() ) ><em>tissue</em>:&nbsp;<@tissue t=bc.tissueTaxonomy />${separator}</#if><#--
  
//  list any diseases:
 --><#if (bc.diseases?exists && bc.diseases?size>0) >
    <em>diseases</em>::&nbsp;<#list bc.diseases as bcd><#if (bcd_index > 0)>,:&nbsp;</#if><@disease d=bcd/></#list>${separator}
    </#if><#--
  
//  list any perturbations:  
 --><#if (bc.perturbations?exists && bc.perturbations?size>0) >
    <em>perturbations</em>::&nbsp;<#list bc.perturbations as bcp><#if (bcp_index > 0)>,:&nbsp;</#if><@perturbation p=bcp/></#list>${separator}
   </#if>
</#macro>


<#--
=== macro: sugar_image ===

Usage: 
{{{
    <@sugar_image id=[GlycanSequence id] scale=[scale factor, default=1] output=[image type, default='png']/>
}}}

Renders a sugar image (ie: <img src="..." />) for the given GlycanSequence id.
'scale' argument should be between 0-1.0 inclusive.

Only the 'id' argument is required.

-->
<#macro sugar_image id scale=1 output="png">
<#if (id>0)>
<img id="glycanSequence_${id?c}" class="sugar_image" src="get_sugar_image.action?download=true&scale=${scale}&outputType=${output}&tolerateUnknown=1&glycanSequenceId=${id?c}"/>
<#else>
<img id="glycanSequence_${id?c}" class="sugar_image" src="/eurocarb/images/holding_image.png" width="50" height="50"/>
</#if>
</#macro>

<#--
=== macro: sugar_image_for_seq_from_search ===

Usage: 
{{{
    <@sugar_image seq=[a glycan sequence string] format=[seq format, default='gws'] scale=[scale factor, default=1] output=[image type, default='png']/>
}}}
We have a problem with some sugar images not rendering from within the database it's self.
Currently this can only be detected when a glycan sequence is missing the internal identifier.
Therefore we forced an ID to always be required - otherwise a question mark would be shown.
However this breaks any code which doesn't have an ID available - searching for a substructure
is the main example of this.

Renders a sugar image (ie: <img src="..." />) for the given sequence and format.
'scale' argument should be between 0-1.0 inclusive.

only the 'seq' argument is required. Note that you will probably have to URL-escape 
the sequence, ie: your_seq?url

-->
<#macro sugar_image_for_seq_from_search seq format="gws" output="png" scale=1 map...><img class="sugar_image" id="glycanSequence_${id?c}" src="get_sugar_image.action?download=true&scale=${scale}&outputType=${output}&inputType=${format}&tolerateUnknown=1&sequences=${seq}" <#if map?exists><#list map?keys as k>${k}="${map[k]}" </#list></#if>/></#macro>


<#--
=== macro: sugar_image_for_seq ===

Usage: 
{{{
    <@sugar_image seq=[a glycan sequence string] format=[seq format, default='gws'] scale=[scale factor, default=1] output=[image type, default='png']/>
}}}

Renders a sugar image (ie: <img src="..." />) for the given sequence and format.
'scale' argument should be between 0-1.0 inclusive.

only the 'seq' argument is required. Note that you will probably have to URL-escape 
the sequence, ie: your_seq?url

-->
<#macro sugar_image_for_seq seq id format="gws" output="png" scale=1 map...><img class="sugar_image" id="glycanSequence_${id?c}" src="get_sugar_image.action?download=true&scale=${scale}&outputType=${output}&inputType=${format}&tolerateUnknown=1&sequences=${seq}&glycanSequenceId=${id?c}" <#if map?exists><#list map?keys as k>${k}="${map[k]}" </#list></#if>/></#macro>

<#--
=== macro: linked_sugar_image ===

Usage: 
{{{
    <@linked_sugar_image id=[GlycanSequence id] scale=[scale factor]/>
}}}

Renders a hyperlinked sugar image (ie: <a href="..."><img src="..." /></a>)
for the given GlycanSequence id. Provide both an id and a seq, if the seq is empty then an image
with a question mark will be used instead.
'scale' argument should be between 0-1.0 inclusive.
-->
<#-- <#macro linked_sugar_image id seq="" scale=1><a href="show_glycan.action?glycanSequenceId=${id?c}" title="Sequence ID ${id?c} - click for full detail" class="sugar_image"><#if (seq?exists && seq?length > 0)><@sugar_image_for_seq id=id seq=seq scale=scale /><#else><@sugar_image id=-2 scale=scale /></#if></a></#macro> -->
<#macro linked_sugar_image_seq_only id seq="" scale=1><a href="show_glycan.action?glycanSequenceId=${id?c}" title="Sequence ID ${id?c} - click for full detail" class="sugar_image"><#if (seq?exists && seq?length > 0)><@sugar_image_for_seq id=id seq=seq scale=scale /><#else><@sugar_image id=-2 scale=scale /></#if></a></#macro>


<#--
=== macro: linked_sugar_image ===

Usage: 
{{{
    <@linked_sugar_image id=[GlycanSequence id] scale=[scale factor]/>
}}}

Renders a hyperlinked sugar image (ie: <a href="..."><img src="..." /></a>)
for the given GlycanSequence id.
'scale' argument should be between 0-1.0 inclusive.
-->
<#-- <#macro linked_sugar_image id seq="" scale=1><a href="show_glycan.action?glycanSequenceId=${id?c}" title="Sequence ID ${id?c} - click for full detail" class="sugar_image"><#if (seq?exists && seq?length > 0)><@sugar_image_for_seq id=id seq=seq scale=scale /><#else><@sugar_image id=-2 scale=scale /></#if></a></#macro> -->
<#macro linked_sugar_image id seq="" scale=1><a href="show_glycan.action?glycanSequenceId=${id?c}" title="Sequence ID ${id?c} - click for full detail" class="sugar_image"><@sugar_image_for_seq id=id seq=seq scale=scale /></a></#macro>






<#--
=== macro: guided_sugar_image ===

Usage: 
{{{
    <@guided_sugar_image id=[GlycanSequence id] scale=[scale factor]/>
}}}

Renders a hyperlinked sugar image (ie: <a href="..."><img src="..." /></a>)
for the given GlycanSequence id.
'scale' argument should be between 0-1.0 inclusive.
-->
<#macro guided_sugar_image id seq="" scale=1><#if (seq?exists && seq?length > 0)><@sugar_image_for_seq seq=seq id=id scale=scale /><#else><@sugar_image id=-2 scale=scale /></#if></#macro>


<#--
=== macro: guided_sugar_image_with_id ===

Usage: 
{{{
    <@guided_sugar_image_with_id id=[GlycanSequence id] scale=[scale factor]/>
}}}

Renders a hyperlinked sugar image (ie: <a href="..."><img src="..." /></a>)
for the given GlycanSequence id.
'scale' argument should be between 0-1.0 inclusive.
-->
<#macro guided_sugar_image_with_id id seq="" scale=1><#if (seq?exists && seq?length > 0)><@sugar_image_for_seq seq=seq scale=scale /><#else><@sugar_image id=-2 scale=scale /></#if></#macro>


<#--
=== macro: experiment ===

Usage: 
{{{
    <@experiment e=[Experiment] />
}}}

Renders a experiment detail hyperlink.
-->
<#macro experiment e ><@detail_link object=e text=e.experimentName /></#macro>


<#--
=== macro: evidence ===

Usage: 
{{{
    <@evidence ev=[Evidence] />
}}}

Renders an evidence detail hyperlink.
-->
<#macro evidence ev ><@detail_link object=ev text=ev.evidenceType title=ev.technique.techniqueName /></#macro>


<#--
=== macro: experiment_step ===

Usage: 
{{{
    <@experiment_step object=[ExperimentStep] />
}}}

Renders a experiment step detail hyperlink
-->
<#macro experiment_step step ><@detail_link object=step text=step.technique.techniqueName /><#if (step.evidence?exists) ><@detail_link object=step.evidence text=step.evidence.evidenceType /></#if></#macro>

<#--
=== macro: reference ===

Usage: 
{{{
    <@reference ref=[Reference object] />
}}}

displays summary information for a reference
-->
<#macro reference ref>
<#if (ref.referenceType?exists && ref.referenceType=="journal")>
      ${ref.authorListAsCitationString},
      <#if ref.title?exists>
      <b>${ref.title}</b>,
      </#if>      
      <i>${ref.journalAsCitationString}</i>
      (<@detail_link object=ref text="detail" />, <a href="${ref.url}">${ref.externalReferenceName}<#if (ref.pubmedId?exists && ref.pubmedId > 0)> id ${ref.pubmedId}</#if></a>)
<#elseif (ref.referenceType?exists && ref.referenceType=="database")>
      ${ref.externalReferenceName} entry 
	<#if ref.url??>	
	<a href="${ref.url}">${ref.externalReferenceId}</a>
	<#else>
	${ref.externalReferenceId}
        </#if>
<#elseif (ref.referenceType?exists && ref.referenceType=="website")>  
	<#if ref.url??>
	<a href="${ref.url}">${ref.externalReferenceId}</a>
	<#else>
	${ref.externalReferenceId}
	</#if>
</#if>
<#t>
    <#--//
    <#if (ref.url?exists && ref.url?length>0) >
      (<a href="${ref.url}">Visit the website.</a>)
    </#if>
    <#if (ref.referenceComments?exists && ref.referenceComments?length>0) >
    <br><i>${ref.referenceComments}</i>
    </#if>
    -->
</#macro>


<#--
=== macro: reference_brief ===

Usage: 
{{{
    <@reference_brief object=[Reference] />
}}}

Renders a brief reference link
-->
<#macro reference_brief object text=object.toString() ><#t>
<#if (object.referenceType.toString() == "journal") >
<@detail_link object=object text=object.citationString />
<#elseif (object.referenceType.toString() == "database") >
<#--<@detail_link object=object text="${object.contributor.contributorName} reference id ${object.externalReferenceId}" />-->
${object.externalReferenceName} entry 
<#if object.url??>	
	<a href="${object.url}">${object.externalReferenceId}</a>
	<#else>
	${object.externalReferenceId}
        </#if>
<#elseif (object.referenceType?exists && object.referenceType=="website")>  
	<#if object.url??>
	<a href="${object.url}">${object.externalReferenceId}</a>
	<#else>
	${object.externalReferenceId}
	</#if>
<#elseif (object.referenceType.toString() == "website") >
<a href="${object.url}">${object.url}</a>
<#else>
<@detail_link object=object text=text />
</#if>
</#macro>

<#--
=== macro: addGu ===

Usage: 
{{{
    <@addGu ref=[Reference object] />
}}}

display a summary information for a reference for adding gu data.
-->
<#macro addGu ref hplc>
<#if ref.referenceType=="journal" && ref.pubmedId?exists>
      
      <a href="create_gu.action?glycanHplcId=${hplc.glycanId}&amp;pubmedId=${ref.pubmedId?c}&amp;glycanSequenceId=${glycanSequenceId}&amp;glycanReferenceId=${ref.referenceId?c}">${ref.externalReferenceName} Id: ${ref.pubmedId?c}</a>:  
      ${ref.authorListAsCitationString},
      <#if ref.title?exists>
      <b>${ref.title}</b>,
      </#if>  
      
      
      <i>${ref.journalAsCitationString}</i>
<#elseif ref.referenceType=="database">
      <a href="${ref.url}">${ref.externalReferenceName} id ${ref.externalReferenceId}</a>
<#elseif ref.referenceType=="website">  
      <a href="${ref.url}">${ref.url}</a>
</#if>
</#macro>


<#--
=== macro: page_navigator ===

Usage: 
{{{
    <@page_navigator action_name=[string] />
}}}

Display a simple page, with links to change page in a pageable action.

-->
<#macro page_navigator action_name><#attempt>
<#if ( resultsPerPage <= 0) ><#return></#if>
<#if ( totalResults <= 0 || lastPage == 1)><#return></#if>
<!-- page navigator -->
<div>
<ul class="pagenumbers hmenu"> 
    <#if ( page < 2 )>
        <#assign start = 1 />
        <#assign end = page+2 />
    <#elseif ( page > lastPage - 2 )>
        <#assign start = page-2 />
        <#assign end = page + 1 />
    <#else>
        <#assign start = page-2 />
        <#assign end = page+2 />
    </#if>
    <#if ( start < 1 )>
        <#assign start = 1 />
    </#if>
    <#if ( end > lastPage )>
        <#assign end = lastPage />
    </#if>
    <#if ( start > 1 )>   
    <li>
        <a href="${action_name}page=${1}&resultsPerPage=${resultsPerPage?c}&indexedBy=${index.name}" title="go to first page">first</a>
        &lt;&lt;
    </li>
    </#if>
    <#if ( page > 15 )>   
    <li>
        <a href="${action_name}page=${(page-10)?c}&resultsPerPage=${resultsPerPage?c}&indexedBy=${index.name}" title="go to page ${page-10}">${page - 10}</a>
        &lt;
    </li>
    </#if>
        
    <#list start .. end as p>	
    <li>
    <#if (p == page)>
        <strong><em><u>${p}</u></em></strong>
    <#else>
        <a href="${action_name}page=${p?c}&resultsPerPage=${resultsPerPage?c}&indexedBy=${index.name}" title="go to page ${p}">${p}</a>
    </#if>
    </li>        	
    </#list>

    <#if ( (page + 15) < lastPage )>   
    <li>
        &gt;
        <a href="${action_name}page=${(page+10)?c}&resultsPerPage=${resultsPerPage?c}&indexedBy=${index.name}" title="go to page ${page + 10}">${page + 10}</a>
    </li>
    </#if>
    
    <#if (end < action.lastPage) >   
    <li>
        &gt;&gt;
        <a href="${action_name}page=${action.lastPage?c}&resultsPerPage=${resultsPerPage?c}&indexedBy=${index.name}" title="go to page ${action.lastPage}">last</a>
    </li>
    </#if>
</ul>
</div>
<#recover>
<!-- page navigation couldnt be rendered -->
</#attempt>
</#macro>


<#-- 
=== macro: jit_json_tree ===

Usage:
{{{
    <@jit_json_tree graph=[A EurocarbDB Graph object] />
    <@jit_json_tree graph=[A EurocarbDB Graph object] node=graph.rootVertex />
}}}

Turns a Eurocarb Graph into a JSON tree suitable for use with the JIT javascript
visualisations library.
-->
<#macro jit_json_tree graph node=graph.rootVertex >{ id:"node_${node.value.id?c}",name:"${node.value.name}",data:{ id:${node.value.id?c},type:"${node.value.type}",detail:"<@detail_url object=node.value />" },children:[<#list node.getOutgoingEdges() as e><#if e_index != 0>,</#if><@jit_json_tree graph=graph node=e.child/></#list>]} <#--if node=graph.rootVertex>]</#if--></#macro>


<#--
=== macro: jit_spacetree_widget ===

Usage:
{{{
    <@jit_json_tree graph=[A EurocarbDB Graph object] />
    <@jit_json_tree graph=[A EurocarbDB Graph object] id=[JS variable name for tree] />
    <@jit_json_tree graph=[A EurocarbDB Graph object] json=[some (unquoted) JIT tree JSON] />
    <@jit_json_tree graph=[A EurocarbDB Graph object] div=[name of HTML div which will receive tree widget] />
}}}

Turns a Eurocarb Graph into a static JIT SpaceTree widget, see:
-->
<#macro jit_spacetree_widget graph id="st" json="" div="div_for_${id}">

</#macro>


