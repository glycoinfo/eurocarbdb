<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<#assign title>Acquisition Form</#assign>
<#include "/template/common/header.ftl" />
<style type="text/css">
span.red {color:red;font-weight:bold}
span.green {color:darkolivegreen;font-weight:bold}
</style>
<#assign temp ="RAWData"/>
<h2>review page</h2>
<h4> Please verify the given information </h4>
<@ww.form method ="post" enctype="multipart/form-data" >
<@ww.hidden name="acquisitionFileFileName"/>
<@ww.label label="File Name" name="acquisitionFileFileName"/>
<@ww.hidden name="fpath"/>
<@ww.hidden name="filepath" />
<@ww.hidden name="temp"/>
<@ww.label label="msManufacturer" name="msManufacturer"/>
<@ww.hidden name="msManufacturer"/>
<@ww.label label="Manufacturer url" name="msurl"/>
<@ww.hidden name="msurl"/>
<@ww.label label="msModel" name="msModel" />
<@ww.hidden name="msModel" />
<@ww.label label="msIonization" name="msIonization"/>
<@ww.hidden name="msIonization"/>
<@ww.label label="Software Type" name="mssoftware_types" />
<@ww.hidden name="mssoftware_types" />
<@ww.label label="Software Name" name="mssoftware_name" />
<@ww.hidden name="mssoftware_name" />
<@ww.label label="Software Version" name="mssoftware_version" />
<@ww.hidden name="mssoftware_version" />
<@ww.hidden name="dsoftware_cutoff" />
<@ww.label label="Spot Integration" name="spot"/>
<@ww.hidden name="spot"/>
<@ww.label label ="contributer quality" name="seq"/>
<@ww.hidden name="seq"/>
<h4>  mzXML file generation </h4>
<@ww.label label="Software Type" name="dsoftware_types"/>
<@ww.hidden name="dsoftware_types"/>
<@ww.label label="Software Name" name="dsoftware_name"/>
<@ww.hidden name="dsoftware_name"/>
<@ww.label label="Software Version" name="dsoftware_version" />
<@ww.hidden name="dsoftware_version" />
<@ww.label label="Scans To be Processed " name="scansid"/>
<@ww.hidden name="scansid"/>
<@ww.hidden name="state"/>
<@ww.hidden name="persubstitutionId"/>
<@ww.hidden name= "count"/>
<@ww.hidden name="fdate"/>
<p><span class="red">The Upload may take upto 2 mins</span></p>
<@ww.submit value="Create" name="submitAction"/>
<@ecdb.actionlink name="create_form">Back</@>
</@ww.form>
<#include "/template/common/footer.ftl" />
