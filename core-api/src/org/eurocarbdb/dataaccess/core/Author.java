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
*   Last commit: $Rev: 1210 $ by $Author: glycoslave $ on $Date:: 2009-06-12 #$  
*/

package org.eurocarbdb.dataaccess.core;

//  stdlib imports
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.MatchResult;
import java.io.Serializable;

//  3rd party imports
import org.apache.log4j.Logger;

//  eurocarb imports

//  static imports
import static org.eurocarbdb.util.JavaUtils.*;
import static org.eurocarbdb.util.StringUtils.join;

/**
*   Simple wrapper class representing a {@link JournalReference} or 
*   {@link Reference} author, along with various methods to export/parse 
*   an Author to/from a {@link String}.
*   @see Author.Format
*   @author mjh
*/
public class Author implements Serializable
{
    //  INNER CLASSES
    
    /*  enum Format  *//********************************************* 
    *
    *   Enumeration of string formats to match a person's name.
    *   Formats are intended to be matched in declared order, using
    *   the {@link #matches} method, ie:<br/>
    *<pre>
    *       String input = ...;
    *       for ( Format format : Format.values() )
    *           if ( format.matches( input )
    *               Author a = format.getAuthor();
    *</pre>
    *   Similarly, formats are intended to be bi-drectional, ie:
    *   an {@link Author}'s name ought to be able to be produced in a
    *   given {@link Format} using the {@link Author#toString(Format)} 
    *   method, ie:
    *<pre>
    *       Author a = ...;
    *       for ( Format format : Format.values() )
    *           System.out.println( a.toString( format ) );
    *</pre>
    *
    *   @see Author#toString(Format)
    */
    public enum Format
    {
        /*  format Lastname_First_Then_Initials  *//*****************
        *
        *   Format spec to match a string with lastname first, then an 
        *   optional comma, then 1-3 initials, with or without fullstops, 
        *   eg: <tt>Harrison MJ</tt>, <tt>Harrison, MJ</tt>, or <tt>Harrison, M.J.</tt>.
        *   Initials need to be capitalised to be recognised as such.
        */
        Lastname_First_Then_Initials
        ( 
              "("                   //--- start capture 1 ---
            +   "(?:\\w+)"          // a word, then...
            +   "(?:"               //    (a grouping of)
            +      "(?:-|\\s)"      // ...either hyphen or space  
            +      "(?:\\w+)"       // ...then another word
            +   "){0,3}"            // ...up to 3 (possibly hyphenated) words     
            + ")"                   //--- end capture group 1 ---
            + "\\s*,?\\s*"          // optional comma/spaces
            + "("                   //--- start capture group 2 ---
            +    "\\b"              //  start of a word (boundary)
            +    "(?:"              //    (a grouping of)
            +       "(?:[A-Z])"     // ...an initial
            +       "\\.?"          // ...then maybe a fullstop
            +    "){1,3}"           // ...and between 1-3 of them (initials)
            + ")"                   //--- end capture group 2 ---
        )
        {
            void convertMatchResult2Author( MatchResult result )
            {
                String lname  = result.group(1);
                String initials = result.group(2);
                
                if ( log.isTraceEnabled() )
                    log.trace( "creating Author: lastname=" 
                             + lname 
                             + ", initials=" 
                             + initials 
                             );
                
                this.author = new Author( lname, initials.split("(\\s|\\.)+") );
            }
        }
        ,
        
        /*  format Lastname_First_Then_Firstnames  *//***************
        *
        *   Format spec to match a string with lastname first, then a comma,
        *   then a list of firstnames, eg: <tt>Harrison, Mathew John</tt>
        */
        Lastname_First_Then_Firstnames
        (
              "("                   //--- start capture group 1 ---
            +   "\\w+"              // a word, then...
            +   "(?:"               //    (a grouping of)
            +      "(?:-|\\s)"      // ...either hyphen or space  
            +      "(?:\\w+)"       // ...then another word
            +   "){0,3}"            // ...up to 3 (possibly hyphenated) words     
            + ")"                   //--- end capture group 1 ---
            + "\\s*,\\s*"           // required comma, optional space
            + "("                   //--- start capture group 2 ---
            +     "\\w+"            //    a word...
            +     "(?:"             //    (a grouping of) 
            +        "(?:-|\\s)+"   // ...hyphen or space  
            +        "\\w+"         // ...then another word
            +     "){0,3}"          // ...and up to 3 (possibly hyphenated) words
            + ")"                   //--- end capture group 2 ---
        )
        {
            void convertMatchResult2Author( MatchResult result )
            {
                String lname  = result.group(1);
                String fnames = result.group(2);
                
                if ( log.isTraceEnabled() )
                    log.trace( "creating Author: lastname=" 
                             + lname 
                             + ", firstnames=" 
                             + fnames 
                             );
                    
                this.author = new Author( lname, fnames.split("\\s+") );
            }
        }
        ,
        
