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
* $Id: TrancheUploadTest.java 1549 2009-07-19 02:40:46Z glycoslave $
* Last changed $Author: glycoslave $
* EUROCarbDB Project
*/
package org.eurocarbdb.action;

import java.io.File;
import java.net.URI;

import org.eurocarbdb.tranche.TrancheUtility;

/**
* @author             hirenj
* @version                $Rev: 1549 $
*/
public class TrancheUploadTest extends EurocarbAction {
    
    private File file;
    private String fileContentType = null;
    private String fileFilename = null;
    private String hash;

    
    /**
     * @return the readHash
     */
    public String getHash() {
        return hash;
    }

    /**
     * @param readHash the readHash to set
     */
    public void setHash(String hash) {
        this.hash = hash;
    }

    public String execute() throws Exception {
        if (file == null) {
            if (hash != null) {
                file = (new TrancheUtility()).downloadFile(new URI("tranche://"+hash));
                return SUCCESS;
            }
            return INPUT;
        }
        URI uri = (new TrancheUtility()).uploadFile(file,fileFilename, fileContentType);
        hash = uri.getAuthority()+uri.getPath();
        return SUCCESS;
    }

    /**
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * @param file the file to set
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * @return the fileContentType
     */
    public String getFileContentType() {
        return fileContentType;
    }

    /**
     * @param fileContentType the fileContentType to set
     */
    public void setFileContentType(String fileContentType) {
        this.fileContentType = fileContentType;
    }

    /**
     * @return the fileFilename
     */
    public String getFileFilename() {
        return fileFilename;
    }

    /**
     * @param fileFilename the fileFilename to set
     */
    public void setFileFilename(String fileFilename) {
        this.fileFilename = fileFilename;
    }
}
