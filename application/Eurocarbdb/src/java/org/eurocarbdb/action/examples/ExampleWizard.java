/*
*   EuroCarbDB, a framework for carbohydrate bioinformatics
*
*   Copyright (c) 2006-2009, Eurocarb project, or third-party contributors as
*   indicated by the @author tags or express copyright attribution
*   statements applied by the authors.  
*
*   This copyrighted material is made available to anyone wishing to use, modify,
*   copy, or redistribute it subject to the terms and conditions of the GNU
*   Lesser General Public License, as published by the Free Software Foundation.
*   A copy of this license accompanies this distribution in the file LICENSE.txt.
*
*   This program is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
*   or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
*   for more details.
*
*   Last commit: $Rev: 1549 $ by $Author: glycoslave $ on $Date:: 2009-07-19 #$  
*/

package org.eurocarbdb.action.examples;

import java.util.Map;

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.ActionSupport;

public class ExampleWizard implements Action
{
    /* parameters */
    private String stringData = null;
    
    private String[] arrayData = null;
    
    private double numericalData = -1;
    
    /*  parameter accessors, settable from CGI parameters with the 
    *   param names 'string', 'array', and 'number'.
    */
    
    public String getString() { return stringData; }
    public void setString( String data ) { stringData = data; }
    
    public String[] getArray() { return arrayData; }
    public void setArray( String[] data ) { arrayData = data; }
    
    public double getNumber() { return numericalData; }
    public void setNumber( double data ) { numericalData = data; }

   
    public String execute()
    {
        if ( stringData == null ) 
            return "input_string_view";
        
        if ( stringData.length() == 0 )
        {
            //this.addFieldError( "stringData", "Missing string data" );
            return "input_string_view";
        }
        
        if ( arrayData == null   ) 
            return "input_array_view";
        
        if ( arrayData.length == 0 )
        {
            //this.addFieldError( "arrayData", "Missing array data" );
            return "input_array_view";
        }
        
        if ( numericalData == -1 ) 
            return "input_number_view";
        
        return SUCCESS;
    }

    
}
