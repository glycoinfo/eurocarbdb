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

package org.eurocarbdb.util;

//  stdlib imports
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;

//  3rd party imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory; 

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;



/*  class FTP_Client  *//****************************
*
*   // some doco here...
*
*   @author   mjh <glycoslave@gmail.com>
*   @version  $Rev: 1932 $
*/
public class FTP_Client extends FTPClient
{

    //~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Logging handle. */
    static final Log log = LogFactory.getLog( FTP_Client.class );
    
    protected String host;
    
    protected String userName;
   
   
    //~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~~//

    public FTP_Client() {  super();  }

    //~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~//    
        
    /*  connectAndLogin  *//*****************************************
    *
    *   A convenience method for connecting and logging in. 
    */
    public void 
    connectAndLogin( String host, String username, String password )
    throws IOException, UnknownHostException, FTPConnectionClosedException 
    {
        this.host = host;
        this.userName = username;
    
        log.debug("attempting to connect to " + host );
        
        connect( host );
        
        if ( FTPReply.isPositiveCompletion( getReplyCode() ) )
        {
            log.debug("connect successful, logging in as " + userName );
            login( userName, password );
        }    
        else
        {
            log.warn("failed to connect to " + host + ", disconnecting...");
            disconnect();
            return;
        }
        
        log.info("connected to " + host + " as user " + userName );
    }
    
    
    /*  setPassiveMode  *//****************************************** 
    *
    *   Turn passive transfer mode on or off. If Passive mode is active, a
    *   PASV command to be issued and interpreted before data transfers;
    *   otherwise, a PORT command will be used for data transfers. If you're
    *   unsure which one to use, you probably want Passive mode to be on. 
    */
    public void setPassiveMode( boolean setPassive ) 
    {
        if ( setPassive )
        {
            log.debug("setting PASSIVE mode for file transfers");
            enterLocalPassiveMode();
        }
        else
        {
            log.debug("setting ACTIVE mode for file transfers");
            enterLocalActiveMode();
        }
    }
    
    
    /*  setBinaryMode  *//*******************************************
    *
    *   Sets the use of binary mode for file transfers if passed true, 
    *   ASCII mode if passed false.  
    */
    public void setBinaryMode( boolean useBinaryMode ) throws IOException 
    {
        if ( useBinaryMode )
        {
            log.debug("setting BINARY mode for file transfers");
            setFileType( FTP.BINARY_FILE_TYPE );
        }
        else
        {
            log.debug("setting ASCII mode for file transfers");
            setFileType( FTP.ASCII_FILE_TYPE );
        }
    }
    
        
    /*  downloadFile  *//******************************************** 
    *
    *   Convenience method to stream a file from the server to the 
    *   given output stream.
    */
    public void downloadFile( String server_file, OutputStream out_stream )
    throws IOException, FTPConnectionClosedException 
    {
        log.debug("attempting to download '" + server_file + "'" );
        try 
        {  
            if (! retrieveFile( server_file, out_stream ))
                throw new IOException("retrieve returned false");  
            log.info("successfully downloaded '" + server_file + "'" );
        }
        finally {  log.info("download of '" + server_file + "' failed" );  }
    }    
    

    /*  downloadFile  *//******************************************** 
    *
    *   Convenience method to download a file from the server and save 
    *   it to the specified local file. 
    */
    public void downloadFile( String server_file, String local_file )
    throws IOException, FTPConnectionClosedException 
    {
        FileOutputStream outstream = new FileOutputStream( local_file );
        try     {  downloadFile( server_file, outstream );  }
        finally {  outstream.close();  }
    }


    /*  statFile  *//************************************************
    *
    */
    public FTPFile statFile( String server_file )
    throws IOException, FTPConnectionClosedException
    {
        FTPFile[] filelist = this.listFiles( server_file );
        FTPFile filestat;
        
        if ( filelist == null || filelist.length < 1 )
        {
            if (log.isDebugEnabled())
                log.debug( "File '" + server_file + "' not found on server");
            
            return null;
        }
        else if ( filelist.length > 1 )
        {
            throw new RuntimeException( "Multiple files returned for stat('"
                                      + server_file 
                                      + "') -- expected a single result"
                                      );
        }
        else filestat = filelist[0];
    
        return filestat;
    }
    

    /*  uploadFile  *//**********************************************
    *
    *   Convenience method to upload the output of an inputstream to a named server file. 
    */
    public void uploadFile( InputStream instream, String server_file ) 
    throws IOException, FTPConnectionClosedException 
    {
        log.debug("attempting to upload to " + host + ": " + server_file );
        
        assert server_file != null && server_file.length() > 0;
        assert instream != null;
        
        try
        {
            if (! storeFile( server_file, instream ))
                   throw new IOException("store returned false");
        }
        finally
        {
            log.info("upload of " 
                    + server_file 
                    + " to host " 
                    + host 
                    + " failed" 
                    );
        }
    }
    

    /*  uploadFile  *//**********************************************
    *
    *   Convenience method to upload a local file to the server. 
    */
    public void uploadFile( String localFile, String serverFile ) 
    throws IOException, FTPConnectionClosedException 
    {
        log.debug("attempting to upload local file '" + localFile + "'" );
        FileInputStream in = new FileInputStream( localFile );
        try     {  uploadFile( in, serverFile );  }
        finally {  in.close();  }        
    }




    
    /* --- not yet needed functionality ---
    
    / ** Sanity check that FTP client is connected && in existence. * /
    private final void _check_connection() throws FTPConnectionClosedException
    {  
        if ( ftpClient != null ) return;
        else throw new RuntimeException(
            "client not yet connected - you need to open a " +
            "connection with connectAndLogin first"
        );
    }

    / ** 
    *
    *   Get the list of files in the current directory as a Vector of Strings, 
    *   excluding subdirectories. 
    * /
    public Vector listFileNames () 
    throws IOException, FTPConnectionClosedException 
    {
        FTPFile[] files = listFiles();
        Vector v = new Vector();
        for (int i = 0; i < files.length; i++) {
            if (!files[i].isDirectory())
                v.addElement(files[i].getName());
        }
        return v;
    }
    
    / ** 
    *   Get the list of files in the current directory as a single Strings,
    *   delimited by \n (char '10') (excludes subdirectories) 
    * /
    public String listFileNamesString () 
            throws IOException, FTPConnectionClosedException {
        return vectorToString(listFileNames(), "\n");
    }
    
    
    / ** Get the list of subdirectories in the current directory as a Vector of Strings 
    *   (excludes files) 
    * /
    public Vector listSubdirNames () 
            throws IOException, FTPConnectionClosedException {
        FTPFile[] files = listFiles();
        Vector v = new Vector();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory())
                v.addElement(files[i].getName());
        }
        return v;
    }
    
    
    / ** 
    *   Get the list of subdirectories in the current directory as a single Strings,
    *   delimited by \n (char '10') (excludes files) 
    * /
    public String listSubdirNamesString () 
            throws IOException, FTPConnectionClosedException {
        return vectorToString(listSubdirNames(), "\n");
    }
    
    
    / ** 
    *   Convert a Vector to a delimited String 
    * /
    private String vectorToString (Vector v, String delim) {
        StringBuffer sb = new StringBuffer();
        String s = "";
        for (int i = 0; i < v.size(); i++) {
            sb.append(s).append((String)v.elementAt(i));
            s = delim;
        }
        return sb.toString();
    }
    
    --- not yet needed functionality ---*/         
            
            
} //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ end class

