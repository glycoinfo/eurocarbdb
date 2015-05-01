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
*   Last commit: $Rev: 1995 $ by $Author: khaleefah $ on $Date:: 2010-10-27 #$  
*/
/**
* $Id: ShowAcquisition.java 1995 2010-10-26 20:06:16Z khaleefah $
* Last changed $Author: khaleefah $
* EUROCarbDB Project
*/

package org.eurocarbdb.action.ms;

import org.eurocarbdb.action.*;
import org.eurocarbdb.dataaccess.*;
import org.eurocarbdb.dataaccess.ms.*;
import org.eurocarbdb.dataaccess.core.*;
import org.eurocarbdb.util.XmlSerialiser;

import java.io.*;
import java.util.Date;
import java.util.Set;
import org.apache.log4j.Logger;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;

/**
*   Detail action for a mass-spec {@link Acquisition}, given
*   either a acquisitionId or evidenceId param (acquisitionId
*   parameter takes precedence if evidenceId is also given).
*   
*   @author           aceroni, mjh
*   @version          $Rev: 1995 $
*/
public class ShowAcquisition extends AbstractMsAction 
{

    protected static final Logger log = Logger.getLogger( ShowAcquisition.class.getName() );

    private Acquisition acquisition = null;

    private int evidence_id = -1;

    private int acquisition_id = -1;

    private Boolean output_xml = false;
    
    private Set<Scan> scans = null;
    
    private Date dateEntered = null;
    
    private String contributorName = null;
    
    
    public Acquisition getAcquisition() 
    {      
        return acquisition;  
    }

    public void setAcquisition(Acquisition a) 
    {      
        acquisition = a;
    }
  
    public void setEvidenceId(int id) 
    {
        this.evidence_id = id;
    }

    public int getEvidenceId() 
    {
        return this.evidence_id;
    }

    public void setAcquisitionId(int id) 
    {
        this.acquisition_id = id;
    }

    public int getAcquisitionId() 
    {
        return this.acquisition_id;
    }

    public void setOutputXml(Boolean f) {
    output_xml = f;    
    }

    public InputStream getStream()
    {  
    try {
        StringWriter writer = new StringWriter();
        
        // create XML
        XmlSerialiser xs = new XmlSerialiser();
        xs.setWriter(writer);
        xs.serialise(acquisition,true);
        
        // return stream
        return new ByteArrayInputStream(writer.toString().getBytes());
    }
    catch( Exception e) {
        return new ByteArrayInputStream(new byte[0]);
    }
    }

    public String execute() 
    {
    	//Scan s = new Scan();
   
       if( acquisition!=null ) {
            // retrieve acquisition
            acquisition = Eurocarb.getEntityManager().lookup( Acquisition.class, acquisition.getAcquisitionId() );    
            if ( acquisition == null ) 
            {
                this.addActionError( "No acquisition associated with id " + acquisition.getAcquisitionId() );
                return ERROR;        
            }
        if( output_xml )
        return "success_xml";
        setScans(acquisition.getScans());
        return SUCCESS;
    }
    
    if( acquisition_id>0 ) {
        acquisition = Acquisition.lookupById(acquisition_id);
        if( acquisition==null ) {
        this.addFieldError("acquisitionId", "Invalid acquisitionId: " + acquisition_id);
        return ERROR;
        }
        if( output_xml )
        return "success_xml";
        setScans(acquisition.getScans());
        return SUCCESS;
    }
    
    if( evidence_id>0 ) {
        Evidence e = getEntityManager().lookup( Evidence.class, evidence_id );
        if( e==null ) {
        this.addFieldError("evidenceId", "Invalid evidenceId: " + evidence_id);
        return ERROR;
        }
        if( ! (e instanceof Acquisition) ) {
        this.addActionError( "Experimental technique is not MS" );
        return ERROR;
        }
        
        acquisition = (Acquisition) e;
        if( output_xml )
        return "success_xml";
        setScans(acquisition.getScans());
        return SUCCESS;
    }
    
    this.addActionError( "Nothing to show" );
    return ERROR;
    }

	public void setScans(Set<Scan> scans) {
		this.scans = scans;
	}

	public Set<Scan> getScans() {
		return scans;
	}

	public void setDateEntered(Date dateEntered) {
		this.dateEntered = dateEntered;
	}

	public Date getDateEntered() {
		return dateEntered;
	}

	public void setContributorName(String contributorName) {
		this.contributorName = contributorName;
	}

	public String getContributorName() {
		return contributorName;
	}

} // end class
