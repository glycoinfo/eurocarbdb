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
*   Last commit: $Rev: 1429 $ by $Author: glycoslave $ on $Date:: 2009-07-05 #$  
*/

package org.eurocarbdb.util;

import java.math.BigInteger;
import java.io.Serializable;
import java.util.Set;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Collection;
import java.util.RandomAccess;
import java.util.ArrayList;

/**
*<p>
*   Implementation of {@link Set} interface for {@link Integer}s backed by 
*   a bitset and optimised for speed and smallest memory consumption.
*   This class supports both Set-oriented as well as bitmask/bitstring-oriented 
*   usages. Note that while working with bitstrings, that the 0th index is on 
*   the right, and that iteration over a BitSet effectively traverses from 
*   right to left (ie: lowest to highest index). Lastyle, BitSets silently 
*   grow as needed to accomodate large indexes. {@link #iterator Regular iteration} 
*   over the Set of integers (whose indexes are set to true), and removal of values
*   during iteration is also supported.
*</p>
*<p>
*   Unlike {@link java.lang.BitSet}, this class is extremely memory efficient, 
*   consuming memory only for the actual size of the bitset in bits + 2. 
*   While bitsets are resized on demand, the mephasis on compactness makes 
*   it very worthwhile to pre-size a bitset to or near its expected final 
*   size prior to use, via {@link #ensureCapacity} or the {@link #BitSet(int)} 
*   constructor.
*</p>
*<p>
*   This class is slightly slower than {@link java.lang.BitSet} but uses less 
*   memory in all cases. Compared to a {@link java.util.HashSet} of integers, 
*   this class is much faster and consumes much less memory for all usages 
*   (see {@link test.eurocarbdb.util.BitSetTest} for benchmarks if you're 
*   interested).
*</p>
*
*   @see java.util.BitSet
*   @see java.util.Set
*   @author mjh
*/
public class BitSet implements Set<Integer>, Iterable<Integer>, RandomAccess, Cloneable, Serializable
{
    /** the bitset, resized as needed. */
    private boolean[] bits = null;
    
    /** if true, any operations that require resizing of this bitset 
    *   will throw exceptions. */
    private boolean fixedSize = false;
    
    /** if true, prevents any/all size or value changes to the BitSet */
    private boolean unmodifiable = false;
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /** Creates a bitset of zero size. */
    public BitSet()
    {
        this( 0 );   
    }
    
    
    /** Creates a bitset of the given size. */
    public BitSet( int size )
    {
        bits = new boolean[ size ];
    }
    
    
    // /** Creates a bitset of the given size that will never be resized. */
    // public BitSet( int size, boolean fixedSize )
    // {
    //     this( size );
    //     this.fixedSize = fixedSize;
    // }
    
    
    /** Creates a new bitset initialised (copied) from the passed bit array. */
    public BitSet( boolean[] bits )
    {
        // this.bits = Arrays.copyOf( bits, bits.length );
        boolean[] newbits = new boolean[ bits.length ];
        System.arraycopy( bits, 0, newbits, 0, bits.length );
        this.bits = newbits;
    }
    
    
    /**
    *   Creates a new bitset initialised (copied) from the passed bit array
    *   from the given 'from' index (inclusive), up to the given 'to' index
    *   (exclusive). The {@link #length()} of the bitset is thus: 
    *   <code>to_index - from_index</code>. 
    */
    public BitSet( boolean[] bits, int from_index, int to_index )
    {
        int length = to_index - from_index;
        if ( length < 0 )
            throw new IllegalArgumentException(
                "to_index must be greater than from_index");
            
        boolean[] newbits = new boolean[ length ];
        System.arraycopy( bits, from_index, newbits, 0, length );
        this.bits = newbits;   
    }
    
    
    /** Used internally to create a null bitset */
    private BitSet( String dummy ) {}
    

    //~~~~~~~~~~~~~~~~~~~~~~~~ STATIC METHODS ~~~~~~~~~~~~~~~~~~~~~~~
    
