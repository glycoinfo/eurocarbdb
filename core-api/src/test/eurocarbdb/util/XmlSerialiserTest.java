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

package test.eurocarbdb.util;

import java.util.List;
import java.io.Writer;
import java.io.StringWriter;

import org.testng.Assert;
import org.testng.annotations.*;

import test.eurocarbdb.dataaccess.CoreApplicationTest;

import org.eurocarbdb.util.XmlSerialiser;

import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.dataaccess.Contributed;
import org.eurocarbdb.dataaccess.EurocarbObject;

import org.eurocarbdb.dataaccess.core.Contributor;
import org.eurocarbdb.dataaccess.core.GlycanSequence;
import org.eurocarbdb.dataaccess.core.Evidence;

import org.eurocarbdb.dataaccess.core.Reference;
import org.eurocarbdb.dataaccess.core.JournalReference;
import org.eurocarbdb.dataaccess.core.ref.CarbbankReference;

import org.eurocarbdb.dataaccess.core.BiologicalContext;
import org.eurocarbdb.dataaccess.core.Taxonomy;
import org.eurocarbdb.dataaccess.core.TissueTaxonomy;
import org.eurocarbdb.dataaccess.core.Disease;
import org.eurocarbdb.dataaccess.core.Perturbation;

import org.eurocarbdb.dataaccess.ms.Acquisition;

import static java.lang.System.out;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


public class XmlSerialiserTest extends CoreApplicationTest
{
    @BeforeMethod 
    (   
        groups={"ecdb.xmlio"}, 
        dependsOnGroups={"ecdb.db.populated"}
    )
    void atStart() {  super.setup();  }

    
    @AfterMethod  
    (   
        groups={"ecdb.xmlio"}, 
        dependsOnGroups={"ecdb.db.populated"}
    )
    void atEnd()   {  super.teardown();  }
    
    
    /*~~~~~~~~~~~~~~~~~~~~~~~~~~~ TESTS ~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

    /** Test for GlycanSequence XML #2 */
    @Test
    (   
        groups={"ecdb.xmlio"}, 
        dependsOnGroups={"ecdb.db.populated"}
    )
    public void xmlSerialiseGlycanSequence2() throws Exception
    {
        testSerialise( GlycanSequence.class, 55 );
    }

    
    /** Test for Contributor XML */
    @Test
    (   
        groups={"ecdb.xmlio"}, 
        dependsOnGroups={"ecdb.db.populated"}
    )
    public void xmlSerialiseContributor() throws Exception
    {
        testSerialise( Contributor.class );
    }

    
    /** Test for GlycanSequence XML */
    @Test
    (   
        groups={"ecdb.xmlio"}, 
        dependsOnGroups={"ecdb.db.populated"}
    )
    public void xmlSerialiseGlycanSequence() throws Exception
    {
        testSerialise( GlycanSequence.class );
    }

    
    /** Test for BiologicalContext XML */
    @Test
    (   
        groups={"ecdb.xmlio"}, 
        dependsOnGroups={"ecdb.db.populated"}
    )
    public void xmlSerialiseBiologicalContext() throws Exception
    {
        testSerialise( BiologicalContext.class );
    }

    
    @Test
    (   
        groups={"ecdb.xmlio"}, 
        dependsOnGroups={"ecdb.db.populated"}
    )
    public void xmlSerialiseTaxonomy() throws Exception
    {
        testSerialise( Taxonomy.class );
    }

    
    @Test
    (   
        groups={"ecdb.xmlio"}, 
        dependsOnGroups={"ecdb.db.populated"}
    )
    public void xmlSerialiseTissueTaxonomy() throws Exception
    {
        testSerialise( TissueTaxonomy.class );
    }

    
    @Test
    (   
        groups={"ecdb.xmlio"}, 
        dependsOnGroups={"ecdb.db.populated"}
    )
    public void xmlSerialiseDisease() throws Exception
    {
        testSerialise( Disease.class );
    }
    
    
    @Test
    (   
        groups={"ecdb.xmlio"}, 
        dependsOnGroups={"ecdb.db.populated"}
    )
    public void xmlSerialisePerturbation() throws Exception
    {
        testSerialise( Perturbation.class );
    }
    

    /** Test for Acquisition XML */
    @Test
    (   
        groups={"ecdb.xmlio"}, 
        dependsOnGroups={"ecdb.db.populated"}
    )
    public void xmlSerialiseAcquisition() throws Exception
    {
        testSerialise( Acquisition.class );
    }

    
    /** Test for Evidence XML */
    @Test
    (   
        groups={"ecdb.xmlio"}, 
        dependsOnGroups={"ecdb.db.populated"}
    )
    public void xmlSerialiseEvidence() throws Exception
    {
        testSerialise( Evidence.class );
    }


    /** Test for Reference XML */
    @Test
    (   
        groups={"ecdb.xmlio"}, 
        dependsOnGroups={"ecdb.db.populated"}
    )
    public void xmlSerialiseReference() throws Exception
    {
        testSerialise( Reference.class );
    }
    

    /** Test for JournalReference XML */
    @Test
    (   
        groups={"ecdb.xmlio"}, 
        dependsOnGroups={"ecdb.db.populated"}
    )
    public void xmlSerialiseJournalReference() throws Exception
    {
        testSerialise( JournalReference.class );
    }
    
    
    /** Test for CarbbankReference XML */
    @Test
    (   
        groups={"ecdb.xmlio"}, 
        dependsOnGroups={"ecdb.db.populated"}
    )
    public void xmlSerialiseCarbbankReference() throws Exception
    {
        testSerialise( CarbbankReference.class );
    }
    
    
    /*~~~~~~~~~~~~~~~~~ helper methods ~~~~~~~~~~~~~~~~*/   
 
    protected <T extends EurocarbObject> void testSerialise( Class<T> c ) throws Exception
    {
        List<T> objects = Eurocarb.getRecentlyContributed( c, 5 );

        for ( T object : objects )
        {
            XmlSerialiser xml_io = getXmlSerialiser();
            
            xml_io.serialise( object );
    
            reportXml( object, xml_io );        
        }
    }
    
    
    protected <T extends EurocarbObject> void testSerialise( Class<T> c, int id ) throws Exception
    {
        T object = getEntityManager().lookup( c, id );
        
        XmlSerialiser xml_io = getXmlSerialiser();
        
        xml_io.serialise( object );

        reportXml( object, xml_io );        
    }
    
    
    public static XmlSerialiser getXmlSerialiser()
    {
        XmlSerialiser xml_io = new XmlSerialiser();
        Writer out = new java.io.StringWriter();
        xml_io.setWriter( out );
        return xml_io;
    }
    
    static int count = 0;
    
    protected static void reportXml( Object x, XmlSerialiser xml_io )
    {
        count++;
        
        out.println();
        out.println(  
            "--------------------- xml " 
            + count
            + " - "
            + x.getClass().getSimpleName() 
            + " ---------------------"
        );
        out.println( xml_io.getWriter().toString() );
        out.println(
            "--------------------- end xml " 
            + count 
            + "---------------------"
        );
        out.println();
    }
    
    
    protected static <T> T getInstanceOf( Class<T> the_class, int the_id )
    {
        T object = getEntityManager().lookup( the_class, the_id );
        assert object != null 
            : "Retrieved object " 
            + the_class.getSimpleName() 
            + "=" 
            + the_id 
            + " was null";
            
        return object;
    }
    
}



