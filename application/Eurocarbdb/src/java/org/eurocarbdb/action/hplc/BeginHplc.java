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
/**
* $Id: BeginHplc.java 1549 2009-07-19 02:40:46Z glycoslave $
* Last changed $Author: glycoslave $
* EUROCarbDB Project
*/
package org.eurocarbdb.action.hplc;

import org.eurocarbdb.action.EurocarbAction;

//3rd party imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory; 

/**
* @author             hirenj
* @version                $Rev: 1549 $
*/
public class BeginHplc extends EurocarbAction {
    

    /** Logging handle. */
    protected static final Log log = LogFactory.getLog( BeginHplc.class );
    
    private String myVariable;
    String emptystring = "empty"; 
    
    public String execute() {
        
        
        if (this.getMyVariable().equals("hello")) {
            return this.SUCCESS;
        } else {
            return this.INPUT;
        }
    }

    public String getMyVariable() {


//       if (myVariable == null) { myVariable = emptystring;}
  //     return myVariable;
        return myVariable == null   ? emptystring : myVariable;
    }

    public void setMyVariable(String myVariable) {
        this.myVariable = myVariable;
    }

}
