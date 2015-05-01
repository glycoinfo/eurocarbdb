/**
 * Common Javascripts for ECDB
 * @fileoverview
 */

if ( typeof(ECDB) == 'undefined' ) {
/**
 * @namespace Base ECDB namespace
 */
  ECDB = {};
}

/**
 * Create an OpenID popup window for performing the login to ECDB
 *  @param  {String}    provider        Either google, yahoo or myspace
 *  @param  {URL}       return_url      Return URL for accepting an OpenID response
 *  @param  {URL}       redirect_url    URL to redirect to on successful login
 */
ECDB.PopupOpenIDFunction = function(provider,return_url,redirect_url) {
    var endpoint = null;
    var dimensions = [0,0];
    if (provider == 'google') {
        endpoint = 'https://www.google.com/accounts/o8/ud';
        dimensions = [400,400];
    }
    if (provider == 'yahoo') {
        endpoint = 'https://open.login.yahooapis.com/openid/op/auth';
        dimensions = [800,400];
    }
    if (provider == 'myspace') {
        endpoint = 'http://api.myspace.com/openid';
        dimensions = [400,400];
    }
    
    
    if (this._getHostName(return_url) == null) {
        return_url = this._getHostName(window.location+"") + return_url;
    }
    var extensions = {
        "openid.ns.ext1":"http:\/\/openid.net\/srv\/ax\/1.0",
        "openid.ext1.mode":"fetch_request",
        "openid.ext1.type.email":"http:\/\/axschema.org\/contact\/email",
        "openid.ext1.type.username":"http:\/\/axschema.org\/namePerson\/friendly",
        "openid.ext1.type.first":"http:\/\/axschema.org\/namePerson\/first",
        "openid.ext1.type.last":"http:\/\/axschema.org\/namePerson\/last",
        "openid.ext1.type.altemail":"http:\/\/schema.openid.net\/contact\/email",
        "openid.ext1.required":"email,username,first,last,altemail"
    };    
    if (endpoint != null) {
        var manager = popupManager.createPopupOpener({
            'realm': this._getHostName(return_url),
            'opEndpoint' : endpoint,
            'returnToUrl': return_url,
            'extensions': extensions,
            'shouldEncodeUrls': true,
            'onCloseHandler': (function() { if (ECDB.login_success) window.location = redirect_url; return true; })
        });
        return partial(manager.popup, dimensions[0], dimensions[1]);
    }
    log("No valid provider given to ECDB.PopupOpenIDFunction, returning a no-op function");
    return noop();
};

ECDB._getHostName = function (str) {
    var re = new RegExp('^((?:f|ht)tp(?:s)?\://[^/]+)', 'im');
    var matched = str.match(re);
    return matched ? matched[1].toString() : null;
};

/**
 * Set the default rendering scheme for ECDB. Sets the sugarImageNotation cookie
 *  @param  {String}    renderer    Renderer type to set the notation to (e.g. uoxf)
 */
ECDB.SetRenderingType = function(renderer) {

    var curr_selected = MochiKit.DOM.getElementsByTagAndClassName('*','selected',this.parentNode);
    for (var sel in curr_selected) {
        MochiKit.DOM.removeElementClass(curr_selected[sel],'selected');
    }

    MochiKit.DOM.addElementClass(this,'selected');

    ECDB._setCookie('sugarImageNotation',renderer);
    
    var all_images = $$("img[src]"); // This should be using the
                                     // selector properly to grab
                                     // the appropriate img elements
                                     // but MochiKit (1.4 at least)
                                     // is broken
    for (var image in all_images) {
        if (/get_sugar_image.action/.test(all_images[image].src)) {
            old_src = all_images[image].src;
            all_images[image].src = old_src+"&1=1";
        }
    }
    signal(ECDB,"notationchange");
};

/**
 * Get the current rendering scheme for ECDB. Returns the value of the sugarImageNotation cookie.
 *  @returns {String} current notation scheme (e.g. uoxf)
 */

ECDB.GetRenderingType = function()
{
    return ECDB._getCookie('sugarImageNotation');
};


