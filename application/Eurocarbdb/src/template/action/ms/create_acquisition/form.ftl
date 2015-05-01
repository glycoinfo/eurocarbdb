<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<#assign title>Acquisition Form</#assign>
<#include "/template/common/header.ftl" />
<#assign ftype="mzXML"/>
<h2>Browse Acquisitions</h2>
<h4> Acquisition Generation </h4>
<@ww.form method ="post" enctype="text/plain">
<@ww.textfield label="tfile" name="tfile" value=tfile/>
<@ww.textfield label="File Name" name="file" value=fpath/>
<@ww.label label="File Type" name="ftype" value= ftype/>
<@ww.textfield label="msManufacturer" name="manufacturer" value=msManufacturer/>
<@ww.textfield label="Manufacturer url" name="man_uri" value=msuri/>
<@ww.textfield label="msModel" name="model" value=msModel/>
<@ww.textfield label="msIonization" name="ionization" value=msIonization/>
<@ww.textfield label="Software Type" name="stype" value=mssoftware_types/>
<@ww.textfield label="Software Name" name="sname" value=mssoftware_name/>
<@ww.textfield label="Software Version" name="sversion" value=mssoftware_version/>
<@ww.textfield label= "Intensity Cutoff " name="intcut" value=dsoftware_cutoff/>
<h4>  mzXML file generation </h4>
<@ww.textfield label="Software Type" name="stype" value=dsoftware_types/>
<@ww.textfield label="Software Name" name="sname" value=dsoftware_name/>
<@ww.textfield label="Software Version" name="sversion" value=dsoftware_version/>
<@ww.textfield label="Scan counts" name = "scounts" value = scans/>
<@ww.submit value="Create" name="submitAction"/>

</@ww.form>
<#include "/template/common/footer.ftl" />
