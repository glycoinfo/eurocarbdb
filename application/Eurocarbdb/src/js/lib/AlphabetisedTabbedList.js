/**
 * @fileoverview    Library to convert a regular UL list into a tab-grouped by alphabet list.
 * @author          hirenj
 */

if ( typeof(ECDB) == 'undefined' ) {
    ECDB = {};
}


if ( typeof(ECDB.AlphabetisedTabbedList) == undefined ) {
	ECDB.AlphabetisedTabbedList = {};
}

/**
 * Uses the YUI libraries to compact long lists so 
 * that they do not take up too much screen real estate. Will take a list 
 * containing li elements, and then reformat the original container element so
 * that the list is now tabbed by initial letter. By setting the maxListLength, 
 * it will set the maximum number of list elements in each tab. The algorithm will
 * also collapse tabs together if they have too few elements in them, resulting
 * in spans of character ranges (e.g. A-F and G-K).
 *
 * Use of this functionality is destructive to the document DOM. The original 
 * ul element is removed from the document and replaced with the tabbed control.
 *
 * <h3>Usage</h3>
 * @example
 * <pre>
 *      tabber = new ECDB.AlphabetisedTabbedList('list_id');
 *      tabber.maxListLength = 10;
 *      tabber.buildTabbedList();
 * </pre>
 * @param {HTMLElement} sourceList  Source List element, or id for element.
 * @constructor
 */
ECDB.AlphabetisedTabbedList = function(sourceList) {
	this.sourceList = $(sourceList);
};

ECDB.AlphabetisedTabbedList.prototype = {
	__class__: ECDB.AlphabetisedTabbedList,
	/** The source list element
	 * @type HTMLElement
	 */
	sourceList: null,
	/** The maximum number of list items in each tab
	 *	@type integer
	 */
	maxListLength: 7,
};

/* We need a better bucketing algorithm for alphabetised lists
 */
ECDB.AlphabetisedTabbedList.prototype._grouper = function(el) {
    var string = el.innerText ? el.innerText : el.textContent;
	return string.substring(0,1).toUpperCase();
};
/**
 * Build the tabbed list and insert it into the DOM, replacing the source list.
 */
ECDB.AlphabetisedTabbedList.prototype.buildTabbedList = function() {
  var tabs = new YAHOO.widget.TabView();
	var md = MochiKit.DOM;

	if ( this.sourceList == null ) {
		return;
	}
	
	var list_elements = getElementsByTagAndClassName('li',null,this.sourceList);
	
	list_elements.sort(function(a,b){
                            var aa = (a.innerText ? a.innerText : a.textContent).toUpperCase();
                            var bb = (b.innerText ? b.innerText : b.textContent).toUpperCase();
    						if ( aa > bb )
    							return 1
    						if ( aa < bb )
    							return -1
    						return 0;
    				});
	
	if (list_elements.length < this.maxListLength ) {
		return;
	}

	var target = md.DIV();

	var grouped_list_elements = groupby_as_array(list_elements,this._grouper);

	if (grouped_list_elements.length == 1) {
		return;
	}
	var active = true;
	
	var old_classname = this.sourceList.className;
	
	for (var i = 0; i < grouped_list_elements.length; i = i + 1) {
		var new_list = md.UL();
		md.addElementClass(new_list,old_classname);
		var total_children = 0;
		var label = null;
		while ( total_children < this.maxListLength && grouped_list_elements[i] != null ) {
			group_length = grouped_list_elements[i][1].length;
			if ( (total_children + group_length) < this.maxListLength ) { 
				appendChildNodes(new_list, grouped_list_elements[i][1]);
				label = label == null ? grouped_list_elements[i][0] : label + "-" + grouped_list_elements[i][0];
				total_children += group_length;
				i = i+1;
			} else if ( total_children == 0 ) {
				appendChildNodes(new_list, grouped_list_elements[i][1]);
				label = grouped_list_elements[i][0];
				total_children += group_length;
				break;
			} else {
				i = i-1;
				break;
			}
		}
		label = label.replace(/\-.*\-/,"-");
		tabs.addTab(	new YAHOO.widget.Tab({
       							label: label,
       							contentEl: new_list,
										active: active,
   								}));
		active = false;
	}

	tabs.appendTo(target);

	swapDOM(this.sourceList,target);	
	
	return target;
};