/**
 * Helper function to watch for applet initialisation. When the applet is 
 * initialised, it will send a "appletload" signal from the ECDB object. This
 * can be used to then set the values on the applet if necessary.
 */
ECDB.InitAppletIfNotLoaded = function(applet) {
  if ( ! applet.isActive || ! applet.isActive() ) {
    ECDB.AppletLoader(applet);
    return true;
  }
  return false;
};

/**
 * Function that waits for the applet to be loaded. Polls applet for life using
 * an exponential backdown algorithm.
 * @private
 */
ECDB.AppletLoader = function(applet) {
  this.wait_time = this.wait_time? this.wait_time : 0.3;
  if (! applet.isActive || !applet.isActive()) 
  {
    callLater(this.wait_time,partial(arguments.callee,applet));
    this.wait_time = 2 * this.wait_time;
    return; 
  }
  signal(ECDB,"appletload",applet);
};

/**
 * Take the current value from inputElement, and append it using the multiple selector
 * class from the autocompleter to the targetContainer. The value of inputElement
 * is cleared, and a delete box is added to the newly generated content so that
 * the selected value can be cleared again.
 *
 *  @param  {String}    key             Keyname for the value being added
 *  @param  {Element}   inputElement    Element to read value from
 *  @param  {Element}   targetContainer Target container to generate element for
 */
ECDB.appendMultipleValues = function(key,inputElement,targetContainer) {
  var current_value = inputElement.value;
  inputElement.select();
  inputElement.value = '';

  var md = MochiKit.DOM;
  var disease_div = md.DIV({'class': 'ac_mult_sel selected_'+key});

  var deleter = md.DIV({'class': 'ac_mult_sel_delete'},'X');
  connect(deleter,'onclick', function() {
    target_div.removeChild(disease_div);
  });
  disease_div.appendChild(deleter);    

  disease_div.appendChild(md.INPUT({'type': 'hidden', 'name': key+'Search', 'value': current_value }));
  
  disease_div.appendChild(md.DIV({'class': 'ac_mult_sel_label'},current_value));
  
  targetContainer.appendChild(disease_div);  
};

/**
 * Default function called when page is loaded in ECDB. Fires the "onload"
 * signal on ECDB. Connect to the "onload" signal from ECDB if you wish to
 * execute things on page load.
 */
ECDB.windowLoadScript = function()
{
    signal(ECDB,"onload");
    ECDB._setupSectionToggles();
};




ECDB._setupSectionToggles = function()
{
    var md = MochiKit.DOM;
    var section_toggles = md.getElementsByTagAndClassName('div','section_toggle',document);
    for (var a_toggle_idx in section_toggles) {
        var a_toggle = section_toggles[a_toggle_idx];
        var section_toggle_head = md.getElementsByTagAndClassName('*','hd',a_toggle)[0];
        var section_toggle_body = md.getElementsByTagAndClassName('div','bd',a_toggle)[0];
        if ( ! section_toggle_head && ! section_toggle_body ) {
            log.error("Missing head or body element in toggle. Skipping");
            continue;
        }
        section_toggle_body.style.display = 'none';
        connect(section_toggle_head,'onclick',function() {
        if (section_toggle_body.style.display == 'none') {
          blindDown(section_toggle_body,{'duration': 0.25});
        } else {
          blindUp(section_toggle_body,{'duration': 0.25});
        }
        });        
    }
};

ECDB._setCookie = function(name,value)
{
    // set time, it's in milliseconds
    var today = new Date();
    today.setTime( today.getTime() );
    
    // expire in one month
    var expires = 30 * 1000 * 60 * 60 * 24; 
    var expires_date = new Date( today.getTime() + (expires) );
    
    // set cookie
    var cookie = name + "=" +escape( value ) + ";expires=" + expires_date.toGMTString();
    
    document.cookie = cookie;
};

