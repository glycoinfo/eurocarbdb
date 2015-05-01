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
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

import org.eurocarbdb.resourcesdb.Config;
import org.eurocarbdb.resourcesdb.io.MonosaccharideConverter;
import org.eurocarbdb.resourcesdb.GlycanNamescheme;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarExporterGlycoCT;
import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarExporterGlycoCTCondensed;
import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarImporterGlycoCT;
import org.eurocarbdb.MolecularFramework.io.namespace.GlycoVisitorToGlycoCT;
import org.eurocarbdb.MolecularFramework.io.namespace.GlycoVisitorToGlycoCTextendMSDB;
import org.eurocarbdb.MolecularFramework.io.carbbank.SugarImporterCarbbank;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.eurocarbdb.MolecularFramework.util.validation.GlycoVisitorSugarGraph;
import org.eurocarbdb.MolecularFramework.util.validation.SugarGraphInformation;

import org.eurocarbdb.util.ProgressWatchable;

import org.eurocarbdb.dataaccess.core.Contributor;
import org.eurocarbdb.dataaccess.core.Taxonomy;
import org.eurocarbdb.dataaccess.core.TissueTaxonomy;
import org.eurocarbdb.dataaccess.core.BiologicalContext;
import org.eurocarbdb.dataaccess.core.DiseaseContext;
import org.eurocarbdb.dataaccess.core.Disease;
import org.eurocarbdb.dataaccess.core.Journal;
import org.eurocarbdb.dataaccess.core.JournalReference;
import org.eurocarbdb.dataaccess.core.GlycanSequence;

import static org.eurocarbdb.util.StringUtils.join;


/**
*<p>
*   Parses {@link http://biol.lancs.ac.uk/gig/pages/gag/carbbank.htm 
*   Carbbank} records from an {@link InputStream}. Individual records
*   are accessed as {@link CarbbankRecord}s.
*</p>
*<p>
*   To use:
*<pre>
        CarbbankParser cb = new CarbbankParser( open_stream );
        while ( CarbbankRecord record = cb.parse() )
        {
            //  a null record means end-of-file
            if ( record == null ) break;
            ...   
        }
*</pre>
*</p>
*   
*   @see CarbbankRecord
*   @author mjh [glycoslave@gmail.com]
*/
public class CarbbankParser implements ProgressWatchable
{
    //~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /** Logging handle. */
    static final Logger log = Logger.getLogger( CarbbankParser.class.getName() );
     
    /** Verbosity limitation of debugging information. Setting to 0
    *   allows a basic amount of debugging info through the logging 
    *   system; values higher than 0 provide more and more info. */
    private static final int DEBUG_LEVEL = 2;
    
    /** String used to delimit multiple values for Carbbank fields. */
    private static final String _DELIM_ = ";;";
    
    /** Cache of glycoct condensed sequence to Eurocarb glycan sequence object */
    static Map<String,GlycanSequence> 
        sequenceCache = new HashMap<String,GlycanSequence>();
    
    /** Cache of carbbank taxonomy string to Eurocarb taxonomy object */
    static Map<String,Taxonomy> 
        taxonomyCache = new HashMap<String,Taxonomy>();
    
    /** Cache of carbbank tissue string to Eurocarb tissue_taxonomy */
    static Map<String,TissueTaxonomy> 
        tissueCache = new HashMap<String,TissueTaxonomy>();

    /** Cache of carbbank disease string to Eurocarb disease */
    static Map<String,Disease> 
        diseaseCache = new HashMap<String,Disease>();
        
    /** Cache of carbbank reference string to Eurocarb reference */
    static Map<String,JournalReference> 
        referenceCache = new HashMap<String,JournalReference>();

    /** Cache of carbbank journal name to Eurocarb journal */
    static Map<String,Journal> 
        journalCache = new HashMap<String,Journal>();
        
        
    /** Number of times we hit the cache */    
    static int taxonomyCacheHits = 0;
    /** Number of times we hit the database (slow) */    
    static int taxonomyDatabaseHits = 0;
    /** Total number of taxonomy lookups. Should be == to cache hits + DB hits. */    
    static int taxonomyTotalLookups = 0;
    
