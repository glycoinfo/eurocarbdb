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
* $Id: Example.java 1549 2009-07-19 02:40:46Z glycoslave $
* Last changed $Author: glycoslave $
* EUROCarbDB Project
*/
package org.eurocarbdb.action.hplc;

import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.dataaccess.core.Reference;

//3rd party imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory; 

/**
* @author             hirenj
* @version                $Rev: 1549 $
*/
public class Example extends EurocarbAction {
    

    /** Logging handle. */
    protected static final Log log = LogFactory.getLog( BeginHplc.class );
    
    private Reference ref;
    private Long detectorId;
    private String manufacturer;
    private String model;
    private String excitation;
    private String emission;
    private float bandwidth;
    private String samplingRate;
    
        
    public String execute() {

        if (this.getManufacturer() != null) {
            return this.SUCCESS;
        } else {
            return this.INPUT;
        }
    }

    public String getManufacturer() {
        return manufacturer == null ? "no_man" : manufacturer;
    }

/* The use of a conditional operator in java ? :  The condition, (a > b), is tested. If it is true the first value, a, is returned. If it is false, the second value, b, is returned. Whichever value is returned is dependent on the conditional test, a > b. 
*no_manufacturer is returned if manu == null
* */
  

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Reference getRef() {
        return ref;
    }

    public void setRef(Reference ref) {
        this.ref = ref;
    }



}
