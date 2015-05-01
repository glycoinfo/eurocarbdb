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
*   Last commit: $Rev: 1147 $ by $Author: glycoslave $ on $Date:: 2009-06-04 #$  
*/

package org.eurocarbdb.dataaccess.hibernate;

import org.hibernate.transform.ResultTransformer;
import org.hibernate.transform.PassThroughResultTransformer;

/** 
*   Dumping ground for various Hibernate-related constants 
*   and utility methods. 
*
*   @author mjh
*/
public final class HibernateUtils
{
    /** Not instantiatable */
    private HibernateUtils() {}   


    /** 
    *   Result-set transformer that returns only the first column per row. 
    *
    *   @see org.hibernate.Query#setResultTransformer
    *   @see org.hibernate.Criteria#setResultTransformer
    */
    public static final ResultTransformer RETURN_FIRST_COLUMN_ONLY 
        = new PassThroughResultTransformer() 
            {
                /** Returns only the first column, disregards other columns */
                public Object transformTuple( Object[] tuple, String[] aliases )
                {
                    return tuple[0];
                }
            };
    
}
