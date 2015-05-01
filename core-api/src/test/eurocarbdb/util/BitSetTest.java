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
*   Last commit: $Rev: 1182 $ by $Author: glycoslave $ on $Date:: 2009-06-10 #$  
*/

package test.eurocarbdb.util;

import java.util.Set;
import java.util.HashSet;
import org.apache.log4j.Logger; 
import org.apache.log4j.Level; 

import org.testng.annotations.*;

import org.eurocarbdb.util.BitSet;

import static java.lang.System.out;
import static org.eurocarbdb.util.StringUtils.join;

/**
*<p>
*   Tests basic {@link BitSet} operations; test group is 'util.bitset'.
*</p>
*
*<h2>Performance</h2>
*<p>
*   The benchmarking method, {@link #bitsetBenchmark}, which compares 
*   performance of this class versus {@link java.util.BitSet}
*   and a {@link HashSet} of Integersis disabled by default,
*   and needs to be manually enabled in the source to be run. 
*</p>
*<p>
*   However, these are the typcial results for my machine (2.5GHz core duo macbook pro):
*<pre>
*    iterations = 100000, size = 16
*    add to bitset: 53 msec
*    add to hashset: 82 msec
*    add to java bitset: 16 msec
*    bitset: 70 msec
*    hashset: 282 msec
*    javabitset: 57 msec
*    
*    iterations = 1000000, size = 16
*    add to bitset: 105 msec
*    add to hashset: 804 msec
*    add to java bitset: 130 msec
*    bitset: 667 msec
*    hashset: 2677 msec
*    javabitset: 420 msec
*    
*    iterations = 100000, size = 32
*    add to bitset: 33 msec
*    add to hashset: 159 msec
*    add to java bitset: 24 msec
*    bitset: 132 msec
*    hashset: 520 msec
*    javabitset: 83 msec
*    
*    iterations = 100000, size = 64
*    add to bitset: 66 msec
*    add to hashset: 330 msec
*    add to java bitset: 52 msec
*    bitset: 269 msec
*    hashset: 1024 msec
*    javabitset: 170 msec
*    
*    iterations = 100000, size = 128
*    add to bitset: 248 msec
*    add to hashset: 813 msec
*    add to java bitset: 152 msec
*    bitset: 800 msec
*    hashset: 2549 msec
*    javabitset: 550 msec    
*</pre> 
*   The first 3 results of each test are for add operations only, the 
*   second set of 3 results are for an add, contains and delete operation.  
*</p>
*<p>
*   It should be noted that this BitSet class uses a fraction of the memory
*   of a HashSet and is also always smaller than a java.util.BitSet 
*   for all values.
*</p>
*
*   @author mjh
*/
@Test( groups="util.bitset", sequential=true, timeOut=2000 )
public class BitSetTest
{
    @Test
    public void bitsetCreate()
    {
        BitSet b = new BitSet( 4 );
        
        System.out.println("empty set:");
        assert b.size() == 0;
        print( b );
        
        System.out.println("add 2");
        b.add( 2 );
        print( b );
        assert b.size() == 1;
        
        System.out.println("add 7");
        b.add( 7 );
        print( b );
        assert b.size() == 2;
        
        System.out.println("add 5");
        b.add( 5 );
        print( b );
        assert b.size() == 3;
        
        System.out.println("add 5 again");
        b.add( 5 );
        print( b );
        assert b.size() == 3;
        
        System.out.println("add 25");
        b.add( 25 );
        print( b );
        assert b.size() == 4;

        System.out.println("add a set:");
        BitSet a = new BitSet( 3 );
           
        a.addAll( 1, 2, 3 );
        
        System.out.print("set to add = ");
        print( a );
        assert a.size() == 3;
        
        b.addAll( a );
        System.out.print("resulting set = ");
        print( b );
        assert b.size() == 6;
    }


