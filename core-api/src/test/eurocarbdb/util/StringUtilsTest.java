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
*   Last commit: $Rev: 1181 $ by $Author: glycoslave $ on $Date:: 2009-06-10 #$  
*/

package test.eurocarbdb.util;

import java.util.*;
import java.net.URI;

import org.testng.Assert;
import org.testng.annotations.*;

import static java.lang.System.out;
import static org.eurocarbdb.util.StringUtils.*;


@Test
(   
    groups={"util.strings"}
)
public class StringUtilsTest 
{
    static String[] stringarray = {
        "a",
        "bb",
        "ccc",
        "dddd",
        "eeeee"
    };
    
    static List<String> stringlist = Arrays.asList( stringarray ); 
    

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~ TESTS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    @Test
    public void coercePrimitive()
    {
        int i = coerce( "234", Integer.class ); 
        System.err.println("Integer: expected '234', got " + i );
        assert i == 234;
        
        long l = coerce( "234", Long.class );
        System.err.println("Long: expected '234', got " + l );
        assert l == 234L;
        
        float f = coerce( "234.567", Float.class );
        System.err.println("Float: expected '234.567', got " + f );
        assert f == 234.567f;
        
        double d = coerce( "234.567", Double.class );
        System.err.println("Double: expected '234.567', got " + d );
        assert d == 234.567;

        boolean b = coerce( "false", Boolean.class );        
        System.err.println("Boolean: expected 'false', got " + b );
        assert b == false;
    }
    
  
    @Test
    public void coerceComplex()
    {
        String s = "http://www.eurocarbdb.org";
        URI uri = coerce( s, URI.class ); 
        System.err.println("URI: text was '" + s + "', got " + uri );
        assert uri != null;
        
        s = "file://home/matt/signature.txt";
        uri = coerce( s, URI.class ); 
        System.err.println("URI: text was '" + s + "', got " + uri );
        assert uri != null;
        
        s = "/home/matt/signature.txt";
        uri = coerce( s, URI.class ); 
        System.err.println("URI: text was '" + s + "', got " + uri );
        assert uri != null;
        
        //  others...
        /* Date parsing is iffy...
        Date d3 = coerce( "12:03:06 Dec 15, 2008", Date.class );        
        System.err.println("Date: text was 'Dec 15, 2008', got " + d3 );
        assert d3 != null;
        */
    }    
    
    
    @Test
    public void joinSingleton()
    {
        compare(   
            "single item", 
            join( "-", "single item" ),
            "single item join failed for string" 
        );
        
        compare(   
            "111", 
            join( "-", 111 ),
            "single item join failed for integer" 
        );
    }
    
    @Test
    public void joinArray()
    {
        compare(   
            "abbcccddddeeeee", 
            join( "", stringarray ),
            "array join failed for join string '' (empty string)" 
        );
        
        compare(   
            "a|bb|ccc|dddd|eeeee", 
            join( "|", stringarray ),
            "array join failed for join string '|'"
        );
        
        compare(   
            "a___bb___ccc___dddd___eeeee", 
            join( "___", stringarray ),
            "array join failed for join string '___'" 
        );
    }

    @Test
    public void joinList()
    {
        compare(   
            "abbcccddddeeeee", 
            join( "", stringlist ),
            "list join failed for join string '' (empty string)" 
        );
            
        compare(   
            "a|bb|ccc|dddd|eeeee", 
            join( "|", stringlist ),
            "list join failed for join string '|'" 
        );
        
        compare(   
            "a___bb___ccc___dddd___eeeee", 
            join( "___", stringlist ),
            "list join failed for join string '___'" 
        );
    }
    
    @Test
    public void joinVararg()
    {
        compare(   
            "abc", 
            join("", "a", "b", "c" ),
            "vararg join failed for join string ''" 
        );

        compare(   
            "ham, cheese", 
            join(", ", "ham", "cheese" ),
            "vararg join failed for 2 item list" 
        );

        compare(   
            "ham, cheese, sandwich", 
            join(", ", "ham", "cheese", "sandwich" ),
            "vararg join failed for 3 item list" 
        );
            
        compare(   
            "ham, cheese, tomato, sandwich", 
            join(", ", "ham", "cheese", "tomato", "sandwich" ),
            "vararg join failed for 4 item list"
        );
    }
    
    @Test
    public void joinHeterogeneous()
    {
        compare(   
            "2 turtle doves", 
            join( " ", 2, "turtle doves" ),
            "vararg join failed for heterogeneous 2 item list"
        );

        compare(   
            "3 french hens", 
            join( " ", 3, "french", "hens" ),
            "vararg join failed for heterogeneous 3 item list"
        );

        compare(   
            "a69aabc", 
            join( "", 'a', 69, "a", 'a', 'b', 'c' ),
            "vararg join failed for heterogeneous list and join string ''" 
        );

        compare(   
            "a2692a21222322", 
            join( "2", 'a', 69, "a", 1, 2, 3, "", "" ),
            "vararg join failed for heterogeneous list and join string '2'" 
        );

        compare(   
            "a_69_a_abc", 
            join( "_", 'a', 69, 'a', "abc" ),
            "vararg join failed for heterogeneous multi-joined list" 
        );

        compare(   
            "axbxc_1+2+3_--zomg--", 
            join( "_", 
                join( "x", 'a', 'b', 'c' ),
                join( "+", 1, 2, 3 ),
                join( "-", "", "", "zomg", "", "" )
            ),
            "vararg join failed for heterogeneous multi-joined list" 
        );
    }


