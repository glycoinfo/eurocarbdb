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
*   Last commit: $Rev: 1932 $ by $Author: glycoslave $ on $Date:: 2010-08-05 #$  
*/

package org.eurocarbdb.util.carbbank;

import java.util.Map;
import java.util.List;
import java.util.Collections;

import org.apache.log4j.Logger;

import org.eurocarbdb.dataaccess.core.*;
import org.eurocarbdb.sugar.SugarSequence;
import org.eurocarbdb.sugar.SequenceFormat;
import org.eurocarbdb.sugar.seq.GlycoctSequenceFormat;

import static org.eurocarbdb.util.StringUtils.*;
import static org.eurocarbdb.util.carbbank.CarbbankParser.*;

import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/**
*<p>
*   Represents an individual Carbbank structural record.
*</p>
*<p>
*   A typical Carbbank record looks like this:
*</p>
*<pre>   
    ; start of record
    ; db=ccsd29
    ; Record #=14
    CC: CCSD:43759
    AU: Albersheim P; Darvill A; Augur C; Cheong JJ; Eberhard S; Hahn MG;
    Marfa V; Mohnen D; O'Neill MA; Spiro MD; York WS
    TI: Oligosaccharins: Oligosaccharide regulatory molecules
    CT: Acc Chem Res (1992) 25: 77-83
    BS: (GS) Hansenula holstii
    BS: (GS) Pichia holstii
    SC: 14
    TN: XXFG
    MT: xyloglucan
    SB: Westra B
    DA: 06-12-1995
    FC: 66bf2505
    SI: CBank:17702
    ----------------
    structure:
    
     a-L-Fucp-(1-2)-b-D-Galp-(1-2)-a-D-Xylp-(1-6)+
                                                 |
                          a-D-Xylp-(1-6)+   b-D-Glcp-(1-4)-D-Glc
                                        |        |
                                   b-D-Glcp-(1-4)+
                                        |
           a-D-Xylp-(1-6)-b-D-Glcp-(1-4)+
    ================end of record
*</pre>
*<p>
*   This class provides an interface by which to extract data from
*   individual records and return this information in terms of Eurocarb
*   data objects.  
*</p>
*
*   @author mjh [glycoslave@gmail.com]
*/
public class CarbbankRecord 
{
    /** Logging handle. */
    static final Logger log = Logger.getLogger( CarbbankRecord.class );
    
    /** Entire raw carbbank record */
    private String rawEntry;
    
    /** Hash of carbbank keys => values. */
    private Map<String,String> data;
    
    /** Line number on which this record started */
    final int firstLine;
    
    /** Line number on which this record ends */
    final int lastLine;
    
    /** Carbbank-given id. */
    private int id;
    
    /** The glycan sequence encoded within this Carbbank record. */
    private GlycanSequence glycanSequence;
    
    /** The literature reference given for this Carbbank record. */
    private JournalReference journalReference;

    /** The entry reference created to present the link between 
    *   eurocarb & this carbbank record  */
    private Reference entryReference;
    
    /** List of contexts for this carbbank record */
    private List<BiologicalContext> contexts; 
    
    /** 
    *   Creates a new carbbank record from the passed data Map. 
    *   The data map is expected to be keyed by Carbbank 2-letter
    *   descriptor.
    *
    *   @param data         Hash of carbbank 2-letter keys to values
    *   @param from_line    First line of this carbbank record 
    *   @param to_line      Last line of this record
    */
    CarbbankRecord( String entry, Map<String,String> data, int from_line, int to_line )
    {
        assert data != null;
        assert data.size() > 0;
        
        this.data = data;  
        this.rawEntry = entry;
        
        assert from_line > 0;
        assert from_line < to_line;
        this.firstLine = from_line;
        this.lastLine = to_line;
        
        //  determine carbbank id
        assert data.containsKey("CC");
        String idstring = data.get("CC");
        this.id = parseCarbbankId( idstring );  
    }
    
    
    /** 
    *   Gets the Carbbank-supplied id.
    */
    public int getCarbbankId()
    {
        return id;
    }
    
    
    /** 
    *   Returns the (potentially multi-line) Carbbank sequence for 
    *   this Carbbank record.
    */
    public String getCarbbankSequence()
    {
        assert data != null;
        assert data.containsKey("sequence");
        return data.get("sequence");
    }
    
    
    /** 
    *   Returns the list of {@link BiologicalContext biological contexts}
    *   corresponding to the list of biological sources found within 
    *   this carbbank record. This list will be empty if no biological 
    *   contexts were found.
    */
    public List<BiologicalContext> getContexts()
    {
        if ( contexts == null ) __lookup_contexts();
        return contexts;
    }
    
    
    /*/* 
    *   Returns any/all evidence given in this entry. 
    *   
    *   <strong>Not yet implemented; returns null</strong> 
    * /
    public List<Evidence> getEvidence()
    {
        return null;
    }
    */
    