        /*  format Firstnames_Then_Lastname  *//*********************
        *
        *   Format spec to match a string containing a regular list of 
        *   first names then a lastname eg: <tt>Mathew John Harrison</tt>.
        */
        Firstnames_Then_Lastname
        ( 
              "("                   // match 
            +   "\\w+"              // any number 
            +   "(?:"               // of possibly 
            +     "(?:-|\\s)"       // hyphenated 
            +     "\\w+"            // words and
            +   ")*"                // then split
            + ")"                   // them later
        )
        {
            void convertMatchResult2Author( MatchResult result )
            {
                String namelist = result.group();
                String[] names  = namelist.split("\\s+");
                
                if ( names.length == 1 )
                {
                    if ( log.isTraceEnabled() )
                        log.trace( "creating Author: lastname=" 
                                 + names[0] 
                                 + ", no firstnames" 
                                 );
                        
                    this.author = new Author( names[0], (java.lang.String[]) null );
                    return;
                }
                    
                String lastname = names[names.length - 1];
                List<String> fnames = new ArrayList<String>();
                int i;
                for ( i = 0; i < names.length - 1; i++ )
                    fnames.add( names[i] );
                
                //  check for special names, such as those that use
                //  lastname prefixes like 'de la', 'mc, etc...
                i = names.length - 2;
                while ( i >= 0 ) 
                {
                    boolean perform_another_pass = false;
                    for ( String s : Awkward_Names )
                    {
                        if ( s.equalsIgnoreCase( names[i] ) )
                        {
                            if ( log.isTraceEnabled() )
                                log.trace( "assuming " + fnames.get(i) + " is part of lastname" );
                            lastname = fnames.remove( i ) + " " + lastname; 
                            perform_another_pass = true;   
                            break;
                        }
                    }
                    
                    if ( ! perform_another_pass )
                        break;
                    
                    i--;
                }
                
                if ( log.isTraceEnabled() )
                    log.trace( "creating Author: lastname=" 
                             + lastname 
                             + ", firstnames=" 
                             + join(" ", fnames) 
                             );
                    
                this.author = new Author( lastname, 
                                          fnames.toArray( new String[fnames.size()] ) );
            }
        }
        
        ; // end enumeration of Formats
        
        
        Pattern pattern = null;
        Author author = null;
    
        /** Constructor */
        Format( String regexp )
        {
            if ( regexp != null && regexp.length() > 0 )
                this.pattern = Pattern.compile( regexp );
        }

        /** 
        *   Convert a {@link MatchResult} to an {@link Author} object; 
        *   intended to be overridden by the individual (anonymous) Enum 
        *   instances. 
        */
        abstract void convertMatchResult2Author( MatchResult result );
        
        /** Returns an {@link Author} object from the input string passed 
        *   to the {@link #matches} method, or null if unmatched. */
        public Author getAuthor() {  return author;  }
        
        /** Returns true if the passed input String is matched by the 
        *   current format. */
        public boolean matches( String input )
        {
            log.trace("trying to match '" + input + "' against " + this );
            
            if ( this.pattern != null )
            {
                Matcher m = this.pattern.matcher( input );
                if ( m.matches() )
                {
                    log.trace("match succeeded");
                    convertMatchResult2Author( m.toMatchResult() );
                    return true;
                }
                else 
                {
                    log.trace("match failed");
                    return false;
                }
            }
            else 
            {
                return false;   
            }
        }
        
    } // end enum Format --------------------------------------------
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /** Logging handle. */
    protected static final Logger log 
        = Logger.getLogger( Author.class.getName() );
        
