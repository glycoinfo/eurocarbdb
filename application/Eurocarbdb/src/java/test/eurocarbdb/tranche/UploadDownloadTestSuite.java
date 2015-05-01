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
/**
* Tranche Test Suite
* EUROCarbDB Project
*/
package test.eurocarbdb.tranche;

import org.testng.annotations.*;
import java.io.File;
import java.net.URI;
import java.net.URL;
import org.eurocarbdb.tranche.TrancheAdmin;
import org.eurocarbdb.tranche.TrancheUtility;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.eurocarbdb.dataaccess.Eurocarb;

import org.hibernate.SessionFactory;

// 
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/**
* Base class for all Action-based test suites. Inheriting from this class will
* initialise the database and webwork configuration
*  
* @author             hirenj
*/
public class UploadDownloadTestSuite 
{
    
    // private SessionFactory sf;
    
    @BeforeSuite
    public void initialise() throws Exception 
    {

        // sf = HibernateUtil.getSessionFactory();
        // sf.getCurrentSession().beginTransaction();
        getEntityManager().beginUnitOfWork();
    
        Eurocarb.getConfiguration().addConfiguration( 
            new PropertiesConfiguration("eurocarbdb-application.properties") );
        TrancheAdmin.StartServer();
    
    }    
  
    
    @AfterSuite
    public void teardown() throws Exception 
    {
        // sf.getCurrentSession().getTransaction().commit();
        getEntityManager().endUnitOfWork();
    }

    @Test
    public void testUploadDownload() throws Exception 
    {
        TrancheUtility tu = new TrancheUtility();
        URL inputurl = Object.class.getResource("/test/BasicTestFile.test.xml");
        File inputFile = new File(inputurl.getFile());
        URI url = tu.uploadFile(inputFile);
        File outputFile = tu.downloadFile(url);
        assert inputFile.length() == outputFile.length();
    }
    
    @Test
    public void testDownloadBadHash() throws Exception 
    {
        TrancheUtility tu = new TrancheUtility();
        try 
        {
            File outputFile = tu.downloadFile("tranche://IAMABADHASH");
        } 
        catch (RuntimeException e) 
        {
            assert true;
            return;
        }
        assert false;
    }

    @Test
    public void testDownloadUnknownHash() throws Exception 
    {
        TrancheUtility tu = new TrancheUtility();
        try 
        {
            File outputFile = tu.downloadFile("mGNNW+dCIIk0hEqUFOgM4QBi15TYjGX3I1I6V7uo9JDKHJu8Ks8kx+hwKhumqFu6XXjCy1D7IqP3/hZjLnqW0MbR2YsAAAAAABdZew==");
        } 
        catch (Exception e) 
        {
            assert true;
            return;
        }
        assert false;
    }

    @Test
    public void testMultipleUpload() throws Exception 
    {
        TrancheUtility tu = new TrancheUtility();
        URL inputurl = Object.class.getResource("/test/BasicTestFile.test.xml");
        File inputFile = new File(inputurl.getFile());
        URI url = tu.uploadFile(inputFile);
        URI url2 = tu.uploadFile(inputFile);
        // We should have the same hash for uploading the same file twice.
        assert url.equals(url2);
    }
    
    @Test
    public void testCheckForFile() throws Exception 
    {
        TrancheUtility tu = new TrancheUtility();
        URL inputurl = Object.class.getResource("/test/BasicTestFile.test.xml");
        File inputFile = new File(inputurl.getFile());
        URI url = tu.uploadFile(inputFile);
        assert tu.fileExists(inputFile);
    }
}