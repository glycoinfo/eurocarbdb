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
*   Last commit: $Rev: 1870 $ by $Author: david@nixbioinf.org $ on $Date:: 2010-02-23 #$  
*/

package org.eurocarbdb.util.carbbank;

//  stdlib imports
import java.util.*;
import java.io.*;

//  3rd party imports
import org.apache.log4j.Logger;

import org.hibernate.Session;
import org.hibernate.EntityMode;

import org.dom4j.Element;
import org.dom4j.io.XMLWriter;
import org.dom4j.io.OutputFormat;

//  eurocarb imports
import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.dataaccess.HibernateEntityManager;

import org.eurocarbdb.dataaccess.core.Reference;
import org.eurocarbdb.dataaccess.core.JournalReference;
import org.eurocarbdb.dataaccess.core.Contributor;
import org.eurocarbdb.dataaccess.core.GlycanSequence;
import org.eurocarbdb.dataaccess.core.BiologicalContext;
import org.eurocarbdb.dataaccess.core.Disease;
import org.eurocarbdb.dataaccess.exception.*;

import org.eurocarbdb.util.carbbank.CarbbankParser;
import org.eurocarbdb.util.carbbank.CarbbankRecord;

// import org.eurocarbdb.dataaccess.hibernate.HibernateUtil;

//  static imports
import static org.eurocarbdb.util.StringUtils.join;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/*  class CarbbankManager  *//***************************************
*
*   A data loader and unloader for Carbbank structures, including
*   sequence, reference & biological context information.
*
*   @see      org.eurocarbdb.util.carbbank.CarbbankParser
*   @see      org.eurocarbdb.util.carbbank.CarbbankRecord
*   @author   mjh
*   @version  $Rev: 1870 $
*/
public class CarbbankManager 
{

    //~~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    /** Logging handle. */
    static final Logger log = Logger.getLogger( CarbbankManager.class );

    /** Number of parsing/loading exceptions to tolerate until the 
    *   load process is aborted.  */
    private static final int ERROR_TOLERANCE = 100000;
    
    /** The stream from which we read Carbbank records. @see #getInputStream */
    private InputStream instream = null;
    
    /** The stream to which we output Carbbank CSV once parsed. @see #getOutputStreamErrorSequences */
    private PrintStream outstreamErrorSequences = null;
    
    /** This is the contributor that will be used when loading 
    *   (or unloading) Carbbank structures to the data store. 
    *   @see #getCarbbankContributor  */
    private static Contributor carbbankContributor = null;
   
    /** Carbbank parser instance. */
    private CarbbankParser parser = new CarbbankParser();
    
    /** Max number of entries to parse. Negative means parse all. */
    private int loadLimit = -1;
    
    /** Specifies the first record that will be fully parsed. For example, 
    *   firstRecord=10 means the first record loaded will be record 10. */
    private int firstRecord = 1;
    
    private static final String QUERY_GET_ALL_CARBBANK_STRUCTURES = 
        "org.eurocarbdb.dataaccess.core.Contributor.GET_ALL_CARBBANK_STRUCTURES";
        
    /** Number of records that parsed with errors. */
    private int records_with_errors = 0; 
    
    /** Number of records to save before committing a transaction. */
    private int save_after = 25;
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~//    

