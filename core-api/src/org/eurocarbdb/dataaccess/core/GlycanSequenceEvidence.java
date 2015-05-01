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
*   Encapsulates the association between a {@link GlycanSequence}
*   and a single piece of {@link Evidence} in which this sequence 
*   has been identified. 
*
*   @see GlycanSequence#addEvidence
*   @author mjh
*/
public class GlycanSequenceEvidence extends BasicEurocarbObject 
implements Contributed, Serializable 
{
    
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /** logging handle */
    static Logger log = Logger.getLogger( GlycanSequenceEvidence.class );
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
     private int glycanSequenceEvidenceId;
     
     private GlycanSequence glycanSequence;
     
     private Evidence evidence;
     
     private Double quantitationByPercent;

    /** The contributor of this object; defaults to the current Contributor. 
    *   note that we *cannot* initialise this property at construction 
    *   time as it causes hibernate to go into an endless intialisation loop. */
    private Contributor contributor = null;
     
    /** The date this objects was created/entered into the data store. */
    private Date dateEntered = new Date();


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~~~

    /** default constructor */
    public GlycanSequenceEvidence() 
    {
        this.quantitationByPercent = 0.;
    }

    
    /** minimal constructor */
    public GlycanSequenceEvidence( GlycanSequence glycanSequence, Evidence evidence ) 
    {
        this.glycanSequence = glycanSequence;
        this.evidence = evidence;
        this.quantitationByPercent = 0.;
    }
    
    
    /** full constructor */
    public GlycanSequenceEvidence( GlycanSequence glycanSequence, 
                                   Evidence evidence, 
                                   Double quantitationByPercent ) 
    {
        this.glycanSequence = glycanSequence;
        this.evidence = evidence;
        this.quantitationByPercent = quantitationByPercent;
    }
    

    //~~~~~~~~~~~~~~~~~~~~~~ STATIC METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~
   
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /** Returns this object's unique id */
    public int getGlycanSequenceEvidenceId() 
    {
        return this.glycanSequenceEvidenceId;
    }
    

    /** Returns the GlycanSequence that is part of this association */
    public GlycanSequence getGlycanSequence() 
    {
        return this.glycanSequence;
    }
    
    
    /** Sets the GlycanSequence that is part of this association */
    void setGlycanSequence( GlycanSequence glycanSequence ) 
    {
        this.glycanSequence = glycanSequence;
    }

    
    /** Returns the Evidence that is part of this association. */
    public Evidence getEvidence() 
    {
        return this.evidence;
    }
    
    
    /** Sets the Evidence that is part of this association. */    
    void setEvidence( Evidence evidence ) 
    {
        this.evidence = evidence;
    }

    
    /** 
    *   Returns the amount of the GlycanSequence that is found in the 
    *   associated Evidence contained as a proportional percentage.
    */
    public double getQuantitationByPercent() 
    {
        if( this.quantitationByPercent == null )
            return 0;
        
        return this.quantitationByPercent;
    }
    
    
    /** 
    *   Sets the amount of the GlycanSequence that is found in the 
    *   associated Evidence contained as a proportional percentage.
    */
    public void setQuantitationByPercent( double quantitationByPercent ) 
    {
        this.quantitationByPercent = quantitationByPercent;
    }


    public boolean equals(Object o) 
    {
        if( !(o instanceof GlycanSequenceEvidence) )
            return false;
    
        GlycanSequenceEvidence other = (GlycanSequenceEvidence) o;
        if( this.evidence==null && other.evidence!=null )
            return false;
        if( this.evidence!=null && other.evidence==null )
            return false;
        if ( this.evidence!=null 
            && other.evidence!=null 
            && this.evidence.getEvidenceId()!=other.evidence.getEvidenceId() )        
            return false;
        
        if( this.glycanSequence==null && other.glycanSequence!=null )
            return false;
        if( this.glycanSequence!=null && other.glycanSequence==null )
            return false;
        if( this.glycanSequence!=null && other.glycanSequence!=null && this.glycanSequence.getGlycanSequenceId()!=other.glycanSequence.getGlycanSequenceId() )        
            return false;
        
        return true;
    }
    

    public int hashCode() 
    {    
        int code = 0;
        if( glycanSequence!=null )
            code += glycanSequence.getGlycanSequenceId();
        if( evidence!=null )
            code += evidence.getEvidenceId();
        
        return code;
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
    
    /** Sets this object's unique id; private method. */
    void setGlycanSequenceEvidenceId( int glycanSequenceEvidenceId ) 
    {
        this.glycanSequenceEvidenceId = glycanSequenceEvidenceId;
    }

} // end class
