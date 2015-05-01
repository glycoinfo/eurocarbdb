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

//  stdlib imports
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.URLEncoder;

//  stdlib imports
import java.util.*;
import java.io.*;
import java.net.*;

//  3rd party imports
import org.apache.log4j.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//  eurocarb imports
import org.eurocarbdb.dataaccess.Eurocarb;
import org.eurocarbdb.dataaccess.EntityManager;
import org.eurocarbdb.dataaccess.core.Reference;
import org.eurocarbdb.application.glycanbuilder.XMLUtils;

//  static imports
import static org.eurocarbdb.util.JavaUtils.*;
import static org.eurocarbdb.dataaccess.Eurocarb.getEntityManager;


/**
*   Represents a standard journal article or periodical reference.
*   Since JournalReferences are unique in the sense that only one
*   instance is ever used to implement a specific journal/article
*   reference, it is always best to use the {@link #createOrLookup}
*   method to find or create JournalReference instances.
*
*   @author mjh
*/
public class JournalReference extends Reference implements Serializable
{
    /** Default query base URL for constructing Pubmed links */
    public static final String Default_Pubmed_Query_Url 
        = "http://www.ncbi.nlm.nih.gov/sites/entrez"
        + "?EntrezSystem2.PEntrez.Pubmed.SearchBar.Term=";
    
    private static final Logger log = Logger.getLogger( JournalReference.class );

    /** Used to extract publication year from pubmed HTTP query */        
    private static Pattern year_pattern = Pattern.compile(".*([0-9]{4}).*"); 
    private static Pattern pages_pattern = Pattern.compile("^([0-9]+)(?:\\-([0-9]+))?$"); 

    private static final String Q = "org.eurocarbdb.dataaccess.core.JournalReference."; 
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~  FIELDS  ~~~~~~~~~~~~~~~~~~~~~~~~~~//

    private int journalReferenceId;
     
    private Journal journal;
     
    private Integer pubmedId;
     
    private String authors;
     
    private String title;
     
    private Integer publicationYear;
     
    private Integer journalVolume;
     
    private Integer journalStartPage;
     
    private Integer journalEndPage;

    /** Lazily instantiated from String {@link #authors}*/
    private List<Author> authorlist = null;
     
    //~~~~~~~~~~~~~~~~~~~~~~  CONSTRUCTORS  ~~~~~~~~~~~~~~~~~~~~~~~//

    /** default constructor */
    public JournalReference() 
    {
        //  default journal reference provider is pubmed.
        setExternalReferenceName("Pubmed");
        
        //  objects of this class are the Journal type by definition.
        setReferenceType( Reference.Type.Journal.toString() );
    }

    
    //~~~~~~~~~~~~~~~~~~~~~~  STATIC METHODS  ~~~~~~~~~~~~~~~~~~~~~//
    
    /** Returns a count of all {@link JournalReference}s. */
    public static long countJournalReferences()
    {
        Long count = (Long) Eurocarb.getEntityManager()
                            .getQuery( Q + "COUNT_ALL" )
                            .uniqueResult();
                            
        return count.intValue();
    }
    
    
    public Reference storeOrLookup() throws Exception 
    {
        log.debug("storeOrLookup");

        // search for existing references
        JournalReference ret = lookupByPubmedId( this.pubmedId );        
        if( ret!=null ) 
        {
            if( !this.matches(ret) )
        throw new Exception("The database contains an existing reference that matches only partially");
            return ret;
        }
    
        if( this.journal!=null )
        {
            ret = lookupByCitation( this.journal.getJournalTitle(),
                                    this.publicationYear,
                                    this.journalVolume,
                                    this.journalStartPage );
        }
        else
        {
            ret = lookupByCitation( "",
                                    this.publicationYear,
                                    this.journalVolume,
                                    this.journalStartPage );
        }
            
        if( ret!=null ) 
        {
            if( !this.matches(ret) )
        throw new Exception("The database contains an existing reference that matches only partially");
            return ret;
        }
        
        // create a new reference
        if( this.journal!=null )
            this.journal = this.journal.storeOrLookup();
        
        EntityManager em = getEntityManager();
        em.store( this );
    
        return this;
    }


    /**
    *   Returns an existing {@link JournalReference} if the given 
    *   pubmed id argument matches a JournalReference already in 
    *   the data store, otherwise returns a new (unsaved) 
    *   JournalReference object.
    */
    public static JournalReference createOrLookup( Integer pubmed_id )
    {
        JournalReference jr = lookupByPubmedId( pubmed_id );
        
        if ( jr == null )  
        {
            if ( log.isDebugEnabled() )
                log.debug( "No journal references with pubmed_id=" 
                         + pubmed_id
                         + " so creating new JournalReference" );  
                
            jr = new JournalReference();
            jr.setPubmedId( pubmed_id );
        }
        
        return jr;
    }
    

