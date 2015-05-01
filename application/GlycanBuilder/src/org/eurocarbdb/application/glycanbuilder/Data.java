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
*   Last commit: $Rev: 1210 $ by $Author: glycoslave $ on $Date:: 2009-06-12 #$  
*/

package org.eurocarbdb.application.glycanbuilder;

import java.util.Vector;


/**
   Object used to store a table of values into the clipboard.
   
   @author Alessio Ceroni (a.ceroni@imperial.ac.uk)
*/

public class Data {

    private Vector<Vector<Object>> buffer;
    private Vector<Object> current;


    /**
       Empty constructor.
     */
    public Data() {
    buffer = new Vector<Vector<Object>>();
    current = null;
    newRow();
    }

    /**
       Add a new row to the table.
     */
    public Data newRow() {
    buffer.add(current = new Vector<Object>());
    return this;
    }
    
    /**
       Add a new value to the last row of the table.
     */
    public Data add(Object o) {
    current.add(o);
    return this;
    }
    
    public String toString() {
    StringBuilder sb = new StringBuilder();
    for( Vector<Object> v : buffer ) {
        if( v.size()>0 || v!=current ) {
        boolean first = true;
        for( Object o : v ) {
            if( !first )
            sb.append('\t');

            String ostr = "" + o;
            ostr = ostr.replace('\n',' ').replace('\t',' ');
            sb.append(ostr);

            first = false;
        }
        sb.append('\n');
        }
    }
    return sb.toString();
    }

}