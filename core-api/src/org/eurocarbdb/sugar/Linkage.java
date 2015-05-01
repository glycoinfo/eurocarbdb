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
*   Last commit: $Rev: 1561 $ by $Author: glycoslave $ on $Date:: 2009-07-21 #$  
*/

package org.eurocarbdb.sugar;

/**
*<p>
*   Describes the basic interface for the chemical linkages that
*   exist between chemical entities.
*</p>
*<p>
*   Linkages are modelled as Molecule objects in their own right,
*   to model the fact that the creation of any given chemical linkage
*   involves a distinct change in state and matter (mass). This 
*   state/matter change is encapsulated in this class.
*</p>
*    Created 06-Oct-2005.
*   @author matt
*/
public interface Linkage extends Molecule, PotentiallyIndefinite
{
    /** Indicates an unknown/uncertain terminus position. */
    public static final int Unknown_Terminus = 0; 
    
    
    /*  getParent  **************************************************
    *   @see org.eurocarbdb.Molecule#getParent()
    */
    //public Residue getParent();
    
    
    /*  getChild  *************************************************
    *   
    *   Returns the Molecule object regarded as the <em>child</em>
    *   in this Linkage. 
    */
    //public Residue getChild();
    
    public int getParentTerminus()
    ;
    
    
    public int getChildTerminus()
    ;

    
    public LinkageType getLinkageType()
    ;
}
