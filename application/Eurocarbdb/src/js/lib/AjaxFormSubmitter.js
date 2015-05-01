/** 
 * @fileoverview AJAX form submission library for ECDB
 * @author hirenj
 * @version
 */

if ( typeof(ECDB) == 'undefined' ) {
  ECDB = {};
}

if ( typeof(ECDB.AjaxFormSubmitter) == 'undefined' ) {
	ECDB.AjaxFormSubmitter = {};
}

/**
 *
 * <p>AJAX form submission library for ECDB. Use this class for execution of 
 * actions without triggering a page refresh. This is important for presenting
 * complex workflows to users, without having to track dependent variables
 * across pages, without having to resort to hidden input fields.</p>
 *
 * <p>There are two patterns of usage of this class, either presenting a 
 * window/panel that the data manipulation can occur on, or using an inline 
 * method to present the results from the AJAX request.</p>
 *
 * <p>The basic mechanism behind the form submitter is taking elements that can
 * submit forms - A and FORM elements, and then reconnecting the events so 
 * that the AjaxFormSubmitter controls the workflow of the action. The results
 * from the action are inserted back into the document flow, and any window
 * load scripts are executed.</p>
 *
 * <h2>Typical Action page format</h2>
 * <p>The facility of this utility is totally dependent on the ability for
 * actions to produce pages that are well formed XML. If the pages are not 
 * returned as well formed XML, the data cannot be retrieved from the 
 * asynchronous request, and the execution will be halted. Practically, this 
 * means that all elements should be closed, and that characters like the 
 * ampersand (&) should be escaped (&amp;).</p>
 * 
 * <p>Error conditions should be indicated in actions using actionErrors within
 * the WebWork framework. In the presence of errors, a div with the id of 'errors'
 * is found within the document. This will trigger the Form submitter to fire
 * an onerror event.</p>
 * 
 * <h2>Running actions where the response is important</h2>
 * <p>For actions where you need to display an input page, and then accept 
 * user input to trigger a second dependent action (such as when you have a form
 * that is filled in by a user, and then submitted to the server), possibly looping
 * through this procedure multiple times, the best option is to use a Panel based
 * interface for the execution of the actions. As the submitter does not know 
 * which elements to display from the response, you must supply an id for the 
 * element that contains the resultant data. As you may need to optionally perform
 * transforms on the resultant data, a data transformer method callback is also
 * supplied.</p>
 *
 * <h2>Running actions where the response is not important</h2>
 * <p>When you wish to run an action, but only wish to display error conditions to the
 * user, it is only necessary to connect the submitter to a target URL. No target
 * element is required, as the success signal will be sent as long as an error
 * condition is not observed.</p>
 *
 * <h2>Choice between Panel and Inline display</h2>
 * <p>When the panel interface is used, actions can be cancelled by clicking on the 
 * close box in the panel, while inline displays must finish executing. The final 
 * step in an execution workflow for an inline action must be for it to return 
 * a static page.</p>
 *
 * <h2>Data Converters</h2>
 * <p>Data converters are required to import the XML nodes from the response document
 * coming back from the XML HTTP Request, and import it into the target document.</p>
 * 
 * <h2>Scripts in the response</h2>
 * <p>If the response data requires scripts to be run when the page is loaded, as long
 * as the script elements are contained in the imported data, the scripts will be
 * executed. Internet Explorer has some poor behaviour with respect to the execution
 * of scripts, so it is recommended that at the end of your script segments, a 
 * bare javascript statement is added:</p>
 * <pre>
 * ECDB.NEED_TO_LOAD_SCRIPTS = true;
 *
 * ECDB.onload = function() { };
 * // Other stuff done to prepare the page onload
 *
 * ECDB.NEED_TO_LOAD_SCRIPTS = false;
 * </pre>
 *
 * <p>Setting the ECDB.NEED_TO_LOAD_SCRIPTS flag to false will ensure that the
 * imported scripts are not executed twice.</p>
 *
 * <p>Since the recommended method for executing javascript on page loads for
 * EUROCarbDB is to attach a function to the onload method using the MochiKit
 * signal and slot methodology, to ensure maximum compatibility with other 
 * existing actions, this same method should be used to set events up for the
 * response page. E.g.</p>
 *
 * <h3>Response page</h3>
 * <pre>
 *  &lt;form id="response_form"&gt;
 *  &lt;script&gt;
 *
 *     ECDB.NEED_TO_LOAD_SCRIPTS = true;
 *     connect(ECDB,'appletload',function() {
 *          doThisStuff();
 *          doThisOnAppletLoad();
 *      });
 *     ECDB.NEED_TO_LOAD_SCRIPTS = false;
 *
 *  &lt;/script&gt;
 *  &lt;/form&gt;
 * </pre>
 *
 * <p>This will execute the doThisStuff and doThisOnPageLoad methods when the
 * response document is inserted into the document flow.</p>
 *
 * <h2>Error Conditions</h2>
 * <p>In the case that the returning action has some errors associated with it,
 * the submitter will not send a success signal, and instead display the error
 * messages alongside the form.</p>
 *
 * <h2>Sample Usage</h2>
 * <pre>
 *   // Note, the function $() is equivalent to document.getElementById()
 *
 *   // Simple Panel on a single form
 *   ECDB.AjaxFormSubmitter.PanelBasedFactory($('target_form'),'response_form_name');
 *
 *   // Simple Panel on a single form where the response is unimportant
 *   ECDB.AjaxFormSubmitter.PanelBasedFactory($('target_form'));
 *
 *   // Similarly, instead of passing the form element found with id 'target_form'
 *   // you can pass a reference to an A element, to capture the onclick event
 *   // for that element
 *   ECDB.AjaxFormSubmitter.PanelBasedFactory($('target_link'));
 *
 * </pre>
 * @param target_element    Target element to convert to make into an ajax event
 * @param data_converter    Function that will convert and import incoming data nodes
 * @requires MochiKit
 * @requires YUI::Panel
 * @constructor
 */
