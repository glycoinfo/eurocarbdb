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
* $Id: TrancheAdmin.java 1549 2009-07-19 02:40:46Z glycoslave $
* Last changed $Author: glycoslave $
* EUROCarbDB Project
*/
package org.eurocarbdb.tranche;

import java.io.File;
import java.net.BindException;

import org.apache.log4j.Logger;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.proteomecommons.tranche.flatfile.FlatFileTrancheServer;
import org.proteomecommons.tranche.server.Server;
import org.proteomecommons.tranche.util.MakeUserZipFileTool;
import org.proteomecommons.tranche.util.SecurityUtil;
import org.proteomecommons.tranche.util.UserZipFile;

import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.dataaccess.core.Contributor;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/**
* @author             hirenj
* @version         $Rev: 1549 $
*/
public class TrancheAdmin 
{
    private static Server server;
    private static FlatFileTrancheServer ffserver;

    //public static final Log log = LogFactory.getLog( TrancheAdmin.class );
    /** logging handle */
    static Logger log = Logger.getLogger( TrancheAdmin.class );
    
    
    
    public static void StartServer() 
    {
        UserZipFile zip = null;
        MakeUserZipFileTool maker;
        try
        {
            // Set up our server in subdirectory directory
            ffserver = new FlatFileTrancheServer( new File( Eurocarb.getProperty("tranche.data.directory")));
            maker = new MakeUserZipFileTool();
            
            // Create a user
            maker.setName( Eurocarb.getProperty("tranche.superuser.name"));
            maker.setPassphrase( Eurocarb.getProperty("tranche.superuser.password"));
            maker.setUserFile( new File(Eurocarb.getProperty("tranche.users.directory")+"superuser.zip.encrypted"));
        }
        catch ( Exception e )
        {
            log.warn("Caught exception trying to start Tranche:", e );
            return;
        }
            
        assert ffserver != null;
        assert maker != null;
        
        try 
        {
            zip = (UserZipFile) maker.makeCertificate();
        } 
        catch (Exception e) 
        {
            log.warn( "Could not start up Server - error making certificate", e );
            return;
        }
        
        // Set user permissions as admin (server needs user registered or it will
        // throw a SecurityException on attempted file upload)
        try 
        {
            zip.setFlags(SecurityUtil.getProteomeCommonsAdmin().getFlags());
        } 
        catch (Exception e) 
        {
            log.warn( "Could not give Admin rights to Tranche user", e );
            return;
        }        
        
        // Add user to server
        ffserver.getConfiguration().getUsers().add(zip);

        log.info("Adding in users");
        try 
        {
            getEntityManager().beginUnitOfWork();
            
            for ( Contributor contrib : Contributor.getAllContributors() ) 
            {
                TrancheAdmin.createTrancheUser(contrib);
            }
         
            getEntityManager().endUnitOfWork();
        }
        catch(Exception e) 
        {
            getEntityManager().abortUnitOfWork();
        }

        try 
        {
            server = new Server( ffserver, Integer.parseInt( Eurocarb.getProperty("tranche.server.port")));
            server.start();
        }
        
        catch (BindException e) 
        {
            log.error("Could not start up Tranche server on the given port",e);
        } 
        catch (Exception e) 
        {
            log.error("Could not start up Tranche server",e);
        }
    }
    
    public static void StopServer() 
    {
        // Close server!
        try
        {
            if ( server != null )
                server.setRun(false);
        } 
        catch (Exception e)
        {
            //log.error("Could not stop execution of Server",e);
        }
        try
        {
            if ( ffserver != null )
                ffserver.close();
        } 
        catch (Exception e)
        {
          //  log.error("Could not close the Server",e);
        }
    }


    /** Create a tranche user
         *    Create a tranche user from the current contributor in ECDB
         */ 
        public static void createTrancheUser() {
            TrancheAdmin.createTrancheUser(Eurocarb.getCurrentContributor());
        }

        /** Create a tranche user from a given contributor
         * @param    contributor            Contributor to create a tranche user for
         */
        public static void createTrancheUser(Contributor contributor) {
            UserZipFile zip = null;
      MakeUserZipFileTool maker;
      try
      {
          maker = new MakeUserZipFileTool();
          
          // Create a user
          maker.setName( "ecdb"+contributor.getContributorId() );

          maker.setPassphrase( "password" );
          maker.setUserFile( new File(Eurocarb.getProperty("tranche.users.directory")+"ecdb-"+contributor.getContributorId()+".zip.encrypted"));
          zip = (UserZipFile) maker.makeCertificate();
                    zip.setFlags(SecurityUtil.getProteomeCommonsUser().getFlags());
                    if ( ffserver != null ) {
                ffserver.getConfiguration().getUsers().add(zip);
                    }
      } 
      catch (Exception e) 
      {
          return;
      }            
        }

}
