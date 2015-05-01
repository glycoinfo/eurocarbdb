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
* $Id: TrancheUtility.java 1549 2009-07-19 02:40:46Z glycoslave $
* Last changed $Author: glycoslave $
* EUROCarbDB Project
*/
package org.eurocarbdb.tranche;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eurocarbdb.dataaccess.Eurocarb;
import org.proteomecommons.tranche.util.AddFileTool;
import org.proteomecommons.tranche.util.BigHash;
import org.proteomecommons.tranche.util.GetFileTool;
import org.proteomecommons.tranche.util.UserZipFile;
import org.eurocarbdb.dataaccess.core.Contributor;


/** Wrapper class for Tranche uploading and downloading
* @author             hirenj
* @version                $Rev: 1549 $
*/
public class TrancheUtility {

    public static final Log LOG = LogFactory.getLog( TrancheAdmin.class );
    private boolean forceUpload = true;
    
    /** Upload a file on the tranche network
     * @param file    File to upload
     * @return            A URI for the file on the tranche network
     * @throws IOException    If there are any problems uploading the file
     */
    public static URI uploadFile(File file) throws IOException {
        return uploadFile(file,"","EUROCarbDB data");
    }

    public static URI uploadFile(File file,String fileName) throws IOException {
        return uploadFile(file,fileName,"EUROCarbDB data");
    }

    
    /** Check if a file exists on the tranche network
     * @param file        File to check for
     * @return                Whether the file exists or not
     * @throws FileNotFoundException    If the local file does not exist
     */
    public static boolean fileExists(File file) throws FileNotFoundException {
        BigHash hash = new BigHash(file);
        try {
            downloadFile(hash);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void setUser(File file) {
        Contributor currentContributor = Eurocarb.getCurrentContributor();
        File userFile = new File(
            Eurocarb.getProperty("tranche.users.directory")+"ecdb-"+currentContributor.getContributorId()+".zip.encrypted"
        );
        userFile.delete();
        file.renameTo(userFile);
    }


    private static UserZipFile getUser() {
    
        Contributor currentContributor = Eurocarb.getCurrentContributor();

        File userFile = new File(
            Eurocarb.getProperty("tranche.users.directory")+"ecdb-"+currentContributor.getContributorId()+".zip.encrypted"
        );


        if (! userFile.exists()) {
            TrancheAdmin.createTrancheUser();                
        }
                    
        UserZipFile user = new UserZipFile(userFile);

        user.setPassphrase( "password" );
        return user;
    }

    /** Upload a file to the tranche network. Uses the following properties:
     *      <ul>
     *          <li>tranche.superuser.password</li>
     *          <li>tranche.server</li>
     *      </ul>s
     * @param file          File to upload
     * @param name          Name of the file to be supplied to Tranche
     * @param description   Description of the file to be supplied to tranche
     * @return              URI of the file
     * @throws IOException  On any Tranche errors
     */
    public static URI uploadFile(File file, String name, String description) throws IOException {

        UserZipFile user = getUser();

        AddFileTool addTool = new AddFileTool(user.getCertificate(), user.getPrivateKey());
        String[] servers = Eurocarb.getConfiguration().getStringArray("tranche.servers");
        for (String server : servers ) {
            addTool.addServerURL(server);
        }
        addTool.setTitle(name);
        addTool.setDescription(description);

        BigHash hash;
        try {
            hash = addTool.addFile(file);
        } catch (Exception e) {
            LOG.error("Could not send file to Tranche",e);
            throw new IOException("Error sending file to Tranche: "+e.getMessage());
        }
        try {
            return new URI("tranche://"+hash.toString());
        } catch (URISyntaxException e) {
            LOG.error("Could not create URI for tranche upload - file hash was "+hash.toString(),e);
        }
        return null;
    }
    
    /** Download a file from the Tranche network 
     * @param inputURI      Input URI of the tranche file
     * @return              File at the URI, or null if it does not exist
     * @throws IOException
     */    
    public static File downloadFile(URI inputURI) throws IOException {
        if (inputURI.getScheme().equals("tranche")) {
            return downloadFile(inputURI.getAuthority()+inputURI.getPath());
        } else {
            throw new FileNotFoundException("Supplied URI does not specify a tranche file");
        }
    }
    
    /** Download a file from the Tranche network 
     * @param inputHash     Hash of the file in tranche
     * @return              File with the given hash, null if the file does not exist
     * @throws IOException
     */
    public static File downloadFile(String inputHash) throws IOException {
        // the hash to download
        BigHash hash = BigHash.createHashFromString(inputHash);
        return downloadFile(hash);
    }

    private static File downloadFile(BigHash hash) throws IOException {
            
        // use the GetFileTool to download the project file
        GetFileTool gft = new GetFileTool();

        String[] servers = Eurocarb.getConfiguration().getStringArray("tranche.servers");
        for (String server : servers ) {
            gft.getServersToUse().add(server);
        }

        gft.setHash(hash);
        // don't bother double-checking digital signatures
        gft.setValidate(false);
        
        File output;
        try {
            output = File.createTempFile("tranche", "storedFile");
        } catch (IOException e1) {
            LOG.error(e1);
            return null;
        }
                
        // get the project using the specified base directory
        try {
            gft.getFile(output);
        } catch (Exception e) {
            LOG.error("Could not download file with this hash",e);
            throw new IOException("Could not download file "+e.getMessage());
        }

        return output;
    }
}