ECDB.AjaxFormSubmitter = function(target_element,data_converter)
{
    this._target_form = target_element;
    this._data_converter = data_converter;
    if (target_element.nodeName.toUpperCase() == 'A') {
        this._makeAjaxLink(target_element);
        return;
    } else if (target_element.nodeName.toUpperCase() == 'FORM') {
        for ( var i = 0; i < target_element.elements.length; i++ ) {
            if (target_element.elements[i].type == 'file') {
                this._makeAjaxFileUpload(target_element);
                return;
            }
        }
        this._makeAjaxForm(target_element);
        return;
    }
    log("Not given a link or form element, assuming this is a display only request");
};
/**
 * <p>Factory for AjaxFormSubmitters so that triggering the submission on the given targetElement
 * will retrieve the data from the remote server, looking for data with an id of responseNode, 
 * placing the results in the displayContainer element.</p>
 * <h2>Usage</h2>
 * <pre>
 * &lt;div id="response_container"&gt;
 *     &lt;a id="ajax_link" href="foo.action?param1=value1&amp;param2=value2"&gt; Run AJAX action &lt;/a&gt;
 * &lt;div&gt;
 *
 * &lt;script&gt;
 *     ECDB.AjaxFormSubmitter.InlineBasedFactory(document.getElementById('response_container'),document.getElementById('ajax_link'),'my_response');
 * &lt;/script&gt;
 * </pre>
 * @param  {Element} targetElement       A or FORM element that will be calling a server-side action.
 * @param  {String} responseNode        The id of the node that the response data can be found at
 * @param  {Element} displayContainer    Location to place resultant data. Defaults to parent of targetElement.
 * @return  {AjaxFormSubmitter[]}       Array of AjaxFormSubmitters created
 */
ECDB.AjaxFormSubmitter.InlineBasedFactory = function(displayContainer,targetElement,responseNode)
{
    var elements = [targetElement];
    if (typeof(targetElement) == 'string') {
        elements = getElementsByTagAndClassName('*',targetElement);        
    }
    var data_importer = null;
    if (responseNode != null) {
        data_importer = partial(ECDB.AjaxFormSubmitter.FormFragmentImporter,responseNode);
    } else {
        data_importer = partial(ECDB.AjaxFormSubmitter.SignalSuccessImporter,null);
    }
    var submitters = [];
    for (var i = 0; i < elements.length; i++) {        
        var submitter = new ECDB.AjaxFormSubmitter(elements[i], data_importer);
        var display_element = displayContainer ? displayContainer :  elements[i].parentNode;
        
        submitter.addInlineDisplay(display_element);
        submitters.push(submitter);
    }
    return submitters;    
}
/**
 * <p>Factory for AjaxFormSubmitters so that triggering the submission on the given targetElement
 * will retrieve the data from the remote server, looking for a form with an id of responseNode, 
 * placing the results in a panel.</p>
 * <h2>Usage</h2>
 * <pre>
 * &lt;a id="ajax_link" href="foo.action?param1=value1&amp;param2=value2"&gt; Run AJAX action &lt;/a&gt;
 *
 * &lt;script&gt;
 *     ECDB.AjaxFormSubmitter.PanelBasedFactory(document.getElementById('ajax_link'),'my_response');
 * &lt;/script&gt;
 * </pre>
 * @param  {Element} targetElement       A or FORM element that will be calling a server-side action.
 * @param  {String} responseNode        The id of the node that the response data can be found at
 * @return  {AjaxFormSubmitter[]}       Array of AjaxFormSubmitters created
 */