ECDB._getCookie = function(name)
{
    if (document.cookie.length>0) {
        var start = document.cookie.indexOf(name + "=");
        if (start!=-1) {
            start=start + name.length+1;
            var end=document.cookie.indexOf(";",start);
            if (end==-1) end=document.cookie.length;
            return unescape(document.cookie.substring(start,end));
        }
    }
    return "";
};

/**
 * Utility function to stop the propagation of an event
 */
ECDB.stop_event = function(event) {
    if (! event) event = window.event;
    event.cancelBubble = true;
    if (event.stopPropagation) event.stopPropagation();    
}


/**
 * Get an element by a particular ID. This is needed for Internet explorer
 * where it does not parse the ID attribute for document fragments, or where
 * an appropriate doctype isn't sent with the XML response.
 *  @param  {Element}   element         Root element to search from
 *  @param  {String}    identifier      Identifier to look for
 */
ECDB.getElementByIdXML = function(element,identifier) {
	//get all the tags in the doc
	node_tags = element.getElementsByTagName('*');
	for (i=0;i<node_tags.length;i++) {
	//is there an id attribute?
		if (node_tags[i].getAttribute('id') != null) {
			//if there is, test its value
			if (node_tags[i].getAttribute('id') == identifier) {
				//and return it if it matches
				return node_tags[i];
			}
		}
	}
};

/*
 * Original file header....
 * Developed by , http://www.robertnyman.com
 * Code/licensing: http://code.google.com/p/getelementsbyclassname/
 * 
*/  

/*
The MIT License

Copyright (c) <year> Robert Nyman

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

var getElementsByClassName = function (className, tag, elm){
if (document.getElementsByClassName) {
        getElementsByClassName = function (className, tag, elm) {
                elm = elm || document;
                var elements = elm.getElementsByClassName(className),
                        nodeName = (tag)? new RegExp("\\b" + tag + "\\b", "i") : null,
                        returnElements = [],
                        current;
                for(var i=0, il=elements.length; i<il; i+=1){
                        current = elements[i];
                        if(!nodeName || nodeName.test(current.nodeName)) {
                                returnElements.push(current);
                        }
                }
                return returnElements;
        };
}
else if (document.evaluate) {
        getElementsByClassName = function (className, tag, elm) {
                tag = tag || "*";
                elm = elm || document;
                var classes = className.split(" "),
                        classesToCheck = "",
                        xhtmlNamespace = "http://www.w3.org/1999/xhtml",
                        namespaceResolver = (document.documentElement.namespaceURI === xhtmlNamespace)? xhtmlNamespace : null,
                        returnElements = [],
                        elements,
                        node;
                for(var j=0, jl=classes.length; j<jl; j+=1){
                        classesToCheck += "[contains(concat(' ', @class, ' '), ' " + classes[j] + " ')]";
                }
                try     {
                        elements = document.evaluate(".//" + tag + classesToCheck, elm, namespaceResolver, 0, null);
                }
                catch (e) {
                        elements = document.evaluate(".//" + tag + classesToCheck, elm, null, 0, null);
                }
                while ((node = elements.iterateNext())) {
                        returnElements.push(node);
                }
                return returnElements;
        };
}
else {
        getElementsByClassName = function (className, tag, elm) {
                tag = tag || "*";
                elm = elm || document;
                var classes = className.split(" "),
                        classesToCheck = [],
                        elements = (tag === "*" && elm.all)? elm.all : elm.getElementsByTagName(tag),
                        current,
                        returnElements = [],
                        match;
                for(var k=0, kl=classes.length; k<kl; k+=1){
                        classesToCheck.push(new RegExp("(^|\\s)" + classes[k] + "(\\s|$)"));
                }
                for(var l=0, ll=elements.length; l<ll; l+=1){
                        current = elements[l];
                        match = false;
                        for(var m=0, ml=classesToCheck.length; m<ml; m+=1){
                                match = classesToCheck[m].test(current.className);
                                if (!match) {
                                        break;
                                }
                        }
                        if (match) {
                                returnElements.push(current);
                        }
                }
                return returnElements;
        };
}
return getElementsByClassName(className, tag, elm);
};
