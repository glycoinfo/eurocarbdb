<device deviceId="${x.deviceId?c}"
        model="${x.model}"
	ionisationType="${x.ionisationType}">

  <#if show_detail>
    <#if (x.manufacturer?exists)>
    ${xml.serialise( x.manufacturer, false )}
    </#if>
  </#if>
  
</device>
