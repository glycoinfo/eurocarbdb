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
*   Last commit: $Rev: 1932 $ by $Author: glycoslave $ on $Date:: 2010-08-05 #$  
*/
package org.eurocarbdb.sugar;

//  3rd party imports 
import org.apache.log4j.Logger;

import org.eurocarbdb.sugar.SequenceFormat;

/**
*   This class implements glycosidic linkages (ie: linkages that occur
*   between monosaccharides).
*
*    Created 26-Sep-2005.
*   @author matt
*/
public class GlycosidicLinkage implements Linkage
{
    //~~~~~~~~~~~~~~~~~~~~~ STATIC FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /** Logging instance. */
    static final Logger log = Logger.getLogger( GlycosidicLinkage.class );
    
    public static final int UnknownTerminus = 0;
    
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~ FIELDS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private Anomer anomer;
    private int parentTerminus;
    private int childTerminus;
    
    //private Residue parent;
    //private Residue child;
    
    
    //~~~~~~~~~~~~~~~~~~~~~~ CONSTRUCTORS ~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /*  Constructor  *//*********************************************
    *
    *   Constructs a GlycosidicLinkage in which all elements of the
    *   linkage are unknown. Needed for hibernate.
    */
    public GlycosidicLinkage() 
    {
        this( Anomer.UnknownAnomer, UnknownTerminus, UnknownTerminus );
    }
    
    
    /*  Constructor  *//*********************************************
    *
    *   Construction of a linkage between a Monosaccharide on the
    *   non-reducing side to a Residue on the reducing side with 
    *   unknown anomeric configuration.
    */
    public GlycosidicLinkage( int parentTerminus, int childTerminus  ) 
    {
        if ( log.isDebugEnabled() )
        {
            log.debug(
                "creating new GlycosidicLinkage: parentTerminus=" 
                + parentTerminus 
                + ", childTerminus=" 
                + childTerminus 
            );
        }
        this.anomer = Anomer.UnknownAnomer;
        this.parentTerminus  = parentTerminus;
        this.childTerminus = childTerminus;
    }
    
    
    /*    Constructor  *//*******************************************
    *
    *   Construction of an explicit linkage.
    */
    public GlycosidicLinkage( Anomer a, int parentTerminus, int childTerminus  ) 
    {
        if ( log.isDebugEnabled() )
        {
            log.debug(
                "creating new GlycosidicLinkage: anomer=" 
                + a 
                + ", parentTerminus=" 
                + parentTerminus 
                + ", childTerminus=" 
                + childTerminus 
            );
        }
        this.anomer = a;
        this.parentTerminus  = parentTerminus;
        this.childTerminus = childTerminus;
    }
    
    
    /*/ * 
    *   Creates a new linkage based on the given prototype linkage,
    *   substituting default/null values as necessary for the new
    *   object to satisfy the requirements of this class.
    *-/
    public GlycosidicLinkage( Linkage prototype )
    {
        if ( prototype instanceof GlycosidicLinkage )
        {
            GlycosidicLinkage copy = (GlycosidicLinkage) prototype;
            this( copy.anomer, copy.parentTerminus, copy.childTerminus );   
        }
        else 
        {
            this( Anomer.None, 1, 1 ); 
        }
    }
    */
    
    //~~~~~~~~~~~~~~~~~~~~~~~~ METHODS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /*  getChildAnomer  *//******************************************
    *   
    *   Returns anomeric configuration.
    */
    public Anomer getChildAnomer()
    {
        return anomer;
    }
    
    
    /*  setChildAnomer  *//******************************************
    *   
    *   Sets anomeric configuration.
    */
    public void setChildAnomer( Anomer a )
    {
        assert a != null;
        this.anomer = a;    
    }
    
    
    public int getParentTerminus() {  return getReducingTerminus();  }
    
    
    public int getChildTerminus() {  return getNonReducingTerminus();  }
    
    
    public LinkageType getLinkageType()
    {
        return anomer.getType();    
    }
    
    
    /*  getReducingTerminus  *//*************************************
    *   
    *   Returns the reducing terminus' side's (parent's) terminus position.
    */
    public int getReducingTerminus()
    {
        return parentTerminus;
    }
        
    
    /*  getNonReducingTerminus  *//**********************************
    *   
    *   Returns non-reducing terminus' side's (child's) position.
    */
    public int getNonReducingTerminus()
    {
        return childTerminus;
    }
    
    
    public String toString()
    {
        //  temporary -- this should call a exporting method on a SequenceFormat object 
        return "[" 
            + getClass().getSimpleName()
            + "="
            + anomer 
            + (childTerminus > 0 ? childTerminus : "?") 
            + "-" 
            + (parentTerminus > 0 ? parentTerminus : "?") 
            + "]"
        ;
    }
    
    
    /*  implementation of PotentiallyIndefinite interface  */
    
    public boolean isDefinite()
    {
        return parentTerminus != UnknownTerminus
            && childTerminus != UnknownTerminus
        ;
    }
    
    
    /*  implementation of Linkage interface  */
    
    public double getMass()
    {
        return - Molecule.H2O.getMass();
    }

    
    public double getAvgMass()
    {
        return - Molecule.H2O.getAvgMass();
    }

    
    public String getName()
    {
        return this.toString();
    }

    
    public String getFullName()
    {
        return this.toString();
    }

    
    public String getType()
    {
        return this.getClass().toString();
    }
    
}