    @Test
    public void bitsetContains()
    {
        BitSet a = BitSet.of( 1, 3, 5, 7 );
        print( a );
        
        boolean b;
        
        b = a.contains( 1 );
        System.out.println("contains 1: " + b );
        assert b;
        
        b = a.contains( 3 );
        System.out.println("contains 3: " + b );
        assert b;

        b = a.contains( 5 );
        System.out.println("contains 5: " + b );
        assert b;
        
        b = a.contains( 2 );
        System.out.println("contains 2: " + b );
        assert ! b;

        b = a.contains( 8 );
        System.out.println("contains 8: " + b );
        assert ! b;
        
        b = a.contains( 0 );
        System.out.println("contains 0: " + b );
        assert ! b;
        
    }
    
    
    @Test
    public void bitsetAnd()
    {
        BitSet a = BitSet.of( 1, 3, 5, 7 );
        BitSet b = BitSet.of( 0, 2, 4, 6, 8 );
        BitSet c = BitSet.of( 1, 2, 3, 4, 5, 6, 7, 8 );
        
        System.out.print("a = ");
        print( a );

        System.out.print("b = ");
        print( b );
        
        System.out.print("c = ");
        print( c );
        
        assert a.equals( a );
        assert b.equals( b );
        assert c.equals( c );
        
        System.out.print("a intersect a: ");
        BitSet d = a.bitwiseAndEquals( a );
        print( d );
        assert d.equals( a );

        System.out.print("a intersect b (should be empty set): ");
        BitSet e = a.bitwiseAndEquals( b );
        print( e );
        assert e.size() == 0;
        assert e.isEmpty();
        
        System.out.print("a intersect c: ");
        BitSet f = a.bitwiseAndEquals( c );
        print( f );
        assert f.size() == 4;
        assert f.equals( a );
        
        System.out.print("b intersect c: ");
        BitSet g = b.bitwiseAndEquals( c );
        print( g );
        assert b.size() == 5;
        assert g.size() == 4; // : "b == " + b + "; c == " + c + "; g == " + g;
        assert ! g.equals( b );// : "b == " + b + "; c == " + c + "; intersect == " + g;
        
        System.out.print("(a union b) intersect c: ");        
        BitSet h = new BitSet( a.size() + b.size() );
        h.addAll( a );
        h.addAll( b );
        h.retainAll( c );
        print( h );
        assert h.equals( c );
    }   
    

    @Test
    public void bitsetOr()
    {
        BitSet a = BitSet.of( 1, 3, 5, 7 );
        BitSet b = BitSet.of( 0, 2, 4, 6, 8 );
        BitSet c = BitSet.of( 1, 2, 3, 4, 5, 6, 7, 8 );
        
        System.out.print("a = ");
        print( a );

        System.out.print("b = ");
        print( b );
        
        System.out.print("c = ");
        print( c );
        
        assert a.equals( a );
        assert b.equals( b );
        assert c.equals( c );
        
        System.out.print("a union a: ");
        BitSet d = a.bitwiseOrEquals( a );
        print( d );
        assert a.equals( d );
        
        System.out.print("a union b: ");
        BitSet e = a.bitwiseOrEquals( b );
        print( e );
        assert ! e.equals( a );
        assert ! e.equals( b );
        assert e.size() == 9;
        
        System.out.print("a union c: ");
        BitSet f = a.bitwiseOrEquals( c );
        print( f );
        assert f.equals( c );
        assert f.size() == 8;
        
        System.out.print("b union c: ");
        BitSet g = b.bitwiseOrEquals( c );
        print( g );
        assert ! g.equals( c );
        assert g.size() == 9;
    }


    @Test
    public void bitsetXor()
    {
        BitSet a = BitSet.of( 1, 3, 5, 7 );
        BitSet b = BitSet.of( 0, 2, 4, 6, 8 );
        BitSet c = BitSet.of( 1, 2, 3, 4, 5, 6, 7, 8 );
        
        System.out.print("a = ");
        print( a );

        System.out.print("a ^ a = ");
        BitSet a_xor = a.bitwiseXorEquals( a );
        print( a_xor );
        
        System.out.print("a ^ a ^ a = ");
        BitSet a_xor_xor = a_xor.bitwiseXorEquals( a );
        print( a_xor_xor );
        assert a.equals( a_xor_xor );
        
        // System.out.print("b = ");
        // print( b );
        
        // System.out.print("c = ");
        // print( c );
    }
    
    
    @Test( dependsOnMethods={"bitsetString"} )
    public void bitsetShift()
    {
        //  left shift
        String bitstring1 = "110011";
        BitSet a = BitSet.forString( bitstring1 );

        System.out.println( "test bitstring  : " + a.toBitString() );
        assert a.toBitString().equals( bitstring1 );
        
        a.bitShift( -2 );
        System.out.println( "left shift << 2 : " + a.toBitString() );
        assert a.toBitString().equals( bitstring1 + "00" );

        System.out.println();
        
        //  right shift
        String bitstring2 = "110011";
        BitSet b = BitSet.forString( bitstring2 );

        System.out.println( "test bitstring  : " + b.toBitString() );
        assert b.toBitString().equals( bitstring2 );
        
        b.bitShift( 2 );
        System.out.println( "right shift >> 2: " + b.toBitString() );
        assert b.toBitString().equals( "1100" );

        System.out.println();

        //  shift + insert
        BitSet c = new BitSet( 5 );
        c.bitComplement();
        System.out.println( "before insert   : " + c.toBitString() );
        assert c.toBitString().equals( "11111" );
        
        c.bitShiftInsert( 2, new BitSet( 2 ) );
        System.out.println( "after insert    : " + c.toBitString() );
        assert c.toBitString().equals( "1110011" );
    }
    
    
    @Test
    public void bitsetString()
    {
        String bitstring1 = "1001101001110111";
        
        System.out.println( "test bitstring: " + bitstring1 );
        BitSet a = BitSet.forString( bitstring1 );
        System.out.println( a );
        System.out.println( a.toBitString() );

        assert a.size() == 10;
        assert a.length() == 16;
        assert a.toBitString().equals( bitstring1 );
    
        System.out.println();
        
        String bitstring2 = "0000";
        
        System.out.println( "test bitstring: " + bitstring2 );
        BitSet b = BitSet.forString( bitstring2 );
        System.out.println( b );
        System.out.println( b.toBitString() );

        assert b.size() == 0;
        assert b.length() == 4;
        assert b.toBitString().equals( bitstring2 );
    
    }    
    
    
    
