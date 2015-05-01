<#assign title>Instrument Page</#assign>
<#include "/template/common/header.ftl" />

<h1>Select HPLC Instrument</h1>

<p>Select your instrument setting from the list below.</p>
<p>If your instrument setting is not listed create <a href="create_instrument.action">here</a>.</p>

<!--<@ww.form>
 	
  <select name="instrumentId" size="1">	
      <#list instruments as i>
	  <option value="${i.id}">${i.manufacturer}_${i.model}</option> 
      </#list>
  </select>

<@ww.submit value="Next" name="submit" />

</@ww.form>
-->

<@ww.form>

<style type="text/css">
	tr.hplc_column_row {
		display: none;
	}
	tr.visible {
		display: none; 
	}
</style>

<select id="instrument_selector" name="instrumentId" size="1" onchange="updateDetailTable()">
  <#list instruments as c>
  <option value="${c.id}">${c.manufacturer}_${c.model}</option>
  </#list>
</select>


<@ww.submit value="Next" name="submit" />

</@ww.form>

<table id="detail_table" class="table_top_header full_width">
  <thead>
    <tr>
      <th>Manufacturer</th>
      <th>Model</th>
      <!--
      <th>Temp</th>
      <th>Solvent A</th>
      <th>Solvent B</th>
      -->
      <!--
      <th>Solvent C</th>
      <th>Solvent D</th>
      -->
      <!--
      <th>Flow Rate</th>
      <th>Flow Gradient</th>
      -->
    </tr>  
  </thead>
  <tbody id="detail_table_rows" >    
    <#list instruments as list>
    <tr id="detail_table_row_${list.id}" style="{display: none;}" >
      <td>${list.manufacturer}</td>
      <td>${list.model}</td>
      <#if list_has_next>
      </#if>
    </tr>
    </#list>
  </tbody>
</table>

<script type="text/javascript">

  function updateDetailTable() {	  

    var selected_id = document.getElementById("instrument_selector").value;
    
    rows = document.getElementById("detail_table_rows").getElementsByTagName('tr');
    for ( var i=0; i<rows.length; i++ ) {
      rows[i].style.display='none';
    }

    document.getElementById("detail_table_row_" + selected_id).style.display='table-row';
  }
	
</script>

<#include "/template/common/footer.ftl" />

