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
/**
* $Id: BiologicalContextTest.java 1210 2009-06-11 18:13:15Z glycoslave $
* Last changed $Author: glycoslave $
* EUROCarbDB Project
*/
package test.eurocarbdb.dataaccess.core;

import org.testng.annotations.*;

import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.dataaccess.EntityManager;

import org.eurocarbdb.dataaccess.core.Taxonomy;
import org.eurocarbdb.dataaccess.core.TissueTaxonomy;
import org.eurocarbdb.dataaccess.core.BiologicalContext;

import test.eurocarbdb.dataaccess.CoreApplicationTest;

import static java.lang.System.out;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/**
*
*
*   @author         mjh
*   @version        $Rev: 1210 $
*/
public class BiologicalContextTest extends CoreApplicationTest
{
    BiologicalContext bc1;
    BiologicalContext bc2;
    BiologicalContext bc3;
    
    
    @Test
    (   groups={"ecdb.db.core"}
    ,   dependsOnGroups={"ecdb.db.connection"} 
    )
    public void core_bc_create()
    {
        super.setup();
        
        /* bc1: uses null tax/tissue  */
        bc1 = new BiologicalContext();
        out.println("bc1=" + bc1 );
        
        /* bc2: uses explcit default tax/tissue  */
        Taxonomy tax = Taxonomy.UnknownTaxonomy();
        TissueTaxonomy ttax = TissueTaxonomy.UnknownTissue();
        bc2 = new BiologicalContext( tax, ttax );
        out.println("bc2=" + bc2 );
        
        /*  bc3: uses last added tax/tissue to DB  */
        //  these lookups shouldn't fail if there is data in the DB
        tax  = Eurocarb.getRecentlyContributed( Taxonomy.class, 1 ).get( 0 ); 
        ttax = Eurocarb.getRecentlyContributed( TissueTaxonomy.class, 1 ).get( 0 ); 
        
        //  for bc1 != bc3 relation to be true (in equals() method), 
        //  tax/ttax cannot be the roots of their trees
        assert ! tax.isRoot();
        assert ! ttax.isRoot();
        
        bc3 = new BiologicalContext( tax, ttax );
        out.println("bc3=" + bc3 );
        
        super.teardown();
    }
    
    
    @Test
    (   groups={"ecdb.db.core"}
    ,   dependsOnMethods={"core_bc_create"}  
    )
    public void core_bc_equals()
    {
        out.println("bc1 = " + bc1 );
        out.println("bc2 = " + bc2 );
        out.println("bc3 = " + bc3 );
        
        assert bc1 != null;
        assert bc2 != null;
        assert bc3 != null;
        
        boolean b;
        out.println();
        
        b = bc1.equals( bc2 );
        out.println("bc1.equals( bc2 ): " + b + " (expecting false)");
        assert ! b;
        
        b = bc2.equals( bc1 );
        out.println("bc2.equals( bc1 ): " + b + " (expecting false)");
        assert ! b;
        
        b = bc1.equals( bc3 );
        out.println("bc1.equals( bc3 ): " + b + " (expecting false)");
        assert ! b;
        
        b = bc1.equals( bc1 );
        out.println("bc1.equals( bc1 ): " + b + " (expecting true)");
        assert b;
        
        b = bc2.equals( bc2 );
        out.println("bc2.equals( bc2 ): " + b + " (expecting true)");
        assert b;
        
        b = bc3.equals( bc3 );
        out.println("bc3.equals( bc3 ): " + b + " (expecting true)");
        assert b;
        
    }
    
    /*
    @Test
    ( groups={"ecdb.db.core"}, dependsOnGroups={"ecdb.db.connection"} )
    public void createBiologicalContext()
    {
        setup();
        
        
        
        teardown();
    }
    */
}
