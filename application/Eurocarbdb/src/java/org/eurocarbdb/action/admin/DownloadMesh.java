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
*   Last commit: $Rev: 1932 $ by $Author: glycoslave $ on $Date:: 2010-08-05 #$  
*/

package org.eurocarbdb.action.admin;

//  stdlib imports
import java.io.IOException;
import java.io.FileOutputStream;
import java.net.URI;

//  3rd party imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory; 

//  Eurocarb imports
import org.eurocarbdb.dataaccess.Eurocarb;


/*  class DownloadMesh  *//******************************************
*
*   // some doco here...
*
*   @author   mjh <glycoslave@gmail.com>
*   @version  $Rev: 1932 $
*/
public class DownloadMesh extends AbstractDownloadAction implements org.eurocarbdb.action.RequiresAdminLogin  
{

    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Logging handle. */
    private static final Log log = LogFactory.getLog( DownloadMesh.class );
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//        
      
    /*  execute  *//*************************************************
    *
    */
    public String execute()
    {
        //  get update taxonomy URL & server authorisation params       
        String localfile  = Eurocarb.getProperty("mesh.localfile");
        URI    ncbi_uri   = Eurocarb.getPropertyAsURI("mesh.url");
        String hostname   = ncbi_uri.getHost(); 
        String serverfile = ncbi_uri.getPath(); 
        
        //  download it
        try
        {
            openConnection( hostname, "anonymous", "" );
            
            download( serverfile, new FileOutputStream( localfile ) );
            
            closeConnection();
    
            return SUCCESS;
        }
        catch ( Exception e )
        {
            log.error("execute failed: " + e );
            return ERROR;
        }
        finally
        {
            try {  if ( outStream != null ) outStream.close();  } 
            catch ( IOException ignored ) {}
            closeConnection();
        }
    }

    
    public static void main( String[] args )
    {
        System.err.println( new DownloadMesh().execute() );   
    }
} // end class

