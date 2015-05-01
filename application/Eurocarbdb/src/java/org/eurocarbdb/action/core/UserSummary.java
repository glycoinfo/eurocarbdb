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
*   Last commit: $Rev: 1932 $ by $Author: glycoslave $ on $Date:: 2010-08-05 #$  
*/

package org.eurocarbdb.action.core;

//  stdlib imports
import java.util.Map;
import java.util.List;
import java.util.Iterator;

//  3rd party imports
import org.apache.log4j.Logger;

//  eurocarb imports
import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.action.RequiresLogin;

import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.dataaccess.core.Experiment;

//  static imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/*  class UserSummary  *//*************************************
*
*   
*
*   @author   mjh [glycoslave@gmail.com]
*   @version  $Rev: 1932 $
*/
public class UserSummary extends EurocarbAction implements RequiresLogin
{

    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Logging handle. */
    static final Logger log = Logger.getLogger( UserSummary.class );
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//    
    
    

    /*  execute  *//*************************************************
    *
    */
    public String execute()
    {
        return SUCCESS;
    }

} // end class

