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

package test.eurocarbdb.dataaccess.core;

import java.util.*;

import org.testng.annotations.*;


import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.dataaccess.EntityManager;

import org.eurocarbdb.dataaccess.core.*;

import test.eurocarbdb.dataaccess.CoreApplicationTest;
//import test.eurocarbdb.EurocarbTestApplication;

/**
*   @author mjh
*   @version $Rev: 1147 $
*/
public class GlycanSequenceTest extends CoreApplicationTest
{
    EntityManager em = Eurocarb.getEntityManager();
    
    // private GlycanSequence gs1;
   
    @Test
    (   groups={"ecdb.db.core"}
    ,   dependsOnGroups={"ecdb.db.connection"} 
    )
    public void dataExists()
    {
        super.setup();
        int i = em.countAll( GlycanSequence.class );
        System.err.println("total sequences in data store = " + i );
        // assert count > 0;
        super.teardown();
    }
    
    /*
    @Test
    (   groups={"ecdb.db.core"}
    ,   dependsOnMethods={ "dataExists" } 
    )
    public void getTestSequence() throws Exception
    {
        super.setup();
        super.teardown();
    }
    
    
    @Test
    (   groups={"ecdb.db.core"}
    ,   dependsOnMethods = { "getTestSequence" } 
    )
    public void getReferencesForTestSequence() throws Exception
    {
        List<Reference> refs = gs1.getReferences();
        for ( Reference r : refs )
        {
            System.err.println( r );
        }
    }
    */
    
    
    
    
} // end class



