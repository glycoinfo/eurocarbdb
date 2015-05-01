
<#import "/template/lib/Eurocarb.lib.ftl" as eurocarb />

<#assign title>View Nmr Project</#assign>

<#include "/template/common/header.ftl" />

<style>
	@import url(../css/action/nmr.css);
</style>

<script src="../js/prototype.js"></script>

<script>
	var strShowingID = null;

	function show_page(strID)
	{
		var el, li;
	
		if (strShowingID != null)
		{
			el = $(strShowingID);
			el.style.display = "none";
			
			li = $("li_" + strShowingID);
			li.className = "";
		}
		
		el = $(strID);
		el.style.display = "block";

		li = $("li_" + strID);
		li.className = "selected";
		
		strShowingID = strID;
		
		return false;
	}
	
	function toggle(strID)
	{
		el = $(strID);
		
		if (el.style.display == "block")
		{
			el.style.display = "none";
		}
		else
		{
			el.style.display = "block";
		}
	}
	
	function page_init() 
	{
		   // display first page
		   show_page('exp_details');
		   
			// apply alternating row colors
	       var rows = document.getElementsByTagName('div');
		   
		   j = 0;
	       for (var i=0; i < rows.length; i++) 
		   {
	               if (rows[i].className == "row")
				   {
						if (j % 2 == 0)
						{
	                      rows[i].className += " odd";
						}
						
						j += 1;
	               }
				   else if (rows[i].className == "section" || rows[i].className.indexOf("page") != -1)
				   {
						j = 0;
				   }
	       }
		   
	}
	
	function show_chemcomp(strMolType, strCcpCode)
	{
		var container = $('chemcomp');
		
		strUrl = "drawChemComp.action?molType=" + strMolType + "&ccpCode=" + strCcpCode;
		
		container.innerHTML = "<img width=400 src='" + strUrl + "'/>";
		
		outer = $('outer_chemcomp');
		
		outer.style.display = 'block';
		
	}

	window.onload = page_init;

</script>

<div id="outer_chemcomp" style="display: none;">
	<div class="chemcomp_close"><a href="" onclick="javascript: $('outer_chemcomp').style.display = 'none'; return false;">close</a></div>
	
	<div id="chemcomp">
	
	</div>
</div>

<div class="nmr">
    <div class="section">Experimental Details</div>
    
    <div class="row">
        <div class="field">Project</div>
        <div class="value">${action.getProject().getName()}</div>
    </div>
    
    <#list action.getProject().getNmrProjects() as nmrPro>
        <#list nmrPro.getExperiments() as exp>
        
            <#if exp.getSpectrometer()?has_content>
                <#assign spec = exp.getSpectrometer() />
                
                <div class="row">
                    <div class="field">Spectrometer</div>
                    <div class="value">${spec.getName()}</div>
                </div>
                
                <#if spec.getFrequency()?has_content>
                    <div class="row">
                        <div class="field">Proton Frequency</div>
                        <div class="value">${spec.getFrequency()} MHz</div>
                    </div>                        
                </#if>
            </#if>
        
            <div class="row">
                <div class="field">Experiment</div>
                <div class="value">${exp.getName()}</div>
            </div>
            
            <div class="row">
                <div class="field">Dimensions</div>
                <div class="value">${exp.getDimensions()?c}</div>
            </div>
            
            <#if exp.getComment()?has_content>
                <div class="row">
                    <div class="field">Comments</div>
                    <div class="value">${exp.getComment()}</div>
                </div>
            </#if>
        </#list>
    </#list>
    
    <div class="section">
        Shifts
    </div>
    
    <#list action.getEvidence().getGlycanSequences() as gs>
        <img src="get_sugar_image.action?download=false&outputType=png&tolerateUnknown=1&glycanSequenceId=${gs.getGlycanSequenceId()}" />
    </#list>
    
    <#list action.getProject().getResidues() as r >
            <div class="residue">${r.getName()} <a href="javascript: show_chemcomp('${r.getMolType()}', '${r.getName()}');">Visualize</a></div>
            
            <div class="path">
            <#if r.getLink()?has_content>
                <#list r.getLinkPath() as l>
                    ${l.getName()} - 
                </#list>
                reducing end
            <#else>
                reducing end
            </#if>
            </div>
            
            <#list r.getShifts() as s>
                <div class="row">
                    <div class="field">${s.getAtom()}</div>
                    <div class="value">${s.getValue()} +/-${s.getError()}</div>
                </div>
            </#list>
    </#list>
    
</div>

<#include "/template/common/footer.ftl" />