    @Test
    public void joinHash()
    {
        Map<Object,Object> hash = new TreeMap<Object,Object>();
        hash.put( "a", 1 );

        compare(   
            "a1", 
            join( hash, "", "" ),
            "hash join failed for single item zero-length join strings"
        );

        compare(   
            "a=1", 
            join( hash, "=", ";" ),
            "hash join failed for single item zero-length join strings"
        );
        
        hash.put( "bb", 2 );

        compare(   
            "a=1; bb=2", 
            join( hash, "=", "; " ),
            "hash join failed for single item zero-length join strings"
        );

        hash.put( "ccc", 3 );
        
        compare(   
            "a1bb2ccc3", 
            join( hash, "", "" ),
            "hash join failed for zero-length join strings"
        );

        compare(   
            "a:1, bb:2, ccc:3", 
            join( hash, ":", ", " ),
            "hash join failed"
        );
        
        compare(   
            "a==1___bb==2___ccc==3", 
            join( hash, "==", "___" ),
            "hash join failed"
        );
    }
    
    
    @Test
    public void repeatString()
    {
        compare( "", repeat( "", 3 ) );
        
        compare( "", repeat( "x", 0 ) );
        
        compare( "x", repeat( "x", 1 ) );
        
        compare( "xx", repeat( "x", 2 ) );
        
        compare( "xxx", repeat( "x", 3 ) );

        compare( "__|__|", repeat( "__|", 2 ) );
        
        compare( "__|__|__|", repeat( "__|", 3 ) );        
    }
 
    @Test
    public void repeatChar()
    {
        compare( "", repeat( 'x', 0 ) );

        compare( "x", repeat( 'x', 1 ) );
                
        compare( "xx", repeat( 'x', 2 ) );
        
        compare( "xxx", repeat( 'x', 3 ) );
        
        compare( "222", repeat( '2', 3 ) );
    }
    
    
    @Test( dependsOnMethods={"joinList"} )
    public void stringSplit()
    {
        String s;
        List<String> l;
        
        s = "abc;def;ghi";
        l = split( ';', s );
        out.println( "split: " + l );
        compare( s, join( ";", l ) );
        
        s = "abc;;def;;;ghi";
        l = split( ';', s );
        out.println( "split: " + l );
        compare( s, join( ";", l ) );
        
        s = ";;;abc;def;;";
        l = split( ';', s );
        out.println( "split: " + l );
        compare( s, join( ";", l ) );

        s = ";;;;;";
        l = split( ';', s );
        out.println( "split: " + l );
        compare( s, join( ";", l ) );
    }    
    
    /*
    @Test
    public void testMap()
    {
        List<Integer> lengths = map(  stringlist, 
            new MapFunc<String,Integer>() { 
                public final Integer process( String s ) { 
                    return s.length(); 
                } 
            } 
        );
     
        assert lengths != null && lengths.size() == 5;
        assert lengths.get(0) == 1;
        assert lengths.get(1) == 2;
        assert lengths.get(2) == 3;
    }
    
    @Test
    public void testGrep()
    {
        List<String> strings5plus = grep( stringlist, 
                                        new GrepFunc<String>() { 
                                            public final boolean process( String s ) { 
                                                return s.length() > 4; 
                                            } 
                                        } 
                                    );
        
        assert strings5plus != null && strings5plus.size() == 1;
        assert strings5plus.get(0) == "eeeee";
    }
    */
 
    /*
    public static void main( String[] args )
    {
        
        Object[] stuff = { "string1", 123, 'Z', "string2", "string3" }; 
        System.err.println( join( "<join>", stuff ));
        
        List list = Arrays.asList( stuff );
        System.err.println( join( "<join>", list ) );

        Map<Object, Object> hash = new HashMap<Object, Object>();
        hash.put( 'A', 2 );
        hash.put( 23, 'A' );
        hash.put("java", "sucks");
        System.err.println( join( hash, ":", ", " ) );        
        
        hash.put("a list", stuff );
        System.err.println( join( hash, ":", ", " ) );        
        
        
        String[] camel_strings = {  
            "aNameLikeThis",
            "ANameLikeThis", 
            "AnameLikeThis",
            "NamedLikeThis",
            "ABBA_wasGreat", 
            "ABCD",          
            "TheABBAcollection",
            "The_ABBA_Collection", 
            "anamelikethis",
            "PNGaseF",       
            "PoLiCeMaN",        
            "XMLparser",        
            "theXMLparser",        
            "ECMAscript"  
        };
        
        String[] uscore_strings = new String[ camel_strings.length ];
        
        System.err.println( "--- camelCase to underscore_case ---" );
        for ( int i = 0; i < camel_strings.length; i++ )
        {
            uscore_strings[i] = toUnderscoreCase( camel_strings[i] ); 
            
            System.err.println( "    *   <code>\"" 
                                + camel_strings[i]
                                + "\"</code> becomes <code>\"" 
                                + uscore_strings[i]
                                + "\"</code><br/>" 
                                );
        }
        
        System.err.println( "--- underscore_case to camelCase ---" );
        for ( int i = 0; i < uscore_strings.length; i++ )
        {
            camel_strings[i] = toCamelCase( uscore_strings[i] );
            
            System.err.println( "    *   <code>\"" 
                                + uscore_strings[i]
                                + "\"</code> becomes <code>\"" 
                                + camel_strings[i]
                                + "\"</code><br/>" 
                                );
        }
      
        
    }
    */

    private final void compare( String expected, String result ) 
    { 
        compare( expected, result, null ); 
    }
    
    private final void compare( String expected, String result, String errormsg )
    {
        System.err.println("expected '" + expected + "'");
        System.err.println("received '" + result + "'");
        assert expected.equals( result ) 
             : (errormsg != null ? (errormsg + ": ") : "") 
             + "expected '" 
             + expected 
             + "', got '" 
             + result 
             + "'"
             ;
    }

}



