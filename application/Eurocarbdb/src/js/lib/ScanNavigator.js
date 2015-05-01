/**
 * @fileoverview    Library for creating a Navigator widget for scans from a list
 */

if ( typeof(ECDB) == 'undefined' ) {
  ECDB = {};
}

if ( typeof(ECDB.ScanNavigator) == undefined ) {
	ECDB.ScanNavigator = {};
}

/**
 *  Create a ScanNavigator, converting a UL with a list of scans (and nested ULs with sub-scans), into
 *  a tree widget to navigate all the scans.
 *
 *  @param  {Element}   targetElement       Target UL element to convert to a scan navigator
 *  @param  {String}    detail_id_prefix    Prefix for the identifiers for the detailed scan elements
 *
 *  @requires           YAHOO.widget.TreeView
 *  @constructor
 */
ECDB.ScanNavigator = function( targetElement, detail_id_prefix ) {
  if (targetElement == null) {
    return;
  }
    
  if ( ! YAHOO || ! YAHOO.widget || ! YAHOO.widget.TreeView ) {
      log("Missing YAHOO libraries");
      return;
  }
  
  if (detail_id_prefix == null) {
      detail_id_prefix = 'scan_detail_';
  }
  
  addElementClass(targetElement,'scan_navigator');
  
  treeview = new YAHOO.widget.TreeView(targetElement);


  treeview.singleNodeHighlight = true;
  treeview.subscribe("clickEvent",treeview.onEventToggleHighlight);
  treeview.expandAll();
  treeview.render();
  treeview.subscribe("highlightEvent", function(node) {
    var nodes = [node];
    for (var a_node_idx in nodes) {
      var a_node = nodes[a_node_idx];
      if (a_node == null) {
        continue;
      }
      var scan_det_id = getElementsByTagAndClassName( '*',
                                    'scan_link',
                                    a_node.getContentEl()
                                  )[0].id.match(/\d+/);
      var scan_detail = $(detail_id_prefix+scan_det_id);
      if (scan_detail != null) {
        if (a_node.highlightState == 1) {
          showElement(scan_detail);
        } else {
          hideElement(scan_detail);
        }        
      }
      if ( ! this.detail_container ) {
          this.detail_container = scan_detail.parentNode;
          addElementClass(this.detail_container,'scan_navigator_detail_container');          
          this.detail_container.parentNode.style.position = 'relative';
      }
    }
  });
  if (treeview.getNodeByIndex(1) != null) {
    treeview.getNodeByIndex(1).toggleHighlight();
  }
};
