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
*   Last commit: $Rev: 1278 $ by $Author: glycoslave $ on $Date:: 2009-06-27 #$  
*/

package test.eurocarbdb.dataaccess.core;

import java.util.*;

import org.testng.annotations.*;
// import org.testng.SkipException;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.dataaccess.core.*;

import test.eurocarbdb.dataaccess.CoreApplicationTest;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/**
*   Tests {@link Reference}.
*   
*   @author mjh
*   @version $Rev: 1278 $
*/
@Test( groups={"ecdb.db.core.reference"} )
public class ReferenceTest extends CoreApplicationTest
{
    // private Reference gs1;
   
    @Test
    public void coreReferenceDataExists()
    {
        super.setup();
        int i = getEntityManager().countAll( Reference.class );
        System.out.println("total references in data store = " + i );
        // if ( i == 0 )
        //     throw new SkipException();
        
        super.teardown();
    }
    
    
    
    @Test( dependsOnMethods={ "coreReferenceDataExists" } )
    public void coreReferenceSequenceAssociations() throws Exception
    {
        super.setup();
        
        Criteria q 
            = getEntityManager()
            .createQuery( Reference.class )
            .add( Restrictions.sizeGt("glycanSequenceReferences", 0 ) )
            .setMaxResults( 20 )
            ;
            
        List<Reference> refs = (List<Reference>) q.list();
          
        // if ( refs == null || refs.size() == 0 ) 
        //     throw new SkipException();
            
        for ( Reference r : refs )
        {
            System.out.println( "reference: " + r );
                      
            assert r.getGlycanSequenceReferences() != null;
            assert r.getGlycanSequenceReferences().size() > 0;
            
            System.out.println("glycanSequenceReferences:");
            for ( GlycanSequenceReference gsr : r.getGlycanSequenceReferences() )
            {
                System.out.println( " -> " + gsr );
            }
            
            System.out.println();
        }
        
        super.teardown();
    }
    
    
    @Test( dependsOnMethods={ "coreReferenceDataExists" } )
    public void coreReferenceEvidenceAssociations() throws Exception
    {
        super.setup();
        
        Criteria q 
            = getEntityManager()
            .createQuery( Reference.class )
            .add( Restrictions.sizeGt("referencedEvidence", 0 ) )
            .setMaxResults( 20 )
            ;
            
        List<Reference> refs = (List<Reference>) q.list();
          
        // if ( refs == null || refs.size() == 0 ) 
        //     throw new SkipException();
            
        for ( Reference r : refs )
        {
            System.out.println( "reference: " + r );
                      
            assert r.getReferencedEvidence() != null;
            assert r.getReferencedEvidence().size() > 0;
            
            System.out.println("referencedEvidence:");
            for ( ReferencedEvidence re : r.getReferencedEvidence() )
            {
                System.out.println( " -> " + re );
            }
            
            System.out.println();
        }
        
        super.teardown();
    }
    
} // end class