    /**
    *   Returns the list of {@link JournalReference}s that contain 
    *   the given {@link Author}.
    */
    @SuppressWarnings("unchecked") // cause hibernate is non-generic
    public static List<JournalReference> lookupByAuthor( Author a )
    {
        checkNotNull( a );
        
        String name = a.toCitationString();
        
        if ( log.isDebugEnabled() )
        {
            log.debug( "looking up JournalReferences with author='" 
                     + name
                     + "'"
                     );
        }
        
        List<JournalReference> jr_list = (List<JournalReference>) 
            Eurocarb.getEntityManager()
                    .getQuery("org.eurocarbdb.dataaccess.core.JournalReference.BY_AUTHOR")
                    .setParameter("name", "%" + name + "%" )
                    .list();

        if ( jr_list == null )  
        {
            if ( log.isDebugEnabled() )
                log.debug( "No journal references with author=" 
                         + name
                         );  
            return Collections.emptyList();
        }
        
        return jr_list;
    }
    
    
    /**
    *   Retrieves a JournalReference by citation, returning null if
    *   no reference matches the given parameters.
    */
    public static JournalReference lookupByCitation( 
            String journal_title, 
            Integer pub_year, 
            Integer volume, 
            Integer page )
    {
        checkNotNull( journal_title );
        checkNotEmpty( journal_title );
        

        if ( pub_year <= 1800 )
            throw new IllegalArgumentException(
                "Expecting publication year > 1800");
            
        if ( volume < 0 )
            throw new IllegalArgumentException(
                "Volume argument must be >= 0");

        if ( page < 0 )
            throw new IllegalArgumentException(
                "Page argument must be >= 0");
            
        JournalReference jr = (JournalReference) 
            Eurocarb.getEntityManager()
                    .getQuery("org.eurocarbdb.dataaccess.core.JournalReference.BY_JOURNAL_YEAR_VOLUME_PAGE")
                    .setParameter("title", journal_title )
                    .setParameter("year", pub_year )
                    .setParameter("volume", volume )
                    .setParameter("page", page )
                    .uniqueResult();
                    
        return jr;
    }
    
    
    /**
    *   Retrieves a JournalReference by Pubmed id.
    */
    public static JournalReference lookupByPubmedId( Integer pubmed_id )
    {
        if ( pubmed_id <= 0 )
            throw new IllegalArgumentException(
                "Pubmed id argument must be a positive integer, got " + pubmed_id );
        
        if ( log.isDebugEnabled() )
            log.debug("looking up JournalReference with pubmed_id=" + pubmed_id );
        
        JournalReference jr = (JournalReference) 
            Eurocarb.getEntityManager()
                    .getQuery("org.eurocarbdb.dataaccess.core.JournalReference.BY_PUBMED_ID")
                    .setParameter("pubmed_id", pubmed_id )
                    .uniqueResult();
                    
        return jr;
    }

    /**
     *  Create an instance of a JournalReference from a given pubmedId
     */
    public static JournalReference createFromPubmedId(int pubmedId)
    {        
        JournalReference result = lookupByPubmedId(pubmedId);

        if (result != null) {
            return result;
        }

        String xml_answer;
        try {
            xml_answer = makeHttpRequest(new URL("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=pubmed&retmode=xml&id=" + pubmedId));
        } catch (Exception e) {
            log.debug("Error retrieving Pubmed result",e);
            return null;
        }
        result = createFromPubmed(xml_answer);
        
        if (result.getPubmedId() != pubmedId) {
            return null;
        }
        
        return result;
    }

