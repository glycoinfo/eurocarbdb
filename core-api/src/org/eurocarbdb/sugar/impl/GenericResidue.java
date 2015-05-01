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
*   Last commit: $Rev: 1208 $ by $Author: glycoslave $ on $Date:: 2009-06-12 #$  
*/

package org.eurocarbdb.sugar.impl;

import java.util.List;

import org.eurocarbdb.sugar.*;

/**
*
*   An implementation of the Molecule interface for simple
*   chemical entities.
*
*    Created 22-Sep-2005.
*   @author matt
*
*/
public class GenericResidue extends BasicMolecule implements Residue
{
    //  OBJECT FIELDS  //--------------------------------------------
        
//    /** Attached parent entity, if any.  */
//    protected Molecule parent;
//    
//    /** Child entities attached to this entity, if any.  */
//    protected List<? extends Molecule> children;
    
    
    //  CONSTRUCTORS  //---------------------------------------------
    
    public GenericResidue( String name )
    {
        this.name = name;
    }
    
    
    /*  Constructor  *//*********************************************
    *
    *   For package-private use.
    */
    public GenericResidue( 
        String full_name, String name,
        String type, Composition c, 
        double mass, double avg_mass  )
    {
        super( full_name, name, type, c, mass, avg_mass  );
    }

    
    public void attach( Molecule m, int position ) {}
    
    public Molecule unattach( int position ) { return null; }
    
    public String toString()
    {
        return "[" + getClass().getSimpleName() + "=" + getName() + "]";
    }
    
} // end class