ECDB.AjaxFormSubmitter.PanelBasedFactory = function(targetElement,responseNode)
{
    var elements = [targetElement];
    if (typeof(targetElement) == 'string') {
        elements = getElementsByTagAndClassName('*',targetElement);        
    }
    var data_importer = null;
    if (responseNode != null) {
        data_importer = partial(ECDB.AjaxFormSubmitter.FormImporter,responseNode);
    } else {
        data_importer = partial(ECDB.AjaxFormSubmitter.SignalSuccessImporter,null);
    }
    var submitters = [];    
    for (var i = 0; i < elements.length; i++) {
        var submitter = new ECDB.AjaxFormSubmitter(elements[i], data_importer);
        submitter.addPanelDisplay(document.body);
        connect(submitter,"success",function() { window.location.reload(); });
        submitters.push(submitter);
    }
    return submitters;
};
/**
 * <p>Factory for AjaxFormSubmitters so that triggering the submission on the given targetElement
 * will retrieve the data from the remote server, looking for an HTML element with an id of responseNode, 
 * placing the results in a panel.</p>
 * <h2>Usage</h2>
 * <pre>
 * &lt;a id="ajax_link" href="foo.action?param1=value1&amp;param2=value2"&gt; Run AJAX action &lt;/a&gt;
 *
 * &lt;script&gt;
 *     ECDB.AjaxFormSubmitter.PanelBasedFactory(document.getElementById('ajax_link'),'my_response');
 * &lt;/script&gt;
 * </pre>
 * @param  {Element} targetElement       A or FORM element that will be calling a server-side action.
 * @param  {String} responseNode        The id of the node that the response data can be found at
 */
ECDB.AjaxFormSubmitter.PanelBasedSubPageFactory = function(targetElement,responseNode)
{
    var elements = [targetElement];
    if (typeof(targetElement) == 'string') {
        elements = getElementsByTagAndClassName('*',targetElement);        
    }
    var data_importer = null;
    if (responseNode != null) {
        data_importer = partial(ECDB.AjaxFormSubmitter.FormFragmentImporter,responseNode);
    } else {
        data_importer = partial(ECDB.AjaxFormSubmitter.SignalSuccessImporter,null);
    }
    var submitters = [];
    for (var i = 0; i < elements.length; i++) {
        var submitter = new ECDB.AjaxFormSubmitter(elements[i], data_importer);
        submitter.addPanelDisplay(document.body);
        connect(submitter,"success",function() { window.location.reload(); });        
        submitters.push(submitter);
    }
    return submitters;
}
/**
 * Creates a function that can be statically called so that it halts the 
 * execution of the event, and replaces it instead with an ajax execution.
 * This is useful when third party libraries (such as the TreeView from YUI)
 * cause elements to be unfindable (e.g. through getElementsByTagName) and you
 * still want to bind this action to the element.
 * <h3>Usage</h3>
 * <pre>
 * &lt;a href="some.action?params=foo&bar" onclick="return ECDB.MY_CUSTOM_FUNCTION(event);"&gt;Click to do this&lt;/a&gt;
 *
 * &lt;script&gt;
 * ECDB.MY_CUSTOM_FUNCTION = ECDB.AjaxFormSubmitter.InlineEvent(    ECDB.AjaxFormSubmitter.PanelBasedFactory,
 *                                                                  'response_form_name',
 *                                                                  function(target) {
 *                                                                      alert("I am in the onclick for "+target.href);
 *                                                                  });
 *
 * &lt;/script&gt;
 * </pre>
 *
 * @param   {Function}  factoryMethod   Factory method for creation of 
 *                                      AjaxFormSubmitter. This can be either 
 *                                      the bare function, or a partially
 *                                      bound function (useful for the 
 *                                      InlineBasedFactory).
 * @param   {Elemnent}  responseNode    Node to look for in the response XML
 * @param   {Function}  callback        Callback to apply to the node if you
 *                                      still want custom code to execute
 *                                      when this handler is fired. The element
 *                                      this method is attached to is the argument
 *                                      to this method.
 */
