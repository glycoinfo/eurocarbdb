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
*   Last commit: $Rev: 1593 $ by $Author: hirenj $ on $Date:: 2009-08-14 #$  
*/

package org.eurocarbdb.util;

import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;

import java.net.URI;
import java.util.Date;
import java.text.DateFormat;

import org.apache.log4j.Logger;


/**
*   A utility class to provide various string operations that really 
*   ought to already have been included in Java in the first place. 
* 
*   @author mjh 
*/
public final class StringUtils
{
    /** logging handle */
    static Logger log = Logger.getLogger( StringUtils.class );
    
    /** Shortcut to the java system property "line.separator" for 
    *   platform-independant carriage returns.  */
    public static final String CR = System.getProperty("line.separator");

    /** The DateFormat used for parsing {@link Date}s in the {@link coerce}
    *   method. The default is to use a date/time format consistent with the
    *   current default locale. To reset to the default after changing, set to null. */
    public static DateFormat defaultDateFormat = null;

    /** class has static methods only */    
    private StringUtils() {}
        
    
    /** 
    *   Attempts to coerce or derive the given {@link String} value 
    *   to the given {@link Class}. For example, most primitive classes
    *   can be coerced, eg:
    *<pre>
    *       int i = coerce( "234", Integer.class ); 
    *       long l = coerce( "234", Long.class ); 
    *       float f = coerce( "234.567", Float.class ); 
    *       double f = coerce( "234.567", Double.class ); 
    *       boolean b = coerce( "false", Boolean.class ); 
    *</pre>
    *
    *   Certain additional non-primitive classes are also supported. 
    *<pre>
    *       Date d = coerce("Dec 15, 2008", Date.class ); // kinda broken atm...
    *       URI u = coerce("http://www.eurocarbdb.org", URI.class ); 
    *</pre>
    *
    *   @throws IllegalArgumentException 
    *   If the property value could not be coerced to the given class 
    *   (ie: there was an exception), or if the passed String was null.
    *   @throws UnsupportedOperationException
    *   If there was no support for coercion to the given class.
    */
    public static final <T> T coerce( String value, Class<T> to_class )
    {
        if ( value == null )
            throw new IllegalArgumentException(
                "Passed String value was null");
        try
        {
            //  primitives
            if ( to_class == Integer.class )
                return (T) new Integer( value );
            if ( to_class == Double.class )
                return (T) new Double( value );
            if ( to_class == Boolean.class )
                return (T) new Boolean( value );
            if ( to_class == Long.class )
                return (T) new Long( value );
            if ( to_class == Float.class )
                return (T) new Float( value );
            if ( to_class == Character.class )
                return (T) new Character( value.charAt( 0 ) );
            
            //  other classes...
            
            // Date parsing is wtf broken, commenting out for now...
            /*
            else if ( to_class == Date.class )
            {
                if ( defaultDateFormat == null )
                {
                    defaultDateFormat = DateFormat.getDateTimeInstance( DateFormat.LONG, DateFormat.LONG );
                    defaultDateFormat.setLenient( true );
                }
                synchronized ( defaultDateFormat )
                {
                    return (T) defaultDateFormat.parse( value );
                }
            }
            */
            else if ( to_class == URI.class )
            {
                return (T) new URI( value );  
            }
        }
        catch ( Exception ex )
        {
            throw new IllegalArgumentException(
                "Couldn't coerce '" 
                + value 
                + "' to "
                + to_class
                , ex
            );
        }

        //  fallback if not coerceable
        throw new UnsupportedOperationException(
            "Coercion to " 
            + to_class
            + " is not (yet?) supported"
        );
    }
    
    
    /**
    *   Attempts to derive an abbreviation for the given phrase, minus
    *   prepositional words like 'and', 'the', 'of', 'from', etc. A phrase
    *   consisting of a single word once prepositions are removed is 
    *   returned unchanged.
    *<br/>   
    *   Examples:
    *<ul>
    *   <li>"Journal of Biological Chemistry" becomes "J.B.C."</li>
    *   <li>"The house of the rising sun" becomes "H.R.S."</li>
    *   <li>"The quick brown fox jumped over the lazy hare" becomes "Q.B.F.J.O.L.H."</li> 
    *   <li>"The doctor" becomes "The doctor" (unchanged)
    *   <li>"Medicine" becomes "Medicine" (unchanged)
    *</ul>
    */
    public static final String guessAbbreviationFor( String phrase )
    {
        String[] words = phrase.toLowerCase().split("((\\b(and|of|the|to|a|from|in|&)\\b)\\s*)|\\s+");    
        
        if ( words.length == 0 ) 
            return phrase;
        
        if ( words.length == 1 ) 
            return phrase;
        
        char[] letters = new char[ words.length * 2 ]; 
        
        int count = 0;
        for ( String s : words )
        {
            if ( s.length() == 0 ) continue;
            
            letters[count++] = s.charAt( 0 );
            letters[count++] = '.';
        }
        
        if ( count == 2 ) 
            return phrase;
        
        return new String( letters ).toUpperCase();
    }

    
    /*  join  *//****************************************************
    *   
    *   Joins a list of strings (objects) by the given join string,
    *   as per the Perl function 'join'. Why this method isn't already
    *   in the language is a mystery. This method has the unusual form
    *   <tt>(String, Object, Object... )</tt> to eliminate compiler ambiguity
    *   when encountering code that uses <tt>join( String, Object[] )</tt>.
    *
    *   @param join_string 
    *   The string to be used for joining. 
    *   @param item
    *   The first object in the list of items to be joined.
    *   @param other_items
    *   A list of additional objects/strings to join.
    *   @return
    *   The joined string.
    */
    public static final String join( String join_string, Object item, Object... other_items )
    {
        if ( other_items.length == 0 )
            return item.toString();
        
        if ( other_items.length == 1 )
            return item.toString() + join_string + other_items[0];

        StringBuilder sb = new StringBuilder();
        sb.append( item );

        for ( int i = 0; i < other_items.length; i++ )
        {
            sb.append( join_string );
            sb.append( other_items[i] );
        }
        
        return sb.toString();
    }
    

