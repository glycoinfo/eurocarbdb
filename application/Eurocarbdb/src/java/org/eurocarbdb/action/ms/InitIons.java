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
*   Last commit: $Rev: 1924 $ by $Author: khaleefah $ on $Date:: 2010-06-21 #$  
*/

package org.eurocarbdb.action.ms;

import java.util.*;

import org.hibernate.Criteria; 
import org.hibernate.criterion.Restrictions; 

import org.eurocarbdb.action.*;
import org.eurocarbdb.dataaccess.*;
import org.eurocarbdb.dataaccess.ms.*;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/**
*
*   @author     aceroni
*   @version    $Rev: 1924 $
*/
public class InitIons extends EurocarbAction 
{
    static String[] ions = new String[4];    

    public int added_ions = 0;

    static {
        ions[0] = "H";
        ions[1] = "Na";
        ions[2] = "Li";
        ions[3] = "K";
    }
    

    public int getAddedIons() {
        return added_ions;
    }

    public Collection<Ion> getIons() {
        Criteria crit = getEntityManager().createQuery(Ion.class);
        return crit.list();
    } 

    
    public String execute() throws Exception {

        added_ions = 0;  

        for( int i=0; i<ions.length; i++ ) {
            // check if the ion is existing
            Ion ion = findIon(ions[i]);
            if( ion==null ) {
                // create a new ion
//                ion = new Ion(ions[i]);

                // store
                Eurocarb.getEntityManager().store(ion);          

                added_ions++;    
            }
        }
        
        return SUCCESS;
    }      
   

    public Ion findIon(String type) {
        Criteria crit = getEntityManager().createQuery(Ion.class).add(Restrictions.eq("ionType", type));
        
        Collection<Ion> list = crit.list();
        if( list==null || list.size()==0 )
            return null;
        return list.iterator().next();
    }
      

}
