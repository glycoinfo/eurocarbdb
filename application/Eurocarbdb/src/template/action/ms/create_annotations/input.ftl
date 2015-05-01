<#assign title>Annotation Parameters Page</#assign>
<#setting url_escaping_charset='ISO-8859-1'>
<#include "/template/common/header.ftl" />

<h1>Annotation Settings Page</h1>

<@ww.form id="create_annotation_form" class="create_annotation_form">

<@ww.hidden name="scan.scanId"/>
<@ww.hidden name="glycanSequence.glycanSequenceId"/>

<!-- Fragment options: -->

<h3>Select type of derivatizations</h3>

    <select name="massOptions.derivatization" size="1">	
	<#list persubstitutions as p>
	  <#if p.abbreviation==massOptions.derivatization>
	  <option value="${p.abbreviation}" selected="true">${p.name}</option> 
	  <#else/>
	  <option value="${p.abbreviation}">${p.name}</option> 
	  </#if>
	</#list>
      </select>

      <select name="massOptions.reducingEndTypeString" size="1">	
	<#list reducingEnds as r>
	  <#if r.abbreviation==massOptions.reducingEndTypeString>
	  <option value="${r.abbreviation}" selected="true">${r.name}</option> 
	  <#else/>
	  <option value="${r.abbreviation}">${r.name}</option> 
	  </#if>
	</#list>
      </select>

<h3>Select fragments to be calculated</h3>
<div class="inline_inputs">
<@ww.checkbox label="B fragments" name="fragmentOptions.addBFragments" fieldValue="true" value="fragmentOptions.addBFragments"/>
<@ww.checkbox label="Y fragment" name="fragmentOptions.addYFragments" fieldValue="true" value="fragmentOptions.addYFragments"/>
<@ww.checkbox label="C fragment" name="fragmentOptions.addCFragments" fieldValue="true" value="fragmentOptions.addCFragments"/>
<@ww.checkbox label="Z fragment" name="fragmentOptions.addZFragments" fieldValue="true" value="fragmentOptions.addZFragments"/>
<@ww.checkbox label="A fragment" name="fragmentOptions.addAFragments" fieldValue="true" value="fragmentOptions.addAFragments"/>
<@ww.checkbox label="X fragment" name="fragmentOptions.addXFragments" fieldValue="true" value="fragmentOptions.addXFragments"/>
<div style="height:0px; float: none; clear: both;"></div>
</div>
<h3>Select maximum number of fragmentations</h3>
<div class="inline_inputs">
    <@ww.select label="Max no. of cleavages" name="fragmentOptions.maxNoCleavages" list="{1,2,3,4,5}" />
    <@ww.select label="Max no. of cross rings" name="fragmentOptions.maxNoCrossrings" list="{1,2,3,4,5}" />
    <div style="height:0px; float: none; clear: both;"></div>
</div>  
<h3>Select polarity and charged ions</h3>

    <@ww.checkbox label="Positive mode" name="annotationOptions.positiveMode" fieldValue="true" value="annotationOptions.positiveMode" />
<style type="text/css">
#wwgrp_create_annotation_form_annotationOptions_maxNoCharges {
  clear: right;
  margin-right: 50%;
}
</style>
<div class="inline_inputs">
    <@ww.select label="Max No. of charges" name="annotationOptions.maxNoCharges" list="{1,2,3,4,5,6,7,8,9,10}"/>
    <@ww.select label="Max No. of H+" name="annotationOptions.maxNoHIons" list="{0,1,2,3,4,5,6,7,8,9,10}" />
    <@ww.select label="Max No. of Na+" name="annotationOptions.maxNoNaIons" list="{0,1,2,3,4,5,6,7,8,9,10}" />
    <@ww.select label="Max No. of K+" name="annotationOptions.maxNoKIons" list="{0,1,2,3,4,5,6,7,8,9,10}" />
    <@ww.select label="Max No. of Li+" name="annotationOptions.maxNoLiIons" list="{0,1,2,3,4,5,6,7,8,9,10}" />
<div style="height:0px; float: none; clear: both;"></div>
</div>  
  
<@ww.checkbox label="Neutral exchanges" name="annotationOptions.computeExchanges" fieldValue="true" value="annotationOptions.computeExchanges" />
<div class="inline_inputs">
<@ww.select label="Max No. of ex Na+" name="annotationOptions.maxExNaIons" list="{0,1,2,3,4,5,6,7,8,9,10}" />
<@ww.select label="Max No. of ex K+" name="annotationOptions.maxExKIons" list="{0,1,2,3,4,5,6,7,8,9,10}" />
<@ww.select label="Max No. of ex Li+" name="annotationOptions.maxExLiIons" list="{0,1,2,3,4,5,6,7,8,9,10}" />
<div style="height:0px; float: none; clear: both;"></div>
</div>
<h3>Specify mass accuracy</h3>
<div class="inline_inputs">
<@ww.textfield label="Accuracy of mass" name="annotationOptions.massAccuracy" /><@ww.select label="Mass accuracy unit" name="annotationOptions.massAccuracyUnit" list="{'Da','ppm'}" />
<div style="height:0px; float: none; clear: both;"></div>
</div>
<h3>Specify quality of the annotation</h3>
<@ww.select label="Quality of the Annotation" name="contributorQuality" list="{0,1,2,3,4,5,6,7,8,9,10}" />
<@ww.submit value="Annotate" name="submitAction" />

</@ww.form>

<#include "/template/common/footer.ftl" />