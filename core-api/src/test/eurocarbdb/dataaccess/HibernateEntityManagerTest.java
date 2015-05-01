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
* $Id: HibernateEntityManagerTest.java 1210 2009-06-11 18:13:15Z glycoslave $
* Last changed $Author: glycoslave $
* EUROCarbDB Project
*/
package test.eurocarbdb.dataaccess;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.testng.annotations.*;

import org.hibernate.EntityMode;
import org.hibernate.SessionFactory;
import org.hibernate.HibernateException;
import org.hibernate.metadata.ClassMetadata;

import org.eurocarbdb.dataaccess.core.*;
import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.dataaccess.HibernateEntityManager;

import static java.lang.System.out;
import static org.eurocarbdb.util.StringUtils.join;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/**
*
*
*   @author         mjh
*   @version        $Rev: 1210 $
*/
public class HibernateEntityManagerTest extends CoreApplicationTest
{

    static Class<?>[] classes = new Class<?>[] {   
        GlycanSequence.class, 
        BiologicalContext.class, 
        Taxonomy.class, 
        TissueTaxonomy.class, 
        Disease.class, 
        Perturbation.class, 
        Evidence.class, 
        Reference.class 
    }; 

    
    @Test
    ( groups={"ecdb.db.connection"} )
    public void canConnectToDatabase()
    {
        out.println("trying to connect to DB (start a transaction)");
        
        super.setup();
        
        /*  do nothing */
        
        super.teardown();
    }


    @Test
    ( groups={"ecdb.db.populated"}, dependsOnGroups={"ecdb.db.connection"} )
    public void isDatabasePopulated()
    {
        super.setup();
        
        long total = 0;
        
        HibernateEntityManager hem = (HibernateEntityManager) getEntityManager();
        SessionFactory sf = hem.getSessionFactory();
        
        Map<String,ClassMetadata> hash 
            = (Map<String,ClassMetadata>) sf.getAllClassMetadata();
        
        List<String> list = new ArrayList( hash.keySet() );
        Collections.sort( list );
            
        out.println("Class : database count");            
        for ( Class<?> c : classes )
        {
            try
            {
                long count = getEntityManager().countAll( c ); 
                out.println( 
                    c 
                    + ": " 
                    + count 
                );
                total += count;
            }
            catch ( HibernateException ex )
            {
                out.println( 
                    c
                    + ": " 
                    + join(" -> ", ex.getMessages() )
                );   
            }
        }
        
        super.teardown();
        
        out.println();
        out.println("Total count of entities in data store: " + total );
        
        assert total > 0;
    }
    
    
    //  not used or called atm; needs work
    public void isDatabasePopulated___shows_all_entities()
    {
        super.setup();
        
        long total = 0;
        
        HibernateEntityManager hem = (HibernateEntityManager) getEntityManager();
        SessionFactory sf = hem.getSessionFactory();
        
        Map<String,ClassMetadata> hash 
            = (Map<String,ClassMetadata>) sf.getAllClassMetadata();
        
        List<String> list = new ArrayList( hash.keySet() );
        Collections.sort( list );
            
        out.println("Class : database count");            
        for ( String entity_name : list )
        {
            ClassMetadata class_metadata = null;
            Class<?> c = null;
            try
            {
                class_metadata = hash.get( entity_name );
                c = class_metadata.getMappedClass( EntityMode.POJO ); 
                
                long count = getEntityManager().countAll( c ); 
                out.println( 
                    c 
                    + ": " 
                    + count 
                );
                total += count;
            }
            catch ( HibernateException ex )
            {
                out.println( 
                    c
                    + ": " 
                    + join(" -> ", ex.getMessages() )
                );   
            }
        }
        
        super.teardown();
        
        out.println();
        out.println("Total count of entities in data store: " + total );
        
        assert total > 0;
    }
    
    
    
}
