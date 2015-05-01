
<html>
  <head>
    <title>Device Creation</title>
    <SCRIPT LANGUAGE="JavaScript">     
      function copy(sourceID,destID) {
        document.getElementById(destID).value = document.getElementById(sourceID).value;
      }	  
    </SCRIPT>
  </head>

  <body>
    <h1>Input Device Details</h1>
    <form>
      Manufacturer: 
      <select id="man_list" onchange="copy('man_list','man_name')">
	<option value="---"/>
	<#list manufacturers as m>
	<option>${m.name}</option>
	</#list>
      </select>            
      or 
      <input id="man_name" type="text" name="manufacturer.name"/>
      <br>
      Model:
      <select id="dev_list" onchange="copy('dev_list','dev_model')">
	<option value="---"/>
	<#list devices as d>
	<option>${d.model}</option>
	</#list>
      </select>
      or 
      <input id="dev_model" type="text" name="device.model"/>
      <br>
      <input type="submit"/>
    </form>
  </body>
</html>