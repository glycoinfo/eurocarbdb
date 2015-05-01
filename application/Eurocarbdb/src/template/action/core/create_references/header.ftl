
<script>

  function select_reftype() {
    var refType = document.getElementById('refType').value;

    var tr_items = document.getElementById('frmInput').getElementsByTagName("tr");
    for( var i=0; i<tr_items.length; i++ ) {
        var item_reftype = tr_items[i].getAttribute('refType');
	if( item_reftype!=null && item_reftype!=refType ) {
	  tr_items[i].style.display = "none";
	}
	else {  
	  tr_items[i].style.display = "table-row";
	}
    }
  }
  
</script>

<#if ( references?exists && references?size > 0 )>
<p>Current references</p>
<ul>
  <#list references as r>
  <li>
    <@ecdb.reference ref=r/>
  </li>
  </#list>
</ul>
<#else/>
<p>
  There are currently no references selected.
</p>
</#if>

<p>Specify the details of a reference to be added:</p>