    /** Number of times we hit the cache */    
    static int tissueCacheHits = 0;
    /** Number of times we hit the database (slow) */    
    static int tissueDatabaseHits = 0;
    /** Total number of tissue lookups. Should be == to cache hits + DB hits. */    
    static int tissueTotalLookups = 0;

    /** Number of times we hit the cache */    
    static int diseaseCacheHits = 0;
    /** Number of times we hit the database (slow) */    
    static int diseaseDatabaseHits = 0;
    /** Total number of disease lookups. Should be == to cache hits + DB hits. */    
    static int diseaseTotalLookups = 0;
    
    
    static  
    {         
        /*
        //  report stats on shutdown
        Runtime.getRuntime().addShutdownHook(
            new Thread() 
            {
                public void run() 
                {
                    System.out.println();
                    System.out.println("=== Summary ===");
                    
                    if ( recordsParsed > 0 )
                    {
                        System.out.println("total sequences parsed = " + recordsParsed );
                        System.out.println("unparseable sequences  = " + sequencesUnparseable );
                    }
                    
                    if ( taxonomyTotalLookups > 0 )
                    {
                        System.out.println("taxonomyCacheHits=" + taxonomyCacheHits);
                        System.out.println("taxonomyDatabaseHits=" + taxonomyDatabaseHits);
                        System.out.println("taxonomyTotalLookups=" + taxonomyTotalLookups);
                    }
                }
            }
        );
        */
    }

    
    //~~~~~~~~~~~~~~~~~~~~~ OBJECT FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /** Input stream from which to parse records */
    private BufferedReader in;
    
    /** If {@link #in} is a FileInputStream, then this is the size 
    *   of the file, in bytes.  */
    private long inputStreamSize = 0; 
    
    /** The number of bytes that have been read from {@link #in}. */
    long bytesRead = 0;
    
    /** When parsing started; ie: the first time the {@link #parse} 
    *   method was called. */
    long parsingStartTime = 0;
    
    /** Number of lines parsed, cumulative. */
    int lineCount = 0; 
    
    /** Number of records parsed, cumulative. */
    int recordsParsed = 0;
    
    int sequencesUnparseable = 0;
    
    //~~~~~~~~~~~~~~~~~~~~~ OBJECT METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~
  
    /**
    *   Convenience method that simply returns all carbbank records 
    *   that are parseable from the given {@link InputStream}.
    *
    *   Using this method consumes a significant amount of system memory,
    *   be warned.
    *
    *   mjh: commenting out cause memory usage is over the top.
    * /
    public static parseAll( InputStream instream, List<CarbbankRecord> records )
    {
        if ( records == null ) 
            throw new IllegalArgumentException(
                "expected a list for argument 'records', but got null");
            
        CarbbankParser parser = new CarbbankParser();
        parser.setInputStream( instream );
        int count_added = 0;
        
        while ( true )
        {
            CarbbankRecord r = parser.parse();
            if ( r == null ) 
                break;    
            
            records.add( r );
            count_added++;
        }
        
        log.info("Parsed " + count_added + " carbbank record(s)");
        return;
    }
    */
        

