<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<#assign title>Upload GW File</#assign>
<#include "/template/common/header.ftl" />
<#if gwFile?exists>
<h1> ${gwFileFileName}</h1>
</#if>
<#if acquisitionId != -1>

<h1>Upload GWB File for AcquisitionId ${acquisitionId}</h1>
<@ww.form method="post" enctype="multipart/form-data">
<@ww.hidden name="acquisitionId"/>
<@ww.file label="GW File" name="gFile"/>
<!--
<@ww.select label="Peaklist Quality(*1:Poor-5:Excellent)" list="{1, 2, 3, 4, 5}" name="peakListContributorQuality" />  
<@ww.select label="Annotation Quality(*1:Poor-5:Excellent)" list="{1, 2, 3, 4, 5}" name="peakAnnotatedContributorQuality" />
!-->
<@ww.select label="Peak Processing Type" list="peakProcessingTypes" name="peakProcessingType" />
<@ww.radio label="Deisotoped" name="Deisotoped" list="{'True','False'} "></@ww.radio>
<@ww.radio label="Charge Deconvoluted" name="chargeDeconvoluted" list="{'True','False'}"></@ww.radio>
<@ww.submit value="Upload" name="submitAction"/>
</@ww.form>
<#else>
<h2> This aquisitionId not found in our database.</h2>
<@ecdb.actionlink name="gwupload_phaseI">Back to the list of acquisitions</@>
</#if>


<#include "/template/common/footer.ftl" />