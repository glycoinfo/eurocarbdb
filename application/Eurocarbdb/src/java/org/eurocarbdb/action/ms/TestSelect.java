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
* $Id: TestSelect.java 1549 2009-07-19 02:40:46Z glycoslave $
* Last changed $Author: glycoslave $
* EUROCarbDB Project
*/

package org.eurocarbdb.action.ms;

import org.eurocarbdb.action.*;
import org.eurocarbdb.dataaccess.*;
import org.eurocarbdb.dataaccess.ms.*;

import java.util.*;

import org.apache.log4j.Logger;

/**
* @author             aceroni
* @version                $Rev: 1549 $
*/
public class TestSelect extends EurocarbAction {
    
    public String execute() {
        return SUCCESS;
    }

    public Collection<String> getValues() {
    Vector<String> ret = new Vector<String>();
    ret.add("val1");
    ret.add("val2");
    return ret;
    }

    public Map<String,String> getMapValues() {
    HashMap<String,String> ret = new HashMap<String,String>();
    ret.put("1","val1");
    ret.put("2","val2");
    return ret;
    }
}
