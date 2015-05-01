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
/*
Action.java.template  --  A template for Eurocarb Actions.
author: mjh <glycoslave@gmail.com>

Instructions: fill in the blanks ;-)
*/

package org.eurocarbdb.action.admin;

//  stdlib imports

//  3rd party imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory; 

//  eurocarb imports
import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.dataaccess.EntityManager;

//  static imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/*  class UpdateTaxonomy  *//****************************************
*
*   // some doco here...
*
*   @author   mjh <glycoslave@gmail.com>
*   @version  $Rev: 1932 $
*/
public class UpdateTaxonomy extends EurocarbAction implements org.eurocarbdb.action.RequiresAdminLogin 
{
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Logging handle. */
    private static final Log log = LogFactory.getLog( UpdateTaxonomy.class );
    

    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//    
        
     
    /*  execute  *//*************************************************
    *
    */
    public String execute()
    {
        return SUCCESS;
    }

} // end class