    /** 
    *   Creates a bitset initialised to the given number converted to binary. 
    *   Eg: 
    *<pre>
    *       BitSet.forNumber( 7 ).toBitString() == "111"  
    *       BitSet.forNumber( 8 ).toBitString() == "1000"  
    *       BitSet.forNumber( 19 ).toBitString() == "10011"  
    *</pre>
    */
    public static BitSet forNumber( Number n )
    {
        String bitstring;
        if ( n instanceof BigInteger )
        {
            bitstring = ((BigInteger) n).toString( 2 );
        }
        else
        {
            bitstring = Long.toBinaryString( n.longValue() );
        }
        return forString( bitstring );
    }
    
    
    /** 
    *   Creates a bitset initialised to the given bitstring 
    *   @throws IllegalArgumentException if the given String contains
    *   anything but ones and zeroes.
    */
    public static BitSet forString( String bitstring )
    {
        BitSet b = new BitSet( bitstring.length() );
        char[] chars = bitstring.toCharArray();
        
        int i = 0;
        for ( int j = chars.length - 1; j >= 0; j-- )
        {
            switch ( chars[j] )
            {
                case '1':
                    b.bits[i++] = true;
                    break;
                    
                case '0':
                    b.bits[i++] = false;
                    break;
            
                default:
                    throw new IllegalArgumentException(
                        "invalid char '"
                        + chars[j]
                        + "' for bitset string at index="
                        + j
                        + ", only 1's and 0's allowed"
                    );
            }
        }       
        
        return b;
    }
    
    
    /** Creates a bitset consisting of the given integers set to true. */
    public static BitSet of( int... ints )
    {
        BitSet b = new BitSet( "dummy" );
        b.addAll( ints );
        return b;
    }
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~    
    
    /** Adds the given integer to the set (ie: sets that bit to true). */
    public boolean add( Integer i )
    {
        ensureCapacity( i + 1 );
        if ( bits[i] )
            return false;
        
        bits[i] = true;
        return true;
    }
    
    
    /** 
    *   Adds the given collection of integers to the set (ie: sets each of 
    *   the integer positions to true). 
    */
    public boolean addAll( Collection<? extends Integer> indexes )
    {
        int highest = 0;
        for ( Integer i : indexes )
        {
            if ( i.compareTo( highest ) > 0 )
                highest = i;                
        }
        
        ensureCapacity( highest + 1 );
        
        boolean changed = false;
        for ( int i : indexes )
        {
            if ( ! bits[i] )
            {
                bits[i] = true;
                changed |= true;
            }
        }
        
        return changed;
    }
    
    
    /** 
    *   Adds the given list of integers to the set (ie: sets each of 
    *   the integer positions to true). 
    */
    public void addAll( int... ints )
    {
        int highest = 0;
        for ( int i : ints )
        {
            if ( i > highest )
                highest = i;                
        }
        ensureCapacity( highest + 1 );
                
        for ( int i : ints )
            bits[i] = true;
    }
    
    
    /**
    *   Appends the given BitSets to this BitSet end-to-end, as if 
    *   each argument given were serialised to a bitstring and appended
    *   in reverse order.
    *   eg:
    *<pre>
    *       BitSet b1 = BitSet.forString("1111");
    *       BitSet b2 = BitSet.forString("000");
    *       BitSet b3 = BitSet.forString("11");
    *
    *       b1.append( b2, b3 );
    *       b1.toBitString();
    *
    *       //  returns "110001111"
    *</pre>   
    *   Note that zeroes in high bits (ie: left padding 
    *   with zeroes in the string example given) are preserved.
    */
    public void append( BitSet... bitsets )
    {
        int size = this.bits.length;
        for ( BitSet b : bitsets )
            size += b.bits.length;

        int i = this.bits.length;
        ensureCapacity( size );
        
        for ( BitSet b : bitsets )
        {
            // System.out.println("appending " + b.toBitString() + " to " + this.toBitString() + ": " + size );
            System.arraycopy( b.bits, 0, this.bits, i, b.bits.length );
            i += b.bits.length;
        }
    }
    
    
    /** 
    *   Returns the allocated length of this bitset; note this is not
    *   the same thing as {@link #size()}. capacity() and size() will only 
    *   be equal if all bits in this bitset are set to true. 
    *
    *   @see #length()
    */
    public int capacity()
    {
        return bits.length;   
    }
    
    
    /** Empties this set (sets all bits to false). */
    public void clear()
    {
        Arrays.fill( bits, false );
    }
    
    
    /** Sets the given bit index to false, removing it from the set. */
    public void clear( int i )
    {
        bits[i] = false;  
    }
    
    
    /** 
    *   Sets the bits from from_index (inclusive) to to_index (exclusive) to false. 
    *   The number of bits cleared is thus: <code>to_index - from_index</code>.
    */
    public void clear( int from_index, int to_index )
    {
        for ( int i = from_index; i < to_index; i++ )
            bits[i] = false;
    }
    
    
    /** Clones this set. */
    public BitSet clone()
    {
        BitSet b = new BitSet( bits.length );
        System.arraycopy( bits, 0, b.bits, 0, bits.length ); 
        b.fixedSize = this.fixedSize;
        return b;
    }
    
    
    /** 
    *   This method throws a {@link ClassCastException} unless the 
    *   passed Object is a {@link Number} 
    */
    public boolean contains( Object x )
    {
        return contains( ((Number) x).intValue() );    
    }
    
    
    /** Returns true if the passed integer index is true in this bitset. */
    public boolean contains( Integer i )
    {
        return ( i < bits.length ) ? bits[i] : false;    
    }
    
    
    /** 
    *   This method throws a {@link ClassCastException} unless the 
    *   passed Collection contains only {@link Number}s. 
    */
    public boolean containsAll( Collection<?> indexes )
    {
        int i;
        for ( Object x : indexes )
        {
            i = ((Number) x).intValue();
            if ( i >= bits.length || ! bits[i] )
                return false;
        }
        return true;
    }

    
    /** 
    *   Returns true if all set bits in the current bitset 
    *   are also set to true in the given bitset; The allocated
    *   length of each bitset is *not* considered. 
    */
    public boolean equals( BitSet b )
    {
        if ( this == b )
            return true;
        
        if ( bits.length == b.bits.length )
        {
            return Arrays.equals( this.bits, b.bits );    
        }
        else 
        {
            boolean[] shorter = bits, longer = b.bits;
            if ( bits.length > b.bits.length )
            {
                shorter = b.bits;
                longer  = bits;
            }
            
            for ( int i = 0; i < shorter.length; i++ )
                if ( shorter[i] != longer[i] )
                    return false;
            
            for ( int i = shorter.length; i < longer.length; i++ )
                if ( longer[i] )
                    return false;
         
            return true;
        }
    }
    
    
    /** Lengthens this BitSet to accomodate at least the size given. */
    public void ensureCapacity( int size )
    {
        if ( bits == null )
        {
            bits = new boolean[size];
            return;
        }
        
        if ( size != 0 && size <= bits.length )    
            return;
        
        // if ( fixedSize || unmodifiable )
        if ( fixedSize )
            throw new UnsupportedOperationException(
                "Cannot modify allocated size of fixed size BitSet");
        
        boolean[] newbits = new boolean[size];
        System.arraycopy( bits, 0, newbits, 0, bits.length );
        bits = newbits;
    }
    
    
    /** Sets the value of the bit at the given index to its complement. */
    public void flip( int i )
    {
        ensureCapacity( i + 1 );
        bits[i] ^= true;
    }


