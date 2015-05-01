<#import "/template/lib/Eurocarb.lib.ftl" as eurocarb />

<#assign title>Create NMR Project</#assign>

<#include "/template/common/header.ftl" />

<h1>${title}</h1>

finish - show entered values and let user confirm

<div>
	${ProjectName} <br>
	${ExperimentName} <br>
	${Dimensions} <br>
</div>

<@ww.form method="post">

<@ww.submit value="%{'< Back'}" action="create_nmr_project_back_to_step3" />
<@ww.submit value="%{'Save >'}" action="create_nmr_project_save" />

</@ww.form>

<#include "/template/common/footer.ftl" />

