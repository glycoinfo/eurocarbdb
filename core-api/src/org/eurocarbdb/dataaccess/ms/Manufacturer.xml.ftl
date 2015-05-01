<manufacturer manufacturerId="${x.manufacturerId?c}"
	      name="${x.name}"
	      <#if (x.url?exists)>url="${x.url}"</#if>
	      >

  <#if show_detail>
    <#if (x.devices?exists)>
    <devices>
      <#list x.devices as d >
      ${xml.serialise( d, false )}
      </#list>
    </devices>
    </#if>
  </#if>
  
</manufacturer>