    /** 
    *   Returns the value of the bit at the given position. 
    *   @throws ArrayIndexOutOfBoundsException if given index is 
    *   negative, or equal to/greater than {@link #length()}.
    */
    public boolean get( int i )
    throws ArrayIndexOutOfBoundsException 
    {
        return bits[i];    
    }
    
    
    /** 
    *   Returns true if none of the bits in this bitset are set 
    *   (ie: it is an empty set). 
    */
    public boolean isEmpty()
    {
        for ( boolean b : bits )
            if ( b )
                return false;
            
        return true;
    }
    

    /** 
    *   Returns true if all of the bits in this bitset are set. 
    */
    public boolean isFull()
    {
        for ( boolean b : bits )
            if ( ! b )
                return false;
            
        return true;
    }

    
    /** 
    *   Returns an iterator over all integers in this bitset whose position
    *   in the bitset is set to true, in sorted (ascending) order (lowest
    *   to highest index). Calling {@link Iterator.remove} sets the bit value 
    *   of the current index to false.
    */
    public Iterator<Integer> iterator()
    {
        return new Iterator<Integer>()
            {
                int i = 0;
                
                public boolean hasNext() 
                {  
                    while ( i < bits.length ) 
                    {
                        if ( bits[i] )
                            return true;
                    
                        i++; 
                    }  
                    
                    return false;
                }
                
                public Integer next()    
                {
                    while ( i < bits.length ) 
                    {
                        if ( bits[i] )
                        {
                            int j = i;
                            i++;
                            return j;
                        }
                            
                        i++; 
                    }  

                    throw new java.util.NoSuchElementException(
                        "hasNext not called or next called twice in a row");
                }
                
                public void remove()
                {
                    if ( ! bits[i] )
                        throw new IllegalStateException(
                            "hasNext not called or remove called twice in a row");

                    bits[i] = false;   
                }
            } // end anon inner class
        ; 
    }
    
    
    /** Same as {@link #capacity()}. */
    public int length()
    {
        return bits.length;   
    }
    
    
    /** 
    *   Assumes the given Object argument is a numeric value; otherwise throws
    *   a {@link ClassCastException}.
    *
    *   @throws ClassCastException if argument cannot be cast to a {@link Number}
    *   @see java.lang.Number.intValue()
    */
    public boolean remove( Object x )
    {
        int i = ((Number) x).intValue();      
        if ( i >= bits.length || ! bits[i] )
            return false;
        
        bits[i] = false;
        return true;
    }
    
    
    /** 
    *   Removes all numeric values in the passed collection from this set
    *   (ie: sets all those bits to false).
    *   @throws ClassCastException 
    *       if any element of collection cannot be cast to a {@link Number}
    */
    public boolean removeAll( Collection<?> indexes )
    {
        boolean changed = false;
        for ( Object x : indexes )
        {
            //  throwing a ClassCastException is part of the method spec
            int i = ((Number) x).intValue();                
            if ( i >= bits.length || ! bits[i] )
                continue;
            
            bits[i] = false;
            changed = true;
        }
        
        return changed;
    }
    
    
    /** 
    *   {@inheritDoc} 
    *
    *   @throws ClassCastException 
    *       if any element of collection cannot be cast to an {@link Integer}
    */
    public boolean retainAll( Collection<?> indexes )
    {
        BitSet b;
        if ( indexes instanceof BitSet )
        {
            b = (BitSet) indexes;
        }
        else    
        {
            b = new BitSet( indexes.size() );
            b.addAll( (Collection<Integer>) indexes ); // ClassCastEx allowed to propagate
        }
        
        return bitwiseAnd( b );
    }
    
    
    /** Sets the bit at the given index to true. */
    public void set( int i )
    {
        ensureCapacity( i + 1 );
        bits[i] = true;    
    }


