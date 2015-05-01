<#assign title>User home</#assign>
<#import "/template/lib/Eurocarb.lib.ftl" as eurocarb />

<#assign onload_function="updateDetailTable()" />
<#include "/template/common/header.ftl" />

<div class="hplc_create_form">
<h1>Select Column</h1> 
<p></p>
</div>

<div class="hplc_form">
<p>Select your column from the list below</p>
<p>If your column is not listed create <a href="create_column.action">here</a></p>


<@ww.form>
<p>
<@ww.hidden label="Instrument Id" name="instrumentId" value="${instrumentId}" type="hidden"/></p>
<@ww.hidden label="Detector Id" name="detectorId" value="${detectorId}"/></p>

<style type="text/css">
	tr.hplc_column_row {
		display: none;
	}
	tr.visible {
		display: none; 
	}
</style>

<select id="column_selector" name="columnId" size="1" onchange="updateDetailTable()">
  <#list showTypes as c>
  <option value="${c.id}">${c.manufacturer}_${c.model}</option>
  </#list>
</select>


<@ww.submit value="Next" name="submit" />

</@ww.form>

<table id="detail_table" class="table_top_header full_width">
  <thead>
    <tr>
      <th class="hplc">Manufacturer</th>
      <th class="hplc">Model</th>
      <th class="hplc">Material</th>
      <th class="hplc">Length</th>
      <th class="hplc">Width</th>
      <th class="hplc">Particle Size</th>
    </tr>  
  </thead>
  <tbody id="detail_table_rows" >    
    <#list showTypes as list>
    <tr id="detail_table_row_${list.id}" style="{display: none;}" >
      <td>${list.manufacturer}</td>
      <td>${list.model}</td>
      <td>${list.packingMaterial}</td>
      <td>${list.columnSizeLength}</td>
      <td>${list.columnSizeWidth}</td>
      <td>${list.particleSize}
      <#if list_has_next>
      </#if>
    </tr>
    </#list>
  </tbody>
</table>

<script type="text/javascript">

  function updateDetailTable() {	  

    var selected_id = document.getElementById("column_selector").value;
    
    rows = document.getElementById("detail_table_rows").getElementsByTagName('tr');
    for ( var i=0; i<rows.length; i++ ) {
      rows[i].style.display='none';
    }

    document.getElementById("detail_table_row_" + selected_id).style.display='table-row';
  }
	
</script>


<#include "/template/common/footer.ftl" />

