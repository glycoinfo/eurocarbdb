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
* $Id: EntityManagerTests.java 1210 2009-06-11 18:13:15Z glycoslave $
* Last changed $Author: glycoslave $
* EUROCarbDB Project
*/
package test.eurocarbdb.dataaccess;

import org.testng.annotations.*;

import org.eurocarbdb.dataaccess.core.*;
import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.dataaccess.EntityManager;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/**
* @author             mjh
* @version                $Rev: 1210 $
*/
public class EntityManagerTests extends CoreApplicationTest
{

    
    @Test
    public void createEntity() 
    {
/* mjh: needs work to be useful
        EntityManager em = Eurocarb.getEntityManager();
        
        Taxonomy t        = em.lookup( Taxonomy.class, 100 );
        TissueTaxonomy tt = em.lookup( TissueTaxonomy.class, 100 );
        Disease d         = em.lookup( Disease.class, 100 );
       
        BiologicalContext bc = new BiologicalContext();
        bc.setTaxonomy(t);
        bc.setTissueTaxonomy(tt);
//        bc.getDiseases().add( d );

        try
        {
            em.store( bc );
        }
        catch ( org.hibernate.exception.SQLGrammarException e )
        {               
            for ( String m : e.getMessages() )
            {
                System.err.println( "---" );  
                System.err.println( m );  
            }
           
            System.err.println( e.getCause() );  
            e.printStackTrace();   
        }
*/
    }

}
