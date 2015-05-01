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
package org.eurocarbdb.action.nmr;

import java.io.*;

// 3rd party imports
import com.opensymphony.webwork.dispatcher.multipart.MultiPartRequestWrapper;
import com.opensymphony.webwork.ServletActionContext;

// eurocarb imports
import org.eurocarbdb.action.EurocarbAction;
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.action.exception.InsufficientParams;

// static imports
import static org.eurocarbdb.util.StringUtils.join;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


public class PictureStream extends Casper
{
    private String pictureFileName;
    private InputStream picture;

    public String getPictureFileName()
    {
    return this.pictureFileName;
    }
    public void setPictureFileName(String name)
    {
    System.out.println("In setPictureFileName");
    this.pictureFileName=name;
    }
    public InputStream getPicture()
    {
    return this.picture;
    }
    public void setPicture(InputStream stream)
    {
    this.picture=stream;
    }

    public String execute()
    {
    String fullPath;
    
    try
        {        
        fullPath=createTempDirectory() + File.separator + getPictureFileName();
        
        System.out.println("Piping picture: " + fullPath);
    
        picture=new FileInputStream(fullPath);
        }
    catch (IOException e)
        {
        return ERROR;
        }
    return SUCCESS;
    }
}