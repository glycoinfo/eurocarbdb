<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<#assign title>Test glycanSequence parsing in progress...</#assign>
<#assign additional_head_content>
<meta http-equiv="refresh" content="5" />
</#assign>
<#include "/template/common/header.ftl" />

<h1>${title}</h1>

<p>
    <ul>
        <li>${countTotal} of ${countTotalSequences} sequences parsed (${percentComplete}% done in ${(millisecsElapsed / 1000)} secs; avg ${(countTotal / millisecsElapsed)}msec/sequence)</li>    
        <li>${countSuccessful} parsed successfully (${percentSuccessful}%)</li>    
        <li>${failedSequences?size} with parse exceptions</li>    
        <li>${otherExceptions?size} other exceptions</li>    
    </ul>
    <#if (percentComplete < 99)>
    <a href="?stop=true">stop parsing</a><br/>
    </#if>
<p>
<#if (failedSequences?size > 0) >
<h2>Parse exceptions</h2>
<table class="table_top_header" style="width: 100%">
    <#list failedSequences as seq >     
    <tr>
        <td><a href="${base}/show_glycan.action?glycanSequenceId=${seq.glycanSequenceId?c}">${seq.glycanSequenceId?c}</a></td>
        <#--<td><pre>${seq.sugarSequence}</pre></td>-->
        <td><pre style="font-size: x-small">${failedSequenceExceptions.get(seq_index)}</pre></td>
    </tr>
    </#list>
</table>
</#if>

<#if (otherExceptions?size > 0) >
<h2>Other exceptions</h2>
<table class="table_top_header" style="width: 100%">
    <#list otherExceptions as other >     
    <tr>
        <td>${other}</td>
        <td>
            <#list other.getStackTrace() as stack_element>
            ${stack_element}
            </#list>
        </td>
    </tr>
    </#list>
</table>
</#if>

<#include "/template/common/footer.ftl" />
