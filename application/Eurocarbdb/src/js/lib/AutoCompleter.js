/**
 * @fileoverview    Autocompleter library
 * @author          hirenj
 *
 */

if ( typeof(ECDB) == 'undefined' ) {
  ECDB = {};
}

if ( typeof(ECDB.AutoCompleter) == undefined ) {
	ECDB.Autocompleter = {};
}
/**
 * Autocompletion library for ECDB. Use the factory to
 * create an autocomplete object that can be applied to
 * any text fields that you want to apply it to. The autocompleter expects
 * an URL with no parameters, so this URL is correct
 * <pre>
 *  http://localhost:8123/autocomplete.action
 * </pre>
 * but this URL is NOT CORRECT
 * <pre>
 *  http://localhost:8123/autocompete.action?method=autocomplete
 * </pre>
 *
 * For a given url and key, the autocompleter will generate a request:
 * <pre>
 *  &lt;URL&gt;?queryType=&lt;KEY&gt;&amp;queryString=&lt;TEXT FIELD VALUE&gt;
 * </pre>
 * 
 * Note: Due to caching of the data source, we can't use any more than a 
 * single URL for AutoCompleters on a page. So, given two autocomplete objects
 * pointing to different URLs on a single page, one of them (the one created later)
 * will not function.
 *
 * <h3>Sample usage</h3>
 * @example
 *  autocomplete_maker = new ECDB.AutoCompleter('http://autocomplete_url','taxonomy_name');
 *  autocomplete_maker.apply($('myinput_element'));
 *  @param  {String}    url     URL to use as base for autocompleter
 *  @param  {String}    key     Key to use for autocompleter action
 * @constructor
 */
ECDB.Autocompleter = function(url, key) {
  this._datasource = this._getDatasource(url);  
  this._autocomplete_key = key;
};

/**
 * Return the Datasource for this autocompleter.
 * We're actually caching the data source here, so in any one page, we can't
 * use more than one data source.
 * @private
 * @returns YAHOO.util.XHRDataSource
 */
ECDB.Autocompleter.prototype._getDatasource = function(autocomplete_url) {
  if (! ECDB.Autocompleter.dataSource) {
    ECDB.Autocompleter._initDatasource(autocomplete_url);
  }
  return ECDB.Autocompleter.dataSource;
};

/**
 * Turn the given element into an autocompleter element, adding in necessary
 * DOM elements so that autocomplete results can be displayed
 *  @param  {Element}   element     Element to convert
 *  @return YAHOO.widget.AutoComplete
 */

ECDB.Autocompleter.prototype.apply = function(element) {
  var md = MochiKit.DOM;
  if ( ! element ) {
      log("No element passed, returning");
      return;
  }

  result_container = md.DIV();
  if (element.nextSibling) {
      element.parentNode.insertBefore(result_container,element.nextSibling);
  } else {
      element.parentNode.appendChild(result_container);
  }
	
  var oAC = new YAHOO.widget.AutoComplete(element, result_container, this._getDatasource());
  
  oAC.queryDelay = 0.25;
  oAC.minQueryLength = 1;
  oAC.forceSelection = true;
  
  search_key = this._autocomplete_key;
  
  // We need to bind the function to this object, so we can get access to its
  // member variable (autocomplete_key)
  
  oAC.generateRequest = bind(function(sQuery) {
    return "?queryType="+this._autocomplete_key+"&queryString=" + sQuery ;
  },this);
  
  
  oAC.formatResult = function(oResultData, sQuery, sResultMatch) {
    counter = counter + 1;
    var alts = oResultData.alternates ? " <i>"+ oResultData.alternates + "</i>" : "";
    var result_string = oResultData.name + alts;
    
    if (oResultData.supplemental != '') {
        result_string = "<span>"+result_string+'</span><span class="supplemental">'+oResultData.supplemental+"</span>";
    }
    
    if (oResultData.resultclass != '') {
        return "<div style=\"width: 100%; height: 100%;\" class=\"autocomplete_"+oResultData.resultclass+"\">"+result_string+"</div>";
    }
    return result_string;
  };
  oAC.dataRequestEvent.subscribe(function(completer,query,req) {
     addElementClass(oAC.getInputEl(),'autocomplete_loading');
  });
  oAC.dataReturnEvent.subscribe(function(completer,query,req) {
     removeElementClass(oAC.getInputEl(),'autocomplete_loading');
  });  
  oAC.containerPopulateEvent.subscribe(function(completer){
        var lis = oAC.getListEl().childNodes;
        if ( ! lis ) {
            return;
        }
        var last_class = '';
        for (var li = lis[0]; li != null; li = li.nextSibling) {
          li.className = '';
          if(li.childNodes[0] && li.childNodes[0].className) {
              var new_class = li.childNodes[0].className;
              if (last_class != new_class) {
                  last_class = new_class;
                  md.addElementClass(li,"first_of_class");
                  if (li.previousSibling) {
                      md.addElementClass(li.previousSibling,"last_of_class");
                  }
              }              
              md.addElementClass(li,new_class);
          } else {
              if (last_class != '' && li.previousSibling) {
                  md.addElementClass(li.previousSibling,"last_of_class");
                  last_class = '';                  
              }
          }
        }
      },this);
      
  oAC.resultTypeList = false;
  
  return oAC;
};

ECDB.Autocompleter._initDatasource = function(autocomplete_url) {

  var oDS = new YAHOO.util.XHRDataSource(autocomplete_url);

  oDS.responseType = YAHOO.util.XHRDataSource.TYPE_TEXT;

  oDS.responseSchema = {
      recordDelim: "\n",
      fieldDelim: "\t",
      fields: ["name", "alternates","resultclass","supplemental"]
  };
  
  oDS.maxCacheEntries = 5;

  // Don't let the responses come out of order
  
  oDS.connXhrMode = "ignoreStaleResponses";
  
  this.dataSource = oDS;
};