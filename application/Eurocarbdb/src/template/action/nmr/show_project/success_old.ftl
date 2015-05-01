
<#import "/template/lib/Eurocarb.lib.ftl" as eurocarb />

<#assign title>View CCPN Project</#assign>

<#macro printPath path>
	<font class="res_path_element_first">RE</font>

	<#if (path?size > 0) >
		&gt;
	</#if>
	
	<#list path as item>
		<#if (item != path?last)>
			<font class="res_path_element_middle">${item}</font>	
			&gt;
		<#else>
			<font class="res_path_element_last">${item}</font>	
		</#if>
		
	</#list>
</#macro>

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
		
		// container.innerHTML = "<img width=400 src='" + strUrl + "'/>";
		container.innerHTML = "<object data='" + strUrl + "'type=\"image/svg+xml\" width=400 />";
		
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
	<ul class="nmr_menu">
		<li id="li_exp_details"><a href="" onclick="return show_page('exp_details');">Experimental Details</a></li>
		<li id="li_shifts"><a href="" onclick="return show_page('shifts');">Shifts</a></li>
	</ul>
	
	<div style="clear: left;"></div>

	<div class="page experimental_details" style="display: none;" id="exp_details">
		<div class="row">
			<div class="field">Project</div>
			<div class="value">${action.getMemopsRoot().getName()}</div>
		</div>
		
		<#list action.getMemopsRoot().sortedNmrProjects() as nmrPro>
			<#list nmrPro.getExperiments() as exp>
			
				<#if exp.getSpectrometer()?has_content>
					<#assign spec = exp.getSpectrometer() />
					
					<div class="row">
						<div class="field">Spectrometer</div>
						<div class="value">${spec.getName()}</div>
					</div>
                    
                    <#if spec.getProtonFreq()?has_content>
                        <div class="row">
                            <div class="field">Proton Frequency</div>
                            <div class="value">${spec.getProtonFreq()} MHz</div>
                        </div>                        
                    </#if>
				</#if>
			
				<div class="row">
					<div class="field">Name</div>
					<div class="value">${exp.getName()}</div>
				</div>
                
				<div class="row">
					<div class="field">Dimensions</div>
					<div class="value">${exp.getNumDim()?c}</div>
				</div>
                
                <#if exp.getDetails()?has_content>
    				<div class="row">
    					<div class="field">Details</div>
    					<div class="value">${exp.getDetails()}</div>
    				</div>
                </#if>
			</#list>
		</#list>
	</div>
	
	<div class="page shifts" style="display: none;" id="shifts">
    
        <#if action.getGlycanSequenceId()?has_content >
            <img src="get_sugar_image.action?download=false&outputType=png&tolerateUnknown=1&glycanSequenceId=${action.getGlycanSequenceId()}" />
        <#else>
            no glycan sequence attached to evidence
        </#if>
		
		<#assign arrMolSystems = action.getMemopsRoot().sortedMolSystems() />
		<#assign nID = 0 />
		<#list arrMolSystems as ms>
			
			<#assign arrChains = ms.sortedChains() />
			
			<#list arrChains as c>
				<div class="section">Chain ${c.getCode()}</div>
				
					<#assign arrResidues = c.sortedResidues() />
				
					<#list arrResidues as r>
						<#assign mol_r = r.getMolResidue() />
					
						
					
						<div class="row">
							<div class="res_id">${r.getSeqCode()}</div>
							<div class="res_name"><a href="javascript: toggle('shifts_${nID?c}');">${r.getCcpCode()}</a></div>
							<div class="res_render"><a href="javascript: show_chemcomp('${mol_r.getMolType()}', '${mol_r.getCcpCode()}');">Render ChemComp</a></div>
							
							<div class="res_path"> <@printPath path = action.pathFromReducingEnd(r.getMolResidue()) /> </div>
							
							<#assign nShifts = 0 />
							
							<div class="res_shifts" style="display:none;" id="shifts_${nID?c}">
							<#assign nID = nID + 1/>
							
							<#list r.sortedAtoms() as atom>
								<#if atom.getAtomSet()?has_content >
									<div class="atom">
									<div class="atom_name">${atom.getChemAtom().getName()}</div>
									
									<#list atom.getAtomSet().sortedResonanceSets() as rs>
										<#list rs.sortedResonances() as r>
											<#list r.sortedShifts() as s>
													<div class="shift_value">${s.getValue()}</div>
													<div class="shift_error">(+/-${s.getError()})</div>
													
													<#assign nShifts = nShifts + 1 />
											</#list>
										</#list>
									</#list>
									
									</div>
								</#if>
							</#list>
							
							<#if (nShifts == 0)>
								no shifts assigned to atoms in this residue
							</#if>
							
							</div>
						</div>
						
					</#list>
				</div>
				
			</#list>
			
		</#list>
		
	</div>
</div>

<#include "/template/common/footer.ftl" />