ECDB.AjaxFormSubmitter.InlineEvent = function(factoryMethod,responseNode,callback)
{
    return function(event) {
        if (! event ) {
            event = window.event;
        }
        var targ = event.target || event.srcElement;

        if (callback != null && typeof(callback) == 'function')
        {
            if (! bind(callback,targ)()) {
                return false;
            }
        }

        if ( ! targ._submitter ) {
            targ._submitter = factoryMethod(targ,responseNode);
        }
    
        signal(targ,"ajaxsubmit");
        return false;
  };
};

ECDB.AjaxFormSubmitter.prototype.repr = function()
{
    return "AjaxFormSubmitter [ "+this._target_form.action+(this._panel ? " Panel " : "")+" ]";
};

/**
 * Add an inline display output for this submitter. This will insert the results
 * from a query into the existing document flow. The content of the targetElement
 * will be replaced by the data coming from the AJAX request.
 * @param   {Element}   targetElement   Target element to place results in
 */

ECDB.AjaxFormSubmitter.prototype.addInlineDisplay = function(targetElement)
{

    connect(this,"onsubmit", function() {
        var loading_div = MochiKit.DOM.DIV({'class': 'ajax_loading'});
        replaceChildNodes(targetElement,loading_div);
    } );
    
    connect(this,"ondata", function() {
        ECDB.NEED_TO_LOAD_SCRIPTS = true;
        disconnectAll(ECDB,'onload');
        replaceChildNodes(targetElement,this.data);
        if (ECDB.NEED_TO_LOAD_SCRIPTS) {
            var scripts = target_element.getElementsByTagName('script');
            for (var i = 0; i < scripts.length; i++) {
                eval(scripts[i].innerHTML);
            }
        }
        signal(ECDB,'onload');
    });
    
    connect(this,"onerror", function(data) {
        log("There was an error during form submission");
    });
    
};

/**
 * Add a panel display output for this submitter. This will insert a YUI Panel
 * into the document flow under the parentElement.
 * @param   {Element}   parentElement   Target element to place the panel under
 */
ECDB.AjaxFormSubmitter.prototype.addPanelDisplay = function(parentElement)
{
    var panel = new YAHOO.widget.Panel( "xhr"+(new Date()).getTime(), {
        draggable: false,
        width: '100%',
        height: '100%',
        modal: true,
        zindex: 4,
        visible: false,
        close: true,
        underlay: "shadow",
        constraintoviewport: true,
        fixedcenter: "contained",
    });
    panel.setBody(MochiKit.DOM.DIV());
    panel.render(parentElement);
    connect(this,"onsubmit", function() {
        var loading_div = MochiKit.DOM.DIV({'class': 'ajax_loading'});
        panel.setBody(loading_div);
        panel.body.style.height = '100%';
        panel.center();
        panel.show();
    } );
    connect(this,"ondata", function() {
        ECDB.NEED_TO_LOAD_SCRIPTS = true;
        disconnectAll(ECDB,'onload');
        replaceChildNodes(panel.body,this.data);
        panel.body.style.background = '#ffffff';
        panel.body.style.height = '100%';
        panel.body.style.overflow = 'auto';
        panel.center();
        if (ECDB.NEED_TO_LOAD_SCRIPTS) {
            var scripts = panel.body.getElementsByTagName('script');
            for (var i = 0; i < scripts.length; i++) {
                eval(scripts[i].innerHTML);
            }
        }
        signal(ECDB,'onload');
    });
    
    connect(this,"success",function() {
        panel.hide();
        panel.body = MochiKit.DOM.DIV();        
    });
    
    connect(this,"onerror", function(data) {
        log("There was an error during form submission");
    });
    
    this._panel = panel;
    
    return panel;
};

/**
 * Data importer method. Looks for form elements in the returned xml data, 
 * starting from the fragment_id, and instantiates more form submitters to
 * capture any other form submission from this page, and turn them into AJAX
 * requests.
 */
