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
* $Id: HibernateXmlEntityManagerTest.java 1210 2009-06-11 18:13:15Z glycoslave $
* Last changed $Author: glycoslave $
* EUROCarbDB Project
*/
package test.eurocarbdb.dataaccess;

import java.util.*;

import org.testng.annotations.*;

import org.dom4j.Element;
import org.dom4j.io.XMLWriter;
import org.dom4j.io.OutputFormat;

import org.hibernate.Session;
import org.hibernate.Transaction;

import test.eurocarbdb.CommandLineTest;
import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.dataaccess.HibernateXMLEntityManager;

import org.eurocarbdb.dataaccess.core.BiologicalContext;
import org.eurocarbdb.dataaccess.core.Disease;
import org.eurocarbdb.dataaccess.core.Taxonomy;
import org.eurocarbdb.dataaccess.core.TissueTaxonomy;


/**
* @author             hirenj
* @version                $Rev: 1210 $
*/
public class HibernateXmlEntityManagerTest extends CommandLineTest
{
    //@Test
    public void test1() throws Exception
    {
        //EntityManager em = new HibernateXMLEntityManager();
        
        // Taxonomy t        = em.lookup( Taxonomy.class, 100 );
        // TissueTaxonomy tt = em.lookup( TissueTaxonomy.class, 100 );
        // Disease d         = em.lookup( Disease.class, 100 );
       
        HibernateXMLEntityManager em = new HibernateXMLEntityManager();
        Session xml_session = em.getHibernateSession();
        Transaction tx = null;
        
        try 
        {
            tx = xml_session.beginTransaction();
            xml_session = em.getXmlSession();
            
            //Element tax1 = (Element) xml_session.get( Taxonomy.class, 304356 );
/*
            List results = xml_session
                           .createQuery("from Disease d where d.diseaseId = :id")
                           .setParameter("id", 8413 )
                           .list();
            Element tax1 = (Element) results.get(0);
                           
            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter out = new XMLWriter( System.out, format );
            
            out.write( tax1 );                               
*/            
/*
            List<Element> dlist = (List<Element>) xml_session
                                   .createQuery( "from Disease d join fetch d.relations r "
                                               + "where r.rightIndex - r.leftIndex = 1 " )
                                   .list();
                           
            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter out = new XMLWriter( System.out, format );
            
            out.write( dlist );                              
*/            
            
            List<Element> gslist = (List<Element>) xml_session
                                   .createQuery( "from GlycanSequence gs "
                                               + "join fetch gs.glycanContexts gc "
                                               + "join fetch gc.biologicalContext bc "
                                               //+ "join fetch gs.glycanEvidence ge "
                                               //+ "join fetch ge.evidence ev "
                                               )
                                   .list();
                           
            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter out = new XMLWriter( System.out, format );
            
            out.write( gslist );                              

            
            tx.commit();
        }
        catch ( Exception e ) 
        {
            if ( tx != null ) 
                tx.rollback();
                
            e.printStackTrace();
            throw e;
        }
        finally 
        {
            if ( xml_session != null && xml_session.isOpen() )
                xml_session.close();
        }    
    }
    
    public static void main( String[] args )
    throws Exception
    {
        new HibernateXmlEntityManagerTest().test1();  
    }
    
} // end class