    /**
    *   Returns a {@link CarbbankParser} that may be used for parsing
    *   a raw Carbbank file.
    */
    public CarbbankParser getCarbbankParser() 
    {  
        assert parser != null;
        return parser;  
    }

    
    /** 
    *   Returns an {@link InputStream} to a Carbbank raw data file. The data 
    *   file used is determined at runtime by the value of the Eurocarb
    *   property 'carbbank.raw.file'.
    *   @see Eurocarb#getProperty
    */
    public InputStream getInputStream()
    {
        if ( instream == null )
        {
            String filename = Eurocarb.getProperty("carbbank.raw.file");
            log.info("opening local Carbbank file '" + filename + "'");
            try {  instream = new FileInputStream( filename );  }
            catch ( FileNotFoundException e )
            {
                log.warn("Couldn't open file '" + filename + "': " + e );   
                return null;
            }
        }
        
        return instream;
    }
    
    
    public void setFirstRecord( int index ) 
    {
        if ( index < 0 ) index = 0;
        firstRecord = index;    
    }
    
    
    /** 
    *   Sets the passed {@link InputStream} from which Carbbank 
    *   raw data will be read. 
    *   @see #parseAndLoadCarbbank
    */
    public void setInputStream( InputStream in ) 
    {
        assert in != null;
        instream = in;
    }
    
    
    /**
    *   Returns the {@link PrintStream} that will be used to output Carbbank
    *   records that produce errors. If not set explicitly by 
    *   {@link #setOutputStreamErrorSequences} then the stream returned 
    *   will be directed to a file named by the Eurocarb property 
    *   <tt>'carbbank.errors.file'</tt>.
    *   @see Eurocarb#getProperty
    *   @throws DataAccessException if method cannot open file for writing
    */
    public PrintStream getOutputStreamErrorSequences()
    throws DataAccessException
    {
        if ( outstreamErrorSequences == null )
        {
            String filename = Eurocarb.getProperty("carbbank.errors.file");
            
            if ( log.isDebugEnabled() )
                log.debug("creating cache file '" + filename + "'");
            
            try
            {
                outstreamErrorSequences = new PrintStream( 
                                            new BufferedOutputStream( 
                                                new FileOutputStream( filename )));
            }
            catch ( Exception e )
            {
                outstreamErrorSequences = null;
                String msg = "Caught exception while trying to open file '"
                           + filename
                           + "' for writing: "
                           + e ;
                           
                log.warn( msg );
                
                throw new DataAccessException( msg );
            }
        }
        
        return outstreamErrorSequences;
    }
    
    
    /** 
    *   Sets the passed {@link OutputStream} to which pre-parsed & cached 
    *   Carbbank data will be read (by the method {@link #parseAndLoadCarbbank}). 
    */
    public void setOutputStreamErrorSequences( PrintStream out ) 
    {
        assert out != null;
        outstreamErrorSequences = out;
    }
    
    
    /**
    *   Returns the canonical "Carbbank" contributor.
    *   If a "Carbbank" contributor does not exist in the 
    *   current data store at the time this method is called, 
    *   then it will be created. The name of this contributor
    *   is given by the Eurocarb property 'carbbank.contributor.name'.
    */
    public static Contributor getCarbbankContributor()
    {
        // if ( carbbankContributor != null )
            // return carbbankContributor;
        
        String contributor_name = Eurocarb.getProperty("carbbank.contributor.name");
        if ( contributor_name == null )
        {
            log.warn( "There is no value for property '"
                    + "carbbank.contributor.name"
                    + "' configured! Using last-resort value of 'Carbbank'"
                    );
            contributor_name = "Carbbank";
        }
        
        if ( log.isDebugEnabled() )
            log.debug( "Looking up the canonical Carbbank contributor "
                     + "(contributor name '" 
                     + contributor_name
                     + "')"
                     );
            
        carbbankContributor = Contributor.lookupExactName( contributor_name );
        
        if ( carbbankContributor == null )
        {
            log.debug( "A Carbbank contributor could not be found "
                     + "in the current data store, creating it");
            
            carbbankContributor = new Contributor();
            carbbankContributor.setContributorName( contributor_name );
            
            getEntityManager().store( carbbankContributor );
         
            if ( log.isDebugEnabled() )
            log.debug( "Carbbank contributor with name '"
                     + carbbankContributor.getContributorName()
                     + "', id '"
                     + carbbankContributor.getContributorId()
                     + "' successfully added to the data store"
                     );
        }
        
        return carbbankContributor;
    }

    
    /** 
    *   Sets a limit on the number of carbbank entries that will be 
    *   parsed and loaded via the {@link #parseAndLoadCarbbank} method.
    *   Less than zero means 'load all'.
    */
    public void setLoadLimit( int nmb_of_entries )
    {
        loadLimit = nmb_of_entries;
    }
    