ECDB.AjaxFormSubmitter.FormFragmentImporter = function(fragment_id,xml_data)
{
    var container_div = bind(ECDB.AjaxFormSubmitter.FormImporter,this)(fragment_id,xml_data);
    var forms = container_div.getElementsByTagName('form');
    for (var i = 0; i < forms.length; i++) {
        var new_submitter = new ECDB.AjaxFormSubmitter(forms[i], partial(ECDB.AjaxFormSubmitter.SignalSuccessImporter,this));
        connect(new_submitter,"onsubmit",bind(function() {
            signal(this,"onsubmit");
        },this));        
    }
    return container_div;
};

/**
 * Data importer method. When a form element is found in the received data, 
 * get it out of the DOM, and then attach a new AjaxFormSubmitter to it that
 * will trigger this object to signal a success if there are no errors on the
 * submission of that form
 */
ECDB.AjaxFormSubmitter.FormImporter = function(form_name,xml_data)
{
    var target_node = xml_data.getElementById? xml_data.getElementById(form_name) : ECDB.getElementByIdXML(xml_data,form_name);
    var imported_node = null;

    var error_el = this._hasError(xml_data);
    
    if (target_node == null) {
        if (error_el != null) {
          return this._importNode(error_el);
        } else {
          return MochiKit.DOM.DIV({});
        }
    }

    var new_id = form_name+"_"+((new Date()).getTime());

    if (target_node.id) {
        target_node.id = new_id;
    } else {
        target_node.setAttribute('id', new_id);      
    }
    var imported_node = this._importNode(target_node);
    var container = imported_node;
    if (error_el != null) {
        var imported_error = this._importNode(error_el);
        container = MochiKit.DOM.DIV({});
        container.appendChild(imported_error);
        container.appendChild(imported_node);
    }

    var new_submitter = new ECDB.AjaxFormSubmitter(imported_node, partial(ECDB.AjaxFormSubmitter.SignalSuccessImporter,this));
    connect(new_submitter,"onsubmit",bind(function() {
        signal(this,"onsubmit");
    },this));
    return container;
};

/**
 * Data importer method. When attached to an existing form that has been opened
 * by another AjaxFormSubmitter, it will check for any errors, and if any are
 * found, signal to the original submitter that there is new data, appending
 * the error element.
 * If the action is successful, it signals a success, (which will trigger a 
 * close on panel based AjaxFormSubmitters).
 */
ECDB.AjaxFormSubmitter.SignalSuccessImporter = function(last_submitter, xml_data)
{
    log("Checking for the presence of ECDB errors");
    var error_element = this._hasError(xml_data);
    if (error_element != null)
    {
        log("Have found ECDB errors");
        if (last_submitter == null) {
            return this._importNode(error_element);
        }
        if (last_submitter._data_converter != null) {
            last_submitter.data = last_submitter._data_converter(xml_data);
        } else {
            last_submitter.data = xml_data;
        }
        var new_data = MochiKit.DOM.DIV();

        new_data.appendChild(this._importNode(error_element));
        
        if (last_submitter.data != null) {
            new_data.appendChild(last_submitter.data);
        }
        
        last_submitter.data = new_data;
        signal(last_submitter,"ondata");
    } else {
        log("No ECDB errors found");

        if (last_submitter) {
            if (last_submitter._data_converter != null) {
                log("Running the data converter over this");
                last_submitter.data = last_submitter._data_converter(xml_data);
            } else {
                last_submitter.data = xml_data;
            }
        }
        
        if (last_submitter) {
            signal(last_submitter,"ondata");
            signal(last_submitter,"success");
        }
        
        signal(this,"success");
        
    }
};

ECDB.AjaxFormSubmitter.prototype._hasError = function(xml_data)
{
    return xml_data.getElementById? xml_data.getElementById('errors') : ECDB.getElementByIdXML(xml_data,'errors');
};

ECDB.AjaxFormSubmitter.prototype._importNode = function(external_node)
{
    if ( document.importNode ) {
        return document.importNode(external_node,true);
    } else {
        var new_data = MochiKit.DOM.DIV();
        new_data.innerHTML = external_node.xml;
        return new_data.firstChild;
    }    
};

ECDB.AjaxFormSubmitter.prototype._makeAjaxLink = function(target_link)
{
    connect(target_link,'ajaxsubmit',this,partial(this._convertLinkToXHR,target_link));
    /**
     * @ignore
     */
    target_link.onclick = function() {
        signal(target_link,'ajaxsubmit');
        return false;
    };
};

