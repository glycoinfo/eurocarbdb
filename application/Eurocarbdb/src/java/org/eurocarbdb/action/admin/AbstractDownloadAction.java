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
import java.util.Calendar;
import java.util.Properties;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

//  3rd party imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory; 
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPConnectionClosedException;

//  eurocarb imports
import org.eurocarbdb.util.FTP_Client;
import org.eurocarbdb.util.ProgressWatchable;
import org.eurocarbdb.action.EurocarbAction;

/*  class AbstractDownloadAction  *//****************************************
*
*   Abstract base class for actions that download files. This class 
*   currently only supports FTP downloads (although HTTP downloads
*   would be easy to add).
*
*   @author   mjh <glycoslave@gmail.com>
*   @version  $Rev: 1932 $
*/
public abstract class AbstractDownloadAction 
extends EurocarbAction implements ProgressWatchable
{

    //~~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Logging handle. */
    private static final Log log = LogFactory.getLog( AbstractDownloadAction.class );

    /** Download stream buffer size in bytes. */
    private static final int BUFFER_SIZE = 4096;

    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    /** When downloading commenced, in milliseconds, as given by the 
    *   System.currentTimeMillis() call.  */
    protected long startTime = 0;
   
    /** File size of the current (or most recent) downloaded file in bytes. */
    protected long fileSize = 0;
   
    /** Last modified time of the current (or most recent) downloaded file 
    *   as reported by the server. */
    private Calendar fileTimestamp;
   
    /** Number of bytes downloaded for the current (or most recently) downloaded file. */
    protected long bytesDownloaded = 0;
    
    /** The input stream from the server for the file currently being downloaded. 
    *   This stream will only be active for the duration of the call to download(). */
    protected InputStream inStream;
    
    /** The output stream to which bytes downloaded from the server input stream
    *   will be directed. This stream will only be active for the duration of 
    *   the call to download().  */
    protected OutputStream outStream;
    
    /** The FTP client object. This is null when not connected. */
    protected FTP_Client ftpClient; 


    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//               
     
     
    /**
    *   Returns the output stream to which a downloaded file is 
    *   being directed. The return value will be null if a download is 
    *   not currently in progress.
    */
    public OutputStream getOutputStream() { return outStream; }    
     
     
    /*  openConnection  *//******************************************
    *
    *   Opens a connection to the given server host with the given 
    *   user credentials. Users of this method should wrap network
    *   actions in a <code>try-catch block</code> and call the 
    *   <code>closeConnection</code> method in the <code>finally</code> 
    *   section.
    */
    protected void openConnection( String server, String username, String password )
    throws IOException
    {
        if ( ftpClient != null )
            throw new RuntimeException(
                "FTP client still appears to be connected " +
                "-- perhaps you forgot to close the previous connection?");
                
        ftpClient = new FTP_Client();
        ftpClient.connectAndLogin( server, username, password );
    }
    
    
    /*  closeConnection  *//*****************************************
    *
    *   Closes an open connection; returns immediately without
    *   exception if connection is already closed or non-existent.
    */
    protected void closeConnection()
    {
        if ( ftpClient == null )
        {
            log.debug("connection has already been closed, returning...");
            return;
        }
        else try 
        {
            log.info("FTP client disconnecting...");
            ftpClient.disconnect();  
        }
        catch ( IOException dont_care ) 
        { 
            log.warn("Exception caught while disconnecting:", dont_care ); 
            dont_care.printStackTrace(); 
        }
        finally {  ftpClient = null;  }
    }
    
    
    /*  download  *//************************************************
    *
    *   Downloads the named remote file to the given output stream.
    *   This method blocks until download is complete, however
    *   information about download progress may be obtained 
    *   asynchronously (in another thread) via the following methods:
    *   <ul>
    *       <li>getMillisecsElapsed</li>
    *       <li>getPercentComplete</li>
    *       <li>getDownloadSpeed</li>
    *   </ul>
    *
    *   @see FTP_Client#statFile
    */
    protected void download( String filename, OutputStream out ) 
    throws IOException, FTPConnectionClosedException
    {
        if ( ftpClient == null )
            throw new RuntimeException("client not yet connected!!!");
            
        startTime = System.currentTimeMillis();        
        outStream = out;
        
        try
        {
            ftpClient.setPassiveMode( true );
            ftpClient.setBinaryMode( true );
            
            //  work out file mtime and size in bytes  
            FTPFile filestat = ftpClient.statFile( filename );
            
            //  mjh:TODO check not null here
        
            this.fileSize      = filestat.getSize();
            this.fileTimestamp = filestat.getTimestamp(); 

            if ( log.isDebugEnabled() )
            {
                log.debug( "File size is " 
                         + fileSize 
                         + ", last modified " 
                         + fileTimestamp );             
            }

            //  open input stream 
            this.inStream = ftpClient.retrieveFileStream( filename );
            
            byte[] buffer = new byte[BUFFER_SIZE]; 
            while ( true )
            {
                //  read as many bytes as are available, up to the length of the buffer.
                int available = inStream.available();
                int bytes = (available <= buffer.length) ? available : buffer.length;
                
                //  the call blocks until data is read
                int bytes_read = inStream.read( buffer, 0, bytes );
                
                if ( bytes_read == -1 || bytesDownloaded == fileSize ) break;
                if ( bytes_read == 0 ) continue;
                //if ( log.isTraceEnabled() )
                //    log.trace("read " + bytes_read + " bytes");
            
                bytesDownloaded += bytes_read;
                
                outStream.write( buffer, 0, bytes_read );
            }

            log.debug("finished transfer, ending FTP transaction");
            try {  inStream.close();  } 
            catch ( IOException ioe )
            {  
                log.warn("Caught exception closing input streams:", ioe );  
            }

            if ( ! ftpClient.completePendingCommand() )
                throw new IOException( "completePendingCommand returned false, "
                                     + "probably indicating a failed download "
                                     + "(unspecified reason)" );
            
            log.info("download completed successfully");
        }
        catch ( FTPConnectionClosedException ioe )
        {
            log.warn("FTP server closed connection: ", ioe );
            ioe.printStackTrace();
            closeConnection();
            throw ioe;
        }
        catch ( IOException ioe )
        {
            log.warn("Caught IO exception during FTP: ", ioe );
            ioe.printStackTrace();
            closeConnection();
            throw ioe;
        }

    }

    
    /*  getFileSize  *//*********************************************
    *
    *   Returns the size of the currently downloaded file in bytes.
    *   Note that this value will be 0 unless a download is in progress.
    */
    public long getFileSize() {  return fileSize;  }
    
    
    /*  getMillisecsElapsed  *//*************************************
    *
    *   Returns the number of milliseconds that have elapsed since a
    *   download commenced. Note that this value will be 0 unless a 
    *   download is in progress.
    */
    public int getMillisecsElapsed() 
    { 
        return (int) (System.currentTimeMillis() - startTime); 
    }


    /*  getPercentComplete  *//**************************************
    *
    *   Returns how much of a file has been downloaded at this point.
    *   Note that this value will be 0 unless a download is in progress.
    */
    public int getPercentComplete() 
    {
        if ( outStream == null ) return 0;
        if ( fileSize == 0 ) return 0;
        
        return (int) ((bytesDownloaded * 100) / fileSize);
    }
    
    
    /*  getDownloadSpeed  *//****************************************
    *
    *   Returns the current rate of download in kilobytes/second.
    *   Note that this value will be 0 unless a download is in progress.
    */
    public double getDownloadSpeed()
    {
        double elapsed_secs = (System.currentTimeMillis() - startTime) / 1000;
        if ( elapsed_secs == 0 ) return 0;
        
        // kilobytes per second
        return (bytesDownloaded / 1000) / elapsed_secs; 
    }
    
    
    /*  getEstimateOfTimeRemaining  *//******************************
    *
    *   Returns an estimate of the number of seconds a download has 
    *   left to complete based upon the current download speed and 
    *   file size. Note that this value will only be meaningful if 
    *   a download is in progress.
    */
    public double getEstimateOfTimeRemaining()
    {
        assert fileSize != 0; // in bytes
        double download_speed = getDownloadSpeed(); // in Kb/sec
        if ( download_speed <= 0 ) return 0;
        
        download_speed *= 1000; // now bytes/sec
        
        return (fileSize - bytesDownloaded) / download_speed;        
    }
    
    /*
    public void doLongRunningTask()
    {
        try 
        {
            for ( int i = 1; i <= 10; i++ )
            {
                Thread.sleep( 1000 );
                secondsElapsed = i;
                log.info( i + " second(s)" );
            }
        }
        catch ( InterruptedException interrupt ) {}
    }
    */
    

    public abstract String execute();

} // end class

