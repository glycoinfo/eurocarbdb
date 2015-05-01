<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<#assign title>Acquisition Form</#assign>
<#include "/template/common/header.ftl" />
<#assign temp="mzXML"/>
<#assign seq = [1, 2, 3, 4, 5]>
<h2>Browse Acquisitions</h2>
<h4> Acquisition Generation </h4>
<@ww.form method ="post" enctype="multipart/form-data">
<@ww.label label="File Name" name="acquisitionFileFileName"/>
<@ww.hidden name="acquisitionFileFileName"/>
<@ww.hidden name="fpath"/>
<@ww.hidden name="filepath"/>
<@ww.textfield label="File Type" name= "temp" value=temp />
<@ww.textfield label="msManufacturer" name="msManufacturer" />
<@ww.textfield label="Manufacturer url" name="msurl" />
<@ww.textfield label="msModel" name="msModel"/>
<@ww.textfield label="msIonization" name="msIonization"/>
<@ww.textfield label="Acquisition Software Type" name="mssoftware_types"/>
<@ww.textfield label="Acquisition Software Name" name="mssoftware_name"/>
<@ww.textfield label="Acquisition Software Version" name="mssoftware_version"/>
<@ww.hidden name="dsoftware_cutoff"/>
<@ww.checkbox label="Spot Integration" name="spot"/>
 <@ww.select label="Contributor quality 1-Excellent 5-Poor" list=seq name="seq"/> 
<h4>  mzXML file generation </h4>
<@ww.textfield label="Software Type" name="dsoftware_types" value="conversion"/>
<@ww.textfield label="Software Name" name="dsoftware_name"/>
<@ww.textfield label="Software Version" name="dsoftware_version"/>
<@ww.hidden name="state"/>
<@ww.hidden name="persubstitutionId"/>
<@ww.hidden name="fdate"/>
<@ww.submit value="Create" name="submitAction"/>
</@ww.form>
<#include "/template/common/footer.ftl" />