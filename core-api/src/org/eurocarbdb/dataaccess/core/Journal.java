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
*   Last commit: $Rev: 1472 $ by $Author: hirenj $ on $Date:: 2009-07-10 #$  
*/
package org.eurocarbdb.dataaccess.core;
// Generated Oct 23, 2007 1:31:28 PM by Hibernate Tools 3.2.0.b9


import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

import org.apache.log4j.Logger;

import org.eurocarbdb.util.StringUtils;
import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.dataaccess.BasicEurocarbObject;

/**
*   Represents a journal or periodical.
*/
public class Journal extends BasicEurocarbObject implements Serializable 
{
    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//
    
    /** Logging handle. */
    static final Logger log = Logger.getLogger( Journal.class );

        
    private int journalId;
    
    private String journalTitle;
    
    private String journalAbbrev;
    
    
    //~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~~//    
    
    public Journal() 
    {
    }


    public Journal( String title ) 
    {
        this.journalTitle = title;
    }
    
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//    
    
    public Journal storeOrLookup() 
    {
        Journal j = lookupByTitle( journalTitle );
            if ( j!=null ) 
            return j;    
    
        Eurocarb.getEntityManager().store(this);
        return this;
    }

    /**
    *   Returns an existing Journal if the given journal_name argument
    *   matches a journal in the data store, otherwise returns a new 
    *   Journal object with its title property set to the given 
    *   journal name.
    */
    public static Journal createOrLookup( String journal_name )
    {
        assert journal_name != null;
        if (journal_name.length() == 0) {
            return null;
        }
        assert journal_name.length() > 3;
        
        Journal j = lookupByTitle( journal_name );
        if ( j == null )  
        {
            if ( log.isDebugEnabled() )
                log.debug( "journal with title '" 
                         + journal_name 
                         + "' not found so creating new Journal entry"
                         );  
                
            j = new Journal();
            j.setJournalTitle( journal_name );

            //log.debug("new journal has id=" + j.getJournalId() ); 
            
            //Eurocarb.getEntityManager().store( j );
            //log.debug("new journal has id=" + j.getJournalId() ); 
        }
        
        
        return j;
    }
    
    
    /**
    *   Retrieves a {@link Journal} by matching journal name.
    */
    public static Journal lookupByTitle( String journal_name )
    {
        assert journal_name != null;
        assert journal_name.length() > 3;
        log.debug("looking up Journal by journal name '" + journal_name + "'");
        
        Journal j = (Journal) Eurocarb.getEntityManager()
                    .getQuery("org.eurocarbdb.dataaccess.core.Journal.BY_MATCHING_JOURNAL_NAME")
                    .setParameter("journal_name", journal_name )
                    .uniqueResult();
                    
        if ( j == null ) 
            log.debug("Journal not found");
        
        return j;
    }
    
    
    /**
    *   Returns the {@link List} of all {@link JournalReference}s that 
    *   reference this specific journal.
    *   @throws UnsupportedOperationException 
    *   if this journal doesn't have a positive non-zero journal id
    *   (ie: it is unsaved).
    */
    @SuppressWarnings("unchecked")
    public List<JournalReference> getAllJournalReferences() 
    throws UnsupportedOperationException
    {
        int id = this.getJournalId(); 
        if ( id <= 0 )
            throw new UnsupportedOperationException(
                "Cannot retrieve JournalReferences for a new or unsaved Journal");
        
        log.debug("looking up JournalReferences by journal id '" + id + "'");
        
        List rlist = Eurocarb.getEntityManager()
                    .getQuery("org.eurocarbdb.dataaccess.core.Journal.GET_ALL_REFERENCES")
                    .setParameter("journal_id", id )
                    .list();
                    
        return (List<JournalReference>) rlist;
    }
    

    public int getJournalId() 
    {
        return this.journalId;
    }
    
    public void setJournalId(int journalId) 
    {
        this.journalId = journalId;
    }
    
    public String getJournalTitle() 
    {
        return this.journalTitle;
    }
    
    public void setJournalTitle( String name ) 
    {
        this.journalTitle = name;
    }
    
    public String getJournalAbbrev() 
    {
        if ( this.journalAbbrev != null )
            return this.journalAbbrev;
        else 
            return __guess_abbrev();
    }
    
    public void setJournalAbbrev(String journalAbbrev) 
    {
        this.journalAbbrev = journalAbbrev;
    }

    
    public int hashCode()
    {
        String unique = journalTitle;
        return unique.hashCode(); 
    }
    
    
    public boolean equals( Object x )
    {
        if ( this == x ) {
            return true;
        }
        
        if ( (x == null) || (x.getClass() != this.getClass()) ) {
            return false;
        }

        // objects are the same class
        Journal j = (Journal) x;

        return j.journalTitle.equals(this.journalTitle);
    }
    

    //~~~~~~~~~~~~~~~~~~~~~~ PRIVATE METHODS ~~~~~~~~~~~~~~~~~~~~~~~~//    
    
    private final String __guess_abbrev()
    {
        assert journalTitle != null;   
        assert journalTitle.length() > 0;
        return StringUtils.guessAbbreviationFor( journalTitle );
    }
    
} // end class


