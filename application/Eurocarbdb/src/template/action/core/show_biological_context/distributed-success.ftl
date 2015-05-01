<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<#assign title>Distributed Taxonomy Search</#assign>
<#include "/template/common/header.ftl" />

<h1>${title}</h1>
<h2>This query has been sent to the following servers:</h2>
<ul>
<#list action.getSourceServers() as sourceServer><li>${sourceServer}</li></#list>
</ul>
<p>From these servers, we have obtained the following results:
</p>
<#list action.getAllResults() as biologicalContext>
<div class=" ecdb_distributed_result_entry " >
<div class=" ecdb_distributed_result_serverlist " >
	<#list action.dataSources(biologicalContext) as server><span class=" ecdb.distributed.result.serverlist.entry ">${server}</span></#list>
</div>
<div class=" ecdb_distributed_result_data ">
<#include "common-success.ftl"/>
<#include "bc-sequences.ftl"/>
</div>
</div>
</#list>

<#include "/template/common/footer.ftl" />