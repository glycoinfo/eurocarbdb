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
*   Last commit: $Rev: 1147 $ by $Author: glycoslave $ on $Date:: 2009-06-04 #$  
*/

package test.eurocarbdb.dataaccess.core;

import org.testng.annotations.*;
import org.apache.log4j.Logger;

import org.eurocarbdb.dataaccess.core.Author;
import org.eurocarbdb.util.StringUtils;

public class AuthorTest extends test.eurocarbdb.CommandLineTest
{
    /** Logging handle. */
    private static final Logger log 
        = Logger.getLogger( AuthorTest.class.getName() );
    

    @BeforeClass
    protected void configure() throws Exception 
    {
    }
    
    
    public static final String[] author_list = 
    {
        //  lastname, firstnames    | firstname lastname    | lastname, initials
        "Harrison, Mathew"          , "Mathew Harrison"     , "Harrison, M."    , 
        "Harrison, Mathew John"     , "Mathew John Harrison", "Harrison, M.J."  , 
        "Doe, Jon Samuel James"     , "Jon Samuel James Doe", "Doe, J.S.J."     ,
        "Doe, Jo"                   , "Jo Doe"              , "Doe J"           ,
        "Hill-Harrison, Matt"       , "Matt Hill-Harrison"  , "Hill-Harrison M" ,
        "De Silva, Antony"          , "Antony De Silva"     , "De Silva A"      ,
        "La Salle, Robert"          , "Robert La Salle"     , "La Salle, R."    ,
        "de la Soul, Antony"        , "Antony de la Soul"   , "de la Soul A"    ,
        "de la Croix-rouge, Sam"    , "Sam de la Croix-rouge", "de la Croix-rouge, S",
        "JONES, TOM"                , "TOM JONES"           , "JONES T"         ,
        "Howard, Anna-Louise"       , "Anna-Louise Howard"  , "Howard A"            
    };   
    
    public static final String multiple_authors = StringUtils.join(";", author_list );
    
    @Test
    public void parseSingleAuthor() throws Exception 
    {
        int i = 0;
        Author[] authors = new Author[author_list.length];
        
        for ( String s : author_list )
        {
            try 
            {
                System.err.println();
                System.err.println("----- author test " + i + ": '" + s + "' -----");

                Author a = Author.parseAuthor( s );
                authors[i] = a;
                i++;
                
                assert a != null;
                //if ( a == null ) continue;
                
                log.info( "citation: " + a.toCitationString() );
            }
            catch ( Exception e )
            {
                log.warn( e );   
            }
        }   
        
        //  check authors match
        System.err.println();
        System.err.println("----- checking that equivalent author strings produce equal Author objects -----" );
        for ( i = 0; i < authors.length; i+=3 )
        {
            String a = authors[i+0].toCitationString();
            String b = authors[i+1].toCitationString();
            String c = authors[i+2].toCitationString();
            
            boolean true_if_equal = ( a.equals(b) && b.equals(c) );
            
            log.info( "test " 
                    + ((i/3)+1) 
                    + ": " 
                    + (true_if_equal ? "PASSED" : "FAILED") 
                    + ": " 
                    + a 
                    + " == " 
                    + b 
                    + " == " 
                    + c 
                    );
            
            assert true_if_equal;
        }
    }
    
}