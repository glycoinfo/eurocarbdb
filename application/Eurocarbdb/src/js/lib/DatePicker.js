/**
 * @fileoverview        Date picker library for ECDB
 * @author              hirenj
 */

if ( typeof(ECDB) == 'undefined' ) {
  ECDB = {};
}

if ( typeof(ECDB.DatePicker) == 'undefined' ) {
	ECDB.DatePicker = {};
}

/**
 *  Date picker class. Converts a input[type=text] element into a date picker
 *  widget. Calendar is initially hidden, but then is shown when focus is given
 *  to the converted element. The value for the converted field should be a date
 *  given in YY/MM/DD format. The localised date should be stored in the title 
 *  attribute of the element to convert.
 *
 *  @param  {Element}   element     Element to convert to a date picker
 *
 *  @requires       MochiKit
 *  @requires       YAHOO.widget.Calendar
 *  @constructor
 */
ECDB.DatePicker = function(element)
{
    this._target = element;
    this._setupYUICalendar();
    this._setupFormElements();
};

ECDB.DatePicker.prototype._setupYUICalendar = function(opts)
{
    opts = opts || {};
    this._calendar_el = MochiKit.DOM.DIV({'class':'calendar_widget'});

    var container = MochiKit.DOM.DIV({});;

    this._calendar_el.appendChild(container);
    this._calendar_el.appendChild(MochiKit.DOM.DIV({'style':'float: none; clear: both; height: 0px; width: 100%;'}));

    this._calendar = new YAHOO.widget.Calendar(container,{ close:false,hidden:true });
    
    this._setValue(this._target.value);

    this._calendar.selectEvent.subscribe(this._dateSelectEvent, this, true);

    if (this._calendar.getSelectedDates().length == 0)
    {
        this._calendar.select(this._calendar.today);
    }
    
    connect(this._target,'onchange',this,this._setValue);
};

ECDB.DatePicker.prototype._setupFormElements = function()
{
    this._target.style.display = 'none';
    this._target.parentNode.insertBefore(this._calendar_el,this._target);
    this._calendar.hide();
    this._calendar.render();

    var display = MochiKit.DOM.INPUT({'type':'text'});
    display.value = this._target.title;

    this._target.parentNode.insertBefore(display,this._target);
    
    connect(display,'onfocus',this,function() {
       this._calendar.show();
       display.parentNode.removeChild(display); 
    });

};

ECDB.DatePicker.prototype._dateSelectEvent = function(type,args,obj)
{
  	  var dates = args[0];
  	  var date = dates[0];
  	  var year = date[0], month = date[1], day = date[2];
  	  this._target.value = this._writeDate(day,month,year);
};

ECDB.DatePicker.prototype._setValue = function(value)
{
    if (value == null)
    {
        value = this._target.value;
    }
    if (value == null || value == '')
    {
        return;
    }
    var date = this._readDate(value);
	var year = date[0], month = date[1], day = date[2];
	this._calendar.select(month+"/"+day+"/"+year);
    var selectedDates = this._calendar.getSelectedDates();
	if (selectedDates.length > 0) {
		var firstDate = selectedDates[0];
		this._calendar.cfg.setProperty("pagedate", (firstDate.getMonth()+1) + "/" + firstDate.getFullYear());
		this._calendar.render();
	} else {
		log("Date outside of calendar range, doing nothing");
	}
};

ECDB.DatePicker.prototype._writeDate = function(day,month,year)
{
    return year+"/"+numberFormatter("00")(month)+"/"+numberFormatter("00")(day);
};

ECDB.DatePicker.prototype._readDate = function(value)
{
    dates = value.match(/\d+/g);    
    return [ dates[0],dates[1],dates[2] ];
}