    /*  join  *//****************************************************
    *   
    *   @see #join(String, Object, Object[])
    */
    public static final String join( String join_string, Object[] a )
    {
        //return join( join_string, c.toArray() );
        if ( a == null ) return null;
        if ( a.length == 0 ) return "";
        
        StringBuilder sb = new StringBuilder(); 
        sb.append( a[0] );

        for ( int i = 1; i < a.length; i++ )
        {
            sb.append( join_string );
            sb.append( a[i] );
        }
        
        return sb.toString();
    }
    
    /*
    public static final <Arg,Res> List<Res> 
    map( List<Arg> list, MapFunc<Arg,Res> func )  
    {
        if ( list == null ) 
            throw new IllegalArgumentException(
                "Argument 'list' can't be null");
    
        List<Res> newlist = new ArrayList<Res>( list.size() );
        for ( Arg a : list )
            newlist.add( func.process( a ) );
        
        return newlist;
    }

    public static final <A> List<A> 
    grep( List<A> list, MapFunc<A,Boolean> func )
    {
        if ( list == null ) 
            throw new IllegalArgumentException(
                "Argument 'list' can't be null");

        List<A> newlist = new ArrayList<A>( list.size() );
        for ( A a : list )
            if ( func.process( a ) )
                newlist.add( a );
            
        return newlist;
    }
    
    public interface MapFunc<Arg,Res> 
    {
        public Res process( Arg a );   
    }
    
    public interface GrepFunc<Arg> 
    {
        public boolean process( Arg a );   
    }
    */
    