    /** {@link Pattern} used to {@link Pattern#split split} a 
    *   {@link String} containing potentially multiple {@link Author}s; 
    *   the default pattern is to split on a semicolon ';'. */
    public static Pattern Multiple_Authors
        = Pattern.compile("\\s*;\\s*");
        
    private static final String[] Awkward_Names = 
        {   "della" , "de" , "di" , "du" , "le", "la" , "mc" , "mac"  }; 
       
    
    //~~~~~~~~~~~~~~~~~~~~~~~~  FIELDS  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        
    /** Author's lastname */
    private final String lastname;
    
    /** List of firstnames -- a firstname may be an initial only! */
    private final String[] firstnames;
    
    
    //~~~~~~~~~~~~~~~~~~~~~~  CONSTRUCTORS  ~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /**
    *   Attempts to create an Author by parsing the given text string,
    *   which is assumed to be the author's full name. 
    *
    *   @see #parseAuthor(String)
    *   @see #parseAuthor(String,Format)
    *   @throws IllegalArgumentException 
    *   if passed {@link String} is null or zero-length. 
    */
    public Author( String name )
    {
        Author their = parseAuthor( name );
        this.lastname = their.lastname;
        this.firstnames = their.firstnames;
    }

    
    /**
    *   Explicit lastname/firstnames constructor. Initials
    *   are acceptable as firstnames.
    */
    public Author( String last_name, String... first_names )
    {
        assert first_names != null;
        assert first_names.length > 0;
        
        this.firstnames = first_names;
        this.lastname   = last_name;
    }
    
    
    //~~~~~~~~~~~~~~~~~~~~~~  STATIC METHODS  ~~~~~~~~~~~~~~~~~~~~~~~
    
