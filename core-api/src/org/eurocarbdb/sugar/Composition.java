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
*   Last commit: $Rev: 1231 $ by $Author: glycoslave $ on $Date:: 2009-06-19 #$  
*/

package org.eurocarbdb.sugar;

import java.util.*;
import static org.eurocarbdb.util.StringUtils.join;
import static org.eurocarbdb.util.JavaUtils.checkNotNull;

/**
*   Simple implementation of elemental and/or entity-level chemical
*   compositions.
*
*    Created 20-Sep-2005.
*   @author matt
*/
public class Composition<E extends Molecule>
{
    public static final Composition<?> UnknownComposition = new Composition();
    
    /** String used to join element to count in the string representation 
    *   of this composition. 
    */
    public static String ELEMENT_TO_COUNT_SEPARATOR = ":";
    
    /** String used to join element:count pairs in the string representation 
    *   of this composition.
    */
    public static String ELEMENT_TO_ELEMENT_SEPARATOR = ";";
    
    /**  The internal hash that maps elements to their respective count. */
    protected Map<E, Integer> comp; 
    
    
    //  CONSTRUCTORS  //---------------------------------------------
    
    /*  Constructor  *//*********************************************
    *   
    *   Generic constructor.
    */
    public Composition()
    {
        comp = new HashMap<E, Integer>();
    }
    

    /** 
    *   Add the given molecule to the current composition, raising
    *   the count of that molecule by one.
    */    
    public Composition<E> add( E molecule )
    {
        checkNotNull( molecule );
        
        Integer i = this.comp.get( molecule );
        int count = (i != null) ? i.intValue() : 0;
        this.comp.put( molecule, new Integer( count + 1 ) );
    
        return this;
    }
    
    
    public Composition<E> add( Composition<E> c )
    {
        checkNotNull( c );
        
        for ( E m : c.comp.keySet() )   
        {
            Integer i = this.comp.get( m );
            int count = (i != null) ? i.intValue() : 0;
            this.comp.put( m, new Integer( count ) );
        }
        
        return this;
    }
    
    
    /*  Delegated methods from HashMap comp  */
    /*  most of the below methods are delegated/renamed methods of */
    /*  the Map object underpinning the Composition class.         */
    
    /*  containsElement  *//*****************************************
    *   
    *   Returns true if the given element is found in this
    *   composition.
    *   
    *   @see java.util.Map#containsKey(java.lang.Object)
    */
    public boolean containsElement( Molecule element )
    {
        return comp.containsKey( element );
    }

    
    /*  equals  *//**************************************************
    *   
    *   @see java.lang.Object#equals()
    */
    public <T> boolean equals( Composition<E> c )
    {
        return comp.equals( c );
    }

    
    /*  elementCount  *//********************************************
    *   
    *   Returns the count of the given element in this composition.
    *   
    *   @see java.util.Map#get(java.lang.Object)
    */
    public Integer elementCount( Molecule element )
    {
        return comp.get( element );
    }

    
    /*  isEmpty  *//*************************************************
    *   
    *   Returns true if this composition is empty, that is, it contains
    *   no elements.
    */
    public boolean isEmpty()
    {
        return comp.isEmpty();
    }

    
    /*  elementSet  *//**********************************************
    *   
    *   Returns all elements in this composition as a Set.
    */
    public Set<E> elementSet()
    {
        return comp.keySet();
    }

    
    /*  removeElement  *//*******************************************
    *   
    *   Removes the given element from this composition.
    */
    public void removeElement( Molecule element )
    {
        comp.remove( element );
    }

    
    /*  size  *//****************************************************
    *   
    *   Returns the numbers of unique elements in this composition.
    */
    public int size()
    {
        return comp.size();
    }
    
    
    /*  hashCode  *//************************************************
    * 
    *   @see java.lang.Object#hashCode()
    */
    public int hashCode()
    {
        return comp.hashCode();
    }

    
    @Override
    /*  toString  *//************************************************
    *   
    *   Returns a simple string representation of the current composition.
    *   
    *   String representation takes the following form:
    *   <pre>
    *       (element_1) 
    *       + ELEMENT_TO_COUNT_SEPARATOR
    *       + (count_1)
    *       ...
    *       + ELEMENT_TO_ELEMENT_SEPARATOR
    *       + (element_n)
    *       + ELEMENT_TO_COUNT_SEPARATOR
    *       + (count_n)
    *   </pre>
    *   
    *   @see #ELEMENT_TO_COUNT_SEPARATOR
    *   @see #ELEMENT_TO_ELEMENT_SEPARATOR
    *   @see java.lang.Object#toString()
    *   
    */
    public String toString()
    {
        return join(  comp, 
                      ELEMENT_TO_COUNT_SEPARATOR, 
                      ELEMENT_TO_ELEMENT_SEPARATOR  );
    }


} // end class Composition