    /** Sets the input stream from which carbbank entries are read. */
    public void setInputStream( InputStream instream )
    {
        assert instream != null;
        this.in = new BufferedReader( new InputStreamReader( instream )); 
        
        //  if it's a File, we can read its size, which can be used to
        //  provide progress info.
        if ( instream instanceof FileInputStream )
            try {  inputStreamSize = ((FileInputStream) instream).getChannel().size();  }
            catch ( java.io.IOException e ) {  log.warn("While reading file size", e );  }
        
        //  reset progress counters
        this.bytesRead = 0;
        this.parsingStartTime = 0;
    }
    
    
    /**
    *   Parses & returns 1 carbbank entry. If a complete carbbank
    *   entry cannot be parsed from the stream set by {@link #setInputStream} 
    *   (eg: end of file/stream has been reached), this method returns null.
    */
    public CarbbankRecord parse() throws IOException
    {
        if ( parsingStartTime == 0 )
            parsingStartTime = System.currentTimeMillis();
        
        Map<String,String> map = new HashMap<String,String>();
        StringBuffer sequence = new StringBuffer();
        StringBuffer entry = new StringBuffer();
        
        int first_line = lineCount + 1;
        String last_key = null;
        
        while ( true )
        {
            String line = in.readLine();
            if ( line == null ) 
                break;
            
            bytesRead += line.length();
            entry.append( line );
            entry.append('\n');
            lineCount++;
            
            if ( line.length() <= 2 ) 
                continue;
            
            if ( line.startsWith("---") ) 
                // ignore it - it signals the start of a structure sequence
                continue;
            
            if ( line.startsWith(";") ) 
                // ignore it - comment line
                continue;
                
            if ( line.startsWith("===") ) 
            {
                //  signals the end of a record   
                assert sequence.length() > 0;
                map.put("sequence", sequence.toString() );
                break;
            }
            
            if ( line.startsWith(" ") )
            {
                //  signals that this is part of a (potentially multi-line)
                //  carbbank sequence                
                sequence.append( line );
                sequence.append('\n');
                continue;
            }
            
            if ( line.matches("^[A-Z][A-Z]: .*") )
            {
                //  ie: it's a 'key: value' line, the format of which 
                //  is '[A-Z][A-Z]: <data>'. data may be spread over
                //  multiple lines.
                assert line.length() > 3 
                    : "error at line " + lineCount + ". line was:\n" + line;
                
                String key = line.substring( 0, 2 );
                String val = line.substring( 3 );
                
                if ( map.containsKey(key) )
                    map.put( key, map.get(key) + _DELIM_ + val.trim() );
                else 
                    map.put( key, val.trim() );
                
                last_key = key;
                continue;
            }
            
            else
            {
                //  otherwise it can only be a continuation of a previous
                //  'key: value' line, and so append it to the last key
                //  we observed.
                assert map.containsKey( last_key )
                    : "error at line " + lineCount + ". line was:\n" + line;
                map.put( last_key, map.get(last_key) + line );
                continue;
            }
        }
           
        //  returning null tells the client of this parser that 
        //  there are no more sequences to parse. 
        if ( map.size() == 0 ) 
            return null;
        
        this.recordsParsed++;
        
        if ( log.isDebugEnabled() )
            log.debug( "parsed carbbank entry " 
                     + recordsParsed 
                     + ", lines "
                     + first_line
                     + "-"
                     + lineCount
                     );
        
        return new CarbbankRecord( entry.toString(), map, first_line, lineCount );
    }
    
    public static Contributor carbBankContributor=CarbbankManager.getCarbbankContributor();
        
