<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<#assign title>Acquisition Form</#assign>
<#include "/template/common/header.ftl" />
<#assign ftype="mzXML"/>
<h2>review page</h2>
<h4> Please verify the given information </h4>
<@ww.form method ="post" enctype="text/plain">
<!--<@ww.label label="File Name" name="file" value=fname/>
<@ww.label label="File Type" name="ftype" value= ftype/>
<@ww.label label="msManufacturer" name="manufacturer" value=msManufacturer/>
<@ww.label label="Manufacturer url" name="man_uri" value=temp/>
<@ww.label label="msModel" name="model" value=msModel/>
<@ww.label label="msIonization" name="ionization" value=msIonization/>
<@ww.label label="Software Type" name="stype" value=mssoftware_types/>
<@ww.label label="Software Name" name="sname" value=mssoftware_name/>
<@ww.label label="Software Version" name="sversion" value=mssoftware_version/>
<@ww.label label= "Intensity Cutoff " name="intcut" value=dsoftware_cutoff/>
<h4>  mzXML file generation </h4>
<@ww.label label="Software Type" name="stype" value=dsoftware_types/>
<@ww.label label="Software Name" name="sname" value=dsoftware_name/>
<@ww.label label="Software Version" name="sversion" value=dsoftware_version/>-->
<@ww.submit value="Create" name="submitAction"/>
</@ww.form>
<#include "/template/common/footer.ftl" />