    /** Sets the bit at the given index to the given boolean value. */
    public void set( int i, boolean value )
    {
        ensureCapacity( i + 1 );
        bits[i] = value;    
    }

    
    /** 
    *   Sets the bits from from_index (inclusive) to to_index (exclusive) to true. 
    *   The number of bits set is thus: <code>to_index - from_index</code>.
    */
    public void set( int from_index, int to_index )
    {
        ensureCapacity( to_index );
        for ( int i = from_index; i < to_index; i++ )
            bits[i] = true;
    }
    
    
    /** 
    *   Returns the current size of this bitset, that is, the number of 
    *   integers whose position in the bitset is true. Note: this is not 
    *   the same thing as the {@link #length()} of the bitset.
    */
    public int size()
    {
        int size = 0;
        for ( boolean b : bits )
            if ( b )
                size++;
        return size;
    }
    
    
    /** Returns this bitset as an array of {@link Integer}s. */
    public <T> T[] toArray( T[] a )
    {
        int ones = 0;
        for ( boolean b : bits )
            if ( b )
                ones++;
        
        Integer[] ints = new Integer[ones];
        
        ones = 0;
        for ( int i = 0; i < bits.length; i++ )
            if ( bits[i] )
                ints[ones++] = i;
        
        return (T[]) ints;
    }
    
    
    /** Returns this bitset as an array of {@link Integer}s. */
    public Object[] toArray()
    {
        return toArray( new Integer[0] );    
    }
    
    
    //  bit ops

