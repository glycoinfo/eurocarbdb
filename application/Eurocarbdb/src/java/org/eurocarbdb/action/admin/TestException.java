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

package org.eurocarbdb.action.admin;

//  stdlib imports
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Collections;

//  3rd party imports
import org.apache.log4j.Logger;

//  eurocarb imports
import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.dataaccess.EntityManager;

import org.eurocarbdb.dataaccess.core.*;
import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.dataaccess.Contributed;


//  static imports
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

import org.eurocarbdb.dataaccess.core.Contributor;


/*  class TestException  *//*************************************
*
*   
*
*   @author   ac [a.ceroni@imperial.ac.uk]
*   @version  $Rev: 1549 $
*/
public class TestException extends EurocarbAction implements org.eurocarbdb.action.RequiresAdminLogin {

    /** Logging handle. */
    private static final Logger log = Logger.getLogger( TestException.class );


    /*  execute  *//*************************************************
    *
    */
    public String execute() throws Exception
    {        
    throw new Exception("No cause");
    }

} // end class