    /**
    *   Create an instance of JournalReference from an XML representation
    *   of a Pubmed record.
    */
    private static JournalReference createFromPubmed(String data) 
    {
        Document pmrec = XMLUtils.read(data);
        if( pmrec==null ) 
            return null;
    
        JournalReference ret = new JournalReference();
    
        // parse journal
        ret.journal = Journal.createOrLookup(getPubmedItem(pmrec,"FullJournalName"));
        if (ret.journal != null && ret.journal.getJournalId() == 0) {
            ret.journal.setJournalAbbrev(getPubmedItem(pmrec,"Source"));
        }
    
        // parse fields
        ret.pubmedId = getPubmedItemAsInt(pmrec,"Id",0);            
        ret.authors = getPubmedItem(pmrec,"Author");
        ret.title = getPubmedItem(pmrec,"Title");
        ret.journalVolume = getPubmedItemAsInt(pmrec,"Volume",0);
    
        // parse year
        Matcher ym = year_pattern.matcher(getPubmedItem(pmrec,"PubDate"));
        if( ym.matches() )
            ret.publicationYear = Integer.valueOf(ym.group(1));
        
        // parse pages
        Integer start = 0;
    Integer end = 0;
        Matcher pm = pages_pattern.matcher(getPubmedItem(pmrec,"Pages"));
    if( pm.matches() ) {
        if( pm.group(1)!=null && pm.group(1).length()>0 ) 
        start = Integer.valueOf(pm.group(1));
        
        if( pm.group(2)!=null && pm.group(2).length()>0 ) {
        end = Integer.valueOf(pm.group(2));
        end = start + ((10000+end-start)%100);
        }        
        else
        end = start;
    }
    
        ret.journalStartPage = start;
        ret.journalEndPage = end;   
    
        return ret;
    }

    
    private static Integer getPubmedItemAsInt(Document pmrec, String name, Integer _default) 
    {
        try 
        {
            return Integer.valueOf(getPubmedItem(pmrec,name));
        }
        catch(NumberFormatException e) 
        {
            return _default;
        }
    }

    private static String getPubmedItem( Document pmrec, String name ) 
    {
        StringBuilder ret = new StringBuilder();
    
        NodeList items = pmrec.getElementsByTagName(name);
        if( items.getLength()>0 ) 
        {
            // get by tag name
            for( int i=0; i<items.getLength(); i++ ) 
            {
                Node item = items.item(i);
                if( ret.length()>0 )
                    ret.append("; ");
                ret.append(XMLUtils.getText(item));
            }
        }
        else 
        {
            // get by attribute
            items = pmrec.getElementsByTagName("Item");     
            for( int i=0; i<items.getLength(); i++ ) 
            {
                Node item = items.item(i);
                String item_name = XMLUtils.getAttribute(item,"Name");
                if( item_name!=null && item_name.equals(name) ) 
                {
                    if( ret.length()>0 )
                    ret.append("; ");
                    ret.append(XMLUtils.getText(item));
                }
            }
        }
        
        return ret.toString();
    }

    private static String makeHttpRequest(URL url) throws Exception 
    {                 
        // read result
        URLConnection urlc = url.openConnection();
        urlc.setUseCaches(false); // Don't look at possibly cached data

        BufferedReader br = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
        int ch;
        StringBuilder ret = new StringBuilder();
        while( (ch = br.read())!=-1 ) {
            ret.appendCodePoint(ch);
        }
        return ret.toString();
    }
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~  METHODS  ~~~~~~~~~~~~~~~~~~~~~~~~//
    
    public boolean matches( JournalReference other ) 
    {
        if( other==null ) {
            log.debug("other is null");
            return false;
    }
        
        if( !testNull(this.journal,other.journal) 
            || !this.journal.getJournalTitle().equals(other.journal.getJournalTitle()) ) 
        {
            log.debug("incompatible journals");
            return false;
        }
        
        if( !testNull(this.pubmedId,other.pubmedId) 
            || !this.pubmedId.equals(other.pubmedId) ) 
        {
            log.debug("incompatible pubmed id");
            return false;
        }
        
        if( !testNull(this.authors,other.authors) 
            || !this.authors.equals(other.authors) ) 
        {
            log.debug("incompatible authors");
            return false;
        }
        
        if( !testNull(this.title,other.title) 
            || !this.title.equals(other.title) )
        {
            log.debug("incompatible title");
            return false;
        }
        
        if( ! testNull(this.publicationYear,other.publicationYear) 
            || !this.publicationYear.equals(other.publicationYear) ) 
        {
            log.debug("incompatible pub year " + this.publicationYear + "<>" + other.publicationYear);
            return false;
        }
        
        if( ! testNull(this.journalVolume,other.journalVolume) 
            || ! this.journalVolume.equals(other.journalVolume) ) 
        {
            log.debug("incompatible volume");
            return false;
        }
        
        if( !testNull(this.journalStartPage,other.journalStartPage) 
            || !this.journalStartPage.equals(other.journalStartPage) ) 
        {
            log.debug("incompatible start page");
            return false;
        }
        
        if( !testNull(this.journalEndPage,other.journalEndPage) 
            || !this.journalEndPage.equals(other.journalEndPage) ) 
        {
            log.debug("incompatible end page");
            return false;
        }
    
        return true;
    }
       

    private boolean testNull( Object a, Object b ) 
    {
        if( a==null && b==null )
            return true;
        if( a!=null && b!=null )
            return true;
        return false;
    }
    