    /** Sets this bitset to <tt>this AND b</tt> (this & b). */
    public boolean bitwiseAnd( BitSet b )
    {
        boolean[] shorter = bits, longer = b.bits; // note: initial order important
        if ( shorter.length > longer.length )
        {
            shorter = b.bits;
            longer = bits;
        }   
        
        boolean changed = false;
        
        //  process common bits
        for ( int i = 0; i < shorter.length; i++ )
        {
            changed |= (bits[i] && ! b.bits[i]);
            bits[i] &= b.bits[i];            
        }
        
        //  process remaining bits only if we are longer, since 
        //  if we are shorter, higher bits are absent and therefore false by definition.
        if ( this.bits == longer ) // only true if there was a size difference at the start
        {
            for ( int i = shorter.length; i < longer.length; i++ )
            {
                if ( bits[i] )
                {
                    bits[i] = false;
                    changed = true;
                }
            }
        }
        
        return changed;
    }
    
    
    /** Returns a new bitset that is equal to <tt>this AND b</tt> (this & b). */
    public BitSet bitwiseAndEquals( BitSet b )
    {
        BitSet copy = clone();
        copy.bitwiseAnd( b );    
        return copy;
    }
    
    
    /** Sets this bitset to the complement of the current bitset. */
    public void bitComplement()
    {
        for ( int i = 0; i < bits.length; i++ )
            bits[i] = ! bits[i]; 
    }
    
    
    /** Returns a bitset equal to the complement of the current bitset. */
    public BitSet bitComplementEquals()
    {
        BitSet copy = clone();
        copy.bitComplement();    
        return copy;
    }
    
    
    /** 
    *   Shifts this bitset in the given direction - positive values 
    *   are equivalent to a right (<code>this &gt;&gt; direction</code>) 
    *   shift, negative values are equivalent to a left shift 
    *   (<code>this &lt;&lt; direction</code>). 
    */
    public void bitShift( int direction )
    {
        boolean[] newbits = _shift( bits, direction );
        bits = newbits;
    }
    
    
    /** Returns a new bitset that is equal to a bitshift in the given direction. */
    public void bitShiftEquals( int direction )
    {
        boolean[] newbits = _shift( bits, direction ); 
        BitSet b = new BitSet( newbits );
    }
    
    
    private final boolean[] _shift( boolean[] bit_source, int direction )
    {
        if ( direction == 0 )
            return bit_source;
        
        int length = bit_source.length - direction;
        boolean[] bit_dest = new boolean[ length ];            
        
        if ( direction > 0 )
        {
            //  right shift
            System.arraycopy( bit_source, direction, bit_dest, 0, length );     
        }
        else // ( direction is < 0 )
        {
            //  left shift
            System.arraycopy( bit_source, 0, bit_dest, -direction, bit_source.length );
        }
        
        return bit_dest;
    }
    

    /** 
    *   Inserts the given BitSet at the given (bitstring) index, shifting
    *   all bits at or above this index to the left by the length() of the
    *   given BitSet.
    */
    public void bitShiftInsert( int index, BitSet b )
    {
        int length = bits.length + b.bits.length;
        boolean[] newbits = new boolean[length];
        
        if ( index > 0 )
            System.arraycopy( bits, 0, newbits, 0, index );
        
        System.arraycopy( b.bits, 0, newbits, index, b.bits.length );
        System.arraycopy( bits, index, newbits, index + b.bits.length, bits.length - index );
        
        this.bits = newbits;
    }
    
    
    /** 
    *   Returns a new BitSet consisting of bits copied from this BitSet,
    *   in the range <tt>from_index</tt> (inclusive), to <tt>to_index</tt> 
    *   (exclusive). The length of the returned BitSet is given by 
    *   <code>to_index - from_index</code>. Note that index 0 of 
    *   a bitstring is on the right hand side.
    *   Eg:
    *<pre>
    *       BitSet b = BitSet.forString("01100110011");
    *
    *       //  prints "1100"
    *       out.println( b.bitSlice( 2, 6 ).toBitString() );
    *
    *       //  prints "0011"
    *       out.println( b.bitSlice( 0, 4 ).toBitString() );
    *    
    *</pre>   
    */
    public BitSet bitSlice( int from_index, int to_index )
    {
        return new BitSet( bits, from_index, to_index );      
    }
    
    
    /**
    *   Returns a new BitSet that consists of only the bits that are
    *   boolean true in the passed BitSet, with intervening false bits
    *   filtered out. The {@link #length()} of the bitset returned is 
    *   equal to the {@link #size()} of the given bitset (ie: 
    *   <code>indexes.size()</code>). Similarly, slicing a bitset with
    *   itself returns a bitset equal to its own {@link size()}, in 
    *   which all bits are set to true. 
    *   Eg:
    *<pre>
    *       BitSet b = BitSet.forString("01100110011");
    *
    *       //  prints "011"
    *       BitSet indexes_to_slice1 = BitSet.forString("111");
    *       out.println( b.bitSlice( indexes_to_slice1 ).toBitString() );
    *
    *       //  prints "101"
    *       BitSet indexes_to_slice2 = BitSet.forString("10101");
    *       out.println( b.bitSlice( indexes_to_slice2 ).toBitString() );
    *    
    *       //  prints "111111"
    *       out.println( b.bitSlice( b ).toBitString() );
    *</pre>   
    */
    public BitSet bitSlice( BitSet indexes )
    {
        BitSet slice = new BitSet( indexes.size() );
        int i = 0;
        for ( int index : indexes )
            slice.bits[i++] = this.bits[index];
        
        return slice;
    }
    
    
    /** Sets this bitset to <tt>this OR b</tt> (this | b). */
    public boolean bitwiseOr( BitSet b )
    {
        boolean[] longer = bits, shorter = b.bits; 
        if ( shorter.length > longer.length )
        {
            shorter = bits;
            longer = b.bits;
        }   
        
        boolean changed = false;
        
        //  process common bits
        for ( int i = 0; i < shorter.length; i++ )
        {
            changed |= (bits[i] && ! b.bits[i]);
            bits[i] |= b.bits[i];            
        }
        
        if ( this.bits == shorter ) // only true if there was a size difference at the start
        {
            ensureCapacity( longer.length );
            for ( int i = shorter.length; i < longer.length; i++ )
            {
                changed |= b.bits[i];
                bits[i] = b.bits[i];
            }
        }
        
        return changed;
    }
    