    /*  join  *//****************************************************
    *   
    *   @see #join(String, Object[])
    */
    public static final String join( String join_string, Collection c )
    {
        return join( join_string, c.toArray() );
    }

    
    /*  join  *//****************************************************
    * 
    *   Joins the list of strings given by the objects in the passed Map
    *   by the given join strings, as per the Perl function 'join'. 
    *   
    *   For example, the Map -
    *   <pre>
    *   Map m = {           // java should have a declarative syntax for Maps...
    *       name => "Matt"
    *       age  => 32
    *   }
    *   </pre>
    *   
    *   when called as <code> join( m, ": ", ", " ) </code> would return 
    *   <code> "name: Matt, age: 32" </code> as a result. Note that the order
    *   of key-value pairs in the final string is arbitrary -- use the 4-argument
    *   form of this method if you want the keys to appear in a specific order
    *   or if you want only certain keys/values printed.
    *   
    *   @param map
    *   The map.
    *   @param join_string1 
    *   The string to use to join key-value pairs from the Map.
    *   @param join_string2
    *   The string to use between joined key-value pairs from the Map.
    *   @return
    *   The joined string.
    *   @see #join(String, Object[])
    */
    public static final String 
    join( Map map, String join_string1, String join_string2 )
    {
        return join( map, 
                     join_string1, 
                     join_string2, 
                     map.keySet().toArray() ); 
    }
    
    
    /*  join  *//****************************************************
    *   
    *   Same as the other join method for a Map, except the keys to be
    *   joined are explicitly given. No, this method doesn't check that 
    *   the given keys actually exist in the Map, that's your problem.
    *   
    *   @param map
    *   The map.
    *   @param join_string1 
    *   The string to use to join key-value pairs from the Map.
    *   @param join_string2
    *   The string to use between joined key-value pairs from the Map.
    *   @param keys
    *   An array which specifies which keys to be joined and in what order. 
    *   @return
    *   The joined string.
    *   @see #join(Map, String, String)
    */
    public static final String 
    join( Map map, String join_string1, String join_string2, Object[] keys )
    {
        if ( map == null || map.isEmpty() ) return "";

        StringBuilder sb = new StringBuilder( keys[0]
                                            + join_string1
                                            + map.get( keys[0] ) 
                                            );
        
        for ( int i = 1; i < map.size(); i++ )
        {
            sb.append( join_string2 
                    +  keys[i]
                    +  join_string1 
                    +  map.get( keys[i] ) 
                    );
        }
           
        return sb.toString();        
    }
    
    
    /*  repeat  *//**************************************************
    *   
    *   Returns a string that is formed by the repetition of a provided
    *   string a given number of times.
    *   
    *   @param string
    *   The string to be copied.
    *   @param repeat
    *   The number of times to repeat the given string.
    *   @return
    *   The joined string.
    *   @throws IllegalArgumentException
    *   if argument <tt>times</tt> is < 0 or passed string is null
    */
    public static final String repeat( String string, int times )
    {
        if ( times < 0 ) 
            throw new IllegalArgumentException(
                "Argument 'times' cannot be less than 0");
        if ( string == null ) 
            throw new IllegalArgumentException(
                "Argument 'string' cannot be null");

        if ( times == 1 ) return string;
        if ( times == 0 || string.length() == 0 ) return "";
        
        StringBuilder sb = new StringBuilder( string.length() * times );
        while ( times-- > 0 ) sb.append( string );
        
        return sb.toString();
    }


