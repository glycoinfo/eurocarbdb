<#assign title>Structure browsing</#assign>
<#include "/template/common/header.ftl" />

<#import "/template/lib/TextUtils.lib.ftl" as text />
<h1>${title}</h1>
<h2>This query has been sent to the following servers:</h2>
<ul>
<#list action.getSourceServers() as sourceServer><li>${sourceServer}</li></#list>
</ul>
<p>From these servers, we have obtained the following results:
</p>
<#list action.getAllResults() as glycanSequence>
<div class=" ecdb_distributed_result_entry " >
<div class=" ecdb_distributed_result_serverlist " >
	<#list action.dataSources(glycanSequence) as server><span class=" ecdb.distributed.result.serverlist.entry ">${server}</span></#list>
</div>
<div class=" ecdb_distributed_result_data ">
<#assign server>http://${action.dataSources(glycanSequence).get(0)}</#assign>
<#include "common-success.ftl"/>
</div>
</div>
</#list>

<#include "/template/common/footer.ftl" />