    /** Returns a new bitset that is equal to <tt>this OR b</tt> (this | b). */
    public BitSet bitwiseOrEquals( BitSet b )
    {
        BitSet copy = clone();
        copy.bitwiseOr( b );    
        return copy;
    }
    
    
    /** Sets this bitset to <tt>this XOR b</tt> (this ^ b). */
    public void bitwiseXor( BitSet b )
    {
        boolean[] longer = bits, shorter = b.bits; 
        if ( shorter.length > longer.length )
        {
            shorter = bits;
            longer = b.bits;
        }   
        
        //  process common bits
        for ( int i = 0; i < shorter.length; i++ )
        {
            bits[i] ^= b.bits[i];            
        }

        //  process remaining bits        
        if ( this.bits == shorter ) 
            ensureCapacity( longer.length );
        
        for ( int i = shorter.length; i < longer.length; i++ )
        {
            bits[i] ^= b.bits[i];
        }
    }
    

    /** Returns a new bitset that is equal to <tt>this XOR b</tt> (this ^ b). */
    public BitSet bitwiseXorEquals( BitSet b )
    {
        BitSet copy = clone();
        copy.bitwiseXor( b );    
        return copy;
    }
    
    
    /** 
    *   Returns the index of the highest set bit in this BitSet, or -1 if
    *   no bits are set. 
    */
    public int highestSetBit()
    {
        for ( int i = bits.length - 1; i >= 0; i-- )
            if ( bits[i] )
                return i;
            
        return -1;
    }
    
    
    /** 
    *   Returns the value of this BitSet as an integer, (ie: converts binary 
    *   bitstring to base10) truncating bits equal to or greater than 
    *   {@link Integer.SIZE}. 
    */
    public int intValue()
    {
        int intValue = 0;
        int length = (bits.length > Integer.SIZE) ? Integer.SIZE : bits.length;
        
        for ( int i = 0; i < length; i++ )
            if ( bits[i] )
                intValue |= (1 << i);
        
        return intValue;
    }
    
    
    /** 
    *   Returns the index of the lowest set bit in this BitSet, or -1 if
    *   no bits are set. 
    */
    public int lowestSetBit()
    {
        for ( int i = 0; i < bits.length; i++ )
            if ( bits[i] )
                return i;
            
        return -1;
    }
    
    
    /** 
    *   Returns a string representation of this bitset of form 
    *   <tt>"{1, 3, 5}"</tt>, containing only those integers set
    *   to true.
    */
    public String toString()
    {
        StringBuilder sb = new StringBuilder( bits.length * 2 );
        sb.append('{');
        
        Iterator<Integer> it = iterator();
        
        if ( it.hasNext() )
        {
            sb.append( it.next() );
            while ( it.hasNext() )
            {
                sb.append( ", " );
                sb.append( it.next() );
            }
        }
        sb.append('}');
        
        return sb.toString();
    }
    
    
    /** 
    *   Returns this BitSet as an array of booleans. Modifications to
    *   this array are not reflected in this bitset. 
    */
    public boolean[] toBitArray()
    {
        return clone().bits;
    }
    
    
    /** Returns this bitset as a bitstring of length {@link #length()}. */
    public String toBitString()
    {
        // char[] string = new char[ bits.length ];
        char[] string = new char[ bits.length ];
        int len = string.length - 1;
        
        for ( int i = 0; i < string.length; i++ )
            string[len--] = bits[i] ? '1' : '0';
        
        return new String( string );
    }    
    
}
