<#assign title>Tissue Taxonomy detail</#assign>
<#include "/template/common/header.ftl" />

<#import "/template/lib/TextUtils.lib.ftl" as text />

<#list action.getResult().getAllResults().iterator() as actionResult>
	Result found on <ul><#list action.dataSources(actionResult) as server>
						<li>${server}</li>						
					</#list>
					</ul>
        ${actionResult.tissueTaxonomy.tissueTaxonomyId?c}<br/>
        ${actionResult.getMeshDescriptionHTML()}<br/>
</#list>

<#include "/template/common/footer.ftl" />