    /** 
    *   Parses a biological source ('BS' field in carbbank) and extracts
    *   and looks up biological context information for each/all sources
    *   found. Returns an empty list if no sources could be found.
    */
    public static List<BiologicalContext> parseBiologicalSource( String bs_field )
    {
        String _bs_field = bs_field.trim();
        if ( _bs_field.length() == 0 ) 
            return Collections.emptyList();
        
        String[] sources = _bs_field.split( _DELIM_ );
        List<BiologicalContext> bc_list 
            = new ArrayList<BiologicalContext>( sources.length );
        
        for ( String source : sources )
        {
            log.debug("creating new biological context...");

            BiologicalContext bc = new BiologicalContext();
            bc.addContributor(carbBankContributor , "This biological context was parsed from "
                    + "Carbbank biological source string '" 
                    + source
                    + "'");
             
            if ( log.isDebugEnabled() )
                log.debug("looking up source text '" + source + "'");
            
            Map<String,String> map = new HashMap<String,String>();
            
            __convert_source_to_map( source, map );
            
            /*~~~  Taxonomy - 'domain'/'CN'/'GS' fields ~~~*/
            
            //  identify a taxonomic term with which to search             
            String taxonomy_name = null; 
            if ( map.containsKey("GS") )
                taxonomy_name = map.get("GS");
            else if ( map.containsKey("CN") )
                taxonomy_name = map.get("CN");
            else if ( map.containsKey("domain") ) 
                taxonomy_name = map.get("domain");
            
            //  ...then look it up
            __lookup_taxonomy( taxonomy_name, bc );
            
            
            /*~~~  Tissue Taxonomy - 'OT' field ~~~*/
            String tissue_name = map.get("OT");
            __lookup_tissue( tissue_name, bc );
                
            
            /*~~~  Disease(s) - 'disease' field ~~~*/
            String disease_name = map.get("disease");
            __lookup_disease( disease_name, bc );
            
            
            bc_list.add( bc );
        }
        
        if ( bc_list.size() == 0 ) 
        {
            log.debug("NO biological contexts given in record");
            bc_list = Collections.emptyList();
        }
            
        return bc_list;
    }
   
    
    /** 
    *   Parses a carbbank id from a carbbank 'CC' field. 
    *   Carbbank IDs are usually of form 'CCSD:NNNN' where N is a 
    *   a positive integer. This method returns only the numeric 
    *   portion. Returns -1 if the passed id string does not conform
    *   with the general Carbbank syntax.
    */
    public static int parseCarbbankId( String cc_field )
    {
        assert cc_field != null;
        assert cc_field.startsWith("CCSD:");
        String idstring = cc_field.substring( 5 );
        
        try {  return Integer.parseInt( idstring );  }
        catch ( NumberFormatException string_has_non_numerics_in_it )
        {
            try {  return Integer.parseInt( idstring.replaceAll("\\D", "") );  }
            catch ( NumberFormatException id_is_invalid )
            {
                return -1;   
            }
        }
    }
    

    public static Sugar removeAglyca(Sugar s) throws Exception
    {
        GlycoVisitorSugarGraph sugargraph_visitor = new GlycoVisitorSugarGraph();    
        sugargraph_visitor.start(s);

        List<SugarGraphInformation> sgi_list = sugargraph_visitor.getSugarGraphs();
        
        if ( sgi_list == null || sgi_list.size() == 0 )
            throw new Exception("SugarGraphInformation null or zero size");
        
        assert sgi_list.size() == 1;
        
        return sgi_list.get(0).getSugar();
    }
    

