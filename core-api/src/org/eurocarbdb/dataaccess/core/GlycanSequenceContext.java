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
*   Last commit: $Rev: 1228 $ by $Author: hirenj $ on $Date:: 2009-06-17 #$  
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
*   and a {@link BiologicalContext} (in which it has been found).  
*
*   @see GlycanSequence#addBiologicalContext
*   @author mjh
*/
public class GlycanSequenceContext extends BasicEurocarbObject 
implements Contributed, Serializable 
{
    
    //~~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /** logging handle */
    static Logger log = Logger.getLogger( GlycanSequenceContext.class );
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        
    private int glycanSequenceContextId;
    
    private BiologicalContext biologicalContext;
    
    private GlycanSequence glycanSequence;

    private int glycanSequenceId;
    
    private int biologicalContextId;

    /** The contributor of this object; defaults to the current Contributor. 
    *   note that we *cannot* initialise this property at construction 
    *   time as it causes hibernate to go into an endless intialisation loop. */
    private Contributor contributor = null;
    
    /** The date this objects was created/entered into the data store. */
    private Date dateEntered = new Date();


    //~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~~~

    /** default constructor */
    public GlycanSequenceContext()  {}

    
    /** full constructor */
    public GlycanSequenceContext( BiologicalContext bc, GlycanSequence gs ) 
    {
        this.biologicalContext = bc;
        this.glycanSequence = gs;
    }
    

    //~~~~~~~~~~~~~~~~~~~~~~ STATIC METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~
   
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /*
     * This method is potentially very fragile. For session re-attachment with
     * Hibernate, we need to be able to calculate the hashCode for this object
     * without querying its dependent objects. So we cache the biologicalContextId
     * and glycanSequenceId in the object. If there is no glycanSequenceContextId
     * we don't trust the cached ids, but if there is one, we can query the
     * cached ids.
     */
    public int hashCode()
    {
        if (this.glycanSequenceContextId == 0) {
            return (""+this.biologicalContext.getBiologicalContextId()+""+this.glycanSequence.getGlycanSequenceId()).hashCode();
        } else {
            return (""+this.biologicalContextId+""+this.glycanSequenceId).hashCode();
        }
    }
    
    public boolean equals(Object o)
    {
        if((o == null) || (o.getClass() != this.getClass())) return false;
        return this.hashCode() == o.hashCode();
    }

    public int getGlycanSequenceContextId() 
    {
        return this.glycanSequenceContextId;
    }
    
    
    public BiologicalContext getBiologicalContext() 
    {
        return this.biologicalContext;
    }
    
    
    public void setBiologicalContext(BiologicalContext biologicalContext) 
    {
        this.biologicalContext = biologicalContext;
        this.biologicalContextId = biologicalContext.getBiologicalContextId();
    }

    
    public GlycanSequence getGlycanSequence() 
    {
        return this.glycanSequence;
    }
    
    
    public void setGlycanSequence(GlycanSequence glycanSequence) 
    {
        this.glycanSequence = glycanSequence;
        this.glycanSequenceId = glycanSequence.getGlycanSequenceId();
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
    
    void setGlycanSequenceContextId( int id ) 
    {
        this.glycanSequenceContextId = id;
    }

} // end class
