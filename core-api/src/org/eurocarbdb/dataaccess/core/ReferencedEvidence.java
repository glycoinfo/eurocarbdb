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
*   Last commit: $Rev: 1593 $ by $Author: hirenj $ on $Date:: 2009-08-14 #$  
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
*   Encapsulates the association between a piece of {@link Evidence} 
*   and a {@link Reference}.  
*
*   @author mjh
*/
public class ReferencedEvidence extends BasicEurocarbObject 
implements Contributed, Serializable 
{
    
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /** logging handle */
    static Logger log = Logger.getLogger( ExperimentContext.class );
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    private int referencedEvidenceId;
    
    private Reference reference;
    
    private Evidence evidence;

    /** The contributor of this object; defaults to the current Contributor. 
    *   note that we *cannot* initialise this property at construction 
    *   time as it causes hibernate to go into an endless intialisation loop. */
    private Contributor contributor = null;
    
    /** The date this objects was created/entered into the data store. */
    private Date dateEntered = new Date();


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~~~

    public ReferencedEvidence()  {}
    
    
    /** full constructor */
    public ReferencedEvidence( Reference ref, Evidence ev ) 
    {
        this.reference = ref;
        this.evidence = ev;
    }
    

    //~~~~~~~~~~~~~~~~~~~~~~ STATIC METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~
   
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public int getReferencedEvidenceId() 
    {
        return this.referencedEvidenceId;
    }
    
    
    public Reference getReference() 
    {
        return this.reference;
    }
    
    
    public void setReference( Reference r ) 
    {
        this.reference = r;
    }
    
    
    public Evidence getEvidence() 
    {
        return this.evidence;
    }
    
    
    public void setEvidence( Evidence e ) 
    {
        this.evidence = e;
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

    public boolean equals( Object o ) 
    {
        if( ! (o instanceof ReferencedEvidence) )
            return false;
    
        ReferencedEvidence other = (ReferencedEvidence) o;
        
        if( this.reference == null || other.reference == null )
            return false;
        
        if ( this.reference.getReferenceId() != other.reference.getReferenceId() )        
            return false;
        
        if( this.evidence == null || other.evidence == null )
            return false;

        if( this.evidence.getEvidenceId() != other.evidence.getEvidenceId() )        
            return false;

        return true;
    }
    

    public int hashCode() 
    {    
        int code = 0;
        if( evidence != null )
            code ^= evidence.getEvidenceId();
        
        if( reference != null )
            code ^= reference.getReferenceId();
    
        return code;
    }    

    //~~~~~~~~~~~~~~~~~~~~~~ PRIVATE METHODS ~~~~~~~~~~~~~~~~~~~~~~~~
    
    void setReferencedEvidenceId( int id ) 
    {
        this.referencedEvidenceId = id;
    }

} // end class