    /** 
    *   Compares performance of this class versus {@link java.util.BitSet}
    *   and a {@link HashSet} of Integers; test is disabled by default.
    */
    @Test( enabled=false )
    public void bitsetBenchmark()
    {
        runBenchmark( 100000, 16 );
        runBenchmark( 1000000, 16 );
        runBenchmark( 100000, 32 );
        runBenchmark( 100000, 64 );
        runBenchmark( 100000, 128 );
        runBenchmark( 100000, 512 );
    }
    
    
    private void runBenchmark( int iterations, int size )
    {
        System.out.println();
        System.out.println("iterations = " + iterations + ", size = " + size );
        
        BitSet bitset = new BitSet( size * 2 );
        Set<Integer> hashset = new HashSet<Integer>( size * 2 );
        java.util.BitSet javabitset = new java.util.BitSet( size * 2 ); 
        
        int[] odds = new int[size], evens = new int[size];
        for ( int i = 0, j = 0; i < size; i++ )
        {
            odds[j]  = i * 2 + 1; 
            evens[j] = i * 2;
            j++;
        }
        long start;
        
        //  add
        start = now();      
        for ( int j = 0; j < iterations; j++ )
            for ( int i : odds )
                bitset.add( i );           
        System.out.println("add to bitset: " + (now() - start) + " msec");
        
        //  add
        start = now();      
        for ( int j = 0; j < iterations; j++ )
            for ( int i : odds )
                hashset.add( i );           
        System.out.println("add to hashset: " + (now() - start) + " msec");

        
        start = now();      
        for ( int j = 0; j < iterations; j++ )
            for ( int i : odds )
                javabitset.set( i );           
        System.out.println("add to java bitset: " + (now() - start) + " msec");
        
        
        //  add/contains/remove
        boolean pointless;
        start = now();      
        for ( int j = 0; j < iterations; j++ )
        {
            for ( int i : odds )
                bitset.add( i );
                // bitset.set( i );
            
            for ( int i : odds )
                pointless = bitset.contains( i );

            for ( int i : odds )
                bitset.remove( i );
                // bitset.clear( i );
        }
        System.out.println("bitset: " + (now() - start) + " msec");
        
        
        start = now();      
        for ( int j = 0; j < iterations; j++ )
        {
            for ( int i : odds )
                hashset.add( i );
            
            for ( int i : odds )
                pointless = hashset.contains( i );

            for ( int i : odds )
                hashset.remove( i );
        }
        System.out.println("hashset: " + (now() - start) + " msec");

        
        start = now();      
        for ( int j = 0; j < iterations; j++ )
        {
            for ( int i : odds )
                javabitset.set( i );
            
            for ( int i : odds )
                pointless = javabitset.get( i );

            for ( int i : odds )
                javabitset.clear( i );
        }
        System.out.println("javabitset: " + (now() - start) + " msec");
        
    }
    
    
    private static final long now()
    {
        return System.currentTimeMillis();   
    }
    
    
    private void print( BitSet b )
    {
        System.out.println();
        System.out.print("set: ");
        System.out.println( b );
        System.out.print("bitstring: ");
        System.out.println( b.toBitString() );
        System.out.println();
    }
} // end class