ECDB.AjaxFormSubmitter.prototype._makeAjaxForm = function(target_form)
{
    connect(target_form,'ajaxsubmit',this,partial(this._convertFormToXHR,target_form));
    /**
     * @ignore
     */
    target_form.onsubmit = function() { 
        return false;
    };
    for (var i = 0; i < target_form.elements.length; i++) {
        if (target_form.elements[i].type == 'submit') {
            /**
             * @ignore
             */
            target_form.elements[i].onclick = function() {
                signal(this,'ajaxonclick');
                signal(target_form,'ajaxsubmit');
                return false;                
            };
        }
    }
};

ECDB.AjaxFormSubmitter.prototype._makeAjaxFileUpload = function(target_form)
{
    connect(target_form,'ajaxsubmit',this,partial(this._convertUploadToXHR,target_form));
    /**
     * @ignore
     */
    target_form.onsubmit = function() { 
        signal(target_form,'ajaxsubmit');
        return false;
    };
    for (var i = 0; i < target_form.elements.length; i++) {
        if (target_form.elements[i].type == 'submit') {
            /**
             * @ignore
             */
            target_form.elements[i].onclick = function(event) {
                ECDB.stop_event(event);
                signal(target_form,'ajaxsubmit');
                return false;                
            };
        }
    }
};

ECDB.AjaxFormSubmitter.prototype._convertUploadToXHR = function(target_form) {
    YAHOO.util.Connect.setForm(target_form, true);
    var myself = this;
    var uploadHandler = {
      upload: function(o) {
        myself._receivedData(o);
      },
      failure: function(o) {
        myself._errorOccurred(o);
      }
    };
    YAHOO.util.Connect.asyncRequest('POST', target_form.action, uploadHandler);
    signal(this,"onsubmit");
    return false;
}

ECDB.AjaxFormSubmitter.prototype._convertLinkToXHR = function(target_link) {
    var end_href = target_link.href;
    end_href += "&timestamp="+(new Date()).getTime();
    var deferred = doXHR(end_href, { mimeType: 'text/xml' });
    deferred.addCallbacks(bind(this._receivedData,this),bind(this._errorOccurred,this));
	signal(this,"onsubmit");
	return false;    
};

ECDB.AjaxFormSubmitter.prototype._convertFormToXHR = function(target_form) {
    var toSend = [];
    toSend['timestamp'] = (new Date()).getTime()+"";
	for( var i = 0; i < target_form.elements.length; i++) {	    
	    if (target_form.elements[i].type == "checkbox" && target_form.elements[i].checked == false) {
	        continue;
	    }
	    if (toSend[target_form.elements[i].name] != null) {
	        curr_value = toSend[target_form.elements[i].name];
	        if (! curr_value.push ) {
	            curr_value = [curr_value];
            }
	        curr_value.push(target_form.elements[i].value);
	        toSend[target_form.elements[i].name] = curr_value;	        
	    } else {
	        toSend[target_form.elements[i].name] = target_form.elements[i].value;
        }
	}
	var deferred = doXHR(target_form.action+"?"+queryString(toSend), { mimeType: 'text/xml' });
    deferred.addCallbacks(bind(this._receivedData,this),bind(this._errorOccurred,this));
	signal(this,"onsubmit");
	return false;
};

ECDB.AjaxFormSubmitter.prototype._receivedData = function(request)
{
    if (request.responseXML == null) {
        log("There was an error with the ajax request");
        signal(this,"onerror");
        return;
    }
    
    var returnedData = request.responseXML;
    
    if(document.all) {
        var doc=new ActiveXObject("Microsoft.XMLDOM");
        doc.async="false";
        var responseText = request.responseText+"";
        // We need to add in the DOCTYPE to the responseText, when dealing with
        // Internet Explorer.
        responseText = responseText.replace(/<!DOCTYPE.*">$/gm,'<?xml version="1.0"?>');
        doc.loadXML(responseText);
        returnedData = doc;
    }


    
    if (this._data_converter != null) {
        var converted_data = this._data_converter(returnedData)
        this.data = converted_data ? converted_data : this.data;
    } else {
        this.data = returnedData;
    }
    signal(this,"ondata");
};

ECDB.AjaxFormSubmitter.prototype._errorOccurred = function(request)
{
    signal(this,"onerror");
};