    /**
    *   Attempts to parse an {@link Author} from a text string of 
    *   arbitrary name format.
    *
    *   @throws IllegalArgumentException 
    *           if passed {@link String} is null, zero-length, or contains 
    *           only space characters.
    *   @return Author object if parseable, null if not.
    */
    public static Author parseAuthor( String text )
    {
        checkNotNull( text );
        String author_text = text.trim();
        checkNotEmpty( author_text );
        
        
        //  hack to handle names with apostrophes, otherwise regex-based
        //  name recognition is going to be too painful
        author_text = author_text.replace('\'', '_');
       
        Author a = null;
        for ( Format format : Format.values() )
        {
            if ( format.matches( author_text ) )   
            {
                a = format.getAuthor();
                assert a != null;
                break;
            }
        }
        
        if ( a == null )
        {
            log.trace( "Could not parse an author from text string at '" 
                     + author_text 
                     + "'"
                     );   
        }
        
        return a;
    }
    
    
    /**
    *   Attempts to parse an {@link Author} from a text string using 
    *   the given {@link Format}.
    *
    *   @throws IllegalArgumentException 
    *           if passed {@link String} is null, zero-length, or contains 
    *           only space characters.
    *   @return Author object if parseable, null if not.
    */
    public static Author parseAuthor( String text, Format format )
    {
        checkNotNull( text );
        String author_text = text.trim();
        checkNotEmpty( author_text );
        
        //  hack to handle names with apostrophes, otherwise regex-based
        //  name recognition is going to be too painful
        author_text = author_text.replace('\'', '_');
        
        Author a = null;
        if ( format.matches( author_text ) )   
        {
            a = format.getAuthor();
            assert a != null;
        }
        
        if ( a == null )
        {
            log.trace( "Could not parse an author from text string at '" 
                     + author_text 
                     + "' using format "
                     + format
                     );   
        }
        
        return a;
    }
    
    
    /** 
    *   Parses a string for Authors, returning the list of Authors
    *   found.
    *   @see #Multiple_Authors
    */
    public static List<Author> parseAuthorList( String author_list_string )
    {
        checkNotEmpty( author_list_string );
        
        String[] authors = Multiple_Authors.split( author_list_string );
        
        if ( log.isDebugEnabled() )
            log.debug( "input authorlist string '"
                     + author_list_string
                     + "' parses into list: "
                     + join(", ", authors)
                     );
            
        List<Author> authorlist = new ArrayList<Author>( authors.length ); 
            
        for ( String author_string : authors )
        {
            if ( author_string.length() == 0 )
            {
                log.warn("Encountered zero-length author name, skipping...");
                continue;
            }
            
            Author a = parseAuthor( author_string );
            //checkNotNull( a );

            if ( a == null )
            {
                //authorlist.add( new Author( author_string ) );
                log.warn(
                    "! could not parse an Author from string '" 
                    + author_string 
                    + "' -- skipping..."
                );
                continue;
            }
            else
            {
                authorlist.add( a );        
            }
        }
        
        return authorlist;
    }
    
    
    /** 
    *   Parses a string for Authors, returning the list of Authors
    *   found.
    *   @see #Multiple_Authors
    */
    public static List<Author> parseAuthorList( String author_list_string, Format format )
    {
        checkNotNull( author_list_string );
        checkNotEmpty( author_list_string );
        
        String[] authors = Multiple_Authors.split( author_list_string );
        
        if ( log.isTraceEnabled() )
            log.trace( "input authorlist string '"
                     + author_list_string
                     + "' parses into list: "
                     + join(", ", authors)
                     );
            
        List<Author> authorlist = new ArrayList<Author>( authors.length ); 
            
        for ( String author_string : authors )
        {
            if ( author_string.length() == 0 )
            {
                log.warn("Encountered zero-length author name, skipping...");
                continue;
            }
            
            Author a = parseAuthor( author_string, format );
            //checkNotNull( a );
            
            if ( a == null )
            {
                log.warn("Couldn't parse an Author from string: " + author_string );
                authorlist.add( new Author("<surname>","<name>") );
            }
            else
                authorlist.add( a );
        }
        
        return authorlist;
    }
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~  METHODS  ~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /**
    *   Returns all firstnames, which may be initials if this Author
    *   was constructed using only firstname initials.
    *   @see StringUtils#join(Object[])
    */
    public String[] getAllFirstnames()
    {
        return firstnames;   
    }
    
    
    /** Returns Author's firstname. */
    public String getFirstname() 
    {
        checkNotEmpty( firstnames );
        return firstnames[0];  
    }

    
    /** Returns the list of initials for all firstnames. */
    public char[] getFirstnameInitials() 
    {
        if ( firstnames == null || firstnames.length == 0 )
            return new char[] {};
        
        char[] initials = new char[ firstnames.length ];
        for ( int i = 0; i < firstnames.length; i++ )
            initials[i] = firstnames[i].charAt(0);
        
        return initials;
    }
    
    
    /**
    *   Returns this {@link Author}'s initials as an (uppercase)
    *   {@link String}, ie: <tt>Mathew John Harrison</tt> returns
    *   <tt>"MJ"</tt>
    */
    public String getFirstnameInitialsString()
    {
        return new String( getFirstnameInitials() ).toUpperCase();    
    }
    
    
    /** Returns author lastname */
    public String getLastname() 
    {
        checkNotEmpty( lastname );
        return lastname;  
    }
    
    
    /** 
    *   Returns this Author's name in the format {@link Lastname_First_Then_Initials}, 
    *   eg: <tt>Harrison, M.J.</tt>. 
    *   @see Format#Lastname_First_Then_Initials
    */
    public String toCitationString()
    {
        StringBuilder sb = new StringBuilder( getLastname() );        
        sb.append(' ');
        for ( char i : getFirstnameInitials() ) 
            sb.append( i );       
            
        return sb.toString();
    }
    
    
    /** 
    *   Returns this Author's name in the format {@link Firstnames_Then_Lastname}, 
    *   eg: <tt>Mathew John Harrison</tt>. 
    *   @see Format#Firstnames_Then_Lastname
    */
    public String toString()
    {
        return join(" ", getAllFirstnames()) 
             + " " 
             + getLastname()
             ;
    }
 
    /** 
    *   Returns this Author's name in the given {@link Format}.
    */
    public String toString( Format f )
    {
        return "TODO";
    }
    
} // end class Author


