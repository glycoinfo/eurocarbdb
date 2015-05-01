<#include "./header.ftl">

<form method="get" action="build_monosaccharide_by_groups.action">
<#if size == 0 && !msname??>Select Monosaccharide size (number of backbone carbons): 
<select name="size" size="1">
	<#if size == 0 || size == 3><option value="3">3 - Triose</option></#if>	
	<#if size == 0 || size == 4><option value="4">4 - Tetrose</option></#if>
	<#if size == 0 || size == 5><option value="5">5 - Pentose</option></#if>
	<#if size == 0 || size == 6><option value="6" selected>6 - Hexose</option></#if>
	<#if size == 0 || size == 7><option value="7">7 - Heptose</option></#if>
	<#if size == 0 || size == 8><option value="8">8 - Octose</option></#if>
	<#if size == 0 || size == 9><option value="9">9 - Nonose</option></#if>
	<#if size == 0 || size == 10><option value="10">10 - Decose</option></#if>
</select>
<input type="submit"><br>
</form>
<form method="get" action="build_monosaccharide_by_groups.action">
or enter Monosaccharide name to initialize builder:
<input type="text" size="40" name="msname">
<input type="submit">
<#else>
	<#if ms??>
		<#if ms.name??><span class="bold">Monosaccharide: ${ms.name}</span><br></#if>
		<#if ms.basetype.ringEnd == OPEN_CHAIN>
			<#if ms.hasFischer(defaultGraphicsFormat)><img src="get_ms_representation.action?monosaccName=${ms.name}&repType=fischer&repFormat=PNG&namescheme=msdb&preserveOrientation=true" class="structure_image" title="Fischer representation of ${ms.name}"><br></#if>
		<#else>
			<#if ms.hasHaworth(defaultGraphicsFormat)><img src="get_ms_representation.action?monosaccName=${ms.name}&repType=haworth&repFormat=PNG&namescheme=msdb" class="structure_image" title="Haworth representation of ${ms.name}"><br></#if>
		</#if>
		<br>
	</#if>
	<#if warningString??><div class="bold">${warningString}</div><br></#if>
	<span class="bold">Build Backbone:</span><br>
	<#if errorString??><div class="error">${errorString}</div></#if>
	<#list 1..size as pos>
		<select name="positions" size="1">
		<option value="X" style="margin-bottom:5px;">select C${pos} configuration or core mod.</option>
		<#list builderGroups as group>
			<#assign selectedGroup="">
			<#if positions?? && positions[pos-1]??>
				<#assign selectedGroup=positions[pos-1]>
			</#if>
			<#if ((pos==1 || (pos==size && group.headTail) || (!(pos==1 || pos==size) && ! group.headTail)) && group.displayName!="unknown")>
			<option value="${group.extStereoSymbol}"<#if selectedGroup==group.extStereoSymbol> selected</#if>>${group.displayName}</option>
			</#if>
		</#list>
		</select><#if positionErrors?? && positionErrors[pos]??><span class="error">${positionErrors[pos]}</span></#if><br>
	</#list>
	<br>
	<span class="bold">Set Ring Closure:</span><br>
	Start <select name="ringStart" size="1">
		<option value="-1">open chain</option>
		<#if size gt 3>
		<#list 1..size - 4 as i>
		<#if i gt 0><option value="${i}" <#if i == ringStart>selected</#if>>${i}</option></#if>
		</#list>
		</#if>
	</select>
	<#--><input type="text" name="ringStart" size="2" <#if ringStart?? && ringStart gt 0>value="${ringStart}"</#if>></#-->
	End <select name="ringEnd" size="1">
		<option value="-1">open chain</option>
		<#if size gt 3>
		<#list 4..size as i>
		<#if i gt 3><option value="${i}" <#if i == ringEnd>selected</#if>>${i}</option></#if>
		</#list>
		</#if>
	</select><br>
	<#--><input type="text" name="ringEnd" size="2" <#if ringEnd?? && ringEnd gt 0>value="${ringEnd}"</#if>></#-->
	<input type="hidden" name="size" value="${size}">
	<br>
	<span class="bold">Substituents:</span><br>
	<#list 0..size-1 as substI>
		Position:
		<select name="substMsPos" size="1">
			<option value="">  </option>
		<#list 1..size as substpos>
			<option value="${substpos}" <#if substMsPos?? && substMsPos[substI]?? && substpos?string==substMsPos[substI]>selected</#if>>${substpos}</option>
		</#list>
		</select>
		Name:
		<select name="substName" size="1">
			<option value="">select</option>
		<#list container.getSubstituentTemplateContainer().getTemplateList() as substTmpl>
			<option value="${substTmpl.name}" <#if substName?? && substName[substI]?? && substTmpl.name==substName[substI]>selected</#if>>${substTmpl.name}</option>
		</#list>
		</select>
		Linkage Type:
		<select name="substMsLinktype" size="1">
			<option value="">default</option>
			<option value="H_AT_OH" <#if substMsLinktype?? && substMsLinktype[substI]?? && substMsLinktype[substI]=="H_AT_OH">selected</#if>>O-linked</option>
			<option value="DEOXY" <#if substMsLinktype?? && substMsLinktype[substI]?? && substMsLinktype[substI]=="DEOXY">selected</#if>>deoxy</option>
			<option value="H_LOSE" <#if substMsLinktype?? && substMsLinktype[substI]?? && substMsLinktype[substI]=="H_LOSE">selected</#if>>C-linked</option>
		</select><br>
	</#list>
	<br>
	<input type="submit" name="step" value="preview"/> <input type="submit" name="step" value="finish"/>
</#if>
</form>
<br/>
<#include "./footer.ftl">