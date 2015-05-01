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

package org.eurocarbdb.action.ms;

//  stdlib imports
import java.io.*;

//  3rd party imports
import org.apache.log4j.Logger;

//  eurocarb imports

import org.eurocarbdb.tranche.TrancheUtility;
import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.action.EurocarbAction;

//import com.opensymphony.xwork.ActionSupport;

/**
*
*   @author aceroni
*/
public class GetFile extends EurocarbAction
{
    private static final long serialVersionUID = 1L;

    /** Logging handle. */
    private static final Logger log = Logger.getLogger( GetScanImage.class );
    
    private String uri = null;

    private File file = null;
    
    public void setUri(String s)
    {
    uri = s;
    }
    
    public String getUri()
    {
    return uri;
    }

    public InputStream getStream() 
    {  
    try {
        if( file!=null ) 
        return new FileInputStream(file);
        return new ByteArrayInputStream(new byte[0]);    
    }
    catch(Exception e) {
        return new ByteArrayInputStream(new byte[0]);    
    }
    }

    /**
     * @see com.opensymphony.xwork.ActionSupport#execute()
     */
    @Override
    public String execute() throws Exception
    {
      
    if( uri==null || uri.length()==0 ) {
        this.addFieldError( "uri", "You must specified a valid URI" );
        return "error";
    }

    file = TrancheUtility.downloadFile(new java.net.URI(uri));
    return "success";
       
    } // end execute()
    
}
