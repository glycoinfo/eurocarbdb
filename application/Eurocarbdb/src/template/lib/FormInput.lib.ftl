<#--  
= FormInput.lib.ftl =

== Description ==

A library of generic Freemarker text macros for generating form HTML 
for the Eurocarb project.

== Usage ==

To use this library in templates, use the following line:
    
    <#import "/template/lib/FormInput.lib.ftl" as input />

...assuming that this file is located at "$WEBAPPLICATION_ROOT/template/lib". 

== Author ==

mjh <matt@ebi.ac.uk>

== Macros ==
-------------------------------------------------------------------->

<#assign form_name = "" />

<#-------------------------------------------------------------------
=== form ===

Defines a HTML <form>...</form>

Usage:

    <@input.form> ... </@input.form> 
    <@input.form action="my_action" > ... </@input.form> 
    <@input.form action="my_action" name="my_form" > ... </@input.form> 
    <@input.form action="my_action" name="my_form" id="my_form_id" > ... </@input.form>

Args:

    action      - Target of form. (optional, default = "")
    name        - Name of the form (optional, default = "default")
    id          - Identifier of the form element (optional). Defaults to
                  "form_${name}"
    method      - "GET" or "POST". Default is GET.

Author: mjh
-------------------------------------------------------------------->
<#macro form action="" name="default" id="form_${name}"?replace("-", "_") map... >

<!-- start of form '${name}', action ${action} -->
<form action="${action}.action" id="${id}" name="${name}" <#if (map?exists && map?size > 0 )><#list map?keys as key > ${key}="${map[key]}" </#list></#if>>
<#assign form_name = name?replace("-", "_") />
<#nested/>
</form>
<!-- end of form ${name} -->
<#assign form_name = "" />

</#macro>


<#-------------------------------------------------------------------
=== text ===

Produces HTML for a form input type="text" tag. 

Usage:

    <@input.text name="my_input_text_name" />
    <@input.text name="my_input_text_name" >${value}</@input.text>

Args:

    name        - Name of the input text field.
    id          - Id of the text element (optional).
                  Defaults to "form_<form-name>_text_<name>"
    value       - Initial value of the text input field (optional).

Author: mjh
-------------------------------------------------------------------->
<#macro text name id="form_${form_name}_text_${name}"?replace("-", "_") label="">
<#if (label != "")>
<label for="${name}">${label}</label>
<input type="text" id="${id}" name="${name}" value="<#nested>" />
<#else/>
<input type="text" id="${id}" name="${name}" value="<#nested>" />
</#if>
</#macro>



<#-------------------------------------------------------------------
=== radio ===

Produces HTML for a form input type="radio" tag. 

Usage:

    <@input.radio name="my_input_radio_name" />
    <@input.radio name="my_input_radio_name" >${value}</@input.radio>
    <@input.radio name="my_input_radio_name" default=1 >${value}</@input.radio>

Args:

    name        - Name of the input field.
    id          - Id of the input element (optional).
                  Defaults to "form_<form-name>_radio_<name>"
    value       - Initial value of the input field (optional).
    default     - Indicates that this radio element is initially selected.

Author: mjh
-------------------------------------------------------------------->
<#macro radio name id="form_${form_name}_radio_${name}"?replace("-", "_") default=0 >
<#if (default == 0)>
<input type="radio" id="${id}" name="${name}" value="<#nested>" />
<#else />
<input type="radio" id="${id}" name="${name}" value="<#nested>" selected="selected" />
</#if>
</#macro>



<#-------------------------------------------------------------------
=== submit ===

Produces HTML for a form input type="submit" tag. 

Usage:

    <@input.submit />
    <@input.submit label="Go!" />

Args:

    label       -   Label for the submit button (optional). 
                    Default = "Continue ->")
    id          -   Id of the input element (optional).
                    Default = "form_<form-name>_submit"

Author: mjh
-------------------------------------------------------------------->
<#macro submit label="Continue ->" name="_" id="form_${form_name}_submit"  map... >
<input type="submit" id="${id}" name="${name}" value="${label}" <#if (map?exists && map?size > 0 )><#list map?keys as key > ${key}="${map[key]}" </#list></#if>/>
</#macro>


























