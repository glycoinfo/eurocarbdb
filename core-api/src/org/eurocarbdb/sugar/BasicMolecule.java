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

package org.eurocarbdb.sugar;

import java.util.List;

/**
*
*   An implementation of the Molecule interface for simple
*   chemical entities.
*
*    Created 22-Sep-2005.
*   @author matt
*
*/
public class BasicMolecule implements Molecule
{
    //  OBJECT FIELDS  //--------------------------------------------
    
    /**  Monoisotopic mass.  */
    protected double mass;
    
    /** Average mass.  */
    protected double avg_mass;
    
    /** Canonical sequence name/identifier for this entity.  */
    protected String name;
    
    /** Full chemical name/description of this entity.  */
    protected String full_name;
    
    /** Canonical type of this entity.  */
    protected String type;
    
    /** Chemical composition.  */
    protected Composition composition;
    
//    /** Attached parent entity, if any.  */
//    protected Molecule parent;
//    
//    /** Child entities attached to this entity, if any.  */
//    protected List<? extends Molecule> children;
    
    
    //  CONSTRUCTORS  //---------------------------------------------
    
    protected BasicMolecule()
    {
        //  intnetionally empty; only here so subclasses can call super().
    }
    
    
    /*  Constructor  *//*********************************************
    *
    *   For package-private use.
    */
    public BasicMolecule( String full_name, String name,
                        String type, Composition c, 
                        double mass, double avg_mass  )
    {
        this.avg_mass  = avg_mass;
        this.full_name = full_name;
        this.mass = mass;
        this.name = name;
        this.type = type;
        this.composition = c;
        
//        this.parent = null;
//        this.children = null;
    }

    
    /*  @see Molecule#getMass()  */
    public double getMass()
    {
        //  TODO: implement a calculation of mass from elemental 
        //  composition here if mass <= 0.
        return mass;
    }

    /*  @see Molecule#getAvgMass()  */
    public double getAvgMass()
    {
        //  TODO: implement a calculation of mass from elemental 
        //  composition here if mass <= 0.
        return avg_mass;
    }

    /*  @see Molecule#getName()  */
    public String getName()
    {
        assert( name != null );
        return name;
    }

    /*  @see Molecule#getFullName()  */
    public String getFullName()
    {
        return full_name;
    }

    /*  @see Molecule#getType()  */
    public String getType()
    {
        return type;
    }

    /*  @see Molecule#getComposition()  */
    public Composition getComposition()
    {
        //  compositions will most likely be given or obtained
        //  at startup from a reference file/DB table.
        return composition;
    }

//    /*  @see Molecule#attach(Molecule)  */
//    public Linkage attach( Molecule child )
//    {
//        //  trivial implementation, will change for sure.
//        return new SimpleLinkage( this, child );
//    }
//
//
//
//    public Molecule[] getChildren()
//    {
//        return this.children.toArray( new Molecule[ children.size() ] );
//    }
//
//
//
//    public Molecule getParent()
//    {
//        return this.parent;
//    }
//


    public Composition getElementalComposition()
    {
        //  TODO: recursive traversal through tree of objects
        //  to sum all elemental compositions found. probably 
        //  calculate this on the fly?
        return null;
    }


}
