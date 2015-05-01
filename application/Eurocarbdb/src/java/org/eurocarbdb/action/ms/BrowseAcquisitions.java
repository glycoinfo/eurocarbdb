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

package org.eurocarbdb.action.ms;

//  stdlib imports
import java.util.Collection; 

//  3rd party imports - commons logging
import org.apache.log4j.Logger;

//  eurocarb imports
import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.dataaccess.ms.Acquisition;

import java.util.Collection;

//  static imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/**
*
*   @author     aceroni
*   @version    $Rev: 1549 $
*/
public class BrowseAcquisitions extends EurocarbAction 
{

    protected static final Logger log = Logger.getLogger( BrowseAcquisitions.class );

    public Collection<Acquisition> getAcquisitions() 
    {
        return getEntityManager()
                .createQuery(Acquisition.class)
                .list();
    }

    
    public String execute() 
    {
        return "success";
    }

}
