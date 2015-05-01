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

import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.dataaccess.ms.ScanImage;
import org.eurocarbdb.action.EurocarbAction;

//import com.opensymphony.xwork.ActionSupport;

/**
*
*   @author aceroni
*/
public class GetScanImage extends EurocarbAction
{
    private static final long serialVersionUID = 1L;

    /** Logging handle. */
    private static final Logger log = Logger.getLogger( GetScanImage.class );
    
    private int scanImageId = -1;
    
    private ScanImage scanImage = null;

    private String which = "full";
    
    public void setScanImageId(int id)
    {
    scanImageId = id;
    }
    
    public int getScanImageId()
    {
        return scanImageId;
    }

    public void setScanImage(ScanImage si)
    {
    scanImage = si;
    }
    
    public ScanImage getScanImage()
    {
        return scanImage;
    }           

    public void setWhich(String s) {
    which = s;
    }

    public String getWhich() {
    return which;
    }

    public InputStream getStream() 
    {  
    if( scanImage!=null ) {
        if( which.equals("fullsize") )
        return new ByteArrayInputStream(scanImage.getFullSize());
        else if( which.equals("mediumsize") )
        return new ByteArrayInputStream(scanImage.getMediumSize());
        else if( which.equals("thumbnail") )
        return new ByteArrayInputStream(scanImage.getThumbnail());
        else if( which.equals("report") )
        return new ByteArrayInputStream(scanImage.getAnnotationReport());
        else
        return new ByteArrayInputStream(scanImage.getFullSize());
    }
    return new ByteArrayInputStream(new byte[0]);    
    }

    /**
     * @see com.opensymphony.xwork.ActionSupport#execute()
     */
    @Override
    public String execute() throws Exception
    {
      
    if( scanImage==null && (scanImageId<=0 || (scanImage = Eurocarb.getEntityManager().lookup( ScanImage.class, scanImageId))==null) ) {
        this.addFieldError( "scanImageId", "Invalid scanImageId " + scanImageId );
        return "error";
    }
    if( which.equals("report") )
        return "report";
    return "image";
       
    } // end execute()
    
}
