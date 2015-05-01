<#assign title>Structure stored</#assign>
<#include "/template/common/header.ftl" />
<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />

<h1>${title}</h1>

<p>
  <img style="display: block;" 
       src="get_sugar_image.action?download=true&inputType=gws&outputType=png&sequences=${glycanSequenceAsGWS?url}" 
       />
</p>

<p>
  The structure has been stored with Id ${glycanSequence.glycanSequenceId}.
</p>


<#include "/template/common/footer.ftl" />
