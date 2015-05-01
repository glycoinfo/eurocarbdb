<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<#assign title>Test glycanSequence parsing</#assign>
<#include "/template/common/header.ftl" />

<h1>${title}</h1>

<#if ( action.countTotal == 0 )><#------------------------- empty DB -->

<p>
    The database did not return any sequences.
</p>
<#list otherExceptions as ex >
<p>
    ${ex.toString}
</p>
</#list>

<#else><#---------------------------------------------- typical case -->
<p>
    <ul>
        <li>${countTotal} sequences parsed (${percentComplete}% done in ${(millisecsElapsed / 1000)} secs; avg ${(countTotal / millisecsElapsed)}msec/sequence)</li>    
        <li>${countSuccessful} parsed successfully (${percentSuccessful}%)</li>    
        <li>${failedSequences?size} with parse exceptions</li>    
    </ul>
<p>
<table>
    <#list failedSequences as seq >     
    <tr>
        <td><a href="${base}/show_glycan.action?glycanSequenceId=${seq.glycanSequenceId?c}">${seq.glycanSequenceId?c}</a></td>
        <td><pre>${seq.sugarSequence}</pre></td>
        <td><pre>${failedSequenceExceptions.get(seq_index)}</pre></td>
    </tr>
    </#list>
</table>

</#if>


<#include "/template/common/footer.ftl" />
