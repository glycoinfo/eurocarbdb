<#import "/template/lib/Eurocarb.lib.ftl" as ecdb />
<#assign title>Biological Context Detail</#assign>
<#include "/template/common/header.ftl" />

<#import "/template/lib/TextUtils.lib.ftl" as text />

<h1>${title}</h1>


<#assign bc = biologicalContext/>

<table class="table_left_header">

    <#------------------------------------------------------------------->
    <tr>
        <th>Biological&nbsp;Context&nbsp;ID</th>
        <td>bc:${ bc.biologicalContextId?c }</td>
    </tr>
    
    <#------------------------------------------------------------------->
    
    <tr>
        <th>Contributor<#if bc.biologicalContextContributors?size <=1 ><#else>s</#if></th>
	<#list bc.biologicalContextContributors as x>
	  <td><@ecdb.contributor c=x.contributor /> on ${x.contributor.dateEntered?date}</td>
	</#list> 
    </tr>

    <#------------------------------------------------------------------->
    <tr>
        <th>Taxonomy</th>
        <td><@ecdb.detail_link object=bc.taxonomy text=bc.taxonomy.taxon /></td>
    </tr>
    
    <#------------------------------------------------------------------->
    <tr>
        <th>Tissue</th>
        <td><@ecdb.tissue t=bc.tissueTaxonomy /></td>
    </tr>
    
    <#------------------------------------------------------------------->
    <#if ( bc.diseaseContexts?exists && bc.diseaseContexts?size > 0 )>
    <!-- list of diseases -->
    <tr>
        <th>Disease associations</th>
        <td>
            <#list bc.diseaseContexts as dc>
            <@ecdb.detail_link object=dc.disease text=dc.disease.diseaseName/><br/>
            </#list>	
       </td>
    </tr>
    <#else />
    <!-- no diseases -->
    </#if>
        
    <#------------------------------------------------------------------->
    <#if ( bc.perturbationContexts?exists && bc.perturbationContexts?size > 0 )>
    <!-- list of perturbations -->
    <tr>
        <th>Perturbation associations</th>
        <td>
            <#list bc.perturbationContexts as pc>
            <@ecdb.detail_link object=pc.perturbation text=pc.perturbation.perturbationName/><br/>
            </#list>	
        </td>
    </tr>
    <#else />
    <!-- no perturbations -->
    </#if>
    
    <#------------------------------------------------------------------->
    
<#--

</table>


<h2>Associations</h2>
<table class="table_left_header">
-->

    <#---------------------------- sequences --------------------------------------->
    <tr>
        <th>Sequences</th>
        <td>
        <#if ( bc.glycanSequenceContexts?exists && bc.glycanSequenceContexts?size > 0 )>
            <div class="thumbnails">
            <#list bc.glycanSequenceContexts as gsc >
            <@ecdb.linked_sugar_image id=gsc.glycanSequence.glycanSequenceId seq=gsc.glycanSequence.sequenceGWS?url scale="0.5" />
            </#list>
            </div>
        <#else/>
              (No sequences associated to this context)
        </#if>
        </td>
    </tr>

    <#---------------------------- evidence --------------------------------------->
    <#if ( bc.evidenceContexts?exists && bc.evidenceContexts?size > 0 )>
    <tr>
        <th>Evidence</th>
        <td>
            <#list bc.evidenceContexts as ec >
            <@ecdb.evidence ev=ec.evidence /><br/>
            </#list>
        </td>
    </tr>
    </#if>

</table>


<#include "/template/common/footer.ftl" />