<#assign title>Import Structures</#assign>
<#include "/template/common/header.ftl" />

<#import "/template/lib/FormInput.lib.ftl" as input />

<h1>${title}</h1>

<#if ( message?length > 0 )>
	<div style="padding: 1em 0 1em 0; color: darkred;">
		${message}	
	</div>
</#if>

<p>imports structures from xml files (see application/CarbbankTranslationReader/structures.zip)</p>

<form method="post" enctype="multipart/form-data">
  Select structures file: <input type="file" name="structuresFile"/>
  <input type="submit" name="submitAction" value="Upload"/>
</form>


<#include "/template/common/footer.ftl" />