    public int getJournalReferenceId() 
    {
        return this.journalReferenceId;
    }
    
    
    /** 
    *   Journal references return their Pubmed ID as their external
    *   reference id. @see #getPubmedId 
    */
    public String getExternalReferenceId()
    {
        return "" + getPubmedId();    
    }
    
    
    /** 
    *   Equivalent to to {@link #setPubmedId}
    */
    public void setExternalReferenceId( String pubmed_id )
    {
        checkNotNull( pubmed_id ); 
        String id = pubmed_id.trim();
        
        checkNotEmpty( id ); 
        super.setExternalReferenceId( id );
        
        if ( id.equals( "" + getPubmedId() ) )
            return;
        
        try
        {
            Integer i = new Integer(id);
            setPubmedId( i );    
        }
        catch ( NumberFormatException e )
        {
            log.warn( "Couldn't get a number from String '" 
                    + id 
                    + "'"
                    , e 
                    );
        }
    }
    
    
    /**
    *   Returns the {@link Journal} of this {@link JournalReference}. 
    */
    public Journal getJournal() 
    {
        return this.journal;
    }
    
    
    public void setJournal( Journal j ) 
    {
        this.journal = j;
    }

    
    /**
    *   Returns a typical citation string for a journal reference, eg:
    *   <tt>Am J Hum Genet (1985) 37; 749-760</tt>
    */
    public String getJournalAsCitationString()
    {
        StringBuilder sb = new StringBuilder(); 
    
        sb.append( getJournal().getJournalTitle() );
        sb.append(" (");
        sb.append( publicationYear );
        sb.append(") ");
        sb.append( journalVolume );
        sb.append("; ");
        sb.append( journalStartPage );
        sb.append("-");
        sb.append( journalEndPage );
    
        return sb.toString();
    }
    
    
    /**
     * Return the PubmedID of this Journal Reference. If there is no pubmed
     * ID return 0
     */
    public Integer getPubmedId() 
    {
        if (this.pubmedId == null) {
            this.pubmedId = new Integer(0);
        }
        return this.pubmedId;
    }
    
    
    public void setPubmedId( Integer pubmedId ) 
    {
        this.pubmedId = pubmedId;
        this.setExternalReferenceId( "" + pubmedId );
    }
    
    
    public String getAuthors() 
    {
        return this.authors;
    }
    
    
    public void setAuthors( String authors ) 
    {
        this.authors = authors;
    }
    
    
    public List<Author> getAuthorList()
    {
        if ( this.authorlist != null )
            return Collections.unmodifiableList( authorlist );
        
        if ( authors == null || authors.length() == 0 )
        {
            log.warn("No authors currently set, returning empty authorlist");
            return Collections.emptyList();
        }
     
        authorlist = Author.parseAuthorList( authors );
        
        return Collections.unmodifiableList( authorlist );
    }

    
    public String getAuthorListAsCitationString() 
    {
        StringBuilder sb = new StringBuilder();        
    
        boolean first = true;
        for( Author a : getAuthorList() ) 
        {
            if( !first ) 
            sb.append(", ");        
            sb.append(a.toCitationString());
            first = false;
        }
        return sb.toString();
    }
    
    
    /** 
    *   Returns a short citation String of form "Harrison et al., 2008" 
    *   (for 2+ authors), "Harrison and Ceroni, 2008" (for 2 authors),
    *   or "Harrison, 2008" (for exactly 1 author) 
    */
    public String getCitationString()
    {
        switch ( getAuthorList().size() )
        {
            case 0: 
                return "";
                
            case 1:
                return getAuthorList().get(0).getLastname()
                    + ", "
                    + getPublicationYear();
                    
            case 2:
                return getAuthorList().get(0).getLastname()
                    + " and "
                    + getAuthorList().get(1).getLastname()
                    + ", "
                    + getPublicationYear();
                    
            default:
                return getAuthorList().get(0).getLastname()
                    + " et al., "
                    + getPublicationYear();
        }
    }
    
    
    public Author getFirstAuthor()
    {
    
        if( getAuthorList().size()==0 )
            return null;
        
        return getAuthorList().get( 0 );   
    }
    
    
    public String getTitle() 
    {
        return this.title;
    }
    
    
    public void setTitle( String title ) 
    {
        this.title = title;
    }
    
    
    public Integer getPublicationYear() 
    {
        return this.publicationYear;
    }
    
    
    public void setPublicationYear( Integer year ) 
    {
        this.publicationYear = year;
    }
    
    
    public Integer getJournalVolume() 
    {
        return this.journalVolume;
    }
    
    
    public void setJournalVolume( Integer journalVolume ) 
    {
        this.journalVolume = journalVolume;
    }
    
    
    public Integer getFirstPage() 
    {
        return this.journalStartPage;
    }
    
    
    public void setFirstPage( Integer page_number ) 
    {
        this.journalStartPage = page_number;
    }
    
    
    public Integer getLastPage() 
    {
        return this.journalEndPage;
    }
    
    
    public void setLastPage( Integer page_number ) 
    {
        this.journalEndPage = page_number;
    }

    
    public String getUrl() 
    {
        if ( super.getUrl() != null )
            return super.getUrl();
        
        Integer id      = this.getPubmedId();
        int pubmed_id   = (id != null) ? id.intValue() : 0; 
        String citation = this.toCitationString();
        
        if ( pubmed_id <= 0 && (citation == null || citation.length() == 0) )
        {
            log.warn("cannot return an url for JournalReference: insufficient data");
            return null;
        }
            
        String base_url = Eurocarb.getProperty("pubmed.query.url");
        if ( base_url == null || base_url.length() == 0 )
        {
            log.debug("Property 'pubmed.query.url' not defined, using default value");
            base_url = Default_Pubmed_Query_Url;
        }
        
        try
        {
            String url = ( pubmed_id > 0 )
                       ? base_url + URLEncoder.encode( pubmed_id + " [uid]", "UTF-8" )
                       : base_url + URLEncoder.encode( citation, "UTF-8" )
                       ;   
    
            if ( log.isTraceEnabled() )
                log.trace("pubmed url is " + url );
            
            this.setUrl( url );
            
            return url;
        }
        catch ( java.io.UnsupportedEncodingException ignored ) 
        {
            log.warn( ignored );
            return null;
        }
    }
    
    
    public int hashCode()
    {
        String unique = "" 
                      + pubmedId 
                      + publicationYear
                      + journalVolume
                      + journalStartPage
                      + journalEndPage
                      + (journal == null ? "" : journal.hashCode())
                      ;
        
        return unique.hashCode(); 
    }
    
    
    public boolean equals( Object x )
    {

        if ( this == x ) {
            return true;
        }
        
        if ( (x == null) || ! (x instanceof JournalReference) ) {
            return false;
        }
        
        // objects are the same class
        final JournalReference r = (JournalReference) x;

        return r.pubmedId == null ? this.pubmedId == null : r.pubmedId.equals(this.pubmedId)
            && r.publicationYear == null ? this.publicationYear == null : r.publicationYear.equals(publicationYear)
            && r.journalVolume == null ? this.journalVolume == null : r.journalVolume.equals(this.journalVolume)
            && r.journalStartPage == null ? this.journalStartPage == null : r.journalStartPage.equals(this.journalStartPage)
            && r.journalEndPage == null ? this.journalEndPage == null : r.journalEndPage.equals(this.journalEndPage)
            && (r.journal == null ? this.journal == null : r.journal.equals(this.journal))
            ;
    }
    
    
    /** 
    *   Returns this journal reference as a formatted text journal 
    *   reference citation.
    *   eg: <code></code>
    */
    public String toCitationString2()
    {
        List<Author> authors = getAuthorList();
        int count = authors.size();
        assert count > 0;
        
        String auth_str;
        Author a = getFirstAuthor();
        
        if( a != null )
            auth_str  = count > 1 ? a.getLastname() + ", " : a.getLastname() + " et al., ";
        else 
            auth_str = "";
        
        return auth_str
             + "("
             + getPublicationYear()
             + ") "
             + getJournal().getJournalTitle()
             + " "
             + getJournalVolume()
             + ": "
             + getFirstPage()
             + "-"
             + getLastPage()
             ;
    }
    
    
    /** 
    *   Returns this journal reference as a formatted text journal 
    *   reference citation, <string>without</strong> author names,
    *   ie: just the journal reference.
    *   eg: <code>Adv Microb Physiol (1993) 35: 135-246</code>
    */
    public String toCitationString()
    {
        Journal j = this.getJournal();
        if ( j == null ) 
            return null;
        
        return j.getJournalTitle()
             + " ("
             + getPublicationYear()
             + ") "
             + getJournalVolume()
             + ": "
             + getFirstPage()
             + "-"
             + getLastPage()
             ;
    }
    
    
    //~~~~~~~~~~~~~~~~~~~~~~ PRIVATE METHODS ~~~~~~~~~~~~~~~~~~~~~~~~

    /** Internal use only! */
    void setJournalReferenceId( int journalReferenceId ) 
    {
        this.journalReferenceId = journalReferenceId;
    }
    
    
} // end class


