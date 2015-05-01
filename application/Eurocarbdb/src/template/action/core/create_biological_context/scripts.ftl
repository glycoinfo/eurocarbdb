
<script>      
       var xmlHttp;         
       var search_type;
  
       function resetAll() {
            reset('taxonomy');
            reset('tissue');
            reset('disease');
            reset('perturbation');
       }

       function showSearch(id) {
            resetAll();
	    if( document.getElementById(id+'_value') )  {
	       document.getElementById(id+'_value').style.display = 'none';
	    }
	    else {
	       document.getElementById(id+'_list_add').style.display = 'none';
	    }
	    document.getElementById(id+'_search').style.display = 'inline';
       }

       function reset(id) {
	    if( document.getElementById(id+'_value') )  {
	       document.getElementById(id+'_value').style.display = 'inline';
	    }
	    else {
	       document.getElementById(id+'_list_add').style.display = 'inline';
	    }
	    document.getElementById(id+'_search').style.display = 'none';
	    document.getElementById(id+'_results').style.display = 'none';
	    document.getElementById(id+'_nav').style.display = 'none';
       }
   
       function search(id) {
            search_type = id;
            var name = document.getElementById(search_type+'_search_input').value
	    
	    xmlHttp=GetXmlHttpObject();
	    if (xmlHttp==null) {
	       alert ("Your browser does not support AJAX!");
	       return;
	    } 

	    var url="";
	    if( id=='taxonomy' ) {
	       url = "search_taxonomy.action?showList=1&taxonomyName=" + name;
	    }
	    else if( id=='tissue' ) {
	       url = "search_tissue_taxonomy.action?showList=1&tissueTaxonomyName=" + name;
	    }
	    else if( id=='disease' ) {
	       url = "search_disease.action?showList=1&diseaseName=" + name;
	    }
	    else if( id=='perturbation' ) {
	       url = "search_perturbation.action?showList=1&perturbationName=" + name;
	    }
	    	   
	    document.body.style.cursor = 'wait';
	    xmlHttp.onreadystatechange=stateChanged;
	    xmlHttp.open("GET",url,true);
	    xmlHttp.send(null);
	}

	function select(id,text) {
	    document.getElementById(id+'_value_text').innerHTML = text;		      
	    document.getElementById(id+'_value_input').value = text;
	}

	function add(id,text) {
	    var list = document.getElementById(id+'_list_list');
	    var list_items = list.getElementsByTagName("li");

	    var li_id = list_items.length;
	    var html = text + ' <input type="hidden" name="' + id + 'Search" value="' + text + '"/><span class="clickable" onclick="removeResult(\'' + id + '\',' + li_id + ')">(delete)</span>';
	    list_item = document.createElement("li");
	    list_item.innerHTML = html;
	    list.appendChild(list_item);
	    
	    document.getElementById(id+'_list_list').style.display = 'inline';
	    document.getElementById(id+'_list_empty').style.display = 'none';
	}

	function initResults(id) {
	    var li_items = document.getElementById(search_type + "_results").getElementsByTagName("li");
	    for( var i=0; i<li_items.length; i++ ) {
	        if( i>=20 ) {
		   li_items[i].style.display = 'none';
		}
		else {
		   li_items[i].style.display = 'block';
		}

	        li_items[i].item_type = id;		
		li_items[i].onclick = function() {
		   // react to selection on the search result
		   var id = this.item_type;
		   var text = this.innerHTML;
		   if( document.getElementById(id+'_value') )  {
		      select(id,text);		
		   }
		   else {
		      add(id,text);
		   }
		   reset(id);
		}
	    }	    
	}

	function removeResult(id,li_id) {
	   var list = document.getElementById(id+'_list_list');
	   var list_items = list.getElementsByTagName("li");

	   list.removeChild(list_items[li_id]);
	   if( list_items.length==0 ) {
	      document.getElementById(id+'_list_list').style.display = 'none';
	      document.getElementById(id+'_list_empty').style.display = 'inline';
	   }
	}

	function showPreviousResults(id) {
	    var start = findFirstResult(id);
	    if( start-20>=0 ) {
	        showResults(id,start-20,20);
            }
	}

	function showNextResults(id) {
	    var start = findFirstResult(id);
	    var li_count = document.getElementById(id + "_results").getElementsByTagName("li").length;
	    if( (start+20)<li_count ) {
	        showResults(id,start+20,20);
	    }
	}

	function findFirstResult(id) {
	    var li_items = document.getElementById(id + "_results").getElementsByTagName("li");
	    for( var i=0; i<li_items.length; i++ ) {
	        if( li_items[i].style.display!='none' ) {
		   return i;
		}
	    }
	    return 0;
	}
	
	function showResults(id,start,count) {
	   var li_items = document.getElementById(id + "_results").getElementsByTagName("li");
	   for( var i=0; i<li_items.length; i++ ) {
	      if( i>=start && i<start+count ) {
	         li_items[i].style.display='block';
	      }
	      else {
	         li_items[i].style.display='none';
	      }
	   }
	}

	function stateChanged() 
	{ 
	    if( xmlHttp.readyState==4 ) { 
	       var div_results = document.getElementById(search_type + "_results");

	       document.body.style.cursor = 'default';
	       div_results.innerHTML=xmlHttp.responseText;
	       if( div_results.getElementsByTagName("li").length>0 ) {	 
	          initResults(search_type);
		  if( div_results.getElementsByTagName("li").length>20 ) {	 	      
		     document.getElementById(search_type + '_nav').style.display = 'block';
		  }
	       }
	       else {
	          div_results.innerHTML="no matches";	        
		  document.getElementById(search_type + '_nav').style.display = 'none';
	       }
	       div_results.style.display = 'block';
	    }
	}

</script>