    /**
    *   Convenience method to translate a carbbank sequence into a GlycoCT
    *   sequence string. Syntactically invalid carbbank sequences or sequences
    *   with unparseable elements return null.
    */
    public static String translateCarbbankSequence( String carbbank_sequence )
    throws Exception
    {
        try
        {
            long start = System.currentTimeMillis();
            
            SugarImporterCarbbank carbbank_importer = new SugarImporterCarbbank();
            
            // GlycoVisitorToGlycoCT glycoct_visitor
                // = new GlycoVisitorToGlycoCT(
                        // new MonosaccharideConverter(
                                // new Config() ) );
                                
            GlycoVisitorToGlycoCTextendMSDB glycoct_visitor 
                = new GlycoVisitorToGlycoCTextendMSDB(
                    new MonosaccharideConverter( new Config() ));
                
            glycoct_visitor.setNameScheme(GlycanNamescheme.CARBBANK);    
            glycoct_visitor.setUseStrict( false );
            glycoct_visitor.setUseFusion(true);
                
            Sugar sugar = carbbank_importer.parse( carbbank_sequence );
            glycoct_visitor.start( sugar );
            sugar = glycoct_visitor.getNormalizedSugar();
            
            Sugar no_aglyca = removeAglyca(sugar);
            if( no_aglyca != null )
                sugar = no_aglyca;
        
            //  for glycoct-condensed
            SugarExporterGlycoCTCondensed glycoct_exporter = new SugarExporterGlycoCTCondensed();

            //  for glycoct-XML
            //SugarExporterGlycoCT glycoct_exporter = new SugarExporterGlycoCT();
            
            glycoct_exporter.start( sugar );
            
            //  for glycoct-condensed
            String glycoct_sequence = glycoct_exporter.getHashCode();
            
            //  for glycoct-XML
            //String glycoct_sequence = glycoct_exporter.getXMLCode();

            if ( log.isDebugEnabled() )
            {
                long elapsed = System.currentTimeMillis() - start;
                log.debug( "translation of carbbank sequence to glycoct took " 
                         + elapsed 
                         + "msec"
                         );
            }
            
            return glycoct_sequence;
        }
        catch ( Exception stupid_mf_exception )
        {
            log.warn( stupid_mf_exception );  
            throw stupid_mf_exception;
        }
    }
    
    
    /** 
    *   If parsing has started, and the InputStream being read from 
    *   was a file, then this returns the percent of the file that 
    *   has been read so far, otherwise returns zero. 
    *   @see #setInputStream
    */
    public int getPercentComplete()
    {
        if ( inputStreamSize == 0 )
            return 0;
        
        return (int) ((bytesRead / inputStreamSize) + 0.5);
    }

    
    /**
    *   The number of millisecs that have elapsed since the {@link #parse}
    *   method was first called on the {@link InputStream} passed to 
    *   {@link #setInputStream}.
    */
    public int getMillisecsElapsed()
    {
        if ( parsingStartTime == 0 ) 
            return 0;
        
        long now = System.currentTimeMillis();
        assert now >= parsingStartTime;
        
        return (int) (now - parsingStartTime);
    }
    
    
    //~~~~~~~~~~~~~~~~~~~~~ PRIVATE METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /**
    *   (Pre-)Loads a bunch of taxonomies for which we already have
    *   human-verified mappings of Carbbank term to NCBI id.
    */
    private static final void __preload_taxonomy_cache()
    {
        log.info("Preloading taxonomy cache...");
        assert taxonomyCache.size() == 0;
        for ( CarbbankTaxonomy ct : CarbbankTaxonomy.values() )
        {
            if ( ct.ncbiId < 0 ) 
                continue;
            
            Taxonomy tax = Taxonomy.lookupNcbiId( ct.ncbiId );
            if ( tax == null )
                tax = Taxonomy.UnknownTaxonomy();
            
            taxonomyCache.put( ct.carbbankName, tax );
        }
        log.info("Preloaded " + taxonomyCache.size() + " taxonomies");
    }
    
    
    /** 
    *   Parses a Carbbank biological source ('BS' field) string  
    *   into a Map of 2-letter field to value. Carbbank biological
    *   source strings are of form:<br/>
    *<pre>
    *   (GS) Phytophthora megasperma, (OT) cell wall, (*) f.sp. glycinea
    *</pre>
    *   The Map returned by this method for this string would be:
    *<pre>
    *   GS => Phytophthora megasperma
    *   OT => cell wall
    *   \* => f.sp. glycinea
    *</pre>
    */
    private static final void 
    __convert_source_to_map( String source, Map<String,String> map )
    {
        assert map != null;
        assert map.size() == 0;

        //  split on ', (XX)', where XX is a 2-letter uppercase descriptor 
        String[] bits = source.split( ",?\\s*[\\(\\)]\\s*" );
        
        //  the number of bits after splitting should be ODD,
        //  because the source text should start with a '(',
        //  which means the first element after splitting should be an empty string
        assert source.startsWith("(");
        if ( (bits.length & 1) == 0 )
            log.warn( "Uneven number of fields to values in string '"
                    + source 
                    + "'\nbits parsed were: "
                    + join(", ", bits ) 
                    ); 
        
        //  load the pieces into a hash, skipping empty strings
        for ( int i = 0; i < bits.length - 1; i++ )
        {
            if ( bits[i].length() == 0 ) continue;
            map.put( bits[i], bits[i+1] );
        }        
    }
    
    
    /** 
    *   Look up the given taxonomy name in the database, and stick it in
    *   the given BiologicalContext. 
    */
    private static final void 
    __lookup_taxonomy( String taxonomy_name, BiologicalContext bc )
    {
        if ( taxonomyCache.size() == 0 )
            __preload_taxonomy_cache();
        
        if ( taxonomy_name == null )
        {
            log.debug("no parseable taxonomy term, setting to Unknown taxonomy");
            bc.setTaxonomy( Taxonomy.UnknownTaxonomy() );   
            bc.getBiologicalContextContributor(carbBankContributor.getContributorId()).appendComment("(No parseable taxonomy term)");
            return;   
        }
            
        //  now full-text search for said taxonomic term
        //  - try looking in our static cache first to save a DB lookup
        Taxonomy tax = taxonomyCache.get( taxonomy_name );
        if ( tax != null )
        {
            if ( log.isDebugEnabled() )
                log.debug("taxonomy '" + taxonomy_name + "' is cached");
            
            //  it's in the cache, w00t!
            bc.setTaxonomy( tax );   
            
            taxonomyCacheHits++;
        }
        else
        {
            //  otherwise look it up...
            assert taxonomy_name.length() > 0;
            
            List<Taxonomy> results = null;
            
            try
            {
                results = Taxonomy.lookupExactNameOrSynonym( taxonomy_name );
                
                if ( results == null || results.size() == 0 )
                    results = Taxonomy.lookupNameOrSynonym( taxonomy_name );
            }
            catch ( Exception e )
            {
                log.warn( "Caught exception while hitting the DB", e );
                results = null;
            }
                
            if ( results != null && results.size() > 0 )
            {
                //  1 or more taxonomy matches.
                //
                //  !!! NOTE !!! 
                //  for now, we will simply take the first match,
                //  and discard the others, which upon future revision
                //  might need to be reviewed 
                //  !!! NOTE !!!
                
                if ( log.isDebugEnabled() )
                {
                    log.debug( "found " 
                             + results.size() 
                             + " result(s) for taxonomy '"
                             + taxonomy_name 
                             + "': "
                             + join(", ", results )
                             );
                }
                
                tax = results.get(0);
                bc.setTaxonomy( tax );
                taxonomyCache.put( taxonomy_name, tax );
            }
            else
            {
                //  taxonomy not found 
                bc.setTaxonomy( Taxonomy.UnknownTaxonomy() );   
                bc.getBiologicalContextContributor(carbBankContributor.getContributorId()).appendComment("Taxonomy term '" 
                                + taxonomy_name
                                + "' was not found"
                                );
            }

            taxonomyDatabaseHits++;
        }
        
        taxonomyTotalLookups++;
        return;
    }
    

