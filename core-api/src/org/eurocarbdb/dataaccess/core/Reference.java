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
*   Last commit: $Rev: 1314 $ by $Author: glycoslave $ on $Date:: 2009-06-29 #$  
*/

package org.eurocarbdb.dataaccess.core;


//  stdlib imports 
import java.util.List;
import java.util.Set;
import java.util.Date;
import java.util.HashSet;
import java.io.Serializable;

//  3rd party imports 
import org.apache.log4j.Logger;

//  eurocarb imports 
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.dataaccess.EurocarbObject;
import org.eurocarbdb.dataaccess.BasicEurocarbObject;
import org.eurocarbdb.dataaccess.Contributed;
import org.eurocarbdb.dataaccess.Eurocarb;

//  static imports 
import static java.util.Collections.unmodifiableSet;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/**
*   Encapsulates an external information or data source. This 
*   includes, but is not limited to, literature references,
*   links to entries in other external databases
*
*   @see GlycanSequence#addReference(Reference)
*   @see Evidence#addReference(Reference)
*   @see GlycanSequenceReference
*   @see ReferencedEvidence
*
*   @author mjh
*/
public class Reference extends BasicEurocarbObject 
implements Serializable, Contributed 
{

    /** Logging handle. */
    static final Logger log  = Logger.getLogger( Reference.class );

    /** Enumeration of supported reference types. @see #getReferenceType */
    public enum Type
    {
        Journal("journal"),
        DatabaseEntry("database"),
        Web("website")
        ;
        
        /** string id used in the database */
        String id;
        
        Type( String id ) {  this.id = id;  }
        
        public final String toString() {  return id;  }
    }
    
    
    //~~~~~~~~~~~~~~~~~~~~~  STATIC FIELDS  ~~~~~~~~~~~~~~~~~~~~~~~~~
    
    private static final String Q = "org.eurocarbdb.dataaccess.core.Reference."; 
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~  FIELDS  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private int referenceId;
     
    /** The contributor of this reference. */
    private Contributor contributor;
    
    /** Date this reference was created in the data store. */
    private Date dateEntered;
    
    /** The {@link Type type} of this reference -- this should be 
    *   changed to a Type enum in the future!!  */
    private String referenceType;
    
    /** mjh: This is a temporary variable that holds the enum version of 
    *   String {@link #referenceType} -- remove this variable when 
    *   hibernate is configured to convert to/from enums directly!! */
    private Type refType; 
    
    /** The Id of this reference as given by the 3rd party to which 
    *   this reference belongs  */
    private String externalReferenceId;
    
    /** Name of 3rd party to which this reference belongs */
    private String externalReferenceName;
     
    private String url;
     
    /** Any relevant extra info as appropriate to the type of reference. */
    private String referenceComments;
     
    /** The {@link Set} of {@link Evidence} that is linked to this ref. */
    private Set<ReferencedEvidence> referencedEvidence 
        = new HashSet<ReferencedEvidence>(0);
     
    /** The {@link Set} of {@link GlycanSequence}s that are linked to this ref. */
    private Set<GlycanSequenceReference> glycanSequenceReferences 
        = new HashSet<GlycanSequenceReference>(0);

    
    //~~~~~~~~~~~~~~~~~~~~~~  CONSTRUCTORS  ~~~~~~~~~~~~~~~~~~~~~~~~~

    /** default constructor */
    public Reference() 
    {
    }
    

    @Deprecated
    public Reference storeOrLookup() throws Exception 
    {
        // just store
        EntityManager em = getEntityManager();
        em.store(this);
        return this;
    }
           

    //~~~~~~~~~~~~~~~~~~~~~  STATIC METHODS  ~~~~~~~~~~~~~~~~~~~~~~~~
    
    /** Returns a count of all {@link Reference}s. */
    @Deprecated
    public static long countAllReferences()
    {
        Long count = (Long) Eurocarb.getEntityManager()
                            .getQuery( Q + "COUNT_ALL" )
                            .uniqueResult();
                            
        return count.intValue();
    }

    
    //~~~~~~~~~~~~~~~~~~~~~~~~  METHODS  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /** 
    *   Completes the association of a {@link GlycanSequence} to 
    *   this {@link Reference}.
    *
    *   @see GlycanSequence#addReference.
    */
    boolean addGlycanSequenceReference( GlycanSequenceReference gsr )
    {
        assert gsr != null;
        return glycanSequenceReferences.add( gsr );             
    }
    
    
    /** 
    *   Completes the association of a piece of {@link Evidence} to 
    *   this {@link Reference}.
    *
    *   @see Evidence#addReference.
    */
    boolean addReferencedEvidence( ReferencedEvidence re )
    {
        assert re != null;
        return referencedEvidence.add( re );
    }
    
    
    public Date getDateEntered()
    {
        return dateEntered;   
    }
    
    
    public int getReferenceId() 
    {
        return this.referenceId;
    }
    
    
    /** Returns the contributor of this reference. */
    public Contributor getContributor() 
    {
        if ( contributor == null )
            contributor = Contributor.getCurrentContributor();
        
        return this.contributor;
    }
    
    
    /** Sets the contributor of this reference. */
    public void setContributor( Contributor c ) 
    {
        this.contributor = c;
    }
    
    
    /** Returns the {@link Type type} of this reference. */
    /*public Reference.Type getReferenceType() 
    {
        return this.refType;
    }
    */

    
    public String getReferenceType() 
    {
        return this.referenceType;
    }
    

    public void setReferenceType( String t ) 
    {
        if( t==null )
            setReferenceTypePVT(Type.Web);
        else if( t.equals(Type.Journal.toString()) )
            setReferenceTypePVT(Type.Journal);
        else if( t.equals(Type.DatabaseEntry.toString()) )
            setReferenceTypePVT(Type.DatabaseEntry);
        else
            setReferenceTypePVT(Type.Web);
    }

    
    /** Sets the {@link Type type} of this reference. */
    private void setReferenceTypePVT( Reference.Type t ) 
    {
        if( t!=null ) 
        {
            this.refType = t;
            this.referenceType = t.toString();
        }
        else 
        {
            this.refType = null;
            this.referenceType = null;
        }
    }
    
    
    /** 
    *   Returns the 3rd party identifier that effectively constitutes 
    *   this reference. For a {@link JournalReference}, this should
    *   be the pubmed id, for an external database reference, this
    *   would be the external database id.
    *
    *   @see #setExternalReferenceId
    *   @see #getExternalReferenceName
    *   @see #setExternalReferenceName
    */
    public String getExternalReferenceId() 
    {
        return this.externalReferenceId;
    }
    
    
    /** 
    *   Sets the 3rd party identifier that constitutes 
    *   this reference. 
    *
    *   @see #getExternalReferenceId
    *   @see #getExternalReferenceName
    *   @see #setExternalReferenceName
    */
    public void setExternalReferenceId( String id ) 
    {
        this.externalReferenceId = id;
    }
    
    
    /** 
    *   Returns an identifier (name) of the 3rd party to which
    *   this reference belongs, eg: journal references are normally
    *   'Pubmed'.
    *
    *   @see #getExternalReferenceId
    *   @see #setExternalReferenceId
    *   @see #setExternalReferenceName
    */
    public String getExternalReferenceName() 
    {
        return this.externalReferenceName;
    }

    
    /** 
    *   Returns the 3rd party identifier that constitutes 
    *   this reference. 
    *
    *   @see #getExternalReferenceId
    *   @see #setExternalReferenceId
    *   @see #getExternalReferenceName
    */
    public void setExternalReferenceName( String name ) 
    {
        this.externalReferenceName = name;
    }
    
    
    /** 
    *   Returns an unmodifiable {@link Set} of {@link GlycanSequenceReference}s,
    *   which represent the Set of {@link GlycanSequence}s that this {@link Reference} 
    *   has been associated with.
    */
    public Set<GlycanSequenceReference> getGlycanSequenceReferences() 
    {
        return unmodifiableSet( this.glycanSequenceReferences );
    }
    
    
    /** 
    *   Returns an unmodifiable {@link Set} of {@link ReferencedEvidence},
    *   representing the Set of {@link Evidence} that this {@link Reference} 
    *   has been associated with.
    */
    public Set<ReferencedEvidence> getReferencedEvidence() 
    {
        return unmodifiableSet( this.referencedEvidence );
    }
    
    
    /** 
    *   Returns a direct URL to this reference on the web;
    *   note that this method may return null depending on
    *   the {@link Type} of the reference.
    */
    public String getUrl() 
    {
        return this.url;
    }
    
    
    /** Returns a direct URL to this reference on the web  */
    public void setUrl( String url ) 
    {
        this.url = url;
    }
    
    
    /** Returns miscellaneous reference comments, if any. */
    public String getReferenceComments() 
    {
        return this.referenceComments;
    }
    
    
    /** Sets miscellaneous reference comments (not required). */
    public void setReferenceComments( String comments ) 
    {
        this.referenceComments = comments;
    }
    
    
    public int hashCode()
    {
        String unique = "" 
        + referenceId 
        + refType 
        + externalReferenceId;
    
        if( getContributor()!=null )
            unique += getContributor().hashCode();
        
        return unique.hashCode(); 
    }
    
    
    /** */
    public boolean equals( Object x )
    {
        if ( this == x )
            return true;

        if ( (x == null) || ! (x instanceof Reference) ) {
            return false;
        }

        // objects are the same class
        Reference r = (Reference) x;

        return r.referenceId == this.referenceId
            && r.refType == this.refType 
            && r.externalReferenceId == this.externalReferenceId 
            && r.getContributor() == this.getContributor()
            ;
    }

    
    /** 
    *   Returns an enhanced debug string for {@link Reference}s,
    *   which includes external reference name & id in addition to 
    *   class name plus id. 
    */
    public String toString() 
    {
        return  "[" 
            +   this.getClass().getSimpleName() 
            +   "=" 
            +   this.getId() 
            +   "; " 
            +   this.getExternalReferenceName()
            +   "="
            +   this.getExternalReferenceId()
            +   "]"
            ;
    }
    
    
    @Override
    public int getId()
    {
        return getReferenceId();    
    }
    
    
    @Override
    public Class<? extends EurocarbObject> getIdentifierClass()
    {
        return Reference.class;    
    }
    
    
    //~~~~~~~~~~~~~~~~~~~~~ PRIVATE METHODS ~~~~~~~~~~~~~~~~~~~~~~~//    
    
    void setReferenceId( int referenceId ) 
    {
        this.referenceId = referenceId;
    }

    
    void setReferencedEvidence( Set<ReferencedEvidence> ev2ref ) 
    {
        this.referencedEvidence = ev2ref;
    }
    
    
    void setGlycanSequenceReferences( Set<GlycanSequenceReference> gs2ref ) 
    {
        this.glycanSequenceReferences = gs2ref;
    }

} // end class

