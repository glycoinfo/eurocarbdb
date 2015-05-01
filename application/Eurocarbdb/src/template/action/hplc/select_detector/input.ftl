<#assign title>Detector Page</#assign>
<#include "/template/common/header.ftl" />

<h1>Select Detector</h1>

<p>Select your detector from the list below</p>
<p>If your detector is not listed create <a href="create_detector.action">here</a></p>

<!--
<@ww.form>

  <select name="detectorId" size="1">	
      <#list detectors as d>
	  <option value="${d.id}">${d.manufacturer}_${d.model}</option> 
      </#list>
  </select>

<@ww.submit value="Next" name="submit" />

</@ww.form>
-->

<@ww.form>
<p>

<style type="text/css">
	tr.hplc_column_row {
		display: none;
	}
	tr.visible {
		display: none; 
	}
</style>

<select id="detector_selector" name="detectorId" size="1" onchange="updateDetailTable()">
  <#list detectors as c>
  <option value="${c.id}">${c.manufacturer}_${c.model}</option>
  </#list>
</select>


<@ww.submit value="Next" name="submit" />

</@ww.form>

<!--
<table id="detail_table" class="table_top_header full_width">
  <thead>
    <tr>
      <th class="hplc">Manufacturer</th>
      <th class="hplc">Model</th>
      <th class="hplc">Excitation</th>
      <th class="hplc">Emission</th>
      <th class="hplc">Bandwidth</th>
      <th class="hplc">Sampling Rate</th>
    </tr>  
  </thead>
  <tbody id="detail_table_rows" >    
    <#list detectors as list>
    <tr id="detail_table_row_${list.id}" style="{display: none;}" >
      <td>${list.manufacturer}</td>
      <td>${list.model}</td>
      <td>${list.excitation}</td>
      <td>${list.emission}</td>
      <td>${list.bandwidth}</td>
      <td>${list.samplingRate}
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

-->


<#include "/template/common/footer.ftl" />