    /**
    *   Returns the sequence contained within this carbbank record
    *   in {@link GlycoctSequenceFormat GlycoCT format}.
    */
    public SugarSequence getGlycoctSequence()
    throws Exception
    {
        String carbbank_seq = this.getCarbbankSequence();
        
        assert carbbank_seq != null;
        assert carbbank_seq.length() > 0;
        
        String seq = CarbbankParser.translateCarbbankSequence( carbbank_seq ); 
        
        //  a null return value means an unparseable sequence
        if ( seq == null )
        {
            log.debug("carbbank sequence was deemed uparseable, returning null");
            return null;
        }
        
    return new SugarSequence( seq, SequenceFormat.Glycoct );
    }
    
    
    /**
    *   Returns a {@link Reference} object representing this Carbbank
    *   entry itself, specifically, its carbbank id.
    */
    public Reference getEntryReference()
    {
        if ( entryReference != null )
            return entryReference;
        
        Reference r = new Reference();
        
        r.setReferenceType( Reference.Type.DatabaseEntry.toString() );
        r.setExternalReferenceId( "" + this.getCarbbankId() );
        r.setExternalReferenceName( "Carbbank" );
        
        //  mjh: temporary addition for DB debugging cause lots of info!
        r.setReferenceComments( join( data, ": ", "<br/>" ) );

    // store the object in the session to avoid conflicts
    if( r.getReferenceId()<=0 )
        getEntityManager().store(r);

        this.entryReference = r;        
        return entryReference;
    }
    
    
    /*  getJournalReference  *//******************************************** 
    *
    *   Returns a Eurocarb {@link JournalReference} object encapsulating
    *   the literature reference given for this Carbbank entry. Returns 
    *   null if any exception is encountered during parse.
    *
    *   Carbbank fields used:<br/>
    *<ul>   
    *   <li>CT - citation, the journal/vol/page reference</li>
    *   <li>TI - title of the paper</li>
    *   <li>AU - author list, semicolon delimited</li>
    *   <li>SB - submitting author</li>
    *</ul>
    *<p>
    *   Citation is of typical citation form -- 
    *   <tt>[journal abbrev] ([year]) [volume]: [from_page]-[to_page]</tt>, eg: 
    *<pre>
    *       Acc Chem Res (1992) 25: 77-83'
    *</pre>
    *   Citation is mandatory.
    *</p>
    *<p>
    *   Author list is a semicolon-delimited list of authors, eg:<br/>
    *<pre>
    *       Albersheim P; Darvill A; Augur C; Cheong JJ; Eberhard S; Hahn MG;
    *       Marfa V; Mohnen D; O'Neill MA; Spiro MD; York WS
    *</pre>
    *</p>
    *<p>
    *   Title & submitting author are taken as given.
    *</p>
    */
    public JournalReference getJournalReference()
    {
        if ( journalReference != null ) 
            return journalReference;
        
        assert data != null;
        assert data.size() > 0;
        
        String citation   = data.get("CT");
        String title      = data.get("TI");
        String authorlist = data.get("AU");
        String submitter  = data.get("SB"); 
        
        assert citation != null;
        String idstring = citation + authorlist;
        
        //  check if this journalReference exists in the cache
        if ( (journalReference = CarbbankParser.referenceCache.get(idstring)) != null )
        {
            log.debug("journalReference is cached");
            return journalReference;
        }
            
        if ( log.isDebugEnabled() )
            log.debug( "parsing citation string '" + citation + "'" );
        
        try
        {
            String[] citation_pieces = citation.split("\\s*\\((?=\\d)");
            assert citation_pieces.length == 2;
            
            String[] pieces = citation_pieces[1].split("\\W+"); 
            
            String journal_name = citation_pieces[0];
            //System.out.println( "pieces are: " + join(", ", pieces ) );
            
            int    journal_year = pieces.length > 0
                                ? Integer.parseInt( pieces[0] )
                                : -1;
                                
            String journal_vol_ = ( pieces.length > 1 && pieces[1].length() > 0 )
                                ? pieces[1]
                                : null;
            
            String journal_p1   = pieces.length > 2
                                ? pieces[2]
                                : null;
            
            String journal_p2   = pieces.length > 3
                                ? pieces[3] 
                                : null;
                     
            if ( log.isDebugEnabled() )
            {
                log.debug( "parsed JournalReference => name: '" 
                         + journal_name 
                         + "'; year: " 
                         + journal_year 
                         + "'; vol: '" 
                         + journal_vol_
                         + "'; page: " 
                         + journal_p1 
                         + "-" 
                         + journal_p2 
                         );
            }
            
            assert title != null;
            assert journal_year > 1900;
            
            int journal_vol = __int( journal_vol_ );
            int page1 = __int( journal_p1 );
            int page2 = __int( journal_p2 );
            
            journalReference = JournalReference.lookupByCitation( 
                                    journal_name, journal_year, journal_vol, page1 ); 
            
            if ( journalReference == null ) 
            {
                journalReference = new JournalReference();
            
                journalReference.setTitle( title );
                journalReference.setAuthors( authorlist );
                journalReference.setFirstPage( page1 );
                journalReference.setLastPage( page2 );
                journalReference.setJournalVolume( journal_vol );
                journalReference.setPublicationYear( journal_year );
                
                Journal journal = null;
                if ( journalCache.containsKey( journal_name ) )
                {
                    journal = journalCache.get( journal_name );   
                }
                else 
                {
                    journal = Journal.createOrLookup( journal_name );
                    
                    if ( journal == null )
                    {
                        journal = new Journal();
                        journal.setJournalTitle( journal_name );
                    }
            
            // store the object in the session to avoid conflicts
            if( journal.getJournalId()<=0 )
            getEntityManager().store( journal );            
                }
                journalReference.setJournal( journal );
        
        // store the object in the session to avoid conflicts
        if( journalReference.getJournalReferenceId()<=0 )
            getEntityManager().store( journalReference );            

                CarbbankParser.journalCache.put( journal_name, journal );
            }
            
            //  cache it 
            CarbbankParser.referenceCache.put( idstring, journalReference );
            
            return journalReference;  
        }
        catch ( Exception e )
        {
            log.warn( "Caught exception parsing Carbbank journal "
                    + "reference, returning null", e );
            return null;
        }
    }
    
    
    /**
    *   Returns the {@link GlycanSequence glycan} encoded by 
    *   this carbbank entry. NOTE: carbbank structures are not 
    *   necessarily unique, in particular, identical structures 
    *   that are attached to different aglyca are regarded as 
    *   different structures by carbbank. Currently, carbbank
    *   aglyca are discarded by the current {@link CarbbankSequenceFormat 
    *   carbbank sequence format parser}. Accordingly, multiple
    *   carbbank records may return the same {@link GlycanSequence}.
    *
    *   @see CarbbankSequenceFormat
    *   @see CarbbankParser#sequenceCache
    */
    public GlycanSequence getGlycanSequence() throws Exception
    {
        if ( glycanSequence != null )
            return glycanSequence;
        
        SugarSequence sseq = this.getGlycoctSequence();
        if ( sseq == null )
        {
            if ( log.isDebugEnabled() )
                log.debug( "couldn't obtain a glycoct sequence for carbbank id " 
                         + this.getCarbbankId() );
            return null;
        }
        
        //  look in cache first
        if ( (glycanSequence = CarbbankParser.sequenceCache.get(sseq.toString())) != null )
        {
            //  if it exists we concatenate this carbbank id to the 
            //  existing id(s)
            log.debug("returning cached glycan sequence");
            return glycanSequence;
        }
        else
        {
            glycanSequence = GlycanSequence.lookupByExactSequence( sseq );
            if ( glycanSequence == null )
            {
                //  otherwise create a new sequence
                log.debug( "carbbank sequence does not exist in the "
                         + "data store, creating new sequence" );
                glycanSequence = new GlycanSequence(sseq);
        }

        // store the object in the session to avoid conflicts
        if( glycanSequence.getGlycanSequenceId()<=0 )
        getEntityManager().store(glycanSequence);
            
            //  cache it before returning
            CarbbankParser.sequenceCache.put( sseq.toString(), glycanSequence );
            
            return glycanSequence;
        }
    }
    
    
    /** 
    *   Returns all raw data for the current record keyed by 
    *   Carbbank 2-letter field. Map is returned by reference; 
    *   therefore changes made to the Map will be kept (!). 
    */
    public Map getRawData() {  return data;  }
    
    
    /** Returns the entire raw Carbbank entry as String. */
    public String getRawEntry() {  return rawEntry;  }
    
    /**
    *   Returns the raw value of the given field, <em>sans</em> parsing.
    *   Returns null if field doesn't exist.
    */
    public String getRawField( String field ) {  return data.get( field );  }
    
    
    public String toString()
    {
        return "carbbank record " 
             + id 
             + ": lines " 
             + firstLine 
             + "-" 
             + lastLine
             ;   
    }
    
    //~~~~~ PRIVATE METHODS ~~~~~
    
    /** 
    *   Extracts biological source information from this carbbank
    *   record and looks up biological context information for
    *   any/all biological sources.
    */
    private final void __lookup_contexts()
    {
        assert contexts == null;
        if ( data.containsKey("BS") )
        {
            String sourcelist = data.get("BS");
            this.contexts = parseBiologicalSource( sourcelist );            
        }
        else
        {
            this.contexts = Collections.emptyList();   
        }
    }

    
    /** Converts a string to an int, stripping letters as required. */
    private static final int __int( final String s )
    {
        if ( s == null || s.length() == 0 ) 
            return -1;
        
        try {  return new Integer( s ).intValue();  }
        catch ( NumberFormatException if_string_has_letters )
        {
            String number_only = s.replaceAll("\\D", "");
            return __int( number_only );
        }
    }

    
} // end class

