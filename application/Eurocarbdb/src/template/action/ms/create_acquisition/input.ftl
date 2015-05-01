<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<#assign title>Acquisition Creation</#assign>

<@ecdb.use_js_lib name="${'DatePicker'}"/>

<#include "/template/common/header.ftl" />
<style type="text/css">
  .short_glycan_sequence_context {
    width: 300px;
    height: 75px;
    font-size: 0.75em;
    margin: 5px;
    float: left;
  }
  
</style>

<h1>Create Acquisition</h1>
<h2>Annotated too...</h2>
<h3>Specify acquisition parameters:</h3>
<#assign seq = ["1 -Excellent", "2-Very Good", "3-Good", "4-Average","5-Unsatisfactory"]>
<@ww.form method="post" enctype="multipart/form-data">
<@ww.file label="mzXML file" name="acquisitionFile"/>
<@ww.select label="Persubstitution" list="persubstitutions" listValue="name" listKey="persubstitutionId" name="persubstitutionId"/>

<#assign dateVal=''/>
<#if acquisition? exists>
  <#assign dateVal=acquisition.dateObtained/>
</#if>
<@ecdb.datepicker name="date_obtained" value=dateVal label="Date obtained"/> 
<@ww.submit value="Create" name="submitAction"/>
</@ww.form>



<#include "/template/common/footer.ftl" />
