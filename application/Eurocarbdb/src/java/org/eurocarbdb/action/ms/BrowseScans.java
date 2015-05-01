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

import java.util.Collection;

import org.hibernate.Criteria; 
import org.apache.log4j.Logger;

import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.dataaccess.ms.Scan;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/**
*   @author      aceroni
*   @version     $Rev: 1549 $
*/
public class BrowseScans extends EurocarbAction 
{

    protected static final Logger log = Logger.getLogger( BrowseScans.class );

    public String execute() 
    {
        if( submitAction.equals("Add new scan") )
            return "add";
        return "success";
    }

    public Collection<Scan> getScans() 
    {
        Criteria crit = getEntityManager().createQuery(Scan.class);
        return crit.list();
    }

}