    /*  repeat  *//**************************************************
    *   
    *   Returns a string that is formed by the repetition of the given
    *   <tt>char</tt> the given number of times, eg:
    *<pre>
    *       //  returns "aaaaa"
    *       repeat('a', 5 );
    </pre>
    */
    public static final String repeat( char c, int times )
    {
        if ( times < 0 ) 
            throw new IllegalArgumentException(
                "Argument 'times' cannot be less than 0");
            
        if ( times == 0 ) return "";
        if ( times == 1 ) return Character.toString( c );
            
        char[] chars = new char[times];
        Arrays.fill( chars, c );
        
        return new String( chars );
    }

    
    /*  split  *//***************************************************
    *   
    *   Splits the given String by the given char, vaguely similar to the 
    *   <a href="http://perldoc.perl.org/functions/split.html">
    *   Perl function 'split'</a>.
    *<pre>
    *       //  returns [ "abc", "def", "", "ghi" ]
    *       split(';', "abc;def;;ghi")
    </pre>
    */
    public static final List<String> split( char c, String s )
    {
        List<String> strings = new ArrayList<String>();
        
        int start = 0;
        for ( int i = 0; i < s.length(); i++ )
        {
            if ( s.charAt( i ) != c )
                continue;
            
            strings.add( s.substring( start, i ) );
            start = i + 1;
        }
        
        if ( start <= s.length() )
            strings.add( s.substring( start ) );
        
        return strings;
    }
    
    
    /*  toCamelCase  *//*********************************************
    *<p>
    *   Converts underscore_case_like_this to camelCaseStrings. 
    *</p>
    *<p>
    *   <code>"a_name_like_this"</code> becomes <code>"aNameLikeThis"</code><br/>
    *   <code>"an_ame_like_this"</code> becomes <code>"anAmeLikeThis"</code><br/>
    *   <code>"aname_like_this"</code> becomes <code>"anameLikeThis"</code><br/>
    *   <code>"named_like_this"</code> becomes <code>"namedLikeThis"</code><br/>
    *   <code>"abba_was_great"</code> becomes <code>"abbaWasGreat"</code><br/>
    *   <code>"abcd"</code> becomes <code>"abcd"</code><br/>
    *   <code>"the_abba_collection"</code> becomes <code>"theAbbaCollection"</code><br/>
    *   <code>"the_abba_collection"</code> becomes <code>"theAbbaCollection"</code><br/>
    *   <code>"anamelikethis"</code> becomes <code>"anamelikethis"</code><br/>
    *   <code>"png_ase_f"</code> becomes <code>"pngAseF"</code><br/>
    *   <code>"po_li_ce_ma_n"</code> becomes <code>"poLiCeMaN"</code><br/>
    *   <code>"xml_parser"</code> becomes <code>"xmlParser"</code><br/>
    *   <code>"the_xml_parser"</code> becomes <code>"theXmlParser"</code><br/>
    *   <code>"ecma_script"</code> becomes <code>"ecmaScript"</code><br/>
    *</p>
    *<p>
    *   Note that the conversion from under_score to camelCase is not 
    *   necessarily reversible by the method toUnderscoreCase() although
    *   in most cases, it will be.
    *</p>
    */
    public static final String toCamelCase( String underscore_case )
    {
        if ( underscore_case == null ) return null;
        if ( underscore_case.length() == 0 ) return "";
        
        String[] pieces = underscore_case.split("_");
        if ( pieces.length == 1 ) return underscore_case;
        
        for ( int i = 1; i < pieces.length; i++ )
            pieces[i] = ucfirst( pieces[i] );
        
        return join( "", (Object[]) pieces );    
    }
    
    
    /*  toUnderscoreCase  *//****************************************
    *<p>
    *   Converts camelCaseStrings to underscore_case_like_this. 
    *</p>
    *<p>
    *   <code>"aNameLikeThis"</code> becomes <code>"a_name_like_this"</code><br/>
    *   <code>"ANameLikeThis"</code> becomes <code>"an_ame_like_this"</code><br/>
    *   <code>"AnameLikeThis"</code> becomes <code>"aname_like_this"</code><br/>
    *   <code>"NamedLikeThis"</code> becomes <code>"named_like_this"</code><br/>
    *   <code>"ABBA_wasGreat"</code> becomes <code>"abba_was_great"</code><br/>
    *   <code>"ABCD"</code> becomes <code>"abcd"</code><br/>
    *   <code>"TheABBAcollection"</code> becomes <code>"the_abba_collection"</code><br/>
    *   <code>"The_ABBA_Collection"</code> becomes <code>"the_abba_collection"</code><br/>
    *   <code>"anamelikethis"</code> becomes <code>"anamelikethis"</code><br/>
    *   <code>"PNGaseF"</code> becomes <code>"png_ase_f"</code><br/>
    *   <code>"PoLiCeMaN"</code> becomes <code>"po_li_ce_ma_n"</code><br/>
    *   <code>"XMLparser"</code> becomes <code>"xml_parser"</code><br/>
    *   <code>"theXMLparser"</code> becomes <code>"the_xml_parser"</code><br/>
    *   <code>"ECMAscript"</code> becomes <code>"ecma_script"</code><br/>
    *</p>
    *<p>
    *   Note that the conversion from camelCase to under_score is not 
    *   necessarily reversible by the method toCamelCase(), although
    *   in most cases, it will be.
    *</p>
    */
    public static final String toUnderscoreCase( String camelCase )
    {
        if ( camelCase == null ) return null;
        if ( camelCase.length() == 0 ) return "";
        
        String[] pieces = camelCase.split( "(?<=[^A-Z_])(?=[A-Z])|(?:(?<=[A-Z][A-Z])(?=[^A-Z_]))");
        return join( "_", pieces ).toLowerCase();    
    }
    
    
    /*  lcfirst  *//*************************************************
    *
    *   Lower-cases the first character of the passed string.
    */
    public static final String lcfirst( String s )
    {
        if ( s == null ) return null;
        if ( s.length() == 0 ) return "";
        
        if ( Character.isLowerCase( s.charAt( 0 ) ) ) return s;
        
        char[] chars = s.toCharArray();
        chars[0] = Character.toLowerCase( chars[0] );
        
        return new String( chars );
    }
    

    /*  ucfirst  *//*************************************************
    *
    *   Upper-cases the first character of the passed string.
    */
    public static final String ucfirst( String s )
    {
        if ( s == null ) return null;
        if ( s.length() == 0 ) return "";
        
        if ( Character.isUpperCase( s.charAt( 0 ) ) ) return s;
        
        char[] chars = s.toCharArray();
        chars[0] = Character.toUpperCase( chars[0] );
        
        return new String( chars );
    }
    
    /* paramToInt
    *  Convert a parameter object (either a single String, or an array of Strings)
    *  into an int
    */
    
    public static final int paramToInt(Object input)
    {            
        if (input.getClass().isArray()) {
            input = ((Object[]) input)[0];            
        }
        if (input instanceof String) {
            if (((String) input).length() == 0) {
                return 0;
            }
            return Integer.parseInt((String) input);
        }
        
        throw new IllegalArgumentException(
            "Argument 'input' is neither an array of strings, or a String");
    }
    
    
} // end class





