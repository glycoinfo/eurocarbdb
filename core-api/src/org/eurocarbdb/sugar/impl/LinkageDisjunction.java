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

package org.eurocarbdb.sugar.impl;

// import java.util.Set;
// import java.util.HashSet;
import java.util.BitSet;
import java.util.EnumSet;

import org.apache.log4j.Logger;

import org.eurocarbdb.sugar.Anomer;
import org.eurocarbdb.sugar.Linkage;
import org.eurocarbdb.sugar.LinkageType;
import org.eurocarbdb.sugar.SugarSequence;
import org.eurocarbdb.sugar.SequenceFormat;
// import org.eurocarbdb.sugar.GlycosidicLinkage;

import static org.eurocarbdb.util.JavaUtils.checkNotNull;


public class LinkageDisjunction //implements Linkage
{
    /** logging handle */
    static Logger log = Logger.getLogger( LinkageDisjunction.class );
    
    /** bitmask of parent-side (non-reducing) terminal positions. */
    private BitSet parentTerminii = new BitSet( 8 );
    
    /** bitmask of child-side (reducing) terminal positions. */
    private BitSet childTerminii = new BitSet( 8 );
    
    /** bitmask of linkage types */
    private EnumSet<LinkageType> linkageTypes = EnumSet.noneOf( LinkageType.class );
    
    
    public LinkageDisjunction()
    {
    }
    
    
    public LinkageDisjunction( Linkage... linkages )
    {
        for ( Linkage lk : linkages )
        {
            add( lk );
        }
    }

    
    public void add( Linkage lk )
    {
        checkNotNull( lk );
        
        //  monitor for linkages that may already be in this alternate
        boolean warrants_changes = false;
/*         
        //  resolve potential anomer conflicts -
        //  if anomers don't match, anomer becomes unknown
        if ( anomer == null )
        {
            will_change = true;
            anomer = lk.getAnomer();   
        }
        else
        {
            if ( anomer != lk.getAnomer() )
            {
                will_change = true;
                anomer = Anomer.Unknown;
            }
        }

*/
        //  check linkage types
        LinkageType type = lk.getLinkageType();
        if ( ! linkageTypes.contains( type ) )
            warrants_changes = true;
            
        //  check terminii to see if different
        int p = lk.getParentTerminus();
        int c = lk.getChildTerminus();
        
        if ( ! (parentTerminii.get(p) && childTerminii.get(c)) )
            warrants_changes = true;
            
        if ( warrants_changes )
        {
            //  add terminii
            parentTerminii.set( p );
            childTerminii.set( c );
    
            //  add linkage type
            linkageTypes.add( type );
        }
        else
        {
            //  not sure that adding a duplicate linkage is a big deal,
            //  just warn for now.
            /*
            throw new throw new IllegalArgumentException(
                "Linkage '" 
                + lk 
                + "' already exists in linkage alternate '"
                + this
                + "': cannot add duplicates"
            );
            */
            log.warn(
                "duplicate linkage '" 
                + lk
                + "' being added to alternate"
            );
        }
    }

    
    private int parentTerminus = 0;
    private int childTerminus = 0;
    
    public int getParentTerminus()
    {
        return (parentTerminii.cardinality() == 1)
            ? parentTerminii.length() - 1
            : Linkage.Unknown_Terminus;
    }
    
    
    public int getChildTerminus()
    {
        return (childTerminii.cardinality() == 1)
            ? childTerminii.length() - 1
            : Linkage.Unknown_Terminus;
    }

    
    public LinkageType getLinkageType()
    {
        return null;
    }
    
    
    public int[] getParentTerminii()
    {
        return _bitset_to_list( parentTerminii );
    }
    

    public int[] getChildTerminii()
    {
        return _bitset_to_list( childTerminii );
    }
    
    
    /** Returns current number of linkage alternates */
    public int size()
    {
        int size = parentTerminii.cardinality() 
            + childTerminii.cardinality() 
            - 2;
            
        return ( size < 0 ) ? 0 : size; 
    }
    
    
/*     public String toString()
    {
        StringBuffer sb = new StringBuffer();
        
        sb.append( join("|", linkageTypes ) );
        sb.append( join("|", getParentTerminii() ) );
        sb.append('-');
        sb.append( join("|", getChildTerminii() ) );
        
        return join("|", alternates );    
    }
*/    
    
    //  private methods
    
    private final int[] _bitset_to_list( BitSet bs )
    {
        int[] terminii = new int[ parentTerminii.cardinality() ];
        
        int i = 0;
        int term = bs.nextSetBit(0);
    
        for( ; term >= 0; term = bs.nextSetBit( term + 1 ) )
            terminii[i++] = term;
        
        return terminii;
    }
    
}

