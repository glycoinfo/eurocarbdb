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
*   Last commit: $Rev: 1211 $ by $Author: glycoslave $ on $Date:: 2009-06-13 #$  
*/
package org.eurocarbdb.dataaccess.core;


//  stdlib imports
import java.util.Date;
import java.io.Serializable;

//  3rd party imports
import org.apache.log4j.Logger;

//  eurocarb imports
import org.eurocarbdb.dataaccess.Contributed;
import org.eurocarbdb.dataaccess.BasicEurocarbObject;

//  static imports
import static org.eurocarbdb.util.JavaUtils.checkNotNull;


/**
*   Encapsulates the association between a {@link BiologicalContext} 
*   and an {@link Experiment}.  
*
*   @author mjh
*/
public class EvidenceContext extends BasicEurocarbObject 
implements Contributed, Serializable 
{
    
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~

    /** logging handle */
    static Logger log = Logger.getLogger( EvidenceContext.class );
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

     private int evidenceContextId;
     
     private BiologicalContext biologicalContext;
     
     private Evidence evidence;
     
    /** The contributor of this object; defaults to the current Contributor. 
    *   note that we *cannot* initialise this property at construction 
    *   time as it causes hibernate to go into an endless intialisation loop. */
    private Contributor contributor = null;
     
    /** The date this objects was created/entered into the data store. */
    private Date dateEntered = new Date();


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    public EvidenceContext() 
    {
    }

    
    public EvidenceContext( Evidence ev, BiologicalContext bc ) 
    {
       this.biologicalContext = bc;
       this.evidence = ev;
    }
    
   
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~    
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~    
    
    public int getEvidenceContextId() 
    {
        return this.evidenceContextId;
    }
    
    
    public BiologicalContext getBiologicalContext() 
    {
        return this.biologicalContext;
    }
    
    
    public void setBiologicalContext( BiologicalContext bc ) 
    {
        checkNotNull( bc );
        this.biologicalContext = bc;
    }
    
    
    public Evidence getEvidence() 
    {
        return this.evidence;
    }
    
    
    public void setEvidence( Evidence ev ) 
    {
        checkNotNull( ev );
        this.evidence = evidence;
    }


    /* implementation of Contributed interface */
    
    /** Returns the original contributor of this object. */
    public Contributor getContributor() 
    {
        if ( this.contributor == null )
            setContributor( Contributor.getCurrentContributor() );
        
        return this.contributor;
    }
    
    
    /** Sets the contributor of this object. */
    public void setContributor( Contributor c ) 
    {
        checkNotNull( c );
        this.contributor = c;
    }
    

    /** 
    *   Returns the {@link Date} this object was created. 
    *   Defaults to the date/time this object was instantiated. 
    */
    public Date getDateEntered() 
    {
        return this.dateEntered;
    }
    
    
    /** Sets the {@link Date} this object was created. */
    public void setDateEntered( Date date ) 
    {
        checkNotNull( date );
        this.dateEntered = dateEntered;
    }


    public void validate()
    {
        //  todo    
        super.validate();
    }
    

    //~~~~~~~~~~~~~~~~~~~~~~ PRIVATE METHODS ~~~~~~~~~~~~~~~~~~~~~~~~    
    
    void setEvidenceContextId( int id ) 
    {
        this.evidenceContextId = id;
    }
    
    
} // end class