    /** 
    *   Parses and loads Carbbank data from raw file. Note that
    *   this is much slower than the {@link #loadCarbbank} method,
    *   which loads a pre-parsed version of Carbbank data. 
    *   @return number of carbbank entries parsed
    */
    public int parseAndLoadCarbbank() throws IOException, DataAccessException
    {
        if ( firstRecord < 0 ) 
        {
            log.info("Nothing to do!");
            return 0;
        }
            
        EntityManager em = getEntityManager();
        
        //  all entries parsed will be added to eurocarb db under this contributor.
        Contributor c  = this.getCarbbankContributor();
        
        InputStream in = getInputStream();
        parser.setInputStream( in );
        
        int count = 0;

        assert c != null;

        while ( true )
        {
        CarbbankRecord r = parser.parse();
            if ( r == null ) break;    
            count++;
        
            //  skip records until we reach the first record specified by firstRecord            
            if ( count < firstRecord ) 
            {
                if ( log.isDebugEnabled() )
                log.debug("skipping record " + count + "(<" + firstRecord + ")...");
                continue;
            }
            
            //  stop parsing if we've loaded more than loadLimit records.
            if ( loadLimit == 0 ) 
            {
                log.debug("Load limit reached, stopping...");
                break;
            }
                
            //  check seq has not already been added to DB
            if ( recordAlreadySaved( r ) )
            {
                log.debug("record already exists in DB, skipping...");
                continue;
            }
            
            //  otherwise process records as usual.
            //  skip records with unparseable sequences
            GlycanSequence gs = null;
            try 
            {  
                gs = r.getGlycanSequence(); 
                
                if ( gs == null )
                    throw new RuntimeException("GlycanSequence returned null");
                
                if ( gs.getSequenceCt() == null )
                    throw new RuntimeException("GlycanSequence returned a null Glycoct sequence");
            }
            catch ( Exception ex ) 
            {           
                logErrorRecord( r, ex, "Sequence unparseable" );
                continue;
            }
                
            //  get references for entry
            JournalReference jref = r.getJournalReference();
            if ( jref == null ) 
            {
                logErrorRecord( r, null, "Couldn't get a valid JournalReference" );
                continue;
            }
                
            Reference ref = r.getEntryReference();
            assert ref != null;
                
            //  heavyweight biological context lookup
            List<BiologicalContext> bcs = r.getContexts();
    
            //  everythings ok so far, set associations between
            //  objects and then save them all
            jref.setContributor( c );
            ref.setContributor( c );
            gs.setContributor( c );            

            gs.addReference( ref );
            gs.addReference( jref );
            
            for ( BiologicalContext bc : bcs ) 
            {
            	bc.addContributor(c, "");
                gs.addBiologicalContext( bc );
            }
                
            //  save the whole object graph
            log.debug("attempting to save carbbank record...");
        
            try 
            {
        // update object with new information
        getEntityManager().update(jref);
        getEntityManager().update(ref);
                em.update( gs );
                
                log.debug("record was saved successfully");
        }
            catch ( Exception ex )
            {
        log.warn("record not saved: " + ex.getMessage());
        logErrorRecord( r, ex, "caught exception while trying to save" );
            }
            
            loadLimit--;
            
            if ( (count % save_after) == 0 ) {
        periodicSaveProgress();
        }
               
        } // end while
        
        if ( log.isInfoEnabled() )
        {
            log.info( "Parsed " 
                    + count 
                    + " records, "
                    + records_with_errors
                    + " load error(s)"
                    );
        }
        
        return count;
    }
    
    
    protected void periodicSaveProgress()
    {
        log.info("saving progress...");
        getEntityManager().endUnitOfWork();
        getEntityManager().beginUnitOfWork();
    }
    
    
    /** Returns true if given CarbbankRecord already exists in the DB. */
    protected boolean recordAlreadySaved( CarbbankRecord r )
    {
        assert r != null;
        int id = r.getCarbbankId();
        assert id > 0;
        
        GlycanSequence existing = GlycanSequence.lookupByExternalRef("Carbbank", id );
        return existing != null;
    }
    
    
    /** 
    *   Records that the given Carbbank record had a problem and/or threw 
    *   an error during import. 
    *   @param ex can be null
    */
    protected void logErrorRecord( CarbbankRecord r, Exception ex, String msg )
    {
        assert r != null;
        PrintStream out = this.getOutputStreamErrorSequences();
        
        out.println( ";~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~" );
        out.println( "; carbbank id " + r.getCarbbankId() );
        out.println( "; eurocarb reason for failure: " + msg );
        
        if ( ex != null )
        {
            out.println( "; exception was: " 
                       + ex.getClass().getSimpleName() 
                       + " - " 
                       + ex.getMessage() 
                       );
        }
        
        out.println( r.getRawEntry() );

        out.println();
        out.println();
        
        records_with_errors++;
        
        if ( records_with_errors > ERROR_TOLERANCE )
            throw new DataAccessException(
                "Aborting load, too many errors");
    }
    
        
    /**
    *   Exports a freshly parsed & loaded Carbbank as CSV to the 
    *   {@link OutputStream} given by {@link #getOutputStreamErrorSequences}.
    */
    public int exportCarbbank()
    {
        assert false: "TODO";
        
        //PrintWriter out = getOutputWriter();
        OutputStream out = this.getOutputStreamErrorSequences();
        assert out != null;
        
        Session s = null;
        EntityManager em = Eurocarb.getEntityManager();
        if ( em instanceof HibernateEntityManager )
        {
            s = ((HibernateEntityManager) em).getHibernateSession();   
        }
        else
        {
            throw new RuntimeException( 
                "Only Hibernate-backed EntityManagers "
                + "support bulk exporting Carbbank data"
            );   
        }
            
        assert s != null;
        Session dom4j = s.getSession( EntityMode.DOM4J );
        
        String contrib_name = getCarbbankContributor().getContributorName();
        
        log.debug("query for all carbbank structures...");
/*
        List structures = dom4j.getNamedQuery( QUERY_GET_ALL_CARBBANK_STRUCTURES )
                               .setParameter("name", contrib_name )
                               .list();
        
        if ( log.isDebugEnabled() ) 
            log.debug( "found " 
                     + structures.size() 
                     + " carbbank structures..."
                     );
            
        Element e = (Element) structures.get(0);
*/                           
        Element e = (Element) dom4j.load( Disease.class, 9538 );

        try
        {
            log.debug("generating XML...");
            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter writer = new XMLWriter( out, format );
            writer.write( e );
        }
        catch ( IOException ioex )
        {
            log.warn( "Caught " 
                    + ioex.getClass().getName() 
                    + " while generating export XML"
                    , ioex 
                    );
            throw new RuntimeException( ioex );
        }
        
        return 1;
    }
    
    
    /** 
    *   Loads previously-parsed Carbbank structures and associated 
    *   data into the current data store. This method requires a pre-parsed
    *   version of the raw data, which is created when loading Carbbank
    *   with the {@link parseAndLoadCarbbank} method. If this pre-parsed
    *   data does not exist when this method is called a 
    *   {@link UnsupportedOperationException} is thrown.
    *
    *   @return a string indicating success/failure.
    *   @throws UnsupportedOperationException 
    *           if pre-parsed Carbbank data does not exist at time of calling.
    *   @see    EntityManager
    */
    public int loadCarbank()
    {
        return 0;
    }
    
    
    /**
    *   Unloads (deletes!) Carbbank structures and associated data from 
    *   the current data store.
    *   @return a string indicating success/failure.
    */
    public int unloadCarbbank()
    {
        Contributor c = this.getCarbbankContributor();
        
        //TODO: getEntityManager().delete( c );
        
        this.carbbankContributor = null;
        
        return 0;
    }
    
    
    /**
    *   Saves the passed CarbbankRecord to the current data store.
    */
    protected void storeCarbbankRecord( CarbbankRecord r )
    {
        assert r != null;    
    }
    
    
    public static class CLI
    {
        public static void main( String[] args )
        throws IOException
        {
            getEntityManager().beginUnitOfWork();
            
            CarbbankManager cm = new CarbbankManager();
            
            int parsed = cm.parseAndLoadCarbbank();
            
            getEntityManager().endUnitOfWork();
        }
    }
        
} // end class







