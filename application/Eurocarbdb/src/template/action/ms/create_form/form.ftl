<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<#assign title>Acquisition Form</#assign>
<#include "/template/common/header.ftl" />
<h2> mzXML file Uploaded successfully in Database</h2>
<h3> Summary : </h3>

<table>
<tr>
<td> Acquistion ID </td> <#list ownedAcquisitions as o><td>${o.acquisitionId}</td></#list></tr>
<tr><td> Name of mzXML File</td> <td> ${acquisitionFileFileName}</td> </tr>
<tr><td> Date Obtained </td> <td> ${fdate} </td> </tr>
<tr><td> Date Entered</td> <td> ${cdate?string("yyyy-MM-dd HH:mm:ss")}</td> </tr>
<tr><td> Number of Scan</td> <td> ${count} </td> </tr> </table> <br/>
<@ecdb.actionlink name="create_acquisition!input">Upload another mzXML file</@><br/>
<@ecdb.actionlink name="gwupload_phaseI">Upload another Glycoworkbench annotation file</@><br/>
<@ecdb.actionlink name="create_form">Back</@>
<#include "/template/common/footer.ftl" />
