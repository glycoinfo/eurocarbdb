<#assign title>Glycan sequence detail</#assign>
<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<@ww.url value="/js/lib/addGu.js" includeParams="none" id="url_addGu_js"/>
<@ww.url value="/js/lib/checkGu.js" includeParams="none" id="url_checkGu_js"/>
<@ecdb.include_js url="${url_addGu_js}"/>
<@ecdb.include_js url="${url_checkGu_js}"/>

<#include "/template/common/header.ftl" />


<h1>${title}</h1>

<#if glycan?exists >

<p>
<img style="display: block;" 
     src="get_sugar_image.action?download=true&outputType=png&glycanSequenceId=${glycan.glycanSequenceId?c}" 
     />
</p>
<p>
<table class="table_left_header">

    <tr>
        <th>Glycan Sequence ID</th> 
        <td>${glycan.glycanSequenceId?c}</td>
    </tr>
    
    
    
    <!-- references -->
    <#assign ref = reference />
    <tr>
        <th>References</th>
        <td>
            <ul>
                <li>Pubmed Id: ${ref.pubmedId?c} <#if ref.title?exists> <b>${ref.title}</b> </#if></li>
            </ul>
        </td>
    </tr>


    <tr>
	<th>GU Data</th>
	<#if displayStats?exists>
	<td>
	 <#list displayStats as s>
	    <ul>
	        <li>Average Value: ${s[1]?string("0.##")}</li>
		<#if s[0]?exists >
		<li>Standard Dev: ${s[0]?string("0.##")}</li>
		</#if>
	    </ul>
	</#list>
	</td>
	<#else/>
	  No reported HPLC values
	</#if>
    </tr>

    <tr>
	<th>Name</th>
	<#if glycanAll?exists>
	<td>
	    <ul>
	        <li>${glycanAll.name}</li>
	    </ul>
	</td>
	</#if>
    </tr>
    <tr>
        <th>Add New Value</th>
    <td>
	<ul>
	<form name="addGuValue" onSubmit="return checkban()">	
	<@ww.hidden name="glycanReferenceId" value="${glycanReferenceId?c}"/>
	<@ww.hidden name="glycanJournalReferenceId" value="${ref.journalReferenceId?c}"/>
	<@ww.hidden name="glycanHplcId" value="${glycanHplcId}" />
	Enter GU  value for this structure: <@ww.textfield name="guValue"/>
	<@ww.submit name="submitAction" value="Add" />
	</ul>
	</form>
</td>
    </tr>


    
</table>

<#else/>
    <p>
		No sequence!
    </p>
</#if>

<#include "/template/common/footer.ftl" />
