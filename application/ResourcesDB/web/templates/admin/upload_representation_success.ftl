<#include "../header.ftl">
<span class="bold">Residue representation successfully uploaded into database with id ${representation.dbId}.</span><br><br>
&bull; <a href="upload_representation.action?moleculeClass=${moleculeClass}&moleculeId=${moleculeId}<#if moleculeName??>&moleculeName=${moleculeName}</#if>">upload another representation for this residue</a><br><br>
&bull; <a href="upload_representation.action">upload a representation for a different residue</a>
<#include "../footer.ftl">