    /** 
    *   Look up the given tissue name in the database, and stick it in
    *   the given BiologicalContext. 
    */
    private static final void 
    __lookup_tissue( String tissue_name, BiologicalContext bc )
    {
        if ( tissue_name == null || tissue_name.length() == 0 )
        {
            log.debug("no parseable tissue term, setting to Unknown tissue");
            bc.setTissueTaxonomy( TissueTaxonomy.UnknownTissue() );   
            bc.getBiologicalContextContributor(carbBankContributor.getContributorId()).appendComment( "(No parseable tissue term)" );
            return;   
        }
            
        //  now full-text search for said tissue
        //  - try looking in our static cache first to save a DB lookup
        TissueTaxonomy tissue = tissueCache.get( tissue_name );
        if ( tissue != null )
        {
            if ( log.isDebugEnabled() )
                log.debug("tissue '" + tissue_name + "' is cached");
            
            //  it's in the cache, w00t!
            bc.setTissueTaxonomy( tissue );   
            
            tissueCacheHits++;
        }
        else
        {
            //  otherwise look it up...
            List<TissueTaxonomy> results = null;
            
            try
            {
                results = TissueTaxonomy.lookupNameOrSynonym( tissue_name );
            }
            catch ( Exception e )
            {
                log.warn( "Caught exception while hitting the DB", e );
                results = null;
            }
                
            if ( results != null && results.size() > 0 )
            {
                //  1 or more tissue matches.
                //
                //  !!! NOTE !!! 
                //  for now, we will simply take the first match,
                //  and discard the others, which upon future revision
                //  might need to be reviewed 
                //  !!! NOTE !!!
                
                if ( log.isDebugEnabled() )
                {
                    log.debug( "found " 
                             + results.size() 
                             + " result(s) for tissue '"
                             + tissue_name 
                             + "': "
                             + join(", ", results )
                             );
                }
                
                tissue = results.get(0);
                bc.setTissueTaxonomy( tissue );
                tissueCache.put( tissue_name, tissue );
            }
            else
            {
                //  tissue not found 
                bc.setTissueTaxonomy( TissueTaxonomy.UnknownTissue() );   
                bc.getBiologicalContextContributor(carbBankContributor.getContributorId()).appendComment( "Tissue term '" 
                                + tissue_name
                                + "' was not found"
                                );
            }
            
            tissueDatabaseHits++;
        }
        
        tissueTotalLookups++;
        
        return;
    }

    
    /** 
    *   Look up the given disease name in the database, and stick it in
    *   the given BiologicalContext. 
    */
    private static final void 
    __lookup_disease( String disease_name, BiologicalContext bc )
    {
        if ( disease_name == null || disease_name.length() == 0 )
        {
            log.debug("no parseable disease term, setting to NO disease associations");
            bc.getBiologicalContextContributor(carbBankContributor.getContributorId()).appendComment( "(No disease terms found)" );
            return;   
        }
            
        //  Carbbank often uses 'cancer' as a term but 
        //  MeSH lists 'cancer' as 'neoplasm', so keeps assigning
        //  'cancer' as 'precancerous condition', which is wrong.
        //  this is a hack so it uses 'neoplasm' instead.
        if ( "cancer".equals( disease_name ) )
            disease_name = "neoplasm";
        
        //  now full-text search for said disease
        //  - try looking in our static cache first to save a DB lookup
        Disease disease = diseaseCache.get( disease_name );
        if ( disease != null )
        {
            if ( log.isDebugEnabled() )
                log.debug("disease '" + disease_name + "' is cached");
            
            //  it's in the cache, w00t!
            bc.addDiseaseAssociation( disease );   
            
            diseaseCacheHits++;
        }
        else
        {
            //  otherwise look it up...
            List<Disease> results = null;
            
            try
            {
                results = Disease.lookupNameOrSynonym( disease_name );
            }
            catch ( Exception e )
            {
                log.warn( "Caught exception while hitting the DB", e );
                results = null;
            }
                
            if ( results != null && results.size() > 0 )
            {
                //  1 or more disease matches.
                //
                //  !!! NOTE !!! 
                //  for now, we will simply take the first match,
                //  and discard the others, which upon future revision
                //  might need to be reviewed 
                //  !!! NOTE !!!
                
                if ( log.isDebugEnabled() )
                {
                    log.debug( "found " 
                             + results.size() 
                             + " result(s) for disease '"
                             + disease_name 
                             + "': "
                             + join(", ", results )
                             );
                }
                
                disease = results.get(0);
                bc.getDiseaseContexts().add( new DiseaseContext( bc, disease ) );
                diseaseCache.put( disease_name, disease );
            }
            else
            {
                //  disease not found 
            	bc.getBiologicalContextContributor(carbBankContributor.getContributorId()).appendComment( "Disease term '" 
                                + disease_name
                                + "' was not found"
                                );
            }
            
            diseaseDatabaseHits++;
        }
        
        diseaseTotalLookups++;
        
        return;
    }

    
    /** Simple command-line driver for testing */
    public static void main( String[] args )
    throws java.io.FileNotFoundException, IOException
    {
        if ( args.length == 0 ) 
        {
            System.err.println("no argument!");
            return;
        }
        
        CarbbankParser parser = new CarbbankParser();
        int c = 0;
        int good = 0, bad = 0;
        
        for ( String filename : args )
        {
            FileInputStream instream = new FileInputStream( filename );
            parser.setInputStream( instream );
            
            while ( true )
            {
                CarbbankRecord r = parser.parse();
                if ( r == null ) 
                    break;    
                
                //r.getContexts();
                //r.getJournalReference();
                try
                {
                    System.err.println( "carbbank:" );
                    System.err.println( r.getCarbbankSequence() );
                    System.err.println( "glycoct:" );
                    System.err.println( r.getGlycoctSequence() );
                }
                catch ( Exception e )
                {
                    System.err.println( e );
                    continue;                                        
                }
                
                c++;
            }
        }
        
        System.err.println("parsed " + c + " records");
        
    }
    
    
} // end class
